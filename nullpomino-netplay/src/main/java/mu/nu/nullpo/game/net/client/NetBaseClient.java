/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package mu.nu.nullpo.game.net.client;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.net.NetCmd;
import mu.nu.nullpo.game.net.NetMessage;
import mu.nu.nullpo.game.net.NetUtil;

/**
 * Client(Basic part)
 */
@Log4j
public class NetBaseClient extends Thread {

	/** default Port of number */
	public static final int DEFAULT_PORT = 9200;

	/** The size of the read buffer */
	public static final int BUF_SIZE = 2048;

	/** Default ping interval (1000=1s) */
	public static final int PING_INTERVAL = 5 * 1000;

	/**
	 * This countOnlypingIf there is no reaction even hit the automatic
	 * disconnection
	 */
	public static final int PING_AUTO_DISCONNECT_COUNT = 6;

	/** whether this thread is running */
	@Getter
	protected volatile boolean running;

	/** Socket for connection */
	protected Socket socket;

	/** Destination host */
	protected String host;

	/** Destination port number */
	protected int port;

	/** IP address */
	protected String ip;

	/** Previous incomplete packet */
	protected StringBuilder notCompletePacketBuffer;

	/** Interface receiving messages */
	protected List<NetMessageListener> listeners = new LinkedList<>();

	/** pingHit count(From serverpongReset When a message is received) */
	protected int pingCount;

	/** AutomaticpingHitTimer */
	protected Timer timerPing;

	/**
	 * Default constructor
	 */
	public NetBaseClient() {
		this(null, DEFAULT_PORT);
	}

	/**
	 * Constructor
	 *
	 * @param host Destination host
	 */
	public NetBaseClient(String host) {
		this(host, DEFAULT_PORT);
	}

	/**
	 * Constructor
	 *
	 * @param host Destination host
	 * @param port Destination port number
	 */
	public NetBaseClient(String host, int port) {
		super("NET_" + host + (port == DEFAULT_PORT ? "" : ":" + port));
		this.host = host;
		this.port = port;
	}

	/*
	 * Processing of the thread
	 */
	@Override
	public void run() {
		running = true;
		log.info("Connecting to " + host + ":" + port);

		Throwable exDisconnectReason = null;
		// Connection
		try (Socket s = new Socket(host, port)) {
			socket = s;
			ip = socket.getInetAddress().getHostAddress();

			// pingHitTimerPreparation
			startPingTask(PING_INTERVAL);

			// Message reception
			byte[] buf = new byte[BUF_SIZE];
			int size;

			while (running && (size = socket.getInputStream().read(buf)) != -1) {
				String message = new String(buf, 0, size, StandardCharsets.UTF_8);

				// The various processing depending on the received message
				StringBuilder packetBuffer = new StringBuilder();
				if (notCompletePacketBuffer != null) {
					packetBuffer.append(notCompletePacketBuffer);
				}
				packetBuffer.append(message);

				int index;
				while ((index = packetBuffer.indexOf("\n")) != -1) {
					String msgNow = packetBuffer.substring(0, index);
					if (!msgNow.isBlank()) {
						processPacket(msgNow);
					}
					packetBuffer = packetBuffer.delete(0, index + 1);
				}

				// If there is an incomplete packet
				if (!packetBuffer.isEmpty()) {
					notCompletePacketBuffer = packetBuffer;
				} else {
					notCompletePacketBuffer = null;
				}
			}
		} catch (Exception e) {
			log.info("Socket disconnected", e);
			exDisconnectReason = e;
		}

		if (timerPing != null) {
			timerPing.cancel();
		}
		running = false;

		// Listener
		for (int i = 0; i < listeners.size(); i++) {
			try {
				listeners.get(i).netOnDisconnect(Optional.ofNullable(exDisconnectReason));
			} catch (Exception e2) {
				log.debug("Uncaught Exception on NetMessageListener #" + i + " (disconnect event)", e2);
			}
		}
	}

	public void close() {
		running = false;
	}

	/**
	 * The various processing depending on the received message
	 *
	 * @param fullMessage Received Messages
	 * @throws IOException If there are any errors
	 */
	private final void processPacket(String fullMessage) throws IOException {
		NetMessage message;
		try {
			String[] data = fullMessage.split("\t"); // Tab delimited
			message = NetMessage.of(data);
			handleMessage(message);
		} catch (Exception e) {
			log.error("failed to process message", e);
			log.debug("original: " + fullMessage);
			return;
		}

		// ListenerCall
		for (int i = 0; i < listeners.size(); i++) {
			try {
				listeners.get(i).netOnMessage(message);
			} catch (Exception e) {
				log.error("Uncaught Exception on NetMessageListener #" + i + " (message event)", e);
			}
		}
	}

	protected void handleMessage(NetMessage message) {
		// pingReply
		if (message.command() == NetCmd.PONG) {
			if (pingCount >= PING_AUTO_DISCONNECT_COUNT / 2) {
				log.debug("pong " + pingCount);
			}
			pingCount = 0;
		}
	}

	/**
	 * Send a message to the server
	 *
	 * @param bytes Message to be sent
	 * @return true if successful
	 */
	private boolean send(byte[] bytes) {
		try {
			if (!socket.isClosed()) {
				socket.getOutputStream().write(bytes);
			}
		} catch (Exception e) {
			log.error("Failed to send message", e);
			return false;
		}
		return true;
	}

	/**
	 * Send a message to the server
	 *
	 * @param msg Message to be sent
	 * @return true if successful
	 */
	public boolean send(NetCmd cmd, Object... params) {
		String msg = "";
		if (params.length > 0) {
			msg += "\t";
			msg += Arrays.stream(params).map(Object::toString).collect(Collectors.joining("\t"));
		}
		msg += "\n";
		log.debug("sending: " + cmd);
		return send(NetUtil.stringToBytes(cmd.command() + msg));
	}

	/**
	 * @return whether this client is connected to a server
	 */
	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}

	/**
	 * @return Destination host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return Destination port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return Server's IP address
	 */
	public String getIP() {
		return ip;
	}

	/**
	 * NewNetMessageListenerAdd
	 *
	 * @param l AddNetMessageListener
	 */
	public void addListener(NetMessageListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/**
	 * SpecifiedNetMessageListenerDelete the
	 *
	 * @param l RemoveNetMessageListener
	 * @return Actually been removedtrue, I has not been added originallyfalse
	 */
	public boolean removeListener(NetMessageListener l) {
		return listeners.remove(l);
	}

	/**
	 * Start Ping timer task
	 *
	 * @param interval Interval
	 */
	public void startPingTask(long interval) {
		log.debug("Ping interval:" + interval);
		if (timerPing != null) {
			timerPing.cancel();
		}
		if (interval <= 0) {
			return;
		}
		pingCount = 0;
		timerPing = new Timer(true);
		timerPing.schedule(new PingTask(), interval, interval);
	}

	/**
	 * Stop the Ping timer task
	 */
	public void stopPingTask() {
		if (timerPing != null) {
			timerPing.cancel();
		}
	}

	/**
	 * Ping task
	 */
	protected class PingTask extends TimerTask {
		@Override
		public void run() {
			if (!isConnected()) {
				log.info("Ping Timer Cancelled");
				if (timerPing != null) {
					timerPing.cancel();
				}
				return;
			}
			if (pingCount >= PING_AUTO_DISCONNECT_COUNT) {
				log.error("Ping timeout");
				running = false;
				if (timerPing != null) {
					timerPing.cancel();
				}
			} else {
				send(NetCmd.PING);
				pingCount++;

				if (pingCount >= PING_AUTO_DISCONNECT_COUNT / 2) {
					log.debug("Ping " + pingCount + "/" + PING_AUTO_DISCONNECT_COUNT);
				}
			}
		}
	}
}

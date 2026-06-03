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
package mu.nu.nullpo.game.net;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.types.Version;

/**
 * Client(ObserverUse)
 */
@Log4j
@Getter
public class NetObserverClient extends NetBaseClient {

	/** Number of players */
	protected volatile int playerCount = 0;

	/** Observercount */
	protected volatile int observerCount = 0;

	/**
	 * Constructor
	 *
	 * @param host Destination host
	 * @param port Destination port number
	 */
	public NetObserverClient(String host, int port) {
		super(host, port);
	}

	/*
	 * The various processing depending on the received message
	 */
	@Override
	protected void handleMessage(NetMessage message) {
		switch (message.command()) {
		case WELCOME -> { // Connection completion
			// [VERSION] | [PLAYERS] | [OBSERVERS] | [PING INTERVAL]
			playerCount = message.asInt(1);
			observerCount = message.asInt(2);
			long pingInterval = message.length() > 2 ? message.asLong(2) : PING_INTERVAL;
			if (pingInterval != PING_INTERVAL) {
				startPingTask(pingInterval);
			}
			send(NetCmd.OBSERVER_LOGIN, Version.getCurrent());
			log.info("connected to server v" + message.text(0));
		}
		case OBSERVER_UPDATE -> { // PeoplecountUpdate
			// [PLAYERS] | [OBSERVERS]
			playerCount = message.asInt(0);
			observerCount = message.asInt(1);
		}
		default -> {
			// nothing
		}
		}
		super.handleMessage(message);
	}
}

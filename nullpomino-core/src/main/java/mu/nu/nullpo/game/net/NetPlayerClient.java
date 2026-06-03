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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.types.Version;

/**
 * Client(PlayerUse)
 */
@Log4j
@Getter
public class NetPlayerClient extends NetBaseClient {

	/** PlayerInformation */
	protected List<NetPlayerInfo> playerInfos = new LinkedList<>();

	/** Room Information */
	protected List<NetRoomInfo> roomInfos = new LinkedList<>();

	/** OwnPlayerName */
	protected String playerName;

	/** OwnTeam name */
	protected String playerTeam;

	/** OwnPlayerIdentification number */
	protected int playerUID;

	/** Number of players */
	protected int playerCount = -1;

	/** Observercount */
	protected int observerCount = -1;

	/**
	 * Constructor
	 *
	 * @param host Destination host
	 * @param port Destination port number
	 * @param name of the player
	 */
	public NetPlayerClient(String host, int port, String name) {
		this(host, port, name, "");
	}

	/**
	 * Constructor
	 *
	 * @param host Destination host
	 * @param port Destination port number
	 * @param name player name
	 * @param team team name
	 */
	public NetPlayerClient(String host, int port, String name, String team) {
		super(host, port);
		playerName = name;
		playerTeam = team;
	}

	/*
	 * The various processing depending on the received message
	 */
	@Override
	protected void handleMessage(NetMessage message) {
		log.debug("RECEIVED: " + message.command());

		// Connection completion
		switch (message.command()) {
		case WELCOME -> { // [VERSION] | [PLAYERS] | [OBSERVERS] | [PING INTERVAL]
			playerCount = message.asInt(1);
			observerCount = message.asInt(2);
			long pingInterval = message.length() > 2 ? message.asLong(2) : PING_INTERVAL;
			if (pingInterval != PING_INTERVAL) {
				startPingTask(pingInterval);
			}
			send(NetCmd.PLAYER_LOGIN, Version.getCurrent(), NetUtil.urlEncode(playerName),
					Locale.getDefault().getCountry(), NetUtil.urlEncode(playerTeam));
			log.info("connected to server v" + message.text(0));
		}
		case OBSERVER_UPDATE -> { // [PLAYERS] | [OBSERVERS]
			playerCount = message.asInt(0);
			observerCount = message.asInt(1);
		}
		case PLAYER_LOGIN_SUCCESS -> { // [NAME] | [UID]
			playerName = message.urlDecoded(0);
			playerUID = message.asInt(1);
		}
		case PLAYER_LIST -> { // [PLAYERS] | [PLAYERDATA...]
			int numPlayers = message.asInt(0);
			for (int i = 0; i < numPlayers; i++) {
				NetPlayerInfo p = new NetPlayerInfo(message.text(i + 1));
				playerInfos.add(p);
			}
		}
		case PLAYER_UPDATE, PLAYER_NEW -> { // [PLAYERDATA]
			NetPlayerInfo p = new NetPlayerInfo(message.text(0));
			NetPlayerInfo p2 = getPlayerInfoByUID(p.uid);
			if (p2 == null) {
				playerInfos.add(p);
			} else {
				int index = playerInfos.indexOf(p2);
				playerInfos.set(index, p);
			}
		}
		case PLAYER_LOGOUT -> { // [PLAYERDATA]
			NetPlayerInfo p = new NetPlayerInfo(message.text(0));
			NetPlayerInfo p2 = getPlayerInfoByUID(p.uid);
			if (p2 != null) {
				playerInfos.remove(p2);
				p2.delete();
			}
		}
		case ROOM_LIST -> { // [ROOMS] | [ROOMDATA...]
			int numRooms = message.asInt(0);
			for (int i = 0; i < numRooms; i++) {
				NetRoomInfo r = new NetRoomInfo(message.text(i + 1));
				roomInfos.add(r);
			}
		}
		case ROOM_UPDATE, ROOM_CREATE -> {// [ROOMDATA]
			NetRoomInfo r = new NetRoomInfo(message.text(0));
			NetRoomInfo r2 = getRoomInfo(r.roomID);
			if (r2 == null) {
				roomInfos.add(r);
				log.debug("room " + r.roomID + "added");
			} else {
				int index = roomInfos.indexOf(r2);
				roomInfos.set(index, r);
				log.debug("room " + r.roomID + "replaced");
			}
		}
		case ROOM_DELETE -> { // [ROOMDATA]
			NetRoomInfo r = new NetRoomInfo(message.text(0));
			NetRoomInfo r2 = getRoomInfo(r.roomID);
			if (r2 != null) {
				roomInfos.remove(r2);
				r2.delete();
			}
		}
		case CHANGE_STATUS -> { // Participation status change
			// Status | PlayerUID | name | #seatOrQueue
			String status = message.text(0);
			NetPlayerInfo p = getPlayerInfoByUID(message.asInt(1));
			if (p != null) {
				switch (status) {
				case "watchonly" -> {
					p.seatID = -1;
					p.queueID = -1;
				}
				case "joinqueue" -> {
					p.seatID = -1;
					p.queueID = message.asInt(3);
				}
				case "joinseat" -> {
					p.seatID = message.asInt(3);
					p.queueID = -1;
				}
				default -> log.warn("unknown status change: " + status);
				}
			}
		}
		default -> {
		}
		}
		super.handleMessage(message);
	}

	/**
	 * DesignatedIDReturns information room
	 *
	 * @param roomID RoomID
	 * @return Room Information(Does not existnull)
	 */
	public NetRoomInfo getRoomInfo(int roomID) {
		if (roomID < 0) {
			return null;
		}

		for (NetRoomInfo roomInfo : roomInfos) {
			if (roomID == roomInfo.roomID) {
				return roomInfo;
			}
		}

		return null;
	}

	/**
	 * SpecifiedNameOfPlayerGet the
	 *
	 * @param name Name
	 * @return SpecifiedNameOfPlayerInformation(There were nonull)
	 */
	public NetPlayerInfo getPlayerInfoByName(String name) {
		for (NetPlayerInfo pInfo : playerInfos) {
			if (pInfo != null && pInfo.getPlayerName().equals(name)) {
				return pInfo;
			}
		}
		return null;
	}

	/**
	 * SpecifiedIDOfPlayerGet the
	 *
	 * @param uid ID
	 * @return SpecifiedIDOfPlayerInformation(There were nonull)
	 */
	public NetPlayerInfo getPlayerInfoByUID(int uid) {
		for (NetPlayerInfo pInfo : playerInfos) {
			if (pInfo != null && pInfo.uid == uid) {
				return pInfo;
			}
		}
		return null;
	}

	/**
	 * Get your own information
	 *
	 * @return Their own information
	 */
	public NetPlayerInfo getYourPlayerInfo() {
		return getPlayerInfoByUID(playerUID);
	}

	/**
	 * @return Current room ID
	 */
	public int getCurrentRoomID() {
		try {
			return getYourPlayerInfo().roomID;
		} catch (NullPointerException e) {
		}
		return -1;
	}

	/**
	 * @return Current room info
	 */
	public NetRoomInfo getCurrentRoomInfo() {
		return getRoomInfo(getCurrentRoomID());
	}
}

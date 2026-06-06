package mu.nu.nullpo.game.net;

import java.io.Serializable;
import java.util.Calendar;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.util.GeneralUtil;

/**
 * Chat message
 */
@Log4j
public class NetChatMessage implements Serializable {
	/** Serial version */
	private static final long serialVersionUID = 1L;

	/** User ID */
	public int uid;

	/** Username */
	public String strUserName;

	/** Hostname */
	public String strHost;

	/** Room ID (-1:Lobby) */
	public int roomID;

	/** Room Name */
	public String strRoomName;

	/** Timestamp Calendar */
	public Calendar timestamp;

	/** Message Body */
	public String strMessage;

	/**
	 * Default Constructor
	 */
	public NetChatMessage() {
		reset();
	}

	/**
	 * Constructor
	 *
	 * @param msg Message
	 */
	public NetChatMessage(String msg) {
		reset();
		strMessage = msg;
	}

	/**
	 * Constructor
	 *
	 * @param msg   Message
	 * @param pInfo Player Info
	 */
	public NetChatMessage(String msg, NetPlayerInfo pInfo) {
		reset();
		strMessage = msg;
		uid = pInfo.uid;
		strUserName = pInfo.getPlayerName();
		strHost = pInfo.realHost;
	}

	/**
	 * Constructor
	 *
	 * @param msg      Message
	 * @param pInfo    Player Info
	 * @param roomInfo Room Info
	 */
	public NetChatMessage(String msg, NetPlayerInfo pInfo, NetRoomInfo roomInfo) {
		reset();
		strMessage = msg;
		uid = pInfo.uid;
		strUserName = pInfo.getPlayerName();
		strHost = pInfo.realHost;
		roomID = roomInfo.roomID;
		strRoomName = roomInfo.strName;
	}

	/**
	 * Reset to default values
	 */
	public void reset() {
		uid = -1;
		strUserName = "";
		strHost = "";
		roomID = -1;
		strRoomName = "";
		timestamp = Calendar.getInstance();
		strMessage = "";
	}

	/**
	 * Output to logger
	 */
	public void outputLog() {
		if (roomID == -1) {
			log.info("LobbyChat UID:" + uid + " Name:" + strUserName + " Msg:" + strMessage);
		} else {
			log.info("RoomChat Room:" + strRoomName + " UID:" + uid + " Name:" + strUserName + " Msg:" + strMessage);
		}
	}

	/**
	 * Import from String array
	 *
	 * @param s String array (String[7])
	 */
	private void importStringArray(String[] s) {
		uid = Integer.parseInt(s[0]);
		strUserName = NetUtil.urlDecode(s[1]);
		strHost = NetUtil.urlDecode(s[2]);
		roomID = Integer.parseInt(s[3]);
		strRoomName = NetUtil.urlDecode(s[4]);
		timestamp = GeneralUtil.importCalendarString(s[5]);
		strMessage = NetUtil.urlDecode(s[6]);
	}

	/**
	 * Import from String (Divided by ;)
	 *
	 * @param str String
	 */
	public void importString(String str) {
		importStringArray(str.split(";"));
	}

	/**
	 * Export to String array
	 *
	 * @return String array (String[7])
	 */
	private String[] exportStringArray() {
		String[] s = new String[7];
		s[0] = Integer.toString(uid);
		s[1] = NetUtil.urlEncode(strUserName);
		s[2] = NetUtil.urlEncode(strHost);
		s[3] = Integer.toString(roomID);
		s[4] = NetUtil.urlEncode(strRoomName);
		s[5] = GeneralUtil.exportCalendarString(timestamp);
		s[6] = NetUtil.urlEncode(strMessage);
		return s;
	}

	/**
	 * Export to String (Divided by ;)
	 *
	 * @return String
	 */
	public String exportString() {
		String[] data = exportStringArray();
		return String.join(";", data);
	}
}

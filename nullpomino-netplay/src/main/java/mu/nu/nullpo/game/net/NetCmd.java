/**
 *
 */
package mu.nu.nullpo.game.net;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 *
 */
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum NetCmd {

	// @formatter:off
	ADMIN("admin"),
	ADMIN_LOGIN("adminlogin"),
	ADMIN_LOGIN_FAIL("adminloginfail"),
	ADMIN_LOGIN_SUCCESS("adminloginsuccess"),
	ADMIN_RESULT("adminresult"),
	ANNOUNCE("announce"),
	AUTOSTART("autostart"),
	AUTOSTART_BEGIN("autostartbegin"),
	AUTOSTART_STOP("autostartstop"),
	BAN("ban"),
	BAN_LIST("banlist"),
	BANNED("banned"),
	CHANGE_STATUS("changestatus"),
	CHANGE_TEAM("changeteam"),
	CLIENT_LIST("clientlist"),
	DEAD("dead"),
	DIAG("diag"),
	DISCONNECT("disconnect"),
	FINISH("finish"),
	GAME("game"),
	GET_INFO("getinfo"),
	GET_PRESETS("getpresets"),
	GSTAT("gstat"),
	GSTAT_1P("gstat1p"),
	LOBBY_CHAT("lobbychat"),
	LOBBY_CHAT_HIST("lobbychath"),
	MAP("map"),
	OBSERVER_LOGIN("observerlogin"),
	OBSERVER_LOGIN_FAIL("observerloginfail"),
	OBSERVER_LOGIN_SUCCESS("observerloginsuccess"),
	OBSERVER_UPDATE("observerupdate"),
	PING("ping"),
	PLAYER_DELETE("playerdelete"),
	PLAYER_ENTER("playerenter"),
	PLAYER_LEAVE("playerleave"),
	PLAYER_LIST("playerlist"),
	PLAYER_LOGIN("login"),
	PLAYER_LOGIN_FAIL("loginfail"),
	PLAYER_LOGIN_SUCCESS("loginsuccess"),
	PLAYER_LOGOUT("playerlogout"),
	PLAYER_NEW("playernew"),
	PLAYER_UPDATE("playerupdate"),
	PONG("pong"),
	RACE_WIN("racewin"),
	RATED_PRESETS("ratedpresets"),
	RATING("rating"),
	READY("ready"),
	RESET_SP("reset1p"),
	ROOM_CHAT("chat"),
	ROOM_CHAT_HIST("chath"),
	ROOM_CREATE("roomcreate"),
	ROOM_CREATE_RATED("ratedroomcreate"),
	ROOM_CREATE_SP("singleroomcreate"),
	ROOM_CREATE_SUCCESS("roomcreatesuccess"),
	ROOM_DELETE("roomdelete"),
	ROOM_DELETE_FAIL("roomdeletefail"),
	ROOM_DELETE_SUCCESS("roomdeletesuccess"),
	ROOM_JOIN("roomjoin"),
	ROOM_JOIN_FAIL("roomjoinfail"),
	ROOM_JOIN_SUCCESS("roomjoinsuccess"),
	ROOM_KICKED("roomkicked"),
	ROOM_LIST("roomlist"),
	ROOM_UPDATE("roomupdate"),
	RULE_DATA("ruledata"),
	RULE_DATA_FAIL("ruledatafail"),
	RULE_DATA_SUCCESS("ruledatasuccess"),
	RULE_GET("ruleget"),
	RULE_GET_FAIL("rulegetfail"),
	RULE_GET_RATED("rulegetrated"),
	RULE_GET_RATED_RULE_FAIL("rulegetratedfail"),
	RULE_GET_RATED_RULE_SUCCESS("rulegetratedsuccess"),
	RULE_GET_SUCCESS("rulegetsuccess"),
	RULE_LIST("rulelist"),
	RULE_LOCK("rulelock"),
	SHUTDOWN("shutdown"),
	MP_RANKING("mpranking"),
	SP_DOWNLOAD("spdownload"),
	SP_RANKING("spranking"),
	SP_SEND("spsend"),
	SP_SEND_NG("spsendng"),
	SP_SEND_OK("spsendok"),
	START("start"),
	START_1P("start1p"),
	UNBAN("unban"),
	WELCOME("welcome");
	// @formatter:on

	public static NetCmd of(String s) {
		for (NetCmd cmd : values()) {
			if (cmd.command().equals(s)) {
				return cmd;
			}
		}
		return null;
	}

	@Getter
	private final String command;
}

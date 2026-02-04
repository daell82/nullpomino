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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.zip.Adler32;

import org.apache.log4j.PropertyConfigurator;
import org.cacas.java.gnu.tools.Crypt;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.types.GameStyle;
import mu.nu.nullpo.game.types.Version;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import net.clarenceho.crypto.RC4;

/**
 * NullpoMino NetServer<br>
 * The code is based on
 * <a href="http://rox-xmlrpc.sourceforge.net/niotut/">James Greenfield's The
 * Rox Java NIO Tutorial</a>
 */
@Log4j
public class NetServer {

	/** Default port number */
	public static final int DEFAULT_PORT = 9200;

	/** Read buffer size */
	public static final int BUF_SIZE = 8192;

	/** Rule data send buffer size */
	public static final int RULE_BUF_SIZE = 512;

	/** Default value of ratingNormalMaxDiff */
	public static final double NORMAL_MAX_DIFF = 16;

	/** Default value of ratingProvisionalGames */
	public static final int PROVISIONAL_GAMES = 50;

	/** Default value of maxMPRanking */
	public static final int DEFAULT_MAX_MPRANKING = 100;

	/** Default value of maxSPRanking */
	public static final int DEFAULT_MAX_SPRANKING = 100;

	/** Default minimum gamerate */
	public static final float DEFAULT_MIN_GAMERATE = 80f;

	/** Default time of timeout */
	public static final long DEFAULT_TIMEOUT_TIME = 1000 * 60 * 1;

	/** Default number of lobby chat histories */
	public static final int DEFAULT_MAX_LOBBYCHAT_HISTORY = 10;

	/** Default number of room chat histories */
	public static final int DEFAULT_MAX_ROOMCHAT_HISTORY = 10;

	/** Server config file */
	private static CustomProperties propServer;

	/** Properties of player data list (mainly for rating) */
	private CustomProperties propPlayerData;

	/** Properties of multiplayer leaderboard */
	private CustomProperties propMPRanking;

	/** Properties of single player all-time leaderboard */
	private CustomProperties propSPRankingAlltime;

	/** Properties of single player daily leaderboard */
	private CustomProperties propSPRankingDaily;

	/** Properties of single player personal best */
//	private static CustomProperties propSPPersonalBest;

	/** True to allow hostname display (If false, it will display IP only) */
	private boolean allowDNSAccess;

	/** Timeout time (0=Disable) */
	private long timeoutTime;

	/** Client's ping interval */
	private long clientPingInterval;

	/** Default rating */
	private int ratingDefault;

	/** The maximum possible adjustment per game. (K-value) */
	private double ratingNormalMaxDiff;

	/**
	 * After playing this number of games, the rating logic will take account of
	 * number of games played.
	 */
	private int ratingProvisionalGames;

	/** Min/Max range of rating */
	private int ratingMin, ratingMax;

	/** Allow same IP player for rating change */
	private boolean ratingAllowSameIP;

	/** Max entry of multiplayer leaderboard */
	private int maxMPRanking;

	/** Max entry of singleplayer leaderboard */
	private int maxSPRanking;

	/** TimeZone of daily single player leaderboard */
	private String spDailyTimeZone;

	/** Minimum game rate of single player leaderboard */
	private float spMinGameRate;

	/** Max entry of lobby chat history */
	private int maxLobbyChatHistory;

	/** Max entry of room chat history */
	private int maxRoomChatHistory;

	/** Rated room info presets (compressed NetRoomInfo Strings) */
	private List<String> ratedInfoList;

	/** Rule list for rated game. */
	private EnumMap<GameStyle, List<RuleOptions>> ruleList;

	/** Setting ID list for rated game. */
	private EnumMap<GameStyle, List<Integer>> ruleSettingIDList;

	/** Multiplayer leaderboard list. */
	private EnumMap<GameStyle, List<NetPlayerInfo>> mpRankings;

	/** Multiplayer mode list */
	private EnumMap<GameStyle, List<String>> mpModeList;

	/** Multiplayer race mode flag */
	private EnumMap<GameStyle, List<Boolean>> mpModeIsRace;

	/** Single player mode list. */
	private EnumMap<GameStyle, List<String>> spModeList;

	/** Single player all-time leaderboard list */
	private List<NetSPRanking> spRankingListAlltime;

	/** Single player daily leaderboard list */
	private List<NetSPRanking> spRankingListDaily;

	/** Last-update time of single player daily leaderboard */
	private Calendar spDailyLastUpdate;

	/** Ban list */
	private List<NetServerBan> banList;

	/** Lobby chat message history */
	private List<NetChatMessage> lobbyChats = new LinkedList<>();

	/** List of SocketChannel */
	private List<SocketChannel> channels = new LinkedList<>();

	/** Last communication time */
	private Map<SocketChannel, Long> lastCommTime = new HashMap<>();

	/** Incomplete packet buffer */
	private Map<SocketChannel, StringBuilder> notCompletePacketMap = new HashMap<>();

	/** Player info */
	private Map<SocketChannel, NetPlayerInfo> playerInfos = new HashMap<>();

	/** Room info list */
	private List<NetRoomInfo> roomInfos = new LinkedList<>();

	/** Observer list */
	private List<SocketChannel> observers = new LinkedList<>();

	/** Admin list */
	private List<SocketChannel> admins = new LinkedList<>();

	/** Number of players connected so far (Used for assigning player ID) */
	private int playerCount = 0;

	/** Number of rooms created so far (Used for room ID) */
	private int roomCount = 0;

	/** RNG for map selection */
	private Random rand = new Random();

	/** true if shutdown is requested by the admin */
	private boolean shutdownRequested = false;

	/** The port to listen on */
	private int port;

	/** The channel on which we'll accept connections */
	private ServerSocketChannel serverChannel;

	/** The selector we'll be monitoring */
	private Selector selector;

	/** The buffer into which we'll read data when it's available */
	private ByteBuffer readBuffer;

	/** A list of ChangeRequest instances */
	private LinkedList<ChangeRequest> pendingChanges = new LinkedList<>();

	/** Maps a SocketChannel to a list of ByteBuffer instances */
	private HashMap<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();

	/** current version of the server */
	private Version version = Version.getCurrent();

	/**
	 * Load rated-game room presets from the server config
	 */
	private void loadPresetList() {
		CustomProperties propPresets = new CustomProperties();
		try (var in = new FileInputStream("config/etc/netserver_presets.cfg")) {
			propPresets.load(in);
		} catch (IOException e) {
			log.warn("Failed to load config file", e);
		}

		ratedInfoList = new LinkedList<>();

		String strInfo = "";
		int i = 0;
		while (strInfo != null) { // Iterate over the available presets in the server config
			strInfo = propPresets.getProperty("0.preset." + i++);
			if (strInfo != null) {
				ratedInfoList.add(strInfo);
			}
		}
		log.info("Loaded " + ratedInfoList.size() + " presets.");
	}

	/**
	 * Load rated-game rule list
	 */
	private void loadRuleList() {
		log.info("Loading Rule List...");

		ruleList = new EnumMap<>(GameStyle.class);
		ruleSettingIDList = new EnumMap<>(GameStyle.class);
		for (GameStyle style : GameStyle.values()) {
			ruleList.put(style, new LinkedList<>());
			ruleSettingIDList.put(style, new LinkedList<>());
		}

		try (var txtRuleList = new BufferedReader(new FileReader("config/etc/netserver_rulelist.lst"))) {
			GameStyle style = GameStyle.TETROMINO;

			String str = null;
			while ((str = txtRuleList.readLine()) != null) {
				if (str.isEmpty() || str.startsWith("#")) {
					// Empty or a comment line. Do nothing.
				} else if (str.startsWith(":")) {
					// Game style
					String strStyle = str.substring(1);
					style = GameStyle.valueOf(strStyle);
					if (style == null) {
						log.warn("{StyleChange} Unknown Style:" + str);
						style = GameStyle.TETROMINO;
					} else {
						log.debug("{StyleChange} StyleID:" + style + " StyleName:" + strStyle);
					}
				} else {
					// Rule file
					try {
						int settingID = 0;
						String[] strTempArray = str.split(";");
						if (strTempArray.length > 1) {
							settingID = Integer.parseInt(strTempArray[1]);
						}

						log.debug("{RuleLoad} StyleID:" + style + " RuleFile:" + strTempArray[0] + " SettingID:"
								+ settingID);

						FileInputStream in = new FileInputStream(strTempArray[0]);
						CustomProperties prop = new CustomProperties();
						prop.load(in);
						in.close();

						RuleOptions rule = new RuleOptions();
						rule.readProperty(prop, 0);

						ruleList.get(style).add(rule);
						ruleSettingIDList.get(style).add(settingID);
					} catch (Exception e2) {
						log.warn("Failed to load rule file", e2);
					}
				}
			}
		} catch (Exception e) {
			log.warn("Failed to load rule list", e);
		}
	}

	/**
	 * Load multiplayer leaderboard
	 */
	private void loadMPRankingList() {
		// Load mode list
		mpModeList = new EnumMap<>(GameStyle.class);
		mpModeIsRace = new EnumMap<>(GameStyle.class);
		for (GameStyle style : GameStyle.values()) {
			mpModeList.put(style, new LinkedList<>());
			mpModeIsRace.put(style, new LinkedList<>());
		}

		try {
			BufferedReader in = new BufferedReader(new FileReader("config/list/netlobby_multimode.lst"));

			String str = null;
			GameStyle style = GameStyle.TETROMINO;

			while ((str = in.readLine()) != null) {
				if (str.length() <= 0 || str.startsWith("#")) {
					// Empty line or comment line. Ignore it.
				} else if (str.startsWith(":")) {
					// Game style tag
					String strStyle = str.substring(1);

					style = GameStyle.valueOf(strStyle);
					if (style == null) {
						style = GameStyle.TETROMINO;
					}
				} else {
					// Game mode name
					String[] strSplit = str.split(",");
					String strModeName = strSplit[0];
					boolean isRace = false;
					if (strSplit.length > 1) {
						isRace = Boolean.parseBoolean(strSplit[1]);
					}

					mpModeList.get(style).add(strModeName);
					mpModeIsRace.get(style).add(isRace);
				}
			}

			in.close();
		} catch (Exception e) {
			log.warn("Failed to load multiplayer mode list", e);
		}

		// Load leaderboard
		log.info("Loading Multiplayer Ranking...");
		mpRankings = new EnumMap<>(GameStyle.class);
		for (GameStyle style : GameStyle.values()) {
			mpRankings.put(style, new LinkedList<>());
			int count = propMPRanking.getProperty(style + ".mpranking.count", 0);
			if (count > maxMPRanking) {
				count = maxMPRanking;
			}
			int idx = style.ordinal();
			for (int i = 0; i < count; i++) {
				NetPlayerInfo p = new NetPlayerInfo();
				p.strName = propMPRanking.getProperty(style + ".mpranking.strName." + i, "");
				p.rating[idx] = propMPRanking.getProperty(style + ".mpranking.rating." + i, ratingDefault);
				p.playCount[idx] = propMPRanking.getProperty(style + ".mpranking.playCount." + i, 0);
				p.winCount[idx] = propMPRanking.getProperty(style + ".mpranking.winCount." + i, 0);
				mpRankings.get(style).add(p);
			}
		}
	}

	/**
	 * Find a player in multiplayer leaderboard.
	 *
	 * @param style Game Style
	 * @param name  Player name in String (can be null, returns -1 if so)
	 * @return Index in mpRankingList[style] (-1 if not found)
	 */
	private int getMPRanking(GameStyle style, NetPlayerInfo player) {
		if (player == null) {
			return -1;
		}
		int rank = -1;
		for (NetPlayerInfo pInfo : mpRankings.get(style)) {
			rank++;
			if (player.strName.equals(pInfo.strName)) {
				return rank;
			}
		}
		return -1;
	}

	/**
	 * Update multiplayer leaderboard.
	 *
	 * @param style  Game Style
	 * @param player NetPlayerInfo
	 * @return New place (-1 if not ranked)
	 */
	private int mpRankingUpdate(GameStyle style, NetPlayerInfo player) {
		// Remove existing record
		int prevRecord = getMPRanking(style, player);
		if (prevRecord != -1) {
			mpRankings.get(style).remove(prevRecord);
		}
		int styleIdx = style.ordinal();
		List<NetPlayerInfo> gameStyleRanking = mpRankings.get(style);
		// Insert new record
		int rank = -1;
		for (int i = 0; i < gameStyleRanking.size(); i++) {
			NetPlayerInfo p2 = gameStyleRanking.get(i);
			if (player.rating[styleIdx] > p2.rating[styleIdx]) {
				gameStyleRanking.add(i, player);
				rank = i;
				break;
			}
		}

		// Couldn't rank in? Add to last.
		if (rank == -1) {
			if (gameStyleRanking.size() < maxMPRanking) {
				gameStyleRanking.addLast(player);
				rank = gameStyleRanking.size() - 1;
			}
			return rank;
		}

		// Remove anything after maxMPRanking
		while (gameStyleRanking.size() >= maxMPRanking) {
			gameStyleRanking.removeLast();
		}

		// Done
		return rank;
	}

	/**
	 * Write player data properties (propPlayerData) to a file
	 */
	private void writeMPRankingToFile() {
		for (int style = 0; style < GameStyle.numStyles(); style++) {
			List<NetPlayerInfo> ranking = mpRankings.get(GameStyle.values()[style]);
			int count = ranking.size();
			if (count > maxMPRanking) {
				count = maxMPRanking;
			}
			propMPRanking.setProperty(style + ".mpranking.count", count);

			for (int i = 0; i < count; i++) {
				NetPlayerInfo p = ranking.get(i);
				propMPRanking.setProperty(style + ".mpranking.strName." + i, p.strName);
				propMPRanking.setProperty(style + ".mpranking.rating." + i, p.rating[style]);
				propMPRanking.setProperty(style + ".mpranking.playCount." + i, p.playCount[style]);
				propMPRanking.setProperty(style + ".mpranking.winCount." + i, p.winCount[style]);
			}
		}

		try {
			FileOutputStream out = new FileOutputStream("config/setting/netserver_mpranking.cfg");
			propMPRanking.store(out, "NullpoMino NetServer Multiplayer Leaderboard");
			out.close();
		} catch (IOException e) {
			log.error("Failed to write multiplayer ranking data", e);
		}
	}

	/**
	 * Load single player leaderboard
	 */
	private void loadSPRankingList() {
		log.info("Loading Single Player Ranking...");

		spRankingListAlltime = new LinkedList<>();
		spRankingListDaily = new LinkedList<>();

		// Load mode list
		spModeList = new EnumMap<>(GameStyle.class);
		for (GameStyle style : GameStyle.values()) {
			spModeList.put(style, new LinkedList<>());
		}

		// Daily last-update
		TimeZone z = !spDailyTimeZone.isEmpty() ? TimeZone.getTimeZone(spDailyTimeZone) : TimeZone.getDefault();
		spDailyLastUpdate = GeneralUtil.importCalendarString(propSPRankingDaily.getProperty("daily.lastupdate", ""));
		if (spDailyLastUpdate != null) {
			spDailyLastUpdate.setTimeZone(z);
		}

		try {
			BufferedReader in = new BufferedReader(new FileReader("config/list/netlobby_singlemode.lst"));
			String line = null;
			GameStyle style = GameStyle.TETROMINO;
			while ((line = in.readLine()) != null) {
				if (line.length() <= 0 || line.startsWith("#")) {
					// Empty line or comment line. Ignore it.
				} else if (line.startsWith(":")) {
					// Game style tag
					line = line.substring(1);
					style = GameStyle.valueOf(line);
					if (style == null) {
						log.warn("{StyleChange} Unknown Style:" + line);
						style = GameStyle.TETROMINO;
					} else {
						log.debug("{StyleChange} StyleID:" + style + " StyleName:" + line);
					}
				} else {
					// Game mode name
					String[] strSplit = line.split(",");
					String modeName = strSplit[0];
					int rankingType = 0;
					int maxGameType = 0;
					if (strSplit.length > 1) {
						rankingType = Integer.parseInt(strSplit[1]);
					}
					if (strSplit.length > 2) {
						maxGameType = Integer.parseInt(strSplit[2]);
					}

					log.debug(
							"{Mode} Name:" + modeName + " RankingType:" + rankingType + " MaxGameType:" + maxGameType);

					spModeList.get(style).add(modeName);

					for (int i = 0; i < ruleList.get(style).size() + 1; i++) {
						String ruleName;
						if (i < ruleList.get(style).size()) {
							RuleOptions ruleOpt = ruleList.get(style).get(i);
							ruleName = ruleOpt.strRuleName;
						} else {
							ruleName = "any";
						}

						for (int j = 0; j < maxGameType + 1; j++) {
							for (int k = 0; k < 2; k++) {
								NetSPRanking rankingData = new NetSPRanking();
								rankingData.modeName = modeName;
								rankingData.ruleName = ruleName;
								rankingData.gameType = j;
								rankingData.rankingType = rankingType;
								rankingData.style = style.getMode();
								rankingData.maxRecords = maxSPRanking;

								if (k == 0) {
									rankingData.readProperty(propSPRankingAlltime);
									spRankingListAlltime.add(rankingData);
									log.debug(rankingData.ruleName + "," + rankingData.modeName + ","
											+ rankingData.gameType);
								} else {
									rankingData.readProperty(propSPRankingDaily);
									spRankingListDaily.add(rankingData);
								}
							}
						}
					}
				}
			}

			in.close();
		} catch (Exception e) {
			log.warn("Failed to load single player mode list", e);
		}
	}

	/**
	 * Get specific all-time NetSPRanking
	 *
	 * @param rule  Rule Name
	 * @param mode  Mode Name
	 * @param gtype Game Type
	 * @return NetSPRanking (null if not found)
	 */
	private NetSPRanking getSPRanking(String rule, String mode, int gtype) {
		return getSPRanking(rule, mode, gtype, false);
	}

	/**
	 * Get specific NetSPRanking
	 *
	 * @param rule    Rule Name ("all" to get a merged table)
	 * @param mode    Mode Name
	 * @param gtype   Game Type
	 * @param isDaily <code>true</code> to get daily ranking, <code>false</code> to
	 *                get all-time ranking
	 * @return NetSPRanking (null if not found)
	 */
	private NetSPRanking getSPRanking(String rule, String mode, int gtype, boolean isDaily) {
		if (rule.equals("all")) {
			return getSPRankingAllRules(mode, gtype, isDaily);
		}
		List<NetSPRanking> list = isDaily ? spRankingListDaily : spRankingListAlltime;
		for (NetSPRanking r : list) {
			if (r.ruleName.equals(rule) && r.modeName.equals(mode) && r.gameType == gtype) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Get NetSPRanking for all rule types
	 *
	 * @param mode    Mode Name
	 * @param gtype   Game Type
	 * @param isDaily <code>true</code> to get daily ranking, <code>false</code> to
	 *                get all-time ranking
	 * @return NetSPRanking (null if not found or there are none)
	 */
	private NetSPRanking getSPRankingAllRules(String mode, int gtype, boolean isDaily) {
		List<NetSPRanking> list = isDaily ? spRankingListDaily : spRankingListAlltime;
		List<NetSPRanking> allRanks = new LinkedList<>();
		for (NetSPRanking r : list) {
			if (r.modeName.equals(mode) && r.gameType == gtype) {
				allRanks.add(r);
			}
		}
		NetSPRanking merged = NetSPRanking.mergeRankings(allRanks);
		merged.ruleName = "all";
		return merged;
	}

	/**
	 * Update the last-update variable of daily ranking, and wipe the records if
	 * needed
	 *
	 * @return <code>true</code> if the records are wiped
	 */
	private boolean updateSPDailyRanking() {
		TimeZone z = spDailyTimeZone.length() > 0 ? TimeZone.getTimeZone(spDailyTimeZone) : TimeZone.getDefault();
		Calendar c = Calendar.getInstance(z);
		Calendar oldLastUpdate = spDailyLastUpdate;

		spDailyLastUpdate = c;
		propSPRankingDaily.setProperty("daily.lastupdate", GeneralUtil.exportCalendarString(spDailyLastUpdate));

		if (oldLastUpdate != null) {
			log.debug("SP daily ranking previous-update:" + GeneralUtil.getCalendarString(oldLastUpdate));
		}
		log.debug("SP daily ranking last-update:" + GeneralUtil.getCalendarString(c));

		if (oldLastUpdate == null || c.get(Calendar.DATE) == oldLastUpdate.get(Calendar.DATE)) {
			return false;
		}

		for (NetSPRanking r : spRankingListDaily) {
			r.records.clear();
		}
		log.info("SP daily ranking wiped");

		return true;
	}

	/**
	 * Write single player ranking to a file
	 */
	private void writeSPRankingToFile() {
		// All-time
		for (NetSPRanking r : spRankingListAlltime) {
			r.writeProperty(propSPRankingAlltime);
		}
		try {
			FileOutputStream out = new FileOutputStream("config/setting/netserver_spranking.cfg");
			propSPRankingAlltime.store(out, "NullpoMino NetServer Single Player All-time Leaderboard");
			out.close();
		} catch (IOException e) {
			log.error("Failed to write single player all-time ranking data", e);
		}

		// Daily
		for (NetSPRanking r : spRankingListDaily) {
			r.writeProperty(propSPRankingDaily);
		}
		try {
			FileOutputStream out = new FileOutputStream("config/setting/netserver_spranking_daily.cfg");
			propSPRankingDaily.store(out, "NullpoMino NetServer Single Player Daily Leaderboard");
			out.close();
		} catch (IOException e) {
			log.error("Failed to write single player daily ranking data", e);
		}
	}

	/**
	 * Get player data from propPlayerData
	 *
	 * @param pInfo NetPlayerInfo
	 */
	private void getPlayerDataFromProperty(NetPlayerInfo pInfo) {
		if (pInfo.isTripUse) {
			for (int i = 0; i < GameStyle.numStyles(); i++) {
				pInfo.rating[i] = propPlayerData.getProperty("p.rating." + i + "." + pInfo.strName, ratingDefault);
				pInfo.playCount[i] = propPlayerData.getProperty("p.playCount." + i + "." + pInfo.strName, 0);
				pInfo.winCount[i] = propPlayerData.getProperty("p.winCount." + i + "." + pInfo.strName, 0);
			}
			pInfo.spPersonalBest.strPlayerName = pInfo.strName;
			pInfo.spPersonalBest.readProperty(propPlayerData);
		} else {
			for (int i = 0; i < GameStyle.numStyles(); i++) {
				pInfo.rating[i] = ratingDefault;
				pInfo.playCount[i] = 0;
				pInfo.winCount[i] = 0;
			}
			pInfo.spPersonalBest.strPlayerName = pInfo.strName;
		}
	}

	/**
	 * Set player data to propPlayerData
	 *
	 * @param pInfo NetPlayerInfo
	 */
	private void setPlayerDataToProperty(NetPlayerInfo pInfo) {
		if (pInfo.isTripUse) {
			for (int i = 0; i < GameStyle.numStyles(); i++) {
				propPlayerData.setProperty("p.rating." + i + "." + pInfo.strName, pInfo.rating[i]);
				propPlayerData.setProperty("p.playCount." + i + "." + pInfo.strName, pInfo.playCount[i]);
				propPlayerData.setProperty("p.winCount." + i + "." + pInfo.strName, pInfo.winCount[i]);
			}
			pInfo.spPersonalBest.strPlayerName = pInfo.strName;
			pInfo.spPersonalBest.writeProperty(propPlayerData);
		}
	}

	/**
	 * Write player data properties (propPlayerData) to a file
	 */
	private void writePlayerDataToFile() {
		try {
			FileOutputStream out = new FileOutputStream("config/setting/netserver_playerdata.cfg");
			propPlayerData.store(out, "NullpoMino NetServer PlayerData");
			out.close();
		} catch (IOException e) {
			log.error("Failed to write player data", e);
		}
	}

	/**
	 * Load ban list from a file
	 */
	private void loadBanList() {
		banList = new LinkedList<>();
		try {
			List<String> lines = Files.readAllLines(new File("config/setting/netserver_banlist.cfg").toPath());
			for (String line : lines) {
				if (line.isEmpty()) {
					continue;
				}
				NetServerBan ban = new NetServerBan();
				ban.importString(line);
				if (!ban.isExpired()) {
					banList.add(ban);
				}
			}
		} catch (IOException e) {
			log.debug("Ban list file doesn't exist");
		} catch (Exception e) {
			log.warn("Failed to load ban list", e);
		}
	}

	/**
	 * Write ban list to a file
	 */
	private void saveBanList() {
		try (var out = new PrintWriter(new FileWriter("config/setting/netserver_banlist.cfg"))) {
			for (NetServerBan ban : banList) {
				out.println(ban.exportString());
			}
			out.flush();
			log.info("Ban list saved");
		} catch (Exception e) {
			log.error("Failed to save ban list", e);
		}
	}

	/**
	 * Load lobby chat history file
	 */
	private void loadLobbyChatHistory() {
		if (lobbyChats == null) {
			lobbyChats = new LinkedList<>();
		} else {
			lobbyChats.clear();
		}

		try {
			List<String> lines = Files.readAllLines(new File("config/setting/netserver_lobbychat.cfg").toPath());
			for (String line : lines) {
				if (line.isEmpty()) {
					continue;
				}
				NetChatMessage chat = new NetChatMessage();
				chat.importString(line);
				lobbyChats.add(chat);
			}
		} catch (IOException _) {
			log.debug("Lobby chat history doesn't exist");
		} catch (Exception e) {
			log.info("Failed to load lobby chat history", e);
		}
		while (lobbyChats.size() > maxLobbyChatHistory) {
			lobbyChats.removeFirst();
		}
	}

	/**
	 * Save lobby chat history file
	 */
	private void saveLobbyChatHistory() {
		try (var out = new PrintWriter(new FileWriter("config/setting/netserver_lobbychat.cfg"))) {
			while (lobbyChats.size() > maxLobbyChatHistory) {
				lobbyChats.removeFirst();
			}
			for (NetChatMessage chat : lobbyChats) {
				out.println(chat.exportString());
			}
			out.flush();
			log.debug("Lobby chat history saved");
		} catch (Exception e) {
			log.error("Failed to save lobby chat history file", e);
		}
	}

	/**
	 * Get IP address
	 *
	 * @param client SocketChannel
	 * @return IP address
	 */
	private String getHostAddress(SocketChannel client) {
		try {
			return client.socket().getInetAddress().getHostAddress();
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * Get Hostname
	 *
	 * @param client SocketChannel
	 * @return Hostname
	 */
	private String getHostName(SocketChannel client) {
		if (!allowDNSAccess) {
			return getHostAddress(client);
		}
		try {
			return client.socket().getInetAddress().getHostName();
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * Get both Hostname and IP address
	 *
	 * @param client SocketChannel
	 * @return Hostname and IP address
	 */
	private String getHostFull(SocketChannel client) {
		if (!allowDNSAccess) {
			return getHostAddress(client);
		}
		try {
			return getHostName(client) + " (" + getHostAddress(client) + ")";
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * Main (Entry point)
	 *
	 * @param args optional command-line arguments (0: server port 1: netserver.cfg
	 *             path)
	 */
	public static void main(String[] args) {
		// Init log system (should be first!)
		PropertyConfigurator.configure("config/etc/log_server.cfg");

		// get netserver.cfg file path from 2nd command-line argument, if specified
		String servcfg = "config/etc/netserver.cfg"; // default location
		if (args.length >= 2) {
			servcfg = args[1];
		}

		// Load server config file
		propServer = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream(servcfg);
			propServer.load(in);
			in.close();
		} catch (IOException e) {
			log.warn("Failed to load config file", e);
		}

		// Fetch port number from config file
		int port = propServer.getProperty("netserver.port", DEFAULT_PORT);

		if (args.length > 0) {
			// If command-line option is used, change port number to the new one
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
			}
		}

		// Run
		new NetServer(port).run();
	}

	/**
	 * Constructor
	 */
	public NetServer() {
		init(DEFAULT_PORT);
	}

	/**
	 * Constructor
	 *
	 * @param port The port to listen on
	 */
	public NetServer(int port) {
		init(port);
	}

	/**
	 * Initialize
	 *
	 * @param port The port to listen on
	 */
	private void init(int port) {
		this.port = port;

		// Load player data file
		propPlayerData = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/netserver_playerdata.cfg");
			propPlayerData.load(in);
			in.close();
		} catch (IOException e) {
		}

		// Load multiplayer leaderboard file
		propMPRanking = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/netserver_mpranking.cfg");
			propMPRanking.load(in);
			in.close();
		} catch (IOException e) {
		}

		// Load single player leaderboard file
		propSPRankingAlltime = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/netserver_spranking.cfg");
			propSPRankingAlltime.load(in);
			in.close();
		} catch (IOException e) {
		}

		propSPRankingDaily = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/netserver_spranking_daily.cfg");
			propSPRankingDaily.load(in);
			in.close();
		} catch (IOException e) {
		}

		// Load single player personal best
		CustomProperties propSPPersonalBest = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/netserver_sppersonalbest.cfg");
			propSPPersonalBest.load(in);
			in.close();
		} catch (IOException e) {
		}

		// Load settings
		allowDNSAccess = propServer.getProperty("netserver.allowDNSAccess", true);
		timeoutTime = propServer.getProperty("netserver.timeoutTime", DEFAULT_TIMEOUT_TIME);
		clientPingInterval = propServer.getProperty("netserver.clientPingInterval", (long) (5 * 1000));
		ratingDefault = propServer.getProperty("netserver.ratingDefault", NetPlayerInfo.DEFAULT_MULTIPLAYER_RATING);
		ratingNormalMaxDiff = propServer.getProperty("netserver.ratingNormalMaxDiff", NORMAL_MAX_DIFF);
		ratingProvisionalGames = propServer.getProperty("netserver.ratingProvisionalGames", PROVISIONAL_GAMES);
		ratingMin = propServer.getProperty("netserver.ratingMin", 0);
		ratingMax = propServer.getProperty("netserver.ratingMax", 99999);
		ratingAllowSameIP = propServer.getProperty("netserver.ratingAllowSameIP", true);
		maxMPRanking = propServer.getProperty("netserver.maxMPRanking", DEFAULT_MAX_MPRANKING);
		maxSPRanking = propServer.getProperty("netserver.maxSPRanking", DEFAULT_MAX_SPRANKING);
		spDailyTimeZone = propServer.getProperty("netserver.spDailyTimeZone", "");
		spMinGameRate = propServer.getProperty("netserver.spMinGameRate", DEFAULT_MIN_GAMERATE);
		maxLobbyChatHistory = propServer.getProperty("netserver.maxLobbyChatHistory", DEFAULT_MAX_LOBBYCHAT_HISTORY);
		maxRoomChatHistory = propServer.getProperty("netserver.maxRoomChatHistory", DEFAULT_MAX_ROOMCHAT_HISTORY);

		// Load rules for rated game
		loadRuleList();

		// Load room info presets for rated multiplayer games
		loadPresetList();

		// Load multiplayer leaderboard
		loadMPRankingList();
		propMPRanking.clear(); // Clear all entries in order to reduce file size

		// Load single player leaderboard
		loadSPRankingList();
		propSPRankingAlltime.clear(); // Clear all entries in order to reduce file size
		propSPRankingDaily.clear();

		// Load ban list
		loadBanList();

		// Load lobby chat history
		loadLobbyChatHistory();
	}

	/**
	 * Initialize the selector
	 *
	 * @return The selector we'll be monitoring
	 * @throws IOException When the selector can't be created (Usually when the port
	 *                     is already in use)
	 */
	private Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(port);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		log.info("Listening on port " + port + "...");

		return socketSelector;
	}

	/**
	 * Server mainloop
	 */
	public void run() {
		// Startup
		try {
			selector = initSelector();
		} catch (IOException e) {
			log.fatal("Failed to startup the server", e);
			return;
		}

		// Mainloop
		while (!shutdownRequested) {
			try {
				try {
					// Process any pending changes
					synchronized (pendingChanges) {
						Iterator<ChangeRequest> changes = pendingChanges.iterator();
						while (changes.hasNext()) {
							ChangeRequest change = changes.next();
							SelectionKey key = change.socket.keyFor(selector);

							if (key.isValid()) {
								switch (change.type) {
								case ChangeRequest.DISCONNECT:
									// Delayed disconnect
									List<ByteBuffer> queue = pendingData.get(change.socket);
									if (queue == null || queue.isEmpty()) {
										try {
											changes.remove();
											logout(key);
										} catch (ConcurrentModificationException e) {
											log.debug("ConcurrentModificationException on delayed disconnect", e);
										}
									}
									break;
								case ChangeRequest.CHANGEOPS:
									// interestOps Change
									key.interestOps(change.ops);
									changes.remove();
									break;
								}
							} else {
								changes.remove();
							}
						}
						// this.pendingChanges.clear();
					}

					// Wait for an event one of the registered channels
					selector.select();

					// Iterate over the set of keys for which events are available
					Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
					while (selectedKeys.hasNext()) {
						SelectionKey key = selectedKeys.next();
						selectedKeys.remove();

						if (!key.isValid()) {
							continue;
						}

						try {
							// Check what event is available and deal with it
							if (key.isAcceptable()) {
								doAccept(key);
							} else if (key.isReadable()) {
								doRead(key);
							} else if (key.isWritable()) {
								doWrite(key);
							}
						} catch (NetServerDisconnectRequestedException e) {
							// Intended Disconnect
							log.debug("Socket disconnected by NetServerDisconnectRequestedException");
							logout(key);
						} catch (IOException e) {
							// Disconnect when something bad happens
							log.info("Socket disconnected by IOException", e);
							logout(key);
						} catch (Exception e) {
							log.warn("Socket disconnected by Non-IOException", e);
							logout(key);
						}
					}
				} catch (ConcurrentModificationException e) {
					log.debug("ConcurrentModificationException on server mainloop", e);
				}
			} catch (IOException e) {
				log.fatal("IOException on server mainloop", e);
			} catch (Throwable e) {
				log.fatal("Non-IOException throwed on server mainloop", e);
			}
		}

		log.warn("Server Shutdown!");
	}

	/**
	 * Accept a new client
	 *
	 * @param key SelectionKey
	 * @throws IOException When something bad happens
	 */
	private void doAccept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(selector, SelectionKey.OP_READ);

		// Add to list
		channels.add(socketChannel);
		lastCommTime.put(socketChannel, System.currentTimeMillis());
		adminSendClientList();

		NetServerBan ban = getBan(socketChannel);
		if (ban != null) {
			// Banned
			log.info("Connection is banned:" + getHostName(socketChannel));
			Calendar endDate = ban.getEndDate();
			String strStart = GeneralUtil.exportCalendarString(ban.startDate);
			String strExpire = endDate == null ? "" : GeneralUtil.exportCalendarString(endDate);
			send(socketChannel, "banned\t" + strStart + "\t" + strExpire + "\n");
			synchronized (pendingChanges) {
				pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.DISCONNECT, 0));
			}
		} else {
			// Send welcome message
			log.debug("Accept:" + getHostName(socketChannel));
			send(socketChannel,
					"welcome\t" + version.majorMinor() + "\t" + playerInfos.size() + "\t" + observers.size() + "\t"
							+ version.micro() + "\t" + version.toString() + "\t" + clientPingInterval + "\t"
							+ version.isDevBuild() + "\n");
		}
	}

	/**
	 * Receive message(s) from client
	 *
	 * @param key SelectionKey
	 * @throws IOException When something bad happens
	 */
	private void doRead(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		if (readBuffer == null) {
			readBuffer = ByteBuffer.allocate(BUF_SIZE);
		} else {
			readBuffer.clear();
		}

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			throw e;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			throw new NetServerDisconnectRequestedException("Connection is closed (numBytesRead is -1)");
		}

		// Process the packet
		readBuffer.flip();

		byte[] bytes = new byte[readBuffer.limit()];
		readBuffer.get(bytes);

		String message = NetUtil.bytesToString(bytes);

		// Previous incomplete packet buffer (null if none are present)
		StringBuilder notCompletePacketBuffer = notCompletePacketMap.remove(socketChannel);

		// The new packet buffer
		StringBuilder packetBuffer = new StringBuilder();
		if (notCompletePacketBuffer != null) {
			packetBuffer.append(notCompletePacketBuffer);
		}
		packetBuffer.append(message);

		int index;
		while ((index = packetBuffer.indexOf("\n")) != -1) {
			String msgNow = packetBuffer.substring(0, index);
			processPacket(socketChannel, msgNow);
			packetBuffer = packetBuffer.replace(0, index + 1, "");
		}

		// Place new incomplete packet buffer
		if (!packetBuffer.isEmpty()) {
			notCompletePacketMap.put(socketChannel, packetBuffer);
		}
	}

	/**
	 * Write message(s) to client
	 *
	 * @param key SelectionKey
	 * @throws IOException When something bad happens
	 */
	private void doWrite(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (pendingData) {
			List<ByteBuffer> queue = pendingData.get(socketChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	/**
	 * Logout
	 *
	 * @param key SelectionKey
	 */
	private void logout(SelectionKey key) {
		key.cancel();

		SelectableChannel ch = key.channel();
		if (ch instanceof SocketChannel channel) {
			logout(channel);
		}
	}

	/**
	 * Logout
	 *
	 * @param channel SocketChannel
	 */
	private void logout(SocketChannel channel) {
		if (channel == null) {
			return;
		}

		String remoteAddr = getHostFull(channel);
		log.info("Logout: " + remoteAddr);

		try {
			channel.register(selector, 0);
		} catch (CancelledKeyException e) {
			// CancelledKeyException. This is normal
		} catch (Exception e) {
			log.debug("Exception throwed on logout (channel.register)", e);
		}
		try {
			channel.finishConnect();
		} catch (Exception e) {
			log.debug("Exception throwed on logout (channel.finishConnect)", e);
		}
		try {
			channel.close();
		} catch (Exception e) {
			log.debug("Exception throwed on logout (channel.close)", e);
		}

		try {
			channels.remove(channel);
			lastCommTime.remove(channel);
			notCompletePacketMap.remove(channel);

			List<ByteBuffer> queue = pendingData.get(channel);
			if (queue != null) {
				queue.clear();
			}

			NetPlayerInfo pInfo = playerInfos.remove(channel);
			if (pInfo != null) {
				log.info(pInfo.strName + " has logged out");

				playerDead(pInfo);
				pInfo.connected = false;
				pInfo.ready = false;

				LinkedList<NetRoomInfo> deleteList = new LinkedList<>(); // Room delete check list

				for (NetRoomInfo roomInfo : roomInfos) {
					if (roomInfo.playerList.contains(pInfo)) {
						roomInfo.playerList.remove(pInfo);
						roomInfo.playerQueue.remove(pInfo);
						roomInfo.exitSeat(pInfo);
						deleteList.add(roomInfo);
					}
				}

				for (NetRoomInfo roomInfo : deleteList) {
					if (!deleteRoom(roomInfo)) {
						joinAllQueuePlayers(roomInfo);
						if (!gameFinished(roomInfo) && !gameStartIfPossible(roomInfo)) {
							autoStartTimerCheck(roomInfo);
							broadcastRoomInfoUpdate(roomInfo);
						}
					}
				}
			}
			if (observers.remove(channel)) {
				log.info("Observer logout (" + remoteAddr + ")");
			}
			if (admins.remove(channel)) {
				log.info("Admin logout (" + remoteAddr + ")");
			}

			if (pInfo != null) {
				broadcastPlayerInfoUpdate(pInfo, "playerlogout");
				pInfo.delete();
			}
			broadcastUserCountToAll();
			adminSendClientList();

			log.debug("Channel close success");
		} catch (Exception e) {
			log.warn("Exception throwed on logout", e);
		}

		if (channels.isEmpty()) {
			cleanup();
		} else if (playerInfos.isEmpty()) {
			roomInfos.clear();
		}
	}

	/**
	 * Cleanup (after all clients are disconnected)
	 */
	private void cleanup() {
		log.info("Cleanup");

		channels.clear();
		lastCommTime.clear();
		notCompletePacketMap.clear();
		observers.clear();
		admins.clear();
		playerInfos.clear();
		roomInfos.clear();
		synchronized (pendingData) {
			pendingData.clear();
		}
		if (readBuffer != null) {
			readBuffer.clear();
		}
	}

	/**
	 * Kill timeout (dead) connections
	 *
	 * @param timeout Timeout in millsecond
	 * @return Number of connections killed
	 */
	private int killTimeoutConnections(long timeout) {
		if (timeout <= 0) {
			return 0;
		}

		List<SocketChannel> clients = new LinkedList<>(channels);
		int killCount = 0;

		for (SocketChannel client : clients) {
			Long lasttimeL = lastCommTime.get(client);

			if (lasttimeL != null) {
				long lasttime = lasttimeL;
				long nowtime = System.currentTimeMillis();

				if (nowtime - lasttime >= timeout) {
					logout(client);
					killCount++;
				}
			}
		}

		if (killCount > 0) {
			log.info("Killed " + killCount + " dead connections");
		}

		return killCount;
	}

	/**
	 * Send a message
	 *
	 * @param client SocketChannel
	 * @param bytes  Message to send (byte[])
	 */
	public void send(SocketChannel client, byte[] bytes) {
		synchronized (pendingChanges) {
			// Indicate we want the interest ops set changed
			pendingChanges.add(new ChangeRequest(client, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (pendingData) {
				List<ByteBuffer> queue = pendingData.computeIfAbsent(client, _ -> new ArrayList<>());
				queue.add(ByteBuffer.wrap(bytes));
			}
		}

		// Finally, wake up our selecting thread so it can make the required changes
		selector.wakeup();
	}

	/**
	 * Send a message
	 *
	 * @param client SocketChannel
	 * @param msg    Message to send (String)
	 */
	public void send(SocketChannel client, String msg) {
		send(client, NetUtil.stringToBytes(msg));
	}

	/**
	 * Send a message
	 *
	 * @param pInfo NetPlayerInfo
	 * @param bytes Message to send (byte[])
	 */
	public void send(NetPlayerInfo pInfo, byte[] bytes) {
		SocketChannel ch = getSocketChannelByPlayer(pInfo);
		if (ch == null) {
			return;
		}
		send(ch, bytes);
	}

	/**
	 * Send a message
	 *
	 * @param pInfo NetPlayerInfo
	 * @param msg   Message to send (String)
	 */
	public void send(NetPlayerInfo pInfo, String msg) {
		SocketChannel ch = getSocketChannelByPlayer(pInfo);
		if (ch == null) {
			return;
		}
		send(ch, NetUtil.stringToBytes(msg));
	}

	/**
	 * Broadcast a message to all players
	 *
	 * @param msg Message to send (String)
	 */
	public void broadcast(String msg) {
		synchronized (channels) {
			for (SocketChannel channel : channels) {
				NetPlayerInfo p = playerInfos.get(channel);

				if (p != null) {
					send(channel, msg);
				}
			}
		}
	}

	/**
	 * Broadcast a message to all players in specific room
	 *
	 * @param msg    Message to send (String)
	 * @param roomID Room ID (-1:Lobby)
	 */
	public void broadcast(String msg, int roomID) {
		synchronized (channels) {
			for (SocketChannel channel : channels) {
				NetPlayerInfo player = playerInfos.get(channel);

				if (player != null && roomID == player.roomID) {
					send(channel, msg);
				}
			}
		}
	}

	/**
	 * Broadcast a message to all players in specific room, except for the specified
	 * player
	 *
	 * @param msg    Message to send (String)
	 * @param roomID Room ID (-1:Lobby)
	 * @param pInfo  The player to avoid sending message
	 */
	public void broadcast(String msg, int roomID, NetPlayerInfo pInfo) {
		synchronized (channels) {
			for (SocketChannel channel : channels) {
				NetPlayerInfo player = playerInfos.get(channel);

				if (player != null && player.uid != pInfo.uid && roomID == player.roomID) {
					send(channel, msg);
				}
			}
		}
	}

	/**
	 * Broadcast a message to all observers
	 *
	 * @param msg Message to send (String)
	 */
	public void broadcastObserver(String msg) {
		for (SocketChannel channel : observers) {
			send(channel, msg);
		}
	}

	/**
	 * Broadcast client count (observers and players) to everyone
	 */
	public void broadcastUserCountToAll() {
		String msg = "observerupdate\t" + playerInfos.size() + "\t" + observers.size() + "\n";
		broadcast(msg);
		broadcastObserver(msg);
		writeServerStatusFile();
	}

	/**
	 * Broadcast a message to all admins
	 *
	 * @param msg Message to send (String)
	 */
	public void broadcastAdmin(String msg) {
		for (SocketChannel channel : admins) {
			send(channel, msg);
		}
	}

	/**
	 * Get SocketChannel from NetPlayerInfo
	 *
	 * @param pInfo Player
	 * @return SocketChannel (null if not found)
	 */
	public SocketChannel getSocketChannelByPlayer(NetPlayerInfo pInfo) {
		synchronized (channels) {
			for (SocketChannel channel : channels) {
				NetPlayerInfo player = playerInfos.get(channel);

				if (player != null && player.uid == pInfo.uid) {
					return channel;
				}
			}
		}
		return null;
	}

	/**
	 * Find longest matching player name matching a word boundary in msg. e.g. msg =
	 * "this is a test" player = "this is" will succeed, "this i" will fail "this
	 * is" will be returned if a player named "this" exists Returns SocketChannel of
	 * found player or null.
	 *
	 * @param msg Message to send (String)
	 */
	public SocketChannel findPlayerByMsg(String msg) {
		// Added to support temporary private messaging code, but might be useful even
		// so?
		synchronized (channels) {
			int maxLen = 0, len = 0;
			String player;
			SocketChannel chMatch = null;
			for (SocketChannel ch : channels) {
				NetPlayerInfo p = playerInfos.get(ch);
				len = p.strName.length();
				player = p.strName;
				if (p.isTripUse) {
					len -= 12;
					player = player.substring(0, len);
				}
				if (len + 1 < msg.length()) {
					if (msg.substring(0, len + 1).equals(player + " ") && len > maxLen) {
						chMatch = ch;
						maxLen = len;
					}
				}
			}
			return chMatch;
		}
	}

	/**
	 * Process a packet.
	 *
	 * @param client      The SocketChannel who sent this packet
	 * @param fullMessage The string of packet
	 * @throws IOException When something bad happens
	 */
	private void processPacket(SocketChannel client, String fullMessage) throws IOException {
		// Check ban
		if (checkConnectionOnBanlist(client)) {
			throw new NetServerDisconnectRequestedException("Connection banned");
		}

		// Setup Variables
		String[] message = fullMessage.split("\t"); // Split by \t
		NetPlayerInfo pInfo = playerInfos.get(client); // NetPlayerInfo of this client. null if not logged in.

		// Update last communication time
		lastCommTime.put(client, System.currentTimeMillis());

		// Get information of this server.
		if (message[0].equals("getinfo")) {
			int loggedInUsersCount = playerInfos.size();
			int observerCount = observers.size();
			send(client, "getinfo\t" + version.majorMinor() + "\t" + loggedInUsersCount + "\t" + observerCount + "\n");
			return;
		}
		// Disconnect request.
		if (message[0].equals("disconnect")) {
			throw new NetServerDisconnectRequestedException("Disconnect requested by the client (this is normal)");
		}
		// Ping
		if (message[0].equals("ping")) {
			// ping\t[ID]
			if (message.length > 1) {
				int id = Integer.parseInt(message[1]);
				send(client, "pong\t" + id + "\n");
			} else {
				send(client, "pong\n");
			}

			// Kill dead connections
			killTimeoutConnections(timeoutTime);

			return;
		}
		// Observer login
		if (message[0].equals("observerlogin")) {
			// observer\t[MAJOR VERSION]\t[MINOR VERSION]\t[DEV BUILD]

			// Ignore it if already logged in
			if (observers.contains(client)) {
				return;
			}
			if (admins.contains(client)) {
				return;
			}
			if (playerInfos.containsKey(client)) {
				return;
			}

			// Version check
			String serverVer = version.majorMinor();
			String clientVer = message[1];
			if (!serverVer.equals(clientVer)) {
				send(client, "observerloginfail\tDIFFERENT_VERSION\t" + serverVer + "\n");
				// logout(client);
				synchronized (pendingChanges) {
					pendingChanges.add(new ChangeRequest(client, ChangeRequest.DISCONNECT, 0));
				}
				return;
			}

			// Build type check
			boolean serverBuildType = version.isDevBuild();
			boolean clientBuildType = Boolean.parseBoolean(message[3]);
			if (serverBuildType != clientBuildType) {
				send(client, "observerloginfail\tDIFFERENT_BUILD\t" + serverBuildType + "\n");
				synchronized (pendingChanges) {
					pendingChanges.add(new ChangeRequest(client, ChangeRequest.DISCONNECT, 0));
				}
				return;
			}

			// Kill dead connections
			killTimeoutConnections(timeoutTime);

			// Success
			observers.add(client);
			send(client, "observerloginsuccess\n");
			broadcastUserCountToAll();
			adminSendClientList();

			log.info("New observer has logged in (" + client.toString() + ")");
			return;
		}
		// Player login
		if (message[0].equals("login")) {
			// login\t[MAJOR VERSION]\t[NAME]\t[COUNTRY]\t[TEAM]\t[MINOR VERSION]\t[DEV
			// BUILD]

			// Ignore it if already logged in
			if (observers.contains(client)) {
				return;
			}
			if (admins.contains(client)) {
				return;
			}
			if (playerInfos.containsKey(client)) {
				return;
			}

			// Version check
			String serverVer = version.majorMinor();
			String clientVer = message[1];
			if (!serverVer.equals(clientVer)) {
				send(client, "loginfail\tDIFFERENT_VERSION\t" + serverVer + "\n");
				// logout(client);
				synchronized (pendingChanges) {
					pendingChanges.add(new ChangeRequest(client, ChangeRequest.DISCONNECT, 0));
				}
				return;
			}

			// Build type check
			boolean serverBuildType = version.isDevBuild();
			boolean clientBuildType = Boolean.parseBoolean(message[6]);
			if (serverBuildType != clientBuildType) {
				send(client, "loginfail\tDIFFERENT_BUILD\t" + version.getBuildType() + "\n");
				synchronized (pendingChanges) {
					pendingChanges.add(new ChangeRequest(client, ChangeRequest.DISCONNECT, 0));
				}
				return;
			}

			// Kill dead connections
			killTimeoutConnections(timeoutTime);

			// Tripcode
			String originalName = NetUtil.urlDecode(message[2]);
			int sharpIndex = originalName.indexOf('#');
			boolean isTripUse = false;

			if (sharpIndex != -1) {
				String strTripKey = originalName.substring(sharpIndex + 1);
				String strTripCode = NetUtil.createTripCode(strTripKey,
						propServer.getProperty("netserver.tripcodemax", 10));

				if (sharpIndex > 0) {
					String strTemp = originalName.substring(0, sharpIndex);
					originalName = strTemp.replace('!', '?') + " !" + strTripCode;
				} else {
					originalName = "!" + strTripCode;
				}

				isTripUse = true;
			} else {
				originalName = originalName.replace('!', '?');
			}

			// Decide name (change to something else if needed)
			if (originalName.isEmpty()) {
				originalName = "noname";
			}
			String name = originalName;

			if (isTripUse) {
				// Kill the connection of the same name player
				NetPlayerInfo pInfo2 = searchPlayerByName(name);
				if (pInfo2 != null && pInfo2.channel != null) {
					logout(pInfo2.channel);
				}
			} else {
				// Change to "Name(n)" if the player of the same name exists
				int nameCount = 0;
				while (searchPlayerByName(name) != null) {
					name = originalName + "(" + nameCount + ")";
					nameCount++;
				}
			}

			// Set variables
			pInfo = new NetPlayerInfo();
			pInfo.strName = name;
			if (message.length > 3) {
				pInfo.strCountry = message[3];
			}
			if (message.length > 4) {
				pInfo.strTeam = NetUtil.urlDecode(message[4]);
			}
			pInfo.uid = playerCount;
			pInfo.connected = true;
			pInfo.isTripUse = isTripUse;

			pInfo.strRealHost = getHostName(client);
			pInfo.strRealIP = getHostAddress(client);
			pInfo.channel = client;

			int showhosttype = propServer.getProperty("netserver.showhosttype", 0);
			switch (showhosttype) {
			case 1:
				pInfo.strHost = getHostAddress(client);
				break;
			case 2:
				pInfo.strHost = getHostName(client);
				break;
			case 3: {
				pInfo.strHost = Crypt.crypt(propServer.getProperty("netserver.hostsalt", "AA"), getHostAddress(client));
				int maxlen = propServer.getProperty("netserver.hostcryptmax", 8);
				if (pInfo.strHost.length() > maxlen) {
					pInfo.strHost = pInfo.strHost.substring(pInfo.strHost.length() - maxlen);
				}
				break;
			}
			case 4: {
				pInfo.strHost = Crypt.crypt(propServer.getProperty("netserver.hostsalt", "AA"), getHostName(client));
				int maxlen = propServer.getProperty("netserver.hostcryptmax", 8);
				if (pInfo.strHost.length() > maxlen) {
					pInfo.strHost = pInfo.strHost.substring(pInfo.strHost.length() - maxlen);
				}
				break;
			}
			default:
				break;
			}

			// Load rating
			getPlayerDataFromProperty(pInfo);
			// log.info("Play:" + pInfo.playCount[0] + " Win:" + pInfo.winCount[0]);

			// Success
			playerInfos.put(client, pInfo);
			playerCount++;
			send(client, "loginsuccess\t" + NetUtil.urlEncode(pInfo.strName) + "\t" + pInfo.uid + "\n");
			log.info(pInfo.strName + " has logged in (Host:" + getHostName(client) + " Team:" + pInfo.strTeam + ")");

			sendRatedRuleList(client);
			sendPlayerList(client);
			sendRoomList(client);

			broadcastPlayerInfoUpdate(pInfo, "playernew");
			broadcastUserCountToAll();
			adminSendClientList();

			// Send lobby chat history
			while (lobbyChats.size() > maxLobbyChatHistory) {
				lobbyChats.removeFirst();
			}
			for (NetChatMessage chat : lobbyChats) {
				send(client,
						"lobbychath\t" + NetUtil.urlEncode(chat.strUserName) + "\t"
								+ GeneralUtil.exportCalendarString(chat.timestamp) + "\t"
								+ NetUtil.urlEncode(chat.strMessage) + "\n");
			}

			return;
		}
		// Send rated presets to client
		if (message[0].equals("getpresets")) {
			String str = "ratedpresets";

			for (String element : ratedInfoList) {
				str += "\t" + element;
			}

			str += "\n";

			send(client, str);

			// log.info("Sent preset message: " + str);

			return;
		}
		// Send rule data to server (Client->Server)
		if (message[0].equals("ruledata")) {
			// ruledata\t[ADLER32CHECKSUM]\t[RULEDATA]

			if (pInfo != null) {
				String strData = message[2];

				// Is checksum correct?
				Adler32 checksumObj = new Adler32();
				checksumObj.update(NetUtil.stringToBytes(strData));
				long sChecksum = checksumObj.getValue();
				long cChecksum = Long.parseLong(message[1]);

				// OK
				if (sChecksum == cChecksum) {
					String strRuleData = NetUtil.decompressString(strData);

					CustomProperties prop = new CustomProperties();
					prop.decode(strRuleData);
					pInfo.ruleOpt = new RuleOptions();
					pInfo.ruleOpt.readProperty(prop, 0);
					send(client, "ruledatasuccess\n");
				}
				// FAIL
				else {
					send(client, "ruledatafail\t" + sChecksum + "\n");
				}
			}
			return;
		}
		// Get rule data from server (Server->Client)
		if (message[0].equals("ruleget")) {
			// ruleget\t[UID]

			if (pInfo != null) {
				int uid = Integer.parseInt(message[1]);
				NetPlayerInfo p = searchPlayerByUID(uid);

				if (p != null) {
					if (p.ruleOpt == null) {
						p.ruleOpt = new RuleOptions();
					}

					CustomProperties prop = new CustomProperties();
					p.ruleOpt.writeProperty(prop, 0);
					String strRuleTemp = prop.encode("RuleData " + p.strName);
					String strRuleData = NetUtil.compressString(strRuleTemp);

					// Checksum
					Adler32 checksumObj = new Adler32();
					checksumObj.update(NetUtil.stringToBytes(strRuleData));
					long sChecksum = checksumObj.getValue();

					send(client, "rulegetsuccess\t" + uid + "\t" + sChecksum + "\t" + strRuleData + "\n");
				} else {
					send(client, "rulegetfail\t" + uid + "\n");
				}
			}
			return;
		}
		// Send rated-game rule data (Server->Client)
		if (message[0].equals("rulegetrated")) {
			// rulegetrated\t[STYLE]\t[NAME]

			if (pInfo != null) {
				int style = Integer.parseInt(message[1]);
				String name = message[2];
				RuleOptions rule = getRatedRule(style, name);

				if (rule != null) {
					CustomProperties prop = new CustomProperties();
					rule.writeProperty(prop, 0);
					String strRuleTemp = prop.encode("Rated RuleData " + rule.strRuleName);
					String strRuleData = NetUtil.compressString(strRuleTemp);

					// Checksum
					Adler32 checksumObj = new Adler32();
					checksumObj.update(NetUtil.stringToBytes(strRuleData));
					long sChecksum = checksumObj.getValue();

					send(client, "rulegetratedsuccess\t" + style + "\t" + name + "\t" + sChecksum + "\t" + strRuleData
							+ "\n");
				} else {
					send(client, "rulegetratedfail\t" + style + "\t" + name + "\n");
				}
			}
		}
		// Lobby chat
		if (message[0].equals("lobbychat")) {
			// lobbychat\t[MESSAGE]

//			String[] message = fullMessage.split("\t");	// Split by \t
//			NetPlayerInfo pInfo = playerInfoMap.get(client);	// NetPlayerInfo of this client. null if not logged in.

			if (pInfo != null) {
				NetChatMessage chat = new NetChatMessage(NetUtil.urlDecode(message[1]), pInfo);

				// Begin temporary private message code here
				String msg = chat.strMessage;
				if (msg.length() > 5 && msg.substring(0, 5).equals("/msg ")) {
					SocketChannel ch = findPlayerByMsg(msg.substring(5));
					if (ch == null) {
						send(pInfo.channel,
								"lobbychat\t" + chat.uid + "\t" + NetUtil.urlEncode(chat.strUserName) + "\t"
										+ GeneralUtil.exportCalendarString(chat.timestamp) + "\t"
										+ NetUtil.urlEncode("(private) Cannot find user") + "\n");
					} else {
						NetPlayerInfo p = playerInfos.get(ch);
						String playerName = p.strName;
						int len = playerName.length();
						if (p.isTripUse) {
							len -= 12;
						}
						msg = chat.strMessage.substring(len + 6);
						send(pInfo.channel,
								"lobbychat\t" + chat.uid + "\t" + NetUtil.urlEncode(chat.strUserName) + "\t"
										+ GeneralUtil.exportCalendarString(chat.timestamp) + "\t"
										+ NetUtil.urlEncode("-> *" + playerName.substring(0, len) + "* " + msg) + "\n");
						send(ch, "lobbychat\t" + chat.uid + "\t" + NetUtil.urlEncode(chat.strUserName) + "\t"
								+ GeneralUtil.exportCalendarString(chat.timestamp) + "\t"
								+ NetUtil.urlEncode("(private) " + msg) + "\n");
					}
				} else {
					// End here
					chat.outputLog();
					lobbyChats.add(chat);
					while (lobbyChats.size() > maxLobbyChatHistory) {
						lobbyChats.removeFirst();
					}
					saveLobbyChatHistory();

					broadcast("lobbychat\t" + chat.uid + "\t" + NetUtil.urlEncode(chat.strUserName) + "\t"
							+ GeneralUtil.exportCalendarString(chat.timestamp) + "\t"
							+ NetUtil.urlEncode(chat.strMessage) + "\n");
				}
			}
			return;
		}
		// Room chat
		if (message[0].equals("chat")) {
			// chat\t[MESSAGE]

			if (pInfo != null && pInfo.roomID != -1) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				if (roomInfo != null) {
					NetChatMessage chat = new NetChatMessage(NetUtil.urlDecode(message[1]), pInfo, roomInfo);
					chat.outputLog();
					roomInfo.chats.add(chat);
					while (roomInfo.chats.size() > maxRoomChatHistory) {
						roomInfo.chats.removeFirst();
					}

					broadcast("chat\t" + chat.uid + "\t" + NetUtil.urlEncode(chat.strUserName) + "\t"
							+ GeneralUtil.exportCalendarString(chat.timestamp) + "\t"
							+ NetUtil.urlEncode(chat.strMessage) + "\n", pInfo.roomID);
				}
			}
			return;
		}
		// Get multiplayer leaderboard
		if (message[0].equals("mpranking")) {
			// mpranking\t[STYLE]

			int styleIdx = Integer.parseInt(message[1]);
			GameStyle style = GameStyle.values()[styleIdx];

			int myRank = getMPRanking(style, pInfo);

			String pData = "";
			int prevRating = -1;
			int nowRank = 0;
			for (int i = 0; i < mpRankings.get(style).size(); i++) {
				NetPlayerInfo p = mpRankings.get(style).get(i);
				if (i == 0 || p.rating[styleIdx] < prevRating) {
					prevRating = p.rating[styleIdx];
					nowRank = i;
				}
				pData += nowRank + ";" + NetUtil.urlEncode(p.strName) + ";" + p.rating[styleIdx] + ";"
						+ p.playCount[styleIdx] + ";" + p.winCount[styleIdx] + "\t";
			}
			if (myRank == -1 && pInfo != null) {
				NetPlayerInfo p = pInfo;
				pData += -1 + ";" + NetUtil.urlEncode(p.strName) + ";" + p.rating[styleIdx] + ";"
						+ p.playCount[styleIdx] + ";" + p.winCount[styleIdx] + "\t";
			}
			String strPDataC = NetUtil.compressString(pData);

			String strMsg = "mpranking\t" + styleIdx + "\t" + myRank + "\t" + strPDataC + "\n";
			send(client, strMsg);
		}
		// Single player room
		if (message[0].equals("singleroomcreate")) {
			// singleroomcreate\t[roomName]\t[mode]\t[rule]
			if (pInfo != null && pInfo.roomID == -1) {
				NetRoomInfo roomInfo = new NetRoomInfo();

				roomInfo.singleplayer = true;
				roomInfo.strMode = NetUtil.urlDecode(message[2]);

				roomInfo.strName = NetUtil.urlDecode(message[1]);
				if (roomInfo.strName.length() < 1) {
					roomInfo.strName = "Single (" + pInfo.strName + ")";
				}

				roomInfo.maxPlayers = 1;

				if (message.length > 3) {
					roomInfo.ruleName = NetUtil.urlDecode(message[3]);
					roomInfo.ruleOpt = new RuleOptions(getRatedRule(0, roomInfo.ruleName));
					roomInfo.ruleLock = true;
					roomInfo.rated = true;
				} else {
					roomInfo.ruleName = pInfo.ruleOpt.strRuleName;
					roomInfo.ruleOpt = new RuleOptions(pInfo.ruleOpt);
					roomInfo.ruleLock = false;
					roomInfo.rated = false;
				}

				roomInfo.roomID = roomCount;

				roomCount++;
				if (roomCount == -1) {
					roomCount = 0;
				}

				roomInfos.add(roomInfo);

				pInfo.roomID = roomInfo.roomID;
				pInfo.resetPlayState();
				pInfo.playCountNow = 0;
				pInfo.winCountNow = 0;

				roomInfo.playerList.add(pInfo);
				pInfo.seatID = roomInfo.joinSeat(pInfo);

				// Send rule data if rated room
				if (roomInfo.rated) {
					CustomProperties prop = new CustomProperties();
					roomInfo.ruleOpt.writeProperty(prop, 0);
					String strRuleTemp = prop.encode("RuleData");
					String strRuleData = NetUtil.compressString(strRuleTemp);
					send(client, "rulelock\t" + strRuleData + "\n");
				}

				broadcastPlayerInfoUpdate(pInfo);
				broadcastRoomInfoUpdate(roomInfo, "roomcreate");
				send(client, "roomcreatesuccess\t" + roomInfo.roomID + "\t0\t-1\n");

				log.info("NewSingleRoom ID:" + roomInfo.roomID + " Title:" + roomInfo.strName);
			}
		}
		// Multiplayer room
		if (message[0].equals("roomcreate")) {
			if (pInfo != null && pInfo.roomID == -1) {
				String strRoomInfo = NetUtil.urlDecode(message[2]);
				NetRoomInfo roomInfo = new NetRoomInfo(strRoomInfo);

				roomInfo.strName = NetUtil.urlDecode(message[1]);
				if (roomInfo.strName.length() < 1) {
					roomInfo.strName = "No Title";
				}

				if (roomInfo.maxPlayers < 1) {
					roomInfo.maxPlayers = 1;
				}
				if (roomInfo.maxPlayers > 6) {
					roomInfo.maxPlayers = 6;
				}

				if (roomInfo.ruleLock) {
					roomInfo.ruleName = pInfo.ruleOpt.strRuleName;
					roomInfo.ruleOpt = new RuleOptions(pInfo.ruleOpt);
				}

				if (roomInfo.strMode.length() <= 0) {
					roomInfo.strMode = NetUtil.urlDecode(message[3]);
				}

				// Set map
				if (roomInfo.useMap && message.length > 4) {
					String strDecompressed = NetUtil.decompressString(message[4]);
					String[] strMaps = strDecompressed.split("\t");

					int maxMap = strMaps.length;

					for (int i = 0; i < maxMap; i++) {
						String strMap = strMaps[i];
						roomInfo.mapList.add(strMap);
					}

					if (roomInfo.mapList.isEmpty()) {
						log.debug("Room" + roomInfo.roomID + ": No maps");
						roomInfo.useMap = false;
					} else {
						log.debug("Room" + roomInfo.roomID + ": Received " + roomInfo.mapList.size() + " maps");
					}
				}

				roomInfo.roomID = roomCount;

				roomCount++;
				if (roomCount == -1) {
					roomCount = 0;
				}

				roomInfos.add(roomInfo);

				pInfo.roomID = roomInfo.roomID;
				pInfo.resetPlayState();
				pInfo.playCountNow = 0;
				pInfo.winCountNow = 0;

				roomInfo.playerList.add(pInfo);
				pInfo.seatID = roomInfo.joinSeat(pInfo);

				// Send rule data if rule-lock is enabled
				if (roomInfo.ruleLock) {
					CustomProperties prop = new CustomProperties();
					roomInfo.ruleOpt.writeProperty(prop, 0);
					String strRuleTemp = prop.encode("RuleData");
					String strRuleData = NetUtil.compressString(strRuleTemp);
					send(client, "rulelock\t" + strRuleData + "\n");
					// log.info("rulelock\t" + strRuleData);
				}

				broadcastPlayerInfoUpdate(pInfo);
				broadcastRoomInfoUpdate(roomInfo, "roomcreate");
				send(client, "roomcreatesuccess\t" + roomInfo.roomID + "\t" + pInfo.seatID + "\t-1\n");

				log.info("NewRoom ID:" + roomInfo.roomID + " Title:" + roomInfo.strName + " RuleLock:"
						+ roomInfo.ruleLock + " Map:" + roomInfo.useMap + " Mode:" + roomInfo.strMode);
			}
			return;
		}
		if (message[0].equals("ratedroomcreate")) {
			if (pInfo != null && pInfo.roomID == -1) {
				int i = Integer.parseInt(message[3]);
				String strPreset = NetUtil.decompressString(ratedInfoList.get(i));
				NetRoomInfo roomInfo = new NetRoomInfo(strPreset);

				roomInfo.strName = NetUtil.urlDecode(message[1]);
				if (roomInfo.strName.length() < 1) {
					roomInfo.strName = "No Title";
				}

				roomInfo.maxPlayers = Integer.parseInt(message[2]);
				if (roomInfo.maxPlayers < 1) {
					roomInfo.maxPlayers = 1;
				}
				if (roomInfo.maxPlayers > 6) {
					roomInfo.maxPlayers = 6;
				}

				roomInfo.strMode = NetUtil.urlDecode(message[4]);

				roomInfo.rated = true;
				roomInfo.ruleLock = false; // TODO: implement rule whitelists or rule locks in presets where it is
											// relevant

				roomInfo.roomID = roomCount; // TODO: fix copy-paste code

				roomCount++;
				if (roomCount == -1) {
					roomCount = 0;
				}

				roomInfos.add(roomInfo);

				pInfo.roomID = roomInfo.roomID;
				pInfo.resetPlayState();
				pInfo.playCountNow = 0;
				pInfo.winCountNow = 0;

				roomInfo.playerList.add(pInfo);
				pInfo.seatID = roomInfo.joinSeat(pInfo);

				broadcastPlayerInfoUpdate(pInfo);
				broadcastRoomInfoUpdate(roomInfo, "roomcreate");
				send(client, "roomcreatesuccess\t" + roomInfo.roomID + "\t" + pInfo.seatID + "\t-1\n");

				log.info("NewRatedRoom ID:" + roomInfo.roomID + " Title:" + roomInfo.strName + " RuleLock:"
						+ roomInfo.ruleLock + " Map:" + roomInfo.useMap + " Mode:" + roomInfo.strMode);
			}
			return;
		}
		// Join room (If roomID is -1, the player will return to lobby)
		if (message[0].equals("roomjoin")) {
			// roomjoin\t[ROOMID]\t[WATCH]

			if (pInfo != null) {
				int roomID = Integer.parseInt(message[1]);
				boolean watch = Boolean.parseBoolean(message[2]);
				NetRoomInfo prevRoom = getRoomInfo(pInfo.roomID);
				NetRoomInfo newRoom = getRoomInfo(roomID);

				if (roomID < 0) {
					// Return to lobby
					if (prevRoom != null) {
						int seatID = pInfo.seatID;
						broadcast("playerleave\t" + pInfo.uid + "\t" + NetUtil.urlEncode(pInfo.strName) + "\t" + seatID
								+ "\n", prevRoom.roomID, pInfo);
						playerDead(pInfo);
						pInfo.ready = false;
						prevRoom.exitSeat(pInfo);
						prevRoom.exitQueue(pInfo);
						prevRoom.playerList.remove(pInfo);
						if (!deleteRoom(prevRoom)) {
							joinAllQueuePlayers(prevRoom);

							if (!gameFinished(prevRoom)) {
								if (!gameStartIfPossible(prevRoom)) {
									autoStartTimerCheck(prevRoom);
									broadcastRoomInfoUpdate(prevRoom);
								}
							}
						}
					}
					pInfo.roomID = -1;
					pInfo.seatID = -1;
					pInfo.queueID = -1;
					pInfo.resetPlayState();
					pInfo.playCountNow = 0;
					pInfo.winCountNow = 0;

					broadcastPlayerInfoUpdate(pInfo);
					send(client, "roomjoinsuccess\t-1\t-1\t-1\n");
				} else if (newRoom != null) {
					// Enter a room
					if (prevRoom != null) {
						int seatID = pInfo.seatID;
						broadcast("playerleave\t" + pInfo.uid + "\t" + NetUtil.urlEncode(pInfo.strName) + "\t" + seatID
								+ "\n", prevRoom.roomID, pInfo);
						playerDead(pInfo);
						pInfo.ready = false;
						prevRoom.exitSeat(pInfo);
						prevRoom.exitQueue(pInfo);
						prevRoom.playerList.remove(pInfo);
						if (!deleteRoom(prevRoom)) {
							joinAllQueuePlayers(prevRoom);

							if (!gameFinished(prevRoom)) {
								if (!gameStartIfPossible(prevRoom)) {
									autoStartTimerCheck(prevRoom);
									broadcastRoomInfoUpdate(prevRoom);
								}
							}
						}
					}
					pInfo.roomID = newRoom.roomID;
					pInfo.resetPlayState();
					pInfo.playCountNow = 0;
					pInfo.winCountNow = 0;

					newRoom.playerList.add(pInfo);

					pInfo.seatID = -1;
					if (!watch && !newRoom.singleplayer) {
						pInfo.seatID = newRoom.joinSeat(pInfo);

						if (pInfo.seatID == -1) {
							pInfo.queueID = newRoom.joinQueue(pInfo);
						}
					}

					// Send rule data if rule-lock is enabled
					if (newRoom.ruleLock
					// || newRoom.rated //XXX: This breaks the new Rated with room info preset
					// system, as there is no Rule Lock for Rated now.
					) {
						CustomProperties prop = new CustomProperties();
						newRoom.ruleOpt.writeProperty(prop, 0);
						String strRuleTemp = prop.encode("RuleData");
						String strRuleData = NetUtil.compressString(strRuleTemp);
						send(client, "rulelock\t" + strRuleData + "\n");
						// log.info("rulelock\t" + strRuleData);
					}

					// Map send
					if (newRoom.useMap && !newRoom.mapList.isEmpty()) {
						String strMapTemp = "";
						int maxMap = newRoom.mapList.size();
						for (int i = 0; i < maxMap; i++) {
							strMapTemp += newRoom.mapList.get(i);
							if (i < maxMap - 1) {
								strMapTemp += "\t";
							}
						}
						String strCompressed = NetUtil.compressString(strMapTemp);
						send(client, "map\t" + strCompressed + "\n");
					}

					broadcast("playerenter\t" + pInfo.uid + "\t" + NetUtil.urlEncode(pInfo.strName) + "\t"
							+ pInfo.seatID + "\n", newRoom.roomID, pInfo);
					broadcastRoomInfoUpdate(newRoom);
					broadcastPlayerInfoUpdate(pInfo);
					send(client,
							"roomjoinsuccess\t" + newRoom.roomID + "\t" + pInfo.seatID + "\t" + pInfo.queueID + "\n");

					// Send chat history
					for (NetChatMessage chat : newRoom.chats) {
						send(client,
								"chath\t" + NetUtil.urlEncode(chat.strUserName) + "\t"
										+ GeneralUtil.exportCalendarString(chat.timestamp) + "\t"
										+ NetUtil.urlEncode(chat.strMessage) + "\n");
					}
				} else {
					// No such a room
					send(client, "roomjoinfail\n");
				}
			}
			return;
		}
		// Change team
		if (message[0].equals("changeteam")) {
			// changeteam\t[TEAM]
			if (pInfo != null && !pInfo.playing) {
				String strTeam = "";
				if (message.length > 1) {
					strTeam = NetUtil.urlDecode(message[1]);
				}

				if (!strTeam.equals(pInfo.strTeam)) {
					pInfo.strTeam = strTeam;
					broadcastPlayerInfoUpdate(pInfo);

					broadcast("changeteam\t" + pInfo.uid + "\t" + NetUtil.urlEncode(pInfo.strName) + "\t"
							+ NetUtil.urlEncode(pInfo.strTeam) + "\n", pInfo.roomID);
				}
			}
		}
		// Change Player/Spectator status
		if (message[0].equals("changestatus")) {
			// changestatus\t[WATCH]
			if (pInfo != null && !pInfo.playing && pInfo.roomID != -1) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				boolean watch = Boolean.parseBoolean(message[1]);

				if (!roomInfo.singleplayer) {
					if (watch) {
						// Change to spectator
						int prevSeatID = pInfo.seatID;
						roomInfo.exitSeat(pInfo);
						roomInfo.exitQueue(pInfo);
						pInfo.ready = false;
						pInfo.seatID = -1;
						pInfo.queueID = -1;
						// send(client, "changestatus\twatchonly\t-1\n");
						broadcast("changestatus\twatchonly\t" + pInfo.uid + "\t" + NetUtil.urlEncode(pInfo.strName)
								+ "\t" + prevSeatID + "\n", pInfo.roomID);

						joinAllQueuePlayers(roomInfo); // Let the queue-player to join
					} else // Change to player
					if (roomInfo.canJoinSeat()) {
						pInfo.seatID = roomInfo.joinSeat(pInfo);
						pInfo.queueID = -1;
						pInfo.ready = false;
						// send(client, "changestatus\tjoinseat\t" + pInfo.seatID + "\n");
						broadcast("changestatus\tjoinseat\t" + pInfo.uid + "\t" + NetUtil.urlEncode(pInfo.strName)
								+ "\t" + pInfo.seatID + "\n", pInfo.roomID);
					} else {
						pInfo.seatID = -1;
						pInfo.queueID = roomInfo.joinQueue(pInfo);
						pInfo.ready = false;
						// send(client, "changestatus\tjoinqueue\t" + pInfo.queueID + "\n");
						broadcast("changestatus\tjoinqueue\t" + pInfo.uid + "\t" + NetUtil.urlEncode(pInfo.strName)
								+ "\t" + pInfo.queueID + "\n", pInfo.roomID);
					}
					broadcastPlayerInfoUpdate(pInfo);
					if (!gameStartIfPossible(roomInfo)) {
						autoStartTimerCheck(roomInfo);
					}
					broadcastRoomInfoUpdate(roomInfo);
				}
			}
		}
		// Start game (Single player)
		if (message[0].equals("start1p")) {
			if (pInfo != null) {
				log.info("Starting single player game");

				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				int seat = roomInfo.getPlayerSeatNumber(pInfo);

				if (seat != -1 && roomInfo.singleplayer) {
					gameStart(roomInfo);
				}
			}
		}
		// Ready state change
		if (message[0].equals("ready")) {
			// ready\t[STATE]
			if (pInfo != null) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				int seat = roomInfo.getPlayerSeatNumber(pInfo);

				if (seat != -1 && !roomInfo.singleplayer) {
					pInfo.ready = Boolean.parseBoolean(message[1]);
					broadcastPlayerInfoUpdate(pInfo);

					if (!pInfo.ready) {
						roomInfo.isSomeoneCancelled = true;
					}

					// Start a game if possible
					if (!gameStartIfPossible(roomInfo)) {
						autoStartTimerCheck(roomInfo);
					}
				}
			}
		}
		// Autostart
		if (message[0].equals("autostart")) {
			if (pInfo != null) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				int seat = roomInfo.getPlayerSeatNumber(pInfo);

				if (seat != -1 && roomInfo.autoStartActive && !roomInfo.singleplayer) {
					if (roomInfo.autoStartTNET2) {
						// Move all non-ready players to spectators
						LinkedList<NetPlayerInfo> pList = new LinkedList<>();
						pList.addAll(roomInfo.playerSeat);

						for (NetPlayerInfo p : pList) {
							if (p != null && !p.ready) {
								int prevSeatID = p.seatID;
								roomInfo.exitSeat(p);
								roomInfo.exitQueue(p);
								p.ready = false;
								p.seatID = -1;
								p.queueID = -1;
								broadcast("changestatus\twatchonly\t" + p.uid + "\t" + NetUtil.urlEncode(p.strName)
										+ "\t" + prevSeatID + "\n", p.roomID);
							}
						}

						joinAllQueuePlayers(roomInfo);
					}

					gameStart(roomInfo);
				}
			}
		}
		// Dead
		if (message[0].equals("dead")) {
			if (pInfo != null) {
				if (message.length > 1) {
					int koUID = Integer.parseInt(message[1]);
					NetPlayerInfo koPlayerInfo = searchPlayerByUID(koUID);
					playerDead(pInfo, koPlayerInfo);
				} else {
					playerDead(pInfo);
				}
			}
		}
		// Race mode win (TODO: Replace with something cheat-proof)
		if (message[0].equals("racewin")) {
			if (pInfo != null && pInfo.roomID != -1 && pInfo.seatID != -1) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				int modeIndex = mpModeList.get(roomInfo.style).indexOf(roomInfo.strMode);
				boolean isRace = modeIndex == -1 ? false : mpModeIsRace.get(roomInfo.style).get(modeIndex);

				if (roomInfo.playing && isRace) {
					for (int i = message.length - 1; i > 1; i--) {
						int koUID = Integer.parseInt(message[i]);
						if (koUID != pInfo.uid) {
							NetPlayerInfo koPlayerInfo = searchPlayerByUID(koUID);

							if (koPlayerInfo != null && koPlayerInfo.roomID == roomInfo.roomID) {
								playerDead(koPlayerInfo, pInfo);
							}
						}
					}
				}
			}
		}
		// Multiplayer end-of-game stats
		if (message[0].equals("gstat")) {
			if (pInfo != null && pInfo.roomID != -1 && pInfo.seatID != -1) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);

				if (!roomInfo.singleplayer) {
					String msg = "gstat\t" + pInfo.uid + "\t" + pInfo.seatID + "\t" + NetUtil.urlEncode(pInfo.strName)
							+ "\t";
					for (int i = 1; i < message.length; i++) {
						msg += message[i];
						if (i < message.length - 1) {
							msg += "\t";
						}
					}
					msg += "\n";

					broadcast(msg, roomInfo.roomID);
				}
			}
		}
		// Single player end-of-game stats
		if (message[0].equals("gstat1p")) {
			if (pInfo != null && pInfo.roomID != -1 && pInfo.seatID != -1) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);

				if (roomInfo.singleplayer) {
					String msg = "gstat1p\t" + message[1] + "\n";
					broadcast(msg, roomInfo.roomID);
				}
			}
		}
		// Single player replay send
		if (message[0].equals("spsend")) {
			// spsend\t[CHECKSUM]\t[DATA]
			if (pInfo != null && pInfo.roomID != -1 && pInfo.seatID != -1) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				if (!pInfo.isTripUse) {
					broadcast("spsendok\t-1\tfalse\t-1\n", pInfo.roomID);
				} else if (roomInfo.singleplayer) {
					long sChecksum = Long.parseLong(message[1]);
					Adler32 checksumObj = new Adler32();
					checksumObj.update(NetUtil.stringToBytes(message[2]));
					log.info("Checksums are: " + sChecksum + " and " + checksumObj.getValue());

					if (sChecksum == checksumObj.getValue()) {
						String strData = NetUtil.decompressString(message[2]);
						NetSPRecord spRecord = new NetSPRecord(strData);
						String rule = roomInfo.rated ? roomInfo.ruleName : "any"; // "any" for unrated rules
						spRecord.strPlayerName = pInfo.strName;
						spRecord.strModeName = roomInfo.strMode;
						spRecord.strRuleName = rule;
						spRecord.style = roomInfo.style;
						spRecord.strTimeStamp = GeneralUtil.exportCalendarString();

						float gamerate = spRecord.stats.gamerate * 100f;

						boolean isDailyWiped = updateSPDailyRanking();
						int rank = -1;
						int rankDaily = -1;

						NetSPRanking ranking = getSPRanking(rule, spRecord.strModeName, spRecord.gameType);
						NetSPRanking rankingDaily = getSPRanking(rule, spRecord.strModeName, spRecord.gameType, true);
						if (ranking == null) {
							log.warn("All-time ranking not found:" + spRecord.strModeName);
						}
						if (rankingDaily == null) {
							log.warn("Daily ranking not found:" + spRecord.strModeName);
						}

						if ((ranking != null || rankingDaily != null) && gamerate >= spMinGameRate) {
							if (ranking != null) {
								rank = ranking.registerRecord(spRecord);
							}
							if (rankingDaily != null) {
								rankDaily = rankingDaily.registerRecord(spRecord);
							}

							if (rank != -1 || rankDaily != -1 || isDailyWiped) {
								writeSPRankingToFile();
							}

							boolean isPB = false;
							if (ranking != null) {
								isPB = pInfo.spPersonalBest.registerRecord(ranking.rankingType, spRecord);
								if (isPB) {
									setPlayerDataToProperty(pInfo);
									writePlayerDataToFile();
								}
							}

							log.info("Name:" + pInfo.strName + " Mode:" + spRecord.strModeName + " AllTime:" + rank
									+ " Daily:" + rankDaily);
							broadcast("spsendok\t" + rank + "\t" + isPB + "\t" + rankDaily + "\n", pInfo.roomID);
						} else {
							broadcast("spsendok\t-1\tfalse\t-1\n", pInfo.roomID);
						}
					} else {
						send(client, "spsendng\n");
					}
				}
			}
		}
		// Single player leaderboard
		if (message[0].equals("spranking")) {
			// spranking\t[RULE]\t[MODE]\t[GAMETYPE]\t[DAILY]
			String strRule = NetUtil.urlDecode(message[1]);
			String strMode = NetUtil.urlDecode(message[2]);
			int gameType = Integer.parseInt(message[3]);
			boolean isDaily = Boolean.parseBoolean(message[4]);

			if (isDaily) {
				if (updateSPDailyRanking()) {
					writeSPRankingToFile();
				}
			}

			int myRank = -1;
			NetSPRanking ranking = getSPRanking(strRule, strMode, gameType, isDaily);

			if (ranking != null) {
				int maxRecord = ranking.records.size();

				String strData = "";

				for (int i = 0; i < maxRecord; i++) {
					String strRow = "";
					if (i > 0) {
						strRow = ";";
					}

					NetSPRecord record = ranking.records.get(i);
					strRow += i + "," + NetUtil.urlEncode(record.strPlayerName) + ",";
					strRow += record.strTimeStamp + "," + record.stats.gamerate + ",";
					strRow += record.getStatRow(ranking.rankingType);

					if (pInfo != null && pInfo.strName.equals(record.strPlayerName)) {
						myRank = i;
					}

					strData += strRow;
				}
				if (myRank == -1 && pInfo != null && !isDaily) {
					NetSPRecord record = pInfo.spPersonalBest.getRecord(strRule, strMode, gameType);

					if (record != null) {
						String strRow = "";
						if (maxRecord > 0) {
							strRow += ",";
						}

						maxRecord++;
						strRow += -1 + "," + NetUtil.urlEncode(record.strPlayerName) + ",";
						strRow += record.strTimeStamp + "," + record.stats.gamerate + ",";
						strRow += record.getStatRow(ranking.rankingType);

						strData += strRow;
					}
				}

				String strMsg = "spranking\t" + strRule + "\t" + strMode + "\t" + gameType + "\t" + isDaily + "\t";
				strMsg += ranking.rankingType + "\t" + maxRecord + "\t" + strData + "\n";
				send(client, strMsg);
			} else {
				String strMsg = "spranking\t" + strRule + "\t" + strMode + "\t" + gameType + "\t" + isDaily + "\t";
				strMsg += 0 + "\t" + 0 + "\n";
				send(client, strMsg);
			}
		}
		// Single player replay download
		if (message[0].equals("spdownload")) {
			// spdownload\t[RULE]\t[MODE]\t[GAMETYPE]\t[DAILY]\t[NAME]
			String strRule = NetUtil.urlDecode(message[1]);
			String strMode = NetUtil.urlDecode(message[2]);
			int gameType = Integer.parseInt(message[3]);
			boolean isDaily = Boolean.parseBoolean(message[4]);
			String strName = NetUtil.urlDecode(message[5]);

			// Is any rule room?
			if (pInfo != null && pInfo.roomID != -1) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				if (roomInfo != null && !roomInfo.rated) {
					strRule = "any";
				}
			}

			if (isDaily) {
				if (updateSPDailyRanking()) {
					writeSPRankingToFile();
				}
			}

			NetSPRanking ranking = getSPRanking(strRule, strMode, gameType, isDaily);
			if (ranking != null) {
				// Get from leaderboard...
				NetSPRecord record = ranking.getRecord(strName);
				// or from Personal Best when not found in the leaderboard.
				if (record == null && !isDaily) {
					record = pInfo.spPersonalBest.getRecord(strRule, strMode, gameType);
				}

				if (record != null) {
					Adler32 checksumObj = new Adler32();
					checksumObj.update(NetUtil.stringToBytes(record.strReplayProp));
					long sChecksum = checksumObj.getValue();

					String strMsg = "spdownload\t" + sChecksum + "\t" + record.strReplayProp + "\n";
					send(client, strMsg);
				} else {
					log.warn("Record not found (Mode:" + strMode + ", Rule:" + strRule + ", Type:" + gameType + " Name:"
							+ strName + ")");
				}
			} else if (!isDaily) {
				log.warn("All-time ranking not found (Mode:" + strMode + ", Rule:" + strRule + ", Type:" + gameType
						+ ")");
			} else {
				log.warn("Daily ranking not found (Mode:" + strMode + ", Rule:" + strRule + ", Type:" + gameType + ")");
			}
		}
		// Single player mode reset
		if (message[0].equals("reset1p")) {
			if (pInfo != null) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				if (roomInfo != null) {
					int seat = roomInfo.getPlayerSeatNumber(pInfo);

					if (seat != -1) {
						pInfo.resetPlayState();
						broadcastPlayerInfoUpdate(pInfo);
						gameFinished(roomInfo);
						broadcast("reset1p\n", roomInfo.roomID, pInfo);
					}
				}
			}
		}
		// Game messages (NetServer will deliver them to other players but won't modify
		// it)
		if (message[0].equals("game")) {
			if (pInfo != null) {
				NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
				if (roomInfo != null) {
					int seat = roomInfo.getPlayerSeatNumber(pInfo);

					if (seat != -1) {
						String msg = "game\t" + pInfo.uid + "\t" + seat + "\t";
						for (int i = 1; i < message.length; i++) {
							msg += message[i];
							if (i < message.length - 1) {
								msg += "\t";
							}
						}
						msg += "\n";
						broadcast(msg, roomInfo.roomID, pInfo);
					}
				}
			}
		}
		// ADMIN: Admin Login
		if (message[0].equals("adminlogin")) {
			// Ignore it if already logged in
			if (observers.contains(client)) {
				return;
			}
			if (admins.contains(client)) {
				return;
			}
			if (playerInfos.containsKey(client)) {
				return;
			}

			String strRemoteAddr = getHostFull(client);

			// Check version
			String serverVer = version.majorMinor();
			String clientVer = message[1];
			if (!serverVer.equals(clientVer)) {
				String strLogMsg = strRemoteAddr + " has tried to access admin, but client version is different ("
						+ clientVer + ")";
				log.warn(strLogMsg);
				throw new NetServerDisconnectRequestedException(strLogMsg);
			}

			// Build type check
			boolean serverBuildType = version.isDevBuild();
			boolean clientBuildType = Boolean.parseBoolean(message[4]);
			if (serverBuildType != clientBuildType) {
				String strLogMsg = strRemoteAddr
						+ " has tried to access admin, but build type is different (IsDevBuild:" + clientBuildType
						+ ")";
				log.warn(strLogMsg);
				throw new NetServerDisconnectRequestedException(strLogMsg);
			}

			// Check username and password
			String strServerUsername = propServer.getProperty("netserver.admin.username", "");
			String strServerPassword = propServer.getProperty("netserver.admin.password", "");
			if (strServerUsername.length() == 0 || strServerPassword.length() == 0) {
				log.warn(strRemoteAddr + " has tried to access admin, but admin is disabled");
				send(client, "adminloginfail\tDISABLE\n");
				return;
			}

			String strClientUsername = message[2];
			if (!strClientUsername.equals(strServerUsername)) {
				log.warn(strRemoteAddr + " has tried to access admin with incorrect username (" + strClientUsername
						+ ")");
				send(client, "adminloginfail\tFAIL\n");
				return;
			}

			RC4 rc4 = new RC4(strServerPassword);
			byte[] bPass = Base64.getDecoder().decode(message[3]);
			byte[] bPass2 = rc4.rc4(bPass);
			String strClientPasswordCheckData = NetUtil.bytesToString(bPass2);
			if (!strClientPasswordCheckData.equals(strServerUsername)) {
				log.warn(strRemoteAddr + " has tried to access admin with incorrect password (Username:"
						+ strClientUsername + ")");
				send(client, "adminloginfail\tFAIL\n");
				return;
			}

			// Kill dead connections
			killTimeoutConnections(timeoutTime);

			// Login successful
			admins.add(client);
			send(client, "adminloginsuccess\t" + getHostAddress(client) + "\t" + getHostName(client) + "\n");
			adminSendClientList();
			sendRoomList(client);
			log.info("Admin has logged in (" + strRemoteAddr + ")");
		}
		// ADMIN: Admin commands
		if (message[0].equals("admin")) {
			if (admins.contains(client)) {
				String strAdminCommandTemp = NetUtil.decompressString(message[1]);
				String[] strAdminCommandArray = strAdminCommandTemp.split("\t");
				processAdminCommand(client, strAdminCommandArray);
			} else {
				log.warn(getHostFull(client) + " has tried to access admin command without login");
				logout(client);
				return;
			}
		}
	}

	/**
	 * Process admin command
	 *
	 * @param client  The SocketChannel who sent this packet
	 * @param message The String array of the command
	 * @throws IOException When something bad happens
	 */
	private void processAdminCommand(SocketChannel client, String[] message) throws IOException {
		// Client list (force update)
		if (message[0].equals("clientlist")) {
			adminSendClientList(client);
		}
		// Ban
		if (message[0].equals("ban")) {
			// ban\t[IP]\t(Length)
			int kickCount = 0;

			int banLength = -1;
			if (message.length > 2) {
				banLength = Integer.parseInt(message[2]);
			}

			kickCount = ban(message[1], banLength);
			saveBanList();

			sendAdminResult(client, "ban\t" + message[1] + "\t" + banLength + "\t" + kickCount);
		}
		// Un-Ban
		if (message[0].equals("unban")) {
			// unban\t[IP]
			int count = 0;

			if (message[1].equalsIgnoreCase("ALL")) {
				count = banList.size();
				banList.clear();
			} else {
				LinkedList<NetServerBan> tempList = new LinkedList<>();
				tempList.addAll(banList);

				for (NetServerBan ban : tempList) {
					if (ban.addr.equals(message[1])) {
						banList.remove(ban);
						count++;
					}
				}
			}
			saveBanList();

			sendAdminResult(client, "unban\t" + message[1] + "\t" + count);
		}
		// Ban List
		if (message[0].equals("banlist")) {
			// Cleanup expired bans
			LinkedList<NetServerBan> tempList = new LinkedList<>();
			tempList.addAll(banList);

			for (NetServerBan ban : tempList) {
				if (ban.isExpired()) {
					banList.remove(ban);
				}
			}

			// Create list
			String strResult = "";
			for (NetServerBan ban : banList) {
				strResult += "\t" + ban.exportString();
			}

			sendAdminResult(client, "banlist" + strResult);
		}
		// Player delete
		if (message[0].equals("playerdelete")) {
			// playerdelete\t<Name>

			String name = message[1];
			NetPlayerInfo pInfo = searchPlayerByName(name);

			boolean playerDataChange = false;
			boolean mpRankingDataChange = false;
			boolean spRankingDataChange = false;

			for (int i = 0; i < GameStyle.numStyles(); i++) {
				if (propPlayerData.getProperty("p.rating." + i + "." + name) != null) {
					propPlayerData.setProperty("p.rating." + i + "." + name, ratingDefault);
					propPlayerData.setProperty("p.playCount." + i + "." + name, 0);
					propPlayerData.setProperty("p.winCount." + i + "." + name, 0);
					playerDataChange = true;
				}
				if (propPlayerData.getProperty("sppersonal." + name + ".numRecords") != null) {
					propPlayerData.setProperty("sppersonal." + name + ".numRecords", 0);
					playerDataChange = true;
				}

				if (pInfo != null) {
					pInfo.rating[i] = ratingDefault;
					pInfo.playCount[i] = 0;
					pInfo.winCount[i] = 0;
					pInfo.spPersonalBest.records.clear();
				}
				GameStyle style = GameStyle.values()[i];

				int playerRank = getMPRanking(style, pInfo);
				if (playerRank != -1) {
					mpRankings.get(style).remove(playerRank);
					mpRankingDataChange = true;
				}

				for (NetSPRanking ranking : spRankingListAlltime) {
					NetSPRecord record = ranking.getRecord(name);
					if (record != null) {
						ranking.records.remove(record);
						spRankingDataChange = true;
					}
				}
				for (NetSPRanking ranking : spRankingListDaily) {
					NetSPRecord record = ranking.getRecord(name);
					if (record != null) {
						ranking.records.remove(record);
						spRankingDataChange = true;
					}
				}
			}

			sendAdminResult(client, "playerdelete\t" + name);

			if (playerDataChange) {
				writePlayerDataToFile();
			}
			if (mpRankingDataChange) {
				writeMPRankingToFile();
			}
			if (spRankingDataChange) {
				writeSPRankingToFile();
			}
		}
		// Room delete
		if (message[0].equals("roomdelete")) {
			// roomdelete\t[ID]
			int roomID = Integer.parseInt(message[1]);
			NetRoomInfo roomInfo = getRoomInfo(roomID);

			if (roomInfo != null) {
				String strRoomName = roomInfo.strName;
				forceDeleteRoom(roomInfo);
				sendAdminResult(client, "roomdeletesuccess\t" + roomID + "\t" + strRoomName);
			} else {
				sendAdminResult(client, "roomdeletefail\t" + roomID);
			}
		}
		// Shutdown
		if (message[0].equals("shutdown")) {
			log.warn("Shutdown requested by the admin (" + getHostFull(client) + ")");
			shutdownRequested = true;
			selector.wakeup();
		}
		// Announce
		if (message[0].equals("announce")) {
			// announce\t[Message]
			broadcast("announce\t" + message[1] + "\n");
		}
	}

	/**
	 * Send admin command result
	 *
	 * @param client The admin
	 * @param msg    Message to send
	 */
	private void sendAdminResult(SocketChannel client, String msg) {
		send(client, "adminresult\t" + NetUtil.compressString(msg) + "\n");
	}

	/**
	 * Broadcast admin command result to all admins
	 *
	 * @param msg Message to send
	 */
	private void broadcastAdminResult(String msg) {
		broadcastAdmin("adminresult\t" + NetUtil.compressString(msg) + "\n");
	}

	/**
	 * Send client list to all admins
	 */
	private void adminSendClientList() {
		adminSendClientList(null);
	}

	/**
	 * Send client list to admin
	 *
	 * @param client The admin. If null, it will broadcast to all admins.
	 */
	private void adminSendClientList(SocketChannel client) {
		String strMsg = "clientlist";

		for (SocketChannel ch : channels) {
			String strIP = getHostAddress(ch);
			String strHost = getHostName(ch);
			NetPlayerInfo pInfo = playerInfos.get(ch);

			int type = 0; // Type of client. 0:Not logged in
			if (pInfo != null) {
				type = 1; // 1:Player
			} else if (observers.contains(ch)) {
				type = 2; // 2:Observer
			} else if (admins.contains(ch)) {
				type = 3; // 3:Admin
			}

			String strClientData = strIP + "|" + strHost + "|" + type;
			if (pInfo != null) {
				strClientData += "|" + pInfo.exportString();
			}

			strMsg += "\t" + strClientData;
		}

		if (client == null) {
			broadcastAdminResult(strMsg);
		} else {
			sendAdminResult(client, strMsg);
		}
	}

	/**
	 * Get NetRoomInfo by using roomID
	 *
	 * @param roomID Room ID
	 * @return NetRoomInfo (null if not found)
	 */
	private NetRoomInfo getRoomInfo(int roomID) {
		if (roomID == -1) {
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
	 * Send room list to specified client
	 *
	 * @param client Client to send
	 */
	private void sendRoomList(SocketChannel client) {
		String msg = "roomlist\t" + roomInfos.size();

		for (NetRoomInfo roomInfo : roomInfos) {
			msg += "\t";
			msg += roomInfo.exportString();
		}

		msg += "\n";
		send(client, msg);
	}

	/**
	 * Delete a room
	 *
	 * @param roomInfo Room to delete
	 * @return true if success, false if fails (room not empty)
	 */
	private boolean deleteRoom(NetRoomInfo roomInfo) {
		if (roomInfo != null && roomInfo.playerList.isEmpty()) {
			log.info("RoomDelete ID:" + roomInfo.roomID + " Title:" + roomInfo.strName);
			broadcastRoomInfoUpdate(roomInfo, "roomdelete");
			roomInfos.remove(roomInfo);
			roomInfo.delete();
			return true;
		}
		return false;
	}

	/**
	 * Force delete a room
	 *
	 * @param roomInfo Room to delete
	 * @throws IOException If something bad happens
	 */
	private void forceDeleteRoom(NetRoomInfo roomInfo) throws IOException {
		if (roomInfo != null) {
			if (!roomInfo.playerList.isEmpty()) {
				LinkedList<NetPlayerInfo> tempList = new LinkedList<>(roomInfo.playerList);
				for (NetPlayerInfo pInfo : tempList) {
					if (pInfo != null) {
						SocketChannel client = getSocketChannelByPlayer(pInfo);
						if (client != null) {
							// Packet simulation :p
							processPacket(client, "roomjoin\t-1\tfalse");
							// Send message to the kicked player
							send(client, "roomkicked\t0\t" + roomInfo.roomID + "\t"
									+ NetUtil.urlEncode(roomInfo.strName) + "\n");
						}
					}
				}
				roomInfo.playerList.clear();
			}
			deleteRoom(roomInfo);
		}
	}

	/**
	 * Start/Stop auto start timer. It also turn-off the Ready status if there is
	 * only 1 player.
	 *
	 * @param roomInfo The room
	 */
	private void autoStartTimerCheck(NetRoomInfo roomInfo) {
		if (roomInfo.autoStartSeconds <= 0) {
			return;
		}

		int minPlayers = roomInfo.autoStartTNET2 ? 2 : 1;

		// Stop
		if (roomInfo.getNumberOfPlayerSeated() <= 1
				|| roomInfo.isSomeoneCancelled && roomInfo.disableTimerAfterSomeoneCancelled
				|| roomInfo.getHowManyPlayersReady() < minPlayers
				|| roomInfo.getHowManyPlayersReady() < roomInfo.getNumberOfPlayerSeated() / 2) {
			if (roomInfo.autoStartActive == true) {
				broadcast("autostartstop\n", roomInfo.roomID);
			}
			roomInfo.autoStartActive = false;
		}
		// Start
		else if (roomInfo.autoStartActive == false
				&& (!roomInfo.isSomeoneCancelled || !roomInfo.disableTimerAfterSomeoneCancelled)
				&& roomInfo.getHowManyPlayersReady() >= minPlayers
				&& roomInfo.getHowManyPlayersReady() >= roomInfo.getNumberOfPlayerSeated() / 2) {
			broadcast("autostartbegin\t" + roomInfo.autoStartSeconds + "\n", roomInfo.roomID);
			roomInfo.autoStartActive = true;
		}

		// Turn-off ready status if there is only 1 player
		if (roomInfo.getNumberOfPlayerSeated() == 1) {
			for (NetPlayerInfo p : roomInfo.playerSeat) {
				if (p != null && p.ready == true) {
					p.ready = false;
					broadcastPlayerInfoUpdate(p);
				}
			}
		}
	}

	/**
	 * Start a game if possible
	 *
	 * @param roomInfo The room
	 * @return true if started, false if not
	 */
	private boolean gameStartIfPossible(NetRoomInfo roomInfo) {
		if (roomInfo.getHowManyPlayersReady() == roomInfo.getNumberOfPlayerSeated()
				&& roomInfo.getNumberOfPlayerSeated() >= 2) {
			gameStart(roomInfo);
			return true;
		}
		return false;
	}

	/**
	 * Start a game (force start)
	 *
	 * @param roomInfo The room
	 */
	private void gameStart(NetRoomInfo roomInfo) {
		if (roomInfo == null) {
			return;
		}
		if (roomInfo.getNumberOfPlayerSeated() <= 0) {
			return;
		}
		if (roomInfo.getNumberOfPlayerSeated() <= 1 && !roomInfo.singleplayer) {
			return;
		}
		if (roomInfo.playing) {
			return;
		}

		roomInfo.gameStart();

		int mapNo = 0;
		int mapMax = roomInfo.mapList.size();
		if (roomInfo.useMap && mapMax > 0) {
			do {
				mapNo = rand.nextInt(mapMax);
			} while (mapNo == roomInfo.mapPrevious && mapMax >= 2);

			roomInfo.mapPrevious = mapNo;
		}
		String msg = "start\t" + Long.toString(rand.nextLong(), 16) + "\t" + roomInfo.startPlayers + "\t" + mapNo
				+ "\n";
		broadcast(msg, roomInfo.roomID);

		for (NetPlayerInfo p : roomInfo.playerSeat) {
			p.ready = false;
			p.playing = true;
			p.playCountNow++;

			// If ranked room
			if (roomInfo.rated && !roomInfo.isTeamGame() && (!roomInfo.hasSameIPPlayers() || ratingAllowSameIP)) {
				int index = roomInfo.style.ordinal();
				p.playCount[index]++;
				p.ratingBefore[index] = p.rating[index];
			}
			broadcastPlayerInfoUpdate(p);
		}

		roomInfo.playing = true;
		roomInfo.autoStartActive = false;
		broadcastRoomInfoUpdate(roomInfo);
	}

	/**
	 * Check if the game is finished. If finished, it will notify players.
	 *
	 * @param roomInfo The room
	 * @return true if finished
	 */
	private boolean gameFinished(NetRoomInfo roomInfo) {
		int startPlayers = roomInfo.startPlayers;
		int nowPlaying = roomInfo.getHowManyPlayersPlaying();
		boolean isTeamWin = roomInfo.isTeamWin();

		if (roomInfo != null && roomInfo.playing
				&& (nowPlaying < 1 || startPlayers >= 2 && nowPlaying < 2 || isTeamWin)) {
			// Game finished
			NetPlayerInfo winner = roomInfo.getWinner();
			String msg = "finish\t";

			if (isTeamWin) {
				// Winner is a team
				String teamName = roomInfo.getWinnerTeam();
				if (teamName == null) {
					teamName = "";
				}
				msg += -1 + "\t" + -1 + "\t" + NetUtil.urlEncode(teamName) + "\t" + isTeamWin;

				for (NetPlayerInfo pInfo : roomInfo.playerSeat) {
					if (pInfo != null && pInfo.playing) {
						pInfo.resetPlayState();
						pInfo.winCountNow++;
						broadcastPlayerInfoUpdate(pInfo);
						roomInfo.playerSeatDead.addFirst(pInfo);

						// Rated game
						/*
						 * if(roomInfo.rated) { // TODO: Update ratings?
						 * pInfo.winCount[roomInfo.style]++; setPlayerDataToProperty(pInfo); }
						 */
					}
				}

				/*
				 * if(roomInfo.rated) { writePlayerDataToFile(); }
				 */
			} else if (winner != null && !roomInfo.singleplayer) {
				// Winner is a player
				roomInfo.playerSeatDead.addFirst(winner);

				// Rated game
				if (roomInfo.rated && !roomInfo.isTeamGame() && (!roomInfo.hasSameIPPlayers() || ratingAllowSameIP)) {
					int style = roomInfo.style.ordinal();
					// Update win count
					winner.winCount[style]++;

					// Update rating

					int numPlayers = roomInfo.playerSeatDead.size();
					for (int w = 0; w < numPlayers - 1; w++) {
						for (int l = w + 1; l < numPlayers; l++) {
							NetPlayerInfo wp = roomInfo.playerSeatDead.get(w);
							NetPlayerInfo lp = roomInfo.playerSeatDead.get(l);

							wp.rating[style] += (int) (rankDelta(wp.playCount[style], wp.rating[style],
									lp.rating[style], 1) / (numPlayers - 1));
							lp.rating[style] += (int) (rankDelta(lp.playCount[style], lp.rating[style],
									wp.rating[style], 0) / (numPlayers - 1));

							if (wp.rating[style] < ratingMin) {
								wp.rating[style] = ratingMin;
							}
							if (lp.rating[style] < ratingMin) {
								lp.rating[style] = ratingMin;
							}
							if (wp.rating[style] > ratingMax) {
								wp.rating[style] = ratingMax;
							}
							if (lp.rating[style] > ratingMax) {
								lp.rating[style] = ratingMax;
							}
						}
					}

					// Notify/Save
					for (int i = 0; i < numPlayers; i++) {
						NetPlayerInfo p = roomInfo.playerSeatDead.get(i);
						int change = p.rating[style] - p.ratingBefore[style];
						log.debug("#" + (i + 1) + " Name:" + p.strName + " Rating:" + p.rating[style] + " (" + change
								+ ")");
						setPlayerDataToProperty(p);

						String msgRatingChange = "rating\t" + p.uid + "\t" + p.seatID + "\t"
								+ NetUtil.urlEncode(p.strName) + "\t" + p.rating[style] + "\t" + change + "\n";
						broadcast(msgRatingChange, winner.roomID);
					}
					writePlayerDataToFile();

					// Leaderboard update
					for (NetPlayerInfo player : roomInfo.playerSeatDead) {
						if (player.isTripUse) {
							mpRankingUpdate(roomInfo.style, player);
						}
					}
					writeMPRankingToFile();
				}

				msg += winner.uid + "\t" + winner.seatID + "\t" + NetUtil.urlEncode(winner.strName) + "\t" + isTeamWin;
				winner.resetPlayState();
				winner.winCountNow++;
				broadcastPlayerInfoUpdate(winner);
			} else {
				// No winner(s)
				msg += -1 + "\t" + -1 + "\t" + "" + "\t" + isTeamWin;
			}
			msg += "\n";
			broadcast(msg, roomInfo.roomID);

			roomInfo.playing = false;
			roomInfo.autoStartActive = false;
			broadcastRoomInfoUpdate(roomInfo);

			return true;
		}

		return false;
	}

	/**
	 * Broadcast a room update information (command will be "roomupdate")
	 *
	 * @param roomInfo The room
	 */
	private void broadcastRoomInfoUpdate(NetRoomInfo roomInfo) {
		broadcastRoomInfoUpdate(roomInfo, "roomupdate");
	}

	/**
	 * Broadcast a room update information
	 *
	 * @param roomInfo The room
	 * @param command  Command
	 */
	private void broadcastRoomInfoUpdate(NetRoomInfo roomInfo, String command) {
		roomInfo.updatePlayerCount();
		String msg = command + "\t";
		msg += roomInfo.exportString();
		msg += "\n";
		broadcast(msg);
		broadcastAdmin(msg);
	}

	/**
	 * Send player list to specified client
	 *
	 * @param client Client to send
	 */
	private void sendPlayerList(SocketChannel client) {
		String msg = "playerlist\t" + playerInfos.size();

		for (SocketChannel ch : channels) {
			NetPlayerInfo pInfo = playerInfos.get(ch);

			if (pInfo != null) {
				msg += "\t";
				msg += pInfo.exportString();
			}
		}

		msg += "\n";
		send(client, msg);
	}

	/**
	 * Broadcast a player update information (command will be "playerupdate")
	 *
	 * @param pInfo The player
	 */
	private void broadcastPlayerInfoUpdate(NetPlayerInfo pInfo) {
		broadcastPlayerInfoUpdate(pInfo, "playerupdate");
	}

	/**
	 * Broadcast a player update information
	 *
	 * @param pInfo   The player
	 * @param command Command
	 */
	private void broadcastPlayerInfoUpdate(NetPlayerInfo pInfo, String command) {
		String msg = command + "\t";
		msg += pInfo.exportString();
		msg += "\n";
		broadcast(msg);
	}

	/**
	 * Get NetPlayerInfo by player's name
	 *
	 * @param name Name
	 * @return NetPlayerInfo (null if not found)
	 */
	private NetPlayerInfo searchPlayerByName(String name) {
		for (SocketChannel ch : channels) {
			NetPlayerInfo pInfo = playerInfos.get(ch);
			if (pInfo != null && pInfo.strName.equals(name)) {
				return pInfo;
			}
		}
		return null;
	}

	/**
	 * Get NetPlayerInfo by player's ID
	 *
	 * @param uid ID
	 * @return NetPlayerInfo (null if not found)
	 */
	private NetPlayerInfo searchPlayerByUID(int uid) {
		for (SocketChannel ch : channels) {
			NetPlayerInfo pInfo = playerInfos.get(ch);
			if (pInfo != null && pInfo.uid == uid) {
				return pInfo;
			}
		}
		return null;
	}

	/**
	 * Move queue player(s) to the game seat if possible
	 *
	 * @param roomInfo The room
	 * @return Number of players moved to the game seat
	 */
	private int joinAllQueuePlayers(NetRoomInfo roomInfo) {
		int playerJoinedCount = 0;

		while (roomInfo.canJoinSeat() && !roomInfo.playerQueue.isEmpty()) {
			NetPlayerInfo pInfo = roomInfo.playerQueue.removeFirst();
			pInfo.seatID = roomInfo.joinSeat(pInfo);
			pInfo.queueID = -1;
			pInfo.ready = false;
			broadcast("changestatus\tjoinseat\t" + pInfo.uid + "\t" + NetUtil.urlEncode(pInfo.strName) + "\t"
					+ pInfo.seatID + "\n", pInfo.roomID);
			broadcastPlayerInfoUpdate(pInfo);
			playerJoinedCount++;
		}

		if (playerJoinedCount > 0) {
			broadcastRoomInfoUpdate(roomInfo);
		}

		return playerJoinedCount;
	}

	/**
	 * Signal player-dead
	 *
	 * @param pInfo Player
	 */
	private void playerDead(NetPlayerInfo pInfo) {
		playerDead(pInfo, null);
	}

	/**
	 * Signal player-dead
	 *
	 * @param pInfo   Player
	 * @param pKOInfo Assailant (can be null)
	 */
	private void playerDead(NetPlayerInfo pInfo, NetPlayerInfo pKOInfo) {
		NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);

		if (roomInfo != null && pInfo.seatID != -1 && pInfo.playing && roomInfo.playing) {
			pInfo.resetPlayState();

			int place = roomInfo.startPlayers - roomInfo.deadCount;
			String msg = "dead\t" + pInfo.uid + "\t" + NetUtil.urlEncode(pInfo.strName) + "\t" + pInfo.seatID + "\t"
					+ place + "\t";
			if (pKOInfo == null) {
				msg += -1 + "\t" + "";
			} else {
				msg += pKOInfo.uid + "\t" + NetUtil.urlEncode(pKOInfo.strName);
			}
			msg += "\n";
			broadcast(msg, pInfo.roomID);

			roomInfo.deadCount++;
			roomInfo.playerSeatDead.addFirst(pInfo);
			gameFinished(roomInfo);

			broadcastPlayerInfoUpdate(pInfo);
		}
	}

	/**
	 * Sets a ban by IP address.
	 *
	 * @param strIP     IP address
	 * @param banLength The length of the ban. (-1: Kick only, not ban)
	 * @return Number of players kicked
	 */
	private int ban(String strIP, int banLength) {
		LinkedList<SocketChannel> banChannels = new LinkedList<>();

		for (SocketChannel ch : channels) {
			String ip = getHostAddress(ch);
			if (ip.equals(strIP)) {
				banChannels.add(ch);
			}
		}
		for (SocketChannel ch : banChannels) {
			ban(ch, banLength);
		}
		if (banChannels.isEmpty() && banLength >= 0) {
			// Add ban entry manually
			banList.add(new NetServerBan(strIP, banLength));
		}

		return banChannels.size();
	}

	/**
	 * Sets a ban.
	 *
	 * @param client    The remote address to ban.
	 * @param banLength The length of the ban. (-1: Kick only, not ban)
	 * @return Number of players kicked (always 1 in this routine)
	 */
	private int ban(SocketChannel client, int banLength) {
		String remoteAddr = getHostAddress(client);

		if (banLength < 0) {
			log.info("Kicked player: " + remoteAddr);
		} else {
			banList.add(new NetServerBan(remoteAddr, banLength));
			log.info("Banned player: " + remoteAddr);
		}

		logout(client);
		return 1;
	}

	/**
	 * Checks whether a connection is banned.
	 *
	 * @param client The remote address to check.
	 * @return true if the connection is banned, false if it is not banned or if the
	 *         ban is expired.
	 */
	private boolean checkConnectionOnBanlist(SocketChannel client) {
		return getBan(client) != null;
	}

	/**
	 * Get ban data of the connection.
	 *
	 * @param client The remote address to check.
	 * @return An instance of NetServerBan is the connection is banned, null
	 *         otherwise.
	 */
	private NetServerBan getBan(SocketChannel client) {
		String remoteAddr = getHostAddress(client);

		Iterator<NetServerBan> i = banList.iterator();
		NetServerBan ban;

		while (i.hasNext()) {
			ban = i.next();
			if (ban.addr.equals(remoteAddr)) {
				if (ban.isExpired()) {
					i.remove();
				} else {
					return ban;
				}
			}
		}

		return null;
	}

	/**
	 * Send rated-game rule list
	 *
	 * @param client Client
	 * @throws IOException When something bad occurs
	 */
	private void sendRatedRuleList(SocketChannel client) throws IOException {
		for (GameStyle style : GameStyle.values()) {
			String msg = "rulelist\t" + style.ordinal();
			for (RuleOptions rule : ruleList.get(style)) {
				msg += "\t" + NetUtil.urlEncode(rule.strRuleName);
			}
			msg += "\n";
			send(client, msg);
		}
		// send(client, "rulelistend\n");
	}

	/**
	 * Get rated-game rule
	 *
	 * @param style Style ID
	 * @param name  Rule Name
	 * @return Rated-game rule (null if not found)
	 */
	private RuleOptions getRatedRule(int style, String name) {
		if (style < 0 || style >= GameStyle.numStyles()) {
			return null;
		}
		for (RuleOptions rule : ruleList.get(GameStyle.values()[style])) {
			if (name.equals(rule.strRuleName)) {
				return rule;
			}
		}

		return null;
	}

	/**
	 * Get rated-game rule index
	 *
	 * @param style Style ID
	 * @param name  Rule Name
	 * @return Index (-1 if not found)
	 */
	/*
	 * private int getRatedRuleIndex(int style, String name) { for(int i = 0; i <
	 * ruleList[style].size(); i++) { RuleOptions rule =
	 * (RuleOptions)ruleList[style].get(i);
	 *
	 * if(name.equals(rule.strRuleName)) { return i; } }
	 *
	 * return -1; }
	 */

	/**
	 * Get new rating
	 *
	 * @param playedGames Number of games played by the player
	 * @param myRank      Player's rating
	 * @param oppRank     Opponent's rating
	 * @param myScore     0:Loss, 1:Win
	 * @return New rating
	 */
	private double rankDelta(int playedGames, double myRank, double oppRank, double myScore) {
		return maxDelta(playedGames) * (myScore - expectedScore(myRank, oppRank));
	}

	/**
	 * Subroutine of rankDelta; Returns expected score.
	 *
	 * @param myRank  Player's rating
	 * @param oppRank Opponent's rating
	 * @return Expected score
	 */
	private double expectedScore(double myRank, double oppRank) {
		return 1.0 / (1 + Math.pow(10, (oppRank - myRank) / 400.0));
	}

	/**
	 * Subroutine of rankDelta; Returns multiplier of rating change
	 *
	 * @param playedGames Number of games played by the player
	 * @return Multiplier of rating change
	 */
	private double maxDelta(int playedGames) {
		return playedGames > ratingProvisionalGames ? ratingNormalMaxDiff
				: ratingNormalMaxDiff + 400d / (playedGames + 3);
	}

	/**
	 * Write server-status file
	 */
	private void writeServerStatusFile() {
		if (!propServer.getProperty("netserver.writestatusfile", false)) {
			return;
		}

		String status = propServer.getProperty("netserver.statusformat", "$observers/$players");
		status = status.replace("\\$version", version.majorMinor());
		status = status.replace("\\$observers", Integer.toString(observers.size()));
		status = status.replace("\\$players", Integer.toString(playerInfos.size()));
		status = status.replace("\\$clients", Integer.toString(observers.size() + playerInfos.size()));
		status = status.replace("\\$rooms", Integer.toString(roomInfos.size()));
		String file = propServer.getProperty("netserver.statusfilename", "status.txt");
		try (var outFile = new FileWriter(file)) {
			outFile.write(status);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pending changes
	 */
	private static class ChangeRequest {
		/** Delayed disconnect action */
		public static final int DISCONNECT = 1;
		/** interestOps change action */
		public static final int CHANGEOPS = 2;

		public SocketChannel socket;
		public int type;
		public int ops;

		public ChangeRequest(SocketChannel socket, int type, int ops) {
			this.socket = socket;
			this.type = type;
			this.ops = ops;
		}
	}
}

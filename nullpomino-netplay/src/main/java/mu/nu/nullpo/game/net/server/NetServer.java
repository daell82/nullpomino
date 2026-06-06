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
package mu.nu.nullpo.game.net.server;

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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Adler32;

import org.apache.log4j.PropertyConfigurator;
import org.cacas.java.gnu.tools.Crypt;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.net.NetChatMessage;
import mu.nu.nullpo.game.net.NetCmd;
import mu.nu.nullpo.game.net.NetMessage;
import mu.nu.nullpo.game.net.NetPlayerInfo;
import mu.nu.nullpo.game.net.NetRoomInfo;
import mu.nu.nullpo.game.net.NetServerBan;
import mu.nu.nullpo.game.net.NetServerDisconnectRequestedException;
import mu.nu.nullpo.game.net.NetUtil;
import mu.nu.nullpo.game.net.ranking.NetSPRanking;
import mu.nu.nullpo.game.net.ranking.NetSPRecord;
import mu.nu.nullpo.game.net.ranking.NetSPRecord.RankingType;
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
	public static final long DEFAULT_TIMEOUT_TIME = 1000L * 60L;

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

	/** Min range of rating */
	private int ratingMin;

	/** Max range of rating */
	private int ratingMax;

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
	private final List<String> ratedInfoList = new LinkedList<>();

	/** Rule list for rated game. */
	private final EnumMap<GameStyle, List<RuleOptions>> ruleList = new EnumMap<>(GameStyle.class);

	/** Multiplayer leaderboard list. */
	private final EnumMap<GameStyle, List<NetPlayerInfo>> mpRankings = new EnumMap<>(GameStyle.class);

	/** Multiplayer mode list */
	private final EnumMap<GameStyle, List<String>> mpModeList = new EnumMap<>(GameStyle.class);

	/** Multiplayer race mode flag */
	private final EnumMap<GameStyle, List<Boolean>> mpModeIsRace = new EnumMap<>(GameStyle.class);

	/** Single player mode list. */
	private final EnumMap<GameStyle, List<String>> spModeList = new EnumMap<>(GameStyle.class);

	/** Single player all-time leaderboard list */
	private final List<NetSPRanking> spRankingListAlltime = new LinkedList<>();

	/** Single player daily leaderboard list */
	private final List<NetSPRanking> spRankingListDaily = new LinkedList<>();

	/** Last-update time of single player daily leaderboard */
	private Calendar spDailyLastUpdate;

	/** Ban list */
	private final List<NetServerBan> serverBans = new LinkedList<>();

	/** Lobby chat message history */
	private final List<NetChatMessage> lobbyChats = new LinkedList<>();

	/** List of SocketChannel */
	private final List<SocketChannel> channels = new LinkedList<>();

	/** Last communication time */
	private final Map<SocketChannel, Long> lastCommTime = new HashMap<>();

	/** Incomplete packet buffer */
	private final Map<SocketChannel, StringBuilder> notCompletePacketMap = new HashMap<>();

	/** Player info */
	private final Map<SocketChannel, NetPlayerInfo> playerInfos = new HashMap<>();

	/** Room info list */
	private final List<NetRoomInfo> rooms = new LinkedList<>();

	/** Observer list */
	private final List<SocketChannel> observers = new LinkedList<>();

	/** Admin list */
	private final List<SocketChannel> admins = new LinkedList<>();

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

	/** The selector we'll be monitoring */
	private Selector selector;

	/** The buffer into which we'll read data when it's available */
	private ByteBuffer readBuffer;

	/** A list of ChangeRequest instances */
	private final List<ChangeRequest> pendingChanges = new LinkedList<>();

	/** Maps a SocketChannel to a list of ByteBuffer instances */
	private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();

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

		for (GameStyle style : GameStyle.values()) {
			ruleList.put(style, new LinkedList<>());
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
					CustomProperties prop = CustomProperties.load(str);
					RuleOptions rule = RuleOptions.of(prop, 0);
					ruleList.get(style).add(rule);
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
		for (GameStyle style : GameStyle.values()) {
			mpModeList.put(style, new LinkedList<>());
			mpModeIsRace.put(style, new LinkedList<>());
		}
		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get("config/list/netlobby_multimode.lst"));
		} catch (Exception e) {
			log.warn("Failed to load multiplayer mode list", e);
			return;
		}
		GameStyle style = GameStyle.TETROMINO;
		for (String line : lines) {
			if (line.isEmpty() || line.startsWith("#")) {
				// Empty line or comment line. Ignore it.
			} else if (line.startsWith(":")) {
				// Game style tag
				String strStyle = line.substring(1);

				style = GameStyle.valueOf(strStyle);
				if (style == null) {
					style = GameStyle.TETROMINO;
				}
			} else {
				// Game mode name
				String[] strSplit = line.split(",");
				String strModeName = strSplit[0];
				boolean isRace = false;
				if (strSplit.length > 1) {
					isRace = Boolean.parseBoolean(strSplit[1]);
				}
				mpModeList.get(style).add(strModeName);
				mpModeIsRace.get(style).add(isRace);
			}
		}

		// Load leaderboard
		log.info("Loading Multiplayer Ranking...");
		for (GameStyle gameStyle : GameStyle.values()) {
			int styleIdx = gameStyle.ordinal();
			mpRankings.put(gameStyle, new LinkedList<>());
			int count = Math.min(propMPRanking.getProperty(styleIdx + ".mpranking.count", 0), maxMPRanking);
			for (int i = 0; i < count; i++) {
				NetPlayerInfo p = new NetPlayerInfo();
				p.setPlayerName(propMPRanking.getProperty(styleIdx + ".mpranking.strName." + i, ""));
				p.rating[styleIdx] = propMPRanking.getProperty(styleIdx + ".mpranking.rating." + i, ratingDefault);
				p.playCount[styleIdx] = propMPRanking.getProperty(styleIdx + ".mpranking.playCount." + i, 0);
				p.winCount[styleIdx] = propMPRanking.getProperty(styleIdx + ".mpranking.winCount." + i, 0);
				mpRankings.get(gameStyle).add(p);
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
		int rank = 0;
		for (NetPlayerInfo pInfo : mpRankings.get(style)) {
			if (player.getPlayerName().equals(pInfo.getPlayerName())) {
				return rank;
			}
			rank++;
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
				propMPRanking.setProperty(style + ".mpranking.strName." + i, p.getPlayerName());
				propMPRanking.setProperty(style + ".mpranking.rating." + i, p.rating[style]);
				propMPRanking.setProperty(style + ".mpranking.playCount." + i, p.playCount[style]);
				propMPRanking.setProperty(style + ".mpranking.winCount." + i, p.winCount[style]);
			}
		}

		try {
			String filename = "config/setting/netserver_mpranking.cfg";
			String comment = "NullpoMino NetServer Multiplayer Leaderboard";
			propMPRanking.save(filename, comment);
		} catch (IOException e) {
			log.error("Failed to write multiplayer ranking data", e);
		}
	}

	/**
	 * Load single player leaderboard
	 */
	private void loadSPRankingList() {
		log.info("Loading Single Player Ranking...");

		spRankingListAlltime.clear();
		spRankingListDaily.clear();

		// Load mode list
		for (GameStyle style : GameStyle.values()) {
			spModeList.put(style, new LinkedList<>());
		}

		// Daily last-update
		TimeZone z = !spDailyTimeZone.isEmpty() ? TimeZone.getTimeZone(spDailyTimeZone) : TimeZone.getDefault();
		spDailyLastUpdate = GeneralUtil.importCalendarString(propSPRankingDaily.getProperty("daily.lastupdate", ""));
		if (spDailyLastUpdate != null) {
			spDailyLastUpdate.setTimeZone(z);
		}
		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get("config/list/netlobby_singlemode.lst"));
		} catch (IOException ioe) {
			log.error("fail)ed to load single player modes", ioe);
			lines = Collections.emptyList();
		}
		GameStyle style = GameStyle.TETROMINO;
		for (String line : lines) {
			if (line.isEmpty() || line.startsWith("#")) {
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
				RankingType rankingType = RankingType.GENERIC_SCORE;
				int maxGameType = 0;
				if (strSplit.length > 1) {
					rankingType = RankingType.values()[Integer.parseInt(strSplit[1])];
				}
				if (strSplit.length > 2) {
					maxGameType = Integer.parseInt(strSplit[2]);
				}
				log.debug("Mode:" + modeName + " RankingType:" + rankingType + " MaxGameType:" + maxGameType);

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
							// @formatter:off
							NetSPRanking rankingData = NetSPRanking.builder()
									.modeName(modeName)
									.ruleName(ruleName)
									.gameType(j)
									.rankingType(rankingType)
									.style(style)
									.maxRecords(maxSPRanking)
									.build();
							// @formatter:on
							if (k == 0) {
								rankingData.readProperty(propSPRankingAlltime);
								spRankingListAlltime.add(rankingData);
								log.debug(rankingData.getRuleName() + "," + rankingData.getModeName() + ","
										+ rankingData.getGameType());
							} else {
								rankingData.readProperty(propSPRankingDaily);
								spRankingListDaily.add(rankingData);
							}
						}
					}
				}
			}
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
			if (r.getRuleName().equals(rule) && r.getModeName().equals(mode) && r.getGameType() == gtype) {
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
			if (r.getModeName().equals(mode) && r.getGameType() == gtype) {
				allRanks.add(r);
			}
		}
		return NetSPRanking.mergeRankings(allRanks);
	}

	/**
	 * Update the last-update variable of daily ranking, and wipe the records if
	 * needed
	 *
	 * @return <code>true</code> if the records are wiped
	 */
	private boolean updateSPDailyRanking() {
		TimeZone z = !spDailyTimeZone.isEmpty() ? TimeZone.getTimeZone(spDailyTimeZone) : TimeZone.getDefault();
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
			r.getRecords().clear();
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
		if (pInfo.isTripUse()) {
			for (int i = 0; i < GameStyle.numStyles(); i++) {
				pInfo.rating[i] = propPlayerData.getProperty("p.rating." + i + "." + pInfo.getPlayerName(),
						ratingDefault);
				pInfo.playCount[i] = propPlayerData.getProperty("p.playCount." + i + "." + pInfo.getPlayerName(), 0);
				pInfo.winCount[i] = propPlayerData.getProperty("p.winCount." + i + "." + pInfo.getPlayerName(), 0);
			}
			pInfo.readProperty(propPlayerData);
		} else {
			for (int i = 0; i < GameStyle.numStyles(); i++) {
				pInfo.rating[i] = ratingDefault;
				pInfo.playCount[i] = 0;
				pInfo.winCount[i] = 0;
			}
		}
	}

	/**
	 * Set player data to propPlayerData
	 *
	 * @param pInfo NetPlayerInfo
	 */
	private void setPlayerDataToProperty(NetPlayerInfo pInfo) {
		if (pInfo.isTripUse()) {
			for (int i = 0; i < GameStyle.numStyles(); i++) {
				propPlayerData.setProperty("p.rating." + i + "." + pInfo.getPlayerName(), pInfo.rating[i]);
				propPlayerData.setProperty("p.playCount." + i + "." + pInfo.getPlayerName(), pInfo.playCount[i]);
				propPlayerData.setProperty("p.winCount." + i + "." + pInfo.getPlayerName(), pInfo.winCount[i]);
			}
			pInfo.writeProperty(propPlayerData);
		}
	}

	/**
	 * Write player data properties (propPlayerData) to a file
	 */
	private void writePlayerDataToFile() {
		try {
			String filename = "config/setting/netserver_playerdata.cfg";
			propPlayerData.save(filename, "NullpoMino NetServer PlayerData");
		} catch (IOException e) {
			log.error("Failed to write player data", e);
		}
	}

	/**
	 * Load ban list from a file
	 */
	private void loadBanList() {
		serverBans.clear();
		try {
			List<String> lines = Files.readAllLines(new File("config/setting/netserver_banlist.cfg").toPath());
			for (String line : lines) {
				if (line.isEmpty()) {
					continue;
				}
				NetServerBan ban = new NetServerBan();
				ban.importString(line);
				if (!ban.isExpired()) {
					serverBans.add(ban);
				}
			}
		} catch (IOException _) {
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
			for (NetServerBan ban : serverBans) {
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
		lobbyChats.clear();

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
		} catch (Exception _) {
			// ignore
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
		} catch (Exception _) {
			// ignore
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
		} catch (Exception _) {
			// ignore
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
			} catch (NumberFormatException _) {
				// ignore
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
		propPlayerData = CustomProperties.load("config/setting/netserver_playerdata.cfg");
		// Load multiplayer leaderboard file
		propMPRanking = CustomProperties.load("config/setting/netserver_mpranking.cfg");
		// Load single player leaderboard file
		propSPRankingAlltime = CustomProperties.load("config/setting/netserver_spranking.cfg");
		propSPRankingDaily = CustomProperties.load("config/setting/netserver_spranking_daily.cfg");
		// Load single player personal best

		// XXX CustomProperties propSPPersonalBest =
		// CustomProperties.load("config/setting/netserver_sppersonalbest.cfg");

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
		/** The channel on which we'll accept connections */
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(port);
		log.info("Try binding to: " + isa);
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
						} catch (NetServerDisconnectRequestedException _) {
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
			if (endDate != null) {
				var expire = GeneralUtil.exportCalendarString(endDate);
				send(socketChannel, NetCmd.BANNED, strStart, expire);
			} else {
				send(socketChannel, NetCmd.BANNED, strStart);
			}
			synchronized (pendingChanges) {
				pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.DISCONNECT, 0));
			}
		} else {
			// Send welcome message
			log.debug("Accept:" + getHostName(socketChannel));
			send(socketChannel, NetCmd.WELCOME, version, playerInfos.size(), observers.size(), clientPingInterval);
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
			disconnect("Connection is closed (numBytesRead is -1)");
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
			String fullMsg = packetBuffer.substring(0, index);
			NetMessage netMessage = NetMessage.of(fullMsg.split("\t"));
			processPacket(socketChannel, netMessage);
			packetBuffer = packetBuffer.delete(0, index + 1);
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
		} catch (CancelledKeyException _) {
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

			NetPlayerInfo player = playerInfos.remove(channel);
			if (player != null) {
				log.info(player.getPlayerName() + " has logged out");

				playerDead(player);
				player.setConnected(false);
				player.setReady(false);

				List<NetRoomInfo> deleteList = new LinkedList<>(); // Room delete check list

				for (NetRoomInfo room : rooms) {
					var seats = room.getSeats();
					if (!seats.contains(player)) {
						continue;
					}
					seats.remove(player);
					room.playerQueue.remove(player);
					room.exitSeat(player);
					deleteList.add(room);
				}

				for (NetRoomInfo roomInfo : deleteList) {
					if (!deleteRoom(roomInfo)) {
						continue;
					}
					joinAllQueuePlayers(roomInfo);
					if (!gameFinished(roomInfo) && !gameStartIfPossible(roomInfo)) {
						autoStartTimerCheck(roomInfo);
						broadcastRoomInfoUpdate(roomInfo);
					}
				}
				broadcastPlayerInfoUpdate(player, NetCmd.PLAYER_LOGOUT);
				player.delete();
			}
			if (observers.remove(channel)) {
				log.info("Observer logout (" + remoteAddr + ")");
			}
			if (admins.remove(channel)) {
				log.info("Admin logout (" + remoteAddr + ")");
			}
			broadcastUserCountToAll();
			adminSendClientList();

			log.debug("Channel close success");
		} catch (Exception e) {
			log.warn("Exception during logout", e);
		}

		if (channels.isEmpty()) {
			cleanup();
		} else if (playerInfos.isEmpty()) {
			rooms.clear();
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
		rooms.clear();
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
	private void sendBytes(SocketChannel client, byte[] bytes) {
		synchronized (pendingChanges) {
			// Indicate we want the interest ops set changed
			pendingChanges.add(new ChangeRequest(client, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (pendingData) {
				List<ByteBuffer> queue = pendingData.computeIfAbsent(client, _ -> new LinkedList<>());
				queue.add(ByteBuffer.wrap(bytes));
			}
		}

		// Finally, wake up our selecting thread so it can make the required changes
		selector.wakeup();
	}

	/**
	 * Send a message
	 *
	 * @param client to send to
	 * @param cmd    command type of the message
	 * @param parts  to be added as arguments
	 */
	public void send(SocketChannel client, NetCmd cmd, Object... parts) {
		String message = cmd.command();
		if (parts.length > 0) {
			message += "\t";
			message += Stream.of(parts).map(Object::toString).collect(Collectors.joining("\t"));
		}
		message += "\n";
		log.debug("sending message: " + cmd);
		sendBytes(client, NetUtil.stringToBytes(message));
	}

	/**
	 * Broadcast a message to all players
	 *
	 * @param msg Message to send (String)
	 */
	public void broadcast(NetCmd cmd, String msg) {
		synchronized (channels) {
			for (SocketChannel channel : channels) {
				NetPlayerInfo p = playerInfos.get(channel);
				if (p != null) {
					send(channel, cmd, msg);
				}
			}
		}
	}

	/**
	 * Broadcast a message to all players in specific room
	 *
	 * @param cmd    Command type of the message
	 * @param msg    Message to send (String)
	 * @param roomID Room ID (-1:Lobby)
	 */
	public void broadcastRoom(int roomID, NetCmd cmd, Object... parts) {
		String msg = Stream.of(parts).map(Object::toString).collect(Collectors.joining("\t"));
		synchronized (channels) {
			for (SocketChannel channel : channels) {
				NetPlayerInfo player = playerInfos.get(channel);
				if (player != null && roomID == player.roomID) {
					send(channel, cmd, msg);
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
	public void broadcastRoom(int roomID, NetPlayerInfo pInfo, NetCmd cmd, Object... parts) {
		String msg = Stream.of(parts).map(Object::toString).collect(Collectors.joining("\t"));
		synchronized (channels) {
			for (SocketChannel channel : channels) {
				NetPlayerInfo player = playerInfos.get(channel);
				if (player != null && player.uid != pInfo.uid && roomID == player.roomID) {
					send(channel, cmd, msg);
				}
			}
		}
	}

	/**
	 * Broadcast a message to all observers
	 *
	 * @param msg Message to send (String)
	 */
	public void broadcastObserver(NetCmd cmd, String msg) {
		for (SocketChannel channel : observers) {
			send(channel, cmd, msg);
		}
	}

	/**
	 * Broadcast client count (observers and players) to everyone
	 */
	public void broadcastUserCountToAll() {
		String msg = playerInfos.size() + "\t" + observers.size();
		broadcast(NetCmd.OBSERVER_UPDATE, msg);
		broadcastObserver(NetCmd.OBSERVER_UPDATE, msg);
		writeServerStatusFile();
	}

	/**
	 * Broadcast a message to all admins
	 *
	 * @param msg Message to send (String)
	 */
	public void broadcastAdmin(NetCmd cmd, String msg) {
		for (SocketChannel channel : admins) {
			send(channel, cmd, msg);
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
			int maxLen = 0;
			int len = 0;
			String player;
			SocketChannel chMatch = null;
			for (SocketChannel ch : channels) {
				NetPlayerInfo p = playerInfos.get(ch);
				len = p.getPlayerName().length();
				player = p.getPlayerName();
				if (p.isTripUse()) {
					len -= 12;
					player = player.substring(0, len);
				}
				if (len + 1 < msg.length() && msg.substring(0, len + 1).equals(player + " ") && len > maxLen) {
					chMatch = ch;
					maxLen = len;
				}

			}
			return chMatch;
		}
	}

	private void disconnect(String message) {
		// XXX this is nowhere handled explicitly
		throw new NetServerDisconnectRequestedException(message);
	}

	/**
	 * Process a packet.
	 *
	 * @param client      The SocketChannel who sent this packet
	 * @param fullMessage The string of packet
	 * @throws IOException When something bad happens
	 */
	private void processPacket(SocketChannel client, NetMessage message) {
		// Check ban
		if (checkConnectionOnBanlist(client)) {
			disconnect("Connection banned");
		}

		// Setup Variables
		NetPlayerInfo player = playerInfos.get(client); // NetPlayerInfo of this client. null if not logged in.

		// Update last communication time
		lastCommTime.put(client, System.currentTimeMillis());
		switch (message.command()) {
		case GET_INFO -> serverInfo(client); // Get information of this server.
		case DISCONNECT -> disconnect("Disconnect requested by the client (this is normal)");
		case PING -> ping(client, message);
		case PLAYER_LOGIN -> playerLogin(client, message);
		case ADMIN_LOGIN -> adminLogin(client, message);// ADMIN: Admin Login
		case OBSERVER_LOGIN -> observerLogin(client, message);
		case GET_PRESETS -> sendRatedPresets(client);
		case RULE_DATA -> receivePlayerRuleData(client, message);
		case RULE_GET -> sendRuleData(client, message);
		case RULE_GET_RATED -> sendRatedRule(client, message); // Send rated-game rule data (Server->Client)
		case LOBBY_CHAT -> lobbyChat(client, message);
		case ROOM_CHAT -> roomChat(player, message);
		case MP_RANKING -> sendMultiPlayerRanking(client, message);
		case ROOM_CREATE_SP -> createSinglePlayerRoom(player, message);
		case ROOM_CREATE -> createMultiPlayerRoom(player, message);
		case ROOM_CREATE_RATED -> createRatedRoom(player, message);
		case ROOM_JOIN -> joinRoom(player, message); // Join room (If roomID is -1, the player will return to lobby)
		case CHANGE_TEAM -> changeTeam(player, message);
		case CHANGE_STATUS -> changeStatus(player, message); // Change Player/Spectator status
		case START_1P -> startSinglePlayGame(player); // Start game (Single player)
		case READY -> playerReady(player, message); // Ready state change
		case AUTOSTART -> gameAutoStart(player);
		case DEAD -> playerDead(player, message);
		case RACE_WIN -> raceWin(player, message);
		case GSTAT -> multiplayerGameStats(player, message); // Multiplayer end-of-game stats
		case GSTAT_1P -> singleplayerGameStats(player, message); // Single player end-of-game stats
		case SP_SEND -> sendReplay(player, message); // Single player replay send
		case SP_RANKING -> sendSingleplayerLeaderboard(client, message); // Single player leaderboard
		case SP_DOWNLOAD -> sendReplayDownload(client, message); // Single player replay download
		case RESET_SP -> resetSinglePlayer(player); // Single player mode reset
		case GAME -> broadcastGameMessage(player, message);
		case ADMIN -> processAdminCommand(client, message); // ADMIN: Admin commands
		case null -> log.warn("received null command in message: " + message);
		default -> log.warn("ignoring unknown command: " + message.command());
		}
	}

	/**
	 * Process admin command
	 *
	 * @param client  The SocketChannel who sent this packet
	 * @param message The String array of the command
	 */
	private void processAdminCommand(SocketChannel client, NetMessage message) {
		if (!admins.contains(client)) {
			log.warn(getHostFull(client) + " has tried to access admin command without login");
			logout(client);
			return;
		}
		String admMessage = message.decompressed(0);
		String[] commands = admMessage.split("\t");
		NetMessage subMessage = NetMessage.of(commands);
		switch (subMessage.command()) {
		case ANNOUNCE -> broadcast(NetCmd.ANNOUNCE, commands[1] + "\n");
		case BAN -> banPlayer(client, commands);
		case BAN_LIST -> getBanlist(client);
		case CLIENT_LIST -> adminSendClientList(client);
		case UNBAN -> unbanPlayer(client, commands);
		case PLAYER_DELETE -> deletePlayer(client, commands[1]);
		case ROOM_DELETE -> deleteRoom(client, commands);
		case SHUTDOWN -> shutdown(client);
		case null, default -> log.debug("received unknown command: " + Arrays.toString(commands));
		}
	}

	private void serverInfo(SocketChannel client) {
		send(client, NetCmd.GET_INFO, version, playerInfos.size(), observers.size());
	}

	private void ping(SocketChannel client, NetMessage message) {
		// ping\t[ID]
		if (message.length() > 0) {
			send(client, NetCmd.PONG, message.text(0));
		} else {
			send(client, NetCmd.PONG);
		}
		// Kill dead connections
		killTimeoutConnections(timeoutTime);
	}

	private void observerLogin(SocketChannel client, NetMessage message) {
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
		Version clientVersion = Version.of(message.text(0));
		if (!version.isCompatible(clientVersion)) {
			send(client, NetCmd.OBSERVER_LOGIN_FAIL, "incompatible version", version);
			synchronized (pendingChanges) {
				pendingChanges.add(new ChangeRequest(client, ChangeRequest.DISCONNECT, 0));
			}
			return;
		}

		// Kill dead connections
		killTimeoutConnections(timeoutTime);

		// Success
		observers.add(client);
		send(client, NetCmd.OBSERVER_LOGIN_SUCCESS);
		broadcastUserCountToAll();
		adminSendClientList();

		log.info("New observer has logged in (" + client.toString() + ")");
	}

	private void playerLogin(SocketChannel client, NetMessage message) {
		// login | [VERSION] | [NAME] | [COUNTRY] | [TEAM]

		// Ignore it if already logged in
		if (observers.contains(client) || admins.contains(client) || playerInfos.containsKey(client)) {
			log.warn("client tried to login twice: " + client);
			return;
		}

		// Version check
		Version clientVersion = Version.of(message.text(0));
		if (!version.isCompatible(clientVersion)) {
			send(client, NetCmd.PLAYER_LOGIN_FAIL, "incompatible version", version);
			synchronized (pendingChanges) {
				pendingChanges.add(new ChangeRequest(client, ChangeRequest.DISCONNECT, 0));
			}
			return;
		}

		// Kill dead connections
		killTimeoutConnections(timeoutTime);

		// Tripcode
		String originalName = message.urlDecoded(1);
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
		NetPlayerInfo pInfo = new NetPlayerInfo();
		pInfo.setPlayerName(name);
		if (message.length() > 2) {
			pInfo.setCountry(message.text(2));
		}
		if (message.length() > 3) {
			pInfo.setTeam(message.urlDecoded(3));
		}
		pInfo.uid = playerCount;
		pInfo.setConnected(true);
		pInfo.setTripUse(isTripUse);

		pInfo.realHost = getHostName(client);
		pInfo.realIP = getHostAddress(client);
		pInfo.channel = client;

		int showhosttype = propServer.getProperty("netserver.showhosttype", 0);
		switch (showhosttype) {
		case 1:
			pInfo.setHost(getHostAddress(client));
			break;
		case 2:
			pInfo.setHost(getHostName(client));
			break;
		case 3: {
			String host = Crypt.crypt(propServer.getProperty("netserver.hostsalt", "AA"), getHostAddress(client));
			int maxlen = propServer.getProperty("netserver.hostcryptmax", 8);
			if (host.length() > maxlen) {
				host = host.substring(host.length() - maxlen);
			}
			pInfo.setHost(host);
			break;
		}
		case 4: {
			String host = Crypt.crypt(propServer.getProperty("netserver.hostsalt", "AA"), getHostName(client));
			int maxlen = propServer.getProperty("netserver.hostcryptmax", 8);
			if (host.length() > maxlen) {
				host = host.substring(host.length() - maxlen);
			}
			pInfo.setHost(host);
			break;
		}
		default:
			break;
		}

		// Load rating
		getPlayerDataFromProperty(pInfo);

		// Success
		playerInfos.put(client, pInfo);
		playerCount++;
		send(client, NetCmd.PLAYER_LOGIN_SUCCESS, NetUtil.urlEncode(pInfo.getPlayerName()), pInfo.uid);
		log.info(pInfo.getPlayerName() + " has logged in (Host:" + getHostName(client) + " Team:"
				+ pInfo.getTeam().orElse("") + ")");

		sendRatedRuleList(client);
		sendPlayerList(client);
		sendRoomList(client);

		broadcastPlayerInfoUpdate(pInfo, NetCmd.PLAYER_NEW);
		broadcastUserCountToAll();
		adminSendClientList();

		// Send lobby chat history
		while (lobbyChats.size() > maxLobbyChatHistory) {
			lobbyChats.removeFirst();
		}
		for (NetChatMessage chat : lobbyChats) {
			send(client, NetCmd.LOBBY_CHAT_HIST, NetUtil.urlEncode(chat.strUserName),
					GeneralUtil.exportCalendarString(chat.timestamp), NetUtil.urlEncode(chat.strMessage));
		}
	}

	private void adminLogin(SocketChannel client, NetMessage message) {
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

		String address = getHostFull(client);

		// Check version
		Version clientVer = Version.of(message.text(0));
		if (!version.isCompatible(clientVer)) {
			String strLogMsg = address + " has tried to access admin, but client version is different (" + clientVer
					+ ")";
			log.warn(strLogMsg);
			disconnect(strLogMsg);
		}

		// Check username and password
		String strServerUsername = propServer.getProperty("netserver.admin.username", "");
		String strServerPassword = propServer.getProperty("netserver.admin.password", "");
		if (strServerUsername.isEmpty() || strServerPassword.isEmpty()) {
			log.warn(address + " has tried to access admin, but admin is disabled");
			send(client, NetCmd.ADMIN_LOGIN_FAIL, "DISABLE");
			return;
		}

		String clientName = message.text(1);
		if (!clientName.equals(strServerUsername)) {
			log.warn(address + " has tried to access admin with incorrect username (" + clientName + ")");
			send(client, NetCmd.ADMIN_LOGIN_FAIL, "FAIL");
			return;
		}

		RC4 rc4 = new RC4(strServerPassword);
		byte[] bPass = Base64.getDecoder().decode(message.text(2));
		byte[] bPass2 = rc4.rc4(bPass);
		String strClientPasswordCheckData = NetUtil.bytesToString(bPass2);
		if (!strClientPasswordCheckData.equals(strServerUsername)) {
			log.warn(address + " has tried to access admin with incorrect password (Username:" + clientName + ")");
			send(client, NetCmd.ADMIN_LOGIN_FAIL, "FAIL");
			return;
		}

		// Kill dead connections
		killTimeoutConnections(timeoutTime);

		// Login successful
		admins.add(client);
		send(client, NetCmd.ADMIN_LOGIN_SUCCESS, getHostAddress(client), getHostName(client));
		adminSendClientList();
		sendRoomList(client);
		log.info("Admin has logged in (" + address + ")");
	}

	private void receivePlayerRuleData(SocketChannel client, NetMessage message) {
		// ruledata\t[ADLER32CHECKSUM]\t[RULEDATA]
		NetPlayerInfo pInfo = playerInfos.get(client);
		if (pInfo == null) {
			return; // disconnect player here?
		}
		String strData = message.text(1);

		// Is checksum correct?
		Adler32 checksumObj = new Adler32();
		checksumObj.update(NetUtil.stringToBytes(strData));
		long sChecksum = checksumObj.getValue();
		long cChecksum = message.asLong(0);

		// OK
		if (sChecksum == cChecksum) {
			String strRuleData = NetUtil.decompressString(strData);

			CustomProperties prop = new CustomProperties();
			prop.decode(strRuleData);
			pInfo.setRule(RuleOptions.of(prop, 0));
			send(client, NetCmd.RULE_DATA_SUCCESS);
		}
		// FAIL
		else {
			send(client, NetCmd.RULE_DATA_FAIL, sChecksum);
		}
	}

	private void sendRuleData(SocketChannel client, NetMessage message) {
		// ruleget\t[UID]
		int uid = message.asInt(0);
		NetPlayerInfo pInfo = searchPlayerByUID(uid);
		if (pInfo != null) {
			RuleOptions rule = pInfo.getRule();
			if (rule == null) {
				rule = new RuleOptions();
			}
			CustomProperties prop = new CustomProperties();
			rule.writeProperty(prop, 0);
			pInfo.setRule(RuleOptions.of(prop, 0));
			String strRuleTemp = prop.encode("RuleData " + pInfo.getPlayerName());
			String strRuleData = NetUtil.compressString(strRuleTemp);

			// Checksum
			Adler32 checksumObj = new Adler32();
			checksumObj.update(NetUtil.stringToBytes(strRuleData));
			long sChecksum = checksumObj.getValue();

			send(client, NetCmd.RULE_GET_SUCCESS, uid, sChecksum, strRuleData);
		} else {
			send(client, NetCmd.RULE_GET_FAIL, uid);
		}
	}

	private void sendRatedRule(SocketChannel client, NetMessage message) {
		NetPlayerInfo pInfo = playerInfos.get(client);
		if (pInfo != null) { // is this necessary?
			return;
		}
		// rulegetrated\t[STYLE]\t[NAME]

		GameStyle style = GameStyle.values()[message.asInt(0)];
		String name = message.text(1);
		RuleOptions rule = getRatedRule(style, name);
		if (rule == null) {
			send(client, NetCmd.RULE_GET_RATED_RULE_FAIL, style.ordinal(), name);
			return;
		}
		CustomProperties prop = new CustomProperties();
		rule.writeProperty(prop, 0);
		String strRuleTemp = prop.encode("Rated RuleData " + rule.strRuleName);
		String strRuleData = NetUtil.compressString(strRuleTemp);

		// Checksum
		Adler32 checksumObj = new Adler32();
		checksumObj.update(NetUtil.stringToBytes(strRuleData));
		long sChecksum = checksumObj.getValue();
		send(client, NetCmd.RULE_GET_RATED_RULE_SUCCESS, style.ordinal(), name, sChecksum, strRuleData);
	}

	private void lobbyChat(SocketChannel client, NetMessage message) {
		// lobbychat\t[MESSAGE]

		NetPlayerInfo pInfo = playerInfos.get(client); // NetPlayerInfo of this client. null if not logged in.
		if (pInfo == null) {
			return;
		}
		NetChatMessage chat = new NetChatMessage(message.urlDecoded(0), pInfo);

		// Begin temporary private message code here
		String msg = chat.strMessage;
		if (msg.length() > 5 && msg.substring(0, 5).equals("/msg ")) {
			SocketChannel ch = findPlayerByMsg(msg.substring(5));
			if (ch == null) {
				// @formatter:off
				send(pInfo.channel, NetCmd.LOBBY_CHAT, chat.uid, NetUtil.urlEncode(chat.strUserName),
						GeneralUtil.exportCalendarString(chat.timestamp),
						NetUtil.urlEncode("(private) Cannot find user"));
			} else {
				NetPlayerInfo p = playerInfos.get(ch);
				String playerName = p.getPlayerName();
				int len = playerName.length();
				if (p.isTripUse()) {
					len -= 12;
				}
				msg = chat.strMessage.substring(len + 6);
				send(pInfo.channel, NetCmd.LOBBY_CHAT, chat.uid, NetUtil.urlEncode(chat.strUserName),
						GeneralUtil.exportCalendarString(chat.timestamp),
						NetUtil.urlEncode("-> *" + playerName.substring(0, len) + "* " + msg));
				send(ch, NetCmd.LOBBY_CHAT, chat.uid, NetUtil.urlEncode(chat.strUserName),
						GeneralUtil.exportCalendarString(chat.timestamp),
						NetUtil.urlEncode("(private) " + msg) + "\n");
				// @formatter:on
			}
		} else {
			// End here
			chat.outputLog();
			lobbyChats.add(chat);
			while (lobbyChats.size() > maxLobbyChatHistory) {
				lobbyChats.removeFirst();
			}
			saveLobbyChatHistory();

			broadcast(NetCmd.LOBBY_CHAT,
					chat.uid + "\t" + NetUtil.urlEncode(chat.strUserName) + "\t"
							+ GeneralUtil.exportCalendarString(chat.timestamp) + "\t"
							+ NetUtil.urlEncode(chat.strMessage) + "\n");
		}
	}

	private void roomChat(NetPlayerInfo player, NetMessage message) {
		// chat\t[MESSAGE]
		if (player == null || player.roomID == -1) {
			return;
		}
		NetRoomInfo roomInfo = getRoomInfo(player.roomID);
		if (roomInfo == null) {
			return;
		}
		NetChatMessage chat = new NetChatMessage(message.urlDecoded(0), player, roomInfo);
		chat.outputLog();
		roomInfo.chats.add(chat);
		while (roomInfo.chats.size() > maxRoomChatHistory) {
			roomInfo.chats.removeFirst();
		}
		broadcastRoom(player.roomID, NetCmd.ROOM_CHAT, chat.uid, NetUtil.urlEncode(chat.strUserName),
				GeneralUtil.exportCalendarString(chat.timestamp), NetUtil.urlEncode(chat.strMessage));
	}

	private void sendMultiPlayerRanking(SocketChannel client, NetMessage message) {
		// mpranking\t[STYLE]
		NetPlayerInfo pInfo = playerInfos.get(client);
		int styleIdx = message.asInt(0);
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
			pData += nowRank + ";" + NetUtil.urlEncode(p.getPlayerName()) + ";" + p.rating[styleIdx] + ";"
					+ p.playCount[styleIdx] + ";" + p.winCount[styleIdx] + "\t";
		}
		if (myRank == -1 && pInfo != null) {
			NetPlayerInfo p = pInfo;
			pData += -1 + ";" + NetUtil.urlEncode(p.getPlayerName()) + ";" + p.rating[styleIdx] + ";"
					+ p.playCount[styleIdx] + ";" + p.winCount[styleIdx] + "\t";
		}
		String strPDataC = NetUtil.compressString(pData);

		send(client, NetCmd.MP_RANKING, styleIdx, myRank, strPDataC);
	}

	private void createSinglePlayerRoom(NetPlayerInfo player, NetMessage message) {
		if (player.roomID != -1) {
			return;
		}
		// singleroomcreate\t[roomName]\t[mode]\t[rule]
		NetRoomInfo room = new NetRoomInfo();
		room.strName = message.urlDecoded(0);
		if (room.strName.isEmpty()) {
			room.strName = "Single (" + player.getPlayerName() + ")";
		}
		room.setSingleplayer(true);
		room.setMode(message.urlDecoded(1));
		room.maxPlayers = 1;

		if (message.length() > 2) {
			room.ruleName = message.urlDecoded(2);
			room.ruleOpt = new RuleOptions(getRatedRule(GameStyle.TETROMINO, room.ruleName));
			room.ruleLock = true;
			room.setRated(true);
		} else {
			room.ruleName = player.getRule().strRuleName;
			room.ruleOpt = new RuleOptions(player.getRule());
			room.ruleLock = false;
			room.setRated(false);
		}

		room.roomID = roomCount;

		roomCount++;
		if (roomCount == -1) {
			roomCount = 0;
		}

		rooms.add(room);

		player.roomID = room.roomID;
		player.resetPlayState();
		player.playCountNow = 0;
		player.winCountNow = 0;

		room.getPlayers().add(player);
		player.seatID = room.joinSeat(player);

		// Send rule data if rated room
		if (room.isRated()) {
			CustomProperties prop = new CustomProperties();
			room.ruleOpt.writeProperty(prop, 0);
			String strRuleTemp = prop.encode("RuleData");
			String strRuleData = NetUtil.compressString(strRuleTemp);
			send(player.channel, NetCmd.RULE_LOCK, strRuleData);
		}

		broadcastPlayerInfoUpdate(player);
		broadcastRoomInfoUpdate(room, NetCmd.ROOM_CREATE);
		send(player.channel, NetCmd.ROOM_CREATE_SUCCESS, room.roomID, 0, -1);

		log.info("NewSingleRoom ID:" + room.roomID + " Title:" + room.strName);
	}

	private void createMultiPlayerRoom(NetPlayerInfo player, NetMessage message) {
		if (player == null || player.roomID != -1) {
			return;
		}
		String strRoomInfo = message.urlDecoded(1);
		NetRoomInfo room = new NetRoomInfo(strRoomInfo);

		room.strName = message.urlDecoded(0);
		if (room.strName.isEmpty()) {
			room.strName = "No Title";
		}
		room.maxPlayers = Math.clamp(room.maxPlayers, 1, 6);
		if (room.ruleLock) {
			room.ruleName = player.getRule().strRuleName;
			room.ruleOpt = new RuleOptions(player.getRule());
		}

		if (room.getMode().isEmpty()) {
			room.setMode(message.urlDecoded(2));
		}

		// Set map
		if (room.useMap && message.length() > 3) {
			String strDecompressed = message.decompressed(3);
			String[] strMaps = strDecompressed.split("\t");

			int maxMap = strMaps.length;

			for (int i = 0; i < maxMap; i++) {
				String strMap = strMaps[i];
				room.getMaps().add(strMap);
			}

			if (room.getMaps().isEmpty()) {
				log.debug("Room" + room.roomID + ": No maps");
				room.useMap = false;
			} else {
				log.debug("Room" + room.roomID + ": Received " + room.getMaps().size() + " maps");
			}
		}

		room.roomID = roomCount;

		roomCount++;
		if (roomCount == -1) {
			roomCount = 0;
		}

		rooms.add(room);

		player.roomID = room.roomID;
		player.resetPlayState();
		player.playCountNow = 0;
		player.winCountNow = 0;

		room.getPlayers().add(player);
		player.seatID = room.joinSeat(player);

		// Send rule data if rule-lock is enabled
		if (room.ruleLock) {
			CustomProperties prop = new CustomProperties();
			room.ruleOpt.writeProperty(prop, 0);
			String strRuleTemp = prop.encode("RuleData");
			String strRuleData = NetUtil.compressString(strRuleTemp);
			send(player.channel, NetCmd.RULE_LOCK, room.roomID, strRuleData);
			// log.info("rulelock\t" + strRuleData);
		}

		broadcastPlayerInfoUpdate(player);
		broadcastRoomInfoUpdate(room, NetCmd.ROOM_CREATE);
		send(player.channel, NetCmd.ROOM_CREATE_SUCCESS, room.roomID, player.seatID, -1);

		log.info("NewRoom ID:" + room.roomID + " Title:" + room.strName + " RuleLock:" + room.ruleLock + " Map:"
				+ room.useMap + " Mode:" + room.getMode());
	}

	private void createRatedRoom(NetPlayerInfo player, NetMessage message) {
		if (player == null || player.roomID != -1) {
			return;
		}
		int i = message.asInt(2);
		String strPreset = NetUtil.decompressString(ratedInfoList.get(i));
		NetRoomInfo roomInfo = new NetRoomInfo(strPreset);

		roomInfo.strName = message.urlDecoded(0);
		if (roomInfo.strName.isEmpty()) {
			roomInfo.strName = "No Title";
		}

		roomInfo.maxPlayers = Math.clamp(message.asInt(1), 1, 6);
		roomInfo.setMode(message.urlDecoded(3));

		roomInfo.setRated(true);
		roomInfo.ruleLock = false; // TODO: implement rule whitelists or rule locks in presets where it is
									// relevant

		roomInfo.roomID = roomCount; // TODO: fix copy-paste code

		roomCount++;
		if (roomCount == -1) {
			roomCount = 0;
		}

		rooms.add(roomInfo);

		player.roomID = roomInfo.roomID;
		player.resetPlayState();
		player.playCountNow = 0;
		player.winCountNow = 0;

		roomInfo.playerSeatDead.add(player);
		player.seatID = roomInfo.joinSeat(player);

		broadcastPlayerInfoUpdate(player);
		broadcastRoomInfoUpdate(roomInfo, NetCmd.ROOM_CREATE);
		send(player.channel, NetCmd.ROOM_CREATE_SUCCESS, roomInfo.roomID, player.seatID, -1);

		log.info("NewRatedRoom ID:" + roomInfo.roomID + " Title:" + roomInfo.strName + " RuleLock:" + roomInfo.ruleLock
				+ " Map:" + roomInfo.useMap + " Mode:" + roomInfo.getMode());
	}

	private void joinRoom(NetPlayerInfo player, NetMessage message) {
		// roomjoin\t[ROOMID]\t[WATCH]
		if (player == null) {
			return;
		}
		int roomID = message.asInt(0);
		boolean watch = message.asBool(1);
		NetRoomInfo prevRoom = getRoomInfo(player.roomID);
		NetRoomInfo newRoom = getRoomInfo(roomID);

		if (roomID < 0) {
			// Return to lobby
			leaveRoom(player, prevRoom, roomID);
			broadcastPlayerInfoUpdate(player);
			send(player.channel, NetCmd.ROOM_JOIN_SUCCESS, -1, -1, -1);
		} else if (newRoom != null) {
			// Enter a room
			leaveRoom(player, prevRoom, newRoom.roomID);
			newRoom.getPlayers().add(player);

			if (!watch && !newRoom.isSingleplayer()) {
				player.seatID = newRoom.joinSeat(player);

				if (player.seatID == -1) {
					player.queueID = newRoom.joinQueue(player);
				}
			}

			// Send rule data if rule-lock is enabled
			if (newRoom.ruleLock) {
			// || newRoom.rated //XXX: This breaks the new Rated with room info preset
			// system, as there is no Rule Lock for Rated now.
				CustomProperties prop = new CustomProperties();
				newRoom.ruleOpt.writeProperty(prop, 0);
				String strRuleData = NetUtil.compressString(prop.encode("RuleData"));
				send(player.channel, NetCmd.RULE_LOCK, newRoom.roomID, strRuleData);
			}

			// Map send
			if (newRoom.useMap && !newRoom.getMaps().isEmpty()) {
				String mapData = NetUtil.compressString(String.join("\t", newRoom.getMaps()));
				send(player.channel, NetCmd.MAP, newRoom.roomID, mapData);
			}

			broadcastRoom(newRoom.roomID, player, NetCmd.PLAYER_ENTER, player.uid,
					NetUtil.urlEncode(player.getPlayerName()), player.seatID);
			broadcastRoomInfoUpdate(newRoom);
			broadcastPlayerInfoUpdate(player);
			send(player.channel, NetCmd.ROOM_JOIN_SUCCESS, newRoom.roomID, player.seatID, player.queueID);

			// Send chat history
			for (NetChatMessage chat : newRoom.chats) {
				send(player.channel, NetCmd.ROOM_CHAT_HIST, NetUtil.urlEncode(chat.strUserName),
						GeneralUtil.exportCalendarString(chat.timestamp), NetUtil.urlEncode(chat.strMessage));
			}
		} else {
			// No such a room
			send(player.channel, NetCmd.ROOM_JOIN_FAIL);
		}
	}

	/**
	 * {@link NetPlayerInfo player} leaves a {@link NetRoomInfo room}
	 *
	 * @param player    that leaves the room
	 * @param room      that has been left
	 * @param newRoomId the id of the room the player will join afterwards
	 *                  ({@code -1} for the lobby)
	 */
	private void leaveRoom(NetPlayerInfo player, NetRoomInfo room, int newRoomId) {
		if (room != null) {
			broadcastRoom(room.roomID, player, NetCmd.PLAYER_LEAVE, player.uid,
					NetUtil.urlEncode(player.getPlayerName()), player.seatID);
			playerDead(player);
			player.setReady(false);
			room.exitSeat(player);
			room.exitQueue(player);
			room.getPlayers().remove(player);
			if (!deleteRoom(room)) {
				joinAllQueuePlayers(room);
				if (!gameFinished(room) && !gameStartIfPossible(room)) {
					autoStartTimerCheck(room);
					broadcastRoomInfoUpdate(room);
				}

			}
		}
		player.roomID = newRoomId;
		player.seatID = -1;
		player.queueID = -1;
		player.resetPlayState();
		player.playCountNow = 0;
		player.winCountNow = 0;
	}

	private void changeTeam(NetPlayerInfo player, NetMessage message) {
		// changeteam\t[TEAM]
		if (player == null || player.isPlaying()) {
			return;
		}
		String newTeam = "";
		if (message.length() > 0) {
			newTeam = message.urlDecoded(0);
		}
		var oldTeam = player.getTeam();
		if (oldTeam.isPresent() && oldTeam.get().equals(newTeam)) {
			return;
		}
		player.setTeam(newTeam);
		broadcastPlayerInfoUpdate(player);
		broadcastRoom(player.roomID, NetCmd.CHANGE_TEAM, player.uid, NetUtil.urlEncode(player.getPlayerName()),
				NetUtil.urlEncode(newTeam));
	}

	private void changeStatus(NetPlayerInfo player, NetMessage message) {
		// changestatus\t[WATCH]
		if (player == null || player.isPlaying() || player.roomID == -1) {
			return;
		}
		NetRoomInfo roomInfo = getRoomInfo(player.roomID);
		if (roomInfo == null || roomInfo.isSingleplayer()) {
			return;
		}
		boolean watch = message.asBool(0);
		String name = NetUtil.urlEncode(player.getPlayerName());
		if (watch) {
			// Change to spectator
			int prevSeatID = player.seatID;
			roomInfo.exitSeat(player);
			roomInfo.exitQueue(player);
			player.setReady(false);
			player.seatID = -1;
			player.queueID = -1;
			broadcastRoom(player.roomID, NetCmd.CHANGE_STATUS, "watchonly", player.uid, name, prevSeatID);
			joinAllQueuePlayers(roomInfo); // Let the queue-player to join
		} else if (roomInfo.canJoinSeat()) { // Change to player
			player.seatID = roomInfo.joinSeat(player);
			player.queueID = -1;
			player.setReady(false);
			broadcastRoom(player.roomID, NetCmd.CHANGE_STATUS, "joinseat", player.uid, name, player.seatID);
		} else {
			player.seatID = -1;
			player.queueID = roomInfo.joinQueue(player);
			player.setReady(false);
			broadcastRoom(player.roomID, NetCmd.CHANGE_STATUS, "joinqueue", player.uid, name, player.queueID);
		}
		broadcastPlayerInfoUpdate(player);
		if (!gameStartIfPossible(roomInfo)) {
			autoStartTimerCheck(roomInfo);
		}
		broadcastRoomInfoUpdate(roomInfo);
	}

	private void startSinglePlayGame(NetPlayerInfo player) {
		if (player == null) {
			return;
		}
		log.info("Starting single player game");
		NetRoomInfo roomInfo = getRoomInfo(player.roomID);
		int seat = roomInfo.getPlayerSeatNumber(player);
		if (seat != -1 && roomInfo.isSingleplayer()) {
			gameStart(roomInfo);
		}
	}

	private void playerReady(NetPlayerInfo player, NetMessage message) {
		if (player == null) {
			return;
		}
		// ready\t[STATE]
		NetRoomInfo roomInfo = getRoomInfo(player.roomID);
		if (roomInfo == null) {
			return;
		}
		int seat = roomInfo.getPlayerSeatNumber(player);

		if (seat != -1 && !roomInfo.isSingleplayer()) {
			player.setReady(message.asBool(0));
			broadcastPlayerInfoUpdate(player);

			if (!player.isReady()) {
				roomInfo.isSomeoneCancelled = true;
			}

			// Start a game if possible
			if (!gameStartIfPossible(roomInfo)) {
				autoStartTimerCheck(roomInfo);
			}
		}
	}

	private void gameAutoStart(NetPlayerInfo player) {
		if (player == null) {
			return;
		}
		NetRoomInfo roomInfo = getRoomInfo(player.roomID);
		if (roomInfo == null) {
			return;
		}
		int seat = roomInfo.getPlayerSeatNumber(player);

		if (seat != -1 && roomInfo.autoStartActive && !roomInfo.isSingleplayer()) {
			if (roomInfo.autoStartTNET2) {
				// Move all non-ready players to spectators
				for (NetPlayerInfo p : List.copyOf(roomInfo.getSeats())) {
					if (p == null || p.isReady()) {
						continue;
					}
					int prevSeatID = p.seatID;
					roomInfo.exitSeat(p);
					roomInfo.exitQueue(p);
					p.setReady(false);
					p.seatID = -1;
					p.queueID = -1;
					broadcastRoom(p.roomID, NetCmd.CHANGE_STATUS, "watchonly", p.uid,
							NetUtil.urlEncode(p.getPlayerName()), prevSeatID);
				}
				joinAllQueuePlayers(roomInfo);
			}
			gameStart(roomInfo);
		}
	}

	private void playerDead(NetPlayerInfo player, NetMessage message) {
		if (player == null) {
			return;
		}
		NetPlayerInfo koPlayerInfo = null;
		if (message.length() > 0) {
			int koUID = message.asInt(0);
			koPlayerInfo = searchPlayerByUID(koUID);
		}
		playerDead(player, koPlayerInfo);
	}

	/**
	 * Signal player-dead
	 *
	 * @param player Player
	 */
	private void playerDead(NetPlayerInfo player) {
		playerDead(player, (NetPlayerInfo) null);
	}

	/**
	 * Signal player-dead
	 *
	 * @param player  Player
	 * @param pKOInfo Assailant (can be null)
	 */
	private void playerDead(NetPlayerInfo player, NetPlayerInfo pKOInfo) {
		NetRoomInfo roomInfo = getRoomInfo(player.roomID);

		if (roomInfo == null || !roomInfo.playing || player.seatID == -1 || !player.isPlaying()) {
			return;
		}
		player.resetPlayState();

		int place = roomInfo.startPlayers - roomInfo.deadCount;
		String msg = "";
		msg += player.uid + "\t";
		msg += NetUtil.urlEncode(player.getPlayerName()) + "\t";
		msg += player.seatID + "\t";
		msg += place + "\t";
		if (pKOInfo == null) {
			msg += -1 + "\t" + "";
		} else {
			msg += pKOInfo.uid + "\t";
			msg += NetUtil.urlEncode(pKOInfo.getPlayerName());
		}
		broadcastRoom(player.roomID, NetCmd.DEAD, msg);

		roomInfo.deadCount++;
		roomInfo.playerSeatDead.addFirst(player);
		gameFinished(roomInfo);

		broadcastPlayerInfoUpdate(player);
	}

	private void raceWin(NetPlayerInfo player, NetMessage message) {
		// Race mode win (TODO: Replace with something cheat-proof)
		if (player == null || player.roomID == -1 || player.seatID == -1) {
			return;
		}
		NetRoomInfo roomInfo = getRoomInfo(player.roomID);
		if (roomInfo == null || !roomInfo.playing) {
			return;
		}
		int modeIndex = mpModeList.get(roomInfo.style).indexOf(roomInfo.getMode());
		boolean isRace = modeIndex != -1 && mpModeIsRace.get(roomInfo.style).get(modeIndex);
		if (!isRace) {
			return;
		}
		for (int i = message.length() - 2; i > 0; i--) {
			int koUID = message.asInt(i);
			if (koUID != player.uid) {
				NetPlayerInfo koPlayerInfo = searchPlayerByUID(koUID);

				if (koPlayerInfo != null && koPlayerInfo.roomID == roomInfo.roomID) {
					playerDead(koPlayerInfo, player);
				}
			}
		}
	}

	/**
	 * Broadcast end-of-game stats for a multi player game
	 *
	 * @param player
	 * @param message
	 */
	private void multiplayerGameStats(NetPlayerInfo player, NetMessage message) {
		if (player == null || player.roomID == -1 || player.seatID == -1) {
			return;
		}
		NetRoomInfo roomInfo = getRoomInfo(player.roomID);
		if (roomInfo == null || roomInfo.isSingleplayer()) {
			return;
		}
		String msg = "";
		msg += player.uid + "\t";
		msg += player.seatID + "\t";
		msg += NetUtil.urlEncode(player.getPlayerName()) + "\t";
		msg += String.join("\t", message.data());
		broadcastRoom(roomInfo.roomID, NetCmd.GSTAT, msg);
	}

	/**
	 * Broadcast end-of-game stats for single player game
	 *
	 * @param pInfo
	 * @param message
	 */
	private void singleplayerGameStats(NetPlayerInfo pInfo, NetMessage message) {
		if (pInfo == null || pInfo.roomID == -1 || pInfo.seatID == -1) {
			return;
		}
		NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
		if (roomInfo != null && roomInfo.isSingleplayer()) {
			broadcastRoom(roomInfo.roomID, NetCmd.GSTAT_1P, message.text(0));
		}
	}

	private void sendReplay(NetPlayerInfo pInfo, NetMessage message) {
		if (pInfo == null || pInfo.roomID == -1 || pInfo.seatID == -1) {
			return;
		}
		// spsend\t[CHECKSUM]\t[DATA]
		NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
		if (!pInfo.isTripUse()) {
			broadcastRoom(pInfo.roomID, NetCmd.SP_SEND_OK, -1, false, -1);
		} else if (roomInfo.isSingleplayer()) {
			long sChecksum = message.asLong(0);
			Adler32 checksumObj = new Adler32();
			checksumObj.update(NetUtil.stringToBytes(message.text(1)));
			log.info("Checksums are: " + sChecksum + " and " + checksumObj.getValue());

			if (sChecksum == checksumObj.getValue()) {
				String strData = message.decompressed(1);
				NetSPRecord spRecord = new NetSPRecord(strData);
				String rule = roomInfo.isRated() ? roomInfo.ruleName : "any"; // "any" for unrated rules
				spRecord.playerName = pInfo.getPlayerName();
				spRecord.modeName = roomInfo.getMode();
				spRecord.ruleName = rule;
				spRecord.style = roomInfo.style;
				spRecord.timeStamp = GeneralUtil.exportCalendarString();

				float gamerate = spRecord.stats.gamerate * 100f;

				boolean isDailyWiped = updateSPDailyRanking();
				int rank = -1;
				int rankDaily = -1;

				NetSPRanking ranking = getSPRanking(rule, spRecord.modeName, spRecord.gameType);
				NetSPRanking rankingDaily = getSPRanking(rule, spRecord.modeName, spRecord.gameType, true);
				if (ranking == null) {
					log.warn("All-time ranking not found:" + spRecord.modeName);
				}
				if (rankingDaily == null) {
					log.warn("Daily ranking not found:" + spRecord.modeName);
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
						isPB = pInfo.registerRecord(ranking.getRankingType(), spRecord);
						if (isPB) {
							setPlayerDataToProperty(pInfo);
							writePlayerDataToFile();
						}
					}

					log.info("Name:" + pInfo.getPlayerName() + " Mode:" + spRecord.modeName + " AllTime:" + rank
							+ " Daily:" + rankDaily);
					broadcastRoom(pInfo.roomID, NetCmd.SP_SEND_OK, rank, isPB, rankDaily);
				} else {
					broadcastRoom(pInfo.roomID, NetCmd.SP_SEND_OK, -1, false, -1);
				}
			} else {
				send(pInfo.channel, NetCmd.SP_SEND_NG);
			}
		}
	}

	private void sendSingleplayerLeaderboard(SocketChannel client, NetMessage message) {
		// spranking\t[RULE]\t[MODE]\t[GAMETYPE]\t[DAILY]
		String rule = message.urlDecoded(0);
		String mode = message.urlDecoded(1);
		int gameType = message.asInt(2);
		boolean isDaily = message.asBool(3);
		NetPlayerInfo pInfo = playerInfos.get(client);

		if (isDaily && updateSPDailyRanking()) {
			writeSPRankingToFile();
		}

		int myRank = -1;
		NetSPRanking ranking = getSPRanking(rule, mode, gameType, isDaily);

		if (ranking == null) {
			send(client, NetCmd.SP_RANKING, rule, mode, gameType, isDaily, 0, 0);
			return;
		}
		int maxRecord = ranking.getRecords().size();

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < maxRecord; i++) {
			String strRow = "";
			if (i > 0) {
				strRow = ";";
			}

			NetSPRecord spRecord = ranking.getRecords().get(i);
			strRow += i + "," + NetUtil.urlEncode(spRecord.playerName) + ",";
			strRow += spRecord.timeStamp + "," + spRecord.stats.gamerate + ",";
			strRow += spRecord.getStatRow(ranking.getRankingType());
			if (pInfo != null && pInfo.getPlayerName().equals(spRecord.playerName)) {
				myRank = i;
			}
			builder.append(strRow);
		}
		if (myRank == -1 && pInfo != null && !isDaily) {
			NetSPRecord spRecord = pInfo.findRecord(rule, mode, gameType);

			if (spRecord != null) {
				String strRow = "";
				if (maxRecord > 0) {
					strRow += ",";
				}

				maxRecord++;
				strRow += -1 + "," + NetUtil.urlEncode(spRecord.playerName) + ",";
				strRow += spRecord.timeStamp + "," + spRecord.stats.gamerate + ",";
				strRow += spRecord.getStatRow(ranking.getRankingType());
				builder.append(strRow);
			}
		}
		send(client, NetCmd.SP_RANKING, rule, mode, gameType, isDaily, ranking.getRankingType().ordinal(), maxRecord,
				builder);
	}

	private void sendReplayDownload(SocketChannel client, NetMessage message) {
		// spdownload\t[RULE]\t[MODE]\t[GAMETYPE]\t[DAILY]\t[NAME]
		String strRule = message.urlDecoded(0);
		String strMode = message.urlDecoded(1);
		int gameType = message.asInt(2);
		boolean isDaily = message.asBool(gameType);
		String strName = message.urlDecoded(4);
		NetPlayerInfo pInfo = playerInfos.get(client);

		// Is any rule room?
		if (pInfo != null && pInfo.roomID != -1) {
			NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
			if (roomInfo != null && !roomInfo.isRated()) {
				strRule = "any";
			}
		}

		if (isDaily && updateSPDailyRanking()) {
			writeSPRankingToFile();
		}

		NetSPRanking ranking = getSPRanking(strRule, strMode, gameType, isDaily);
		if (ranking != null) {
			// Get from leaderboard...
			NetSPRecord netRecord = ranking.getRecord(strName);
			// or from Personal Best when not found in the leaderboard.
			if (netRecord == null && !isDaily) {
				netRecord = pInfo.findRecord(strRule, strMode, gameType);
			}

			if (netRecord != null) {
				Adler32 checksumObj = new Adler32();
				checksumObj.update(NetUtil.stringToBytes(netRecord.replayProp));
				long sChecksum = checksumObj.getValue();
				send(client, NetCmd.SP_DOWNLOAD, sChecksum, netRecord.replayProp);
			} else {
				log.warn("Record not found (Mode:" + strMode + ", Rule:" + strRule + ", Type:" + gameType + " Name:"
						+ strName + ")");
			}
		} else if (!isDaily) {
			log.warn("All-time ranking not found (Mode:" + strMode + ", Rule:" + strRule + ", Type:" + gameType + ")");
		} else {
			log.warn("Daily ranking not found (Mode:" + strMode + ", Rule:" + strRule + ", Type:" + gameType + ")");
		}
	}

	private void resetSinglePlayer(NetPlayerInfo pInfo) {
		if (pInfo == null) {
			return;
		}
		NetRoomInfo roomInfo = getRoomInfo(pInfo.roomID);
		if (roomInfo == null) {
			return;
		}
		int seat = roomInfo.getPlayerSeatNumber(pInfo);
		if (seat != -1) {
			pInfo.resetPlayState();
			broadcastPlayerInfoUpdate(pInfo);
			gameFinished(roomInfo);
			broadcastRoom(roomInfo.roomID, pInfo, NetCmd.RESET_SP);
		}
	}

	/**
	 * Game messages (Server will deliver them to other players but won't modify it)
	 *
	 * @param player
	 * @param message
	 */
	private void broadcastGameMessage(NetPlayerInfo player, NetMessage message) {
		if (player == null) {
			return;
		}
		NetRoomInfo roomInfo = getRoomInfo(player.roomID);
		if (roomInfo == null) {
			return;
		}
		int seat = roomInfo.getPlayerSeatNumber(player);
		if (seat == -1) {
			return;
		}
		Object[] data = new Object[message.data().length + 2];
		data[0] = player.uid;
		data[1] = seat;
		System.arraycopy(message.data(), 0, data, 2, message.length());
		broadcastRoom(roomInfo.roomID, player, NetCmd.GAME, data);
	}

	private void sendRatedPresets(SocketChannel client) {
		send(client, NetCmd.RATED_PRESETS, ratedInfoList.toArray());
	}

	/**
	 * Sets a ban by IP address.
	 *
	 * @param strIP     IP address
	 * @param banLength The length of the ban. (-1: Kick only, not ban)
	 * @return Number of players kicked
	 */
	private void banPlayer(SocketChannel admin, String[] message) {
		// ban\t[IP]\t(Length)
		int kickCount = 0;
		String strIP = message[1];
		int banLength = -1;
		if (message.length > 2) {
			banLength = Integer.parseInt(message[2]);
		}

		List<SocketChannel> banChannels = new LinkedList<>();
		for (SocketChannel channel : channels) {
			String ip = getHostAddress(channel);
			if (ip.equals(strIP)) {
				banChannels.add(channel);
			}
		}
		for (SocketChannel player : banChannels) {
			ban(player, banLength);
		}
		if (banChannels.isEmpty() && banLength >= 0) {
			// Add ban entry manually
			serverBans.add(new NetServerBan(strIP, banLength));
		}
		saveBanList();
		// XXX rename response to 'ban_result' or something
		sendAdminResult(admin, NetCmd.BAN, message[1], banLength, kickCount);
	}

	private void unbanPlayer(SocketChannel client, String[] message) {
		// unban\t[IP]
		int count = 0;

		if (message[1].equalsIgnoreCase("ALL")) {
			count = serverBans.size();
			serverBans.clear();
		} else {
			for (NetServerBan ban : List.copyOf(serverBans)) {
				if (ban.addr.equals(message[1])) {
					serverBans.remove(ban);
					count++;
				}
			}
		}
		saveBanList();

		sendAdminResult(client, NetCmd.UNBAN, message[1], count);
	}

	private void getBanlist(SocketChannel client) {
		// Cleanup expired bans
		serverBans.removeIf(NetServerBan::isExpired);
		// Create list
		String strResult = serverBans.stream().map(NetServerBan::exportString).collect(Collectors.joining("\t"));
		sendAdminResult(client, NetCmd.BAN_LIST, strResult);
	}

	private void deletePlayer(SocketChannel client, String name) {
		// playerdelete\t<Name>

		NetPlayerInfo pInfo = searchPlayerByName(name);

		boolean playerDataChange = false;
		boolean mpRankingDataChange = false;
		boolean spRankingDataChange = false;

		for (GameStyle style : GameStyle.values()) {
			int i = style.ordinal();
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
				pInfo.getRecords().clear();
			}

			int playerRank = getMPRanking(style, pInfo);
			if (playerRank != -1) {
				mpRankings.get(style).remove(playerRank);
				mpRankingDataChange = true;
			}

			for (NetSPRanking ranking : spRankingListAlltime) {
				spRankingDataChange |= ranking.getRecords().removeIf(r -> r.playerName.equals(name));
			}
			for (NetSPRanking ranking : spRankingListDaily) {
				spRankingDataChange |= ranking.getRecords().removeIf(r -> r.playerName.equals(name));
			}
		}

		sendAdminResult(client, NetCmd.PLAYER_DELETE, name);

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

	private void deleteRoom(SocketChannel client, String[] message) {
		// roomdelete\t[ID]
		int roomID = Integer.parseInt(message[1]);
		NetRoomInfo roomInfo = getRoomInfo(roomID);

		if (roomInfo == null) {
			sendAdminResult(client, NetCmd.ROOM_DELETE_FAIL, roomID);
			return;
		}
		for (NetPlayerInfo pInfo : List.copyOf(roomInfo.getPlayers())) {
			if (pInfo == null) { // XXX check if really necessary
				continue;
			}
			SocketChannel client2 = getSocketChannelByPlayer(pInfo);
			if (client2 != null) {
				// Packet simulation :p
				processPacket(client2, NetMessage.of(NetCmd.ROOM_JOIN.command(), "-1", "false"));
				// Send message to the kicked player
				send(client2, NetCmd.ROOM_KICKED, 0, roomInfo.roomID, NetUtil.urlEncode(roomInfo.strName));
			}
		}
		roomInfo.getPlayers().clear();
		deleteRoom(roomInfo);
		sendAdminResult(client, NetCmd.ROOM_DELETE_SUCCESS, roomID, roomInfo.strName);

	}

	/**
	 * Delete a room
	 *
	 * @param roomInfo Room to delete
	 * @return true if success, false if fails (room not empty)
	 */
	private boolean deleteRoom(NetRoomInfo roomInfo) {
		if (roomInfo != null && roomInfo.getPlayers().isEmpty()) {
			log.info("RoomDelete ID:" + roomInfo.roomID + " Title:" + roomInfo.strName);
			broadcastRoomInfoUpdate(roomInfo, NetCmd.ROOM_DELETE);
			rooms.remove(roomInfo);
			roomInfo.delete();
			return true;
		}
		return false;
	}

	private void shutdown(SocketChannel client) {
		log.warn("Shutdown requested by the admin (" + getHostFull(client) + ")");
		shutdownRequested = true;
		selector.wakeup();
	}

	/**
	 * Send admin command result
	 *
	 * @param client The admin
	 * @param msg    Message to send
	 */
	private void sendAdminResult(SocketChannel client, NetCmd cmd, Object... parts) {
		String message = Stream.of(parts).map(Object::toString).collect(Collectors.joining("\t"));
		message = NetUtil.compressString(cmd.command() + "\t" + message);
		send(client, NetCmd.ADMIN_RESULT, message);
	}

	/**
	 * Broadcast admin command result to all admins
	 *
	 * @param msg Message to send
	 */
	private void broadcastAdminResult(NetCmd cmd, String msg) {
		String message = NetUtil.compressString(cmd.command() + "\t" + msg);
		broadcastAdmin(NetCmd.ADMIN_RESULT, message);
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
		StringBuilder message = new StringBuilder();

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
			message.append(strClientData);
		}

		if (client == null) {
			broadcastAdminResult(NetCmd.CLIENT_LIST, message.toString());
		} else {
			sendAdminResult(client, NetCmd.CLIENT_LIST, message.toString());
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

		for (NetRoomInfo roomInfo : rooms) {
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
		String msg = "" + rooms.size();
		if (!rooms.isEmpty()) {
			msg += "\t";
			msg += rooms.stream().map(NetRoomInfo::exportString).collect(Collectors.joining("\t"));
		}
		send(client, NetCmd.ROOM_LIST, msg);
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
			if (roomInfo.autoStartActive) {
				broadcastRoom(roomInfo.roomID, NetCmd.AUTOSTART_STOP);
			}
			roomInfo.autoStartActive = false;
		}
		// Start
		else if (!roomInfo.autoStartActive
				&& (!roomInfo.isSomeoneCancelled || !roomInfo.disableTimerAfterSomeoneCancelled)
				&& roomInfo.getHowManyPlayersReady() >= minPlayers
				&& roomInfo.getHowManyPlayersReady() >= roomInfo.getNumberOfPlayerSeated() / 2) {
			broadcastRoom(roomInfo.roomID, NetCmd.AUTOSTART_BEGIN, roomInfo.autoStartSeconds);
			roomInfo.autoStartActive = true;
		}

		// Turn-off ready status if there is only 1 player
		if (roomInfo.getNumberOfPlayerSeated() != 1) {
			return;
		}
		for (NetPlayerInfo player : roomInfo.getSeats()) {
			if (player != null && player.isReady()) {
				player.setReady(false);
				broadcastPlayerInfoUpdate(player);
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
		if (roomInfo.getNumberOfPlayerSeated() <= 1 && !roomInfo.isSingleplayer()) {
			return;
		}
		if (roomInfo.playing) {
			return;
		}

		roomInfo.gameStart();

		int mapNo = 0;
		int mapMax = roomInfo.getMaps().size();
		if (roomInfo.useMap && mapMax > 0) {
			do {
				mapNo = rand.nextInt(mapMax);
			} while (mapNo == roomInfo.mapPrevious && mapMax >= 2);

			roomInfo.mapPrevious = mapNo;
		}
		broadcastRoom(roomInfo.roomID, NetCmd.START, Long.toString(rand.nextLong(), 16), roomInfo.startPlayers, mapNo);

		for (NetPlayerInfo player : roomInfo.getSeats()) {
			player.setReady(false);
			player.setPlaying(true);
			player.playCountNow++;

			// If ranked room
			if (roomInfo.isRated() && !roomInfo.isTeamGame() && (!roomInfo.hasSameIPPlayers() || ratingAllowSameIP)) {
				int index = roomInfo.style.ordinal();
				player.playCount[index]++;
				player.ratingBefore[index] = player.rating[index];
			}
			broadcastPlayerInfoUpdate(player);
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
			String msg = "";

			if (isTeamWin) {
				// Winner is a team
				String teamName = roomInfo.getWinnerTeam();
				if (teamName == null) {
					teamName = "";
				}
				msg += -1 + "\t" + -1 + "\t" + NetUtil.urlEncode(teamName) + "\t" + isTeamWin;

				for (NetPlayerInfo pInfo : roomInfo.getSeats()) {
					if (pInfo != null && pInfo.isPlaying()) {
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
			} else if (winner != null && !roomInfo.isSingleplayer()) {
				// Winner is a player
				roomInfo.playerSeatDead.addFirst(winner);

				// Rated game
				if (roomInfo.isRated() && !roomInfo.isTeamGame()
						&& (!roomInfo.hasSameIPPlayers() || ratingAllowSameIP)) {
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
						log.debug("#" + (i + 1) + " Name:" + p.getPlayerName() + " Rating:" + p.rating[style] + " ("
								+ change + ")");
						setPlayerDataToProperty(p);
						broadcastRoom(winner.roomID, NetCmd.RATING, p.uid, p.seatID,
								NetUtil.urlEncode(p.getPlayerName()), p.rating[style], change);
					}
					writePlayerDataToFile();

					// Leaderboard update
					for (NetPlayerInfo player : roomInfo.playerSeatDead) {
						if (player.isTripUse()) {
							mpRankingUpdate(roomInfo.style, player);
						}
					}
					writeMPRankingToFile();
				}

				msg += winner.uid + "\t" + winner.seatID + "\t" + NetUtil.urlEncode(winner.getPlayerName()) + "\t"
						+ isTeamWin;
				winner.resetPlayState();
				winner.winCountNow++;
				broadcastPlayerInfoUpdate(winner);
			} else {
				// No winner(s)
				msg += -1 + "\t" + -1 + "\t" + "" + "\t" + isTeamWin;
			}
			broadcastRoom(roomInfo.roomID, NetCmd.FINISH, msg);

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
		broadcastRoomInfoUpdate(roomInfo, NetCmd.ROOM_UPDATE);
	}

	/**
	 * Broadcast a room update information
	 *
	 * @param roomInfo The room
	 * @param command  Command
	 */
	private void broadcastRoomInfoUpdate(NetRoomInfo roomInfo, NetCmd command) {
		roomInfo.updatePlayerCount();
		String msg = roomInfo.exportString();
		broadcast(command, msg);
		broadcastAdmin(command, msg);
	}

	/**
	 * Send player list to specified client
	 *
	 * @param client Client to send
	 */
	private void sendPlayerList(SocketChannel client) {
		var players = playerInfos.values().stream().map(NetPlayerInfo::exportString).collect(Collectors.joining("\t"));
		send(client, NetCmd.PLAYER_LIST, playerInfos.values().size(), players);
	}

	/**
	 * Broadcast a player update information (command will be "playerupdate")
	 *
	 * @param pInfo The player
	 */
	private void broadcastPlayerInfoUpdate(NetPlayerInfo pInfo) {
		broadcastPlayerInfoUpdate(pInfo, NetCmd.PLAYER_UPDATE);
	}

	/**
	 * Broadcast a player update information
	 *
	 * @param pInfo   The player
	 * @param command Command
	 */
	private void broadcastPlayerInfoUpdate(NetPlayerInfo pInfo, NetCmd command) {
		broadcast(command, pInfo.exportString());
	}

	/**
	 * Get NetPlayerInfo by player's name
	 *
	 * @param name Name
	 * @return NetPlayerInfo (null if not found)
	 */
	private NetPlayerInfo searchPlayerByName(String name) {
		for (SocketChannel channel : channels) {
			NetPlayerInfo pInfo = playerInfos.get(channel);
			if (pInfo != null && pInfo.getPlayerName().equals(name)) {
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
		for (SocketChannel channel : channels) {
			NetPlayerInfo pInfo = playerInfos.get(channel);
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
			pInfo.setReady(false);
			broadcastRoom(pInfo.roomID, NetCmd.CHANGE_STATUS, "joinseat", pInfo.uid,
					NetUtil.urlEncode(pInfo.getPlayerName()), pInfo.seatID);
			broadcastPlayerInfoUpdate(pInfo);
			playerJoinedCount++;
		}

		if (playerJoinedCount > 0) {
			broadcastRoomInfoUpdate(roomInfo);
		}

		return playerJoinedCount;
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
			serverBans.add(new NetServerBan(remoteAddr, banLength));
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

		Iterator<NetServerBan> i = serverBans.iterator();
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
	 */
	private void sendRatedRuleList(SocketChannel client) {
		for (GameStyle style : GameStyle.values()) {
			var rules = ruleList.get(style).stream().map(r -> r.strRuleName).collect(Collectors.joining("\t"));
			send(client, NetCmd.RULE_LIST, style.ordinal(), rules);
		}
	}

	/**
	 * Get rated-game rule
	 *
	 * @param style Style ID
	 * @param name  Rule Name
	 * @return Rated-game rule (null if not found)
	 */
	private RuleOptions getRatedRule(GameStyle style, String name) {
		if (style == null || name == null) {
			return null;
		}
		for (RuleOptions rule : ruleList.get(style)) {
			if (name.equals(rule.strRuleName)) {
				return rule;
			}
		}

		return null;
	}

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
		status = status.replace("\\$rooms", Integer.toString(rooms.size()));
		String file = propServer.getProperty("netserver.statusfilename", "status.txt");
		try (var outFile = new FileWriter(file)) {
			outFile.write(status);
		} catch (IOException e) {
			log.error("failed to save server status", e);
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

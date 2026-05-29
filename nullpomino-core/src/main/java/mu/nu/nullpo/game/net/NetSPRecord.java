package mu.nu.nullpo.game.net;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.Statistics;
import mu.nu.nullpo.game.types.GameStyle;
import mu.nu.nullpo.util.CustomProperties;

/**
 * Single player mode record
 */
@Log4j
public class NetSPRecord {

	/** Ranking type constants */
	public enum RankingType {
		GENERIC_SCORE, GENERIC_TIME, SCORERACE, DIGRACE, ULTRA, COMBORACE, DIGCHALLENGE, TIMEATTACK;
	}

	/** Player Name */
	public String playerName;

	/** Game Mode Name */
	public String modeName;

	/** Rule Name */
	public String ruleName;

	/** Game Type ID */
	public int gameType;

	/** Game Style */
	public GameStyle style;

	/** Main Stats */
	public Statistics stats;

	/** List of custom stats (Each String is NAME;VALUE format) */
	private final Map<String, String> customStats = new HashMap<>();

	/** Replay data (Compressed) */
	public String replayProp;

	/** Time stamp (GMT) */
	public String timeStamp;

	/**
	 * Compare 2 records
	 *
	 * @param type Ranking Type
	 * @param r1   Record 1
	 * @param r2   Record 2
	 * @return <code>true</code> if r1 is better than r2
	 */
	public static boolean compareRecords(RankingType type, NetSPRecord r1, NetSPRecord r2) {
		Statistics s1 = r1.stats;
		Statistics s2 = r2.stats;

		return switch (type) {
		case GENERIC_SCORE -> {
			if (s1.score > s2.score) {
				yield true;
			}
			if (s1.score == s2.score && s1.lines > s2.lines) {
				yield true;
			}
			if (s1.score == s2.score && s1.lines == s2.lines && s1.time < s2.time) {
				yield true;
			}
			yield false;
		}
		case GENERIC_TIME -> {
			if (s1.time < s2.time) {
				yield true;
			}
			if (s1.time == s2.time && s1.totalPieceLocked < s2.totalPieceLocked) {
				yield true;
			}
			if (s1.time == s2.time && s1.totalPieceLocked == s2.totalPieceLocked && s1.pps > s2.pps) {
				yield true;
			}
			yield false;
		}
		case SCORERACE -> {
			if (s1.time < s2.time) {
				yield true;
			}
			if (s1.time == s2.time && s1.lines < s2.lines) {
				yield true;
			}
			if (s1.time == s2.time && s1.lines == s2.lines && s1.spl > s2.spl) {
				yield true;
			}
			yield false;
		}
		case DIGRACE -> {
			if (s1.time < s2.time) {
				yield true;
			}
			if (s1.time == s2.time && s1.lines < s2.lines) {
				yield true;
			}
			if (s1.time == s2.time && s1.lines == s2.lines && s1.totalPieceLocked < s2.totalPieceLocked) {
				yield true;
			}
			yield false;
		}
		case ULTRA -> {
			if (s1.score > s2.score) {
				yield true;
			}
			if (s1.score == s2.score && s1.lines > s2.lines) {
				yield true;
			}
			if (s1.score == s2.score && s1.lines == s2.lines && s1.totalPieceLocked < s2.totalPieceLocked) {
				yield true;
			}
			yield false;
		}
		case COMBORACE -> {
			if (s1.maxCombo > s2.maxCombo) {
				yield true;
			}
			if (s1.maxCombo == s2.maxCombo && s1.time < s2.time) {
				yield true;
			}
			if (s1.maxCombo == s2.maxCombo && s1.time == s2.time && s1.pps > s2.pps) {
				yield true;
			}
			yield false;
		}
		case DIGCHALLENGE -> {
			if (s1.score > s2.score) {
				yield true;
			}
			if (s1.score == s2.score && s1.lines > s2.lines) {
				yield true;
			}
			if (s1.score == s2.score && s1.lines == s2.lines && s1.time > s2.time) {
				yield true;
			}
			yield false;
		}
		case TIMEATTACK -> {
			// Cap the line count at 150 or 200
			int maxLines = r1.gameType >= 5 ? 200 : 150;
			int l1 = Math.min(s1.lines, maxLines);
			int l2 = Math.min(s2.lines, maxLines);
			if (s1.rollclear > s2.rollclear) {
				yield true;
			}
			if (s1.rollclear == s2.rollclear && l1 > l2) {
				yield true;
			}
			if (s1.rollclear == s2.rollclear && l1 == l2 && s1.time < s2.time) {
				yield true;
			}
			if (s1.rollclear == s2.rollclear && l1 == l2 && s1.time == s2.time && s1.pps > s2.pps) {
				yield true;
			}
			yield false;
		}
		default -> false;
		};
	}

	/**
	 * Default Constructor
	 */
	public NetSPRecord() {
		playerName = "";
		modeName = "";
		ruleName = "";
		stats = null;
		replayProp = "";
		timeStamp = "";
		gameType = 0;
		style = GameStyle.TETROMINO;

	}

	/**
	 * Copy Constructor
	 *
	 * @param s Source
	 */
	public NetSPRecord(NetSPRecord s) {
		playerName = s.playerName;
		modeName = s.modeName;
		ruleName = s.ruleName;
		if (s.stats == null) {
			stats = null;
		} else {
			stats = new Statistics(s.stats);
		}

		customStats.putAll(s.customStats);
		replayProp = s.replayProp;
		timeStamp = s.timeStamp;
		gameType = s.gameType;
		style = s.style;
	}

	/**
	 * Constructor that imports data from a String
	 *
	 * @param s String (Split by ;)
	 */
	public NetSPRecord(String s) {
		importString(s);
	}

	/**
	 * Export custom stats to a String
	 *
	 * @return String (Split by ,)
	 */
	public String exportCustomStats() {
		return customStats.entrySet().stream().map(e -> e.getKey() + ";" + e.getValue())
				.collect(Collectors.joining(","));
	}

	/**
	 * Import custom stats from a String
	 *
	 * @param s String (Split by ,)
	 */
	public void importCustomStats(String s) {
		customStats.clear();
		if (s == null || s.isEmpty()) {
			return;
		}
		String[] array = s.split(",");
		for (String entry : array) {
			String[] data = entry.split(";");
			if (data.length != 2) {
				continue;
			}
			customStats.put(data[0], data[1]);
		}
	}

	/**
	 * Set replay data from CustomProperties
	 *
	 * @param p CustomProperties that contains replay data
	 */
	public void setReplayProp(CustomProperties p) {
		String strEncode = p.encode("NullpoMino Net Single Player Replay (" + playerName + ")");
		replayProp = NetUtil.compressString(strEncode);
	}

	/**
	 * Get replay data as CustomProperties
	 *
	 * @return CustomProperties that contains replay data
	 */
	public CustomProperties getReplayProp() {
		String strEncode = NetUtil.decompressString(replayProp);
		CustomProperties p = new CustomProperties();
		p.decode(strEncode);
		return p;
	}

	/**
	 * Export to a String Array
	 *
	 * @return String Array (String[9])
	 */
	private String[] exportStringArray() {
		String[] s = new String[9];
		s[0] = NetUtil.urlEncode(playerName);
		s[1] = NetUtil.urlEncode(modeName);
		s[2] = NetUtil.urlEncode(ruleName);
		s[3] = stats == null ? "" : NetUtil.compressString(stats.exportString());
		s[4] = customStats.isEmpty() ? "" : NetUtil.compressString(exportCustomStats());
		s[5] = replayProp;
		s[6] = Integer.toString(gameType);
		s[7] = Integer.toString(style.ordinal());
		s[8] = timeStamp;
		return s;
	}

	/**
	 * Export to a String
	 *
	 * @return String (Split by ;)
	 */
	public String exportString() {
		String[] array = exportStringArray();
		return String.join(";", array);
	}

	/**
	 * Import from a String Array
	 *
	 * @param s String Array (String[9])
	 */
	private void importStringArray(String[] s) {
		playerName = NetUtil.urlDecode(s[0]);
		modeName = NetUtil.urlDecode(s[1]);
		ruleName = NetUtil.urlDecode(s[2]);
		if (s[3].isEmpty()) {
			stats = null;
		} else {
			stats = new Statistics(NetUtil.decompressString(s[3]));
		}
		if (s[4].isEmpty()) {
			customStats.clear();
		} else {
			importCustomStats(NetUtil.decompressString(s[4]));
		}
		replayProp = s[5];
		gameType = Integer.parseInt(s[6]);
		style = GameStyle.values()[Integer.parseInt(s[7])];
		timeStamp = s.length > 8 ? s[8] : "";
	}

	/**
	 * Import from a String
	 *
	 * @param s String (Split by ;)
	 */
	private void importString(String s) {
		importStringArray(s.split(";"));
	}

	/**
	 * Compare to other NetSPRecord
	 *
	 * @param type Ranking Type
	 * @param r2   The other NetSPRecord
	 * @return <code>true</code> if this this record is better than r2
	 */
	public boolean compare(RankingType type, NetSPRecord r2) {
		return compareRecords(type, this, r2);
	}

	/**
	 * Set String value of specific custom stat
	 *
	 * @param name  Custom stat name
	 * @param value Value
	 */
	public void setCustomStat(String name, String value) {
		customStats.put(name, value);
	}

	/**
	 * Get String value of specific custom stat
	 *
	 * @param name Custom stat name
	 * @return Value (null if not found)
	 */
	public String getCustomStat(String name) {
		return customStats.get(name);
	}

	/**
	 * Get String value of specific custom stat
	 *
	 * @param name       Custom stat name
	 * @param strDefault Default value (used when the name is not found)
	 * @return Value (strDefault if not found)
	 */
	public String getCustomStat(String name, String strDefault) {
		return customStats.getOrDefault(name, strDefault);
	}

	/**
	 * Get a short String of stats of the record (used by NetServer)
	 *
	 * @param type Ranking Type
	 * @return Short String of stats of the record
	 */
	public String getStatRow(RankingType type) {
		return switch (type) {
		case GENERIC_SCORE -> stats.score + "," + stats.lines + "," + stats.time;
		case GENERIC_TIME -> stats.time + "," + stats.totalPieceLocked + "," + stats.pps;
		case SCORERACE -> stats.time + "," + stats.lines + "," + stats.spl;
		case DIGRACE -> stats.time + "," + stats.lines + "," + stats.totalPieceLocked;
		case ULTRA -> stats.score + "," + stats.lines + "," + stats.totalPieceLocked;
		case COMBORACE -> stats.maxCombo + "," + stats.time + "," + stats.pps;
		case DIGCHALLENGE -> stats.score + "," + stats.lines + "," + stats.time;
		case TIMEATTACK -> stats.lines + "," + stats.time + "," + stats.pps + "," + stats.rollclear;
		default -> "";
		};
	}
}

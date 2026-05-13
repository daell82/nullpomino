package mu.nu.nullpo.game.net;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import mu.nu.nullpo.game.component.Statistics;
import mu.nu.nullpo.game.types.GameStyle;
import mu.nu.nullpo.util.CustomProperties;

/**
 * Single player mode record
 */
public class NetSPRecord implements Serializable {
	/** serialVersionUID for Serialize */
	private static final long serialVersionUID = 1L;

	/** Ranking type constants */
	public static final int RANKINGTYPE_GENERIC_SCORE = 0;
	public static final int RANKINGTYPE_GENERIC_TIME = 1;
	public static final int RANKINGTYPE_SCORERACE = 2;
	public static final int RANKINGTYPE_DIGRACE = 3;
	public static final int RANKINGTYPE_ULTRA = 4;
	public static final int RANKINGTYPE_COMBORACE = 5;
	public static final int RANKINGTYPE_DIGCHALLENGE = 6;
	public static final int RANKINGTYPE_TIMEATTACK = 7;

	/** Player Name */
	public String strPlayerName;

	/** Game Mode Name */
	public String strModeName;

	/** Rule Name */
	public String strRuleName;

	/** Main Stats */
	public Statistics stats;

	/** List of custom stats (Each String is NAME;VALUE format) */
	public final Map<String, String> customStats = new HashMap<>();

	/** Replay data (Compressed) */
	public String strReplayProp;

	/** Time stamp (GMT) */
	public String strTimeStamp;

	/** Game Type ID */
	public int gameType;

	/** Game Style */
	public GameStyle style;

	/**
	 * Compare 2 records
	 *
	 * @param type Ranking Type
	 * @param r1   Record 1
	 * @param r2   Record 2
	 * @return <code>true</code> if r1 is better than r2
	 */
	public static boolean compareRecords(int type, NetSPRecord r1, NetSPRecord r2) {
		Statistics s1 = r1.stats;
		Statistics s2 = r2.stats;

		switch (type) {
		case RANKINGTYPE_GENERIC_SCORE:
			if (s1.score > s2.score) {
				return true;
			}
			if (s1.score == s2.score && s1.lines > s2.lines) {
				return true;
			}
			if (s1.score == s2.score && s1.lines == s2.lines && s1.time < s2.time) {
				return true;
			}
			break;
		case RANKINGTYPE_GENERIC_TIME:
			if (s1.time < s2.time) {
				return true;
			}
			if (s1.time == s2.time && s1.totalPieceLocked < s2.totalPieceLocked) {
				return true;
			}
			if (s1.time == s2.time && s1.totalPieceLocked == s2.totalPieceLocked && s1.pps > s2.pps) {
				return true;
			}
			break;
		case RANKINGTYPE_SCORERACE:
			if (s1.time < s2.time) {
				return true;
			}
			if (s1.time == s2.time && s1.lines < s2.lines) {
				return true;
			}
			if (s1.time == s2.time && s1.lines == s2.lines && s1.spl > s2.spl) {
				return true;
			}
			break;
		case RANKINGTYPE_DIGRACE:
			if (s1.time < s2.time) {
				return true;
			}
			if (s1.time == s2.time && s1.lines < s2.lines) {
				return true;
			}
			if (s1.time == s2.time && s1.lines == s2.lines && s1.totalPieceLocked < s2.totalPieceLocked) {
				return true;
			}
			break;
		case RANKINGTYPE_ULTRA:
			if (s1.score > s2.score) {
				return true;
			}
			if (s1.score == s2.score && s1.lines > s2.lines) {
				return true;
			}
			if (s1.score == s2.score && s1.lines == s2.lines && s1.totalPieceLocked < s2.totalPieceLocked) {
				return true;
			}
			break;
		case RANKINGTYPE_COMBORACE:
			if (s1.maxCombo > s2.maxCombo) {
				return true;
			}
			if (s1.maxCombo == s2.maxCombo && s1.time < s2.time) {
				return true;
			}
			if (s1.maxCombo == s2.maxCombo && s1.time == s2.time && s1.pps > s2.pps) {
				return true;
			}
			break;
		case RANKINGTYPE_DIGCHALLENGE:
			if (s1.score > s2.score) {
				return true;
			}
			if (s1.score == s2.score && s1.lines > s2.lines) {
				return true;
			}
			if (s1.score == s2.score && s1.lines == s2.lines && s1.time > s2.time) {
				return true;
			}
			break;
		case RANKINGTYPE_TIMEATTACK: {
			// Cap the line count at 150 or 200
			int maxLines = r1.gameType >= 5 ? 200 : 150;
			int l1 = Math.min(s1.lines, maxLines);
			int l2 = Math.min(s2.lines, maxLines);
			if (s1.rollclear > s2.rollclear) {
				return true;
			}
			if (s1.rollclear == s2.rollclear && l1 > l2) {
				return true;
			}
			if (s1.rollclear == s2.rollclear && l1 == l2 && s1.time < s2.time) {
				return true;
			}
			if (s1.rollclear == s2.rollclear && l1 == l2 && s1.time == s2.time && s1.pps > s2.pps) {
				return true;
			}
			break;
		}
		default:
			break;
		}

		return false;
	}

	/**
	 * Default Constructor
	 */
	public NetSPRecord() {
		reset();
	}

	/**
	 * Copy Constructor
	 *
	 * @param s Source
	 */
	public NetSPRecord(NetSPRecord s) {
		copy(s);
	}

	/**
	 * Constructor that imports data from a String Array
	 *
	 * @param s String Array (String[6])
	 */
	public NetSPRecord(String[] s) {
		importStringArray(s);
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
	 * Initialization
	 */
	public void reset() {
		strPlayerName = "";
		strModeName = "";
		strRuleName = "";
		stats = null;
		customStats.clear();
		strReplayProp = "";
		strTimeStamp = "";
		gameType = 0;
		style = GameStyle.TETROMINO;
	}

	/**
	 * Copy from other NetSPRecord
	 *
	 * @param s Source
	 */
	public void copy(NetSPRecord s) {
		strPlayerName = s.strPlayerName;
		strModeName = s.strModeName;
		strRuleName = s.strRuleName;

		if (s.stats == null) {
			stats = null;
		} else {
			stats = new Statistics(s.stats);
		}

		customStats.clear();
		customStats.putAll(s.customStats);

		strReplayProp = s.strReplayProp;
		strTimeStamp = s.strTimeStamp;
		gameType = s.gameType;
		style = s.style;
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
		String strEncode = p.encode("NullpoMino Net Single Player Replay (" + strPlayerName + ")");
		strReplayProp = NetUtil.compressString(strEncode);
	}

	/**
	 * Get replay data as CustomProperties
	 *
	 * @return CustomProperties that contains replay data
	 */
	public CustomProperties getReplayProp() {
		String strEncode = NetUtil.decompressString(strReplayProp);
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
		s[0] = NetUtil.urlEncode(strPlayerName);
		s[1] = NetUtil.urlEncode(strModeName);
		s[2] = NetUtil.urlEncode(strRuleName);
		s[3] = stats == null ? "" : NetUtil.compressString(stats.exportString());
		s[4] = customStats.isEmpty() ? "" : NetUtil.compressString(exportCustomStats());
		s[5] = strReplayProp;
		s[6] = Integer.toString(gameType);
		s[7] = Integer.toString(style.ordinal());
		s[8] = strTimeStamp;
		return s;
	}

	/**
	 * Export to a String
	 *
	 * @return String (Split by ;)
	 */
	public String exportString() {
		String[] array = exportStringArray();
		return Arrays.stream(array).collect(Collectors.joining(";"));
	}

	/**
	 * Import from a String Array
	 *
	 * @param s String Array (String[9])
	 */
	public void importStringArray(String[] s) {
		strPlayerName = NetUtil.urlDecode(s[0]);
		strModeName = NetUtil.urlDecode(s[1]);
		strRuleName = NetUtil.urlDecode(s[2]);
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
		strReplayProp = s[5];
		gameType = Integer.parseInt(s[6]);
		style = GameStyle.values()[Integer.parseInt(s[7])];
		strTimeStamp = s.length > 8 ? s[8] : "";
	}

	/**
	 * Import from a String
	 *
	 * @param s String (Split by ;)
	 */
	public void importString(String s) {
		importStringArray(s.split(";"));
	}

	/**
	 * Compare to other NetSPRecord
	 *
	 * @param type Ranking Type
	 * @param r2   The other NetSPRecord
	 * @return <code>true</code> if this this record is better than r2
	 */
	public boolean compare(int type, NetSPRecord r2) {
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
		String strResult = getCustomStat(name);
		return strResult == null ? strDefault : strResult;
	}

	/**
	 * Get a short String of stats of the record (used by NetServer)
	 *
	 * @param type Ranking Type
	 * @return Short String of stats of the record
	 */
	public String getStatRow(int type) {
		return switch (type) {
		case RANKINGTYPE_GENERIC_SCORE -> stats.score + "," + stats.lines + "," + stats.time;
		case RANKINGTYPE_GENERIC_TIME -> stats.time + "," + stats.totalPieceLocked + "," + stats.pps;
		case RANKINGTYPE_SCORERACE -> stats.time + "," + stats.lines + "," + stats.spl;
		case RANKINGTYPE_DIGRACE -> stats.time + "," + stats.lines + "," + stats.totalPieceLocked;
		case RANKINGTYPE_ULTRA -> stats.score + "," + stats.lines + "," + stats.totalPieceLocked;
		case RANKINGTYPE_COMBORACE -> stats.maxCombo + "," + stats.time + "," + stats.pps;
		case RANKINGTYPE_DIGCHALLENGE -> stats.score + "," + stats.lines + "," + stats.time;
		case RANKINGTYPE_TIMEATTACK -> +stats.lines + "," + stats.time + "," + stats.pps + "," + stats.rollclear;
		default -> "";
		};
	}
}

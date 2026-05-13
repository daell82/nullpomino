package mu.nu.nullpo.game.net;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import mu.nu.nullpo.util.CustomProperties;

/**
 * Single player mode ranking
 */
public class NetSPRanking implements Serializable {

	/** serialVersionUID for Serialize */
	private static final long serialVersionUID = 1L;

	/** Game Mode Name */
	public String modeName;

	/** Rule Name */
	public String ruleName;

	/** Game Type ID */
	public int gameType;

	/** Game Style ID */
	public int style;

	/** Ranking Type */
	public int rankingType;

	/** Max number of records (-1:Unlimited) */
	public int maxRecords;

	/** Records */
	public List<NetSPRecord> records;

	/**
	 * Default Constructor
	 */
	public NetSPRanking() {
		reset();
	}

	/**
	 * Copy Constructor
	 *
	 * @param s Source
	 */
	public NetSPRanking(NetSPRanking s) {
		copy(s);
	}

	/**
	 * Constructor
	 *
	 * @param modename Game Mode Name
	 * @param rulename Rule Name
	 * @param gtype    Game Type ID
	 * @param style    Game Style ID
	 * @param rtype    Ranking Type
	 * @param max      Max number of records
	 */
	public NetSPRanking(String modename, String rulename, int gtype, int style, int rtype, int max) {
		reset();
		modeName = modename;
		ruleName = rulename;
		gameType = gtype;
		this.style = style;
		rankingType = rtype;
		maxRecords = max;
	}

	/**
	 * Initialization
	 */
	public void reset() {
		modeName = "";
		ruleName = "";
		gameType = 0;
		style = 0;
		rankingType = 0;
		maxRecords = 100;
		records = new LinkedList<>();
	}

	/**
	 * Copy from other NetSPRankingData
	 *
	 * @param s Source
	 */
	public void copy(NetSPRanking s) {
		modeName = s.modeName;
		ruleName = s.ruleName;
		gameType = s.gameType;
		style = s.style;
		rankingType = s.rankingType;
		maxRecords = s.maxRecords;
		records = new LinkedList<>();
		for (int i = 0; i < s.records.size(); i++) {
			records.add(new NetSPRecord(s.records.get(i)));
		}
	}

	/**
	 * Get specific player's record
	 *
	 * @param strPlayerName Player Name
	 * @return NetSPRecord (null if not found)
	 */
	public NetSPRecord getRecord(String strPlayerName) {
		int index = indexOf(strPlayerName);
		return index == -1 ? null : records.get(index);
	}

	/**
	 * Get specific player's record
	 *
	 * @param pInfo NetPlayerInfo
	 * @return NetSPRecord (null if not found)
	 */
	public NetSPRecord getRecord(NetPlayerInfo pInfo) {
		return getRecord(pInfo.strName);
	}

	/**
	 * Get specific player's index
	 *
	 * @param strPlayerName Player Name
	 * @return Index (-1 if not found)
	 */
	public int indexOf(String strPlayerName) {
		for (int i = 0; i < records.size(); i++) {
			NetSPRecord r = records.get(i);
			if (r.strPlayerName.equals(strPlayerName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get specific player's index
	 *
	 * @param pInfo NetPlayerInfo
	 * @return Index (-1 if not found)
	 */
	public int indexOf(NetPlayerInfo pInfo) {
		return indexOf(pInfo.strName);
	}

	/**
	 * Remove specific player's record
	 *
	 * @param strPlayerName Player Name
	 * @return Number of records removed (0 if not found)
	 */
	public int removeRecord(String strPlayerName) {
		int count = 0;

		List<NetSPRecord> list = new LinkedList<>(records);
		for (int i = 0; i < list.size(); i++) {
			NetSPRecord r = list.get(i);

			if (r.strPlayerName.equals(strPlayerName)) {
				records.remove(i);
				count++;
			}
		}

		return count;
	}

	/**
	 * Remove specific player's record
	 *
	 * @param pInfo NetPlayerInfo
	 * @return Number of records removed (0 if not found)
	 */
	public int removeRecord(NetPlayerInfo pInfo) {
		return removeRecord(pInfo.strName);
	}

	/**
	 * Checks if r1 is a new record.
	 *
	 * @param r1 Newer Record
	 * @return Returns <code>true</code> if there are no previous record of this
	 *         player, or if the newer record (r1) is better than old one.
	 */
	public boolean isNewRecord(NetSPRecord r1) {
		NetSPRecord r2 = getRecord(r1.strPlayerName);
		if (r2 == null) {
			return true;
		}
		return r1.compare(rankingType, r2);
	}

	/**
	 * Register a new record
	 *
	 * @param r1 Record
	 * @return Rank (-1 if out of rank)
	 */
	public int registerRecord(NetSPRecord r1) {
		if (!isNewRecord(r1)) {
			return -1;
		}

		// Remove older records
		removeRecord(r1.strPlayerName);

		// Insert new record
		LinkedList<NetSPRecord> list = new LinkedList<>(records);
		int rank = -1;

		for (int i = 0; i < list.size(); i++) {
			if (r1.compare(rankingType, list.get(i))) {
				records.add(i, r1);
				rank = i;
				break;
			}
		}

		// Couldn't rank in? Add to last.
		if (rank == -1) {
			records.add(r1);
			rank = records.size() - 1;
		}

		// Remove anything after maxRecords
		while (records.size() >= maxRecords) {
			records.removeLast();
		}

		// Done
		return rank >= maxRecords ? -1 : rank;
	}

	/**
	 * Write to a CustomProperties
	 *
	 * @param prop CustomProperties
	 */
	public void writeProperty(CustomProperties prop) {
		String strKey = "spranking." + ruleName + "." + modeName + "." + gameType + ".";
		prop.setProperty(strKey + "numRecords", records.size());

		for (int i = 0; i < records.size(); i++) {
			NetSPRecord netRecord = records.get(i);
			String strRecordCompressed = NetUtil.compressString(netRecord.exportString());
			prop.setProperty(strKey + i, strRecordCompressed);
		}
	}

	/**
	 * Read from a CustomProperties
	 *
	 * @param prop CustomProperties
	 */
	public void readProperty(CustomProperties prop) {
		String strKey = "spranking." + ruleName + "." + modeName + "." + gameType + ".";
		int numRecords = prop.getProperty(strKey + "numRecords", 0);
		if (numRecords > maxRecords) {
			numRecords = maxRecords;
		}

		records.clear();
		for (int i = 0; i < numRecords; i++) {
			String strRecordCompressed = prop.getProperty(strKey + i);
			if (strRecordCompressed != null) {
				String strRecord = NetUtil.decompressString(strRecordCompressed);
				NetSPRecord netRecord = new NetSPRecord(strRecord);
				records.add(netRecord);
			}
		}
	}

	/**
	 * Condense a list of rankings into a single ranking file.
	 *
	 * @param rankings The list of rankings.
	 * @return A ranking that is the combination of all of the rankings.
	 */
	public static NetSPRanking mergeRankings(List<NetSPRanking> rankings) {
		if (rankings == null || rankings.isEmpty()) {
			return null;
		}
		NetSPRanking acc = new NetSPRanking(rankings.get(0));
		for (NetSPRanking ranking : rankings) {
			for (NetSPRecord element : ranking.records) {
				acc.registerRecord(new NetSPRecord(element));
			}
		}
		return acc;
	}
}

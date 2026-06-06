package mu.nu.nullpo.game.net.ranking;

import java.util.LinkedList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import mu.nu.nullpo.game.net.NetUtil;
import mu.nu.nullpo.game.net.ranking.NetSPRecord.RankingType;
import mu.nu.nullpo.game.types.GameStyle;
import mu.nu.nullpo.util.CustomProperties;

/**
 * Single player mode ranking
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NetSPRanking {

	/** Game Mode Name */
	private String modeName;

	/** Rule Name */
	private String ruleName;

	/** Game Type ID */
	private int gameType;

	/** Game Style ID */
	private GameStyle style;

	/** Ranking Type */
	private RankingType rankingType;

	/** Max number of records (-1:Unlimited) */
	private int maxRecords;

	/** Records */
	private final List<NetSPRecord> records = new LinkedList<>();

	/**
	 * Copy Constructor
	 *
	 * @param s Source
	 */
	private NetSPRanking(NetSPRanking s) {
		modeName = s.modeName;
		ruleName = s.ruleName;
		gameType = s.gameType;
		style = s.style;
		rankingType = s.rankingType;
		maxRecords = s.maxRecords;
		s.records.stream().map(NetSPRecord::new).forEach(records::add);
	}

	/**
	 * Get specific player's record
	 *
	 * @param strPlayerName Player Name
	 * @return NetSPRecord (null if not found)
	 */
	public NetSPRecord getRecord(String strPlayerName) {
		// @formatter:off
		return records.stream()
				.filter(r -> r.playerName.equals(strPlayerName))
				.findFirst().orElse(null);
		// @formatter:on
	}

	/**
	 * Checks if r1 is a new record.
	 *
	 * @param r1 Newer Record
	 * @return Returns <code>true</code> if there are no previous record of this
	 *         player, or if the newer record (r1) is better than old one.
	 */
	private boolean isNewRecord(NetSPRecord r1) {
		NetSPRecord r2 = getRecord(r1.playerName);
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
		records.removeIf(r -> r.playerName.equals(r1.playerName));

		// Insert new record
		List<NetSPRecord> list = new LinkedList<>(records);
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
		NetSPRanking accumulated = new NetSPRanking(rankings.get(0));
		for (int i = 1; i < rankings.size(); i++) {
			NetSPRanking ranking = rankings.get(i);
			for (NetSPRecord element : ranking.records) {
				accumulated.registerRecord(new NetSPRecord(element));
			}
		}
		accumulated.ruleName = "all";
		return accumulated;
	}
}

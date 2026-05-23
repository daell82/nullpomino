package mu.nu.nullpo.game.net;

import java.util.LinkedList;
import java.util.List;

import mu.nu.nullpo.util.CustomProperties;

/**
 * Single player personal record manager
 */
public class NetSPPersonalBest {

	/** Player Name */
	public String playerName;

	/** Records */
	public List<NetSPRecord> records;

	/**
	 * Constructor
	 */
	public NetSPPersonalBest() {
		reset();
	}

	/**
	 * Copy Constructor
	 *
	 * @param s Source
	 */
	public NetSPPersonalBest(NetSPPersonalBest s) {
		copy(s);
	}

	/**
	 * Constructor that imports data from a String Array
	 *
	 * @param s String Array (String[2])
	 */
	public NetSPPersonalBest(String[] s) {
		importStringArray(s);
	}

	/**
	 * Constructor that imports data from a String
	 *
	 * @param s String (Split by ;)
	 */
	public NetSPPersonalBest(String s) {
		importString(s);
	}

	/**
	 * Initialization
	 */
	public void reset() {
		playerName = "";
		records = new LinkedList<>();
	}

	/**
	 * Copy from other NetSPPersonalBest
	 *
	 * @param s Source
	 */
	public void copy(NetSPPersonalBest s) {
		playerName = s.playerName;
		records = new LinkedList<>();
		for (int i = 0; i < s.records.size(); i++) {
			records.add(new NetSPRecord(s.records.get(i)));
		}
	}

	/**
	 * Get specific NetSPRecord
	 *
	 * @param rule  Rule Name
	 * @param mode  Mode Name
	 * @param gtype Game Type
	 * @return NetSPRecord (null if not found)
	 */
	public NetSPRecord getRecord(String rule, String mode, int gtype) {
		for (NetSPRecord r : records) {
			if (r.strRuleName.equals(rule) && r.strModeName.equals(mode) && r.gameType == gtype) {
				return r;
			}
		}
		return null;
	}

	/**
	 * Checks if r1 is a new record.
	 *
	 * @param rtype Ranking Type
	 * @param r1    Newer Record
	 * @return Returns <code>true</code> if there are no previous record of this
	 *         player, or if the newer record (r1) is better than old one.
	 */
	public boolean isNewRecord(int rtype, NetSPRecord r1) {
		NetSPRecord r2 = getRecord(r1.strRuleName, r1.strModeName, r1.gameType);
		if (r2 == null) {
			return true;
		}
		return r1.compare(rtype, r2);
	}

	/**
	 * Register a record.
	 *
	 * @param rtype Ranking Type
	 * @param r1    Newer Record
	 * @return Returns <code>true</code> if the newer record (r1) is registered.
	 */
	public boolean registerRecord(int rtype, NetSPRecord r1) {
		NetSPRecord r2 = getRecord(r1.strRuleName, r1.strModeName, r1.gameType);

		if (r2 != null) {
			if (r1.compare(rtype, r2)) {
				// Replace with a new record
				r2.copy(r1);
			} else {
				return false;
			}
		} else {
			// Register a new record
			records.add(r1);
		}

		return true;
	}

	/**
	 * Write to a CustomProperties
	 *
	 * @param prop CustomProperties
	 */
	public void writeProperty(CustomProperties prop) {
		String strKey = "sppersonal." + playerName + ".";
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
		String strKey = "sppersonal." + playerName + ".";
		int numRecords = prop.getProperty(strKey + "numRecords", 0);

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
	 * Export the records to a String
	 *
	 * @return String (Split by ;)
	 */
	public String exportListRecord() {
		String strResult = "";
		for (int i = 0; i < records.size(); i++) {
			if (i > 0) {
				strResult += ";";
			}
			strResult += NetUtil.compressString(records.get(i).exportString());
		}
		return strResult;
	}

	/**
	 * Import the record from a String
	 *
	 * @param s String (Split by ;)
	 */
	public void importListRecord(String s) {
		records.clear();

		String[] array = s.split(";");
		for (String element : array) {
			String strTemp = NetUtil.decompressString(element);
			NetSPRecord netRecord = new NetSPRecord(strTemp);
			records.add(netRecord);
		}
	}

	/**
	 * Export to a String Array
	 *
	 * @return String Array (String[2])
	 */
	public String[] exportStringArray() {
		String[] s = new String[2];
		s[0] = NetUtil.urlEncode(playerName);
		s[1] = exportListRecord();
		return s;
	}

	/**
	 * Export to a String
	 *
	 * @return String (Split by ;)
	 */
	public String exportString() {
		String[] array = exportStringArray();
		String result = "";

		for (int i = 0; i < array.length; i++) {
			if (i > 0) {
				result += ";";
			}
			result += array[i];
		}

		return result;
	}

	/**
	 * Import from a String Array
	 *
	 * @param s String Array (String[8])
	 */
	public void importStringArray(String[] s) {
		if (s.length > 0) {
			playerName = NetUtil.urlDecode(s[0]);
		}
		if (s.length > 1) {
			importListRecord(s[1]);
		}
	}

	/**
	 * Import from a String
	 *
	 * @param s String (Split by ;)
	 */
	public void importString(String s) {
		importStringArray(s.split(";"));
	}
}

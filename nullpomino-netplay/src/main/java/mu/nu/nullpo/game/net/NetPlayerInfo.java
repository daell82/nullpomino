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

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.net.ranking.NetSPRecord;
import mu.nu.nullpo.game.net.ranking.NetSPRecord.RankingType;
import mu.nu.nullpo.game.types.GameStyle;
import mu.nu.nullpo.util.CustomProperties;

/**
 * Player information
 */
@Log4j
@Getter
public class NetPlayerInfo {

	/** Default rating for multiplayer games */
	public static final int DEFAULT_MULTIPLAYER_RATING = 1500;

	/** Name */
	@Setter
	private String playerName = "";

	/** Country code */
	@Setter
	private String country = "";

	/** Host */
	@Setter
	private String host = "";

	/** Team name */
	private Optional<String> team = Optional.empty();

	/** Rules in use */
	@Setter
	private RuleOptions rule = null;

	/** Multiplayer rating */
	public int[] rating = new int[GameStyle.numStyles()];

	/** Rating backup (internal use) */
	public int[] ratingBefore = new int[GameStyle.numStyles()];

	/** Number of rated multiplayer games played */
	public int[] playCount = new int[GameStyle.numStyles()];

	/** Number of games played in current room */
	public int playCountNow = 0;

	/** Number of rated multiplayer games win */
	public int[] winCount = new int[GameStyle.numStyles()];

	/** Number of wins in current room */
	public int winCountNow = 0;

	/** Single player personal records */
	private final List<NetSPRecord> records = new LinkedList<>();

	/** User ID */
	public int uid = -1;

	/** Current room ID */
	public int roomID = -1;

	/** Game seat number (-1 if spectator) */
	public int seatID = -1;

	/** Join queue number (-1 if not in queue) */
	public int queueID = -1;

	/** true if "Ready" sign */
	@Setter
	private boolean ready = false;

	/** true if playing now */
	@Setter
	private boolean playing = false;

	/** true if connected */
	@Setter
	private boolean connected = false;

	/** true if this player is using tripcode */
	@Setter
	private boolean tripUse = false;

	/** Real host name (for internal use) */
	public String realHost = "";

	/** Real IP (for internal use) */
	public String realIP = "";

	/** SocketChannel of this player (for internal use) */
	public SocketChannel channel = null;

	/**
	 * Constructor
	 */
	public NetPlayerInfo() {
	}

	/**
	 * String constructor (Uses importString)
	 *
	 * @param str String(Divided by ;)
	 */
	public NetPlayerInfo(String str) {
		importStringArray(str.split(";"));
	}

	/**
	 * Import from String array
	 *
	 * @param pdata String array (String[27])
	 */
	private void importStringArray(String[] pdata) {
		playerName = NetUtil.urlDecode(pdata[0]);
		country = NetUtil.urlDecode(pdata[1]);
		host = NetUtil.urlDecode(pdata[2]);
		setTeam(NetUtil.urlDecode(pdata[3]));
		roomID = Integer.parseInt(pdata[4]);
		uid = Integer.parseInt(pdata[5]);
		seatID = Integer.parseInt(pdata[6]);
		queueID = Integer.parseInt(pdata[7]);
		ready = Boolean.parseBoolean(pdata[8]);
		playing = Boolean.parseBoolean(pdata[9]);
		connected = Boolean.parseBoolean(pdata[10]);
		tripUse = Boolean.parseBoolean(pdata[11]);
		rating[0] = Integer.parseInt(pdata[12]);
		rating[1] = Integer.parseInt(pdata[13]);
		rating[2] = Integer.parseInt(pdata[14]);
		rating[3] = Integer.parseInt(pdata[15]);
		playCount[0] = Integer.parseInt(pdata[16]);
		playCount[1] = Integer.parseInt(pdata[17]);
		playCount[2] = Integer.parseInt(pdata[18]);
		playCount[3] = Integer.parseInt(pdata[19]);
		winCount[0] = Integer.parseInt(pdata[20]);
		winCount[1] = Integer.parseInt(pdata[21]);
		winCount[2] = Integer.parseInt(pdata[22]);
		winCount[3] = Integer.parseInt(pdata[23]);
		if (pdata.length > 24) {
			importRecords(pdata[24]);
		}
		if (pdata.length > 25) {
			playCountNow = Integer.parseInt(pdata[25]);
		}
		if (pdata.length > 26) {
			winCountNow = Integer.parseInt(pdata[26]);
		}
	}

	/**
	 * Sets the team name of this player. If the team name is empty or {@code null}
	 * the team-property will be cleared
	 *
	 * @param teamName to set
	 * @see Optional#isEmpty()
	 */
	public void setTeam(String teamName) {
		if (teamName == null || teamName.isBlank()) {
			team = Optional.empty();
		} else {
			team = Optional.of(teamName);
		}
	}

	/**
	 * Export to String array
	 *
	 * @return String array (String[27])
	 */
	private String[] exportStringArray() {
		String[] pdata = new String[27];
		pdata[0] = NetUtil.urlEncode(playerName);
		pdata[1] = NetUtil.urlEncode(country);
		pdata[2] = NetUtil.urlEncode(host);
		pdata[3] = NetUtil.urlEncode(team.orElse(""));
		pdata[4] = Integer.toString(roomID);
		pdata[5] = Integer.toString(uid);
		pdata[6] = Integer.toString(seatID);
		pdata[7] = Integer.toString(queueID);
		pdata[8] = Boolean.toString(ready);
		pdata[9] = Boolean.toString(playing);
		pdata[10] = Boolean.toString(connected);
		pdata[11] = Boolean.toString(tripUse);
		pdata[12] = Integer.toString(rating[0]);
		pdata[13] = Integer.toString(rating[1]);
		pdata[14] = Integer.toString(rating[2]);
		pdata[15] = Integer.toString(rating[3]);
		pdata[16] = Integer.toString(playCount[0]);
		pdata[17] = Integer.toString(playCount[1]);
		pdata[18] = Integer.toString(playCount[2]);
		pdata[19] = Integer.toString(playCount[3]);
		pdata[20] = Integer.toString(winCount[0]);
		pdata[21] = Integer.toString(winCount[1]);
		pdata[22] = Integer.toString(winCount[2]);
		pdata[23] = Integer.toString(winCount[3]);
		pdata[24] = exportRecords();
		pdata[25] = Integer.toString(playCountNow);
		pdata[26] = Integer.toString(winCountNow);
		return pdata;
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
			String recordData = NetUtil.compressString(netRecord.exportString());
			prop.setProperty(strKey + i, recordData);
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
			String recordData = prop.getProperty(strKey + i);
			if (recordData != null) {
				String strRecord = NetUtil.decompressString(recordData);
				records.add(new NetSPRecord(strRecord));
			}
		}
	}

	/**
	 * Import the record from a String
	 *
	 * @param s String (Split by ;)
	 */
	private void importRecords(String raw) {
		records.clear();
		if (raw.isBlank()) {
			return;
		}
		String uncompressed = NetUtil.decompressString(raw);
		if (uncompressed.isBlank()) {
			return;
		}
		String[] array = uncompressed.split(";");
		for (String data : array) {
			String recordData = NetUtil.decompressString(data);
			if (recordData.isBlank()) {
				continue;
			}
			records.add(new NetSPRecord(recordData));
		}
	}

	/**
	 * Export the records to a String
	 *
	 * @return String (Split by ;)
	 */
	private String exportRecords() {
		// @formatter:off
		String data = records.stream()
				.map(NetSPRecord::exportString)
				.map(NetUtil::compressString)
				.collect(Collectors.joining(";"));
		return NetUtil.compressString(data);
		// @formatter:on
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

	/**
	 * Get specific NetSPRecord
	 *
	 * @param rule  Rule Name
	 * @param mode  Mode Name
	 * @param gtype Game Type
	 * @return NetSPRecord (null if not found)
	 */
	public NetSPRecord findRecord(String rule, String mode, int gtype) {
		for (NetSPRecord r : records) {
			if (r.ruleName.equals(rule) && r.modeName.equals(mode) && r.gameType == gtype) {
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
	public boolean isNewRecord(RankingType rtype, NetSPRecord r1) {
		NetSPRecord r2 = findRecord(r1.ruleName, r1.modeName, r1.gameType);
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
	public boolean registerRecord(RankingType rtype, NetSPRecord r1) {
		NetSPRecord r2 = findRecord(r1.ruleName, r1.modeName, r1.gameType);

		if (r2 != null) {
			if (r1.compare(rtype, r2)) {
				// Replace with a new record
				records.set(records.indexOf(r2), r1);
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
	 * Reset play flags
	 */
	public void resetPlayState() {
		ready = false;
		playing = false;
	}

	/**
	 * Delete this player
	 */
	public void delete() {
		rule = null;
	}
}

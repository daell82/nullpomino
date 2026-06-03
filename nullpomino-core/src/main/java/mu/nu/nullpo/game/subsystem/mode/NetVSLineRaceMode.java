package mu.nu.nullpo.game.subsystem.mode;

import java.util.LinkedList;
import java.util.List;

import mu.nu.nullpo.game.net.NetCmd;
import mu.nu.nullpo.game.net.NetMessage;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.types.DisplaySize;
import mu.nu.nullpo.util.Colors;
import mu.nu.nullpo.util.GeneralUtil;
import mu.nu.nullpo.util.Sounds;

/**
 * NET-VS-LINE RACE mode
 */
public class NetVSLineRaceMode extends NetDummyVSMode {

	/** Number of lines required to win */
	private int goalLines; // TODO: Add option to change this

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "NET-VS-LINE RACE";
	}

	/*
	 * Mode init
	 */
	@Override
	public void modeInit(GameManager manager) {
		super.modeInit(manager);
		goalLines = 40;
	}

	/*
	 * Player init
	 */
	@Override
	protected void netPlayerInit(GameEngine engine, int playerID) {
		super.netPlayerInit(engine, playerID);
		engine.meterColor = Colors.METER_COLOR_GREEN;
	}

	/**
	 * Apply room settings, but ignore non-speed settings
	 */
	@Override
	protected void netvsApplyRoomSettings(GameEngine engine) {
		if (netCurrentRoomInfo != null) {
			engine.speed.gravity = netCurrentRoomInfo.gravity;
			engine.speed.denominator = netCurrentRoomInfo.denominator;
			engine.speed.are = netCurrentRoomInfo.are;
			engine.speed.areLine = netCurrentRoomInfo.areLine;
			engine.speed.lineDelay = netCurrentRoomInfo.lineDelay;
			engine.speed.lockDelay = netCurrentRoomInfo.lockDelay;
			engine.speed.das = netCurrentRoomInfo.das;
		}
	}

	/*
	 * Called at game start
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		super.startGame(engine, playerID);
		engine.meterColor = Colors.METER_COLOR_GREEN;
		engine.meterValue = owner.renderer.getMeterMax(engine);
	}

	/**
	 * Get player's place
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return Player's place
	 */
	private int getNowPlayerPlace(GameEngine engine, int playerID) {
		if (!netvsPlayerExist[playerID] || netvsPlayerDead[playerID]) {
			return -1;
		}

		int place = 0;
		int myLines = Math.min(engine.statistics.lines, goalLines);

		for (int i = 0; i < getPlayers(); i++) {
			if (i != playerID && netvsPlayerExist[i] && !netvsPlayerDead[i]) {
				int enemyLines = Math.min(owner.engines[i].statistics.lines, goalLines);

				if (myLines < enemyLines) {
					place++;
				} else if (myLines == enemyLines && engine.statistics.pps < owner.engines[i].statistics.pps) {
					place++;
				} else if (myLines == enemyLines && engine.statistics.pps == owner.engines[i].statistics.pps
						&& engine.statistics.lpm < owner.engines[i].statistics.lpm) {
					place++;
				}
			}
		}

		return place;
	}

	/**
	 * Update progress meter
	 *
	 * @param engine GameEngine
	 */
	private void updateMeter(GameEngine engine) {
		if (goalLines > 0) {
			int remainLines = goalLines - engine.statistics.lines;
			engine.meterValue = remainLines * owner.renderer.getMeterMax(engine) / goalLines;
			engine.meterColor = Colors.METER_COLOR_GREEN;
			if (remainLines <= 30) {
				engine.meterColor = Colors.METER_COLOR_YELLOW;
			}
			if (remainLines <= 20) {
				engine.meterColor = Colors.METER_COLOR_ORANGE;
			}
			if (remainLines <= 10) {
				engine.meterColor = Colors.METER_COLOR_RED;
			}
		}
	}

	/*
	 * Calculate Score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Meter
		updateMeter(engine);

		// All clear
		if (lines >= 1 && engine.field.isEmpty()) {
			engine.playSE(Sounds.BRAVO);
		}

		// Game Completed
		if (engine.statistics.lines >= goalLines && playerID == 0) {
			if (netvsIsPractice) {
				engine.stat = GameEngine.Status.EXCELLENT;
				engine.resetStatc();
			} else {
				// Send game end message
				int[] places = new int[NETVS_MAX_PLAYERS];
				int[] uidArray = new int[NETVS_MAX_PLAYERS];
				for (int i = 0; i < getPlayers(); i++) {
					places[i] = getNowPlayerPlace(owner.engines[i], i);
					uidArray[i] = -1;
				}
				for (int i = 0; i < getPlayers(); i++) {
					if (places[i] >= 0 && places[i] < NETVS_MAX_PLAYERS) {
						uidArray[places[i]] = netvsPlayerUID[i];
					}
				}

				List<Integer> ids = new LinkedList<>();
				for (int i = 0; i < getPlayers(); i++) {
					if (uidArray[i] != -1) {
						ids.add(uidArray[i]);
					}
				}
				netLobby.netPlayerClient.send(NetCmd.RACE_WIN, ids.toArray());

				// Wait until everyone dies
				engine.stat = GameEngine.Status.NOTHING;
				engine.resetStatc();
			}
		}
	}

	/*
	 * Drawing processing at the end of every frame
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		super.renderLast(engine, playerID);

		int x = owner.renderer.getFieldDisplayPositionX(engine, playerID);
		int y = owner.renderer.getFieldDisplayPositionY(engine, playerID);

		if (!netvsPlayerExist[playerID] || !engine.isVisible) {
			return;
		}
		if ((netvsIsGameActive || netvsIsPractice && playerID == 0) && engine.stat != GameEngine.Status.RESULT) {
			// Lines left
			int remainLines = Math.max(0, goalLines - engine.statistics.lines);
			int fontColor = Colors.FONT_WHITE;
			if (remainLines <= 30 && remainLines > 0) {
				fontColor = Colors.FONT_YELLOW;
			}
			if (remainLines <= 20 && remainLines > 0) {
				fontColor = Colors.FONT_ORANGE;
			}
			if (remainLines <= 10 && remainLines > 0) {
				fontColor = Colors.FONT_RED;
			}

			String strLines = String.valueOf(remainLines);

			if (engine.displaySize != DisplaySize.SMALL) {
				if (strLines.length() == 1) {
					owner.renderer.drawMenuFont(engine, playerID, 4, 21, strLines, fontColor, 2.0f);
				} else if (strLines.length() == 2) {
					owner.renderer.drawMenuFont(engine, playerID, 3, 21, strLines, fontColor, 2.0f);
				} else if (strLines.length() == 3) {
					owner.renderer.drawMenuFont(engine, playerID, 2, 21, strLines, fontColor, 2.0f);
				}
			} else if (strLines.length() == 1) {
				owner.renderer.drawDirectFont(engine, playerID, x + 4 + 32, y + 168, strLines, fontColor, 1.0f);
			} else if (strLines.length() == 2) {
				owner.renderer.drawDirectFont(engine, playerID, x + 4 + 24, y + 168, strLines, fontColor, 1.0f);
			} else if (strLines.length() == 3) {
				owner.renderer.drawDirectFont(engine, playerID, x + 4 + 16, y + 168, strLines, fontColor, 1.0f);
			}
		}

		if (netvsIsGameActive && engine.stat != GameEngine.Status.RESULT) {
			// Place
			int place = getNowPlayerPlace(engine, playerID);
			if (netvsPlayerDead[playerID]) {
				place = netvsPlayerPlace[playerID];
			}

			if (engine.displaySize != DisplaySize.SMALL) {
				switch (place) {
				case 0 -> renderer.drawMenuFont(engine, playerID, -2, 22, "1ST", Colors.FONT_ORANGE);
				case 1 -> renderer.drawMenuFont(engine, playerID, -2, 22, "2ND", Colors.FONT_WHITE);
				case 2 -> renderer.drawMenuFont(engine, playerID, -2, 22, "3RD", Colors.FONT_RED);
				case 3 -> renderer.drawMenuFont(engine, playerID, -2, 22, "4TH", Colors.FONT_GREEN);
				case 4 -> renderer.drawMenuFont(engine, playerID, -2, 22, "5TH", Colors.FONT_BLUE);
				case 5 -> renderer.drawMenuFont(engine, playerID, -2, 22, "6TH", Colors.FONT_PURPLE);
				default -> { // nothing
				}
				}
			} else {
				switch (place) {
				case 0 -> renderer.drawDirectFont(engine, playerID, x, y + 168, "1ST", Colors.FONT_ORANGE, 0.5f);
				case 1 -> renderer.drawDirectFont(engine, playerID, x, y + 168, "2ND", Colors.FONT_WHITE, 0.5f);
				case 2 -> renderer.drawDirectFont(engine, playerID, x, y + 168, "3RD", Colors.FONT_RED, 0.5f);
				case 3 -> renderer.drawDirectFont(engine, playerID, x, y + 168, "4TH", Colors.FONT_GREEN, 0.5f);
				case 4 -> renderer.drawDirectFont(engine, playerID, x, y + 168, "5TH", Colors.FONT_BLUE, 0.5f);
				case 5 -> renderer.drawDirectFont(engine, playerID, x, y + 168, "6TH", Colors.FONT_PURPLE, 0.5f);
				default -> { // nothing
				}
				}
			}
		}
		// Games count
		else if (!netvsIsPractice || playerID != 0) {
			String strTemp = netvsPlayerWinCount[playerID] + "/" + netvsPlayerPlayCount[playerID];

			if (engine.displaySize != DisplaySize.SMALL) {
				int y2 = 21;
				if (engine.stat == GameEngine.Status.RESULT) {
					y2 = 22;
				}
				owner.renderer.drawMenuFont(engine, playerID, 0, y2, strTemp, Colors.FONT_WHITE);
			} else {
				owner.renderer.drawDirectFont(engine, playerID, x + 4, y + 168, strTemp, Colors.FONT_WHITE, 0.5f);
			}
		}
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		super.renderResult(engine, playerID);

		float scale = 1.0f;
		if (engine.displaySize == DisplaySize.SMALL) {
			scale = 0.5f;
		}
		// @formatter:off
		drawResultScale(engine, playerID, 2, Colors.FONT_ORANGE, scale,
				"LINE",	String.format("%10d", engine.statistics.lines),
				"PIECE", String.format("%10d", engine.statistics.totalPieceLocked),
				"LINE/MIN", String.format("%10g", engine.statistics.lpm),
				"PIECE/SEC", String.format("%10g", engine.statistics.pps),
				"TIME", String.format("%10s", GeneralUtil.getTime(engine.statistics.time)));
		// @formatter:on
	}

	/*
	 * Send stats
	 */
	@Override
	protected void netSendStats(GameEngine engine) {
		if (engine.playerID == 0 && !netvsIsPractice && !netvsIsWatch()) {
			String stats = "stats\t";
			stats += engine.statistics.lines + "\t";
			stats += engine.statistics.pps + "\t";
			stats += engine.statistics.lpm;
			netLobby.netPlayerClient.send(NetCmd.GAME, stats);
		}
	}

	/*
	 * Receive stats
	 */
	@Override
	protected void netRecvStats(GameEngine engine, NetMessage message) {
		if (message.length() > 3) {
			engine.statistics.lines = message.asInt(3);
		}
		if (message.length() > 4) {
			engine.statistics.pps = message.asFloat(4);
		}
		if (message.length() > 5) {
			engine.statistics.lpm = message.asFloat(5);
		}
		updateMeter(engine);
	}

	/*
	 * Send end-of-game stats
	 */
	@Override
	protected void netSendEndGameStats(GameEngine engine) {
		int playerID = engine.playerID;
		String stats = "";
		stats += netvsPlayerPlace[playerID] + "\t";
		stats += 0 + "\t";
		stats += 0 + "\t";
		stats += 0 + "\t";
		stats += engine.statistics.lines + "\t";
		stats += engine.statistics.lpm + "\t";
		stats += engine.statistics.totalPieceLocked + "\t";
		stats += engine.statistics.pps + "\t";
		stats += netvsPlayTimer + "\t";
		stats += 0 + "\t";
		stats += netvsPlayerWinCount[playerID] + "\t";
		stats += netvsPlayerPlayCount[playerID];
		netLobby.netPlayerClient.send(NetCmd.GSTAT, stats);
	}

	/*
	 * Receive end-of-game stats
	 */
	@Override
	protected void netvsRecvEndGameStats(NetMessage message) {
		int seatID = message.asInt(1);
		int playerID = netvsGetPlayerIDbySeatID(seatID);

		if (playerID != 0 || netvsIsWatch()) {
			GameEngine engine = owner.engines[playerID];

			engine.statistics.lines = message.asInt(7);
			engine.statistics.lpm = message.asFloat(8);
			engine.statistics.totalPieceLocked = message.asInt(9);
			engine.statistics.pps = message.asFloat(10);
			engine.statistics.time = message.asInt(11);

			netvsPlayerResultReceived[playerID] = true;
		}
	}
}

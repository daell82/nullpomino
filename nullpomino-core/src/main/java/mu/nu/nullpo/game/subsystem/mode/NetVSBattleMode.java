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
package mu.nu.nullpo.game.subsystem.mode;

import java.util.LinkedList;
import java.util.Locale;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.net.NetCmd;
import mu.nu.nullpo.game.net.NetMessage;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.types.DisplaySize;
import mu.nu.nullpo.util.Colors;
import mu.nu.nullpo.util.GeneralUtil;
import mu.nu.nullpo.util.Sounds;

/**
 * NET-VS-BATTLE Mode
 */
public class NetVSBattleMode extends NetDummyVSMode {

	/** Type of attack performed */
	private static final int ATTACK_CATEGORY_NORMAL = 0;
	private static final int ATTACK_CATEGORY_B2B = 1;
	private static final int ATTACK_CATEGORY_SPIN = 2;
	private static final int ATTACK_CATEGORY_COMBO = 3;
	private static final int ATTACK_CATEGORY_BRAVO = 4;
	private static final int ATTACK_CATEGORY_GEM = 5;
	private static final int ATTACK_CATEGORIES = 6;

	/** Attack table (for T-Spin only) */
	private static final int[][] LINE_ATTACK_TABLE = {
			// 1-2P, 3P, 4P, 5P, 6P
			{ 0, 0, 0, 0, 0 }, // Single
			{ 1, 1, 0, 0, 0 }, // Double
			{ 2, 2, 1, 1, 1 }, // Triple
			{ 4, 3, 2, 2, 2 }, // Four
			{ 1, 1, 0, 0, 0 }, // T-Mini-S
			{ 2, 2, 1, 1, 1 }, // T-Single
			{ 4, 3, 2, 2, 2 }, // T-Double
			{ 6, 4, 3, 3, 3 }, // T-Triple
			{ 4, 3, 2, 2, 2 }, // T-Mini-D
			{ 1, 1, 0, 0, 0 }, // EZ-T
	};

	/** Attack table(for All Spin) */
	private static final int[][] LINE_ATTACK_TABLE_ALLSPIN = {
			// 1-2P, 3P, 4P, 5P, 6P
			{ 0, 0, 0, 0, 0 }, // Single
			{ 1, 1, 0, 0, 0 }, // Double
			{ 2, 2, 1, 1, 1 }, // Triple
			{ 4, 3, 2, 2, 2 }, // Four
			{ 0, 0, 0, 0, 0 }, // T-Mini-S
			{ 2, 2, 1, 1, 1 }, // T-Single
			{ 4, 3, 2, 2, 2 }, // T-Double
			{ 6, 4, 3, 3, 3 }, // T-Triple
			{ 3, 2, 1, 1, 1 }, // T-Mini-D
			{ 0, 0, 0, 0, 0 }, // EZ-T
	};

	/** Indexes of attack types in attack table */
	private static final int LINE_ATTACK_INDEX_SINGLE = 0;
	private static final int LINE_ATTACK_INDEX_DOUBLE = 1;
	private static final int LINE_ATTACK_INDEX_TRIPLE = 2;
	private static final int LINE_ATTACK_INDEX_FOUR = 3;
	private static final int LINE_ATTACK_INDEX_TMINI = 4;
	private static final int LINE_ATTACK_INDEX_TSINGLE = 5;
	private static final int LINE_ATTACK_INDEX_TDOUBLE = 6;
	private static final int LINE_ATTACK_INDEX_TTRIPLE = 7;
	private static final int LINE_ATTACK_INDEX_TMINI_D = 8;
	private static final int LINE_ATTACK_INDEX_EZ_T = 9;

	/** Combo attack table */
	private static final int[][] COMBO_ATTACK_TABLE = { //
			{ 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 4, 5 }, // 1-2 Player(s)
			{ 0, 0, 1, 1, 1, 2, 2, 3, 3, 4, 4, 4 }, // 3 Player
			{ 0, 0, 0, 1, 1, 1, 2, 2, 3, 3, 4, 4 }, // 4 Player
			{ 0, 0, 0, 1, 1, 1, 1, 2, 2, 3, 3, 4 }, // 5 Player
			{ 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 3, 3 }, // 6 Payers
	};

	/** Garbage denominator (can be divided by 2,3,4,5) */
	private static final int GARBAGE_DENOMINATOR = 60;

	/** Column number of hole in most recent garbage line */
	private int lastHole = -1;

	/** true if Hurry Up has been started */
	private boolean hurryupStarted;

	/** Number of frames left to show "HURRY UP!" text */
	private int hurryupShowFrames;

	/** Number of pieces placed after Hurry Up has started */
	private int hurryupCount;

	/** true if you KO'd player */
	private boolean[] playerKObyYou;

	/** Your KO count */
	private int currentKO;

	/** Time to display the most recent increase in score */
	private int[] scgettime;

	private LineClearEvent[] lastevents;

	/** true if most recent scoring event was B2B */
	private boolean[] lastb2b;

	/** Most recent scoring event Combo count */
	private int[] lastcombo;

	/** Most recent scoring event piece type */
	private int[] lastpiece;

	/** Count of garbage lines send */
	private int[] garbageSent;

	/** Amount of garbage in garbage queue */
	private int[] garbage;

	/** Recieved garbage entries */
	private LinkedList<GarbageEntry> garbageEntries;

	/** APL (Attack Per Line) */
	private float[] playerAPL;

	/** APM (Attack Per Minute) */
	private float[] playerAPM;

	/** Target ID (-1:All) */
	private int targetID;

	/** Target Timer */
	private int targetTimer;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "NET-VS-BATTLE";
	}

	@Override
	public boolean isVSMode() {
		return true;
	}

	/*
	 * Mode Initialization
	 */
	@Override
	public void modeInit(GameManager manager) {
		super.modeInit(manager);
		playerKObyYou = new boolean[NETVS_MAX_PLAYERS];
		scgettime = new int[NETVS_MAX_PLAYERS];
		lastevents = new LineClearEvent[NETVS_MAX_PLAYERS];
		lastb2b = new boolean[NETVS_MAX_PLAYERS];
		lastcombo = new int[NETVS_MAX_PLAYERS];
		lastpiece = new int[NETVS_MAX_PLAYERS];
		garbageSent = new int[NETVS_MAX_PLAYERS];
		garbage = new int[NETVS_MAX_PLAYERS];
		playerAPL = new float[NETVS_MAX_PLAYERS];
		playerAPM = new float[NETVS_MAX_PLAYERS];
	}

	/**
	 * Get number of possible targets (number of opponents)
	 *
	 * @return Number of possible targets (number of opponents)
	 */
	private int getNumberOfPossibleTargets() {
		int count = 0;
		for (int i = 1; i < getPlayers(); i++) {
			if (netvsIsAttackable(i)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Set new target
	 */
	private void setNewTarget() {
		if (getNumberOfPossibleTargets() >= 1 && netCurrentRoomInfo != null && netCurrentRoomInfo.isTarget
				&& !netvsIsWatch() && !netvsIsPractice) {
			do {
				targetID++;
				if (targetID >= getPlayers()) {
					targetID = 1;
				}
			} while (!netvsIsAttackable(targetID));
		} else {
			targetID = -1;
		}
	}

	/**
	 * Get number of garbage lines the local player has
	 *
	 * @return Number of garbage lines
	 */
	private int getTotalGarbageLines() {
		int count = 0;
		for (GarbageEntry entry : garbageEntries) {
			count += entry.lines;
		}
		return count;
	}

	/*
	 * Initialization for each player
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		renderer = engine.owner.renderer;

		if (playerID == 0 && !netvsIsWatch()) {
			lastHole = -1;
			hurryupCount = 0;
			currentKO = 0;
			targetID = -1;
			targetTimer = 0;

			if (garbageEntries == null) {
				garbageEntries = new LinkedList<>();
			} else {
				garbageEntries.clear();
			}
		}

		playerKObyYou[playerID] = false;
		scgettime[playerID] = 0;
		lastevents[playerID] = LineClearEvent.NONE;
		lastb2b[playerID] = false;
		lastcombo[playerID] = 0;
		lastpiece[playerID] = 0;
		garbageSent[playerID] = 0;
		garbage[playerID] = 0;
		playerAPL[playerID] = 0f;
		playerAPM[playerID] = 0f;
	}

	/*
	 * Executed after Ready->Go, before the first piece appears.
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		super.startGame(engine, playerID);

		if (playerID == 0 && !netvsIsWatch()) {
			if (!netvsIsPractice) {
				hurryupStarted = false;
				hurryupShowFrames = 0;
			}
			setNewTarget();
			targetTimer = 0;
		}
	}

	/*
	 * Calculate Score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Attack
		if (lines > 0 && playerID == 0) {
			int[] pts = new int[ATTACK_CATEGORIES];

			scgettime[playerID] = 0;

			int numAliveTeams = netvsGetNumberOfTeamsAlive();
			int attackNumPlayerIndex = numAliveTeams - 2;
			if (netvsIsPractice || !netCurrentRoomInfo.reduceLineSend) {
				attackNumPlayerIndex = 0;
			}
			if (attackNumPlayerIndex < 0) {
				attackNumPlayerIndex = 0;
			}
			if (attackNumPlayerIndex > 4) {
				attackNumPlayerIndex = 4;
			}

			int attackLineIndex = LINE_ATTACK_INDEX_SINGLE;
			int mainAttackCategory = ATTACK_CATEGORY_NORMAL;

			// T-Spin style attack
			if (engine.tspin) {
				mainAttackCategory = ATTACK_CATEGORY_SPIN;

				// EZ-T
				if (engine.tspinez) {
					attackLineIndex = LINE_ATTACK_INDEX_EZ_T;
					lastevents[playerID] = LineClearEvent.TSPIN_EZ;
				}
				// T-Spin 1 line
				else if (lines == 1) {
					if (engine.tspinmini) {
						attackLineIndex = LINE_ATTACK_INDEX_TMINI;
						lastevents[playerID] = LineClearEvent.TSPIN_SINGLE_MINI;
					} else {
						attackLineIndex = LINE_ATTACK_INDEX_TSINGLE;
						lastevents[playerID] = LineClearEvent.TSPIN_SINGLE;
					}
				}
				// T-Spin 2 lines
				else if (lines == 2) {
					if (engine.tspinmini && engine.useAllSpinBonus) {
						attackLineIndex = LINE_ATTACK_INDEX_TMINI_D;
						lastevents[playerID] = LineClearEvent.TSPIN_DOUBLE_MINI;
					} else {
						attackLineIndex = LINE_ATTACK_INDEX_TDOUBLE;
						lastevents[playerID] = LineClearEvent.TSPIN_DOUBLE;
					}
				}
				// T-Spin 3 lines
				else if (lines >= 3) {
					attackLineIndex = LINE_ATTACK_INDEX_TTRIPLE;
					lastevents[playerID] = LineClearEvent.TSPIN_TRIPLE;
				}
			}
			// Normal style attack
			else {
				// Single
				switch (lines) {
				case 1:
					attackLineIndex = LINE_ATTACK_INDEX_SINGLE;
					lastevents[playerID] = LineClearEvent.SINGLE;
					break;
				case 2:
					attackLineIndex = LINE_ATTACK_INDEX_DOUBLE;
					lastevents[playerID] = LineClearEvent.DOUBLE;
					break;
				case 3:
					attackLineIndex = LINE_ATTACK_INDEX_TRIPLE;
					lastevents[playerID] = LineClearEvent.TRIPLE;
					break;
				default:
					if (lines >= 4) {
						attackLineIndex = LINE_ATTACK_INDEX_FOUR;
						lastevents[playerID] = LineClearEvent.FOUR;
					}
					break;
				}
			}

			if (engine.useAllSpinBonus) {
				pts[mainAttackCategory] += LINE_ATTACK_TABLE_ALLSPIN[attackLineIndex][attackNumPlayerIndex];
			} else {
				pts[mainAttackCategory] += LINE_ATTACK_TABLE[attackLineIndex][attackNumPlayerIndex];
			}

			// B2B
			if (engine.b2b) {
				lastb2b[playerID] = true;

				if (pts[mainAttackCategory] > 0) {
					if (attackLineIndex == LINE_ATTACK_INDEX_TTRIPLE && !engine.useAllSpinBonus) {
						pts[ATTACK_CATEGORY_B2B] += 2;
					} else {
						pts[ATTACK_CATEGORY_B2B] += 1;
					}
				}
			} else {
				lastb2b[playerID] = false;
			}

			// Combo
			if (engine.comboType != GameEngine.COMBO_TYPE_DISABLE) {
				int cmbindex = engine.combo - 1;
				if (cmbindex < 0) {
					cmbindex = 0;
				}
				if (cmbindex >= COMBO_ATTACK_TABLE[attackNumPlayerIndex].length) {
					cmbindex = COMBO_ATTACK_TABLE[attackNumPlayerIndex].length - 1;
				}
				pts[ATTACK_CATEGORY_COMBO] += COMBO_ATTACK_TABLE[attackNumPlayerIndex][cmbindex];
				lastcombo[playerID] = engine.combo;
			}

			// All clear (Bravo)
			if (lines >= 1 && engine.field.isEmpty() && netCurrentRoomInfo.bravo) {
				engine.playSE(Sounds.BRAVO);
				pts[ATTACK_CATEGORY_BRAVO] += 6;
			}

			// Gem block attack
			pts[ATTACK_CATEGORY_GEM] += engine.field.getHowManyGemClears();

			lastpiece[playerID] = engine.nowPieceObject.id;

			for (int i = 0; i < pts.length; i++) {
				pts[i] *= GARBAGE_DENOMINATOR;
			}
			if (netCurrentRoomInfo.useFractionalGarbage && !netvsIsPractice) {
				if (numAliveTeams >= 3) {
					for (int i = 0; i < pts.length; i++) {
						pts[i] = pts[i] / (numAliveTeams - 1);
					}
				}
			}

			// Attack lines count
			for (int i : pts) {
				garbageSent[playerID] += i;
			}

			// Garbage countering
			garbage[playerID] = getTotalGarbageLines();
			for (int i = 0; i < pts.length; i++) { // TODO: Establish specific priority of garbage cancellation.
				if (pts[i] > 0 && garbage[playerID] > 0 && netCurrentRoomInfo.counter) {
					while (!netCurrentRoomInfo.useFractionalGarbage && !garbageEntries.isEmpty() && pts[i] > 0
							|| netCurrentRoomInfo.useFractionalGarbage && !garbageEntries.isEmpty()
									&& pts[i] >= GARBAGE_DENOMINATOR) {
						GarbageEntry garbageEntry = garbageEntries.getFirst();
						garbageEntry.lines -= pts[i];

						if (garbageEntry.lines <= 0) {
							pts[i] = Math.abs(garbageEntry.lines);
							garbageEntries.removeFirst();
						} else {
							pts[i] = 0;
						}
					}
				}
			}

			// Send garbage lines
			if (!netvsIsPractice) {
				garbage[playerID] = getTotalGarbageLines();

				String stringPts = "";
				for (int i : pts) {
					stringPts += i + "\t";
				}

				if (targetID != -1 && !netvsIsAttackable(targetID)) {
					setNewTarget();
				}
				int targetSeatID = targetID == -1 ? -1 : netvsPlayerSeatID[targetID];

				netLobby.netPlayerClient.send(NetCmd.GAME, "attack", stringPts, lastevents[playerID], lastb2b[playerID],
						lastcombo[playerID], garbage[playerID], lastpiece[playerID], targetSeatID);
			}
		}

		// Garbage lines appear
		if ((lines == 0 || !netCurrentRoomInfo.rensaBlock) && getTotalGarbageLines() >= GARBAGE_DENOMINATOR
				&& !netvsIsPractice) {
			engine.playSE(Sounds.GARBAGE);

			int smallGarbageCount = 0;
			int hole = lastHole;
			int newHole;
			if (hole == -1) {
				hole = engine.random.nextInt(engine.field.getWidth());
			}

			int finalGarbagePercent = netCurrentRoomInfo.garbagePercent;
			if (netCurrentRoomInfo.divideChangeRateByPlayers) {
				finalGarbagePercent /= netvsGetNumberOfTeamsAlive() - 1;
			}

			// Make regular garbage lines appear
			while (!garbageEntries.isEmpty()) {
				GarbageEntry garbageEntry = garbageEntries.poll();
				smallGarbageCount += garbageEntry.lines % GARBAGE_DENOMINATOR;

				if (garbageEntry.lines / GARBAGE_DENOMINATOR > 0) {
					int seatFrom = netvsPlayerSeatID[garbageEntry.playerID];
					int garbageColor = seatFrom < 0 ? Colors.BLOCK_COLOR_GRAY : NETVS_PLAYER_COLOR_BLOCK[seatFrom];
					netvsLastAttackerUID = garbageEntry.uid;
					if (netCurrentRoomInfo.garbageChangePerAttack) {
						if (engine.random.nextInt(100) < finalGarbagePercent) {
							newHole = engine.random.nextInt(engine.field.getWidth() - 1);
							if (newHole >= hole) {
								newHole++;
							}
							hole = newHole;
						}
						engine.field.addSingleHoleGarbage(hole, garbageColor, engine.getSkin(),
								garbageEntry.lines / GARBAGE_DENOMINATOR);
					} else {
						for (int i = garbageEntry.lines / GARBAGE_DENOMINATOR; i > 0; i--) {
							if (engine.random.nextInt(100) < finalGarbagePercent) {
								newHole = engine.random.nextInt(engine.field.getWidth() - 1);
								if (newHole >= hole) {
									newHole++;
								}
								hole = newHole;
							}

							engine.field.addSingleHoleGarbage(hole, garbageColor, engine.getSkin(), 1);
						}
					}
				}
			}

			// Make small garbage lines appear
			if (smallGarbageCount > 0) {
				if (smallGarbageCount / GARBAGE_DENOMINATOR > 0) {
					netvsLastAttackerUID = -1;

					if (netCurrentRoomInfo.garbageChangePerAttack) {
						if (engine.random.nextInt(100) < finalGarbagePercent) {
							newHole = engine.random.nextInt(engine.field.getWidth() - 1);
							if (newHole >= hole) {
								newHole++;
							}
							hole = newHole;
						}
						engine.field.addSingleHoleGarbage(hole, Colors.BLOCK_COLOR_GRAY, engine.getSkin(),
								smallGarbageCount / GARBAGE_DENOMINATOR);
					} else {
						for (int i = smallGarbageCount / GARBAGE_DENOMINATOR; i > 0; i--) {
							if (engine.random.nextInt(100) < finalGarbagePercent) {
								newHole = engine.random.nextInt(engine.field.getWidth() - 1);
								if (newHole >= hole) {
									newHole++;
								}
								hole = newHole;
							}

							engine.field.addSingleHoleGarbage(hole, Colors.BLOCK_COLOR_GRAY, engine.getSkin(), 1);
						}
					}
				}

				if (smallGarbageCount % GARBAGE_DENOMINATOR > 0) {
					GarbageEntry smallGarbageEntry = new GarbageEntry(smallGarbageCount % GARBAGE_DENOMINATOR, -1);
					garbageEntries.add(smallGarbageEntry);
				}
			}

			lastHole = hole;
			garbage[playerID] = getTotalGarbageLines();
		}

		// HURRY UP!
		if (netCurrentRoomInfo.hurryupSeconds >= 0 && engine.timerActive && !netvsIsPractice) {
			if (hurryupStarted) {
				hurryupCount++;

				if (hurryupCount % netCurrentRoomInfo.hurryupInterval == 0) {
					engine.field.addHurryupFloor(1, engine.getSkin());
				}
			} else {
				hurryupCount = netCurrentRoomInfo.hurryupInterval - 1;
			}
		}
	}

	/*
	 * Executed at the end of each frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		super.onLast(engine, playerID);

		scgettime[playerID]++;
		if (playerID == 0 && hurryupShowFrames > 0) {
			hurryupShowFrames--;
		}

		// HURRY UP!
		if (playerID == 0 && engine.timerActive && netCurrentRoomInfo != null && netCurrentRoomInfo.hurryupSeconds >= 0
				&& netvsPlayTimer == netCurrentRoomInfo.hurryupSeconds * 60 && !hurryupStarted) {
			if (!netvsIsWatch() && !netvsIsPractice) {
				netLobby.netPlayerClient.send(NetCmd.GAME, "hurryup");
				owner.renderer.playSE(Sounds.HURRY_UP);
			}
			hurryupStarted = true;
			hurryupShowFrames = 60 * 5;
		}

		// Garbage meter
		int tempGarbage = garbage[playerID] / GARBAGE_DENOMINATOR;
		float tempGarbageF = (float) garbage[playerID] / GARBAGE_DENOMINATOR;
		int newMeterValue = (int) (tempGarbageF * owner.renderer.getBlockGraphicsHeight(engine, playerID));
		if (playerID == 0 && !netvsIsWatch()) {
			if (newMeterValue > engine.meterValue) {
				engine.meterValue += owner.renderer.getBlockGraphicsHeight(engine, playerID) / 2;
				if (engine.meterValue > newMeterValue) {
					engine.meterValue = newMeterValue;
				}
			} else if (newMeterValue < engine.meterValue) {
				engine.meterValue--;
			}
		} else {
			engine.meterValue = newMeterValue;
		}
		if (tempGarbage >= 4) {
			engine.meterColor = Colors.METER_COLOR_RED;
		} else if (tempGarbage >= 3) {
			engine.meterColor = Colors.METER_COLOR_ORANGE;
		} else if (tempGarbage >= 1) {
			engine.meterColor = Colors.METER_COLOR_YELLOW;
		} else {
			engine.meterColor = Colors.METER_COLOR_GREEN;
		}

		// APL & APM
		if (playerID == 0 && engine.gameActive && engine.timerActive && !netvsIsWatch()) {
			float tempGarbageSent = (float) garbageSent[playerID] / GARBAGE_DENOMINATOR;
			playerAPM[0] = tempGarbageSent * 3600 / engine.statistics.time;

			if (engine.statistics.lines > 0) {
				playerAPL[0] = tempGarbageSent / engine.statistics.lines;
			} else {
				playerAPL[0] = 0f;
			}
		}

		// Target
		if (playerID == 0 && !netvsIsWatch() && netvsPlayTimerActive && engine.gameActive && engine.timerActive
				&& getNumberOfPossibleTargets() >= 1 && netCurrentRoomInfo != null && netCurrentRoomInfo.isTarget) {
			targetTimer++;

			if (targetTimer >= netCurrentRoomInfo.targetTimer || !netvsIsAttackable(targetID)) {
				targetTimer = 0;
				setNewTarget();
			}
		}
	}

	/*
	 * Drawing processing at the end of every frame
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		super.renderLast(engine, playerID);

		int x = renderer.getFieldDisplayPositionX(engine, playerID);
		int y = renderer.getFieldDisplayPositionY(engine, playerID);

		if (netvsPlayerExist[playerID] && engine.isVisible) {
			// Garbage Count
			if (garbage[playerID] > 0 && netCurrentRoomInfo.useFractionalGarbage
					&& engine.stat != GameEngine.Status.RESULT) {
				String strTempGarbage;

				int fontColor = Colors.FONT_WHITE;
				if (garbage[playerID] >= GARBAGE_DENOMINATOR) {
					fontColor = Colors.FONT_YELLOW;
				}
				if (garbage[playerID] >= GARBAGE_DENOMINATOR * 3) {
					fontColor = Colors.FONT_ORANGE;
				}
				if (garbage[playerID] >= GARBAGE_DENOMINATOR * 4) {
					fontColor = Colors.FONT_RED;
				}

				if (engine.displaySize != DisplaySize.SMALL) {
					strTempGarbage = String.format(Locale.US, "%5.2f", (float) garbage[playerID] / GARBAGE_DENOMINATOR);
					renderer.drawDirectFont(engine, playerID, x + 96, y + 372, strTempGarbage, fontColor, 1.0f);
				} else {
					strTempGarbage = String.format(Locale.US, "%4.1f", (float) garbage[playerID] / GARBAGE_DENOMINATOR);
					renderer.drawDirectFont(engine, playerID, x + 64, y + 168, strTempGarbage, fontColor, 0.5f);
				}
			}

			// Target
			if (playerID == targetID && netCurrentRoomInfo != null && netCurrentRoomInfo.isTarget
					&& netvsNumAlivePlayers >= 3 && netvsIsGameActive && netvsIsAttackable(playerID)
					&& !netvsIsWatch()) {
				int fontcolor = Colors.FONT_GREEN;
				if (targetTimer >= netCurrentRoomInfo.targetTimer - 20 && targetTimer % 2 == 0) {
					fontcolor = Colors.FONT_WHITE;
				}

				if (engine.displaySize != DisplaySize.SMALL) {
					renderer.drawMenuFont(engine, playerID, 2, 12, "TARGET", fontcolor);
				} else {
					renderer.drawDirectFont(engine, playerID, x + 4 + 16, y + 80, "TARGET", fontcolor, 0.5f);
				}
			}
		}

		// Practice mode
		if (playerID == 0 && netvsIsPractice && netvsIsPracticeExitAllowed && engine.stat != GameEngine.Status.RESULT) {
			if (lastevents[playerID] == LineClearEvent.NONE || scgettime[playerID] >= 120) {
				renderer.drawMenuFont(engine, 0, 0, 21,
						"F(" + renderer.getKeyNameByButtonID(engine, Controller.BUTTON_F) + " KEY):\n END GAME",
						Colors.FONT_PURPLE);
			}
		}

		// Hurry Up
		if (netCurrentRoomInfo != null && playerID == 0) {
			if (netCurrentRoomInfo.hurryupSeconds >= 0 && hurryupShowFrames > 0 && !netvsIsPractice && hurryupStarted) {
				renderer.drawDirectFont(engine, 0, 256 - 8, 32, "HURRY UP!", hurryupShowFrames % 2 == 0);
			}
		}

		// Bottom message
		if (netvsPlayerExist[playerID] && engine.isVisible) {
			// K.O.
			if (playerKObyYou[playerID]) {
				if (engine.displaySize != DisplaySize.SMALL) {
					renderer.drawMenuFont(engine, playerID, 3, 21, "K.O.", Colors.FONT_PINK);
				} else {
					renderer.drawDirectFont(engine, playerID, x + 4 + 24, y + 168, "K.O.", Colors.FONT_PINK, 0.5f);
				}
			}
			// Line clear event
			else if (lastevents[playerID] != LineClearEvent.NONE && scgettime[playerID] < 120) {
				String piece = Piece.getPieceName(lastpiece[playerID]);
				int b2bColor = lastb2b[playerID] ? Colors.FONT_RED : Colors.FONT_ORANGE;
				if (engine.displaySize != DisplaySize.SMALL) {
					switch (lastevents[playerID]) {
					case SINGLE -> renderer.drawMenuFont(engine, playerID, 2, 21, "SINGLE", Colors.FONT_DARKBLUE);
					case DOUBLE -> renderer.drawMenuFont(engine, playerID, 2, 21, "DOUBLE", Colors.FONT_BLUE);
					case TRIPLE -> renderer.drawMenuFont(engine, playerID, 2, 21, "TRIPLE", Colors.FONT_GREEN);
					case FOUR -> renderer.drawMenuFont(engine, playerID, 3, 21, "FOUR", b2bColor);
					case TSPIN_SINGLE_MINI ->
						renderer.drawMenuFont(engine, playerID, 1, 21, piece + "-MINI-S", b2bColor);
					case TSPIN_SINGLE -> renderer.drawMenuFont(engine, playerID, 1, 21, piece + "-SINGLE", b2bColor);
					case TSPIN_DOUBLE_MINI ->
						renderer.drawMenuFont(engine, playerID, 1, 21, piece + "-MINI-D", b2bColor);
					case TSPIN_DOUBLE -> renderer.drawMenuFont(engine, playerID, 1, 21, piece + "-DOUBLE", b2bColor);
					case TSPIN_TRIPLE -> renderer.drawMenuFont(engine, playerID, 1, 21, piece + "-TRIPLE", b2bColor);
					case TSPIN_EZ -> renderer.drawMenuFont(engine, playerID, 3, 21, "EZ-" + piece, b2bColor);
					default -> {
					}
					}

					if (lastcombo[playerID] >= 2) {
						renderer.drawMenuFont(engine, playerID, 2, 22, lastcombo[playerID] - 1 + "COMBO",
								Colors.FONT_CYAN);
					}
				} else {
					int x2 = 8;
					if (netCurrentRoomInfo.useFractionalGarbage && garbage[playerID] > 0) {
						x2 = 0;
					}
					int y2 = y + 168;
					switch (lastevents[playerID]) {
					case SINGLE ->
						renderer.drawDirectFont(engine, playerID, x + 4 + 16, y2, "SINGLE", Colors.FONT_DARKBLUE, 0.5f);
					case DOUBLE ->
						renderer.drawDirectFont(engine, playerID, x + 4 + 16, y2, "DOUBLE", Colors.FONT_BLUE, 0.5f);
					case TRIPLE ->
						renderer.drawDirectFont(engine, playerID, x + 4 + 16, y2, "TRIPLE", Colors.FONT_GREEN, 0.5f);
					case FOUR -> renderer.drawDirectFont(engine, playerID, x + 4 + 24, y2, "FOUR", b2bColor, 0.5f);
					case TSPIN_SINGLE_MINI ->
						renderer.drawDirectFont(engine, playerID, x + 4 + x2, y2, piece + "-MINI-S", b2bColor, 0.5f);
					case TSPIN_SINGLE ->
						renderer.drawDirectFont(engine, playerID, x + 4 + x2, y2, piece + "-SINGLE", b2bColor, 0.5f);
					case TSPIN_DOUBLE_MINI ->
						renderer.drawDirectFont(engine, playerID, x + 4 + x2, y2, piece + "-MINI-D", b2bColor, 0.5f);
					case TSPIN_DOUBLE ->
						renderer.drawDirectFont(engine, playerID, x + 4 + x2, y2, piece + "-DOUBLE", b2bColor, 0.5f);
					case TSPIN_TRIPLE ->
						renderer.drawDirectFont(engine, playerID, x + 4 + x2, y2, piece + "-TRIPLE", b2bColor, 0.5f);
					case TSPIN_EZ ->
						renderer.drawDirectFont(engine, playerID, x + 4 + 24, y2, "EZ-" + piece, b2bColor, 0.5f);
					default -> {
					}
					}

					if (lastcombo[playerID] >= 2) {
						renderer.drawDirectFont(engine, playerID, x + 4 + 16, y + 176,
								lastcombo[playerID] - 1 + "COMBO", Colors.FONT_CYAN, 0.5f);
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
					renderer.drawMenuFont(engine, playerID, 0, y2, strTemp, Colors.FONT_WHITE);
				} else {
					renderer.drawDirectFont(engine, playerID, x + 4, y + 168, strTemp, Colors.FONT_WHITE, 0.5f);
				}
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
				"ATTACK", String.format("%10g", (float) garbageSent[playerID] / GARBAGE_DENOMINATOR),
				"LINE", String.format("%10d", engine.statistics.lines),
				"PIECE", String.format("%10d", engine.statistics.totalPieceLocked),
				"ATK/LINE",	String.format("%10g", playerAPL[playerID]),
				"ATTACK/MIN", String.format("%10g", playerAPM[playerID]),
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
			netLobby.netPlayerClient.send(NetCmd.GAME, "stats", garbage[engine.playerID]);
		}
	}

	/*
	 * Receive stats
	 */
	@Override
	protected void netRecvStats(GameEngine engine, NetMessage message) {
		if (message.length() > 3) {
			garbage[engine.playerID] = message.asInt(3);
		}
	}

	/*
	 * Send end-of-game stats
	 */
	@Override
	protected void netSendEndGameStats(GameEngine engine) {
		int playerID = engine.playerID;
		String stats = "";
		stats += netvsPlayerPlace[playerID] + "\t";
		stats += (float) garbageSent[playerID] / GARBAGE_DENOMINATOR + "\t";
		stats += playerAPL[playerID] + "\t";
		stats += playerAPM[playerID] + "\t";
		stats += engine.statistics.lines + "\t";
		stats += engine.statistics.lpm + "\t";
		stats += engine.statistics.totalPieceLocked + "\t";
		stats += engine.statistics.pps + "\t";
		stats += netvsPlayTimer + "\t";
		stats += currentKO + "\t";
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

			float tempGarbageSend = message.asFloat(4);
			garbageSent[playerID] = (int) (tempGarbageSend * GARBAGE_DENOMINATOR);

			playerAPL[playerID] = message.asFloat(5);
			playerAPM[playerID] = message.asFloat(6);
			engine.statistics.lines = message.asInt(7);
			engine.statistics.lpm = message.asFloat(8);
			engine.statistics.totalPieceLocked = message.asInt(9);
			engine.statistics.pps = message.asFloat(10);
			engine.statistics.time = message.asInt(11);
			netvsPlayerResultReceived[playerID] = true;
		}
	}

	/*
	 * Message received
	 */
	@Override
	public void netlobbyOnMessage(NetMessage message) {
		super.netlobbyOnMessage(message);

		switch (message.command()) {
		// Dead
		case DEAD -> {
			int seatID = message.asInt(2);
			int playerID = netvsGetPlayerIDbySeatID(seatID);
			int koUID = -1;
			if (message.length() > 4) {
				koUID = message.asInt(4);
			}

			// Increase KO count
			if (koUID == netLobby.netPlayerClient.getPlayerUID()) {
				playerKObyYou[playerID] = true;
				currentKO++;
			}
		}
		// Game messages
		case GAME -> {
			int uid = message.asInt(0);
			int seatID = message.asInt(1);
			int playerID = netvsGetPlayerIDbySeatID(seatID);
			// GameEngine engine = owner.engine[playerID];
			String gameCmd = message.text(2);
			// Attack
			if ("attack".equals(gameCmd)) {
				int[] pts = new int[ATTACK_CATEGORIES];
				int sumPts = 0;

				for (int i = 0; i < ATTACK_CATEGORIES; i++) {
					pts[i] = message.asInt(i + 3);
					sumPts += pts[i];
				}

				lastevents[playerID] = LineClearEvent.values()[message.asInt(ATTACK_CATEGORIES + 4)];
				lastb2b[playerID] = message.asBool(ATTACK_CATEGORIES + 5);
				lastcombo[playerID] = message.asInt(ATTACK_CATEGORIES + 6);
				garbage[playerID] = message.asInt(ATTACK_CATEGORIES + 7);
				lastpiece[playerID] = message.asInt(ATTACK_CATEGORIES + 8);
				scgettime[playerID] = 0;
				int targetSeatID = message.asInt(ATTACK_CATEGORIES + 9);
				if (!netvsIsWatch() && owner.engines[0].timerActive && sumPts > 0 && !netvsIsPractice
						&& !netvsIsNewcomer
						&& (targetSeatID == -1 || netvsPlayerSeatID[0] == targetSeatID || !netCurrentRoomInfo.isTarget)
						&& netvsIsAttackable(playerID)) {
					int secondAdd = 0; // TODO: Allow for chunking of attack types other than b2b.
					if (netCurrentRoomInfo.b2bChunk) {
						secondAdd = pts[ATTACK_CATEGORY_B2B];
					}

					GarbageEntry garbageEntry = new GarbageEntry(sumPts - secondAdd, playerID, uid);
					garbageEntries.add(garbageEntry);

					if (secondAdd > 0) {
						garbageEntry = new GarbageEntry(secondAdd, playerID, uid);
						garbageEntries.add(garbageEntry);
					}

					garbage[0] = getTotalGarbageLines();
					if (garbage[0] >= 4 * GARBAGE_DENOMINATOR) {
						owner.engines[0].playSE(Sounds.DANGER);
					}
					netSendStats(owner.engines[0]);
				}
			}
			// HurryUp
			if ("hurryup".equals(gameCmd)) {
				if (!hurryupStarted && netCurrentRoomInfo != null && netCurrentRoomInfo.hurryupSeconds > 0) {
					if (!netvsIsWatch() && !netvsIsPractice && owner.engines[0].timerActive) {
						renderer.playSE(Sounds.HURRY_UP);
					}
					hurryupStarted = true;
					hurryupShowFrames = 60 * 5;
				}
			}
		}
		default -> {
			// ignore
		}
		}
	}

	/**
	 * Garbage data
	 */
	private class GarbageEntry {
		/** Number of garbage lines */
		public int lines = 0;

		/** Sender's playerID */
		public int playerID = 0;

		/** Sender's UID */
		public int uid = 0;

		/**
		 * Constructor
		 *
		 * @param g Lines
		 */
		@SuppressWarnings("unused")
		public GarbageEntry(int g) {
			lines = g;
		}

		/**
		 * Constructor
		 *
		 * @param g Lines
		 * @param p Sender's playerID
		 */
		public GarbageEntry(int g, int p) {
			lines = g;
			playerID = p;
		}

		/**
		 * Constructor
		 *
		 * @param g Lines
		 * @param p Sender's playerID
		 * @param s Sender's UID
		 */
		public GarbageEntry(int g, int p, int s) {
			lines = g;
			playerID = p;
			uid = s;
		}
	}
}

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

import mu.nu.nullpo.game.component.BGMusicStatus;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.Statistics.Statistic;
import mu.nu.nullpo.game.net.NetCmd;
import mu.nu.nullpo.game.net.NetMessage;
import mu.nu.nullpo.game.net.NetUtil;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.Colors;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import mu.nu.nullpo.util.Sounds;

/**
 * EXTREME Mode
 */
public class ExtremeMode extends NetDummyMode {
	/** Current version */
	private static final int CURRENT_VERSION = 1;

	/** Ending time */
	protected static final int ROLLTIMELIMIT = 2968;

	/** ARE table */
	private static final int[] tableARE = { 25, 20, 15, 10, 10, 10, 8, 6, 5, 4, 4, 3, 2, 2, 1, 1, 0, 0, 0, 0 };

	/** ARE after line clear table */
	private static final int[] tableARELine = { 25, 20, 15, 10, 6, 4, 4, 2, 2, 2, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0 };

	/** Line clear time table */
	private static final int[] tableLineDelay = { 40, 20, 10, 5, 6, 4, 4, 2, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0 };

	/** Lock delay table */
	private static final int[] tableLockDelay = { 30, 25, 25, 20, 18, 18, 17, 16, 15, 15, 14, 14, 14, 13, 13, 13, 13,
			12, 11, 11 };

	/** DAS table */
	private static final int[] tableDAS = { 16, 10, 10, 8, 8, 6, 6, 6, 6, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3 };

	/** Line counts when BGM changes occur */
	private static final int[] tableBGMChange = { 50, 100, 150, -1 };

	/** Number of ranking types */
	private static final int RANKING_TYPE = 2;

	/** Most recent increase in score */
	protected int lastscore;

	/**
	 * Elapsed time from last line clear (lastscore is displayed to screen until
	 * this reaches to 120)
	 */
	protected int scgettime;

	/** True if most recent scoring event is a B2B */
	private boolean lastb2b;

	/** Combo count for most recent scoring event */
	private int lastcombo;

	/** Piece ID for most recent scoring event */
	private int lastpiece;

	/** Ending time */
	private int rolltime;

	/** Current BGM */
	private int bgmlv;

	/** Level at start time */
	private int startlevel;

	/** Flag for types of T-Spins allowed (0=none, 1=normal, 2=all spin) */
	private int tspinEnableType;

	/** Old flag for allowing T-Spins */
	private boolean enableTSpin;

	/** Flag for enabling wallkick T-Spins */
	private boolean enableTSpinKick;

	/** Spin check type (4Point or Immobile) */
	private int spinCheckType;

	/** Immobile EZ spin */
	private boolean tspinEnableEZ;

	/** Flag for enabling B2B */
	private boolean enableB2B;

	/** Flag for enabling combos */
	private boolean enableCombo;

	/** Endless flag */
	private boolean endless;

	/** Big */
	private boolean big;

	/** Version */
	private int version;

	/** Current round's ranking rank */
	private int rankingRank;

	/** Rankings' scores */
	private int[][] rankingScore;

	/** Rankings' line counts */
	private int[][] rankingLines;

	/** Rankings' times */
	private int[][] rankingTime;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "EXTREME";
	}

	/*
	 * Initialization
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		renderer = engine.owner.renderer;
		lastscore = 0;
		scgettime = 0;
		lastevent = LineClearEvent.NONE;
		lastb2b = false;
		lastcombo = 0;
		lastpiece = 0;
		bgmlv = 0;
		rolltime = 0;

		rankingRank = -1;
		rankingScore = new int[RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

		netPlayerInit(engine, playerID);

		if (!owner.replayMode) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.staffrollEnable = true;
		engine.staffrollNoDeath = true;
		engine.staffrollEnableStatistics = true;

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = Colors.FRAME_COLOR_RED;
	}

	/**
	 * Set the gravity rate
	 *
	 * @param engine GameEngine
	 */
	public void setSpeed(GameEngine engine) {
		int lv = engine.statistics.level;

		if (lv < 0) {
			lv = 0;
		}
		if (lv >= tableARE.length) {
			lv = tableARE.length - 1;
		}

		engine.speed.gravity = -1;
		engine.speed.are = tableARE[lv];
		engine.speed.areLine = tableARELine[lv];
		engine.speed.lineDelay = tableLineDelay[lv];
		engine.speed.lockDelay = tableLockDelay[lv];
		engine.speed.das = tableDAS[lv];
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// NET: Net Ranking
		if (netIsNetRankingDisplayMode) {
			netOnUpdateNetPlayRanking(engine, netGetGoalType());
		}
		// Menu
		else if (!engine.owner.replayMode) {
			// Configuration changes
			int change = updateCursor(engine, 8);

			if (change != 0) {
				engine.playSE(Sounds.CHANGE);

				switch (menuCursor) {
				case 0:
					startlevel += change;
					if (startlevel < 0) {
						startlevel = 19;
					}
					if (startlevel > 19) {
						startlevel = 0;
					}
					engine.owner.backgroundStatus.bg = startlevel;
					break;
				case 1:
					tspinEnableType += change;
					if (tspinEnableType < 0) {
						tspinEnableType = 2;
					}
					if (tspinEnableType > 2) {
						tspinEnableType = 0;
					}
					break;
				case 2:
					enableTSpinKick = !enableTSpinKick;
					break;
				case 3:
					spinCheckType += change;
					if (spinCheckType < 0) {
						spinCheckType = 1;
					}
					if (spinCheckType > 1) {
						spinCheckType = 0;
					}
					break;
				case 4:
					tspinEnableEZ = !tspinEnableEZ;
					break;
				case 5:
					enableB2B = !enableB2B;
					break;
				case 6:
					enableCombo = !enableCombo;
					break;
				case 7:
					endless = !endless;
					break;
				case 8:
					big = !big;
					break;
				}

				// NET: Signal options change
				if (netIsNetPlay && netNumSpectators > 0) {
					netSendOptions(engine);
				}
			}

			// Confirm
			if (engine.ctrl.isPush(Controller.BUTTON_A) && menuTime >= 5) {
				engine.playSE(Sounds.DECIDE);
				saveSetting(owner.modeConfig);
				GeneralUtil.saveModeConfig(owner.modeConfig);

				// NET: Signal start of the game
				if (netIsNetPlay) {
					netLobby.netPlayerClient.send(NetCmd.START_1P);
				}

				return false;
			}

			// Cancel
			if (engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
				engine.quitflag = true;
			}

			// NET: Netplay Ranking
			if (engine.ctrl.isPush(Controller.BUTTON_D) && netIsNetPlay && startlevel == 0 && !big
					&& engine.ai == null) {
				netEnterNetPlayRankingScreen(engine, playerID, netGetGoalType());
			}

			menuTime++;
		}
		// Replay
		else {
			menuTime++;
			menuCursor = -1;

			if (menuTime >= 60) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Render the settings screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		if (netIsNetRankingDisplayMode) {
			// NET: Netplay Ranking
			netOnRenderNetPlayRanking(engine, playerID, renderer);
		} else {
			String strTSpinEnable = "";
			if (version >= 1) {
				if (tspinEnableType == 0) {
					strTSpinEnable = "OFF";
				}
				if (tspinEnableType == 1) {
					strTSpinEnable = "T-ONLY";
				}
				if (tspinEnableType == 2) {
					strTSpinEnable = "ALL";
				}
			} else {
				strTSpinEnable = GeneralUtil.getONorOFF(enableTSpin);
			}
			drawMenu(engine, playerID, 0, Colors.FONT_BLUE, 0, "LEVEL", String.valueOf(startlevel + 1), "SPIN BONUS",
					strTSpinEnable, "EZ SPIN", GeneralUtil.getONorOFF(enableTSpinKick), "SPIN TYPE",
					spinCheckType == 0 ? "4POINT" : "IMMOBILE", "EZIMMOBILE", GeneralUtil.getONorOFF(tspinEnableEZ),
					"B2B", GeneralUtil.getONorOFF(enableB2B), "COMBO", GeneralUtil.getONorOFF(enableCombo), "ENDLESS",
					GeneralUtil.getONorOFF(endless), "BIG", GeneralUtil.getONorOFF(big));
		}
	}

	/*
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel;
		engine.statistics.levelDispAdd = 1;
		engine.b2bEnable = enableB2B;
		if (enableCombo) {
			engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
		} else {
			engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
		}
		engine.big = big;

		if (netIsWatch) {
			owner.bgmStatus.bgm = BGMusicStatus.BGM_NOTHING;
		} else {
			owner.bgmStatus.bgm = bgmlv + 2;
		}

		if (version >= 1) {
			engine.tspinAllowKick = enableTSpinKick;
			if (tspinEnableType == 0) {
				engine.tspinEnable = false;
			} else if (tspinEnableType == 1) {
				engine.tspinEnable = true;
			} else {
				engine.tspinEnable = true;
				engine.useAllSpinBonus = true;
			}
		} else {
			engine.tspinEnable = enableTSpin;
		}

		engine.spinCheckType = spinCheckType;
		engine.tspinEnableEZ = tspinEnableEZ;

		setSpeed(engine);
	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if (owner.menuOnly) {
			return;
		}

		renderer.drawScoreFont(engine, playerID, 0, 0, "EXTREME", Colors.FONT_RED);

		if (engine.stat == GameEngine.Status.SETTING || engine.stat == GameEngine.Status.RESULT && !owner.replayMode) {
			if (!owner.replayMode && !big && engine.ai == null) {
				float scale = renderer.getNextDisplayType() == 2 ? 0.5f : 1.0f;
				int topY = renderer.getNextDisplayType() == 2 ? 6 : 4;
				renderer.drawScoreFont(engine, playerID, 3, topY - 1, "SCORE  LINE TIME", Colors.FONT_BLUE, scale);

				for (int i = 0; i < RANKING_MAX; i++) {
					int endlessIndex = 0;
					if (endless) {
						endlessIndex = 1;
					}

					renderer.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1),
							Colors.FONT_YELLOW, scale);
					renderer.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScore[endlessIndex][i]),
							i == rankingRank, scale);
					renderer.drawScoreFont(engine, playerID, 10, topY + i,
							String.valueOf(rankingLines[endlessIndex][i]), i == rankingRank, scale);
					renderer.drawScoreFont(engine, playerID, 15, topY + i,
							GeneralUtil.getTime(rankingTime[endlessIndex][i]), i == rankingRank, scale);
				}
			}
		} else {
			renderer.drawScoreFont(engine, playerID, 0, 2, "SCORE", Colors.FONT_BLUE);
			String strScore;
			if (lastscore == 0 || scgettime >= 120) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = String.valueOf(engine.statistics.score) + "(+" + lastscore + ")";
			}
			renderer.drawScoreFont(engine, playerID, 0, 3, strScore);

			renderer.drawScoreFont(engine, playerID, 0, 5, "LINE", Colors.FONT_BLUE);
			if (engine.statistics.level < 19 || !endless && engine.ending == 0) {
				renderer.drawScoreFont(engine, playerID, 0, 6,
						engine.statistics.lines + "/" + (engine.statistics.level + 1) * 10);
			} else {
				renderer.drawScoreFont(engine, playerID, 0, 6, engine.statistics.lines + "");
			}

			renderer.drawScoreFont(engine, playerID, 0, 8, "LEVEL", Colors.FONT_BLUE);
			renderer.drawScoreFont(engine, playerID, 0, 9, String.valueOf(engine.statistics.level + 1));

			renderer.drawScoreFont(engine, playerID, 0, 11, "TIME", Colors.FONT_BLUE);
			renderer.drawScoreFont(engine, playerID, 0, 12, GeneralUtil.getTime(engine.statistics.time));

			if (engine.gameActive && engine.ending == 2) {
				int remainRollTime = ROLLTIMELIMIT - rolltime;
				if (remainRollTime < 0) {
					remainRollTime = 0;
				}

				renderer.drawScoreFont(engine, playerID, 0, 14, "ROLL TIME", Colors.FONT_BLUE);
				renderer.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(remainRollTime),
						remainRollTime > 0 && remainRollTime < 10 * 60);
			}

			if (lastevent != LineClearEvent.NONE && scgettime < 120) {
				String strPieceName = Piece.getPieceName(lastpiece);

				switch (lastevent) {
				case LineClearEvent.SINGLE:
					renderer.drawMenuFont(engine, playerID, 2, 21, "SINGLE", Colors.FONT_DARKBLUE);
					break;
				case LineClearEvent.DOUBLE:
					renderer.drawMenuFont(engine, playerID, 2, 21, "DOUBLE", Colors.FONT_BLUE);
					break;
				case LineClearEvent.TRIPLE:
					renderer.drawMenuFont(engine, playerID, 2, 21, "TRIPLE", Colors.FONT_GREEN);
					break;
				case LineClearEvent.FOUR:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 3, 21, "FOUR", Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 3, 21, "FOUR", Colors.FONT_ORANGE);
					}
					break;
				case LineClearEvent.TSPIN_ZERO_MINI:
					renderer.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", Colors.FONT_PURPLE);
					break;
				case LineClearEvent.TSPIN_ZERO:
					renderer.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", Colors.FONT_PINK);
					break;
				case LineClearEvent.TSPIN_SINGLE_MINI:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", Colors.FONT_ORANGE);
					}
					break;
				case LineClearEvent.TSPIN_SINGLE:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", Colors.FONT_ORANGE);
					}
					break;
				case LineClearEvent.TSPIN_DOUBLE_MINI:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", Colors.FONT_ORANGE);
					}
					break;
				case LineClearEvent.TSPIN_DOUBLE:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", Colors.FONT_ORANGE);
					}
					break;
				case LineClearEvent.TSPIN_TRIPLE:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", Colors.FONT_ORANGE);
					}
					break;
				case LineClearEvent.TSPIN_EZ:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, Colors.FONT_ORANGE);
					}
					break;
				default:
					break;
				}

				if (lastcombo >= 2 && lastevent != LineClearEvent.TSPIN_ZERO_MINI
						&& lastevent != LineClearEvent.TSPIN_ZERO) {
					renderer.drawMenuFont(engine, playerID, 2, 22, lastcombo - 1 + "COMBO", Colors.FONT_CYAN);
				}
			}
		}

		// NET: Number of spectators
		netDrawSpectatorsCount(engine, 0, 18);
		// NET: All number of players
		if (playerID == getPlayers() - 1) {
			netDrawAllPlayersCount(engine);
			netDrawGameRate(engine);
		}
		// NET: Player name (It may also appear in offline replay)
		netDrawPlayerName(engine);
	}

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		// Ending
		if (engine.gameActive && engine.ending == 2) {
			rolltime++;

			// Time meter
			int remainRollTime = ROLLTIMELIMIT - rolltime;
			if (remainRollTime < 0) {
				remainRollTime = 0;
			}
			engine.meterValue = remainRollTime * renderer.getMeterMax(engine) / ROLLTIMELIMIT;
			engine.meterColor = Colors.METER_COLOR_GREEN;
			if (remainRollTime <= 30 * 60) {
				engine.meterColor = Colors.METER_COLOR_YELLOW;
			}
			if (remainRollTime <= 20 * 60) {
				engine.meterColor = Colors.METER_COLOR_ORANGE;
			}
			if (remainRollTime <= 10 * 60) {
				engine.meterColor = Colors.METER_COLOR_RED;
			}

			// Finished
			if (rolltime >= ROLLTIMELIMIT) {
				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.Status.EXCELLENT;
			}
		}

		scgettime++;
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Line clear bonus
		int pts = 0;

		if (engine.tspin) {
			// T-Spin 0 lines
			if (lines == 0 && !engine.tspinez) {
				if (engine.tspinmini) {
					pts += 100 * (engine.statistics.level + 1);
					lastevent = LineClearEvent.TSPIN_ZERO_MINI;
				} else {
					pts += 400 * (engine.statistics.level + 1);
					lastevent = LineClearEvent.TSPIN_ZERO;
				}
			}
			// Immobile EZ Spin
			else if (engine.tspinez && lines > 0) {
				if (engine.b2b) {
					pts += 180 * (engine.statistics.level + 1);
				} else {
					pts += 120 * (engine.statistics.level + 1);
				}
				lastevent = LineClearEvent.TSPIN_EZ;
			}
			// T-Spin 1 line
			else if (lines == 1) {
				if (engine.tspinmini) {
					if (engine.b2b) {
						pts += 300 * (engine.statistics.level + 1);
					} else {
						pts += 200 * (engine.statistics.level + 1);
					}
					lastevent = LineClearEvent.TSPIN_SINGLE_MINI;
				} else {
					if (engine.b2b) {
						pts += 1200 * (engine.statistics.level + 1);
					} else {
						pts += 800 * (engine.statistics.level + 1);
					}
					lastevent = LineClearEvent.TSPIN_SINGLE;
				}
			}
			// T-Spin 2 lines
			else if (lines == 2) {
				if (engine.tspinmini && engine.useAllSpinBonus) {
					if (engine.b2b) {
						pts += 600 * (engine.statistics.level + 1);
					} else {
						pts += 400 * (engine.statistics.level + 1);
					}
					lastevent = LineClearEvent.TSPIN_DOUBLE_MINI;
				} else {
					if (engine.b2b) {
						pts += 1800 * (engine.statistics.level + 1);
					} else {
						pts += 1200 * (engine.statistics.level + 1);
					}
					lastevent = LineClearEvent.TSPIN_DOUBLE;
				}
			}
			// T-Spin 3 lines
			else if (lines >= 3) {
				if (engine.b2b) {
					pts += 2400 * (engine.statistics.level + 1);
				} else {
					pts += 1600 * (engine.statistics.level + 1);
				}
				lastevent = LineClearEvent.TSPIN_TRIPLE;
			}
		} else {
			switch (lines) {
			case 1:
				pts += 100 * (engine.statistics.level + 1); // 1Column
				lastevent = LineClearEvent.SINGLE;
				break;
			case 2:
				pts += 300 * (engine.statistics.level + 1); // 2Column
				lastevent = LineClearEvent.DOUBLE;
				break;
			case 3:
				pts += 500 * (engine.statistics.level + 1); // 3Column
				lastevent = LineClearEvent.TRIPLE;
				break;
			default:
				if (lines >= 4) {
					// 4 lines
					if (engine.b2b) {
						pts += 1200 * (engine.statistics.level + 1);
					} else {
						pts += 800 * (engine.statistics.level + 1);
					}
					lastevent = LineClearEvent.FOUR;
				}
				break;
			}
		}

		lastb2b = engine.b2b;

		// Combo
		if (enableCombo && engine.combo >= 1 && lines >= 1) {
			pts += (engine.combo - 1) * 50 * (engine.statistics.level + 1);
			lastcombo = engine.combo;
		}

		// All clear
		if (lines >= 1 && engine.field.isEmpty()) {
			engine.playSE(Sounds.BRAVO);
			pts += 1800 * (engine.statistics.level + 1);
		}

		// Add to score
		if (pts > 0) {
			lastpiece = engine.nowPieceObject.id;
			lastscore = pts;
			scgettime = 0;
			if (lines >= 1) {
				engine.statistics.scoreFromLineClear += pts;
			} else {
				engine.statistics.scoreFromOtherBonus += pts;
			}
			engine.statistics.score += pts;
		}

		if (engine.ending == 0) {
			// BGM fade-out effects and BGM changes
			if (tableBGMChange[bgmlv] != -1) {
				if (engine.statistics.lines >= tableBGMChange[bgmlv] - 5) {
					owner.bgmStatus.fadesw = true;
				}

				if (engine.statistics.lines >= tableBGMChange[bgmlv]) {
					bgmlv++;
					owner.bgmStatus.bgm = bgmlv + 2;
					owner.bgmStatus.fadesw = false;
				}
			}

			// Meter
			engine.meterValue = engine.statistics.lines % 10 * renderer.getMeterMax(engine) / 9;
			engine.meterColor = Colors.METER_COLOR_GREEN;
			if (engine.statistics.lines % 10 >= 4) {
				engine.meterColor = Colors.METER_COLOR_YELLOW;
			}
			if (engine.statistics.lines % 10 >= 6) {
				engine.meterColor = Colors.METER_COLOR_ORANGE;
			}
			if (engine.statistics.lines % 10 >= 8) {
				engine.meterColor = Colors.METER_COLOR_RED;
			}

			if (engine.statistics.lines >= 200 && !endless) {
				// Ending
				engine.playSE(Sounds.LEVEL_UP);
				engine.playSE(Sounds.ENDING_START);
				owner.bgmStatus.bgm = BGMusicStatus.BGM_ENDING1;
				owner.bgmStatus.fadesw = false;
				engine.bone = true;
				engine.ending = 2;
				engine.timerActive = false;
			} else if (engine.statistics.lines >= (engine.statistics.level + 1) * 10 && engine.statistics.level < 19) {
				// Level up
				engine.statistics.level++;

				owner.backgroundStatus.fadesw = true;
				owner.backgroundStatus.fadecount = 0;
				owner.backgroundStatus.fadebg = engine.statistics.level;

				setSpeed(engine);
				engine.playSE(Sounds.LEVEL_UP);
			}
		}
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		drawResultStats(engine, playerID, 0, Colors.FONT_BLUE, Statistic.SCORE, Statistic.LINES, Statistic.LEVEL,
				Statistic.TIME, Statistic.SPL, Statistic.LPM);
		drawResultRank(engine, playerID, 12, Colors.FONT_BLUE, rankingRank);
		drawResultNetRank(engine, playerID, 14, Colors.FONT_BLUE, netRankingRank[0]);
		drawResultNetRankDaily(engine, playerID, 16, Colors.FONT_BLUE, netRankingRank[1]);

		if (netIsPB) {
			renderer.drawMenuFont(engine, playerID, 2, 21, "NEW PB", Colors.FONT_ORANGE);
		}

		if (netIsNetPlay && netReplaySendStatus == 1) {
			renderer.drawMenuFont(engine, playerID, 0, 22, "SENDING...", Colors.FONT_PINK);
		} else if (netIsNetPlay && !netIsWatch && netReplaySendStatus == 2) {
			renderer.drawMenuFont(engine, playerID, 1, 22, "A: RETRY", Colors.FONT_RED);
		}
	}

	/*
	 * Called when saving replay
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(prop);

		// NET: Save name
		if (netPlayerName != null && !netPlayerName.isEmpty()) {
			prop.setProperty(playerID + ".net.netPlayerName", netPlayerName);
		}

		// Update rankings
		if (!owner.replayMode && !big && engine.ai == null) {
			updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, endless);

			if (rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				GeneralUtil.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * Load settings from property file
	 *
	 * @param prop Property file
	 */
	@Override
	protected void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("extreme.startlevel", 0);
		tspinEnableType = prop.getProperty("extreme.tspinEnableType", 1);
		enableTSpin = prop.getProperty("extreme.enableTSpin", true);
		enableTSpinKick = prop.getProperty("extreme.enableTSpinKick", true);
		spinCheckType = prop.getProperty("extreme.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("extreme.tspinEnableEZ", false);
		enableB2B = prop.getProperty("extreme.enableB2B", true);
		enableCombo = prop.getProperty("extreme.enableCombo", true);
		endless = prop.getProperty("extreme.endless", false);
		big = prop.getProperty("extreme.big", false);
		version = prop.getProperty("extreme.version", 0);
	}

	/**
	 * Save settings to property file
	 *
	 * @param prop Property file
	 */
	@Override
	protected void saveSetting(CustomProperties prop) {
		prop.setProperty("extreme.startlevel", startlevel);
		prop.setProperty("extreme.tspinEnableType", tspinEnableType);
		prop.setProperty("extreme.enableTSpin", enableTSpin);
		prop.setProperty("extreme.enableTSpinKick", enableTSpinKick);
		prop.setProperty("extreme.spinCheckType", spinCheckType);
		prop.setProperty("extreme.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("extreme.enableB2B", enableB2B);
		prop.setProperty("extreme.enableCombo", enableCombo);
		prop.setProperty("extreme.endless", endless);
		prop.setProperty("extreme.big", big);
		prop.setProperty("extreme.version", version);
	}

	/**
	 * Read rankings from property file
	 *
	 * @param prop     Property file
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for (int i = 0; i < RANKING_MAX; i++) {
			for (int endlessIndex = 0; endlessIndex < 2; endlessIndex++) {
				rankingScore[endlessIndex][i] = prop
						.getProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".score." + i, 0);
				rankingLines[endlessIndex][i] = prop
						.getProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".lines." + i, 0);
				rankingTime[endlessIndex][i] = prop
						.getProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".time." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 *
	 * @param prop     Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for (int i = 0; i < RANKING_MAX; i++) {
			for (int endlessIndex = 0; endlessIndex < 2; endlessIndex++) {
				prop.setProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".score." + i,
						rankingScore[endlessIndex][i]);
				prop.setProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".lines." + i,
						rankingLines[endlessIndex][i]);
				prop.setProperty("extreme.ranking." + ruleName + "." + endlessIndex + ".time." + i,
						rankingTime[endlessIndex][i]);
			}
		}
	}

	/**
	 * Update rankings
	 *
	 * @param sc   Score
	 * @param li   Lines
	 * @param time Time
	 */
	private void updateRanking(int sc, int li, int time, boolean endlessMode) {
		rankingRank = checkRanking(sc, li, time, endlessMode);

		if (rankingRank != -1) {
			int endlessIndex = 0;
			if (endlessMode) {
				endlessIndex = 1;
			}

			// Shift down ranking entries
			for (int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingScore[endlessIndex][i] = rankingScore[endlessIndex][i - 1];
				rankingLines[endlessIndex][i] = rankingLines[endlessIndex][i - 1];
				rankingTime[endlessIndex][i] = rankingTime[endlessIndex][i - 1];
			}

			// Add new data
			rankingScore[endlessIndex][rankingRank] = sc;
			rankingLines[endlessIndex][rankingRank] = li;
			rankingTime[endlessIndex][rankingRank] = time;
		}
	}

	/**
	 * Calculate ranking position
	 *
	 * @param sc   Score
	 * @param li   Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int sc, int li, int time, boolean endlessMode) {
		int endlessIndex = 0;
		if (endlessMode) {
			endlessIndex = 1;
		}

		for (int i = 0; i < RANKING_MAX; i++) {
			if (sc > rankingScore[endlessIndex][i]) {
				return i;
			}
			if (sc == rankingScore[endlessIndex][i] && li > rankingLines[endlessIndex][i]) {
				return i;
			}
			if (sc == rankingScore[endlessIndex][i] && li == rankingLines[endlessIndex][i]
					&& time < rankingTime[endlessIndex][i]) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * NET: Send various in-game stats (as well as goaltype)
	 *
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendStats(GameEngine engine) {
		int bg = engine.owner.backgroundStatus.fadesw ? engine.owner.backgroundStatus.fadebg
				: engine.owner.backgroundStatus.bg;
		String stats = "stats\t";
		stats += engine.statistics.score + "\t";
		stats += engine.statistics.lines + "\t";
		stats += engine.statistics.totalPieceLocked + "\t";
		stats += engine.statistics.time + "\t";
		stats += engine.statistics.level + "\t";
		stats += engine.statistics.lpm + "\t";
		stats += engine.statistics.spl + "\t";
		stats += endless + "\t";
		stats += engine.gameActive + "\t";
		stats += engine.timerActive + "\t";
		stats += lastscore + "\t";
		stats += scgettime + "\t";
		stats += lastevent.ordinal() + "\t";
		stats += lastb2b + "\t";
		stats += lastcombo + "\t";
		stats += lastpiece + "\t";
		stats += bg + "\t";
		stats += rolltime + "\t";
		stats += engine.meterValue + "\t";
		stats += engine.meterColor + "\n";
		netLobby.netPlayerClient.send(NetCmd.GAME, stats);
	}

	/**
	 * NET: Receive various in-game stats (as well as goaltype)
	 */
	@Override
	protected void netRecvStats(GameEngine engine, NetMessage message) {
		engine.statistics.score = message.asInt(3);
		engine.statistics.lines = message.asInt(4);
		engine.statistics.totalPieceLocked = message.asInt(5);
		engine.statistics.time = message.asInt(6);
		engine.statistics.level = message.asInt(7);
		engine.statistics.lpm = message.asFloat(8);
		engine.statistics.spl = message.asDouble(9);
		endless = message.asBool(10);
		engine.gameActive = message.asBool(11);
		engine.timerActive = message.asBool(12);
		lastscore = message.asInt(13);
		scgettime = message.asInt(14);
		lastevent = LineClearEvent.values()[message.asInt(15)];
		lastb2b = message.asBool(16);
		lastcombo = message.asInt(17);
		lastpiece = message.asInt(18);
		engine.owner.backgroundStatus.bg = message.asInt(19);
		rolltime = message.asInt(20);
		engine.meterValue = message.asInt(21);
		engine.meterColor = message.asInt(22);
	}


	/**
	 * NET: Send end-of-game stats
	 *
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendEndGameStats(GameEngine engine) {
		String stats = "";
		stats += "SCORE;" + engine.statistics.score + "\t";
		stats += "LINE;" + engine.statistics.lines + "\t";
		stats += "LEVEL;" + (engine.statistics.level + engine.statistics.levelDispAdd) + "\t";
		stats += "TIME;" + GeneralUtil.getTime(engine.statistics.time) + "\t";
		stats += "SCORE/LINE;" + engine.statistics.spl + "\t";
		stats += "LINE/MIN;" + engine.statistics.lpm;
		netLobby.netPlayerClient.send(NetCmd.GSTAT_1P, NetUtil.urlEncode(stats));
	}

	/**
	 * NET: Send game options to all spectators
	 *
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendOptions(GameEngine engine) {
		String options = "option\t";
		options += startlevel + "\t";
		options += tspinEnableType + "\t";
		options += enableTSpinKick + "\t";
		options += enableB2B + "\t";
		options += enableCombo + "\t";
		options += endless + "\t";
		options += big + "\t";
		options += spinCheckType + "\t";
		options += tspinEnableEZ;
		netLobby.netPlayerClient.send(NetCmd.GAME, options);
	}

	/**
	 * NET: Receive game options
	 */
	@Override
	protected void netRecvOptions(GameEngine engine, NetMessage message) {
		startlevel = message.asInt(3);
		tspinEnableType = message.asInt(4);
		enableTSpinKick = message.asBool(5);
		enableB2B = message.asBool(6);
		enableCombo = message.asBool(7);
		endless = message.asBool(8);
		big = message.asBool(9);
		spinCheckType = message.asInt(10);
		tspinEnableEZ = message.asBool(11);
	}

	/**
	 * NET: Get goal type
	 */
	@Override
	protected int netGetGoalType() {
		return endless ? 1 : 0;
	}

	/**
	 * NET: It returns true when the current settings doesn't prevent leaderboard
	 * screen from showing.
	 */
	@Override
	protected boolean netIsNetRankingViewOK(GameEngine engine) {
		return startlevel == 0 && !big && engine.ai == null;
	}
}

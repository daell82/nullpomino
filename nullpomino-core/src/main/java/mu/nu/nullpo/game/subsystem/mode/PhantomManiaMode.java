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

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.mode.menu.BooleanMenuItem;
import mu.nu.nullpo.game.subsystem.mode.menu.IntegerMenuItem;
import mu.nu.nullpo.game.subsystem.mode.menu.OnOffMenuItem;
import mu.nu.nullpo.util.Colors;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import mu.nu.nullpo.util.Sounds;

/**
 * PHANTOM MANIA mode (Original from NullpoUE build 121909 by Zircean)
 */
public class PhantomManiaMode extends AbstractMode {

	/** Current version */
	private static final int CURRENT_VERSION = 1;

	/** ARE table */
	private static final int[] tableARE = { 15, 11, 11, 5, 4, 3 };

	/** ARE Line table */
	private static final int[] tableARELine = { 11, 5, 5, 4, 4, 3 };

	/** Line Delay table */
	private static final int[] tableLineDelay = { 12, 6, 6, 7, 5, 4 };

	/** Lock Delay table */
	private static final int[] tableLockDelay = { 31, 27, 23, 19, 16, 16 };

	/** DAS table */
	private static final int[] tableDAS = { 11, 11, 10, 9, 7, 7 };

	/** BGM fadeout level */
	private static final int[] tableBGMFadeout = { 280, 480, -1 };

	/** BGM change level */
	private static final int[] tableBGMChange = { 300, 500, -1 };

	/** Grade names */
	private static final String[] tableGradeName = { "", "M", "MK", "MV", "MO", "MM", "GM" };

	/** Secret grade names */
	private static final String[] tableSecretGradeName = { "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", // 0?`
																													// 8
			"M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", // 9?`17
			"GM" // 18
	};

	/** Required level for grade */
	private static final int[] tableGradeLevel = { 0, 300, 500, 600, 700, 800, 999 };

	/** Ending time limit */
	private static final int ROLLTIMELIMIT = 1982;

	/** Number of hiscore records */
	private static final int RANKING_MAX = 10;

	/** Level 300 time limit */
	private static final int LV300TORIKAN = 8880;

	/** Level 500 time limit */
	private static final int LV500TORIKAN = 13080;

	/** Level 800 time limit */
	private static final int LV800TORIKAN = 19380;

	/** Number of sections */
	private static final int SECTION_MAX = 10;

	/** Default section time */
	private static final int DEFAULT_SECTION_TIME = 3600;

	/** GameManager object (Manages entire game status) */

	/**
	 * EventReceiver object (This receives many game events, can also be used for
	 * drawing the fonts.)
	 */

	/** Next section level */
	private int nextseclv;

	/** Level up flag (Set to true when the level increases) */
	private boolean lvupflag;

	/** Current grade */
	private int grade;

	/** Remaining frames of flash effect of grade display */
	private int gradeflash;

	/** Used by combo scoring */
	private int comboValue;

	/** Amount of points you just get from line clears */
	private int lastscore;

	/**
	 * Elapsed time from last line clear (lastscore is displayed to screen until
	 * this reaches to 120)
	 */
	private int scgettime;

	/** Secret Grade */
	private int secretGrade;

	/** Remaining ending time limit */
	private int rolltime;

	/** 0:Died before ending, 1:Died during ending, 2:Completed ending */
	private int rollclear;

	/** True if ending has started */
	private boolean rollstarted;

	/** Current BGM */
	private int bgmlv;

	/** Section Time */
	private int[] sectiontime;

	/**
	 * This will be true if the player achieves new section time record in specific
	 * section
	 */
	private boolean[] sectionIsNewRecord;

	/** Amount of sections completed */
	private int sectionscomp;

	/** Average section time */
	private int sectionavgtime;

	/** Current section time */
	private int sectionlasttime;

	/** Number of 4-Line clears in current section */
	private int sectionfourline;

	/** Set to true by default, set to false when sectionfourline is below 2 */
	private boolean gmfourline;

	/** AC medal (0:None, 1:Bronze, 2:Silver, 3:Gold) */
	private int medalAC;

	/** ST medal */
	private int medalST;

	/** SK medal */
	private int medalSK;

	/** RE medal */
	private int medalRE;

	/** RO medal */
	private int medalRO;

	/** CO medal */
	private int medalCO;

	/** Used by RE medal */
	private boolean recoveryFlag;

	/** Total rotations */
	private int rotateCount;

	/**
	 * false:Leaderboard, true:Section time record (Push F in settings screen to
	 * flip it)
	 */
	private boolean isShowBestSectionTime;

	/** Selected start level */
	private IntegerMenuItem startlevel;

	/** Enable/Disable level stop sfx */
	private BooleanMenuItem lvstopse;

	/** Big mode */
	private BooleanMenuItem big;

	/** Show section time */
	private BooleanMenuItem showsectiontime;

	/** Version of this mode */
	private int version;

	/** Your place on leaderboard (-1: out of rank) */
	private int rankingRank;

	/** Grade records */
	private int[] rankingGrade;

	/** Level records */
	private int[] rankingLevel;

	/** Time records */
	private int[] rankingTime;

	/** Roll-Cleared records */
	private int[] rankingRollclear;

	/** Best section time records */
	private int[] bestSectionTime;

	public PhantomManiaMode() {
		propName = "phantommania";

		startlevel = new IntegerMenuItem("startlevel", "LEVEL", Colors.FONT_BLUE, 0, 0, 9) {
			@Override
			public String getValueString() {
				return String.valueOf(value * 100);
			}
		};
		lvstopse = new OnOffMenuItem("lvstopse", "LVSTOPSE", Colors.FONT_BLUE, false);
		big = new OnOffMenuItem("big", "BIG", Colors.FONT_BLUE, false);
		showsectiontime = new OnOffMenuItem("showsectiontime", "SHOW STIME", Colors.FONT_BLUE, false);
		menu.add(startlevel);
		menu.add(lvstopse);
		menu.add(showsectiontime);
		menu.add(big);
	}

	/**
	 * Returns the name of this mode
	 */
	@Override
	public String getName() {
		return "PHANTOM MANIA";
	}

	/**
	 * This function will be called when the game enters the main game screen.
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		renderer = engine.owner.renderer;

		nextseclv = 0;
		lvupflag = true;
		grade = 0;
		gradeflash = 0;
		comboValue = 0;
		lastscore = 0;
		scgettime = 0;
		rolltime = 0;
		rollclear = 0;
		rollstarted = false;
		bgmlv = 0;
		sectiontime = new int[SECTION_MAX];
		sectionIsNewRecord = new boolean[SECTION_MAX];
		sectionavgtime = 0;
		sectionlasttime = 0;
		sectionfourline = 0;
		gmfourline = true;
		medalAC = 0;
		medalST = 0;
		medalSK = 0;
		medalRE = 0;
		medalRO = 0;
		medalCO = 0;
		recoveryFlag = false;
		rotateCount = 0;
		isShowBestSectionTime = false;

		rankingRank = -1;
		rankingGrade = new int[RANKING_MAX];
		rankingLevel = new int[RANKING_MAX];
		rankingTime = new int[RANKING_MAX];
		rankingRollclear = new int[RANKING_MAX];
		bestSectionTime = new int[SECTION_MAX];

		engine.tspinEnable = false;
		engine.b2bEnable = false;
		engine.comboType = GameEngine.COMBO_TYPE_DOUBLE;
		engine.framecolor = Colors.FRAME_COLOR_CYAN;
		engine.blockHidden = engine.ruleopt.lockflash;
		engine.bighalf = true;
		engine.bigmove = true;
		engine.staffrollEnable = true;
		engine.staffrollNoDeath = false;

		if (!owner.replayMode) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			for (int i = 0; i < SECTION_MAX; i++) {
				bestSectionTime[i] = DEFAULT_SECTION_TIME;
			}
			loadSetting(owner.replayProp);
			version = owner.replayProp.getProperty("phantommania.version", 0);
		}

		owner.backgroundStatus.bg = startlevel.value;
	}

	/**
	 * Set the starting bgmlv
	 */
	private void setStartBgmlv(GameEngine engine) {
		bgmlv = 0;
		while (tableBGMChange[bgmlv] != -1 && engine.statistics.level >= tableBGMChange[bgmlv]) {
			bgmlv++;
		}
	}

	/**
	 * Set the gravity speed
	 *
	 * @param engine GameEngine object
	 */
	private void setSpeed(GameEngine engine) {
		engine.speed.gravity = -1;

		int section = engine.statistics.level / 100;
		if (section > tableARE.length - 1) {
			section = tableARE.length - 1;
		}
		engine.speed.are = tableARE[section];
		engine.speed.areLine = tableARELine[section];
		engine.speed.lineDelay = tableLineDelay[section];
		engine.speed.lockDelay = tableLockDelay[section];
		engine.speed.das = tableDAS[section];
	}

	/**
	 * Calculates average section time
	 */
	private void setAverageSectionTime() {
		if (sectionscomp > 0) {
			int temp = 0;
			for (int i = startlevel.value; i < startlevel.value + sectionscomp; i++) {
				temp += sectiontime[i];
			}
			sectionavgtime = temp / sectionscomp;
		} else {
			sectionavgtime = 0;
		}
	}

	/**
	 * Checks ST medal
	 *
	 * @param engine        GameEngine
	 * @param sectionNumber Section Number
	 */
	private void stMedalCheck(GameEngine engine, int sectionNumber) {
		int best = bestSectionTime[sectionNumber];

		if (sectionlasttime < best) {
			if (medalST < 3) {
				engine.playSE(Sounds.MEDAL);
				medalST = 3;
			}
			if (!owner.replayMode) {
				sectionIsNewRecord[sectionNumber] = true;
			}
		} else if (sectionlasttime < best + 300 && medalST < 2) {
			engine.playSE(Sounds.MEDAL);
			medalST = 2;
		} else if (sectionlasttime < best + 600 && medalST < 1) {
			engine.playSE(Sounds.MEDAL);
			medalST = 1;
		}
	}

	/**
	 * Checks RO medal
	 */
	private void roMedalCheck(GameEngine engine) {
		float rotateAverage = (float) rotateCount / (float) engine.statistics.totalPieceLocked;

		if (rotateAverage >= 1.2f && medalRO < 3) {
			renderer.playSE(Sounds.MEDAL);
			medalRO++;
		}
	}

	/**
	 * Get medal font color
	 */
	private int getMedalFontColor(int medalColor) {
		switch (medalColor) {
		case 1:
			return Colors.FONT_RED;
		case 2:
			return Colors.FONT_WHITE;
		case 3:
			return Colors.FONT_YELLOW;
		default:
			break;
		}
		return -1;
	}

	/**
	 * Main routine for game setup screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		if (!owner.replayMode) {
			updateMenu(engine);
			owner.backgroundStatus.bg = startlevel.value;

			// Check for F button, when pressed this will flip Leaderboard/Best Section Time
			// Records
			if (engine.ctrl.isPush(Controller.BUTTON_F) && menuTime >= 5) {
				engine.playSE(Sounds.CHANGE);
				isShowBestSectionTime = !isShowBestSectionTime;
			}

			// Check for A button, when pressed this will begin the game
			if (engine.ctrl.isPush(Controller.BUTTON_A) && menuTime >= 5) {
				renderer.playSE(Sounds.DECIDE);
				saveSetting(owner.modeConfig);
				GeneralUtil.saveModeConfig(owner.modeConfig);
				isShowBestSectionTime = false;
				return false;
			}

			// Check for B button, when pressed this will shutdown the game engine.
			if (engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
			}

			menuTime++;
		} else {
			menuTime++;
			menuCursor = -1;

			if (menuTime >= 60) {
				return false;
			}
		}

		return true;
	}

	/**
	 * This function will be called before the game actually begins (after Ready&Go
	 * screen disappears)
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel.value * 100;

		nextseclv = engine.statistics.level + 100;
		if (engine.statistics.level < 0) {
			nextseclv = 100;
		}
		if (engine.statistics.level >= 900) {
			nextseclv = 999;
		}

		owner.backgroundStatus.bg = engine.statistics.level / 100;

		engine.big = big.value;

		setSpeed(engine);
		setStartBgmlv(engine);
		owner.bgmStatus.bgm = bgmlv + 1;

		sectionscomp = 0;
	}

	/**
	 * Renders HUD (leaderboard or game statistics)
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		renderer.drawScoreFont(engine, playerID, 0, 0, "PHANTOM MANIA", Colors.FONT_WHITE);

		if (engine.stat == GameEngine.Status.SETTING
				|| engine.stat == GameEngine.Status.RESULT && !owner.replayMode) {
			if (!owner.replayMode && startlevel.value == 0 && !big.getValue() && engine.ai == null) {
				if (!isShowBestSectionTime) {
					// Leaderboard
					float scale = renderer.getNextDisplayType() == 2 ? 0.5f : 1.0f;
					int topY = renderer.getNextDisplayType() == 2 ? 5 : 3;
					renderer.drawScoreFont(engine, playerID, 3, topY - 1, "GRADE LEVEL TIME", Colors.FONT_BLUE,
							scale);

					for (int i = 0; i < RANKING_MAX; i++) {
						int gcolor = Colors.FONT_WHITE;
						if (rankingRollclear[i] == 1) {
							gcolor = Colors.FONT_GREEN;
						}
						if (rankingRollclear[i] == 2) {
							gcolor = Colors.FONT_ORANGE;
						}

						renderer.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1),
								Colors.FONT_YELLOW, scale);
						if (rankingGrade[i] >= 0 && rankingGrade[i] < tableGradeName.length) {
							renderer.drawScoreFont(engine, playerID, 3, topY + i, tableGradeName[rankingGrade[i]],
									gcolor, scale);
						}
						renderer.drawScoreFont(engine, playerID, 9, topY + i, String.valueOf(rankingLevel[i]),
								i == rankingRank, scale);
						renderer.drawScoreFont(engine, playerID, 15, topY + i, GeneralUtil.getTime(rankingTime[i]),
								i == rankingRank, scale);
					}

					renderer.drawScoreFont(engine, playerID, 0, 17, "F:VIEW SECTION TIME", Colors.FONT_GREEN);
				} else {
					// Best section time records
					renderer.drawScoreFont(engine, playerID, 0, 2, "SECTION TIME", Colors.FONT_BLUE);

					int totalTime = 0;
					for (int i = 0; i < SECTION_MAX; i++) {
						int temp = Math.min(i * 100, 999);
						int temp2 = Math.min((i + 1) * 100 - 1, 999);

						String strSectionTime;
						strSectionTime = String.format("%3d-%3d %s", temp, temp2,
								GeneralUtil.getTime(bestSectionTime[i]));

						renderer.drawScoreFont(engine, playerID, 0, 3 + i, strSectionTime, sectionIsNewRecord[i]);

						totalTime += bestSectionTime[i];
					}

					renderer.drawScoreFont(engine, playerID, 0, 14, "TOTAL", Colors.FONT_BLUE);
					renderer.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(totalTime));
					renderer.drawScoreFont(engine, playerID, 9, 14, "AVERAGE", Colors.FONT_BLUE);
					renderer.drawScoreFont(engine, playerID, 9, 15, GeneralUtil.getTime(totalTime / SECTION_MAX));

					renderer.drawScoreFont(engine, playerID, 0, 17, "F:VIEW RANKING", Colors.FONT_GREEN);
				}
			}
		} else {
			renderer.drawScoreFont(engine, playerID, 0, 2, "GRADE", Colors.FONT_BLUE);
			if (grade >= 0 && grade < tableGradeName.length) {
				renderer.drawScoreFont(engine, playerID, 0, 3, tableGradeName[grade],
						gradeflash > 0 && gradeflash % 4 == 0);
			}

			renderer.drawScoreFont(engine, playerID, 0, 5, "SCORE", Colors.FONT_BLUE);
			String strScore = String.valueOf(engine.statistics.score);
			if (lastscore != 0 && scgettime > 0) {
				strScore += "\n(+" + lastscore + ")";
			}
			renderer.drawScoreFont(engine, playerID, 0, 6, strScore);

			renderer.drawScoreFont(engine, playerID, 0, 9, "LEVEL", Colors.FONT_BLUE);
			int tempLevel = engine.statistics.level;
			if (tempLevel < 0) {
				tempLevel = 0;
			}
			String strLevel = String.format("%3d", tempLevel);
			renderer.drawScoreFont(engine, playerID, 0, 10, strLevel);

			int speed = engine.speed.gravity / 128;
			if (engine.speed.gravity < 0) {
				speed = 40;
			}
			renderer.drawSpeedMeter(engine, playerID, 0, 11, speed);

			renderer.drawScoreFont(engine, playerID, 0, 12, String.format("%3d", nextseclv));

			renderer.drawScoreFont(engine, playerID, 0, 14, "TIME", Colors.FONT_BLUE);
			renderer.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(engine.statistics.time));

			if (engine.gameActive && engine.ending == 2) {
				int time = ROLLTIMELIMIT - rolltime;
				if (time < 0) {
					time = 0;
				}
				renderer.drawScoreFont(engine, playerID, 0, 17, "ROLL TIME", Colors.FONT_BLUE);
				renderer.drawScoreFont(engine, playerID, 0, 18, GeneralUtil.getTime(time),
						time > 0 && time < 10 * 60);
			}

			if (medalAC >= 1) {
				renderer.drawScoreFont(engine, playerID, 0, 20, "AC", getMedalFontColor(medalAC));
			}
			if (medalST >= 1) {
				renderer.drawScoreFont(engine, playerID, 3, 20, "ST", getMedalFontColor(medalST));
			}
			if (medalSK >= 1) {
				renderer.drawScoreFont(engine, playerID, 0, 21, "SK", getMedalFontColor(medalSK));
			}
			if (medalRE >= 1) {
				renderer.drawScoreFont(engine, playerID, 3, 21, "RE", getMedalFontColor(medalRE));
			}
			if (medalRO >= 1) {
				renderer.drawScoreFont(engine, playerID, 0, 22, "RO", getMedalFontColor(medalRO));
			}
			if (medalCO >= 1) {
				renderer.drawScoreFont(engine, playerID, 3, 22, "CO", getMedalFontColor(medalCO));
			}

			if (showsectiontime.getValue() && sectiontime != null) {
				int x = renderer.getNextDisplayType() == 2 ? 8 : 12;
				int x2 = renderer.getNextDisplayType() == 2 ? 9 : 12;

				renderer.drawScoreFont(engine, playerID, x, 2, "SECTION TIME", Colors.FONT_BLUE);

				for (int i = 0; i < sectiontime.length; i++) {
					if (sectiontime[i] > 0) {
						int temp = i * 100;
						if (temp > 999) {
							temp = 999;
						}

						int section = engine.statistics.level / 100;
						String strSeparator = " ";
						if (i == section && engine.ending == 0) {
							strSeparator = "b";
						}

						String strSectionTime;
						strSectionTime = String.format("%3d%s%s", temp, strSeparator,
								GeneralUtil.getTime(sectiontime[i]));

						renderer.drawScoreFont(engine, playerID, x, 3 + i, strSectionTime, sectionIsNewRecord[i]);
					}
				}

				if (sectionavgtime > 0) {
					renderer.drawScoreFont(engine, playerID, x2, 14, "AVERAGE", Colors.FONT_BLUE);
					renderer.drawScoreFont(engine, playerID, x2, 15, GeneralUtil.getTime(sectionavgtime));
				}
			}
		}
	}

	/**
	 * This function will be called when the piece is active
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		if (engine.ending == 0 && engine.statc_0() == 0 && !engine.holdDisable && !lvupflag) {
			if (engine.statistics.level < nextseclv - 1) {
				engine.statistics.level++;
				if (engine.statistics.level == nextseclv - 1 && lvstopse.getValue()) {
					owner.renderer.playSE(Sounds.LEVEL_STOP);
				}
			}
			levelUp(engine);

			if (engine.timerActive && medalRE < 3) {
				int blocks = engine.field.getHowManyBlocks();

				if (!recoveryFlag) {
					if (blocks >= 150) {
						recoveryFlag = true;
					}
				} else if (blocks <= 70) {
					recoveryFlag = false;
					renderer.playSE(Sounds.MEDAL);
					medalRE++;
				}
			}
		}
		if (engine.ending == 0 && engine.statc_0() > 0) {
			lvupflag = false;
		}

		if (engine.ending == 2 && !rollstarted) {
			rollstarted = true;
		}

		return false;
	}

	/**
	 * This function will be called during ARE
	 */
	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		if (engine.ending == 0 && engine.statc_0() >= engine.statc_0() - 1 && !lvupflag) {
			if (engine.statistics.level < nextseclv - 1) {
				engine.statistics.level++;
				if (engine.statistics.level == nextseclv - 1 && lvstopse.getValue()) {
					owner.renderer.playSE(Sounds.LEVEL_STOP);
				}
			}
			levelUp(engine);
			lvupflag = true;
		}

		return false;
	}

	/**
	 * Levelup
	 */
	private void levelUp(GameEngine engine) {
		engine.meterValue = engine.statistics.level % 100 * renderer.getMeterMax(engine) / 99;
		engine.meterColor = Colors.METER_COLOR_GREEN;
		if (engine.statistics.level % 100 >= 50) {
			engine.meterColor = Colors.METER_COLOR_YELLOW;
		}
		if (engine.statistics.level % 100 >= 80) {
			engine.meterColor = Colors.METER_COLOR_ORANGE;
		}
		if (engine.statistics.level >= nextseclv - 1) {
			engine.meterColor = Colors.METER_COLOR_RED;
		}

		setSpeed(engine);

		if (tableBGMFadeout[bgmlv] != -1 && engine.statistics.level >= tableBGMFadeout[bgmlv]) {
			owner.bgmStatus.fadesw = true;
		}
	}

	/**
	 * Calculates line-clear score (This function will be called even if no lines
	 * are cleared)
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		if (lines == 0) {
			comboValue = 1;
		} else {
			comboValue = comboValue + 2 * lines - 2;
			if (comboValue < 1) {
				comboValue = 1;
			}
		}

		int rotateTemp = engine.nowPieceRotateCount;
		if (rotateTemp > 4) {
			rotateTemp = 4;
		}
		rotateCount += rotateTemp;

		if (lines >= 1 && engine.ending == 0) {
			if (lines >= 4) {
				sectionfourline++;

				if (big.getValue()) {
					if (engine.statistics.totalFour == 1 || engine.statistics.totalFour == 2
							|| engine.statistics.totalFour == 4) {
						renderer.playSE(Sounds.MEDAL);
						medalSK++;
					}
				} else if (engine.statistics.totalFour == 5 || engine.statistics.totalFour == 10
						|| engine.statistics.totalFour == 17) {
					renderer.playSE(Sounds.MEDAL);
					medalSK++;
				}
			}

			if (engine.field.isEmpty()) {
				renderer.playSE("bravo");

				if (medalAC < 3) {
					renderer.playSE(Sounds.MEDAL);
					medalAC++;
				}
			}

			if (big.getValue()) {
				if (engine.combo >= 2 && medalCO < 1) {
					renderer.playSE(Sounds.MEDAL);
					medalCO = 1;
				} else if (engine.combo >= 3 && medalCO < 2) {
					renderer.playSE(Sounds.MEDAL);
					medalCO = 2;
				} else if (engine.combo >= 4 && medalCO < 3) {
					renderer.playSE(Sounds.MEDAL);
					medalCO = 3;
				}
			} else if (engine.combo >= 4 && medalCO < 1) {
				renderer.playSE(Sounds.MEDAL);
				medalCO = 1;
			} else if (engine.combo >= 5 && medalCO < 2) {
				renderer.playSE(Sounds.MEDAL);
				medalCO = 2;
			} else if (engine.combo >= 7 && medalCO < 3) {
				renderer.playSE(Sounds.MEDAL);
				medalCO = 3;
			}

			int levelb = engine.statistics.level;
			engine.statistics.level += lines;
			levelUp(engine);

			if (engine.statistics.level >= 999) {
				if (engine.timerActive) {
					sectionscomp++;
					setAverageSectionTime();
				}

				renderer.playSE(Sounds.ENDING_START);
				engine.statistics.level = 999;
				engine.timerActive = false;
				engine.ending = 2;
				rollclear = 1;

				sectionlasttime = sectiontime[levelb / 100];

				stMedalCheck(engine, levelb / 100);

				roMedalCheck(engine);

				if (engine.statistics.totalFour >= 31 && gmfourline && sectionfourline >= 1) {
					grade = 6;
					gradeflash = 180;
				}
			} else if (nextseclv == 300 && engine.statistics.level >= 300
					&& engine.statistics.time > LV300TORIKAN) {
				if (engine.timerActive) {
					sectionscomp++;
					setAverageSectionTime();
				}

				renderer.playSE(Sounds.ENDING_START);
				engine.statistics.level = 300;
				engine.timerActive = false;
				engine.ending = 2;

				if (tableBGMChange[bgmlv] != -1 && engine.statistics.level >= tableBGMChange[bgmlv]) {
					bgmlv++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = bgmlv + 1;
				}

				sectionlasttime = sectiontime[levelb / 100];

				stMedalCheck(engine, levelb / 100);
			} else if (nextseclv == 500 && engine.statistics.level >= 500
					&& engine.statistics.time > LV500TORIKAN) {
				if (engine.timerActive) {
					sectionscomp++;
					setAverageSectionTime();
				}

				renderer.playSE(Sounds.ENDING_START);
				engine.statistics.level = 500;
				engine.timerActive = false;
				engine.ending = 2;

				if (tableBGMChange[bgmlv] != -1 && engine.statistics.level >= tableBGMChange[bgmlv]) {
					bgmlv++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = bgmlv + 1;
				}

				sectionlasttime = sectiontime[levelb / 100];

				stMedalCheck(engine, levelb / 100);
			} else if (nextseclv == 800 && engine.statistics.level >= 800
					&& engine.statistics.time > LV800TORIKAN) {
				if (engine.timerActive) {
					sectionscomp++;
					setAverageSectionTime();
				}

				renderer.playSE(Sounds.ENDING_START);
				engine.statistics.level = 800;
				engine.timerActive = false;
				engine.ending = 2;

				if (tableBGMChange[bgmlv] != -1 && engine.statistics.level >= tableBGMChange[bgmlv]) {
					bgmlv++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = bgmlv + 1;
				}

				sectionlasttime = sectiontime[levelb / 100];

				stMedalCheck(engine, levelb / 100);
			} else if (engine.statistics.level >= nextseclv) {
				renderer.playSE("levelup");

				owner.backgroundStatus.fadesw = true;
				owner.backgroundStatus.fadecount = 0;
				owner.backgroundStatus.fadebg = nextseclv / 100;

				if (tableBGMChange[bgmlv] != -1 && engine.statistics.level >= tableBGMChange[bgmlv]) {
					bgmlv++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = bgmlv + 1;
				}

				sectionscomp++;

				sectionlasttime = sectiontime[levelb / 100];

				if (sectionfourline < 2) {
					gmfourline = false;
				}

				sectionfourline = 0;

				stMedalCheck(engine, levelb / 100);

				if (nextseclv == 300 || nextseclv == 700) {
					roMedalCheck(engine);
				}

				if (startlevel.value == 0) {
					for (int i = 0; i < tableGradeLevel.length - 1; i++) {
						if (engine.statistics.level >= tableGradeLevel[i]) {
							grade = i;
							gradeflash = 180;
						}
					}
				}

				nextseclv += 100;
				if (nextseclv > 999) {
					nextseclv = 999;
				}
			} else if (engine.statistics.level == nextseclv - 1 && lvstopse.getValue()) {
				renderer.playSE(Sounds.LEVEL_STOP);
			}

			int manuallock = 0;
			if (engine.manualLock) {
				manuallock = 1;
			}

			int bravo = 1;
			if (engine.field.isEmpty()) {
				bravo = 4;
			}

			int speedBonus = engine.getLockDelay() - engine.statc_0();
			if (speedBonus < 0) {
				speedBonus = 0;
			}

			lastscore = ((levelb + lines) / 4 + engine.softdropFall + manuallock) * lines * comboValue * bravo
					+ engine.statistics.level / 2 + speedBonus * 7;
			engine.statistics.score += lastscore;
			scgettime = 120;
		}
	}

	/**
	 * This function will be called when the game timer updates
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		if (gradeflash > 0) {
			gradeflash--;
		}

		if (scgettime > 0) {
			scgettime--;
		}

		if (engine.timerActive && engine.ending == 0) {
			int section = engine.statistics.level / 100;

			if (section >= 0 && section < sectiontime.length) {
				sectiontime[section]++;
				setAverageSectionTime();
			}
		}

		if (engine.gameActive && engine.ending == 2) {
			if (version >= 1 && engine.ctrl.isPress(Controller.BUTTON_F) && engine.statistics.level < 999) {
				rolltime += 5;
			} else {
				rolltime += 1;
			}

			int remainRollTime = ROLLTIMELIMIT - rolltime;
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

			if (rolltime >= ROLLTIMELIMIT) {
				if (engine.statistics.level >= 999) {
					rollclear = 2;
				}

				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.Status.EXCELLENT;
			}
		}
	}

	/**
	 * This function will be called when the player tops out
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		if (engine.statc_0() == 0) {
			secretGrade = engine.field.getSecretGrade();
		}
		return false;
	}

	/**
	 * Renders game result screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		int status1 = engine.statc_1();
		renderer.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (status1 + 1) + "/3",
				Colors.FONT_RED);

		switch (status1) {
		case 0: {
			int gcolor = Colors.FONT_WHITE;
			if (rollclear == 1) {
				gcolor = Colors.FONT_GREEN;
			}
			if (rollclear == 2) {
				gcolor = Colors.FONT_ORANGE;
			}
			renderer.drawMenuFont(engine, playerID, 0, 2, "GRADE", Colors.FONT_BLUE);
			String strGrade = String.format("%10s", tableGradeName[grade]);
			renderer.drawMenuFont(engine, playerID, 0, 3, strGrade, gcolor);
			drawResultStats(engine, playerID, renderer, 4, Colors.FONT_BLUE, Statistic.SCORE, Statistic.LINES,
					Statistic.LEVEL_MANIA, Statistic.TIME);
			drawResultRank(engine, playerID, renderer, 12, Colors.FONT_BLUE, rankingRank);
			if (secretGrade > 4) {
				drawResult(engine, playerID, renderer, 15, Colors.FONT_BLUE, "S. GRADE",
						String.format("%10s", tableSecretGradeName[secretGrade - 1]));
			}
			break;
		}
		case 1:
			renderer.drawMenuFont(engine, playerID, 0, 2, "SECTION", Colors.FONT_BLUE);
			for (int i = 0; i < sectiontime.length; i++) {
				if (sectiontime[i] > 0) {
					renderer.drawMenuFont(engine, playerID, 2, 3 + i, GeneralUtil.getTime(sectiontime[i]),
							sectionIsNewRecord[i]);
				}
			}
			if (sectionavgtime > 0) {
				renderer.drawMenuFont(engine, playerID, 0, 14, "AVERAGE", Colors.FONT_BLUE);
				renderer.drawMenuFont(engine, playerID, 2, 15, GeneralUtil.getTime(sectionavgtime));
			}
			break;
		case 2:
			renderer.drawMenuFont(engine, playerID, 0, 2, "MEDAL", Colors.FONT_BLUE);
			if (medalAC >= 1) {
				renderer.drawMenuFont(engine, playerID, 5, 3, "AC", getMedalFontColor(medalAC));
			}
			if (medalST >= 1) {
				renderer.drawMenuFont(engine, playerID, 8, 3, "ST", getMedalFontColor(medalST));
			}
			if (medalSK >= 1) {
				renderer.drawMenuFont(engine, playerID, 5, 4, "SK", getMedalFontColor(medalSK));
			}
			if (medalRE >= 1) {
				renderer.drawMenuFont(engine, playerID, 8, 4, "RE", getMedalFontColor(medalRE));
			}
			if (medalRO >= 1) {
				renderer.drawMenuFont(engine, playerID, 5, 5, "SK", getMedalFontColor(medalRO));
			}
			if (medalCO >= 1) {
				renderer.drawMenuFont(engine, playerID, 8, 5, "CO", getMedalFontColor(medalCO));
			}
			drawResultStats(engine, playerID, renderer, 6, Colors.FONT_BLUE, Statistic.LPM, Statistic.SPM,
					Statistic.PIECE, Statistic.PPS);
			break;
		default:
			break;
		}
	}

	/**
	 * Additional routine for game result screen
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		// Page change
		int status1 = engine.statc_1();
		if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
			status1 -= 1;
			if (status1 < 0) {
				status1 = 2;
			}
			engine.statc_1(status1);
			engine.playSE(Sounds.CHANGE);
		}
		if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
			status1 += 1;
			if (status1 > 2) {
				status1 = 0;
			}
			engine.statc_1(status1);
			engine.playSE(Sounds.CHANGE);
		}
		// Flip Leaderboard/Best Section Time Records
		if (engine.ctrl.isPush(Controller.BUTTON_F)) {
			engine.playSE(Sounds.CHANGE);
			isShowBestSectionTime = !isShowBestSectionTime;
		}

		return false;
	}

	/**
	 * This function will be called when the replay data is going to be saved
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(owner.replayProp);
		owner.replayProp.setProperty("phantommania.version", version);

		if (!owner.replayMode && startlevel.value == 0 && !big.getValue() && engine.ai == null) {
			updateRanking(grade, engine.statistics.level, engine.statistics.time, rollclear);
			if (medalST == 3) {
				updateBestSectionTime();
			}

			if (rankingRank != -1 || medalST == 3) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				GeneralUtil.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * Load the ranking
	 */
	private void loadRanking(CustomProperties prop, String ruleName) {
		for (int i = 0; i < RANKING_MAX; i++) {
			rankingGrade[i] = prop.getProperty("phantommania.ranking." + ruleName + ".grade." + i, 0);
			rankingLevel[i] = prop.getProperty("phantommania.ranking." + ruleName + ".level." + i, 0);
			rankingTime[i] = prop.getProperty("phantommania.ranking." + ruleName + ".time." + i, 0);
			rankingRollclear[i] = prop.getProperty("phantommania.ranking." + ruleName + ".rollclear." + i, 0);
		}
		for (int i = 0; i < SECTION_MAX; i++) {
			bestSectionTime[i] = prop.getProperty("phantommania.bestSectionTime." + ruleName + "." + i,
					DEFAULT_SECTION_TIME);
		}
	}

	/**
	 * Save the ranking
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for (int i = 0; i < RANKING_MAX; i++) {
			prop.setProperty("phantommania.ranking." + ruleName + ".grade." + i, rankingGrade[i]);
			prop.setProperty("phantommania.ranking." + ruleName + ".level." + i, rankingLevel[i]);
			prop.setProperty("phantommania.ranking." + ruleName + ".time." + i, rankingTime[i]);
			prop.setProperty("phantommania.ranking." + ruleName + ".rollclear." + i, rankingRollclear[i]);
		}
		for (int i = 0; i < SECTION_MAX; i++) {
			prop.setProperty("phantommania.bestSectionTime." + ruleName + "." + i, bestSectionTime[i]);
		}
	}

	/**
	 * Update the ranking
	 */
	private void updateRanking(int gr, int lv, int time, int clear) {
		rankingRank = checkRanking(gr, lv, time, clear);

		if (rankingRank != -1) {
			for (int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingGrade[i] = rankingGrade[i - 1];
				rankingLevel[i] = rankingLevel[i - 1];
				rankingTime[i] = rankingTime[i - 1];
				rankingRollclear[i] = rankingRollclear[i - 1];
			}

			rankingGrade[rankingRank] = gr;
			rankingLevel[rankingRank] = lv;
			rankingTime[rankingRank] = time;
			rankingRollclear[rankingRank] = clear;
		}
	}

	/**
	 * This function will check the ranking and returns which place you are. (-1:
	 * Out of rank)
	 */
	private int checkRanking(int gr, int lv, int time, int clear) {
		for (int i = 0; i < RANKING_MAX; i++) {
			if (clear > rankingRollclear[i]) {
				return i;
			}
			if (clear == rankingRollclear[i] && gr > rankingGrade[i]) {
				return i;
			}
			if (clear == rankingRollclear[i] && gr == rankingGrade[i] && lv > rankingLevel[i]) {
				return i;
			}
			if (clear == rankingRollclear[i] && gr == rankingGrade[i] && lv == rankingLevel[i]
					&& time < rankingTime[i]) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Updates best section time records
	 */
	private void updateBestSectionTime() {
		for (int i = 0; i < SECTION_MAX; i++) {
			if (sectionIsNewRecord[i]) {
				bestSectionTime[i] = sectiontime[i];
			}
		}
	}
}

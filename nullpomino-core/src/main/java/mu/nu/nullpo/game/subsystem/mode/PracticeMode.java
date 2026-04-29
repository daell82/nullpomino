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

import org.apache.log4j.Logger;

import mu.nu.nullpo.game.component.BGMusicStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.Colors;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

/**
 * PRACTICE Mode
 */
public class PracticeMode extends AbstractMode {
	/** Log */
	static Logger log = Logger.getLogger(PracticeMode.class);

	/** Current version */
	private static final int CURRENT_VERSION = 5;

	/** Most recent scoring event typeConstantcount */
	private static final int EVENT_NONE = 0, EVENT_SINGLE = 1, EVENT_DOUBLE = 2, EVENT_TRIPLE = 3, EVENT_FOUR = 4,
			EVENT_TSPIN_ZERO_MINI = 5, EVENT_TSPIN_ZERO = 6, EVENT_TSPIN_SINGLE_MINI = 7, EVENT_TSPIN_SINGLE = 8,
			EVENT_TSPIN_DOUBLE_MINI = 9, EVENT_TSPIN_DOUBLE = 10, EVENT_TSPIN_TRIPLE = 11, EVENT_TSPIN_EZ = 12;

	/** ComboGet in point */
	private static final int COMBO_GOAL_TABLE[] = { 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 4, 5 };

	/** LevelConstant of typecount */
	private static final int LEVELTYPE_NONE = 0, LEVELTYPE_10LINES = 1, LEVELTYPE_POINTS = 2, LEVELTYPE_MANIA = 3,
			LEVELTYPE_MANIAPLUS = 4, LEVELTYPE_MAX = 5;

	/** Dan&#39;s backName */
	private static final String[] tableSecretGradeName = { "9", "8", "7", "6", "5", "4", "3", "2", "1", // 0~ 8
			"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", // 9~17
			"GM" // 18
	};

	/** LevelThe display name of the type */
	private static final String[] LEVELTYPE_STRING = { "NONE", "10LINES", "POINTS", "MANIA", "MANIA+" };

	/** ComboThe display name of the type */
	private static final String[] COMBOTYPE_STRING = { "DISABLE", "NORMAL", "DOUBLE" };

	/** Outline type names */
	private static final String[] BLOCK_OUTLINE_TYPE_STRING = { "NONE", "NORMAL", "CONNECT", "SAMECOLOR" };

	/** Level upRemaining until point */
	private int goal;

	/** I got just before point */
	private int lastgoal;

	/** Most recent increase in score */
	private int lastscore;

	/** Time to display the most recent increase in score */
	private int scgettime;

	/** Most recent scoring event type */
	private int lastevent;

	/** Most recent scoring eventInB2BIf it&#39;s the casetrue */
	private boolean lastb2b;

	/** Most recent scoring eventInCombocount */
	private int lastcombo;

	/** Most recent scoring eventPeace inID */
	private int lastpiece;

	/** EndingThe rest of the time */
	private int rolltime;

	/** EndingStart flag */
	private boolean rollstarted;

	/** Dan back */
	private int secretGrade;

	/** BGM number */
	private int bgmno;

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

	/** ComboType */
	private int comboType;

	/** Big */
	private boolean big;

	/** BigLateral movement of the unit when */
	private boolean bigmove;

	/** BigWhenLinescountHalf */
	private boolean bighalf;

	/** LevelType */
	private int leveltype;

	/** Preset number */
	private int presetNumber;

	/** Map number */
	private int mapNumber;

	/** Current version */
	private int version;

	/** Next Section Of level (This-1At levelStop) */
	private int nextseclv;

	/** LevelHas increased flag */
	private boolean lvupflag;

	/** Combo bonus */
	private int comboValue;

	/** Hard drop bonus */
	private int harddropBonus;

	/** levelstop sound */
	private boolean lvstopse;

	/** Become clear level */
	private int goallv;

	/** Limit time (0:No) */
	private int timelimit;

	/** Ending time (0:No) */
	private int rolltimelimit;

	/** Arrangement of the pieces can appear */
	private boolean[] pieceEnable;

	/** MapUse flag */
	private boolean useMap;

	/** For backupfield (MapUsed to save the replay) */
	private Field fldBackup;

	/** Rest time */
	private int timelimitTimer;

	/** Level upLimit for each timeReset */
	private boolean timelimitResetEveryLevel;

	/** BoneBlockI use */
	private boolean bone;

	/** Number of frames before placed blocks disappear (-1:Disable) */
	private int blockHidden;

	/** Use alpha-blending for blockHidden */
	private boolean blockHiddenAnim;

	/** Outline type */
	private int blockOutlineType;

	/**
	 * Show outline only flag. If enabled it does not show actual image of blocks.
	 */
	private boolean blockShowOutlineOnly;

	/** Hebo hidden level (0=None) */
	private int heboHiddenLevel;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "PRACTICE";
	}

	/*
	 * Initialization
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		log.debug("playerInit called");

		owner = engine.owner;
		renderer = engine.owner.renderer;
		goal = 0;
		lastgoal = 0;
		lastscore = 0;
		scgettime = 0;
		lastevent = EVENT_NONE;
		lastb2b = false;
		lastcombo = 0;
		lastpiece = 0;
		nextseclv = 100;
		lvupflag = false;
		comboValue = 0;
		harddropBonus = 0;
		rolltime = 0;
		rollstarted = false;
		secretGrade = 0;
		pieceEnable = new boolean[Piece.PIECE_COUNT];
		fldBackup = null;
		timelimitTimer = 0;
		engine.framecolor = Colors.FRAME_COLOR_YELLOW;

		if (engine.owner.replayMode == false) {
			version = CURRENT_VERSION;
			presetNumber = engine.owner.modeConfig.getProperty("practice.presetNumber", 0);
			mapNumber = engine.owner.modeConfig.getProperty("practice.mapNumber", 0);
			loadPreset(engine, engine.owner.modeConfig, -1);
		} else {
			version = engine.owner.replayProp.getProperty("practice.version", CURRENT_VERSION);
			presetNumber = 0;
			mapNumber = 0;
			loadPreset(engine, engine.owner.replayProp, -1);
		}
	}

	/**
	 * PresetRead
	 *
	 * @param engine GameEngine
	 * @param prop   Property file to read from
	 * @param preset Preset number
	 */
	private void loadPreset(GameEngine engine, CustomProperties prop, int preset) {
		engine.speed.gravity = prop.getProperty("practice.gravity." + preset, 4);
		engine.speed.denominator = prop.getProperty("practice.denominator." + preset, 256);
		engine.speed.are = prop.getProperty("practice.are." + preset, 0);
		engine.speed.areLine = prop.getProperty("practice.areLine." + preset, 0);
		engine.speed.lineDelay = prop.getProperty("practice.lineDelay." + preset, 0);
		engine.speed.lockDelay = prop.getProperty("practice.lockDelay." + preset, 30);
		engine.speed.das = prop.getProperty("practice.das." + preset, 14);
		bgmno = prop.getProperty("practice.bgmno." + preset, 0);
		tspinEnableType = prop.getProperty("practice.tspinEnableType." + preset, 1);
		enableTSpin = prop.getProperty("practice.enableTSpin." + preset, true);
		enableTSpinKick = prop.getProperty("practice.enableTSpinKick." + preset, true);
		spinCheckType = prop.getProperty("practice.spinCheckType." + preset, 0);
		tspinEnableEZ = prop.getProperty("practice.tspinEnableEZ." + preset, false);
		enableB2B = prop.getProperty("practice.enableB2B." + preset, true);
		comboType = prop.getProperty("practice.comboType." + preset, GameEngine.COMBO_TYPE_NORMAL);
		big = prop.getProperty("practice.big." + preset, false);
		bigmove = prop.getProperty("practice.bigmove." + preset, true);
		bighalf = prop.getProperty("practice.bighalf." + preset, true);
		leveltype = prop.getProperty("practice.leveltype." + preset, LEVELTYPE_NONE);
		lvstopse = prop.getProperty("practice.lvstopse." + preset, true);
		goallv = prop.getProperty("practice.goallv." + preset, -1);
		timelimit = prop.getProperty("practice.timelimit." + preset, 0);
		rolltimelimit = prop.getProperty("practice.rolltimelimit." + preset, 0);
		for (int i = 0; i < Piece.PIECE_COUNT; i++) {
			pieceEnable[i] = prop.getProperty("practice.pieceEnable." + i + "." + preset,
					i < Piece.PIECE_STANDARD_COUNT);
		}
		useMap = prop.getProperty("practice.useMap." + preset, false);
		timelimitResetEveryLevel = prop.getProperty("practice.timelimitResetEveryLevel." + preset, false);
		bone = prop.getProperty("practice.bone." + preset, false);
		blockHidden = prop.getProperty("practice.blockHidden." + preset, -1);
		blockHiddenAnim = prop.getProperty("practice.blockHiddenAnim." + preset, true);
		blockOutlineType = prop.getProperty("practice.blockOutlineType." + preset, GameEngine.BLOCK_OUTLINE_NORMAL);
		blockShowOutlineOnly = prop.getProperty("practice.blockShowOutlineOnly." + preset, false);
		heboHiddenLevel = prop.getProperty("practice.heboHiddenLevel." + preset, 0);
	}

	/**
	 * PresetSave the
	 *
	 * @param engine GameEngine
	 * @param prop   Property file to save to
	 * @param preset Preset number
	 */
	private void savePreset(GameEngine engine, CustomProperties prop, int preset) {
		prop.setProperty("practice.gravity." + preset, engine.speed.gravity);
		prop.setProperty("practice.denominator." + preset, engine.speed.denominator);
		prop.setProperty("practice.are." + preset, engine.speed.are);
		prop.setProperty("practice.areLine." + preset, engine.speed.areLine);
		prop.setProperty("practice.lineDelay." + preset, engine.speed.lineDelay);
		prop.setProperty("practice.lockDelay." + preset, engine.speed.lockDelay);
		prop.setProperty("practice.das." + preset, engine.speed.das);
		prop.setProperty("practice.bgmno." + preset, bgmno);
		prop.setProperty("practice.tspinEnableType." + preset, tspinEnableType);
		prop.setProperty("practice.enableTSpin." + preset, enableTSpin);
		prop.setProperty("practice.enableTSpinKick." + preset, enableTSpinKick);
		prop.setProperty("practice.spinCheckType." + preset, spinCheckType);
		prop.setProperty("practice.tspinEnableEZ." + preset, tspinEnableEZ);
		prop.setProperty("practice.enableB2B." + preset, enableB2B);
		prop.setProperty("practice.comboType." + preset, comboType);
		prop.setProperty("practice.big." + preset, big);
		prop.setProperty("practice.bigmove." + preset, bigmove);
		prop.setProperty("practice.bighalf." + preset, bighalf);
		prop.setProperty("practice.leveltype." + preset, leveltype);
		prop.setProperty("practice.lvstopse." + preset, lvstopse);
		prop.setProperty("practice.goallv." + preset, goallv);
		prop.setProperty("practice.timelimit." + preset, timelimit);
		prop.setProperty("practice.rolltimelimit." + preset, rolltimelimit);
		for (int i = 0; i < Piece.PIECE_COUNT; i++) {
			prop.setProperty("practice.pieceEnable." + i + "." + preset, pieceEnable[i]);
		}
		prop.setProperty("practice.useMap." + preset, useMap);
		prop.setProperty("practice.timelimitResetEveryLevel." + preset, timelimitResetEveryLevel);
		prop.setProperty("practice.bone." + preset, bone);
		prop.setProperty("practice.blockHidden." + preset, blockHidden);
		prop.setProperty("practice.blockHiddenAnim." + preset, blockHiddenAnim);
		prop.setProperty("practice.blockOutlineType." + preset, blockOutlineType);
		prop.setProperty("practice.blockShowOutlineOnly." + preset, blockShowOutlineOnly);
		prop.setProperty("practice.heboHiddenLevel." + preset, heboHiddenLevel);
	}

	/**
	 * MapRead
	 *
	 * @param field  field
	 * @param prop   Property file to read from
	 * @param preset AnyID
	 */
	private void loadMap(Field field, CustomProperties prop, int id) {
		field.reset();
		field.readProperty(prop, id);
		field.setAllAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
		field.setAllAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
		field.setAllAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, false);
	}

	/**
	 * MapSave
	 *
	 * @param field field
	 * @param prop  Property file to save to
	 * @param id    AnyID
	 */
	private void saveMap(Field field, CustomProperties prop, int id) {
		field.writeProperty(prop, id);
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		if (engine.owner.replayMode == false) {
			owner.menuOnly = true;

			// Configuration changes
			int change = updateCursor(engine, 45);

			if (change != 0) {
				engine.playSE("change");

				int m = 1;
				if (engine.ctrl.isPress(Controller.BUTTON_E)) {
					m = 100;
				}
				if (engine.ctrl.isPress(Controller.BUTTON_F)) {
					m = 1000;
				}

				switch (menuCursor) {
				case 0:
					engine.speed.gravity += change * m;
					if (engine.speed.gravity < -1) {
						engine.speed.gravity = 99999;
					}
					if (engine.speed.gravity > 99999) {
						engine.speed.gravity = -1;
					}
					break;
				case 1:
					engine.speed.denominator += change * m;
					if (engine.speed.denominator < -1) {
						engine.speed.denominator = 99999;
					}
					if (engine.speed.denominator > 99999) {
						engine.speed.denominator = -1;
					}
					break;
				case 2:
					engine.speed.are += change;
					if (engine.speed.are < 0) {
						engine.speed.are = 99;
					}
					if (engine.speed.are > 99) {
						engine.speed.are = 0;
					}
					break;
				case 3:
					engine.speed.areLine += change;
					if (engine.speed.areLine < 0) {
						engine.speed.areLine = 99;
					}
					if (engine.speed.areLine > 99) {
						engine.speed.areLine = 0;
					}
					break;
				case 4:
					engine.speed.lineDelay += change;
					if (engine.speed.lineDelay < 0) {
						engine.speed.lineDelay = 99;
					}
					if (engine.speed.lineDelay > 99) {
						engine.speed.lineDelay = 0;
					}
					break;
				case 5:
					engine.speed.lockDelay += change;
					if (engine.speed.lockDelay < 0) {
						engine.speed.lockDelay = 99;
					}
					if (engine.speed.lockDelay > 99) {
						engine.speed.lockDelay = 0;
					}
					break;
				case 6:
					engine.speed.das += change;
					if (engine.speed.das < 0) {
						engine.speed.das = 99;
					}
					if (engine.speed.das > 99) {
						engine.speed.das = 0;
					}
					break;
				case 7:
					bgmno += change;
					if (bgmno < 0) {
						bgmno = BGMusicStatus.BGM_COUNT - 1;
					}
					if (bgmno > BGMusicStatus.BGM_COUNT - 1) {
						bgmno = 0;
					}
					break;
				case 8:
					big = !big;
					break;
				case 9:
					leveltype += change;
					if (leveltype < 0) {
						leveltype = LEVELTYPE_MAX - 1;
					}
					if (leveltype > LEVELTYPE_MAX - 1) {
						leveltype = 0;
					}
					break;
				case 10:
					// enableTSpin = !enableTSpin;
					tspinEnableType += change;
					if (tspinEnableType < 0) {
						tspinEnableType = 2;
					}
					if (tspinEnableType > 2) {
						tspinEnableType = 0;
					}
					break;
				case 11:
					enableTSpinKick = !enableTSpinKick;
					break;
				case 12:
					spinCheckType += change;
					if (spinCheckType < 0) {
						spinCheckType = 1;
					}
					if (spinCheckType > 1) {
						spinCheckType = 0;
					}
					break;
				case 13:
					tspinEnableEZ = !tspinEnableEZ;
					break;
				case 14:
					enableB2B = !enableB2B;
					break;
				case 15:
					comboType += change;
					if (comboType < 0) {
						comboType = 2;
					}
					if (comboType > 2) {
						comboType = 0;
					}
					break;
				case 16:
					lvstopse = !lvstopse;
					break;
				case 17:
					bigmove = !bigmove;
					break;
				case 18:
					bighalf = !bighalf;
					break;
				case 19:
					goallv += change * m;
					if (goallv < -1) {
						goallv = 9999;
					}
					if (goallv > 9999) {
						goallv = -1;
					}
					break;
				case 20:
					timelimit += change * 60 * m;
					if (timelimit < 0) {
						timelimit = 3600 * 20;
					}
					if (timelimit > 3600 * 20) {
						timelimit = 0;
					}
					break;
				case 21:
					rolltimelimit += change * 60 * m;
					if (rolltimelimit < 0) {
						rolltimelimit = 3600 * 20;
					}
					if (rolltimelimit > 3600 * 20) {
						rolltimelimit = 0;
					}
					break;
				case 22:
					timelimitResetEveryLevel = !timelimitResetEveryLevel;
					break;
				case 23:
					bone = !bone;
					break;
				case 24:
					blockHidden += change * m;
					if (blockHidden < -2) {
						blockHidden = 9999;
					}
					if (blockHidden > 9999) {
						blockHidden = -2;
					}
					break;
				case 25:
					blockHiddenAnim = !blockHiddenAnim;
					break;
				case 26:
					blockOutlineType += change;
					if (blockOutlineType < 0) {
						blockOutlineType = 3;
					}
					if (blockOutlineType > 3) {
						blockOutlineType = 0;
					}
					break;
				case 27:
					blockShowOutlineOnly = !blockShowOutlineOnly;
					break;
				case 28:
					heboHiddenLevel += change;
					if (heboHiddenLevel < 0) {
						heboHiddenLevel = 7;
					}
					if (heboHiddenLevel > 7) {
						heboHiddenLevel = 0;
					}
					break;
				case 29:
					pieceEnable[0] = !pieceEnable[0];
					break;
				case 30:
					pieceEnable[1] = !pieceEnable[1];
					break;
				case 31:
					pieceEnable[2] = !pieceEnable[2];
					break;
				case 32:
					pieceEnable[3] = !pieceEnable[3];
					break;
				case 33:
					pieceEnable[4] = !pieceEnable[4];
					break;
				case 34:
					pieceEnable[5] = !pieceEnable[5];
					break;
				case 35:
					pieceEnable[6] = !pieceEnable[6];
					break;
				case 36:
					pieceEnable[7] = !pieceEnable[7];
					break;
				case 37:
					pieceEnable[8] = !pieceEnable[8];
					break;
				case 38:
					pieceEnable[9] = !pieceEnable[9];
					break;
				case 39:
					pieceEnable[10] = !pieceEnable[10];
					break;
				case 40:
					useMap = !useMap;
					break;
				case 41:
				case 42:
				case 43:
					mapNumber += change;
					if (mapNumber < 0) {
						mapNumber = 99;
					}
					if (mapNumber > 99) {
						mapNumber = 0;
					}
					break;
				case 44:
				case 45:
					presetNumber += change;
					if (presetNumber < 0) {
						presetNumber = 99;
					}
					if (presetNumber > 99) {
						presetNumber = 0;
					}
					break;
				}
			}

			// 決定
			if (engine.ctrl.isPush(Controller.BUTTON_A) && menuTime >= 5) {
				engine.playSE("decide");

				switch (menuCursor) {
				case 41:
					// fieldエディット
					engine.enterFieldEdit();
					return true;
				case 42: {
					// Map読み込み
					engine.createFieldIfNeeded();
					engine.field.reset();
					CustomProperties prop = CustomProperties.load("config/map/practice/" + mapNumber + ".map");
					if (prop != null) {
						loadMap(engine.field, prop, 0);
						engine.field.setAllSkin(engine.getSkin());
					}
					break;
				}
				case 43:
					// Map保存
					if (engine.field != null) {
						CustomProperties prop = new CustomProperties();
						saveMap(engine.field, prop, 0);
						prop.saveSilent("config/map/practice/" + mapNumber + ".map");
					}
					break;
				case 44:
					// Preset読み込み
					loadPreset(engine, owner.modeConfig, presetNumber);
					break;
				case 45:
					// Preset保存
					savePreset(engine, owner.modeConfig, presetNumber);
					GeneralUtil.saveModeConfig(owner.modeConfig);
					break;
				default:
					// Start game
					owner.modeConfig.setProperty("practice.presetNumber", presetNumber);
					owner.modeConfig.setProperty("practice.mapNumber", mapNumber);
					savePreset(engine, owner.modeConfig, -1);
					GeneralUtil.saveModeConfig(owner.modeConfig);
					if (useMap && (engine.field == null || engine.field.isEmpty())) {
						CustomProperties prop = CustomProperties.load("config/map/practice/" + mapNumber + ".map");
						if (prop != null) {
							engine.createFieldIfNeeded();
							loadMap(engine.field, prop, 0);
							engine.field.setAllSkin(engine.getSkin());
						} else {
							useMap = false;
						}
					}
					owner.menuOnly = false;
					return false;
				}
			}

			// Cancel
			if (engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
			}

			menuTime++;
		} else {
			owner.menuOnly = true;

			menuTime++;
			menuCursor = 0;

			if (menuTime >= 60) {
				menuCursor = 22;
			}
			if (menuTime >= 120 || engine.ctrl.isPush(Controller.BUTTON_F)) {
				owner.menuOnly = false;
				return false;
			}
		}

		return true;
	}

	/*
	 * Setting screen drawing
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		renderer.drawMenuFont(engine, playerID, 1, 1, "PRACTICE MODE SETTINGS", Colors.FONT_ORANGE);

		if (engine.owner.replayMode == false) {
			renderer.drawMenuFont(engine, playerID, 1, 27, "A:START B:EXIT C+<>:FAST CHANGE", Colors.FONT_CYAN);
		} else {
			renderer.drawMenuFont(engine, playerID, 1, 27, "F:SKIP", Colors.FONT_RED);
		}

		if (menuCursor < 23) {
			if (owner.replayMode == false) {
				renderer.drawMenuFont(engine, playerID, 1, menuCursor + 3, "b", Colors.FONT_RED);
			}

			renderer.drawMenuFont(engine, playerID, 2, 3, "GRAVITY:" + engine.speed.gravity, menuCursor == 0);
			renderer.drawMenuFont(engine, playerID, 2, 4, "G-MAX:" + engine.speed.denominator, menuCursor == 1);
			renderer.drawMenuFont(engine, playerID, 2, 5, "ARE:" + engine.speed.are, menuCursor == 2);
			renderer.drawMenuFont(engine, playerID, 2, 6, "ARE LINE:" + engine.speed.areLine, menuCursor == 3);
			renderer.drawMenuFont(engine, playerID, 2, 7, "LINE DELAY:" + engine.speed.lineDelay, menuCursor == 4);
			renderer.drawMenuFont(engine, playerID, 2, 8, "LOCK DELAY:" + engine.speed.lockDelay, menuCursor == 5);
			renderer.drawMenuFont(engine, playerID, 2, 9, "DAS:" + engine.speed.das, menuCursor == 6);
			renderer.drawMenuFont(engine, playerID, 2, 10, "BGM:" + bgmno, menuCursor == 7);
			renderer.drawMenuFont(engine, playerID, 2, 11, "BIG:" + GeneralUtil.getONorOFF(big), menuCursor == 8);
			renderer.drawMenuFont(engine, playerID, 2, 12, "LEVEL TYPE:" + LEVELTYPE_STRING[leveltype],
					menuCursor == 9);
			String strTSpinEnable = "";
			if (version >= 4) {
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
			renderer.drawMenuFont(engine, playerID, 2, 13, "SPIN BONUS:" + strTSpinEnable, menuCursor == 10);
			renderer.drawMenuFont(engine, playerID, 2, 14, "EZ SPIN:" + GeneralUtil.getONorOFF(enableTSpinKick),
					menuCursor == 11);
			renderer.drawMenuFont(engine, playerID, 2, 15,
					"SPIN TYPE:" + (spinCheckType == 0 ? "4POINT" : "IMMOBILE"), menuCursor == 12);
			renderer.drawMenuFont(engine, playerID, 2, 16, "EZ IMMOBILE:" + GeneralUtil.getONorOFF(tspinEnableEZ),
					menuCursor == 13);
			renderer.drawMenuFont(engine, playerID, 2, 17, "B2B:" + GeneralUtil.getONorOFF(enableB2B),
					menuCursor == 14);
			renderer.drawMenuFont(engine, playerID, 2, 18, "COMBO:" + COMBOTYPE_STRING[comboType], menuCursor == 15);
			renderer.drawMenuFont(engine, playerID, 2, 19, "LEVEL STOP SE:" + GeneralUtil.getONorOFF(lvstopse),
					menuCursor == 16);
			renderer.drawMenuFont(engine, playerID, 2, 20, "BIG MOVE:" + (bigmove ? "2 CELL" : "1 CELL"),
					menuCursor == 17);
			renderer.drawMenuFont(engine, playerID, 2, 21, "BIG HALF:" + GeneralUtil.getONorOFF(bighalf),
					menuCursor == 18);
			String strGoalLv = "ENDLESS";
			if (goallv >= 0) {
				if (leveltype == LEVELTYPE_MANIA || leveltype == LEVELTYPE_MANIAPLUS) {
					strGoalLv = "LV" + String.valueOf((goallv + 1) * 100);
				} else if (leveltype == LEVELTYPE_NONE) {
					strGoalLv = String.valueOf(goallv + 1) + " LINES";
				} else {
					strGoalLv = "LV" + String.valueOf(goallv + 1);
				}
			}
			renderer.drawMenuFont(engine, playerID, 2, 22, "GOAL LEVEL:" + strGoalLv, menuCursor == 19);
			renderer.drawMenuFont(engine, playerID, 2, 23,
					"TIME LIMIT:" + (timelimit == 0 ? "NONE" : GeneralUtil.getTime(timelimit)), menuCursor == 20);
			renderer.drawMenuFont(engine, playerID, 2, 24,
					"ROLL LIMIT:" + (rolltimelimit == 0 ? "NONE" : GeneralUtil.getTime(rolltimelimit)),
					menuCursor == 21);
			renderer.drawMenuFont(engine, playerID, 2, 25,
					"TIME LIMIT RESET EVERY LEVEL:" + GeneralUtil.getONorOFF(timelimitResetEveryLevel),
					menuCursor == 22);
		} else if (menuCursor < 46) {
			if (owner.replayMode == false) {
				renderer.drawMenuFont(engine, playerID, 1, menuCursor - 23 + 3, "b", Colors.FONT_RED);
			}

			renderer.drawMenuFont(engine, playerID, 2, 3, "USE BONE BLOCKS:" + GeneralUtil.getONorOFF(bone),
					menuCursor == 23);
			String strHiddenFrames = "NONE";
			if (blockHidden == -2) {
				strHiddenFrames = "LOCK FLASH (" + engine.ruleopt.lockflash + "F)";
			}
			if (blockHidden >= 0) {
				strHiddenFrames = String.format("%d (%.2f SEC.)", blockHidden, blockHidden / 60f);
			}
			renderer.drawMenuFont(engine, playerID, 2, 4, "BLOCK HIDDEN FRAMES:" + strHiddenFrames, menuCursor == 24);
			renderer.drawMenuFont(engine, playerID, 2, 5,
					"BLOCK HIDDEN ANIM:" + GeneralUtil.getONorOFF(blockHiddenAnim), menuCursor == 25);
			renderer.drawMenuFont(engine, playerID, 2, 6,
					"BLOCK OUTLINE TYPE:" + BLOCK_OUTLINE_TYPE_STRING[blockOutlineType], menuCursor == 26);
			renderer.drawMenuFont(engine, playerID, 2, 7,
					"BLOCK OUTLINE ONLY:" + GeneralUtil.getONorOFF(blockShowOutlineOnly), menuCursor == 27);
			renderer.drawMenuFont(engine, playerID, 2, 8,
					"HEBO HIDDEN:" + (heboHiddenLevel == 0 ? "NONE" : "LV" + heboHiddenLevel), menuCursor == 28);
			renderer.drawMenuFont(engine, playerID, 2, 9, "PIECE I:" + GeneralUtil.getONorOFF(pieceEnable[0]),
					menuCursor == 29);
			renderer.drawMenuFont(engine, playerID, 2, 10, "PIECE L:" + GeneralUtil.getONorOFF(pieceEnable[1]),
					menuCursor == 30);
			renderer.drawMenuFont(engine, playerID, 2, 11, "PIECE O:" + GeneralUtil.getONorOFF(pieceEnable[2]),
					menuCursor == 31);
			renderer.drawMenuFont(engine, playerID, 2, 12, "PIECE Z:" + GeneralUtil.getONorOFF(pieceEnable[3]),
					menuCursor == 32);
			renderer.drawMenuFont(engine, playerID, 2, 13, "PIECE T:" + GeneralUtil.getONorOFF(pieceEnable[4]),
					menuCursor == 33);
			renderer.drawMenuFont(engine, playerID, 2, 14, "PIECE J:" + GeneralUtil.getONorOFF(pieceEnable[5]),
					menuCursor == 34);
			renderer.drawMenuFont(engine, playerID, 2, 15, "PIECE S:" + GeneralUtil.getONorOFF(pieceEnable[6]),
					menuCursor == 35);
			renderer.drawMenuFont(engine, playerID, 2, 16, "PIECE I1:" + GeneralUtil.getONorOFF(pieceEnable[7]),
					menuCursor == 36);
			renderer.drawMenuFont(engine, playerID, 2, 17, "PIECE I2:" + GeneralUtil.getONorOFF(pieceEnable[8]),
					menuCursor == 37);
			renderer.drawMenuFont(engine, playerID, 2, 18, "PIECE I3:" + GeneralUtil.getONorOFF(pieceEnable[9]),
					menuCursor == 38);
			renderer.drawMenuFont(engine, playerID, 2, 19, "PIECE L3:" + GeneralUtil.getONorOFF(pieceEnable[10]),
					menuCursor == 39);
			renderer.drawMenuFont(engine, playerID, 2, 20, "USE MAP:" + GeneralUtil.getONorOFF(useMap),
					menuCursor == 40);
			renderer.drawMenuFont(engine, playerID, 2, 21, "[EDIT FIELD MAP]", menuCursor == 41);
			renderer.drawMenuFont(engine, playerID, 2, 22, "[LOAD FIELD MAP]:" + mapNumber, menuCursor == 42);
			renderer.drawMenuFont(engine, playerID, 2, 23, "[SAVE FIELD MAP]:" + mapNumber, menuCursor == 43);
			renderer.drawMenuFont(engine, playerID, 2, 24, "[LOAD PRESET]:" + presetNumber, menuCursor == 44);
			renderer.drawMenuFont(engine, playerID, 2, 25, "[SAVE PRESET]:" + presetNumber, menuCursor == 45);
		}
	}

	/*
	 * Called for initialization during Ready (before initialization)
	 */
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		if (engine.statc_0() == 0) {
			// timeLimit setting
			if (timelimit > 0) {
				timelimitTimer = timelimit;
			}

			// BoneBlock
			engine.bone = bone;

			// Set the piece that can appear
			if (version >= 1) {
				for (int i = 0; i < Piece.PIECE_COUNT; i++) {
					engine.nextPieceEnable[i] = pieceEnable[i];
				}
			}

			// MapFor storing backup Replay read
			if (version >= 2) {
				if (useMap) {
					if (owner.replayMode) {
						log.debug("Loading map data from replay data");
						engine.createFieldIfNeeded();
						loadMap(engine.field, owner.replayProp, 0);
						engine.field.setAllSkin(engine.getSkin());
					} else {
						log.debug("Backup map data");
						fldBackup = new Field(engine.field);
					}
				} else if (engine.field != null) {
					log.debug("Use no map, reseting field");
					engine.field.reset();
				} else {
					log.debug("Use no map");
				}
			}
		}

		return false;
	}

	/*
	 * ReadyAt the time ofCalled at initialization (Start gameJust before)
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.big = big;
		engine.bigmove = bigmove;
		engine.bighalf = bighalf;

		if (leveltype != LEVELTYPE_MANIA && leveltype != LEVELTYPE_MANIAPLUS) {
			engine.b2bEnable = enableB2B;
			engine.comboType = comboType;
			engine.statistics.levelDispAdd = 1;

			engine.tspinAllowKick = enableTSpinKick;
			if (version >= 4) {
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
		} else {
			engine.tspinEnable = false;
			engine.tspinAllowKick = false;
			engine.b2bEnable = false;
			engine.comboType = GameEngine.COMBO_TYPE_DOUBLE;
			engine.statistics.levelDispAdd = 0;
		}

		if (version >= 5) {
			// Hidden
			if (blockHidden == -2) {
				engine.blockHidden = engine.ruleopt.lockflash;
			} else {
				engine.blockHidden = blockHidden;
			}
			engine.blockHiddenAnim = blockHiddenAnim;
			engine.blockOutlineType = blockOutlineType;
			engine.blockShowOutlineOnly = blockShowOutlineOnly;

			// Hebo Hidden
			setHeboHidden(engine);
		}

		owner.bgmStatus.bgm = bgmno;

		goal = 5 * (engine.statistics.level + 1);

		engine.meterValue = 0;
		engine.meterColor = Colors.METER_COLOR_GREEN;
		setMeter(engine, playerID);
	}

	/**
	 * Set Hebo Hidden params
	 *
	 * @param engine GameEngine
	 */
	private void setHeboHidden(GameEngine engine) {
		if (heboHiddenLevel >= 1) {
			engine.heboHiddenEnable = true;

			if (heboHiddenLevel == 1) {
				engine.heboHiddenYLimit = 15;
				engine.heboHiddenTimerMax = (engine.heboHiddenYNow + 2) * 120;
			}
			if (heboHiddenLevel == 2) {
				engine.heboHiddenYLimit = 17;
				engine.heboHiddenTimerMax = (engine.heboHiddenYNow + 1) * 100;
			}
			if (heboHiddenLevel == 3) {
				engine.heboHiddenYLimit = 19;
				engine.heboHiddenTimerMax = engine.heboHiddenYNow * 60 + 60;
			}
			if (heboHiddenLevel == 4) {
				engine.heboHiddenYLimit = 19;
				engine.heboHiddenTimerMax = engine.heboHiddenYNow * 30 + 45;
			}
			if (heboHiddenLevel == 5) {
				engine.heboHiddenYLimit = 19;
				engine.heboHiddenTimerMax = engine.heboHiddenYNow * 30 + 30;
			}
			if (heboHiddenLevel == 6) {
				engine.heboHiddenYLimit = 19;
				engine.heboHiddenTimerMax = engine.heboHiddenYNow * 2 + 15;
			}
			if (heboHiddenLevel == 7) {
				engine.heboHiddenYLimit = 20;
				engine.heboHiddenTimerMax = engine.heboHiddenYNow + 15;
			}
		} else {
			engine.heboHiddenEnable = false;
		}
	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		renderer.drawScoreFont(engine, playerID, 0, 0, "PRACTICE", Colors.FONT_YELLOW);

		if (engine.stat == GameEngine.Status.FIELDEDIT) {
			// fieldエディットのとき

			// 座標
			renderer.drawScoreFont(engine, playerID, 0, 2, "X POS", Colors.FONT_BLUE);
			renderer.drawScoreFont(engine, playerID, 0, 3, "" + engine.fldeditX);
			renderer.drawScoreFont(engine, playerID, 0, 4, "Y POS", Colors.FONT_BLUE);
			renderer.drawScoreFont(engine, playerID, 0, 5, "" + engine.fldeditY);

			// Put your field-checking algorithm test codes here
			/*
			 * if(engine.field != null) { renderer.drawScoreFont(engine, playerID, 0, 7,
			 * "T-SLOT+LINECLEAR", Colors.COLOR_BLUE); renderer.drawScoreFont(engine,
			 * playerID, 0, 8, "" + engine.field.getTSlotLineClearAll(false));
			 * renderer.drawScoreFont(engine, playerID, 0, 9, "HOLE", Colors.COLOR_BLUE);
			 * renderer.drawScoreFont(engine, playerID, 0, 10, "" +
			 * engine.field.getHowManyHoles()); }
			 */
		} else if (leveltype == LEVELTYPE_MANIA || leveltype == LEVELTYPE_MANIAPLUS) {
			// levelTypesMANIAWhen

			// Score
			renderer.drawScoreFont(engine, playerID, 0, 5, "SCORE", Colors.FONT_BLUE);
			String strScore = String.valueOf(engine.statistics.score);
			if (lastscore > 0 && scgettime < 120) {
				strScore += "(+" + lastscore + ")";
			}
			renderer.drawScoreFont(engine, playerID, 0, 6, strScore);

			// level
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

			// Time
			renderer.drawScoreFont(engine, playerID, 0, 14, "TIME", Colors.FONT_BLUE);
			int time = engine.statistics.time;
			if (timelimit > 0) {
				time = timelimitTimer;
			}
			if (time < 0) {
				time = 0;
			}
			int fontcolor = Colors.FONT_WHITE;
			if (time < 30 * 60 && time > 0 && timelimit > 0) {
				fontcolor = Colors.FONT_YELLOW;
			}
			if (time < 20 * 60 && time > 0 && timelimit > 0) {
				fontcolor = Colors.FONT_ORANGE;
			}
			if (time < 10 * 60 && time > 0 && timelimit > 0) {
				fontcolor = Colors.FONT_RED;
			}
			renderer.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(time), fontcolor);

			// Roll Rest time
			if (engine.gameActive && engine.ending == 2) {
				int remainTime = rolltimelimit - rolltime;
				if (remainTime < 0) {
					remainTime = 0;
				}
				renderer.drawScoreFont(engine, playerID, 0, 17, "ROLL TIME", Colors.FONT_BLUE);
				renderer.drawScoreFont(engine, playerID, 0, 18, GeneralUtil.getTime(remainTime),
						remainTime > 0 && remainTime < 10 * 60);
			}
		} else {
			// levelTypesMANIAAt other times

			// Score
			renderer.drawScoreFont(engine, playerID, 0, 2, "SCORE", Colors.FONT_BLUE);
			String strScore = String.valueOf(engine.statistics.score);
			if (lastscore > 0 && scgettime < 120) {
				strScore += "(+" + lastscore + ")";
			}
			renderer.drawScoreFont(engine, playerID, 0, 3, strScore);

			if (leveltype == LEVELTYPE_POINTS) {
				// ゴール
				renderer.drawScoreFont(engine, playerID, 0, 5, "GOAL", Colors.FONT_BLUE);
				String strGoal = String.valueOf(goal);
				if (lastgoal != 0 && scgettime < 120 && engine.ending == 0) {
					strGoal += "(-" + String.valueOf(lastgoal) + ")";
				}
				renderer.drawScoreFont(engine, playerID, 0, 6, strGoal);
			} else if (leveltype == LEVELTYPE_10LINES) {
				// Lines( levelタイプが10LINESのとき)
				renderer.drawScoreFont(engine, playerID, 0, 5, "LINE", Colors.FONT_BLUE);
				renderer.drawScoreFont(engine, playerID, 0, 6,
						engine.statistics.lines + "/" + (engine.statistics.level + 1) * 10);
			} else {
				// Lines( levelタイプがNONEのとき)
				renderer.drawScoreFont(engine, playerID, 0, 5, "LINE", Colors.FONT_BLUE);
				renderer.drawScoreFont(engine, playerID, 0, 6, String.valueOf(engine.statistics.lines));
			}

			// level
			if (leveltype != LEVELTYPE_NONE) {
				renderer.drawScoreFont(engine, playerID, 0, 8, "LEVEL", Colors.FONT_BLUE);
				renderer.drawScoreFont(engine, playerID, 0, 9, String.valueOf(engine.statistics.level + 1));
			}

			// 1分間あたり score
			renderer.drawScoreFont(engine, playerID, 0, 11, "SCORE/MIN", Colors.FONT_BLUE);
			renderer.drawScoreFont(engine, playerID, 0, 12, String.format("%-10g", engine.statistics.spm));

			// 1分間あたりのLines
			renderer.drawScoreFont(engine, playerID, 0, 14, "LINE/MIN", Colors.FONT_BLUE);
			renderer.drawScoreFont(engine, playerID, 0, 15, String.valueOf(engine.statistics.lpm));

			// Time
			renderer.drawScoreFont(engine, playerID, 0, 17, "TIME", Colors.FONT_BLUE);
			int time = engine.statistics.time;
			if (timelimit > 0) {
				time = timelimitTimer;
			}
			if (time < 0) {
				time = 0;
			}
			int fontcolor = Colors.FONT_WHITE;
			if (time < 30 * 60 && time > 0 && timelimit > 0) {
				fontcolor = Colors.FONT_YELLOW;
			}
			if (time < 20 * 60 && time > 0 && timelimit > 0) {
				fontcolor = Colors.FONT_ORANGE;
			}
			if (time < 10 * 60 && time > 0 && timelimit > 0) {
				fontcolor = Colors.FONT_RED;
			}
			renderer.drawScoreFont(engine, playerID, 0, 18, GeneralUtil.getTime(time), fontcolor);

			// Roll Rest time
			if (engine.gameActive && engine.ending == 2) {
				int remainTime = rolltimelimit - rolltime;
				if (remainTime < 0) {
					remainTime = 0;
				}
				renderer.drawScoreFont(engine, playerID, 0, 20, "ROLL TIME", Colors.FONT_BLUE);
				renderer.drawScoreFont(engine, playerID, 0, 21, GeneralUtil.getTime(remainTime),
						remainTime > 0 && remainTime < 10 * 60);
			}

			// Line clear event
			if (lastevent != EVENT_NONE && scgettime < 120) {
				String strPieceName = Piece.getPieceName(lastpiece);

				switch (lastevent) {
				case EVENT_SINGLE:
					renderer.drawMenuFont(engine, playerID, 2, 21, "SINGLE", Colors.FONT_DARKBLUE);
					break;
				case EVENT_DOUBLE:
					renderer.drawMenuFont(engine, playerID, 2, 21, "DOUBLE", Colors.FONT_BLUE);
					break;
				case EVENT_TRIPLE:
					renderer.drawMenuFont(engine, playerID, 2, 21, "TRIPLE", Colors.FONT_GREEN);
					break;
				case EVENT_FOUR:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 3, 21, "FOUR", Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 3, 21, "FOUR", Colors.FONT_ORANGE);
					}
					break;
				case EVENT_TSPIN_ZERO_MINI:
					renderer.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", Colors.FONT_PURPLE);
					break;
				case EVENT_TSPIN_ZERO:
					renderer.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", Colors.FONT_PINK);
					break;
				case EVENT_TSPIN_SINGLE_MINI:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S",
								Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S",
								Colors.FONT_ORANGE);
					}
					break;
				case EVENT_TSPIN_SINGLE:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE",
								Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE",
								Colors.FONT_ORANGE);
					}
					break;
				case EVENT_TSPIN_DOUBLE_MINI:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D",
								Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D",
								Colors.FONT_ORANGE);
					}
					break;
				case EVENT_TSPIN_DOUBLE:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE",
								Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE",
								Colors.FONT_ORANGE);
					}
					break;
				case EVENT_TSPIN_TRIPLE:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE",
								Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE",
								Colors.FONT_ORANGE);
					}
					break;
				case EVENT_TSPIN_EZ:
					if (lastb2b) {
						renderer.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, Colors.FONT_RED);
					} else {
						renderer.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName,
								Colors.FONT_ORANGE);
					}
					break;
				}

				if (lastcombo >= 2 && lastevent != EVENT_TSPIN_ZERO_MINI && lastevent != EVENT_TSPIN_ZERO) {
					renderer.drawMenuFont(engine, playerID, 2, 22, lastcombo - 1 + "COMBO", Colors.FONT_CYAN);
				}
			}
		}
	}

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		scgettime++;

		if (engine.gameActive && engine.timerActive) {
			// Hebo Hidden
			setHeboHidden(engine);
		}

		if (engine.gameActive && engine.ending == 2) {
			// EndingMedium
			rolltime++;

			// Roll End
			if (rolltime >= rolltimelimit) {
				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.Status.EXCELLENT;
			}
		} else {
			if (timelimitTimer > 0 && engine.timerActive) {
				timelimitTimer--;
			}

			// Out of time
			if (timelimit > 0 && timelimitTimer <= 0 && engine.timerActive) {
				engine.gameEnded();
				engine.timerActive = false;
				engine.resetStatc();
				if (goallv == -1) {
					engine.stat = GameEngine.Status.ENDINGSTART;
				} else {
					engine.stat = GameEngine.Status.GAMEOVER;
				}
			}

			// 10Seconds before the countdown
			if (timelimit > 0 && timelimitTimer <= 10 * 60 && timelimitTimer % 60 == 0
					&& engine.timerActive) {
				engine.playSE("countdown");
			}

			// 5Of seconds beforeBGM fadeout
			if (timelimit > 0 && timelimitTimer <= 5 * 60 && timelimitResetEveryLevel == false
					&& engine.timerActive) {
				owner.bgmStatus.fadesw = true;
			}
		}

		// Update meter
		setMeter(engine, playerID);
	}

	/*
	 * Called at game over
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		if (engine.statc_0() == 0 && engine.gameActive) {
			secretGrade = engine.field.getSecretGrade();
		}
		return false;
	}

	/*
	 * Processing on the move
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// Occurrence new piece
		if (leveltype == LEVELTYPE_MANIA || leveltype == LEVELTYPE_MANIAPLUS) {
			if (engine.ending == 0 && engine.statc_0() == 0 && engine.holdDisable == false && !lvupflag) {
				// Level up
				if (engine.statistics.level < nextseclv - 1) {
					engine.statistics.level++;
					if (engine.statistics.level == nextseclv - 1 && lvstopse) {
						engine.playSE("levelstop");
					}
					setMeter(engine, playerID);
				}

				// Hard drop bonusInitialization
				harddropBonus = 0;
			}
			if (engine.ending == 0 && engine.statc_0() > 0 && (version >= 1 || engine.holdDisable == false)) {
				lvupflag = false;
			}
		}

		// EndingStart
		if (engine.ending == 2 && rollstarted == false) {
			rollstarted = true;

			if (leveltype == LEVELTYPE_MANIA || leveltype == LEVELTYPE_MANIAPLUS) {
				engine.blockHidden = 300;
				engine.blockHiddenAnim = true;

				if (leveltype == LEVELTYPE_MANIA) {
					engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NONE;
				}
			}

			owner.bgmStatus.bgm = BGMusicStatus.BGM_ENDING1;
		}

		return false;
	}

	/*
	 * AREProcessing during
	 */
	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		// Last frame
		if (leveltype == LEVELTYPE_MANIA || leveltype == LEVELTYPE_MANIAPLUS) {
			if (engine.ending == 0 && engine.statc_0() >= engine.statc_1() - 1 && !lvupflag) {
				if (engine.statistics.level < nextseclv - 1) {
					engine.statistics.level++;
					if (engine.statistics.level == nextseclv - 1 && lvstopse) {
						engine.playSE("levelstop");
					}
					setMeter(engine, playerID);
				}
				lvupflag = true;
			}
		}

		return false;
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Decrease Hebo Hidden
		if (engine.heboHiddenEnable && lines > 0) {
			engine.heboHiddenTimerNow = 0;
			engine.heboHiddenYNow -= lines;
			if (engine.heboHiddenYNow < 0) {
				engine.heboHiddenYNow = 0;
			}
		}

		if (leveltype == LEVELTYPE_MANIA || leveltype == LEVELTYPE_MANIAPLUS) {
			calcScoreMania(engine, playerID, lines);
		} else {
			calcScoreNormal(engine, playerID, lines);
		}
	}

	/**
	 * levelTypesMANIAAt the time ofCalculate score
	 */
	private void calcScoreMania(GameEngine engine, int playerID, int lines) {
		// Combo
		if (lines == 0) {
			comboValue = 1;
		} else {
			comboValue = comboValue + 2 * lines - 2;
			if (comboValue < 1) {
				comboValue = 1;
			}
		}

		if (lines >= 1 && engine.ending == 0) {
			// Level up
			int levelb = engine.statistics.level;

			if (leveltype == LEVELTYPE_MANIA) {
				engine.statistics.level += lines;
			} else {
				int levelplus = lines;
				if (lines == 3) {
					levelplus = 4;
				}
				if (lines >= 4) {
					levelplus = 6;
				}
				engine.statistics.level += levelplus;
			}

			if (engine.statistics.level >= (goallv + 1) * 100 && goallv != -1) {
				// Ending
				engine.statistics.level = (goallv + 1) * 100;
				engine.ending = 1;
				engine.timerActive = false;
				if (rolltimelimit == 0) {
					engine.gameEnded();
					secretGrade = engine.field.getSecretGrade();
				} else {
					engine.staffrollEnable = true;
					engine.staffrollEnableStatistics = false;
					engine.staffrollNoDeath = false;
				}
			} else if (engine.statistics.level >= nextseclv) {
				// Next Section
				engine.playSE("levelup");

				// BackgroundSwitching
				if (owner.backgroundStatus.bg < 19) {
					owner.backgroundStatus.fadesw = true;
					owner.backgroundStatus.fadecount = 0;
					owner.backgroundStatus.fadebg = owner.backgroundStatus.bg + 1;
				}

				// Update level for next section
				nextseclv += 100;

				// Limit timeReset
				if (timelimitResetEveryLevel && timelimit > 0) {
					timelimitTimer = timelimit;
				}
			} else if (engine.statistics.level == nextseclv - 1 && lvstopse) {
				engine.playSE("levelstop");
			}

			// Calculate score
			if (leveltype == LEVELTYPE_MANIA) {
				int manuallock = 0;
				if (engine.manualLock) {
					manuallock = 1;
				}

				int bravo = 1;
				if (engine.field.isEmpty()) {
					bravo = 4;
					engine.playSE("bravo");
				}

				int speedBonus = engine.getLockDelay() - engine.statc_0();
				if (speedBonus < 0) {
					speedBonus = 0;
				}

				lastscore = ((levelb + lines) / 4 + engine.softdropFall + manuallock + harddropBonus) * lines
						* comboValue * bravo + engine.statistics.level / 2 + speedBonus * 7;

				engine.statistics.score += lastscore;
				engine.statistics.scoreFromLineClear += lastscore;
				scgettime = 0;
			} else {
				int manuallock = 0;
				if (engine.manualLock) {
					manuallock = 1;
				}

				int bravo = 1;
				if (engine.field.isEmpty()) {
					bravo = 2;
					engine.playSE("bravo");
				}

				int speedBonus = engine.getLockDelay() - engine.statc_0();
				if (speedBonus < 0) {
					speedBonus = 0;
				}

				lastscore = (((levelb + lines) / 4 + engine.softdropFall + manuallock + harddropBonus) * lines
						* comboValue + speedBonus + engine.statistics.level / 2) * bravo;

				engine.statistics.score += lastscore;
				engine.statistics.scoreFromLineClear += lastscore;
				scgettime = 0;
			}

			setMeter(engine, playerID);
		}
	}

	/**
	 * levelTypesMANIAWhen a non-systemCalculate score
	 */
	private void calcScoreNormal(GameEngine engine, int playerID, int lines) {
		// Line clear bonus
		int pts = 0;
		int cmb = 0;

		if (engine.tspin) {
			// T-Spin 0 lines
			if (lines == 0 && !engine.tspinez) {
				if (engine.tspinmini) {
					pts += 100 * (engine.statistics.level + 1);
					lastevent = EVENT_TSPIN_ZERO_MINI;
				} else {
					pts += 400 * (engine.statistics.level + 1);
					lastevent = EVENT_TSPIN_ZERO;
				}
			}
			// Immobile EZ Spin
			else if (engine.tspinez && lines > 0) {
				if (engine.b2b) {
					pts += 180 * (engine.statistics.level + 1);
				} else {
					pts += 120 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_TSPIN_EZ;
			}
			// T-Spin 1 line
			else if (lines == 1) {
				if (engine.tspinmini) {
					if (engine.b2b) {
						pts += 300 * (engine.statistics.level + 1);
					} else {
						pts += 200 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_SINGLE_MINI;
				} else {
					if (engine.b2b) {
						pts += 1200 * (engine.statistics.level + 1);
					} else {
						pts += 800 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_SINGLE;
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
					lastevent = EVENT_TSPIN_DOUBLE_MINI;
				} else {
					if (engine.b2b) {
						pts += 1800 * (engine.statistics.level + 1);
					} else {
						pts += 1200 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_DOUBLE;
				}
			}
			// T-Spin 3 lines
			else if (lines >= 3) {
				if (engine.b2b) {
					pts += 2400 * (engine.statistics.level + 1);
				} else {
					pts += 1600 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_TSPIN_TRIPLE;
			}
		} else {
			switch (lines) {
			case 1:
				pts += 100 * (engine.statistics.level + 1); // 1Column
				lastevent = EVENT_SINGLE;
				break;
			case 2:
				pts += 300 * (engine.statistics.level + 1); // 2Column
				lastevent = EVENT_DOUBLE;
				break;
			case 3:
				pts += 500 * (engine.statistics.level + 1); // 3Column
				lastevent = EVENT_TRIPLE;
				break;
			default:
				if (lines >= 4) {
					// 4 lines
					if (engine.b2b) {
						pts += 1200 * (engine.statistics.level + 1);
					} else {
						pts += 800 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_FOUR;
				}
				break;
			}
		}

		lastb2b = engine.b2b;

		// Combo
		if (engine.combo >= 1 && lines >= 1) {
			cmb += (engine.combo - 1) * 50 * (engine.statistics.level + 1);
			lastcombo = engine.combo;
		}

		// All clear
		if (lines >= 1 && engine.field.isEmpty()) {
			engine.playSE("bravo");
			pts += 1800 * (engine.statistics.level + 1);
		}

		// Add to score
		if (pts > 0 || cmb > 0) {
			lastpiece = engine.nowPieceObject.id;
			lastscore = pts + cmb;
			scgettime = 0;
			if (lines >= 1) {
				engine.statistics.scoreFromLineClear += pts;
			} else {
				engine.statistics.scoreFromOtherBonus += pts;
			}
			engine.statistics.score += pts;

			int cmbindex = engine.combo - 1;
			if (cmbindex < 0) {
				cmbindex = 0;
			}
			if (cmbindex >= COMBO_GOAL_TABLE.length) {
				cmbindex = COMBO_GOAL_TABLE.length - 1;
			}
			lastgoal = pts / 100 / (engine.statistics.level + 1) + COMBO_GOAL_TABLE[cmbindex];
			goal -= lastgoal;
			if (goal <= 0) {
				goal = 0;
			}
		}

		boolean endingFlag = false; // EndingIf the inrushtrue

		if (leveltype == LEVELTYPE_10LINES && engine.statistics.lines >= (engine.statistics.level + 1) * 10
				|| leveltype == LEVELTYPE_POINTS && goal <= 0) {
			if (engine.statistics.level >= goallv && goallv != -1) {
				// Ending
				endingFlag = true;
			} else {
				// Level up
				engine.statistics.level++;

				if (owner.backgroundStatus.bg < 19) {
					owner.backgroundStatus.fadesw = true;
					owner.backgroundStatus.fadecount = 0;
					owner.backgroundStatus.fadebg = owner.backgroundStatus.bg + 1;
				}

				goal = 5 * (engine.statistics.level + 1);

				// Limit timeReset
				if (timelimitResetEveryLevel && timelimit > 0) {
					timelimitTimer = timelimit;
				}

				engine.playSE("levelup");
			}
		}

		// Ending ( levelTypeNONE)
		if (version >= 2 && leveltype == LEVELTYPE_NONE && engine.statistics.lines >= goallv + 1
				&& (goallv != -1 || version <= 2)) {
			endingFlag = true;
		}

		// EndingRush processing
		if (endingFlag) {
			engine.timerActive = false;

			if (rolltimelimit == 0) {
				engine.ending = 1;
				engine.gameEnded();
				secretGrade = engine.field.getSecretGrade();
			} else {
				engine.ending = 2;
				engine.staffrollEnable = true;
				engine.staffrollEnableStatistics = true;
				engine.staffrollNoDeath = true;
			}
		}

		setMeter(engine, playerID);
	}

	/**
	 * MeterUpdate the amount of
	 *
	 * @param engine   GameEngine
	 * @param playerID Player number
	 */
	private void setMeter(GameEngine engine, int playerID) {
		if (engine.gameActive && engine.ending == 2) {
			int remainRollTime = rolltimelimit - rolltime;
			engine.meterValue = remainRollTime * renderer.getMeterMax(engine) / rolltimelimit;
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
		} else if (timelimit > 0) {
			int remainTime = timelimitTimer;
			engine.meterValue = remainTime * renderer.getMeterMax(engine) / timelimit;
			engine.meterColor = Colors.METER_COLOR_GREEN;
			if (remainTime <= 30 * 60) {
				engine.meterColor = Colors.METER_COLOR_YELLOW;
			}
			if (remainTime <= 20 * 60) {
				engine.meterColor = Colors.METER_COLOR_ORANGE;
			}
			if (remainTime <= 10 * 60) {
				engine.meterColor = Colors.METER_COLOR_RED;
			}
		} else {
			switch (leveltype) {
			case LEVELTYPE_10LINES:
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
				break;
			case LEVELTYPE_POINTS:
				engine.meterValue = goal * renderer.getMeterMax(engine) / (5 * (engine.statistics.level + 1));
				engine.meterColor = Colors.METER_COLOR_GREEN;
				if (engine.meterValue <= renderer.getMeterMax(engine) / 2) {
					engine.meterColor = Colors.METER_COLOR_YELLOW;
				}
				if (engine.meterValue <= renderer.getMeterMax(engine) / 3) {
					engine.meterColor = Colors.METER_COLOR_ORANGE;
				}
				if (engine.meterValue <= renderer.getMeterMax(engine) / 4) {
					engine.meterColor = Colors.METER_COLOR_RED;
				}
				break;
			case LEVELTYPE_MANIA:
			case LEVELTYPE_MANIAPLUS:
				engine.meterValue = engine.statistics.level % 100 * renderer.getMeterMax(engine) / 99;
				engine.meterColor = Colors.METER_COLOR_GREEN;
				if (engine.statistics.level % 100 >= 50) {
					engine.meterColor = Colors.METER_COLOR_YELLOW;
				}
				if (engine.statistics.level % 100 >= 80) {
					engine.meterColor = Colors.METER_COLOR_ORANGE;
				}
				if (engine.statistics.level == nextseclv - 1) {
					engine.meterColor = Colors.METER_COLOR_RED;
				}
				break;
			default:
				if (leveltype == LEVELTYPE_NONE && goallv != -1) {
					engine.meterValue = engine.statistics.lines * renderer.getMeterMax(engine) / (goallv + 1);
					engine.meterColor = Colors.METER_COLOR_GREEN;
					if (engine.meterValue >= renderer.getMeterMax(engine) / 10) {
						engine.meterColor = Colors.METER_COLOR_YELLOW;
					}
					if (engine.meterValue >= renderer.getMeterMax(engine) / 5) {
						engine.meterColor = Colors.METER_COLOR_ORANGE;
					}
					if (engine.meterValue >= renderer.getMeterMax(engine) / 2) {
						engine.meterColor = Colors.METER_COLOR_RED;
					}
				}
				break;
			}
		}

		if (engine.meterValue < 0) {
			engine.meterValue = 0;
		}
		if (engine.meterValue > renderer.getMeterMax(engine)) {
			engine.meterValue = renderer.getMeterMax(engine);
		}
	}

	/*
	 * Soft drop
	 */
	@Override
	public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
		if (leveltype != LEVELTYPE_MANIA && leveltype != LEVELTYPE_MANIAPLUS) {
			engine.statistics.scoreFromSoftDrop += fall;
			engine.statistics.score += fall;
		}
	}

	/*
	 * Hard drop
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		if (leveltype == LEVELTYPE_MANIA || leveltype == LEVELTYPE_MANIAPLUS) {
			if (fall * 2 > harddropBonus) {
				harddropBonus = fall * 2;
			}
		} else {
			engine.statistics.scoreFromHardDrop += fall * 2;
			engine.statistics.score += fall * 2;
		}
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		drawResultStats(engine, playerID, renderer, 0, Colors.FONT_BLUE, Statistic.SCORE, Statistic.LINES,
				Statistic.LEVEL_ADD_DISP, Statistic.TIME, Statistic.SPL, Statistic.SPM, Statistic.LPM);
		if (secretGrade > 0) {
			drawResult(engine, playerID, renderer, 14, Colors.FONT_BLUE, "S. GRADE",
					String.format("%10s", tableSecretGradeName[secretGrade - 1]));
		}
	}

	/*
	 * Called when saving replay
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		engine.owner.replayProp.setProperty("practice.version", version);
		if (useMap && fldBackup != null) {
			saveMap(fldBackup, prop, 0);
		}
		savePreset(engine, engine.owner.replayProp, -1);
	}
}

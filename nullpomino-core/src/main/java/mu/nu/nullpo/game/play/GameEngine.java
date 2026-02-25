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
package mu.nu.nullpo.game.play;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.BGMusicStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.ReplayData;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.component.Statistics;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;
import mu.nu.nullpo.game.types.DisplaySize;
import mu.nu.nullpo.game.types.Version;
import mu.nu.nullpo.util.Colors;
import mu.nu.nullpo.util.GeneralUtil;
import mu.nu.nullpo.util.SpinBonus;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.MemorylessRandomizer;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

/**
 * Each player's Game processing
 */
@Log4j
public class GameEngine {

	/** Number of free status counters (used by statc array) */
	public static final int MAX_STATC = 10;

	/** Constants of block outline type */
	public static final int BLOCK_OUTLINE_AUTO = -1;
	public static final int BLOCK_OUTLINE_NONE = 0;
	public static final int BLOCK_OUTLINE_NORMAL = 1;
	public static final int BLOCK_OUTLINE_CONNECT = 2;
	public static final int BLOCK_OUTLINE_SAMECOLOR = 3;

	/** Default duration of Ready->Go */
	private static final int READY_START = 0;
	private static final int READY_END = 49;
	private static final int GO_START = 50;
	private static final int GO_END = 100;

	/** Constants of T-Spin Mini detection type */
	private static final int TSPINMINI_TYPE_ROTATECHECK = 0;
	private static final int TSPINMINI_TYPE_WALLKICKFLAG = 1;

	/** Constants of combo type */
	public static final int COMBO_TYPE_DISABLE = 0;
	public static final int COMBO_TYPE_NORMAL = 1;
	public static final int COMBO_TYPE_DOUBLE = 2;

	/** Constants of gameplay-interruptable items */
	public static final int INTERRUPTITEM_NONE = 0;
	public static final int INTERRUPTITEM_MIRROR = 1;

	/** Line gravity types */
	public enum LineGravity {
		NATIVE, CASCADE, CASCADE_SLOW
	}

	/** Clear mode settings */
	public enum ClearType {
		LINE, COLOR, LINE_COLOR, GEM_COLOR
	}

	/** Constants of main game status */
	public enum Status {
		NOTHING, SETTING, READY, MOVE, LOCKFLASH, LINECLEAR, ARE, ENDINGSTART, CUSTOM, EXCELLENT, GAMEOVER, RESULT,
		FIELDEDIT, INTERRUPTITEM
	}

	/** Constants of last successful movements */
	public enum LastMove {
		NONE, FALL_AUTO, FALL_SELF, SLIDE_AIR, SLIDE_GROUND, ROTATE_AIR, ROTATE_GROUND
	}

	/** Table for color-block item */
	private static final int[] ITEM_COLOR_BRIGHT_TABLE = { 10, 10, 9, 9, 8, 8, 8, 7, 7, 7, 6, 6, 6, 5, 5, 5, 4, 4, 4, 4,
			3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 };

	/** Default list of block colors to use for random block colors. */
	private static final int[] BLOCK_COLORS_DEFAULT = { Colors.BLOCK_COLOR_RED, Colors.BLOCK_COLOR_ORANGE,
			Colors.BLOCK_COLOR_YELLOW, Colors.BLOCK_COLOR_GREEN, Colors.BLOCK_COLOR_CYAN, Colors.BLOCK_COLOR_BLUE,
			Colors.BLOCK_COLOR_PURPLE };

	/** GameManager: Owner of this GameEngine */
	public GameManager owner;

	/** Player ID (0=1P) */
	public int playerID;

	/** RuleOptions: Most game settings are here */
	public RuleOptions ruleopt;

	/** Wallkick: The wallkick system */
	public Wallkick wallkick;

	/** Randomizer: Used by creation of next piece sequence */
	public Randomizer randomizer;

	/** Field: The playfield */
	public Field field;

	/** Controller: You can get player's input from here */
	public Controller ctrl;

	/** Statistics: Various game statistics such as score, number of lines, etc */
	public Statistics statistics;

	/**
	 * SpeedParam: Parameters of game speed (Gravity, ARE, Line clear delay, etc)
	 */
	public SpeedParam speed;

	/**
	 * Gravity counter (The piece falls when this reaches to the value of
	 * speed.denominator)
	 */
	public int gcount;

	/** The first random-seed */
	public long randSeed;

	/** Random: Used for creating various randomness */
	public Random random;

	/** ReplayData: Manages input data for replays */
	public ReplayData replayData;

	/** AIPlayer: AI for auto playing */
	public DummyAI ai;

	/** AI move delay */
	public int aiMoveDelay;

	/** AI think delay (Only when using thread) */
	public int aiThinkDelay;

	/** Use thread for AI */
	public boolean aiUseThread;

	/** Show Hint with AI */
	public boolean aiShowHint;

	/** Prethink with AI */
	public boolean aiPrethink;

	/** Show internal state of AI */
	public boolean aiShowState;

	/** AI Hint piece (copy of current or hold) */
	public Piece aiHintPiece;

	/** True if AI Hint is ready */
	public boolean aiHintReady;

	/** Current main game status */
	public Status stat;

	/** Free status counters */
	protected int[] statc;

	/** true if game play, false if menu. Used for alternate keyboard mappings. */
	public boolean isInGame;

	/** true if the game is active */
	public boolean gameActive;

	/** true if the timer is active */
	public boolean timerActive;

	/**
	 * true if the game is started (It will not change back to false until the game
	 * is reset)
	 */
	public boolean gameStarted;

	/** Timer for replay */
	public int replayTimer;

	/** Time of game start in milliseconds */
	public long startTime;

	/** Time of game end in milliseconds */
	public long endTime;

	private Version version;

	/** Game quit flag */
	public boolean quitflag;

	/** Piece object of current piece */
	public Piece nowPieceObject;

	/** X coord of current piece */
	public int nowPieceX;

	/** Y coord of current piece */
	public int nowPieceY;

	/** Bottommost Y coord of current piece (Used for ghost piece and harddrop) */
	public int nowPieceBottomY;

	/** Write anything other than -1 to override whole current piece color */
	public int nowPieceColorOverride;

	/** Allow/Disallow certain piece */
	public boolean[] nextPieceEnable;

	/**
	 * Preferred size of next piece array. Might be ignored by certain Randomizer.
	 * (Default:1400)
	 */
	public int nextPieceArraySize;

	/** Array of next piece IDs */
	public int[] nextPieceIDs;

	/** Array of next piece Objects */
	public Piece[] nextPieces;

	/** Number of pieces put (Used by next piece sequence) */
	public int nextPieceCount;

	/** Hold piece (null: None) */
	public Piece holdPieceObject;

	/** true if hold is disabled because player used it already */
	public boolean holdDisable;

	/** Number of holds used */
	public int holdUsedCount;

	/** Number of lines currently clearing */
	public int lineClearing;

	/** Line gravity type (Native, Cascade, etc) */
	public LineGravity lineGravityType;

	/** Current number of chains */
	public int chain;

	/** Number of lines cleared for this chains */
	public int lineGravityTotalLines;

	/** Lock delay counter */
	public int lockDelayNow;

	/** DAS counter */
	public int dasCount;

	/** DAS direction (-1:Left 0:None 1:Right) */
	public int dasDirection;

	/** DAS delay counter */
	public int dasSpeedCount;

	/** Repeat statMove() for instant DAS */
	public boolean dasRepeat;

	/** In the middle of an instant DAS loop */
	public boolean dasInstant;

	/** Disallow shift while locking key is pressed */
	public int shiftLock;

	/** IRS direction */
	public int initialRotateDirection;

	/** Last IRS direction */
	public int initialRotateLastDirection;

	/** IRS continuous use flag */
	public boolean initialRotateContinuousUse;

	/** IHS */
	public boolean initialHoldFlag;

	/** IHS continuous use flag */
	public boolean initialHoldContinuousUse;

	/** Number of current piece movement */
	public int nowPieceMoveCount;

	/** Number of current piece rotations */
	public int nowPieceRotateCount;

	/** Number of current piece failed rotations */
	public int nowPieceRotateFailCount;

	/** Number of movement while touching to the floor */
	public int extendedMoveCount;

	/** Number of rotations while touching to the floor */
	public int extendedRotateCount;

	/** Number of wallkicks used by current piece */
	public int nowWallkickCount;

	/** Number of upward wallkicks used by current piece */
	public int nowUpwardWallkickCount;

	/** Number of rows falled by soft drop (Used by soft drop bonuses) */
	public int softdropFall;

	/** Number of rows falled by hard drop (Used by soft drop bonuses) */
	public int harddropFall;

	/** Soft drop continuous use flag */
	public boolean softdropContinuousUse;

	/** Hard drop continuous use flag */
	public boolean harddropContinuousUse;

	/** true if the piece was manually locked by player */
	public boolean manualLock;

	/** Last successful movement */
	public LastMove lastmove;

	/** ture if T-Spin */
	public boolean tspin;

	/** true if T-Spin Mini */
	public boolean tspinmini;

	/** EZ T-spin */
	public boolean tspinez;

	/** true if B2B */
	public boolean b2b;

	/** B2B counter */
	public int b2bcount;

	/** Number of combos */
	public int combo;

	/** T-Spin enable flag */
	public boolean tspinEnable;

	/** EZ-T toggle */
	public boolean tspinEnableEZ;

	/** Allow T-Spin with wallkicks */
	public boolean tspinAllowKick;

	/** T-Spin Mini detection type */
	public int tspinminiType;

	/** Spin detection type */
	public int spinCheckType;

	/** All Spins flag */
	public boolean useAllSpinBonus;

	/** B2B enable flag */
	public boolean b2bEnable;

	/** Combo type */
	public int comboType;

	/** Number of frames before placed blocks disappear (-1:Disable) */
	public int blockHidden;

	/** Use alpha-blending for blockHidden */
	public boolean blockHiddenAnim;

	/** Outline type */
	public int blockOutlineType;

	/**
	 * Show outline only flag. If enabled it does not show actual image of blocks.
	 */
	public boolean blockShowOutlineOnly;

	/** Hebo-hidden Enable flag */
	public boolean heboHiddenEnable;

	/** Hebo-hidden Timer */
	public int heboHiddenTimerNow;

	/** Hebo-hidden Timer Max */
	public int heboHiddenTimerMax;

	/** Hebo-hidden Y coord */
	public int heboHiddenYNow;

	/** Hebo-hidden Y coord Limit */
	public int heboHiddenYLimit;

	/** Set when ARE or line delay is canceled */
	public boolean delayCancel;

	/** Piece must move left after canceled delay */
	public boolean delayCancelMoveLeft;

	/** Piece must move right after canceled delay */
	public boolean delayCancelMoveRight;

	/** Use bone blocks [][][][] */
	public boolean bone;

	/** Big blocks */
	public boolean big;

	/** Big movement type (false:1cell true:2cell) */
	public boolean bigmove;

	/** Halves the amount of lines cleared in Big mode */
	public boolean bighalf;

	/** true if wallkick is used */
	public boolean kickused;

	/** Field size (-1:Default) */
	public int fieldWidth, fieldHeight, fieldHiddenHeight;

	/** Ending mode (0:During the normal game) */
	public int ending;

	/** Enable staffroll challenge (Credits) in ending */
	public boolean staffrollEnable;

	/** Disable death in staffroll challenge */
	public boolean staffrollNoDeath;

	/** Update various statistics in staffroll challenge */
	public boolean staffrollEnableStatistics;

	/** Frame color */
	public int framecolor;

	/** Duration of Ready->Go */
	public int readyStart, readyEnd, goStart, goEnd;

	/** true if Ready->Go is already done */
	public boolean readyDone;

	/** Number of lives */
	public int lives;

	/** Ghost piece flag */
	public boolean ghost;

	/** Amount of meter */
	public int meterValue;

	/** Color of meter */
	public int meterColor;

	/** Amount of meter (layer 2) */
	public int meterValueSub;

	/** Color of meter (layer 2) */
	public int meterColorSub;

	/**
	 * Lag flag (Infinite length of ARE will happen after placing a piece until this
	 * flag is set to false)
	 */
	private boolean lagARE;

	/** Lag flag (Pause the game completely) */
	private boolean lagStop;

	/** Field display size */
	public DisplaySize displaySize;

	/** Sound effects enable flag */
	public boolean enableSE;

	/** Stops all other players when this player dies */
	public boolean gameoverAll;

	/** Field visible flag (false for invisible challenge) */
	public boolean isVisible;

	/** Piece preview visible flag */
	public boolean isNextVisible;

	/** Hold piece visible flag */
	public boolean isHoldVisible;

	/** Field edit screen: Cursor coord */
	public int fldeditX, fldeditY;

	/** Field edit screen: Selected color */
	public int fldeditColor;

	/** Field edit screen: Previous game status number */
	public Status fldeditPreviousStat;

	/** Field edit screen: Frame counter */
	public int fldeditFrames;

	/** Next-skip during Ready->Go */
	public boolean holdButtonNextSkip;

	/** Allow default text rendering (such as "READY", "GO!", "GAME OVER", etc) */
	public boolean allowTextRenderByReceiver;

	/** RollRoll (Auto rotation) enable flag */
	public boolean itemRollRollEnable;

	/** RollRoll (Auto rotation) interval */
	public int itemRollRollInterval;

	/** X-RAY enable flag */
	public boolean itemXRayEnable;

	/** X-RAY counter */
	private int itemXRayCount;

	/** Color-block enable flag */
	public boolean itemColorEnable;

	/** Color-block counter */
	public int itemColorCount;

	/** Gameplay-interruptable item */
	public int interruptItemNumber;

	/** Post-status of interruptable item */
	public Status interruptItemPreviousStat;

	/** Backup field for Mirror item */
	public Field interruptItemMirrorField;

	/** A button direction -1=Auto(Use rule settings) 0=Left 1=Right */
	public int owRotateButtonDefaultRight;

	/** Block Skin (-1=Auto 0orAbove=Fixed) */
	public int owSkin;

	/** Min/Max DAS (-1=Auto 0orAbove=Fixed) */
	public int owMinDAS, owMaxDAS;

	/** DAS delay (-1=Auto 0orAbove=Fixed) */
	public int owDasDelay;

	/** Reverse roles of up/down keys in-game */
	public boolean owReverseUpDown;

	/** Diagonal move (-1=Auto 0=Disable 1=Enable) */
	public int owMoveDiagonal;

	/** Outline type (-1:Auto 0orAbove:Fixed) */
	public int owBlockOutlineType;

	/** Show outline only flag (-1:Auto 0:Always Normal 1:Always Outline Only) */
	public int owBlockShowOutlineOnly;

	/** Clear mode selection */
	public ClearType clearMode;

	/** Size needed for a color-group clear */
	public int colorClearSize;

	/** If true, color clears will also clear adjacent garbage blocks. */
	public boolean garbageColorClear;

	/** If true, each individual block is a random color. */
	public boolean randomBlockColor;

	/** If true, block in pieces are connected. */
	public boolean connectBlocks;

	/** List of block colors to use for random block colors. */
	public int[] blockColors;

	/** Number of colors in blockColors to use. */
	public int numColors;

	/**
	 * If true, line color clears can be diagonal.
	 *
	 * @deprecated unused variable
	 */
	@Deprecated(forRemoval = true)
	private boolean lineColorDiagonals;

	/**
	 * If true, gems count as the same color as their respectively-colored normal
	 * blocks
	 */
	public boolean gemSameColor;

	/** Delay for each step in cascade animations */
	public int cascadeDelay;

	/** Delay between landing and checking for clears in cascade */
	public int cascadeClearDelay;

	/** If true, color clears will ignore hidden rows */
	public boolean ignoreHidden;

	/** Set to true to process rainbow block effects, false to skip. */
	public boolean rainbowAnimate;

	/**
	 * If true, the game will execute double rotation to I2 piece when regular
	 * rotation fails twice
	 */
	public boolean dominoQuickTurn;

	/**
	 * 0 = default, 1 = link by color, 2 = link by color but ignore links for
	 * cascade (Avalanche)
	 */
	public int sticky;

	/**
	 * Constructor
	 *
	 * @param owner    Own the game engineGameOwnerClass
	 * @param playerID PlayerOf number
	 */
	public GameEngine(GameManager owner, int playerID) {
		this.owner = owner;
		this.playerID = playerID;
		ruleopt = new RuleOptions();
		wallkick = null;
		randomizer = null;

		owRotateButtonDefaultRight = -1;
		owSkin = -1;
		owMinDAS = -1;
		owMaxDAS = -1;
		owDasDelay = -1;
		owReverseUpDown = false;
		owMoveDiagonal = -1;
		owBlockOutlineType = -1;
		owBlockShowOutlineOnly = -1;
	}

	/**
	 * With parameters such as the rule setConstructor
	 *
	 * @param owner      Own the game engineGameOwnerClass
	 * @param playerID   PlayerOf number
	 * @param ruleopt    Rule Set
	 * @param wallkick   WallkickSystem
	 * @param randomizer BlockGeneration algorithm of the order of appearance of the
	 *                   piece
	 */
	public GameEngine(GameManager owner, int playerID, RuleOptions ruleopt, Wallkick wallkick, Randomizer randomizer) {
		this(owner, playerID);
		this.ruleopt = ruleopt;
		this.wallkick = wallkick;
		this.randomizer = randomizer;
	}

	/**
	 * READYPreviousInitialization
	 */
	public void init() {
		// log.debug("GameEngine init() playerID:" + playerID);

		field = null;
		ctrl = new Controller();
		statistics = new Statistics();
		speed = new SpeedParam();
		gcount = 0;
		replayData = new ReplayData();

		if (!owner.replayMode) {
			version = Version.getCurrent();
			Random tempRand = new Random();
			randSeed = tempRand.nextLong();
			log.debug("Player + " + playerID + "Random seed :" + Long.toString(randSeed, 16));
			random = new Random(randSeed);
		} else {
			String replayVersion;
			if (owner.replayProp.containsKey("version")) {
				replayVersion = owner.replayProp.getProperty("version");
				log.debug("using new replay version mechanism with: " + replayVersion);
			} else {
				replayVersion = owner.replayProp.getProperty("version.core.major", "0");
				replayVersion += "." + owner.replayProp.getProperty("version.core.minor", "0");
				if (owner.replayProp.getProperty("version.core.dev", false)) {
					replayVersion += "D";
				}
				log.debug("using old rpleay version mechanism with: " + replayVersion);
			}
			version = Version.of(replayVersion);

			replayData.readProperty(owner.replayProp, playerID);

			String tempRand = owner.replayProp.getProperty(playerID + ".replay.randSeed", "0");
			randSeed = Long.parseLong(tempRand, 16);
			random = new Random(randSeed);

			owRotateButtonDefaultRight = owner.replayProp.getProperty(playerID + ".tuning.owRotateButtonDefaultRight",
					-1);
			owSkin = owner.replayProp.getProperty(playerID + ".tuning.owSkin", -1);
			owMinDAS = owner.replayProp.getProperty(playerID + ".tuning.owMinDAS", -1);
			owMaxDAS = owner.replayProp.getProperty(playerID + ".tuning.owMaxDAS", -1);
			owDasDelay = owner.replayProp.getProperty(playerID + ".tuning.owDasDelay", -1);
			owReverseUpDown = owner.replayProp.getProperty(playerID + ".tuning.owReverseUpDown", false);
			owMoveDiagonal = owner.replayProp.getProperty(playerID + ".tuning.owMoveDiagonal", -1);
			owBlockOutlineType = owner.replayProp.getProperty(playerID + ".tuning.owBlockOutlineType", -1);
			owBlockShowOutlineOnly = owner.replayProp.getProperty(playerID + ".tuning.owBlockShowOutlineOnly", -1);

			// Fixing old replays to accomodate for new DAS notation
			if (version.isLower(7, 3, 0)) {
				if (owDasDelay >= 0) {
					owDasDelay++;
				} else {
					owDasDelay = owner.replayProp.getProperty(playerID + ".ruleopt.dasDelay", 0) + 1;
				}
			}
		}

		quitflag = false;

		stat = Status.SETTING;
		statc = new int[MAX_STATC];

		isInGame = false;
		gameActive = false;
		timerActive = false;
		gameStarted = false;
		replayTimer = 0;

		nowPieceObject = null;
		nowPieceX = 0;
		nowPieceY = 0;
		nowPieceBottomY = 0;
		nowPieceColorOverride = -1;

		nextPieceArraySize = 1400;
		nextPieceEnable = new boolean[Piece.PIECE_COUNT];
		Arrays.fill(nextPieceEnable, 0, Piece.PIECE_STANDARD_COUNT, true);

		nextPieceIDs = null;
		nextPieces = null;
		nextPieceCount = 0;

		holdPieceObject = null;
		holdDisable = false;
		holdUsedCount = 0;

		lineClearing = 0;
		lineGravityType = LineGravity.NATIVE;
		chain = 0;
		lineGravityTotalLines = 0;

		lockDelayNow = 0;

		dasCount = 0;
		dasDirection = 0;
		dasSpeedCount = getDASDelay();
		dasRepeat = false;
		dasInstant = false;
		shiftLock = 0;

		initialRotateDirection = 0;
		initialRotateLastDirection = 0;
		initialHoldFlag = false;
		initialRotateContinuousUse = false;
		initialHoldContinuousUse = false;

		nowPieceMoveCount = 0;
		nowPieceRotateCount = 0;
		nowPieceRotateFailCount = 0;

		extendedMoveCount = 0;
		extendedRotateCount = 0;

		nowWallkickCount = 0;
		nowUpwardWallkickCount = 0;

		softdropFall = 0;
		harddropFall = 0;
		softdropContinuousUse = false;
		harddropContinuousUse = false;

		manualLock = false;

		lastmove = LastMove.NONE;

		tspin = false;
		tspinmini = false;
		tspinez = false;
		b2b = false;
		b2bcount = 0;
		combo = 0;

		tspinEnable = false;
		tspinEnableEZ = false;
		tspinAllowKick = true;
		tspinminiType = TSPINMINI_TYPE_ROTATECHECK;
		spinCheckType = SpinBonus.SPINTYPE_4POINT;
		useAllSpinBonus = false;
		b2bEnable = false;
		comboType = COMBO_TYPE_DISABLE;

		blockHidden = -1;
		blockHiddenAnim = true;
		blockOutlineType = BLOCK_OUTLINE_NORMAL;
		blockShowOutlineOnly = false;

		heboHiddenEnable = false;
		heboHiddenTimerNow = 0;
		heboHiddenTimerMax = 0;
		heboHiddenYNow = 0;
		heboHiddenYLimit = 0;

		delayCancel = false;
		delayCancelMoveLeft = false;
		delayCancelMoveRight = false;

		bone = false;

		big = false;
		bigmove = true;
		bighalf = true;

		kickused = false;

		fieldWidth = -1;
		fieldHeight = -1;
		fieldHiddenHeight = -1;

		ending = 0;
		staffrollEnable = false;
		staffrollNoDeath = false;
		staffrollEnableStatistics = false;

		framecolor = Colors.FRAME_COLOR_BLUE;

		readyStart = READY_START;
		readyEnd = READY_END;
		goStart = GO_START;
		goEnd = GO_END;

		readyDone = false;

		lives = 0;

		ghost = true;

		meterValue = 0;
		meterColor = Colors.METER_COLOR_RED;
		meterValueSub = 0;
		meterColorSub = Colors.METER_COLOR_RED;

		lagARE = false;
		lagStop = false;
		displaySize = playerID >= 2 ? DisplaySize.SMALL : DisplaySize.NORMAL;

		enableSE = true;
		gameoverAll = true;

		isNextVisible = true;
		isHoldVisible = true;
		isVisible = true;

		holdButtonNextSkip = false;

		allowTextRenderByReceiver = true;

		itemRollRollEnable = false;
		itemRollRollInterval = 30;

		itemXRayEnable = false;
		itemXRayCount = 0;

		itemColorEnable = false;
		itemColorCount = 0;

		interruptItemNumber = INTERRUPTITEM_NONE;

		clearMode = ClearType.LINE;
		colorClearSize = -1;
		garbageColorClear = false;
		ignoreHidden = false;
		connectBlocks = true;
		lineColorDiagonals = false;
		blockColors = BLOCK_COLORS_DEFAULT;
		cascadeDelay = 0;
		cascadeClearDelay = 0;

		rainbowAnimate = false;
		dominoQuickTurn = false;
		sticky = 0;

		startTime = 0;
		endTime = 0;

		// event 発生
		if (owner.mode != null) {
			owner.mode.playerInit(this, playerID);
			if (owner.replayMode) {
				owner.mode.loadReplay(this, playerID, owner.replayProp);
			}
		}
		owner.receiver.playerInit(this, playerID);
		if (ai != null) {
			ai.shutdown(this, playerID);
			ai.init(this, playerID);
		}
	}

	/**
	 * End processing
	 */
	public void shutdown() {
		// log.debug("GameEngine shutdown() playerID:" + playerID);

		if (ai != null) {
			ai.shutdown(this, playerID);
		}
		owner = null;
		ruleopt = null;
		wallkick = null;
		randomizer = null;
		field = null;
		ctrl = null;
		statistics = null;
		speed = null;
		random = null;
		replayData = null;
	}

	/* accessors for statc-array currently used across all over the game */
	public int statc_0() {
		return statc[0];
	}

	public void statc_0(int value) {
		statc[0] = value;
	}

	public int statc_1() {
		return statc[1];
	}

	public void statc_1(int value) {
		statc[1] = value;
	}

	public int statc_4() {
		return statc[4];
	}

	public void statc_4(int value) {
		statc[4] = value;
	}

	public int statc_5() {
		return statc[5];
	}

	public void statc_5(int value) {
		statc[5] = value;
	}

	/**
	 * Status counterInitialization
	 */
	public void resetStatc() {
		Arrays.fill(statc, 0);
	}

	/**
	 * Sound effectsPlay (enableSEThetrueOnly when)
	 *
	 * @param name Sound effectsOfName
	 */
	public void playSE(String name) {
		if (enableSE) {
			owner.receiver.playSE(name);
		}
	}

	/**
	 * NEXTOf PeaceIDGet the
	 *
	 * @param c Want to getNEXTThe position of the
	 * @return NEXTOf PeaceID
	 */
	public int getNextID(int c) {
		if (nextPieceIDs == null) {
			return Piece.PIECE_NONE;
		}
		int c2 = c;
		while (c2 >= nextPieceIDs.length) {
			c2 = c2 - nextPieceIDs.length;
		}
		return nextPieceIDs[c2];
	}

	/**
	 * NEXTGets an object of Peace
	 *
	 * @param c Want to getNEXTThe position of the
	 * @return NEXTObject of Peace
	 */
	public Piece getNextObject(int c) {
		if (nextPieces == null) {
			return null;
		}
		int c2 = c;
		while (c2 >= nextPieces.length) {
			c2 = c2 - nextPieces.length;
		}
		return nextPieces[c2];
	}

	/**
	 * NEXTObtain a copy of the object of the piece
	 *
	 * @param c Want to getNEXTThe position of the
	 * @return NEXTCopy of the object of the piece
	 */
	public Piece getNextObjectCopy(int c) {
		Piece p = getNextObject(c);
		Piece r = null;
		if (p != null) {
			r = new Piece(p);
		}
		return r;
	}

	/**
	 * Current AREGets the value of the (Also consider setting rules)
	 *
	 * @return Current ARE
	 */
	public int getARE() {
		if (speed.are < ruleopt.minARE && ruleopt.minARE >= 0) {
			return ruleopt.minARE;
		}
		if (speed.are > ruleopt.maxARE && ruleopt.maxARE >= 0) {
			return ruleopt.maxARE;
		}
		return speed.are;
	}

	/**
	 * Gets the value of the current ARE after line clear (Also considers setting
	 * rules)
	 *
	 * @return Current ARE after line clear
	 */
	public int getARELine() {
		if (speed.areLine < ruleopt.minARELine && ruleopt.minARELine >= 0) {
			return ruleopt.minARELine;
		}
		if (speed.areLine > ruleopt.maxARELine && ruleopt.maxARELine >= 0) {
			return ruleopt.maxARELine;
		}
		return speed.areLine;
	}

	/**
	 * Gets the value of the current Line clear time (Also consider setting rules)
	 *
	 * @return Current Line clear time
	 */
	public int getLineDelay() {
		if (speed.lineDelay < ruleopt.minLineDelay && ruleopt.minLineDelay >= 0) {
			return ruleopt.minLineDelay;
		}
		if (speed.lineDelay > ruleopt.maxLineDelay && ruleopt.maxLineDelay >= 0) {
			return ruleopt.maxLineDelay;
		}
		return speed.lineDelay;
	}

	/**
	 * Gets the value of the current Fixation time (Also consider setting rules)
	 *
	 * @return Current Fixation time
	 */
	public int getLockDelay() {
		if (speed.lockDelay < ruleopt.minLockDelay && ruleopt.minLockDelay >= 0) {
			return ruleopt.minLockDelay;
		}
		if (speed.lockDelay > ruleopt.maxLockDelay && ruleopt.maxLockDelay >= 0) {
			return ruleopt.maxLockDelay;
		}
		return speed.lockDelay;
	}

	/**
	 * Gets the value of the current DAS (Also consider setting rules)
	 *
	 * @return Current DAS
	 */
	public int getDAS() {
		if (speed.das < owMinDAS && owMinDAS >= 0) {
			return owMinDAS;
		}
		if (speed.das > owMaxDAS && owMaxDAS >= 0) {
			return owMaxDAS;
		}
		if (speed.das < ruleopt.minDAS && ruleopt.minDAS >= 0) {
			return ruleopt.minDAS;
		}
		if (speed.das > ruleopt.maxDAS && ruleopt.maxDAS >= 0) {
			return ruleopt.maxDAS;
		}
		return speed.das;
	}

	/**
	 * @return Controller.BUTTON_UP if controls are normal, Controller.BUTTON_DOWN
	 *         if up/down are reversed
	 */
	protected int getUp() {
		return owReverseUpDown ? Controller.BUTTON_DOWN : Controller.BUTTON_UP;
	}

	/**
	 * @return Controller.BUTTON_DOWN if controls are normal, Controller.BUTTON_UP
	 *         if up/down are reversed
	 */
	protected int getDown() {
		return owReverseUpDown ? Controller.BUTTON_UP : Controller.BUTTON_DOWN;
	}

	/**
	 * Current Gets the horizontal movement speed
	 *
	 * @return Lateral movement speed
	 */
	public int getDASDelay() {
		if (ruleopt == null || owDasDelay >= 0) {
			return owDasDelay;
		}
		return ruleopt.dasDelay;
	}

	/**
	 * Get the BlockSkin number in current use
	 *
	 * @return BlockSkin number
	 */
	public int getSkin() {
		if (ruleopt == null || owSkin >= 0) {
			return owSkin;
		}
		return ruleopt.skin;
	}

	/**
	 * @return A buttonI left when pressedrotationIf thefalse, RightrotationIf
	 *         thetrue
	 */
	public boolean isRotateButtonDefaultRight() {
		if (ruleopt == null || owRotateButtonDefaultRight >= 0) {
			return owRotateButtonDefaultRight != 0;
		}
		return ruleopt.rotateButtonDefaultRight;
	}

	/**
	 * Is diagonal movement enabled?
	 *
	 * @return true if diagonal movement is enabled
	 */
	public boolean isDiagonalMoveEnabled() {
		if (ruleopt == null || owMoveDiagonal >= 0) {
			return owMoveDiagonal == 1;
		}
		return ruleopt.moveDiagonal;
	}

	/**
	 * Visible / disappearRoll Of the statefieldReturned to the normal state
	 */
	public void resetFieldVisible() {
		if (field == null) {
			return;
		}
		for (int x = 0; x < field.getWidth(); x++) {
			for (int y = 0; y < field.getHeight(); y++) {
				Block block = field.getBlock(x, y);

				if (block != null && block.color > Colors.BLOCK_COLOR_NONE) {
					block.alpha = 1f;
					block.darkness = 0f;
					block.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					block.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
				}
			}
		}
	}

	/**
	 * SoftHard drop· Hold preceding precedingrotationRestrictions on the use of
	 * release
	 */
	public void checkDropContinuousUse() {
		if (!gameActive) {
			return;
		}
		if (!ctrl.isPress(getDown()) || !ruleopt.softdropLimit) {
			softdropContinuousUse = false;
		}
		if (!ctrl.isPress(getUp()) || !ruleopt.harddropLimit) {
			harddropContinuousUse = false;
		}
		if (!ctrl.isPress(Controller.BUTTON_D) || !ruleopt.holdInitialLimit) {
			initialHoldContinuousUse = false;
		}
		if (!ruleopt.rotateInitialLimit) {
			initialRotateContinuousUse = false;
		}

		if (initialRotateContinuousUse) {
			int dir = 0;
			if (ctrl.isPress(Controller.BUTTON_A) || ctrl.isPress(Controller.BUTTON_C)) {
				dir = -1;
			} else if (ctrl.isPress(Controller.BUTTON_B)) {
				dir = 1;
			} else if (ctrl.isPress(Controller.BUTTON_E)) {
				dir = 2;
			}

			if (initialRotateLastDirection != dir || dir == 0) {
				initialRotateContinuousUse = false;
			}
		}
	}

	/**
	 * Get the Direction Of Lateral motion input
	 *
	 * @return -1:Left 0:No 1:Right
	 */
	protected int getMoveDirection() {
		if (ctrl.isPress(Controller.BUTTON_LEFT) && ctrl.isPress(Controller.BUTTON_RIGHT)) {
			if (!ruleopt.moveLeftAndRightAllow) {
				return 0;
			}
			if (ctrl.buttonTime(Controller.BUTTON_LEFT) > ctrl.buttonTime(Controller.BUTTON_RIGHT)) {
				return ruleopt.moveLeftAndRightUsePreviousInput ? -1 : 1;
			}
			if (ctrl.buttonTime(Controller.BUTTON_LEFT) < ctrl.buttonTime(Controller.BUTTON_RIGHT)) {
				return ruleopt.moveLeftAndRightUsePreviousInput ? 1 : -1;
			}
		}
		if (ctrl.isPress(Controller.BUTTON_LEFT)) {
			return -1;
		}
		if (ctrl.isPress(Controller.BUTTON_RIGHT)) {
			return 1;
		}
		return 0;
	}

	/**
	 * Processing horizontal reservoir
	 */
	protected void padRepeat() {
		int moveDirection = getMoveDirection();
		if (moveDirection != 0) {
			dasCount++;
		} else if (!ruleopt.dasStoreChargeOnNeutral) {
			dasCount = 0;
		}
		dasDirection = moveDirection;
	}

	/**
	 * Called if delay doesn't allow charging but dasRedirectInDelay == true Updates
	 * dasDirection so player can change direction without dropping charge on entry.
	 */
	protected void dasRedirect() {
		dasDirection = getMoveDirection();
	}

	/**
	 * Move countDetermines whether or not to exceed the limit
	 *
	 * @return Move countI have exceeded the limittrue
	 */
	protected boolean isMoveCountExceed() {
		if (ruleopt.lockresetLimitShareCount == true) {
			if (extendedMoveCount + extendedRotateCount >= ruleopt.lockresetLimitMove
					&& ruleopt.lockresetLimitMove >= 0) {
				return true;
			}
		} else if (extendedMoveCount >= ruleopt.lockresetLimitMove && ruleopt.lockresetLimitMove >= 0) {
			return true;
		}

		return false;
	}

	/**
	 * rotation countDetermines whether or not to exceed the limit
	 *
	 * @return rotation countI have exceeded the limittrue
	 */
	protected boolean isRotateCountExceed() {
		if (ruleopt.lockresetLimitShareCount == true) {
			if (extendedMoveCount + extendedRotateCount >= ruleopt.lockresetLimitMove
					&& ruleopt.lockresetLimitMove >= 0) {
				return true;
			}
		} else if (extendedRotateCount >= ruleopt.lockresetLimitRotate && ruleopt.lockresetLimitRotate >= 0) {
			return true;
		}

		return false;
	}

	/**
	 * T-Spin routine
	 *
	 * @param x     X coord
	 * @param y     Y coord
	 * @param piece Current piece object
	 * @param fld   Field object
	 */
	public void setTSpin(int x, int y, Piece piece, Field fld) {
		if (piece == null || piece.id != Piece.PIECE_T) {
			tspin = false;
			return;
		}

		if (!tspinAllowKick && kickused) {
			tspin = false;
			return;
		}

		if (spinCheckType == SpinBonus.SPINTYPE_4POINT) {
			if (tspinminiType == TSPINMINI_TYPE_ROTATECHECK) {
				if (nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection(-1), field)
						&& nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection(1), field)) {
					tspinmini = true;
				}
			} else if (tspinminiType == TSPINMINI_TYPE_WALLKICKFLAG) {
				tspinmini = kickused;
			}

			int[] tx = new int[4];
			int[] ty = new int[4];

			// Setup 4-point coordinates
			if (piece.big) {
				tx[0] = 1;
				ty[0] = 1;
				tx[1] = 4;
				ty[1] = 1;
				tx[2] = 1;
				ty[2] = 4;
				tx[3] = 4;
				ty[3] = 4;
			} else {
				tx[0] = 0;
				ty[0] = 0;
				tx[1] = 2;
				ty[1] = 0;
				tx[2] = 0;
				ty[2] = 2;
				tx[3] = 2;
				ty[3] = 2;
			}
			for (int i = 0; i < tx.length; i++) {
				if (piece.big) {
					tx[i] += ruleopt.pieceOffsetX[piece.id][piece.direction] * 2;
					ty[i] += ruleopt.pieceOffsetY[piece.id][piece.direction] * 2;
				} else {
					tx[i] += ruleopt.pieceOffsetX[piece.id][piece.direction];
					ty[i] += ruleopt.pieceOffsetY[piece.id][piece.direction];
				}
			}

			// Check the corner of the T piece
			int count = 0;

			for (int i = 0; i < tx.length; i++) {
				if (fld.getBlockColor(x + tx[i], y + ty[i]) != Colors.BLOCK_COLOR_NONE) {
					count++;
				}
			}

			if (count >= 3) {
				tspin = true;
			}
		} else if (spinCheckType == SpinBonus.SPINTYPE_IMMOBILE) {
			if (piece.checkCollision(x, y - 1, fld) && piece.checkCollision(x + 1, y, fld)
					&& piece.checkCollision(x - 1, y, fld)) {
				tspin = true;
				Field copyField = new Field(fld);
				piece.placeToField(x, y, copyField);
				if (copyField.checkLineNoFlag() == 1 && kickused) {
					tspinmini = true;
				}
			} else if (tspinEnableEZ && kickused) {
				tspin = true;
				tspinez = true;
			}
		}
	}

	/**
	 * SpinJudgment(When all rules for spin)
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param piece Current BlockPeace
	 * @param fld   field
	 */
	public void setAllSpin(int x, int y, Piece piece, Field fld) {
		tspin = false;
		tspinmini = false;
		tspinez = false;

		if (piece == null || !tspinAllowKick && kickused || piece.big) {
			return;
		}

		if (spinCheckType == SpinBonus.SPINTYPE_4POINT) {

			int offsetX = ruleopt.pieceOffsetX[piece.id][piece.direction];
			int offsetY = ruleopt.pieceOffsetY[piece.id][piece.direction];

			for (int i = 0; i < SpinBonus.HIGH_X[piece.id][piece.direction].length / 2; i++) {
				boolean isHighSpot1 = false;
				boolean isHighSpot2 = false;
				boolean isLowSpot1 = false;
				boolean isLowSpot2 = false;

				if (!fld.getBlockEmpty(x + SpinBonus.HIGH_X[piece.id][piece.direction][i * 2 + 0] + offsetX,
						y + SpinBonus.HIGH_Y[piece.id][piece.direction][i * 2 + 0] + offsetY)) {
					isHighSpot1 = true;
				}
				if (!fld.getBlockEmpty(x + SpinBonus.HIGH_X[piece.id][piece.direction][i * 2 + 1] + offsetX,
						y + SpinBonus.HIGH_Y[piece.id][piece.direction][i * 2 + 1] + offsetY)) {
					isHighSpot2 = true;
				}
				if (!fld.getBlockEmpty(x + SpinBonus.LOW_X[piece.id][piece.direction][i * 2 + 0] + offsetX,
						y + SpinBonus.LOW_Y[piece.id][piece.direction][i * 2 + 0] + offsetY)) {
					isLowSpot1 = true;
				}
				if (!fld.getBlockEmpty(x + SpinBonus.LOW_X[piece.id][piece.direction][i * 2 + 1] + offsetX,
						y + SpinBonus.LOW_Y[piece.id][piece.direction][i * 2 + 1] + offsetY)) {
					isLowSpot2 = true;
				}

				// log.debug(isHighSpot1 + "," + isHighSpot2 + "," + isLowSpot1 + "," +
				// isLowSpot2);

				if (isHighSpot1 && isHighSpot2 && (isLowSpot1 || isLowSpot2)) {
					tspin = true;
				} else if (!tspin && isLowSpot1 && isLowSpot2 && (isHighSpot1 || isHighSpot2)) {
					tspin = true;
					tspinmini = true;
				}
			}
		} else if (spinCheckType == SpinBonus.SPINTYPE_IMMOBILE) {
			// int y2 = y - 1;
			// log.debug(x + "," + y2 + ":" + piece.checkCollision(x, y2, fld));

			if (piece.checkCollision(x, y - 1, fld) && piece.checkCollision(x + 1, y, fld)
					&& piece.checkCollision(x - 1, y, fld)) {
				tspin = true;
				Field copyField = new Field(fld);
				piece.placeToField(x, y, copyField);
				if (piece.getHeight() + 1 != copyField.checkLineNoFlag() && kickused) {
					tspinmini = true;
					// if((copyField.checkLineNoFlag() == 1) && (kickused == true)) tspinmini =
					// true;
				}
			} else if (tspinEnableEZ && kickused) {
				tspin = true;
				tspinez = true;
			}
		}
	}

	/**
	 * Determines whether the hold
	 *
	 * @return If you can holdtrue
	 */
	public boolean isHoldOK() {
		if (!ruleopt.holdEnable || holdDisable || holdUsedCount >= ruleopt.holdLimit && ruleopt.holdLimit >= 0
				|| initialHoldContinuousUse) {
			return false;
		}

		return true;
	}

	/**
	 * Peace appearsX-coordinateGet the
	 *
	 * @param fld   field
	 * @param piece Piece
	 * @return Appearance position ofX-coordinate
	 */
	public int getSpawnPosX(Field fld, Piece piece) {
		int x = -1 + (fld.getWidth() - piece.getWidth() + 1) / 2;

		if (big && bigmove && x % 2 != 0) {
			x++;
		}

		if (big) {
			x += ruleopt.pieceSpawnXBig[piece.id][piece.direction];
		} else {
			x += ruleopt.pieceSpawnX[piece.id][piece.direction];
		}

		return x;
	}

	/**
	 * Peace appearsY-coordinateGet the
	 *
	 * @param piece Piece
	 * @return Appearance position ofY-coordinate
	 */
	public int getSpawnPosY(Piece piece) {
		int y = 0;

		if (ruleopt.pieceEnterAboveField && !ruleopt.fieldCeiling) {
			y = -1 - piece.getMaximumBlockY();
			if (big) {
				y--;
			}
		} else {
			y = -piece.getMinimumBlockY();
		}

		if (big) {
			y += ruleopt.pieceSpawnYBig[piece.id][piece.direction];
		} else {
			y += ruleopt.pieceSpawnY[piece.id][piece.direction];
		}

		return y;
	}

	/**
	 * Get the Piece rotation Direction after pressing the button
	 *
	 * @param move rotationDirection (-1:Left 1:Right 2:180Degrees)
	 * @return rotation buttonPiece after pressing theDirection
	 */
	public int getRotateDirection(int move) {
		int direction = 0;
		if (nowPieceObject != null) {
			direction = nowPieceObject.direction;
		}
		return Piece.getRotateDirection(move, direction);
	}

	/**
	 * PrecedingrotationHold processing and precedence
	 */
	public void initialRotate() {
		initialRotateDirection = 0;
		initialHoldFlag = false;

		if (ruleopt.rotateInitial && !initialRotateContinuousUse) {
			int dir = 0;
			if (ctrl.isPress(Controller.BUTTON_A) || ctrl.isPress(Controller.BUTTON_C)) {
				dir = -1;
			} else if (ctrl.isPress(Controller.BUTTON_B)) {
				dir = 1;
			} else if (ctrl.isPress(Controller.BUTTON_E)) {
				dir = 2;
			}
			initialRotateDirection = dir;
		}

		if (ctrl.isPress(Controller.BUTTON_D) && ruleopt.holdInitial && isHoldOK()) {
			initialHoldFlag = true;
			initialHoldContinuousUse = true;
			playSE("initialhold");
		}
	}

	/**
	 * fieldOfBlock stateUpdate
	 */
	public void fieldUpdate() {
		boolean outlineOnly = blockShowOutlineOnly; // Show outline only flag
		if (owBlockShowOutlineOnly == 0) {
			outlineOnly = false;
		}
		if (owBlockShowOutlineOnly == 1) {
			outlineOnly = true;
		}

		if (field != null) {
			for (int i = 0; i < field.getWidth(); i++) {
				for (int j = field.getHiddenHeight() * -1; j < field.getHeight(); j++) {
					Block blk = field.getBlock(i, j);

					if (blk != null && blk.color >= Colors.BLOCK_COLOR_GRAY) {
						if (blk.elapsedFrames < 0) {
							if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
								blk.darkness = 0f;
							}
						} else if (blk.elapsedFrames < ruleopt.lockflash) {
							blk.darkness = -0.8f;
							if (outlineOnly) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, false);
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_BONE, false);
							}
						} else {
							blk.darkness = 0f;
							blk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
							if (outlineOnly) {
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, false);
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_BONE, false);
							}
						}

						if (blockHidden != -1 && blk.elapsedFrames >= blockHidden - 10 && gameActive) {
							if (blockHiddenAnim) {
								blk.alpha -= 0.1f;
								if (blk.alpha < 0.0f) {
									blk.alpha = 0.0f;
								}
							}

							if (blk.elapsedFrames >= blockHidden) {
								blk.alpha = 0.0f;
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, false);
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, false);
							}
						}

						if (blk.elapsedFrames >= 0) {
							blk.elapsedFrames++;
						}
					}
				}
			}
		}

		// X-RAY
		if (field != null && gameActive && itemXRayEnable) {
			for (int i = 0; i < field.getWidth(); i++) {
				for (int j = field.getHiddenHeight() * -1; j < field.getHeight(); j++) {
					Block blk = field.getBlock(i, j);

					if (blk != null && blk.color >= Colors.BLOCK_COLOR_GRAY) {
						blk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, itemXRayCount % 36 == i);
						blk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, itemXRayCount % 36 == i);
					}
				}
			}
			itemXRayCount++;
		} else {
			itemXRayCount = 0;
		}

		// COLOR
		if (field != null && gameActive && itemColorEnable) {
			for (int i = 0; i < field.getWidth(); i++) {
				for (int j = field.getHiddenHeight() * -1; j < field.getHeight(); j++) {
					int bright = j;
					if (bright >= 5) {
						bright = 9 - bright;
					}
					bright = 40 - ((20 - i + bright) * 4 + itemColorCount) % 40;
					if (bright >= 0 && bright < ITEM_COLOR_BRIGHT_TABLE.length) {
						bright = 10 - ITEM_COLOR_BRIGHT_TABLE[bright];
					}
					if (bright > 10) {
						bright = 10;
					}

					Block blk = field.getBlock(i, j);

					if (blk != null) {
						blk.alpha = bright * 0.1f;
						blk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, false);
						blk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					}
				}
			}
			itemColorCount++;
		} else {
			itemColorCount = 0;
		}

		// BunglerHIDDEN
		if (heboHiddenEnable && gameActive) {
			heboHiddenTimerNow++;

			if (heboHiddenTimerNow > heboHiddenTimerMax) {
				heboHiddenTimerNow = 0;
				heboHiddenYNow++;
				if (heboHiddenYNow > heboHiddenYLimit) {
					heboHiddenYNow = heboHiddenYLimit;
				}
			}
		}
	}

	/**
	 * Called when saving replay
	 */
	public void saveReplay() {
		if (owner.replayMode && !owner.replayRerecord) {
			return;
		}
		owner.replayProp.setProperty("version", version.toString());
		owner.replayProp.setProperty(playerID + ".replay.randSeed", Long.toString(randSeed, 16));

		replayData.writeProperty(owner.replayProp, playerID, replayTimer);
		statistics.writeProperty(owner.replayProp, playerID);
		ruleopt.writeProperty(owner.replayProp, playerID);

		if (playerID == 0) {
			if (owner.mode != null) {
				owner.replayProp.setProperty("name.mode", owner.mode.getName());
			}
			if (ruleopt.strRuleName != null) {
				owner.replayProp.setProperty("name.rule", ruleopt.strRuleName);
			}

			// Local timestamp
			Calendar currentTime = Calendar.getInstance();
			int month = currentTime.get(Calendar.MONTH) + 1;
			String strDate = String.format("%04d/%02d/%02d", currentTime.get(Calendar.YEAR), month,
					currentTime.get(Calendar.DATE));
			String strTime = String.format("%02d:%02d:%02d", currentTime.get(Calendar.HOUR_OF_DAY),
					currentTime.get(Calendar.MINUTE), currentTime.get(Calendar.SECOND));
			owner.replayProp.setProperty("timestamp.date", strDate);
			owner.replayProp.setProperty("timestamp.time", strTime);

			// GMT timestamp
			owner.replayProp.setProperty("timestamp.gmt", GeneralUtil.exportCalendarString());
		}

		owner.replayProp.setProperty(playerID + ".tuning.owRotateButtonDefaultRight", owRotateButtonDefaultRight);
		owner.replayProp.setProperty(playerID + ".tuning.owSkin", owSkin);
		owner.replayProp.setProperty(playerID + ".tuning.owMinDAS", owMinDAS);
		owner.replayProp.setProperty(playerID + ".tuning.owMaxDAS", owMaxDAS);
		owner.replayProp.setProperty(playerID + ".tuning.owDasDelay", owDasDelay);
		owner.replayProp.setProperty(playerID + ".tuning.owReverseUpDown", owReverseUpDown);
		owner.replayProp.setProperty(playerID + ".tuning.owMoveDiagonal", owMoveDiagonal);

		if (owner.mode != null) {
			owner.mode.saveReplay(this, playerID, owner.replayProp);
		}
	}

	/**
	 * fieldProcessing to enter the edit screen
	 */
	public void enterFieldEdit() {
		fldeditPreviousStat = stat;
		stat = Status.FIELDEDIT;
		fldeditX = 0;
		fldeditY = 0;
		fldeditColor = Colors.BLOCK_COLOR_GRAY;
		fldeditFrames = 0;
		owner.menuOnly = false;
		createFieldIfNeeded();
	}

	/**
	 * fieldAInitialization (If I do not exist yet)
	 */
	public void createFieldIfNeeded() {
		if (fieldWidth < 0) {
			fieldWidth = ruleopt.fieldWidth;
		}
		if (fieldHeight < 0) {
			fieldHeight = ruleopt.fieldHeight;
		}
		if (fieldHiddenHeight < 0) {
			fieldHiddenHeight = ruleopt.fieldHiddenHeight;
		}
		if (field == null) {
			field = new Field(fieldWidth, fieldHeight, fieldHiddenHeight, ruleopt.fieldCeiling);
		}
	}

	/**
	 * Call this if the game has ended
	 */
	public void gameEnded() {
		if (endTime == 0) {
			endTime = System.nanoTime();
			statistics.gamerate = (float) (replayTimer / (0.00000006 * (endTime - startTime)));
			statistics.update();
		}
		gameActive = false;
		timerActive = false;
		isInGame = false;
		if (ai != null) {
			ai.shutdown(this, playerID);
		}
	}

	/**
	 * Game stateUpdates
	 */
	public void update() {
		if (gameActive) {
			// Related processing replay
			if (!owner.replayMode || owner.replayRerecord) {
				// AIOf buttonProcessing
				if (ai != null) {
					if (!aiShowHint) {
						ai.setControl(this, playerID, ctrl);
					} else {
						aiHintReady = ai.thinkComplete
								|| ai.thinkCurrentPieceNo > 0 && ai.thinkCurrentPieceNo <= ai.thinkLastPieceNo;
						if (aiHintReady) {
							aiHintPiece = null;
							if (ai.bestHold) {
								if (holdPieceObject != null) {
									aiHintPiece = new Piece(holdPieceObject);
								} else {
									aiHintPiece = getNextObjectCopy(nextPieceCount);
									if (!aiHintPiece.offsetApplied) {
										aiHintPiece.applyOffsetArray(ruleopt.pieceOffsetX[aiHintPiece.id],
												ruleopt.pieceOffsetY[aiHintPiece.id]);
									}
								}
							} else if (nowPieceObject != null) {
								aiHintPiece = new Piece(nowPieceObject);
							}
						}
					}
				}

				// input Replay recorded in the state
				replayData.setInputData(ctrl.getButtonBit(), replayTimer);
			} else {
				// input Replay the state read from
				ctrl.setButtonBit(replayData.getInputData(replayTimer));
			}
			replayTimer++;
		}

		// button input timeUpdates
		ctrl.updateButtonTime();

		// 最初の処理
		if (owner.mode != null) {
			owner.mode.onFirst(this, playerID);
		}
		owner.receiver.onFirst(this, playerID);
		if (ai != null && (!owner.replayMode || owner.replayRerecord)) {
			ai.onFirst(this, playerID);
		}

		// Processing status of each
		if (!lagStop) {
			switch (stat) {
			case SETTING -> statSetting();
			case READY -> statReady();
			case LOCKFLASH -> statLockFlash();
			case LINECLEAR -> statLineClear();
			case ARE -> statARE();
			case ENDINGSTART -> statEndingStart();
			case CUSTOM -> statCustom();
			case EXCELLENT -> statExcellent();
			case GAMEOVER -> statGameOver();
			case RESULT -> statResult();
			case FIELDEDIT -> statFieldEdit();
			case INTERRUPTITEM -> statInterruptItem();
			case MOVE -> {
				dasRepeat = true;
				dasInstant = false;
				while (dasRepeat) {
					statMove();
				}
			}
			case NOTHING -> {
			}
			}
		}

		// fieldOfBlock stateUpdate and statistics
		fieldUpdate();
		if (ending == 0 || staffrollEnableStatistics) {
			statistics.update();
		}

		// 最後の処理
		if (owner.mode != null) {
			owner.mode.onLast(this, playerID);
		}
		owner.receiver.onLast(this, playerID);
		if (ai != null && (!owner.replayMode || owner.replayRerecord)) {
			ai.onLast(this, playerID);
		}

		// TimerIncrease
		if (gameActive && timerActive) {
			statistics.time++;
		}

		/*
		 * if(startTime > 0 && endTime == 0) { statistics.gamerate = (float)(replayTimer
		 * / (0.00000006*(System.nanoTime() - startTime))); }
		 */
	}

	/**
	 * Draw the screen (EachMode Ya event Processing classes event Just call,
	 * OtherwiseGameEngineItself does not do anything)
	 */
	public void render() {
		// 最初の処理
		owner.receiver.renderFirst(this, playerID);
		if (owner.mode != null) {
			owner.mode.renderFirst(this, playerID);
		}

		if (rainbowAnimate) {
			Block.updateRainbowPhase(this);
		}

		// Processing status of each
		switch (stat) {
		case NOTHING:
			break;
		case SETTING:
			if (owner.mode != null) {
				owner.mode.renderSetting(this, playerID);
			}
			owner.receiver.renderSetting(this, playerID);
			break;
		case READY:
			if (owner.mode != null) {
				owner.mode.renderReady(this, playerID);
			}
			owner.receiver.renderReady(this, playerID);
			break;
		case MOVE:
			if (owner.mode != null) {
				owner.mode.renderMove(this, playerID);
			}
			owner.receiver.renderMove(this, playerID);
			break;
		case LOCKFLASH:
			if (owner.mode != null) {
				owner.mode.renderLockFlash(this, playerID);
			}
			owner.receiver.renderLockFlash(this, playerID);
			break;
		case LINECLEAR:
			if (owner.mode != null) {
				owner.mode.renderLineClear(this, playerID);
			}
			owner.receiver.renderLineClear(this, playerID);
			break;
		case ARE:
			if (owner.mode != null) {
				owner.mode.renderARE(this, playerID);
			}
			owner.receiver.renderARE(this, playerID);
			break;
		case ENDINGSTART:
			if (owner.mode != null) {
				owner.mode.renderEndingStart(this, playerID);
			}
			owner.receiver.renderEndingStart(this, playerID);
			break;
		case CUSTOM:
			if (owner.mode != null) {
				owner.mode.renderCustom(this, playerID);
			}
			owner.receiver.renderCustom(this, playerID);
			break;
		case EXCELLENT:
			if (owner.mode != null) {
				owner.mode.renderExcellent(this, playerID);
			}
			owner.receiver.renderExcellent(this, playerID);
			break;
		case GAMEOVER:
			if (owner.mode != null) {
				owner.mode.renderGameOver(this, playerID);
			}
			owner.receiver.renderGameOver(this, playerID);
			break;
		case RESULT:
			if (owner.mode != null) {
				owner.mode.renderResult(this, playerID);
			}
			owner.receiver.renderResult(this, playerID);
			break;
		case FIELDEDIT:
			if (owner.mode != null) {
				owner.mode.renderFieldEdit(this, playerID);
			}
			owner.receiver.renderFieldEdit(this, playerID);
			break;
		case INTERRUPTITEM:
			break;
		}

		if (owner.showInput) {
			if (owner.mode != null) {
				owner.mode.renderInput(this, playerID);
			}
			owner.receiver.renderInput(this, playerID);
		}
		if (ai != null) {
			if (aiShowState) {
				ai.renderState(this, playerID);
			}
			if (aiShowHint) {
				ai.renderHint(this, playerID);
			}
		}

		// 最後の処理
		if (owner.mode != null) {
			owner.mode.renderLast(this, playerID);
		}
		owner.receiver.renderLast(this, playerID);
	}

	/**
	 * Processing when the setup screen before the start of
	 */
	public void statSetting() {
		// event 発生
		if (owner.mode != null && owner.mode.onSetting(this, playerID)) {
			return;
		}

		owner.receiver.onSetting(this, playerID);

		// Mode側が何もしない場合はReady画面へ移動
		stat = Status.READY;
		resetStatc();
	}

	/**
	 * Ready→GoProcessing time
	 */
	public void statReady() {
		// event 発生
		if (owner.mode != null && owner.mode.onReady(this, playerID)) {
			return;
		}

		owner.receiver.onReady(this, playerID);

		// Horizontal reservoir
		if (ruleopt.dasInReady && gameActive) {
			padRepeat();
		} else if (ruleopt.dasRedirectInDelay) {
			dasRedirect();
		}

		// Initialization
		if (statc[0] == 0) {
			// fieldInitialization
			createFieldIfNeeded();

			// NEXTCreating Peace
			if (nextPieceIDs == null) {
				// Peace is possible emergence1If no one is to be able to all appearance
				boolean allDisable = true;
				for (boolean element : nextPieceEnable) {
					if (element) {
						allDisable = false;
						break;
					}
				}
				if (allDisable) {
					for (int i = 0; i < nextPieceEnable.length; i++) {
						nextPieceEnable[i] = true;
					}
				}

				// NEXTCreate the order of appearance of the piece
				if (randomizer == null) {
					randomizer = new MemorylessRandomizer(nextPieceEnable, randSeed);
				} else {
					randomizer.setState(nextPieceEnable, randSeed);
				}
				nextPieceIDs = new int[nextPieceArraySize];
				for (int i = 0; i < nextPieceArraySize; i++) {
					nextPieceIDs[i] = randomizer.next();
				}
			}
			// NEXTCreate an object of Peace
			if (nextPieces == null) {
				nextPieces = new Piece[nextPieceIDs.length];

				for (int i = 0; i < nextPieces.length; i++) {
					nextPieces[i] = new Piece(nextPieceIDs[i]);
					nextPieces[i].direction = ruleopt.pieceDefaultDirection[nextPieces[i].id];
					if (nextPieces[i].direction >= Piece.DIRECTION_COUNT) {
						nextPieces[i].direction = random.nextInt(Piece.DIRECTION_COUNT);
					}
					nextPieces[i].connectBlocks = connectBlocks;
					nextPieces[i].setColor(ruleopt.pieceColor[nextPieces[i].id]);
					nextPieces[i].setSkin(getSkin());
					nextPieces[i].updateConnectData();
					nextPieces[i].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					nextPieces[i].setAttribute(Block.BLOCK_ATTRIBUTE_BONE, bone);
				}
				if (randomBlockColor) {
					if (blockColors.length < numColors || numColors < 1) {
						numColors = blockColors.length;
					}
					for (Piece element : nextPieces) {
						int size = element.getMaxBlock();
						int[] colors = new int[size];
						for (int j = 0; j < size; j++) {
							colors[j] = blockColors[random.nextInt(numColors)];
						}
						element.setColor(colors);
						element.updateConnectData();
					}
				}
			}

			if (!readyDone) {
				// button inputReset state
				ctrl = new Controller();
				// Game flagON
				gameActive = true;
				gameStarted = true;
				isInGame = true;
			}
		}

		// READYSound
		if (statc[0] == readyStart) {
			playSE("ready");
		}

		// GOSound
		if (statc[0] == goStart) {
			playSE("go");
		}

		// NEXTSkip
		if (statc[0] > 0 && statc[0] < goEnd && holdButtonNextSkip && isHoldOK() && ctrl.isPush(Controller.BUTTON_D)) {
			playSE("initialhold");
			holdPieceObject = getNextObjectCopy(nextPieceCount);
			holdPieceObject.applyOffsetArray(ruleopt.pieceOffsetX[holdPieceObject.id],
					ruleopt.pieceOffsetY[holdPieceObject.id]);
			nextPieceCount++;
			if (nextPieceCount < 0) {
				nextPieceCount = 0;
			}
		}

		// Start
		if (statc[0] >= goEnd) {
			if (!readyDone) {
				owner.bgmStatus.bgm = 0;
			}
			if (owner.mode != null) {
				owner.mode.startGame(this, playerID);
			}
			owner.receiver.startGame(this, playerID);
			initialRotate();
			stat = Status.MOVE;
			resetStatc();
			if (!readyDone) {
				startTime = System.nanoTime();
				// startTime = System.nanoTime()/1000000L;
			}
			readyDone = true;
			return;
		}

		statc[0]++;
	}

	/**
	 * BlockProcess of moving the pieces
	 */
	public void statMove() {
		dasRepeat = false;

		// event 発生
		if (owner.mode != null && owner.mode.onMove(this, playerID)) {
			return;
		}
		owner.receiver.onMove(this, playerID);

		// Horizontal reservoir Initialization
		int moveDirection = getMoveDirection();

		if ((statc[0] > 0 || ruleopt.dasInMoveFirstFrame) && dasDirection != moveDirection) {
			dasDirection = moveDirection;
			if (!(dasDirection == 0 && ruleopt.dasStoreChargeOnNeutral)) {
				dasCount = 0;
			}
		}

		// Processing at the time of emergence
		if (statc[0] == 0) {
			if (statc[1] == 0 && !initialHoldFlag) {
				// Normal appearance
				nowPieceObject = getNextObjectCopy(nextPieceCount);
				nextPieceCount++;
				if (nextPieceCount < 0) {
					nextPieceCount = 0;
				}
				holdDisable = false;
			} else {
				// Hold appearance
				if (initialHoldFlag) {
					// Hold preceding
					if (holdPieceObject == null) {
						// 1Th
						holdPieceObject = getNextObjectCopy(nextPieceCount);
						holdPieceObject.applyOffsetArray(ruleopt.pieceOffsetX[holdPieceObject.id],
								ruleopt.pieceOffsetY[holdPieceObject.id]);
						nextPieceCount++;
						if (nextPieceCount < 0) {
							nextPieceCount = 0;
						}

						if (bone) {
							getNextObject(nextPieceCount + ruleopt.nextDisplay - 1)
									.setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);
						}

						nowPieceObject = getNextObjectCopy(nextPieceCount);
						nextPieceCount++;
						if (nextPieceCount < 0) {
							nextPieceCount = 0;
						}
					} else {
						// 2Subsequent
						Piece pieceTemp = holdPieceObject;
						holdPieceObject = getNextObjectCopy(nextPieceCount);
						holdPieceObject.applyOffsetArray(ruleopt.pieceOffsetX[holdPieceObject.id],
								ruleopt.pieceOffsetY[holdPieceObject.id]);
						nowPieceObject = pieceTemp;
						nextPieceCount++;
						if (nextPieceCount < 0) {
							nextPieceCount = 0;
						}
					}
				} else // Usually hold
				if (holdPieceObject == null) {
					// 1Th
					nowPieceObject.big = false;
					holdPieceObject = nowPieceObject;
					nowPieceObject = getNextObjectCopy(nextPieceCount);
					nextPieceCount++;
					if (nextPieceCount < 0) {
						nextPieceCount = 0;
					}
				} else {
					// 2Subsequent
					nowPieceObject.big = false;
					Piece pieceTemp = holdPieceObject;
					holdPieceObject = nowPieceObject;
					nowPieceObject = pieceTemp;
				}

				// DirectionReturn
				if (ruleopt.holdResetDirection
						&& ruleopt.pieceDefaultDirection[holdPieceObject.id] < Piece.DIRECTION_COUNT) {
					holdPieceObject.direction = ruleopt.pieceDefaultDirection[holdPieceObject.id];
					holdPieceObject.updateConnectData();
				}

				// Was used count+1
				holdUsedCount++;
				statistics.totalHoldUsed++;

				// Disabling Hold
				initialHoldFlag = false;
				holdDisable = true;
			}
			playSE("piece" + getNextObject(nextPieceCount).id);

			if (!nowPieceObject.offsetApplied) {
				nowPieceObject.applyOffsetArray(ruleopt.pieceOffsetX[nowPieceObject.id],
						ruleopt.pieceOffsetY[nowPieceObject.id]);
			}

			nowPieceObject.big = big;

			// Appearance position (Horizontal)
			nowPieceX = getSpawnPosX(field, nowPieceObject);

			// Appearance position (Vertical)
			nowPieceY = getSpawnPosY(nowPieceObject);

			nowPieceBottomY = nowPieceObject.getBottom(nowPieceX, nowPieceY, field);
			nowPieceColorOverride = -1;

			if (itemRollRollEnable) {
				nowPieceColorOverride = Colors.BLOCK_COLOR_GRAY;
			}

			// Preceding rotation
			if (version.isLower(7, 5, 0)) {
				initialRotate(); // XXX: Weird active time IRS
				// if( (getARE() != 0) && ((getARELine() != 0) || (version < 6.3f)) )
				// initialRotate();
			}

			if (speed.gravity > speed.denominator && speed.denominator > 0) {
				gcount = speed.gravity % speed.denominator;
			} else {
				gcount = 0;
			}

			lockDelayNow = 0;
			dasSpeedCount = getDASDelay();
			dasRepeat = false;
			dasInstant = false;
			extendedMoveCount = 0;
			extendedRotateCount = 0;
			softdropFall = 0;
			harddropFall = 0;
			manualLock = false;
			nowPieceMoveCount = 0;
			nowPieceRotateCount = 0;
			nowPieceRotateFailCount = 0;
			nowWallkickCount = 0;
			nowUpwardWallkickCount = 0;
			lineClearing = 0;
			lastmove = LastMove.NONE;
			kickused = false;
			tspin = false;
			tspinmini = false;
			tspinez = false;

			getNextObject(nextPieceCount + ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, bone);

			if (ending == 0) {
				timerActive = true;
			}

			if (ai != null && (!owner.replayMode || owner.replayRerecord)) {
				ai.newPiece(this, playerID);
			}
		}

		checkDropContinuousUse();

		boolean softdropUsed = false; // This frame ToSoft dropI usedtrue
		int softdropFallNow = 0; // This frame OfSoft dropStage which has fallen incount

		boolean updown = false; // UpUnder simultaneous press flag
		if (ctrl.isPress(getUp()) && ctrl.isPress(getDown())) {
			updown = true;
		}

		if (!dasInstant) {

			// Hold
			if (ctrl.isPush(Controller.BUTTON_D) || initialHoldFlag) {
				if (isHoldOK()) {
					statc[0] = 0;
					statc[1] = 1;
					if (!initialHoldFlag) {
						playSE("hold");
					}
					initialHoldContinuousUse = true;
					initialHoldFlag = false;
					holdDisable = true;
					initialRotate(); // Hold swap triggered IRS
					statMove();
					return;
				} else if (statc[0] > 0 && !initialHoldFlag) {
					playSE("holdfail");
				}
			}

			// rotation
			boolean onGroundBeforeRotate = nowPieceObject.checkCollision(nowPieceX, nowPieceY + 1, field);
			int move = 0;
			boolean rotated = false;

			if (initialRotateDirection != 0) {
				move = initialRotateDirection;
				initialRotateLastDirection = initialRotateDirection;
				initialRotateContinuousUse = true;
				playSE("initialrotate");
			} else if (statc[0] > 0 || ruleopt.moveFirstFrame) {
				if (itemRollRollEnable && replayTimer % itemRollRollInterval == 0) {
					move = 1; // Roll Roll
				}

				// button input
				if (ctrl.isPush(Controller.BUTTON_A) || ctrl.isPush(Controller.BUTTON_C)) {
					move = -1;
				} else if (ctrl.isPush(Controller.BUTTON_B)) {
					move = 1;
				} else if (ctrl.isPush(Controller.BUTTON_E)) {
					move = 2;
				}

				if (move != 0) {
					initialRotateLastDirection = move;
					initialRotateContinuousUse = true;
				}
			}

			if (!ruleopt.rotateButtonAllowDouble && move == 2) {
				move = -1;
			}
			if (!ruleopt.rotateButtonAllowReverse && move == 1) {
				move = -1;
			}
			if (isRotateButtonDefaultRight() && move != 2) {
				move = move * -1;
			}

			if (move != 0) {
				// Direction after rotationI decided to
				int rt = getRotateDirection(move);

				// rotationYou can determine whether the
				if (!nowPieceObject.checkCollision(nowPieceX, nowPieceY, rt, field)) {
					// WallkickWithoutrotationwhen you can
					rotated = true;
					kickused = false;
					nowPieceObject.direction = rt;
					nowPieceObject.updateConnectData();
				} else if (ruleopt.rotateWallkick && wallkick != null
						&& (initialRotateDirection == 0 || ruleopt.rotateInitialWallkick)
						&& (ruleopt.lockresetLimitOver != RuleOptions.LOCKRESET_LIMIT_OVER_NOWALLKICK
								|| !isRotateCountExceed())) {
					// WallkickAttempt to
					boolean allowUpward = ruleopt.rotateMaxUpwardWallkick < 0
							|| nowUpwardWallkickCount < ruleopt.rotateMaxUpwardWallkick;
					WallkickResult kick = wallkick.executeWallkick(nowPieceX, nowPieceY, move, nowPieceObject.direction,
							rt, allowUpward, nowPieceObject, field, ctrl);

					if (kick != null) {
						rotated = true;
						kickused = true;
						nowWallkickCount++;
						if (kick.isUpward()) {
							nowUpwardWallkickCount++;
						}
						nowPieceObject.direction = kick.direction();
						nowPieceObject.updateConnectData();
						nowPieceX += kick.offsetX();
						nowPieceY += kick.offsetY();

						if (ruleopt.lockresetWallkick && !isRotateCountExceed()) {
							lockDelayNow = 0;
							nowPieceObject.setDarkness(0f);
						}
					}
				}

				// Domino Quick Turn
				if (!rotated && dominoQuickTurn && nowPieceObject.id == Piece.PIECE_I2
						&& nowPieceRotateFailCount >= 1) {
					rt = getRotateDirection(2);
					rotated = true;
					nowPieceObject.direction = rt;
					nowPieceObject.updateConnectData();
					nowPieceRotateFailCount = 0;

					if (nowPieceObject.checkCollision(nowPieceX, nowPieceY, rt, field)) {
						nowPieceY--;
					} else if (onGroundBeforeRotate) {
						nowPieceY++;
					}
				}

				if (rotated) {
					// rotationSuccess
					nowPieceBottomY = nowPieceObject.getBottom(nowPieceX, nowPieceY, field);

					if (ruleopt.lockresetRotate && !isRotateCountExceed()) {
						lockDelayNow = 0;
						nowPieceObject.setDarkness(0f);
					}

					if (onGroundBeforeRotate) {
						extendedRotateCount++;
						lastmove = LastMove.ROTATE_GROUND;
					} else {
						lastmove = LastMove.ROTATE_AIR;
					}

					if (initialRotateDirection == 0) {
						playSE("rotate");
					}

					nowPieceRotateCount++;
					if (ending == 0 || staffrollEnableStatistics) {
						statistics.totalPieceRotate++;
					}
				} else {
					// rotationFailure
					playSE("rotfail");
					nowPieceRotateFailCount++;
				}
			}
			initialRotateDirection = 0;

			// game over check
			if (statc[0] == 0 && nowPieceObject.checkCollision(nowPieceX, nowPieceY, field)) {
				// BlockSo if you can shift on the position of the emergence of
				for (int i = 0; i < ruleopt.pieceEnterMaxDistanceY; i++) {
					if (nowPieceObject.big) {
						nowPieceY -= 2;
					} else {
						nowPieceY--;
					}

					if (!nowPieceObject.checkCollision(nowPieceX, nowPieceY, field)) {
						nowPieceBottomY = nowPieceObject.getBottom(nowPieceX, nowPieceY, field);
						break;
					}
				}

				// Death
				if (nowPieceObject.checkCollision(nowPieceX, nowPieceY, field)) {
					nowPieceObject.placeToField(nowPieceX, nowPieceY, field);
					nowPieceObject = null;
					stat = Status.GAMEOVER;
					if (ending == 2 && staffrollNoDeath) {
						stat = Status.NOTHING;
					}
					resetStatc();
					return;
				}
			}

		}

		int move = 0;
		boolean sidemoveflag = false; // This frame I moved next totrue

		if (statc[0] > 0 || ruleopt.moveFirstFrame) {
			// Lateral motion
			boolean onGroundBeforeMove = nowPieceObject.checkCollision(nowPieceX, nowPieceY + 1, field);

			move = moveDirection;

			if (statc[0] == 0 && delayCancel) {
				if (delayCancelMoveLeft) {
					move = -1;
				}
				if (delayCancelMoveRight) {
					move = 1;
				}
				dasCount = 0;
				// delayCancel = false;
				delayCancelMoveLeft = false;
				delayCancelMoveRight = false;
			} else if (statc[0] == 1 && delayCancel && dasCount < getDAS()) {
				move = 0;
				delayCancel = false;
			}

			if (move != 0) {
				sidemoveflag = true;
			}

			if (big && bigmove) {
				move *= 2;
			}

			if (move != 0 && dasCount == 0) {
				shiftLock = 0;
			}

			if (move != 0 && (dasCount == 0 || dasCount >= getDAS())) {
				shiftLock &= ctrl.getButtonBit();

				if (shiftLock == 0) {
					if (dasSpeedCount >= getDASDelay() || dasCount == 0) {
						if (dasCount > 0) {
							dasSpeedCount = 1;
						}

						if (!nowPieceObject.checkCollision(nowPieceX + move, nowPieceY, field)) {
							nowPieceX += move;

							if (getDASDelay() == 0 && dasCount > 0
									&& !nowPieceObject.checkCollision(nowPieceX + move, nowPieceY, field)) {
								dasRepeat = true;
								dasInstant = true;
							}

							// log.debug("Successful movement: move="+move);

							if (ruleopt.lockresetMove && !isMoveCountExceed()) {
								lockDelayNow = 0;
								nowPieceObject.setDarkness(0f);
							}

							nowPieceMoveCount++;
							if (ending == 0 || staffrollEnableStatistics) {
								statistics.totalPieceMove++;
							}
							nowPieceBottomY = nowPieceObject.getBottom(nowPieceX, nowPieceY, field);

							if (onGroundBeforeMove) {
								extendedMoveCount++;
								lastmove = LastMove.SLIDE_GROUND;
								playSE("slide");
							} else {
								lastmove = LastMove.SLIDE_AIR;
							}

							if (!dasInstant) {
								playSE("move");
							}

						} else if (ruleopt.dasChargeOnBlockedMove) {
							dasCount = getDAS();
							dasSpeedCount = getDASDelay();
						}
					} else {
						dasSpeedCount++;
					}
				}
			}

			if (!dasRepeat || version.isLower(7, 6, 0)) {
				// Hard drop
				if (ctrl.isPress(getUp()) && !harddropContinuousUse && ruleopt.harddropEnable
						&& (isDiagonalMoveEnabled() || !sidemoveflag) && (ruleopt.moveUpAndDown || !updown)
						&& nowPieceY < nowPieceBottomY) {
					harddropFall += nowPieceBottomY - nowPieceY;

					if (nowPieceY != nowPieceBottomY) {
						nowPieceY = nowPieceBottomY;
						playSE("harddrop");
					}

					if (owner.mode != null) {
						owner.mode.afterHardDropFall(this, playerID, harddropFall);
					}
					owner.receiver.afterHardDropFall(this, playerID, harddropFall);

					lastmove = LastMove.FALL_SELF;
					if (ruleopt.lockresetFall) {
						lockDelayNow = 0;
						nowPieceObject.setDarkness(0f);
						extendedMoveCount = 0;
						extendedRotateCount = 0;
					}
				}

				if (!ruleopt.softdropGravitySpeedLimit || ruleopt.softdropSpeed < 1.0f) {
					// Old Soft Drop codes
					if (ctrl.isPress(getDown()) && !softdropContinuousUse && ruleopt.softdropEnable
							&& (isDiagonalMoveEnabled() || !sidemoveflag) && (ruleopt.moveUpAndDown || !updown)) {
						if (ruleopt.softdropMultiplyNativeSpeed || speed.denominator <= 0) {
							gcount += (int) (speed.gravity * ruleopt.softdropSpeed);
						} else {
							gcount += (int) (speed.denominator * ruleopt.softdropSpeed);
						}

						softdropUsed = true;
					}
				} else // New Soft Drop codes
				if (ctrl.isPress(getDown()) && !softdropContinuousUse && ruleopt.softdropEnable
						&& (isDiagonalMoveEnabled() || !sidemoveflag) && (ruleopt.moveUpAndDown || !updown)
						&& (ruleopt.softdropMultiplyNativeSpeed
								|| speed.gravity < (int) (speed.denominator * ruleopt.softdropSpeed))) {
					if (ruleopt.softdropMultiplyNativeSpeed || speed.denominator <= 0) {
						// gcount += (int)(speed.gravity * ruleopt.softdropSpeed)
						gcount = (int) (speed.gravity * ruleopt.softdropSpeed);
					} else {
						// gcount += (int)(speed.denominator * ruleopt.softdropSpeed)
						gcount = (int) (speed.denominator * ruleopt.softdropSpeed);
					}

					softdropUsed = true;
				} else {
					// Fall
					// This prevents soft drop from adding to the gravity speed.
					gcount += speed.gravity;
				}
			}

			if (ending == 0 || staffrollEnableStatistics) {
				statistics.totalPieceActiveTime++;
			}
		}

		if (!ruleopt.softdropGravitySpeedLimit || ruleopt.softdropSpeed < 1.0f) {
			gcount += speed.gravity; // Part of Old Soft Drop
		}

		while (gcount >= speed.denominator || speed.gravity < 0) {
			if (!nowPieceObject.checkCollision(nowPieceX, nowPieceY + 1, field)) {
				if (speed.gravity >= 0) {
					gcount -= speed.denominator;
				}
				nowPieceY++;

				if (ruleopt.lockresetFall) {
					lockDelayNow = 0;
					nowPieceObject.setDarkness(0f);
				}

				if (lastmove != LastMove.ROTATE_GROUND && lastmove != LastMove.SLIDE_GROUND
						&& lastmove != LastMove.FALL_SELF) {
					extendedMoveCount = 0;
					extendedRotateCount = 0;
				}

				if (softdropUsed) {
					lastmove = LastMove.FALL_SELF;
					softdropFall++;
					softdropFallNow++;
					playSE("softdrop");
				} else {
					lastmove = LastMove.FALL_AUTO;
				}
			} else {
				break;
			}
		}

		if (softdropFallNow > 0) {
			if (owner.mode != null) {
				owner.mode.afterSoftDropFall(this, playerID, softdropFallNow);
			}
			owner.receiver.afterSoftDropFall(this, playerID, softdropFallNow);
		}

		// And fixed ground
		if (nowPieceObject.checkCollision(nowPieceX, nowPieceY + 1, field)
				&& (statc[0] > 0 || ruleopt.moveFirstFrame)) {
			if (lockDelayNow == 0 && getLockDelay() > 0) {
				playSE("step");
			}

			if (lockDelayNow < getLockDelay()) {
				lockDelayNow++;
			}

			if (getLockDelay() >= 99 && lockDelayNow > 98) {
				lockDelayNow = 98;
			}

			if (lockDelayNow < getLockDelay()) {
				if (lockDelayNow >= getLockDelay() - 1) {
					nowPieceObject.setDarkness(0.5f);
				} else {
					nowPieceObject.setDarkness(lockDelayNow * 7f / getLockDelay() * 0.05f);
				}
			}

			if (getLockDelay() != 0) {
				gcount = speed.gravity;
			}

			// trueI fixed immediately becomes
			boolean instantlock = false;

			// Hard dropFixation
			if (ctrl.isPress(getUp()) && !harddropContinuousUse && ruleopt.harddropEnable
					&& (isDiagonalMoveEnabled() || !sidemoveflag) && (ruleopt.moveUpAndDown || !updown)
					&& ruleopt.harddropLock) {
				harddropContinuousUse = true;
				manualLock = true;
				instantlock = true;
			}

			// Soft dropFixation
			if (ctrl.isPress(getDown()) && !softdropContinuousUse && ruleopt.softdropEnable
					&& (isDiagonalMoveEnabled() || !sidemoveflag) && (ruleopt.moveUpAndDown || !updown)
					&& ruleopt.softdropLock) {
				softdropContinuousUse = true;
				manualLock = true;
				instantlock = true;
			}

			// Soft-drop fixed in the ground state
			if (ctrl.isPush(getDown()) && ruleopt.softdropEnable && (isDiagonalMoveEnabled() || !sidemoveflag)
					&& (ruleopt.moveUpAndDown || !updown) && ruleopt.softdropSurfaceLock) {
				softdropContinuousUse = true;
				manualLock = true;
				instantlock = true;
			}

			if (manualLock && ruleopt.shiftLockEnable) {
				// bit 1 and 2 are button_up and button_down currently
				shiftLock = ctrl.getButtonBit() & 3;
			}

			// &amp; MobilerotationcountLimit exceeded
			if (ruleopt.lockresetLimitOver == RuleOptions.LOCKRESET_LIMIT_OVER_INSTANT
					&& (isMoveCountExceed() || isRotateCountExceed())) {
				instantlock = true;
			}

			// Immediately fixed ground
			if (getLockDelay() == 0 && (gcount >= speed.denominator || speed.gravity < 0)) {
				instantlock = true;
			}

			// Fixation
			if (lockDelayNow >= getLockDelay() && getLockDelay() > 0 || instantlock) {
				if (ruleopt.lockflash > 0) {
					nowPieceObject.setDarkness(-0.8f);
				}

				// T-Spin判定
				if ((lastmove == LastMove.ROTATE_GROUND || lastmove == LastMove.ROTATE_AIR) && tspinEnable) {
					if (useAllSpinBonus) {
						setAllSpin(nowPieceX, nowPieceY, nowPieceObject, field);
					} else {
						setTSpin(nowPieceX, nowPieceY, nowPieceObject, field);
					}
				}

				nowPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, true);

				boolean partialLockOut = nowPieceObject.isPartialLockOut(nowPieceX, nowPieceY, field);
				boolean put = nowPieceObject.placeToField(nowPieceX, nowPieceY, field);

				playSE("lock");

				holdDisable = false;

				if (ending == 0 || staffrollEnableStatistics) {
					statistics.totalPieceLocked++;
				}

				switch (clearMode) {
				case LINE:
					lineClearing = field.checkLineNoFlag();
					break;
				case COLOR:
					lineClearing = field.checkColor(colorClearSize, false, garbageColorClear, gemSameColor,
							ignoreHidden);
					break;
				case LINE_COLOR:
					lineClearing = field.checkLineColor(colorClearSize, false, lineColorDiagonals, gemSameColor);
					break;
				case GEM_COLOR:
					lineClearing = field.gemColorCheck(colorClearSize, false, garbageColorClear, ignoreHidden);
					break;
				case null:
				default:
					break;
				}
				chain = 0;
				lineGravityTotalLines = 0;

				if (lineClearing == 0) {
					combo = 0;

					if (tspin) {
						playSE("tspin0");

						if (ending == 0 || staffrollEnableStatistics) {
							if (tspinmini) {
								statistics.totalTSpinZeroMini++;
							} else {
								statistics.totalTSpinZero++;
							}
						}
					}

					if (owner.mode != null) {
						owner.mode.calcScore(this, playerID, lineClearing);
					}
					owner.receiver.calcScore(this, playerID, lineClearing);
				}

				if (owner.mode != null) {
					owner.mode.pieceLocked(this, playerID, lineClearing);
				}
				owner.receiver.pieceLocked(this, playerID, lineClearing);

				dasRepeat = false;
				dasInstant = false;

				// Next 処理を決める(Mode 側でステータスを弄っている場合は何もしない)
				if (stat == Status.MOVE || version.isLower(6, 4, 0)) {
					resetStatc();

					if (ending == 1 && version.isGreater(6, 6, 0)) {
						// Ending
						stat = Status.ENDINGSTART;
					} else if (!put && ruleopt.fieldLockoutDeath
							|| partialLockOut && ruleopt.fieldPartialLockoutDeath) {
						// 画面外に置いて死亡
						stat = Status.GAMEOVER;
						if (ending == 2 && staffrollNoDeath) {
							stat = Status.NOTHING;
						}
					} else if ((lineGravityType == LineGravity.CASCADE || lineGravityType == LineGravity.CASCADE_SLOW)
							&& !connectBlocks) {
						stat = Status.LINECLEAR;
						statc[0] = getLineDelay();
						statLineClear();
					} else if (lineClearing > 0 && (ruleopt.lockflash <= 0 || !ruleopt.lockflashBeforeLineClear)) {
						// Line clear
						stat = Status.LINECLEAR;
						statLineClear();
					} else if ((getARE() > 0 || lagARE || ruleopt.lockflashBeforeLineClear) && ruleopt.lockflash > 0
							&& ruleopt.lockflashOnlyFrame) {
						// AREあり (光あり）
						stat = Status.LOCKFLASH;
					} else if (getARE() > 0 || lagARE) {
						// ARESome (No light)
						statc[1] = getARE();
						stat = Status.ARE;
					} else if (interruptItemNumber != INTERRUPTITEM_NONE) {
						// Effective treatment interruption item
						nowPieceObject = null;
						interruptItemPreviousStat = Status.MOVE;
						stat = Status.INTERRUPTITEM;
					} else {
						// AREなし
						stat = Status.MOVE;
						if (!ruleopt.moveFirstFrame) {
							statMove();
						}
					}
				}
				return;
			}
		}

		// Horizontal reservoir
		if ((statc[0] > 0 || ruleopt.dasInMoveFirstFrame) && moveDirection != 0 && moveDirection == dasDirection
				&& (dasCount < getDAS() || getDAS() <= 0)) {
			dasCount++;
		}
		statc[0]++;
	}

	/**
	 * BlockSparkling happens when fixed immediately after
	 */
	public void statLockFlash() {
		// event 発生
		if (owner.mode != null && owner.mode.onLockFlash(this, playerID)) {
			return;
		}

		owner.receiver.onLockFlash(this, playerID);

		statc[0]++;

		checkDropContinuousUse();

		// Horizontal reservoir
		if (ruleopt.dasInLockFlash) {
			padRepeat();
		} else if (ruleopt.dasRedirectInDelay) {
			dasRedirect();
		}

		// Next Status
		if (statc[0] >= ruleopt.lockflash) {
			resetStatc();

			if (lineClearing > 0) {
				// Line clear
				stat = Status.LINECLEAR;
				statLineClear();
			} else {
				// ARE
				statc[1] = getARE();
				stat = Status.ARE;
			}
		}
	}

	/**
	 * Line clearProcessing
	 */
	public void statLineClear() {
		// event 発生
		if (owner.mode != null && owner.mode.onLineClear(this, playerID)) {
			return;
		}

		owner.receiver.onLineClear(this, playerID);

		checkDropContinuousUse();

		// Horizontal reservoir
		if (ruleopt.dasInLineClear) {
			padRepeat();
		} else if (ruleopt.dasRedirectInDelay) {
			dasRedirect();
		}

		// First frame
		if (statc[0] == 0) {
			if (sticky > 0) {
				field.setBlockLinkByColor();
			}
			if (sticky == 2) {
				field.setAllAttribute(Block.BLOCK_ATTRIBUTE_IGNORE_BLOCKLINK, true);
			}
			// Line clear flagを設定
			switch (clearMode) {
			case LINE:
				lineClearing = field.checkLine();
				break;
			case COLOR:
				lineClearing = field.checkColor(colorClearSize, true, garbageColorClear, gemSameColor, ignoreHidden);
				break;
			case LINE_COLOR:
				lineClearing = field.checkLineColor(colorClearSize, true, lineColorDiagonals, gemSameColor);
				break;
			case GEM_COLOR:
				lineClearing = field.gemColorCheck(colorClearSize, true, garbageColorClear, ignoreHidden);
				break;
			case null:
			default:
				break;
			}

			// LinescountI decided to
			int linesCleared = lineClearing;
			if (big && bighalf) {
				linesCleared >>= 1;
				// if(li > 4) li = 4;
			}

			if (tspin) {
				playSE("tspin" + linesCleared);

				if (ending == 0 || staffrollEnableStatistics) {
					if (linesCleared == 1 && tspinmini) {
						statistics.totalTSpinSingleMini++;
					}
					if (linesCleared == 1 && !tspinmini) {
						statistics.totalTSpinSingle++;
					}
					if (linesCleared == 2 && tspinmini) {
						statistics.totalTSpinDoubleMini++;
					}
					if (linesCleared == 2 && !tspinmini) {
						statistics.totalTSpinDouble++;
					}
					if (linesCleared == 3) {
						statistics.totalTSpinTriple++;
					}
				}
			} else {
				if (clearMode == ClearType.LINE) {
					playSE("erase" + linesCleared);
				}

				if (ending == 0 || staffrollEnableStatistics) {
					if (linesCleared == 1) {
						statistics.totalSingle++;
					}
					if (linesCleared == 2) {
						statistics.totalDouble++;
					}
					if (linesCleared == 3) {
						statistics.totalTriple++;
					}
					if (linesCleared == 4) {
						statistics.totalFour++;
					}
				}
			}

			// B2B bonus
			if (b2bEnable) {
				if (tspin || linesCleared >= 4) {
					b2bcount++;

					if (b2bcount == 1) {
						playSE("b2b_start");
					} else {
						b2b = true;
						playSE("b2b_continue");

						if (ending == 0 || staffrollEnableStatistics) {
							if (linesCleared == 4) {
								statistics.totalB2BFour++;
							} else {
								statistics.totalB2BTSpin++;
							}
						}
					}
				} else if (b2bcount != 0) {
					b2b = false;
					b2bcount = 0;
					playSE("b2b_end");
				}
			}

			// Combo
			if (comboType != COMBO_TYPE_DISABLE && chain == 0) {
				if (comboType == COMBO_TYPE_NORMAL || comboType == COMBO_TYPE_DOUBLE && linesCleared >= 2) {
					combo++;
				}

				if (combo >= 2) {
					int cmbse = combo - 1;
					if (cmbse > 20) {
						cmbse = 20;
					}
					playSE("combo" + cmbse);
				}

				if ((ending == 0 || staffrollEnableStatistics) && combo > statistics.maxCombo) {
					statistics.maxCombo = combo;
				}
			}

			lineGravityTotalLines += lineClearing;

			if (ending == 0 || staffrollEnableStatistics) {
				statistics.lines += linesCleared;
			}

			if (field.getHowManyGemClears() > 0) {
				playSE("gem");
			}

			// Calculate score
			if (owner.mode != null) {
				owner.mode.calcScore(this, playerID, linesCleared);
			}
			owner.receiver.calcScore(this, playerID, linesCleared);

			// Blockを消す演出を出す (まだ実際には消えていない）
			if (clearMode == ClearType.LINE) {
				for (int i = 0; i < field.getHeight(); i++) {
					if (field.getLineFlag(i)) {
						for (int j = 0; j < field.getWidth(); j++) {
							Block blk = field.getBlock(j, i);

							if (blk != null) {
								if (owner.mode != null) {
									owner.mode.blockBreak(this, playerID, j, i, blk);
								}
								owner.receiver.blockBreak(this, playerID, j, i, blk);
							}
						}
					}
				}
			} else if (clearMode == ClearType.LINE_COLOR || clearMode == ClearType.COLOR
					|| clearMode == ClearType.GEM_COLOR) {
				for (int y = 0; y < field.getHeight(); y++) {
					for (int x = 0; x < field.getWidth(); x++) {
						Block block = field.getBlock(x, y);
						if (block == null) {
							continue;
						}
						if (block.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE)) {
							if (owner.mode != null) {
								owner.mode.blockBreak(this, playerID, x, y, block);
							}
							if (displaySize == DisplaySize.BIG) {
								owner.receiver.blockBreak(this, playerID, 2 * x, 2 * y, block);
								owner.receiver.blockBreak(this, playerID, 2 * x + 1, 2 * y, block);
								owner.receiver.blockBreak(this, playerID, 2 * x, 2 * y + 1, block);
								owner.receiver.blockBreak(this, playerID, 2 * x + 1, 2 * y + 1, block);
							} else {
								owner.receiver.blockBreak(this, playerID, x, y, block);
							}
						}
					}
				}
			}

			// Blockを消す
			switch (clearMode) {
			case LINE:
				field.clearLine();
				break;
			case COLOR:
				field.clearColor(colorClearSize, garbageColorClear, gemSameColor, ignoreHidden);
				break;
			case LINE_COLOR:
				field.clearLineColor(colorClearSize, lineColorDiagonals, gemSameColor);
				break;
			case GEM_COLOR:
				lineClearing = field.gemClearColor(colorClearSize, garbageColorClear, ignoreHidden);
				break;
			case null:
			default:
				break;
			}
		}

		// Linesを1段落とす
		if (lineGravityType == LineGravity.NATIVE && getLineDelay() >= lineClearing - 1
				&& statc[0] >= getLineDelay() - (lineClearing - 1) && ruleopt.lineFallAnim) {
			field.downFloatingBlocksSingleLine();
		}

		// Line delay cancel check
		delayCancelMoveLeft = ctrl.isPush(Controller.BUTTON_LEFT);
		delayCancelMoveRight = ctrl.isPush(Controller.BUTTON_RIGHT);

		boolean moveCancel = ruleopt.lineCancelMove
				&& (ctrl.isPush(getUp()) || ctrl.isPush(getDown()) || delayCancelMoveLeft || delayCancelMoveRight);
		boolean rotateCancel = ruleopt.lineCancelRotate
				&& (ctrl.isPush(Controller.BUTTON_A) || ctrl.isPush(Controller.BUTTON_B)
						|| ctrl.isPush(Controller.BUTTON_C) || ctrl.isPush(Controller.BUTTON_E));
		boolean holdCancel = ruleopt.lineCancelHold && ctrl.isPush(Controller.BUTTON_D);

		delayCancel = moveCancel || rotateCancel || holdCancel;

		if (statc[0] < getLineDelay() && delayCancel) {
			statc[0] = getLineDelay();
		}

		// Next Status
		if (statc[0] >= getLineDelay()) {
			// Cascade
			if (lineGravityType == LineGravity.CASCADE || lineGravityType == LineGravity.CASCADE_SLOW) {
				if (statc[6] < getCascadeDelay()) {
					statc[6]++;
					return;
				} else if (field.doCascadeGravity(lineGravityType)) {
					statc[6] = 0;
					return;
				} else if (statc[6] < getCascadeClearDelay()) {
					if (sticky > 0) {
						field.setBlockLinkByColor();
					}
					statc[6]++;
					return;
				} else if (clearMode == ClearType.LINE && field.checkLineNoFlag() > 0 || clearMode == ClearType.COLOR
						&& field.checkColor(colorClearSize, false, garbageColorClear, gemSameColor, ignoreHidden) > 0
						|| clearMode == ClearType.LINE_COLOR
								&& field.checkLineColor(colorClearSize, false, lineColorDiagonals, gemSameColor) > 0
						|| clearMode == ClearType.GEM_COLOR
								&& field.gemColorCheck(colorClearSize, false, garbageColorClear, ignoreHidden) > 0) {
					tspin = false;
					tspinmini = false;
					chain++;
					if (chain > statistics.maxChain) {
						statistics.maxChain = chain;
					}
					statc[0] = 0;
					statc[6] = 0;
					return;
				}
			}

			boolean skip = false;
			if (owner.mode != null) {
				skip = owner.mode.lineClearEnd(this, playerID);
			}
			owner.receiver.lineClearEnd(this, playerID);
			if (sticky > 0) {
				field.setBlockLinkByColor();
			}
			if (sticky == 2) {
				field.setAllAttribute(Block.BLOCK_ATTRIBUTE_IGNORE_BLOCKLINK, true);
			}

			if (!skip) {
				if (lineGravityType == LineGravity.NATIVE) {
					field.downFloatingBlocks();
				}
				playSE("linefall");

				field.lineColorsCleared = null;

				if (stat == Status.LINECLEAR || version.isLower(6, 4, 0)) {
					resetStatc();
					if (ending == 1) {
						// Ending
						stat = Status.ENDINGSTART;
					} else if (getARELine() > 0 || lagARE) {
						// ARESome
						statc[0] = 0;
						statc[1] = getARELine();
						statc[2] = 1;
						stat = Status.ARE;
					} else if (interruptItemNumber != INTERRUPTITEM_NONE) {
						// Effective treatment interruption item
						nowPieceObject = null;
						interruptItemPreviousStat = Status.MOVE;
						stat = Status.INTERRUPTITEM;
					} else {
						// ARENo
						nowPieceObject = null;
						if (version.isLower(7, 5, 0)) {
							initialRotate(); // XXX: Weird IRS thing on lines cleared but no ARE
						}
						stat = Status.MOVE;
					}
				}
			}

			return;
		}

		statc[0]++;
	}

	public int getCascadeDelay() {
		return cascadeDelay;
	}

	public int getCascadeClearDelay() {
		return cascadeClearDelay;
	}

	/**
	 * AREProcessing during
	 */
	public void statARE() {
		// event 発生
		if (owner.mode != null && owner.mode.onARE(this, playerID)) {
			return;
		}

		owner.receiver.onARE(this, playerID);

		statc[0]++;

		checkDropContinuousUse();

		// ARE cancel check
		delayCancelMoveLeft = ctrl.isPush(Controller.BUTTON_LEFT);
		delayCancelMoveRight = ctrl.isPush(Controller.BUTTON_RIGHT);

		boolean moveCancel = ruleopt.areCancelMove
				&& (ctrl.isPush(getUp()) || ctrl.isPush(getDown()) || delayCancelMoveLeft || delayCancelMoveRight);
		boolean rotateCancel = ruleopt.areCancelRotate
				&& (ctrl.isPush(Controller.BUTTON_A) || ctrl.isPush(Controller.BUTTON_B)
						|| ctrl.isPush(Controller.BUTTON_C) || ctrl.isPush(Controller.BUTTON_E));
		boolean holdCancel = ruleopt.areCancelHold && ctrl.isPush(Controller.BUTTON_D);

		delayCancel = moveCancel || rotateCancel || holdCancel;

		if (statc[0] < statc[1] && delayCancel) {
			statc[0] = statc[1];
		}

		// Horizontal reservoir
		if (ruleopt.dasInARE && (statc[0] < statc[1] - 1 || ruleopt.dasInARELastFrame)) {
			padRepeat();
		} else if (ruleopt.dasRedirectInDelay) {
			dasRedirect();
		}

		// Next Status
		if (statc[0] >= statc[1] && !lagARE) {
			nowPieceObject = null;
			resetStatc();

			if (interruptItemNumber != INTERRUPTITEM_NONE) {
				// 中断効果のあるアイテム処理
				interruptItemPreviousStat = Status.MOVE;
				stat = Status.INTERRUPTITEM;
			} else {
				// BlockPeace movement process
				initialRotate();
				stat = Status.MOVE;
			}
		}
	}

	/**
	 * EndingRush processing
	 */
	public void statEndingStart() {
		// event 発生
		if (owner.mode != null && owner.mode.onEndingStart(this, playerID)) {
			return;
		}
		owner.receiver.onEndingStart(this, playerID);
		checkDropContinuousUse();

		// Horizontal reservoir
		if (ruleopt.dasInEndingStart) {
			padRepeat();
		} else if (ruleopt.dasRedirectInDelay) {
			dasRedirect();
		}

		if (statc[2] == 0) {
			timerActive = false;
			owner.bgmStatus.bgm = BGMusicStatus.BGM_NOTHING;
			playSE("endingstart");
			statc[2] = 1;
		}

		if (statc[0] < getLineDelay()) {
			statc[0]++;
		} else if (statc[1] < field.getHeight() * 6) {
			if (statc[1] % 6 == 0) {
				int y = field.getHeight() - statc[1] / 6;
				field.setLineFlag(y, true);

				for (int i = 0; i < field.getWidth(); i++) {
					Block blk = field.getBlock(i, y);

					if (blk != null && blk.color != Colors.BLOCK_COLOR_NONE) {
						if (owner.mode != null) {
							owner.mode.blockBreak(this, playerID, i, y, blk);
						}
						owner.receiver.blockBreak(this, playerID, i, y, blk);
						field.setBlockColor(i, y, Colors.BLOCK_COLOR_NONE);
					}
				}
			}

			statc[1]++;
		} else if (statc[0] < getLineDelay() + 2) {
			statc[0]++;
		} else {
			ending = 2;
			field.reset();
			resetStatc();

			if (staffrollEnable) {
				nowPieceObject = null;
				stat = Status.MOVE;
			} else {
				stat = Status.EXCELLENT;
			}
		}
	}

	/**
	 * Each gameMode Treatment of status that can be freely used
	 */
	public void statCustom() {
		// event 発生
		if (owner.mode != null && owner.mode.onCustom(this, playerID)) {
			return;
		}

		owner.receiver.onCustom(this, playerID);
	}

	/**
	 * EndingScreen
	 */
	public void statExcellent() {
		// event 発生
		if (owner.mode != null && owner.mode.onExcellent(this, playerID)) {
			return;
		}

		owner.receiver.onExcellent(this, playerID);

		if (statc[0] == 0) {
			gameEnded();
			owner.bgmStatus.fadesw = true;

			resetFieldVisible();

			playSE("excellent");
		}

		if (statc[0] >= 120 && ctrl.isPush(Controller.BUTTON_A)) {
			statc[0] = 600;
		}

		if (statc[0] >= 600 && statc[1] == 0) {
			resetStatc();
			stat = Status.GAMEOVER;
		} else {
			statc[0]++;
		}
	}

	/**
	 * game overProcessing
	 */
	public void statGameOver() {
		// event 発生
		if (owner.mode != null && owner.mode.onGameOver(this, playerID)) {
			return;
		}

		owner.receiver.onGameOver(this, playerID);

		if (lives <= 0) {
			// When I can not be recovered anymore
			if (statc[0] == 0) {
				gameEnded();
				blockShowOutlineOnly = false;
				if (owner.getPlayers() < 2) {
					owner.bgmStatus.bgm = BGMusicStatus.BGM_NOTHING;
				}

				if (field.isEmpty()) {
					statc[0] = field.getHeight() + 1;
				} else {
					resetFieldVisible();
				}
			}

			if (statc[0] < field.getHeight() + 1) {
				for (int i = 0; i < field.getWidth(); i++) {
					if (field.getBlockColor(i, field.getHeight() - statc[0]) != Colors.BLOCK_COLOR_NONE) {
						Block blk = field.getBlock(i, field.getHeight() - statc[0]);

						if (blk != null) {
							if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
								blk.color = Colors.BLOCK_COLOR_GRAY;
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
							}
							if (displaySize != DisplaySize.SMALL) {
								blk.darkness = 0.3f;
							}
							blk.elapsedFrames = -1;
						}
					}
				}
				statc[0]++;
			} else if (statc[0] == field.getHeight() + 1) {
				playSE("gameover");
				statc[0]++;
			} else if (statc[0] < field.getHeight() + 1 + 180) {
				if (statc[0] >= field.getHeight() + 1 + 60 && ctrl.isPush(Controller.BUTTON_A)) {
					statc[0] = field.getHeight() + 1 + 180;
				}

				statc[0]++;
			} else {
				if (!owner.replayMode || owner.replayRerecord) {
					owner.saveReplay();
				}

				for (int i = 0; i < owner.getPlayers(); i++) {
					if (i == playerID || gameoverAll) {
						if (owner.engine[i].field != null) {
							owner.engine[i].field.reset();
						}
						owner.engine[i].resetStatc();
						owner.engine[i].stat = Status.RESULT;
					}
				}
			}
		} else {
			// When it can be revived
			if (statc[0] == 0) {
				blockShowOutlineOnly = false;
				playSE("died");

				resetFieldVisible();

				for (int i = field.getHiddenHeight() * -1; i < field.getHeight(); i++) {
					for (int j = 0; j < field.getWidth(); j++) {
						if (field.getBlockColor(j, i) != Colors.BLOCK_COLOR_NONE) {
							field.setBlockColor(j, i, Colors.BLOCK_COLOR_GRAY);
						}
					}
				}

				statc[0] = 1;
			}

			if (!field.isEmpty()) {
				field.pushDown();
			} else if (statc[1] < getARE()) {
				statc[1]++;
			} else {
				lives--;
				resetStatc();
				stat = Status.MOVE;
			}
		}
	}

	/**
	 * Results screen
	 */
	public void statResult() {
		// Event
		if (owner.mode != null && owner.mode.onResult(this, playerID)) {
			return;
		}

		owner.receiver.onResult(this, playerID);

		// Turn-off in-game flags
		gameActive = false;
		timerActive = false;
		isInGame = false;

		// Cursor movement
		if (ctrl.isMenuRepeatKey(Controller.BUTTON_LEFT) || ctrl.isMenuRepeatKey(Controller.BUTTON_RIGHT)) {
			if (statc[0] == 0) {
				statc[0] = 1;
			} else {
				statc[0] = 0;
			}
			playSE("cursor");
		}

		// Confirm
		if (ctrl.isPush(Controller.BUTTON_A)) {
			playSE("decide");

			if (statc[0] == 0) {
				owner.reset();
			} else {
				quitflag = true;
			}
		}
	}

	/**
	 * fieldEdit screen
	 */
	public void statFieldEdit() {
		// event 発生
		if (owner.mode != null && owner.mode.onFieldEdit(this, playerID)) {
			return;
		}

		owner.receiver.onFieldEdit(this, playerID);

		fldeditFrames++;

		// Cursor movement
		if (ctrl.isMenuRepeatKey(Controller.BUTTON_LEFT, false) && !ctrl.isPress(Controller.BUTTON_C)) {
			playSE("move");
			fldeditX--;
			if (fldeditX < 0) {
				fldeditX = fieldWidth - 1;
			}
		}
		if (ctrl.isMenuRepeatKey(Controller.BUTTON_RIGHT, false) && !ctrl.isPress(Controller.BUTTON_C)) {
			playSE("move");
			fldeditX++;
			if (fldeditX > fieldWidth - 1) {
				fldeditX = 0;
			}
		}
		if (ctrl.isMenuRepeatKey(getUp(), false)) {
			playSE("move");
			fldeditY--;
			if (fldeditY < 0) {
				fldeditY = fieldHeight - 1;
			}
		}
		if (ctrl.isMenuRepeatKey(getDown(), false)) {
			playSE("move");
			fldeditY++;
			if (fldeditY > fieldHeight - 1) {
				fldeditY = 0;
			}
		}

		// Color selection
		if (ctrl.isMenuRepeatKey(Controller.BUTTON_LEFT, false) && ctrl.isPress(Controller.BUTTON_C)) {
			playSE("cursor");
			fldeditColor--;
			if (fldeditColor < Colors.BLOCK_COLOR_GRAY) {
				fldeditColor = Colors.BLOCK_COLOR_GEM_PURPLE;
			}
		}
		if (ctrl.isMenuRepeatKey(Controller.BUTTON_RIGHT, false) && ctrl.isPress(Controller.BUTTON_C)) {
			playSE("cursor");
			fldeditColor++;
			if (fldeditColor > Colors.BLOCK_COLOR_GEM_PURPLE) {
				fldeditColor = Colors.BLOCK_COLOR_GRAY;
			}
		}

		// Placement
		if (ctrl.isPress(Controller.BUTTON_A) && fldeditFrames > 10) {
			try {
				if (field.getBlockColor(fldeditX, fldeditY) != fldeditColor) {
					Block blk = new Block(fldeditColor, getSkin(),
							Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE);
					field.setBlock(fldeditX, fldeditY, blk);
					playSE("change");
				}
			} catch (Exception e) {
			}
		}

		// Elimination
		if (ctrl.isPress(Controller.BUTTON_D) && fldeditFrames > 10) {
			try {
				if (!field.getBlockEmpty(fldeditX, fldeditY)) {
					field.setBlockColor(fldeditX, fldeditY, Colors.BLOCK_COLOR_NONE);
					playSE("change");
				}
			} catch (Exception e) {
			}
		}

		// End
		if (ctrl.isPush(Controller.BUTTON_B) && fldeditFrames > 10) {
			stat = fldeditPreviousStat;
			if (owner.mode != null) {
				owner.mode.fieldEditExit(this, playerID);
			}
			owner.receiver.fieldEditExit(this, playerID);
		}
	}

	/**
	 * Effective treatment interruption Play items
	 */
	protected void statInterruptItem() {
		boolean contFlag = false; // Continue flag

		if (interruptItemNumber == INTERRUPTITEM_MIRROR) {
			// Miller
			contFlag = interruptItemMirrorProc();
		}

		if (!contFlag) {
			interruptItemNumber = INTERRUPTITEM_NONE;
			resetStatc();
			stat = interruptItemPreviousStat;
		}
	}

	/**
	 * Mirror operation
	 *
	 * @return When true,Process continues Miller
	 */
	protected boolean interruptItemMirrorProc() {
		if (statc[0] == 0) {
			// fieldCopy the backup
			interruptItemMirrorField = new Field(field);
			// fieldOfBlockTurn off all the
			field.reset();
		} else if (statc[0] >= 21 && statc[0] < 21 + field.getWidth() * 2 && statc[0] % 2 == 0) {
			// Inversion
			int x = (statc[0] - 20) / 2 - 1;

			for (int y = field.getHiddenHeight() * -1; y < field.getHeight(); y++) {
				field.setBlock(field.getWidth() - x - 1, y, interruptItemMirrorField.getBlock(x, y));
			}
		} else if (statc[0] < 21 + field.getWidth() * 2 + 5) {
			// Wait time
		} else {
			// End
			statc[0] = 0;
			interruptItemMirrorField = null;
			return false;
		}

		statc[0]++;
		return true;
	}
}

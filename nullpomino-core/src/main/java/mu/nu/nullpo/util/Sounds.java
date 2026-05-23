/**
 *
 */
package mu.nu.nullpo.util;

import lombok.experimental.UtilityClass;

/**
 * Constants for sound related things
 *
 * @author daell
 */
@UtilityClass
public class Sounds {

	public static final String B2B_CONTINUE = "b2b_continue";

	public static final String B2B_END = "b2b_end";

	public static final String B2B_START = "b2b_start";

	/** sound effect for bravo (field completely cleared) */
	public static final String BRAVO = "bravo";

	/** Sound for changing a value in the menu */
	public static final String CHANGE = "change";

	/** Sound effect for 'cool' achievement in TGM3 */
	public static final String COOL = "cool";

	/** sound effect during count down */
	public static final String COUNTDOWN = "countdown";

	/** Sound for moving to another entry in the menu */
	public static final String CURSOR = "cursor";

	/** sound effect for player being in danger (field is nearly full) */
	public static final String DANGER = "danger";

	/** Sound for selection a menu item */
	public static final String DECIDE = "decide";

	/** sound effect when player died */
	public static final String DIED = "died";

	public static final String ENDING_START = "endingstart";

	/** sound effect when one line was cleared */
	public static final String ERASE1 = "erase1";

	/** sound effect when two lines were cleared */
	public static final String ERASE2 = "erase2";

	/** sound effect when three lines were cleared */
	public static final String ERASE3 = "erase3";

	/** sound effect when four lines were cleared */
	public static final String ERASE4 = "erase4";

	/** Sound effect for 'excellent' achievement */
	public static final String EXCELLENT = "excellent";

	/** Sound effect for 'game over' state */
	public static final String GAME_OVER = "gameover";

	/** sound effect when garbage is added to the game */
	public static final String GARBAGE = "garbage";

	/** sound effect when gem block was cleared */
	public static final String GEM = "gem";

	/** sound effect for go (on the ready->go transition) */
	public static final String GO = "go";

	/** Sound effect for grading up in mania modes */
	public static final String GRADE_UP = "gradeup";

	public static final String HARDDROP = "harddrop";

	/** sound effect when the player uses hold */
	public static final String HOLD = "hold";

	/** sound effect when holding a piece failed */
	public static final String HOLD_FAIL = "holdfail";

	/** sound effect when hurry up lines are added to the field */
	public static final String HURRY_UP = "hurryup";

	/** sound effect when hold is used during ARE */
	public static final String INITIAL_HOLD = "initialhold";

	/** sound effect when a piece is rotated during ARE */
	public static final String INITIAL_ROTATE = "initialrotate";

	/** sound effect when current level is close to finish */
	public static final String LEVEL_STOP = "levelstop";

	/** sound for leveling up one level */
	public static final String LEVEL_UP = "levelup";

	public static final String LINE_FALL = "linefall";

	public static final String LOCK = "lock";

	public static final String MATCH_END = "matchend";

	/** Sound when a medal in mania modes is earned */
	public static final String MEDAL = "medal";

	/** sound effect when a piece is moved */
	public static final String MOVE = "move";

	/** sound effect when moving a piece failed */
	public static final String MOVE_FAIL = "movefail";

	/** sound effect when the game was paused / unpaused */
	public static final String PAUSE = "pause";

	public static final String PIECE_I = "piece0";

	public static final String PIECE_J = "piece5";

	public static final String PIECE_L = "piece1";

	public static final String PIECE_O = "piece2";

	public static final String PIECE_S = "piece6";

	public static final String PIECE_T = "piece4";

	public static final String PIECE_Z = "piece3";

	public static final String PIECE_I1 = "piece7";

	public static final String PIECE_I2 = "piece8";

	public static final String PIECE_I3 = "piece9";

	public static final String PIECE_L3 = "piece10";

	/** sound effect for ready (on ready->go transition) */
	public static final String READY = "ready";

	/** sound effect for 'regret' in TGM3 */
	public static final String REGRET = "regret";

	/** sound effect when a piece is rotated */
	public static final String ROTATE = "rotate";

	/** sound effect when piece rotation failed */
	public static final String ROTATE_FAIL = "rotfail";

	/** sound effect when a piece slides on the ground */
	public static final String SLIDE = "slide";

	/** sound effect when soft drop is used */
	public static final String SOFTDROP = "softdrop";

	/** sound effect when gold square was cleared */
	public static final String SQUARE_GOLD = "square_g";

	/** sound effect when silver square was cleared */
	public static final String SQUARE_SILVER = "square_s";

	/** sound effect for clearing a stage successfully */
	public static final String STAGE_CLEAR = "stageclear";

	/** sound effect when current stage failed */
	public static final String STAGE_FAIL = "stagefail";

	public static final String STEP = "step";

	/** Sound effect for t-spin 0 */
	public static final String TSPIN0 = "tspin0";

	/** Sound effect for t-spin 1 */
	public static final String TSPIN1 = "tspin1";

	/** Sound effect for t-spin 2 */
	public static final String TSPIN2 = "tspin2";

	/** Sound effect for t-spin 3 */
	public static final String TSPIN3 = "tspin3";

	/**
	 * Get the sound effect for a specific combo chain
	 *
	 * @param which type of combo (must be between 1 and 20 inclusive)
	 * @return the combo sound constant
	 * @throws IllegalArgumentException if argument is out of range
	 */
	public static String combo(int which) {
		if (which < 1 || which > 20) {
			throw new IllegalArgumentException("inavlid combo sound: " + which);
		}
		return "combo" + which;
	}

}

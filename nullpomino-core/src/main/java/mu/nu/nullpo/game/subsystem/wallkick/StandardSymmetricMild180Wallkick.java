package mu.nu.nullpo.game.subsystem.wallkick;

import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;

/**
 * SRS with symmetric I piece kicks and saner 180 kicks
 */
public class StandardSymmetricMild180Wallkick extends BaseStandardWallkick {

	// @formatter:off
	// Wallkick data
	private static final int[][][] WALLKICK_NORMAL_L = {
			{ { 1, 0 }, { 1, -1 }, { 0, 2 }, { 1, 2 } }, // 0>>3
			{ { 1, 0 }, { 1, 1 }, { 0, -2 }, { 1, -2 } }, // 1>>0
			{ { -1, 0 }, { -1, -1 }, { 0, 2 }, { -1, 2 } }, // 2>>1
			{ { -1, 0 }, { -1, 1 }, { 0, -2 }, { -1, -2 } }, // 3>>2
	};
	private static final int[][][] WALLKICK_NORMAL_R = {
			{ { -1, 0 }, { -1, -1 }, { 0, 2 }, { -1, 2 } }, // 0>>1
			{ { 1, 0 }, { 1, 1 }, { 0, -2 }, { 1, -2 } }, // 1>>2
			{ { 1, 0 }, { 1, -1 }, { 0, 2 }, { 1, 2 } }, // 2>>3
			{ { -1, 0 }, { -1, 1 }, { 0, -2 }, { -1, -2 } }, // 3>>0
	};
	private static final int[][][] WALLKICK_I_L = {
			{ { 2, 0 }, { -1, 0 }, { -1, -2 }, { 2, 1 } }, // 0>>3
			{ { 2, 0 }, { -1, 0 }, { 2, -1 }, { -1, 2 } }, // 1>>0
			{ { -2, 0 }, { 1, 0 }, { -2, -1 }, { 1, 1 } }, // 2>>1
			{ { 1, 0 }, { -2, 0 }, { 1, -2 }, { -2, 1 } }, // 3>>2
	};
	private static final int[][][] WALLKICK_I_R = {
			{ { -2, 0 }, { 1, 0 }, { 1, -2 }, { -2, 1 } }, // 0>>1
			{ { -1, 0 }, { 2, 0 }, { -1, -2 }, { 2, 1 } }, // 1>>2
			{ { 2, 0 }, { -1, 0 }, { 2, -1 }, { -1, 1 } }, // 2>>3
			{ { -2, 0 }, { 1, 0 }, { -2, -1 }, { 1, 2 } }, // 3>>0
	};
	private static final int[][][] WALLKICK_I2_L = {
			{ { 1, 0 }, { 0, -1 }, { 1, -2 } }, // 0>>3
			{ { 0, 1 }, { 1, 0 }, { 1, 1 } }, // 1>>0
			{ { -1, 0 }, { 0, 1 }, { -1, 0 } }, // 2>>1
			{ { 0, -1 }, { -1, 0 }, { -1, 1 } }, // 3>>2
	};
	private static final int[][][] WALLKICK_I2_R = {
			{ { 0, -1 }, { -1, 0 }, { -1, -1 } }, // 0>>1
			{ { 1, 0 }, { 0, -1 }, { 1, 0 } }, // 1>>2
			{ { 0, 1 }, { 1, 0 }, { 1, -1 } }, // 2>>3
			{ { -1, 0 }, { 0, 1 }, { -1, 2 } }, // 3>>0
	};
	private static final int[][][] WALLKICK_I3_L = {
			{ { 1, 0 }, { -1, 0 }, { 0, 0 }, { 0, 0 } }, // 0>>3
			{ { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } }, // 1>>0
			{ { -1, 0 }, { 1, 0 }, { 0, 2 }, { 0, -2 } }, // 2>>1
			{ { 1, 0 }, { -1, 0 }, { 0, -1 }, { 0, 1 } }, // 3>>2
	};
	private static final int[][][] WALLKICK_I3_R = {
			{ { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } }, // 0>>1
			{ { 1, 0 }, { -1, 0 }, { 0, -2 }, { 0, 2 } }, // 1>>2
			{ { -1, 0 }, { 1, 0 }, { 0, 1 }, { 0, -1 } }, // 2>>3
			{ { -1, 0 }, { 1, 0 }, { 0, 0 }, { 0, 0 } }, // 3>>0
	};
	private static final int[][][] WALLKICK_L3_L = {
			{ { 0, -1 }, { 0, 1 } }, // 0>>3
			{ { 1, 0 }, { -1, 0 } }, // 1>>0
			{ { 0, 1 }, { 0, -1 } }, // 2>>1
			{ { -1, 0 }, { 1, 0 } }, // 3>>2
	};
	private static final int[][][] WALLKICK_L3_R = {
			{ { -1, 0 }, { 1, 0 } }, // 0>>1
			{ { 0, -1 }, { 0, 1 } }, // 1>>2
			{ { 1, 0 }, { -1, 0 } }, // 2>>3
			{ { 0, 1 }, { 0, -1 } }, // 3>>0
	};

	// 180-degree rotation wallkick data
	private static final int[][][] WALLKICK_NORMAL_180 = {
			{ { 1, 0 }, { -1, 0 }, { 0, -1 }, { 0, 1 }, { 0, -2 }, { 0, 2 } }, // 0>>2─ ┐
			{ { 0, 1 }, { 0, -1 }, { 0, -2 }, { 0, 2 }, { -1, 0 }, { 1, 0 } }, // 1>>3─ ┼ ┐
			{ { -1, 0 }, { 1, 0 }, { 0, 1 }, { 0, -1 }, { 0, 2 }, { 0, -2 } }, // 2>>0─ ┘ │
			{ { 0, 1 }, { 0, -1 }, { 0, -2 }, { 0, 2 }, { 1, 0 }, { -1, 0 } } // 3>>1─ ─ ┘
			// {{ 1, 0},{ 2, 0},{ 1, 1},{ 2, 1},{-1, 0},{-2, 0},{-1, 1},{-2, 1},{ 0,-1},{ 3, 0},{-3, 0}}, // 0>>2─ ┐
			// {{ 0, 1},{ 0, 2},{-1, 1},{-1, 2},{ 0,-1},{ 0,-2},{-1,-1},{-1,-2},{ 1, 0},{ 0, 3},{ 0,-3}}, // 1>>3─ ┼ ┐
			// {{-1, 0},{-2, 0},{-1,-1},{-2,-1},{ 1, 0},{ 2, 0},{ 1,-1},{ 2,-1},{ 0, 1},{-3, 0},{ 3, 0}}, // 2>>0─ ┘ │
			// {{ 0, 1},{ 0, 2},{ 1, 1},{ 1, 2},{ 0,-1},{ 0,-2},{ 1,-1},{ 1,-2},{-1, 0},{ 0, 3},{ 0,-3}}, // 3>>1─ ─ ┘
	};
	private static final int[][][] WALLKICK_I_180 = {
			{ { -1, 0 }, { -2, 0 }, { 1, 0 }, { 2, 0 } }, // 0>>2─ ┐
			{ { 0, 1 }, { 0, -1 }, { 0, -2 }, { 0, 2 } }, // 1>>3─ ┼ ┐
			{ { 1, 0 }, { 2, 0 }, { -1, 0 }, { -2, 0 } }, // 2>>0─ ┘ │
			{ { 0, 1 }, { 0, -1 }, { 0, -2 }, { 0, 2 } }, // 3>>1─ ─ ┘
			// {{-1, 0},{-2, 0},{ 1, 0},{ 2, 0},{ 0, 1}}, // 0>>2─ ┐
			// {{ 0, 1},{ 0, 2},{ 0,-1},{ 0,-2},{-1, 0}}, // 1>>3─ ┼ ┐
			// {{ 1, 0},{ 2, 0},{-1, 0},{-2, 0},{ 0,-1}}, // 2>>0─ ┘ │
			// {{ 0, 1},{ 0, 2},{ 0,-1},{ 0,-2},{ 1, 0}}, // 3>>1─ ─ ┘
	};
	// @formatter:on

	/*
	 * Get kick table
	 */
	@Override
	protected int[][][] getKickTable(int x, int y, int rtDir, int rtOld, int rtNew, boolean allowUpward, Piece piece,
			Field field) {
		return switch (rtDir) {
		case 2 -> // 180-degree rotation
			switch (piece.id) {
			case Piece.PIECE_I -> WALLKICK_I_180;
			default -> WALLKICK_NORMAL_180;
			};
		case -1 -> // Left rotation
			switch (piece.id) {
			case Piece.PIECE_I -> WALLKICK_I_L;
			case Piece.PIECE_I2 -> WALLKICK_I2_L;
			case Piece.PIECE_I3 -> WALLKICK_I3_L;
			case Piece.PIECE_L3 -> WALLKICK_L3_L;
			default -> WALLKICK_NORMAL_L;
			};
		case 1 -> // Right rotation
			switch (piece.id) {
			case Piece.PIECE_I -> WALLKICK_I_R;
			case Piece.PIECE_I2 -> WALLKICK_I2_R;
			case Piece.PIECE_I3 -> WALLKICK_I3_R;
			case Piece.PIECE_L3 -> WALLKICK_L3_R;
			default -> WALLKICK_NORMAL_R;
			};
		default -> null;
		};
	}
}

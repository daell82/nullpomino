package mu.nu.nullpo.gui.common;

import lombok.Setter;
import mu.nu.nullpo.game.GameEngine;
import mu.nu.nullpo.game.event.Renderer;
import mu.nu.nullpo.game.types.DisplaySize;

public abstract class AbstractRenderer<T> implements Renderer<T> {

	/** Field X position */
	private static final int[][][] NEW_FIELD_OFFSET_X = { //
			{ // TETROMINO
					{ 119, 247, 375, 503, 247, 375 }, // Small
					{ 32, 432, 432, 432, 432, 432 }, // Normal
					{ 16, 416, 416, 416, 416, 416 }, // Big
			}, { // AVALANCHE
					{ 119, 247, 375, 503, 247, 375 }, // Small
					{ 32, 432, 432, 432, 432, 432 }, // Normal
					{ 16, 352, 352, 352, 352, 352 }, // Big
			}, { // PHYSICIAN
					{ 119, 247, 375, 503, 247, 375 }, // Small
					{ 32, 432, 432, 432, 432, 432 }, // Normal
					{ 16, 416, 416, 416, 416, 416 }, // Big
			}, { // SPF
					{ 119, 247, 375, 503, 247, 375 }, // Small
					{ 32, 432, 432, 432, 432, 432 }, // Normal
					{ 16, 352, 352, 352, 352, 352 }, // Big
			}, };
	/** Field Y position */
	private static final int[][][] NEW_FIELD_OFFSET_Y = { //
			{ // TETROMINO
					{ 80, 80, 80, 80, 286, 286 }, // Small
					{ 32, 32, 32, 32, 32, 32 }, // Normal
					{ 8, 8, 8, 8, 8, 8 }, // Big
			}, { // AVALANCHE
					{ 80, 80, 80, 80, 286, 286 }, // Small
					{ 32, 32, 32, 32, 32, 32 }, // Normal
					{ 8, 8, 8, 8, 8, 8 }, // Big
			}, { // PHYSICIAN
					{ 80, 80, 80, 80, 286, 286 }, // Small
					{ 32, 32, 32, 32, 32, 32 }, // Normal
					{ 8, 8, 8, 8, 8, 8 }, // Big
			}, { // SPF
					{ 80, 80, 80, 80, 286, 286 }, // Small
					{ 32, 32, 32, 32, 32, 32 }, // Normal
					{ -8, -8, -8, -8, -8, -8 }, // Big
			}, };

	/** Field X position (Big side preview) */
	private static final int[][][] NEW_FIELD_OFFSET_X_BSP = { //
			{ // TETROMINO
					{ 208, 320, 432, 544, 320, 432 }, // Small
					{ 64, 400, 400, 400, 400, 400 }, // Normal
					{ 16, 352, 352, 352, 352, 352 }, // Big
			}, { // AVALANCHE
					{ 208, 320, 432, 544, 320, 432 }, // Small
					{ 64, 400, 400, 400, 400, 400 }, // Normal
					{ 16, 352, 352, 352, 352, 352 }, // Big
			}, { // PHYSICIAN
					{ 208, 320, 432, 544, 320, 432 }, // Small
					{ 64, 400, 400, 400, 400, 400 }, // Normal
					{ 16, 352, 352, 352, 352, 352 }, // Big
			}, { // SPF
					{ 208, 320, 432, 544, 320, 432 }, // Small
					{ 64, 400, 400, 400, 400, 400 }, // Normal
					{ 16, 352, 352, 352, 352, 352 }, // Big
			}, };
	/** Field Y position (Big side preview) */
	private static final int[][][] NEW_FIELD_OFFSET_Y_BSP = { //
			{ // TETROMINO
					{ 80, 80, 80, 80, 286, 286 }, // Small
					{ 32, 32, 32, 32, 32, 32 }, // Normal
					{ 8, 8, 8, 8, 8, 8 }, // Big
			}, { // AVALANCHE
					{ 80, 80, 80, 80, 286, 286 }, // Small
					{ 32, 32, 32, 32, 32, 32 }, // Normal
					{ 8, 8, 8, 8, 8, 8 }, // Big
			}, { // PHYSICIAN
					{ 80, 80, 80, 80, 286, 286 }, // Small
					{ 32, 32, 32, 32, 32, 32 }, // Normal
					{ 8, 8, 8, 8, 8, 8 }, // Big
			}, { // SPF
					{ 80, 80, 80, 80, 286, 286 }, // Small
					{ 32, 32, 32, 32, 32, 32 }, // Normal
					{ -16, -16, -16, -16, -16, -16 }, // Big
			}, };

	/** Background display */
	protected boolean showbg;

	/** Show meter */
	protected boolean showMeter;

	/** Outline ghost piece */
	protected boolean outlineGhost;

	/** Piece previews on sides */
	protected boolean sidenext;

	/** Use bigger side previews */
	protected boolean bigsidenext;

	@Setter
	protected T graphics;

	ResourceHolder resources;

	protected AbstractRenderer(T graphics) {
		this.graphics = graphics;
	}

	/**
	 * Get X position of field
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return X position of field
	 */
	@Override
	public int getFieldDisplayPositionX(GameEngine engine, int playerID) {
		int style = engine.owner.mode.getGameStyle().getMode();
		if (getNextDisplayType() == 2) {
			return NEW_FIELD_OFFSET_X_BSP[style][engine.displaySize.ordinal()][playerID];
		}
		return NEW_FIELD_OFFSET_X[style][engine.displaySize.ordinal()][playerID];
	}

	/**
	 * Get Y position of field
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return Y position of field
	 */
	@Override
	public int getFieldDisplayPositionY(GameEngine engine, int playerID) {
		int style = engine.owner.mode.getGameStyle().getMode();
		if (getNextDisplayType() == 2) {
			return NEW_FIELD_OFFSET_Y_BSP[style][engine.displaySize.ordinal()][playerID];
		}
		return NEW_FIELD_OFFSET_Y[style][engine.displaySize.ordinal()][playerID];
	}

	/**
	 * Get width of block image.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return Width of block image
	 */
	@Override
	public int getBlockGraphicsWidth(GameEngine engine, int playerID) {
		return engine.displaySize.getBlockSize();
	}

	/**
	 * Get height of block image.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return Height of block image
	 */
	@Override
	public int getBlockGraphicsHeight(GameEngine engine, int playerID) {
		return engine.displaySize.getBlockSize();
	}

	/**
	 * Get X position of score display area
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return X position of score display area
	 */
	public int getScoreDisplayPositionX(GameEngine engine, int playerID) {
		int xOffset = getNextDisplayType() == 2 ? 256 : 216;
		if (engine.displaySize == DisplaySize.BIG) {
			xOffset += 32;
		}
		return getFieldDisplayPositionX(engine, playerID) + xOffset;
	}

	/**
	 * Get Y position of score display area
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return Y position of score display area
	 */
	public int getScoreDisplayPositionY(GameEngine engine, int playerID) {
		return getFieldDisplayPositionY(engine, playerID) + 48;
	}

	/**
	 * Get type of piece preview
	 *
	 * @return 0=Above 1=Side Small 2=Side Big
	 */
	@Override
	public int getNextDisplayType() {
		if (sidenext) {
			return bigsidenext ? 2 : 1;
		}
		return 0;
	}

	/**
	 * Get maximum value of the meter.
	 *
	 * @param engine GameEngine
	 * @return Maximum value of the meter
	 */
	@Override
	public int getMeterMax(GameEngine engine) {
		if (!showMeter) {
			return 0;
		}
		return engine.fieldHeight * engine.displaySize.getBlockSize();
	}

	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		// does nothing
	}

	@Override
	public void renderLockFlash(GameEngine engine, int playerID) {
		// does nothing
	}

	@Override
	public void renderLineClear(GameEngine engine, int playerID) {
		// does nothing
	}

	@Override
	public void renderARE(GameEngine engine, int playerID) {
		// does nothing
	}

	@Override
	public void renderEndingStart(GameEngine engine, int playerID) {
		// does nothing
	}

	@Override
	public void renderCustom(GameEngine engine, int playerID) {
		// does nothing
	}

	@Override
	public void renderInput(GameEngine engine, int playerID) {
		// does nothing
	}
}

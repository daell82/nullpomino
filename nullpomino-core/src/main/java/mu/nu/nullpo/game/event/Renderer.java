/**
 *
 */
package mu.nu.nullpo.game.event;

import mu.nu.nullpo.game.GameEngine;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.util.Colors;

/**
 * A {@link Renderer} is responsible for drawing game-specific contents onto the
 * screen, including game fields, pieces menus and other contents
 *
 * @author daell
 */
public interface Renderer<T> {

	void setGraphics(T graphics);

	/**
	 * Called on block break. May be used for animations like line clear or gem
	 * removals
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 */
	void blockBreak(GameEngine engine, int playerID, int x, int y, Block block);

	/**
	 * Draw String inside the field.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param color    Font color
	 * @param scale    Font size (0.5f, 1.0f, 2.0f)
	 */
	void drawMenuFont(GameEngine engine, int playerID, int x, int y, String str, int color, float scale);

	/**
	 * [You don't have to override this] Draw String inside the field. (Font color
	 * is {@link Colors#FONT_WHITE})
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 */
	default void drawMenuFont(GameEngine engine, int playerID, int x, int y, String str) {
		drawMenuFont(engine, playerID, x, y, str, Colors.FONT_WHITE, 1.0f);
	}

	/**
	 * [You don't have to override this] Draw String inside the field. (Font color
	 * is {@link Colors#FONT_WHITE})
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param scale    Font size (0.5f, 1.0f, 2.0f)
	 */
	default void drawMenuFont(GameEngine engine, int playerID, int x, int y, String str, float scale) {
		drawMenuFont(engine, playerID, x, y, str, Colors.FONT_WHITE, scale);
	}

	/**
	 * [You don't have to override this] Draw String inside the field.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param color    Font color
	 */
	default void drawMenuFont(GameEngine engine, int playerID, int x, int y, String str, int color) {
		drawMenuFont(engine, playerID, x, y, str, color, 1.0f);
	}

	/**
	 * [You don't have to override this] Draw String inside the field. If flag is
	 * false, it will use white font color. If flag is true, it will use red
	 * instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 */
	default void drawMenuFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag) {
		drawMenuFont(engine, playerID, x, y, str, flag, 1.0f);
	}

	/**
	 * [You don't have to override this] Draw String inside the field. If flag is
	 * false, it will use {@link Colors#FONT_WHITE} as font color. If flag is true,
	 * it will use red instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 * @param scale    Font size
	 */
	private void drawMenuFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag, float scale) {
		if (!flag) {
			drawMenuFont(engine, playerID, x, y, str, Colors.FONT_WHITE, scale);
		} else {
			int fontcolor = playerID == 1 ? Colors.FONT_BLUE : Colors.FONT_RED;
			drawMenuFont(engine, playerID, x, y, str, fontcolor, scale);
		}
	}

	/**
	 * Draw String inside the field by using a TTF font.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param color    Font color
	 */
	void drawTTFMenuFont(GameEngine engine, int playerID, int x, int y, String str, int color);

	/**
	 * [You don't have to override this] Draw String inside the field by using a TTF
	 * font. (Font color is white)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 */
	default void drawTTFMenuFont(GameEngine engine, int playerID, int x, int y, String str) {
		drawTTFMenuFont(engine, playerID, x, y, str, Colors.FONT_WHITE);
	}

	/**
	 * [You don't have to override this] Draw String inside the field by using a TTF
	 * font. If flag is false, it will use white font color. If flag is true, it
	 * will use red instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 */
	default void drawTTFMenuFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag) {
		if (!flag) {
			drawTTFMenuFont(engine, playerID, x, y, str, Colors.FONT_WHITE);
		} else {
			int fontcolor = playerID == 1 ? Colors.FONT_BLUE : Colors.FONT_RED;
			drawTTFMenuFont(engine, playerID, x, y, str, fontcolor);
		}
	}

	/**
	 * Draw String to score display area.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param color    Font color
	 * @param scale    Font size (0.5f, 1.0f, 2.0f)
	 */
	void drawScoreFont(GameEngine engine, int playerID, int x, int y, String str, int color, float scale);

	/**
	 * [You don't have to override this] Draw String to score display area. (Font
	 * color is white)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 */
	default void drawScoreFont(GameEngine engine, int playerID, int x, int y, String str) {
		drawScoreFont(engine, playerID, x, y, str, Colors.FONT_WHITE, 1.0f);
	}

	/**
	 * [You don't have to override this] Draw String to score display area. (Font
	 * color is white)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param scale    Font size (0.5f, 1.0f, 2.0f)
	 */
	default void drawScoreFont(GameEngine engine, int playerID, int x, int y, String str, float scale) {
		drawScoreFont(engine, playerID, x, y, str, Colors.FONT_WHITE, scale);
	}

	/**
	 * [You don't have to override this] Draw String to score display area.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param color    Font color
	 */
	default void drawScoreFont(GameEngine engine, int playerID, int x, int y, String str, int color) {
		drawScoreFont(engine, playerID, x, y, str, color, 1.0f);
	}

	/**
	 * [You don't have to override this] Draw String to score display area. If flag
	 * is false, it will use white font color. If flag is true, it will use red
	 * instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 */
	default void drawScoreFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag) {
		drawScoreFont(engine, playerID, x, y, str, flag, 1.0f);
	}

	/**
	 * [You don't have to override this] Draw String to score display area. If flag
	 * is false, it will use white font color. If flag is true, it will use red
	 * instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 * @param scale    Font size
	 */
	default void drawScoreFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag, float scale) {
		if (!flag) {
			drawScoreFont(engine, playerID, x, y, str, Colors.FONT_WHITE, scale);
		} else {
			int fontcolor = playerID == 1 ? Colors.FONT_BLUE : Colors.FONT_RED;
			drawScoreFont(engine, playerID, x, y, str, fontcolor, scale);
		}
	}

	/**
	 * Draw String to score display area by using a TTF font.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param color    Font color
	 */
	void drawTTFScoreFont(GameEngine engine, int playerID, int x, int y, String str, int color);

	/**
	 * [You don't have to override this] Draw String to score display area by using
	 * a TTF font. (Font color is white)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 */
	default void drawTTFScoreFont(GameEngine engine, int playerID, int x, int y, String str) {
		drawTTFScoreFont(engine, playerID, x, y, str, Colors.FONT_WHITE);
	}

	/**
	 * [You don't have to override this] Draw String to score display area by using
	 * a TTF font. If flag is false, it will use white font color. If flag is true,
	 * it will use red instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 */
	default void drawTTFScoreFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag) {
		if (!flag) {
			drawTTFScoreFont(engine, playerID, x, y, str, Colors.FONT_WHITE);
		} else {
			int fontcolor = playerID == 1 ? Colors.FONT_BLUE : Colors.FONT_RED;
			drawTTFScoreFont(engine, playerID, x, y, str, fontcolor);
		}
	}

	/**
	 * Draw String to any location.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param color    Font color
	 * @param scale    Font size (0.5f, 1.0f, 2.0f)
	 */
	void drawDirectFont(GameEngine engine, int playerID, int x, int y, String str, int color, float scale);

	/**
	 * [You don't have to override this] Draw String to any location. (Font color if
	 * white)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 */
	default void drawDirectFont(GameEngine engine, int playerID, int x, int y, String str) {
		drawDirectFont(engine, playerID, x, y, str, Colors.FONT_WHITE, 1.0f);
	}

	/**
	 * [You don't have to override this] Draw String to any location. (Font color if
	 * white)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param scale    Font size (0.5f, 1.0f, 2.0f)
	 */
	default void drawDirectFont(GameEngine engine, int playerID, int x, int y, String str, float scale) {
		drawDirectFont(engine, playerID, x, y, str, Colors.FONT_WHITE, scale);
	}

	/**
	 * [You don't have to override this] Draw String to any location.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param color    Font color
	 */
	default void drawDirectFont(GameEngine engine, int playerID, int x, int y, String str, int color) {
		drawDirectFont(engine, playerID, x, y, str, color, 1.0f);
	}

	/**
	 * [You don't have to override this] Draw String to any location. If flag is
	 * false, it will use colorF as font color. If flag is true, it will use colorT
	 * instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 * @param colorF   Font color when flag is false
	 * @param colorT   Font color when flag is true
	 */
	default void drawDirectFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag, int colorF,
			int colorT) {
		if (!flag) {
			drawDirectFont(engine, playerID, x, y, str, colorF, 1.0f);
		} else {
			drawDirectFont(engine, playerID, x, y, str, colorT, 1.0f);
		}
	}

	/**
	 * [You don't have to override this] Draw String to any location. If flag is
	 * false, it will use white font color. If flag is true, it will use red
	 * instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 */
	default void drawDirectFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag) {
		drawDirectFont(engine, playerID, x, y, str, flag, 1.0f);
	}

	/**
	 * [You don't have to override this] Draw String to any location. If flag is
	 * false, it will use white font color. If flag is true, it will use red
	 * instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 */
	default void drawDirectFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag, float scale) {
		if (!flag) {
			drawDirectFont(engine, playerID, x, y, str, Colors.FONT_WHITE, scale);
		} else {
			int fontcolor = playerID == 1 ? Colors.FONT_BLUE : Colors.FONT_RED;
			drawDirectFont(engine, playerID, x, y, str, fontcolor, scale);
		}
	}

	/**
	 * Draw String to any location by using a TTF font.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param color    Font color
	 */
	void drawTTFDirectFont(GameEngine engine, int playerID, int x, int y, String str, int color);

	/**
	 * [You don't have to override this] Draw String to any location by using a TTF
	 * font. (Font color is white)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 */
	default void drawTTFDirectFont(GameEngine engine, int playerID, int x, int y, String str) {
		drawTTFDirectFont(engine, playerID, x, y, str, Colors.FONT_WHITE);
	}

	/**
	 * [You don't have to override this] Draw String to any location by using a TTF
	 * font. If flag is false, it will use white font color. If flag is true, it
	 * will use red instead.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param str      String to draw
	 * @param flag     Any boolean variable
	 */
	default void drawTTFDirectFont(GameEngine engine, int playerID, int x, int y, String str, boolean flag) {
		if (!flag) {
			drawTTFDirectFont(engine, playerID, x, y, str, Colors.FONT_WHITE);
		} else {
			int fontcolor = playerID == 1 ? Colors.FONT_BLUE : Colors.FONT_RED;
			drawTTFDirectFont(engine, playerID, x, y, str, fontcolor);
		}
	}

	/**
	 * Draw speed meter.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param s        Speed
	 */
	void drawSpeedMeter(GameEngine engine, int playerID, int x, int y, int s);

	/**
	 * Get width of block image.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return Width of block image
	 */
	int getBlockGraphicsWidth(GameEngine engine, int playerID);

	/**
	 * Get height of block image.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return Height of block image
	 */
	int getBlockGraphicsHeight(GameEngine engine, int playerID);

	/**
	 * Get X position of field
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return X position of field
	 */
	int getFieldDisplayPositionX(GameEngine engine, int playerID);

	/**
	 * Get Y position of field
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @return Y position of field
	 */
	int getFieldDisplayPositionY(GameEngine engine, int playerID);

	/**
	 * Get maximum value of the meter.
	 *
	 * @param engine GameEngine
	 * @return Maximum value of the meter
	 */
	int getMeterMax(GameEngine engine);

	/**
	 * Get type of piece preview
	 *
	 * @return 0=Above 1=Side Small 2=Side Big
	 */
	int getNextDisplayType();

	/**
	 * Get key name by button ID
	 *
	 * @param engine GameEngine
	 * @param btnID  Button ID
	 * @return Key name
	 */
	String getKeyNameByButtonID(GameEngine engine, int btnID);

	/**
	 * Check if the skin is sticky type
	 *
	 * @param skin Skin ID
	 * @return true if the skin is sticky type
	 */
	boolean isStickySkin(int skin);

	/**
	 * [You don't have to override this] Check if the current skin is sticky type
	 *
	 * @param engine GameEngine
	 * @return true if the current skin is sticky type
	 */
	default boolean isStickySkin(GameEngine engine) {
		return isStickySkin(engine.getSkin());
	}

	/**
	 * Is TTF font available?
	 *
	 * @return true if you can use TTF font routines.
	 */
	boolean isTTFSupport();

	/**
	 * Processing that takes place at the end of each frame
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void onLast(GameEngine engine, int playerID);

	/**
	 * Play sound effects
	 *
	 * @param name Name of SFX
	 */
	void playSE(String name);

	/**
	 * It will be called at the start of each frame. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderFirst(GameEngine engine, int playerID);

	/**
	 * It will be called at the end of each frame. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderLast(GameEngine engine, int playerID);

	/**
	 * It will be called at the settings screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderSetting(GameEngine engine, int playerID);

	/**
	 * It will be called during the "Ready->Go" screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderReady(GameEngine engine, int playerID);

	/**
	 * It will be called during the piece movement. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderMove(GameEngine engine, int playerID);

	/**
	 * It will be called during the "Lock flash". (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderLockFlash(GameEngine engine, int playerID);

	/**
	 * It will be called during the line clear. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderLineClear(GameEngine engine, int playerID);

	/**
	 * It will be called during the ARE. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderARE(GameEngine engine, int playerID);

	/**
	 * It will be called during the "Ending start" screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderEndingStart(GameEngine engine, int playerID);

	/**
	 * It will be called during the "Custom" screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderCustom(GameEngine engine, int playerID);

	/**
	 * It will be called during the "EXCELLENT!" screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderExcellent(GameEngine engine, int playerID);

	/**
	 * It will be called during the Game Over screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderGameOver(GameEngine engine, int playerID);

	/**
	 * It will be called during the end-of-game stats screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderResult(GameEngine engine, int playerID);

	/**
	 * It will be called during the field editor screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderFieldEdit(GameEngine engine, int playerID);

	/**
	 * It will be called if the player's input is being displayed. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	void renderInput(GameEngine engine, int playerID);
}

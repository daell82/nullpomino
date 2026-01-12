/**
 *
 */
package mu.nu.nullpo.gui.swing;

import java.awt.Color;

import mu.nu.nullpo.util.Colors;

/**
 *
 */
public class SwingColors {

	public static Color getMeterColorAsColor(int meterColor) {
		return switch (meterColor) {
		case Colors.METER_COLOR_PINK -> new Color(255, 0, 255);
		case Colors.METER_COLOR_PURPLE -> new Color(128, 0, 255);
		case Colors.METER_COLOR_DARKBLUE -> new Color(0, 0, 128);
		case Colors.METER_COLOR_BLUE -> Color.blue;
		case Colors.METER_COLOR_CYAN -> Color.cyan;
		case Colors.METER_COLOR_DARKGREEN -> new Color(0, 128, 0);
		case Colors.METER_COLOR_GREEN -> Color.green;
		case Colors.METER_COLOR_YELLOW -> Color.yellow;
		case Colors.METER_COLOR_ORANGE -> Color.orange;
		case Colors.METER_COLOR_RED -> Color.red;
		default -> Color.white;
		};
	}

	/**
	 * Specified font ColorAWTUseColorObtained as
	 *
	 * @param fontColor font Color
	 * @return font ColorColor
	 */
	public static Color getFontColorAsColor(int fontColor) {
		return switch (fontColor) {
		case Colors.FONT_BLUE -> new Color(0, 0, 255);
		case Colors.FONT_RED -> new Color(255, 0, 0);
		case Colors.FONT_PINK -> new Color(255, 128, 128);
		case Colors.FONT_GREEN -> new Color(0, 255, 0);
		case Colors.FONT_YELLOW -> new Color(255, 255, 0);
		case Colors.FONT_CYAN -> new Color(0, 255, 255);
		case Colors.FONT_ORANGE -> new Color(255, 128, 0);
		case Colors.FONT_PURPLE -> new Color(255, 0, 255);
		case Colors.FONT_DARKBLUE -> new Color(0, 0, 128);
		default -> new Color(255, 255, 255);
		};
	}

	/**
	 * Block colorIDDepending onAWTUseColorObjects created or received
	 *
	 * @param colorID Block colorID
	 * @return AWTUseColorObject
	 */
	public static Color getColorByID(int colorID) {
		return switch (colorID) {
		case Colors.BLOCK_COLOR_GRAY -> new Color(64, 64, 64);
		case Colors.BLOCK_COLOR_RED -> new Color(128, 0, 0);
		case Colors.BLOCK_COLOR_ORANGE -> new Color(128, 64, 0);
		case Colors.BLOCK_COLOR_YELLOW -> new Color(128, 128, 0);
		case Colors.BLOCK_COLOR_GREEN -> new Color(0, 128, 0);
		case Colors.BLOCK_COLOR_CYAN -> new Color(0, 128, 128);
		case Colors.BLOCK_COLOR_BLUE -> new Color(0, 0, 128);
		case Colors.BLOCK_COLOR_PURPLE -> new Color(128, 0, 128);
		default -> new Color(0, 0, 0);
		};
	}

	public static Color getColorByIDBright(int colorID) {
		return switch (colorID) {
		case Colors.BLOCK_COLOR_GRAY -> new Color(128, 128, 128);
		case Colors.BLOCK_COLOR_RED -> new Color(255, 0, 0);
		case Colors.BLOCK_COLOR_ORANGE -> new Color(255, 128, 0);
		case Colors.BLOCK_COLOR_YELLOW -> new Color(255, 255, 0);
		case Colors.BLOCK_COLOR_GREEN -> new Color(0, 255, 0);
		case Colors.BLOCK_COLOR_CYAN -> new Color(0, 255, 255);
		case Colors.BLOCK_COLOR_BLUE -> new Color(0, 0, 255);
		case Colors.BLOCK_COLOR_PURPLE -> new Color(255, 0, 255);
		default -> new Color(0, 0, 0);
		};
	}


}

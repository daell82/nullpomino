/**
 *
 */
package mu.nu.nullpo.gui.swing;

import java.awt.Color;

import lombok.experimental.UtilityClass;
import mu.nu.nullpo.util.Colors;

/**
 *
 */
@UtilityClass
public class SwingColors {

	public static Color getMeterColor(int meterColor) {
		return switch (meterColor) {
		case Colors.METER_COLOR_PINK -> Color.MAGENTA;
		case Colors.METER_COLOR_PURPLE -> new Color(128, 0, 255);
		case Colors.METER_COLOR_DARKBLUE -> new Color(0, 0, 128);
		case Colors.METER_COLOR_BLUE -> Color.BLUE;
		case Colors.METER_COLOR_CYAN -> Color.CYAN.darker();
		case Colors.METER_COLOR_DARKGREEN -> new Color(0, 128, 0);
		case Colors.METER_COLOR_GREEN -> Color.GREEN;
		case Colors.METER_COLOR_YELLOW -> Color.YELLOW;
		case Colors.METER_COLOR_ORANGE -> Color.ORANGE;
		case Colors.METER_COLOR_RED -> Color.RED;
		default -> Color.WHITE;
		};
	}

	/**
	 * Specified font ColorAWTUseColorObtained as
	 *
	 * @param fontColor font Color
	 * @return font ColorColor
	 */
	public static Color getFontColor(int fontColor) {
		return switch (fontColor) {
		case Colors.FONT_BLUE -> Color.BLUE;
		case Colors.FONT_RED -> Color.RED;
		case Colors.FONT_PINK -> new Color(255, 128, 128);
		case Colors.FONT_GREEN -> Color.GREEN;
		case Colors.FONT_YELLOW -> Color.YELLOW;
		case Colors.FONT_CYAN -> Color.MAGENTA;
		case Colors.FONT_ORANGE -> new Color(255, 128, 0);
		case Colors.FONT_PURPLE -> Color.MAGENTA;
		case Colors.FONT_DARKBLUE -> new Color(0, 0, 128);
		default -> Color.WHITE;
		};
	}

	/**
	 * Get the {@link Color} for a block if simple blocks are used
	 *
	 * @param color id of the color
	 * @return Color instance
	 */
	public static Color getSimpleBlockColor(int color) {
		return switch (color) {
		case Colors.BLOCK_COLOR_GRAY -> Color.LIGHT_GRAY;
		case Colors.BLOCK_COLOR_RED -> Color.RED;
		case Colors.BLOCK_COLOR_ORANGE -> Color.ORANGE;
		case Colors.BLOCK_COLOR_YELLOW -> Color.YELLOW;
		case Colors.BLOCK_COLOR_GREEN -> Color.GREEN;
		case Colors.BLOCK_COLOR_CYAN -> Color.CYAN;
		case Colors.BLOCK_COLOR_BLUE -> Color.BLUE;
		case Colors.BLOCK_COLOR_PURPLE -> Color.MAGENTA;
		default -> Color.WHITE;
		};
	}


	/**
	 * Block colorIDDepending onAWTUseColorObjects created or received
	 *
	 * @param colorID Block colorID
	 * @return AWTUseColorObject
	 */
	public static Color getBlockColor(int colorID) {
		return switch (colorID) {
		case Colors.BLOCK_COLOR_GRAY -> Color.DARK_GRAY;
		case Colors.BLOCK_COLOR_RED -> new Color(128, 0, 0);
		case Colors.BLOCK_COLOR_ORANGE -> new Color(128, 64, 0);
		case Colors.BLOCK_COLOR_YELLOW -> new Color(128, 128, 0);
		case Colors.BLOCK_COLOR_GREEN -> new Color(0, 128, 0);
		case Colors.BLOCK_COLOR_CYAN -> new Color(0, 128, 128);
		case Colors.BLOCK_COLOR_BLUE -> new Color(0, 0, 128);
		case Colors.BLOCK_COLOR_PURPLE -> new Color(128, 0, 128);
		default -> Color.BLACK;
		};
	}

	public static Color getBlockColorBright(int colorID) {
		return switch (colorID) {
		case Colors.BLOCK_COLOR_GRAY -> Color.GRAY;
		case Colors.BLOCK_COLOR_RED -> Color.RED;
		case Colors.BLOCK_COLOR_ORANGE -> new Color(255, 128, 0);
		case Colors.BLOCK_COLOR_YELLOW -> Color.YELLOW;
		case Colors.BLOCK_COLOR_GREEN -> Color.GREEN;
		case Colors.BLOCK_COLOR_CYAN -> Color.CYAN;
		case Colors.BLOCK_COLOR_BLUE -> Color.BLUE;
		case Colors.BLOCK_COLOR_PURPLE -> Color.MAGENTA;
		default -> Color.BLACK;
		};
	}


}

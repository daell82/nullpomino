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
package mu.nu.nullpo.gui.slick;

import org.newdawn.slick.Color;

import lombok.experimental.UtilityClass;
import mu.nu.nullpo.util.Colors;

/**
 * Normal display class string
 */
@UtilityClass
public class NormalFontSlick {

	/**
	 * Specified font ColorSlickUseColorObtained as
	 *
	 * @param fontColor font Color
	 * @return font ColorColor
	 */
	private static Color getFontColorAsColor(int fontColor) {
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
	 * Draws the text using the TTF font and the specified color
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param text  String
	 * @param color Letter color
	 */
	public static void printTTFFont(int x, int y, String text, int color) {
		if (ResourceHolderSlick.ttfFont == null) {
			return;
		}
		ResourceHolderSlick.ttfFont.drawString(x, y, text, getFontColorAsColor(color));
	}

	/**
	 * Draws the text using white color and the TTF font
	 *
	 * @param x    X-coordinate
	 * @param y    Y-coordinate
	 * @param text String
	 */
	public static void printTTFFont(int x, int y, String text) {
		printTTFFont(x, y, text, Colors.FONT_WHITE);
	}

	/**
	 * Draws the string
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param text   String
	 * @param color Letter color
	 * @param scale     Enlargement factor
	 */
	public static void printFont(int x, int y, String text, int color, float scale) {
		int dx = x;
		int dy = y;

		for (int i = 0; i < text.length(); i++) {
			char stringChar = text.charAt(i);

			if (stringChar == '\n') {
				// New line (\n)
				if (scale == 1.0f) {
					dy += 16;
				} else {
					dy += 8;
				}
				dx = x;
			} else // Character output
			if (scale == 0.5f) {
				int sx = (stringChar - 32) % 32 * 8;
				int sy = (stringChar - 32) / 32 * 8 + color * 24;
				ResourceHolderSlick.imgFontSmall.draw(dx, dy, dx + 8f, dy + 8f, sx, sy, sx + 8f, sy + 8f);
				dx = dx + 8;
			} else {
				int sx = (stringChar - 32) % 32 * 16;
				int sy = (stringChar - 32) / 32 * 16 + color * 48;
				ResourceHolderSlick.imgFont.draw(dx, dy, dx + 16 * scale, dy + 16 * scale, sx, sy, sx + 16f, sy + 16f);
				dx = (int) (dx + 16 * scale);
			}
		}
	}

	/**
	 * Draws the string using default scale (1.0)
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param text  String
	 * @param color Letter color
	 */
	public static void printFont(int x, int y, String text, int color) {
		printFont(x, y, text, color, 1.0f);
	}

	/**
	 * Draws the text using white color
	 *
	 * @param x    X-coordinate
	 * @param y    Y-coordinate
	 * @param text String
	 */
	public static void printFont(int x, int y, String text) {
		printFont(x, y, text, Colors.FONT_WHITE);
	}

	/**
	 * Draws the string in red if flag is {@code true}, white otherwise.
	 *
	 * @param x    X-coordinate
	 * @param y    Y-coordinate
	 * @param text String
	 * @param flag Conditional expression (red font on true, white else)
	 */
	public static void printFont(int x, int y, String text, boolean flag) {
		printFont(x, y, text, flag ? Colors.FONT_RED : Colors.FONT_WHITE);
	}

	/**
	 * Draws the string in the specified color. The method uses 16x16 grid units,
	 * meaning coordinates are multiplied
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param text  String
	 * @param color Letter color
	 */
	public static void printFontGrid(int x, int y, String text, int color) {
		printFont(x * 16, y * 16, text, color);
	}

	/**
	 * Draws the string in white color. The method uses 16x16 grid units, meaning
	 * coordinates are multiplied
	 *
	 * @param x    X-coordinate
	 * @param y    Y-coordinate
	 * @param text String
	 */
	public static void printFontGrid(int x, int y, String text) {
		printFont(x * 16, y * 16, text, Colors.FONT_WHITE);
	}

	/**
	 * Draws the string in red if flag is {@code true}, white otherwise. THe method
	 * uses 16x16 grid units, meaning coordinates are multiplied
	 *
	 * @param x    X-coordinate
	 * @param y    Y-coordinate
	 * @param text String
	 * @param flag Conditional expression (red font on true, white else)
	 */
	public static void printFontGrid(int x, int y, String text, boolean flag) {
		printFont(x * 16, y * 16, text, flag ? Colors.FONT_RED : Colors.FONT_WHITE);
	}
}

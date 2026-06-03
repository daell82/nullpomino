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
package mu.nu.nullpo.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

/**
 * Generic static utils
 */
@Log4j
@UtilityClass
public class GeneralUtil {

	/**
	 * Converts play time into a String
	 *
	 * @param t Play time
	 * @return String for play time
	 */
	public static String getTime(int t) {
		if (t < 0) {
			return "--:--.--";
		}

		return String.format("%02d:%02d.%02d", t / 3600, t / 60 % 60, t % 60 * 5 / 3);
	}

	/**
	 * Returns ON if b is true, OFF if b is false
	 *
	 * @param b Boolean variable to be checked
	 * @return ON if b is true, OFF if b is false
	 */
	public static String getONorOFF(boolean b) {
		return b ? "ON" : "OFF";
	}

	/**
	 * The method returns the value of {@link #getONorOFF(boolean)}.
	 *
	 * @param b Boolean variable to be checked
	 * @return "ON" if b {@code true}, "OFF" otheriwse
	 * @see #getONorOFF(boolean)
	 */
	public static String getOorX(boolean b) {
		// return b ? "c" : "e"
		return getONorOFF(b);
	}

	/**
	 * Fetches the filename for a replay
	 *
	 * @return Replay's filename
	 */
	public static String getReplayFilename() {
		Calendar c = Calendar.getInstance();
		DateFormat dfm = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		return dfm.format(c.getTime()) + ".rep";
	}

	/**
	 * Get date and time from a Calendar
	 *
	 * @param c Calendar
	 * @return Date and Time String
	 */
	public static String getCalendarString(Calendar c) {
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dfm.format(c.getTime());
	}

	/**
	 * Get date and time from a Calendar with specific TimeZone
	 *
	 * @param c Calendar
	 * @param z TimeZone
	 * @return Date and Time String
	 */
	public static String getCalendarString(Calendar c, TimeZone z) {
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dfm.setTimeZone(z);
		return dfm.format(c.getTime());
	}

	/**
	 * Get date from a Calendar
	 *
	 * @param c Calendar
	 * @return Date String
	 */
	public static String getCalendarStringDate(Calendar c) {
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
		return dfm.format(c.getTime());
	}

	/**
	 * Get date from a Calendar with specific TimeZone
	 *
	 * @param c Calendar
	 * @param z TimeZone
	 * @return Date String
	 */
	public static String getCalendarStringDate(Calendar c, TimeZone z) {
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
		dfm.setTimeZone(z);
		return dfm.format(c.getTime());
	}

	/**
	 * Get time from a Calendar
	 *
	 * @param c Calendar
	 * @return Time String
	 */
	public static String getCalendarStringTime(Calendar c) {
		DateFormat dfm = new SimpleDateFormat("HH:mm:ss");
		return dfm.format(c.getTime());
	}

	/**
	 * Get time from a Calendar with specific TimeZone
	 *
	 * @param c Calendar
	 * @param z TimeZone
	 * @return Time String
	 */
	public static String getCalendarStringTime(Calendar c, TimeZone z) {
		DateFormat dfm = new SimpleDateFormat("HH:mm:ss");
		dfm.setTimeZone(z);
		return dfm.format(c.getTime());
	}

	/**
	 * Export a Calendar to a String for saving/sending. TimeZone is always GMT.
	 * Time is based on current time.
	 *
	 * @return Calendar String (Each field is separated with a hyphen '-')
	 */
	public static String exportCalendarString() {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		return exportCalendarString(c);
	}

	/**
	 * Export a Calendar to a String for saving/sending. TimeZone is always GMT.
	 *
	 * @param c Calendar
	 * @return Calendar String (Each field is separated with a hyphen '-')
	 */
	public static String exportCalendarString(Calendar c) {
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dfm.format(c.getTime());
	}

	/**
	 * Create a Calendar by using a String that came from exportCalendarString.
	 * TimeZone is always GMT.
	 *
	 * @param s String (Each field is separated with a hyphen '-')
	 * @return Calendar (null if fails)
	 */
	public static Calendar importCalendarString(String s) {
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		dfm.setTimeZone(TimeZone.getTimeZone("GMT"));

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

		try {
			Date date = dfm.parse(s);
			c.setTime(date);
		} catch (Exception _) {
			return null;
		}

		return c;
	}

	/**
	 * Get the number of piece types can appear
	 *
	 * @param pieceEnable Piece enable flags
	 * @return Number of piece types can appear (In the normal Tetromino games, it
	 *         returns 7)
	 */
	public static int getNumberOfPiecesCanAppear(boolean[] pieceEnable) {
		if (pieceEnable == null) {
			return Piece.PIECE_COUNT;
		}
		int count = 0;
		for (boolean piece : pieceEnable) {
			if (piece) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Returns true if enabled piece types are S,Z,O only.
	 *
	 * @param pieceEnable Piece enable flags
	 * @return <code>true</code> if enabled piece types are S,Z,O only.
	 */
	public static boolean isPieceSZOOnly(boolean[] pieceEnable) {
		if (pieceEnable == null) {
			return false;
		}
		for (int i = 0; i < pieceEnable.length; i++) {
			if (pieceEnable[i] && i != Piece.PIECE_S && i != Piece.PIECE_Z && i != Piece.PIECE_O) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Create piece ID array from a String
	 *
	 * @param strSrc String
	 * @return Piece ID array
	 */
	public static int[] createNextPieceArrayFromNumberString(String strSrc) {
		int len = strSrc.length();
		int[] nextArray = new int[len];
		for (int i = 0; i < len; i++) {
			int pieceID = strSrc.charAt(i) - 48; // subtract non-ascii
			if (pieceID < 0 || pieceID >= Piece.PIECE_COUNT) {
				pieceID = Piece.PIECE_I;
			}
			nextArray[i] = pieceID;
		}
		return nextArray;
	}

	/**
	 * Load rule file
	 *
	 * @param filename Filename
	 * @return RuleOptions
	 */
	public static RuleOptions loadRule(String filename) {
		CustomProperties prop = new CustomProperties();

		try (var in = new FileInputStream(filename)) {
			prop.load(in);
		} catch (Exception e) {
			log.warn("Failed to load rule from " + filename, e);
		}
		return RuleOptions.of(prop, 0);
	}

	private static <T> T loadClass(String name) {
		try {
			Class<?> clazz = Class.forName(name);
			@SuppressWarnings("unchecked")
			T instance = (T) clazz.getConstructor().newInstance();
			return instance;
		} catch (Exception e) {
			log.warn("Failed to load class from " + name, e);
			return null;
		}
	}

	/**
	 * Load Randomizer
	 *
	 * @param filename Classpath of the randomizer
	 * @return Randomizer (null if something fails)
	 */
	public static Randomizer loadRandomizer(String filename) {
		return loadClass(filename);
	}

	/**
	 * Load Wallkick
	 *
	 * @param className of the wallkick
	 * @return Wallkick (null if something fails)
	 */
	public static Wallkick loadWallkick(String className) {
		return loadClass(className);
	}

	/**
	 * Loads an AI Bot for the game
	 *
	 * @param playerID   to load the AI for
	 * @param properties of the AI agent
	 * @return an AI agent if defined in the properties or {@code null} if none
	 */
	public static DummyAI loadAI(int playerID, CustomProperties properties) {
		DummyAI aiAgent = null;
		String aiClass = properties.getProperty(playerID + ".ai");
		if (aiClass != null && !aiClass.isBlank()) {
			aiAgent = GeneralUtil.loadClass(aiClass);
			aiAgent.setMoveDelay(properties.getProperty(playerID + ".aiMoveDelay", 0));
			aiAgent.setThinkDelay(properties.getProperty(playerID + ".aiThinkDelay", 0));
			aiAgent.setUseThread(properties.getProperty(playerID + ".aiUseThread", true));
			aiAgent.setShowHint(properties.getProperty(playerID + ".aiShowHint", false));
			aiAgent.setPrethink(properties.getProperty(playerID + ".aiPrethink", false));
			aiAgent.setShowState(properties.getProperty(playerID + ".aiShowState", false));
		}
		return aiAgent;
	}

	/**
	 * Combine array of strings
	 *
	 * @param strings    Array of strings
	 * @param separator  Separator used for combine
	 * @param startIndex First element which will be combined
	 * @return Combined string
	 */
	public static String stringCombine(String[] strings, String separator, int startIndex) {
		return Arrays.stream(strings, startIndex, strings.length).collect(Collectors.joining(separator));
	}

	/**
	 * Save properties to "config/setting/mode.cfg"
	 *
	 * @param modeConfig Properties you want to save
	 */
	public static void saveModeConfig(CustomProperties modeConfig) {
		try {
			modeConfig.save("config/setting/mode.cfg", "NullpoMino Mode Config");
		} catch (IOException e) {
			log.error("Failed to save mode config", e);
		}
	}
}

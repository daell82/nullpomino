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

import java.util.ArrayList;
import java.util.List;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.event.Renderer;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.menu.AbstractMenuItem;
import mu.nu.nullpo.game.types.GameStyle;
import mu.nu.nullpo.util.Colors;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import mu.nu.nullpo.util.Sounds;

/**
 * Dummy implementation of game mode. Used as a base of most game modes.
 */
public abstract class AbstractMode implements GameMode {

	/** events which can occur after line clears */
	protected enum LineClearEvent {
		/** no line clear */
		NONE,
		/** single line clear */
		SINGLE,
		/** double line clear */
		DOUBLE,
		/** triple line clear */
		TRIPLE,
		/** tetris line clear */
		FOUR,
		/** T-Spin zero (with wallkick) line clear */
		TSPIN_ZERO_MINI,
		/** T-Spin zero line clear */
		TSPIN_ZERO,
		/** T-Spin single (with wallkick) line clear */
		TSPIN_SINGLE_MINI,
		/** T-Spin single line clear */
		TSPIN_SINGLE,
		/** T-Spin double (with wallkick) line clear */
		TSPIN_DOUBLE_MINI,
		/** T-Spin double line clear */
		TSPIN_DOUBLE,
		/** T-Spin triple line clear */
		TSPIN_TRIPLE,
		/** T-Spin EZ line clear */
		TSPIN_EZ;
	}

	protected LineClearEvent lastevent;

	/** Number of entries in rankings */
	protected static final int RANKING_MAX = 10;

	/** Total score */
	protected enum Statistic {
		SCORE, LINES, TIME, LEVEL, LEVEL_MANIA, PIECE, MAXCOMBO, SPL, SPM, SPS, LPM, LPS, PPM, PPS, MAXCHAIN,
		LEVEL_ADD_DISP
	}

	/** GameManager that owns this mode */
	protected GameManager owner;

	/** Drawing and event handling EventReceiver */
	protected Renderer<?> renderer;

	/** Current state of menu for drawMenu */
	protected int statcMenu;
	protected int menuColor;
	protected int menuY;

	protected List<AbstractMenuItem<?>> menu;

	/** Name of mode in properties file */
	protected String propName;

	/** Position of cursor in menu */
	protected int menuCursor;

	/** Number of frames spent in menu */
	protected int menuTime;

	protected AbstractMode() {
		statcMenu = 0;
		menuCursor = 0;
		menuTime = 0;
		menuColor = Colors.FONT_WHITE;
		menuY = 0;
		menu = new ArrayList<>();
		propName = "dummy";
	}

	protected void loadSetting(CustomProperties prop) {
		for (AbstractMenuItem<?> item : menu) {
			item.load(-1, prop, propName);
		}
	}

	protected void saveSetting(CustomProperties prop) {
		for (AbstractMenuItem<?> item : menu) {
			item.save(-1, prop, propName);
		}
	}

	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
	}

	@Override
	public boolean lineClearEnd(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
	}

	@Override
	public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
	}

	@Override
	public void blockBreak(GameEngine engine, int playerID, int x, int y, Block blk) {
	}

	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
	}

	@Override
	public void fieldEditExit(GameEngine engine, int playerID) {
	}

	@Override
	public String getName() {
		return "DUMMY";
	}

	@Override
	public int getPlayers() {
		return 1;
	}

	@Override
	public GameStyle getGameStyle() {
		return GameStyle.TETROMINO;
	}

	@Override
	public void loadReplay(GameEngine engine, int playerID, CustomProperties prop) {
	}

	@Override
	public void modeInit(GameManager manager) {
	}

	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public boolean onCustom(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public boolean onEndingStart(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public boolean onExcellent(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public void onFirst(GameEngine engine, int playerID) {
	}

	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public void onLast(GameEngine engine, int playerID) {
	}

	@Override
	public boolean onLineClear(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public boolean onLockFlash(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public boolean onFieldEdit(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		renderer = engine.owner.renderer;
	}

	@Override
	public void renderARE(GameEngine engine, int playerID) {
	}

	@Override
	public void renderCustom(GameEngine engine, int playerID) {
	}

	@Override
	public void renderEndingStart(GameEngine engine, int playerID) {
	}

	@Override
	public void renderExcellent(GameEngine engine, int playerID) {
	}

	@Override
	public void renderFirst(GameEngine engine, int playerID) {
	}

	@Override
	public void renderGameOver(GameEngine engine, int playerID) {
	}

	@Override
	public void renderLast(GameEngine engine, int playerID) {
	}

	@Override
	public void renderLineClear(GameEngine engine, int playerID) {
	}

	@Override
	public void renderLockFlash(GameEngine engine, int playerID) {
	}

	@Override
	public void renderMove(GameEngine engine, int playerID) {
	}

	@Override
	public void renderReady(GameEngine engine, int playerID) {
	}

	@Override
	public void renderResult(GameEngine engine, int playerID) {
	}

	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		// TODO: Custom page breaks
		AbstractMenuItem<?> menuItem;
		int pageNum = menuCursor / 10;
		int pageStart = pageNum * 10;
		int endPage = Math.min(menu.size(), pageStart + 10);
		for (int i = pageStart; i < endPage; i++) {
			menuItem = menu.get(i);
			renderer.drawMenuFont(engine, playerID, 0, i << 1, menuItem.displayName, menuItem.color);
			if (menuCursor == i && !engine.owner.replayMode) {
				renderer.drawMenuFont(engine, playerID, 0, (i << 1) + 1, "b" + menuItem.getValueString(), true);
			} else {
				renderer.drawMenuFont(engine, playerID, 1, (i << 1) + 1, menuItem.getValueString());
			}
		}
	}

	@Override
	public void renderFieldEdit(GameEngine engine, int playerID) {
	}

	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
	}

	@Override
	public void startGame(GameEngine engine, int playerID) {
	}

	@Override
	public boolean isNetplayMode() {
		return false;
	}

	@Override
	public boolean isVSMode() {
		return false;
	}

	@Override
	public void netplayInit(Object obj) {
	}

	@Override
	public void netplayUnload(Object obj) {
	}

	@Override
	public void netplayOnRetryKey(GameEngine engine, int playerID) {
	}

	/**
	 * Update menu cursor
	 *
	 * @param engine    GameEngine
	 * @param maxCursor Max value of cursor position
	 * @return -1 if Left key is pressed, 1 if Right key is pressed, 0 otherwise
	 */
	protected int updateCursor(GameEngine engine, int maxCursor) {
		return updateCursor(engine, maxCursor, 0);
	}

	/**
	 * Update menu cursor
	 *
	 * @param engine    GameEngine
	 * @param maxCursor Max value of cursor position
	 * @param playerID  Player ID (unused)
	 * @return -1 if Left key is pressed, 1 if Right key is pressed, 0 otherwise
	 */
	protected int updateCursor(GameEngine engine, int maxCursor, int playerID) {
		// Up
		if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
			menuCursor--;
			if (menuCursor < 0) {
				menuCursor = maxCursor;
			}
			engine.playSE(Sounds.CURSOR);
		}
		// Down
		if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
			menuCursor++;
			if (menuCursor > maxCursor) {
				menuCursor = 0;
			}
			engine.playSE(Sounds.CURSOR);
		}

		// Configuration changes
		if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_LEFT)) {
			return -1;
		}
		if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_RIGHT)) {
			return 1;
		}
		return 0;
	}

	protected void updateMenu(GameEngine engine) {
		// Configuration changes
		int change = updateCursor(engine, menu.size() - 1);

		if (change != 0) {
			engine.playSE(Sounds.CHANGE);
			int fast = 0;
			if (engine.ctrl.isPush(Controller.BUTTON_E)) {
				fast++;
			}
			if (engine.ctrl.isPush(Controller.BUTTON_F)) {
				fast += 2;
			}
			menu.get(menuCursor).change(change, fast);
		}
	}

	protected void initMenu(int y, int color, int statc) {
		menuY = y;
		menuColor = color;
		statcMenu = statc;
	}

	protected void initMenu(int color, int statc) {
		menuY = 0;
		statcMenu = statc;
		menuColor = color;
	}

	protected void drawMenu(GameEngine engine, int playerID, String... strings) {
		for (int i = 0; i < strings.length; i++) {
			if ((i & 1) == 0) {
				renderer.drawMenuFont(engine, playerID, 0, menuY, strings[i], menuColor);
			} else if (menuCursor == statcMenu && !engine.owner.replayMode) {
				renderer.drawMenuFont(engine, playerID, 0, menuY, "b" + strings[i], true);
				statcMenu++;
			} else {
				renderer.drawMenuFont(engine, playerID, 1, menuY, strings[i]);
				statcMenu++;
			}
			menuY++;
		}
	}

	protected void drawMenu(GameEngine engine, int playerID, int y, int color, int statc, String... str) {
		menuY = y;
		menuColor = color;
		statcMenu = statc;
		drawMenu(engine, playerID, str);
	}

	protected void drawMenuCompact(GameEngine engine, int playerID, String... strings) {
		for (int i = 0; i < strings.length - 1; i += 2) {
			renderer.drawMenuFont(engine, playerID, 1, menuY, strings[i] + ":", menuColor);
			if (menuCursor == statcMenu && !engine.owner.replayMode) {
				renderer.drawMenuFont(engine, playerID, 0, menuY, "b", true);
				renderer.drawMenuFont(engine, playerID, strings[i].length() + 2, menuY, strings[i + 1], true);
			} else {
				renderer.drawMenuFont(engine, playerID, strings[i].length() + 2, menuY, strings[i + 1]);
			}
			statcMenu++;
			menuY++;
		}
	}

	protected void drawMenuCompact(GameEngine engine, int playerID, int y, int color, int statc, String... str) {
		menuY = y;
		menuColor = color;
		statcMenu = statc;
		drawMenuCompact(engine, playerID, str);
	}

	protected void drawResult(GameEngine engine, int playerID, int y, int color, String... str) {
		drawResultScale(engine, playerID, y, color, 1.0f, str);
	}

	protected void drawResultScale(GameEngine engine, int playerID, int y, int color, float scale, String... str) {
		for (int i = 0; i < str.length; i++) {
			renderer.drawMenuFont(engine, playerID, 0, y + i, str[i], (i & 1) == 0 ? color : Colors.FONT_WHITE, scale);
		}
	}

	protected void drawResultRank(GameEngine engine, int playerID, int y, int color, int rank) {
		drawResultRankScale(engine, playerID, y, color, 1.0f, rank);
	}

	protected void drawResultRankScale(GameEngine engine, int playerID, int y, int color,
			float scale, int rank) {
		if (rank != -1) {
			renderer.drawMenuFont(engine, playerID, 0, y, "RANK", color, scale);
			renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", rank + 1), scale);
		}
	}

	protected void drawResultNetRank(GameEngine engine, int playerID, int y, int color,
			int rank) {
		drawResultNetRankScale(engine, playerID, y, color, 1.0f, rank);
	}

	protected void drawResultNetRankScale(GameEngine engine, int playerID, int y, int color,
			float scale, int rank) {
		if (rank != -1) {
			renderer.drawMenuFont(engine, playerID, 0, y, "NET-RANK", color, scale);
			renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", rank + 1), scale);
		}
	}

	protected void drawResultNetRankDaily(GameEngine engine, int playerID, int y, int color,
			int rank) {
		drawResultNetRankDailyScale(engine, playerID, y, color, 1.0f, rank);
	}

	protected void drawResultNetRankDailyScale(GameEngine engine, int playerID, int y, int color,
			float scale, int rank) {
		if (rank != -1) {
			renderer.drawMenuFont(engine, playerID, 0, y, "DAILY-RANK", color, scale);
			renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", rank + 1), scale);
		}
	}

	protected void drawResultStats(GameEngine engine, int playerID, int y, int color,
			Statistic... stats) {
		drawResultStatsScale(engine, playerID, y, color, 1.0f, stats);
	}

	protected void drawResultStatsScale(GameEngine engine, int playerID, int y, int color,
			float scale, Statistic... statistics) {
		var stats = engine.statistics;
		for (Statistic stat : statistics) {
			switch (stat) {
			case SCORE -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "SCORE", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", stats.score), scale);
			}
			case LINES -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "LINES", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", stats.lines), scale);
			}
			case TIME -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "TIME", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1,
						String.format("%10s", GeneralUtil.getTime(stats.time)), scale);
			}
			case LEVEL -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "LEVEL", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", stats.level + 1), scale);
			}
			case LEVEL_MANIA -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "LEVEL", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", stats.level), scale);
			}
			case PIECE -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "PIECE", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", stats.totalPieceLocked), scale);
			}
			case MAXCOMBO -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "MAX COMBO", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", stats.maxCombo - 1), scale);
			}
			case SPL -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "SCORE/LINE", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10g", stats.spl), scale);
			}
			case SPM -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "SCORE/MIN", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10g", stats.spm), scale);
			}
			case SPS -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "SCORE/SEC", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10g", stats.sps), scale);
			}
			case LPM -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "LINE/MIN", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10g", stats.lpm), scale);
			}
			case LPS -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "LINE/SEC", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10g", stats.lps), scale);
			}
			case PPM -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "PIECE/MIN", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10g", stats.ppm), scale);
			}
			case PPS -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "PIECE/SEC", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10g", stats.pps), scale);
			}
			case MAXCHAIN -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "MAX CHAIN", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1, String.format("%10d", stats.maxChain), scale);
			}
			case LEVEL_ADD_DISP -> {
				renderer.drawMenuFont(engine, playerID, 0, y, "LEVEL", color, scale);
				renderer.drawMenuFont(engine, playerID, 0, y + 1,
						String.format("%10d", stats.level + stats.levelDispAdd), scale);
			}
			}
			y += 2;
		}
	}

	/**
	 * Default method to render controller input display
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderInput(GameEngine engine, int playerID) {
		int y = 24;
		if (isVSMode() && !isNetplayMode()) {
			int color = Colors.FONT_BLUE;
			if (playerID == 0) {
				color = Colors.FONT_RED;
				y--;
			}
			renderer.drawScoreFont(engine, 0, -9, y, playerID + 1 + "P INPUT:", color);
		} else {
			renderer.drawScoreFont(engine, 0, -6, y, "INPUT:", Colors.FONT_BLUE);
		}
		Controller ctrl = engine.ctrl;
		if (ctrl.isPress(Controller.BUTTON_LEFT)) {
			renderer.drawScoreFont(engine, 0, 0, y, "<");
		}
		if (ctrl.isPress(Controller.BUTTON_DOWN)) {
			renderer.drawScoreFont(engine, 0, 1, y, "n");
		}
		if (ctrl.isPress(Controller.BUTTON_UP)) {
			renderer.drawScoreFont(engine, 0, 2, y, "k");
		}
		if (ctrl.isPress(Controller.BUTTON_RIGHT)) {
			renderer.drawScoreFont(engine, 0, 3, y, ">");
		}
		if (ctrl.isPress(Controller.BUTTON_A)) {
			renderer.drawScoreFont(engine, 0, 4, y, "A");
		}
		if (ctrl.isPress(Controller.BUTTON_B)) {
			renderer.drawScoreFont(engine, 0, 5, y, "B");
		}
		if (ctrl.isPress(Controller.BUTTON_C)) {
			renderer.drawScoreFont(engine, 0, 6, y, "C");
		}
		if (ctrl.isPress(Controller.BUTTON_D)) {
			renderer.drawScoreFont(engine, 0, 7, y, "D");
		}
		if (ctrl.isPress(Controller.BUTTON_E)) {
			renderer.drawScoreFont(engine, 0, 8, y, "E");
		}
		if (ctrl.isPress(Controller.BUTTON_F)) {
			renderer.drawScoreFont(engine, 0, 9, y, "F");
		}
	}
}

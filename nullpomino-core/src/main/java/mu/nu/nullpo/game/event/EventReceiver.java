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
package mu.nu.nullpo.game.event;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.GameEngine;
import mu.nu.nullpo.game.GameManager;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.modes.AbstractMode;
import mu.nu.nullpo.game.modes.GameMode;
import mu.nu.nullpo.game.types.GameStyle;
import mu.nu.nullpo.util.CustomProperties;

/**
 * Drawing and event handling EventReceiver
 */
@Log4j
public class EventReceiver extends AbstractMode implements GameMode {

	/**
	 * It will be called before game screen appears.
	 *
	 * @param manager GameManager that owns this mode
	 */
	@Override
	public void modeInit(GameManager manager) {
	}

	/**
	 * It will be called at the end of initialization for each player.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called when Ready->Go is about to end, before first piece appears.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called at the start of each frame.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void onFirst(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called at the end of each frame.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called at the settings screen.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the "Ready->Go" screen.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the piece movement.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the "Lock flash".
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onLockFlash(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the line clear.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onLineClear(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the ARE.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the "Ending start" screen.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onEndingStart(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the "Custom" screen.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onCustom(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the "EXCELLENT!" screen.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onExcellent(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the Game Over screen.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the end-of-game stats screen.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called during the field editor screen.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean onFieldEdit(GameEngine engine, int playerID) {
		return false;
	}

	/**
	 * It will be called at the start of each frame. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderFirst(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called at the end of each frame. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called at the settings screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the "Ready->Go" screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderReady(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the piece movement. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderMove(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the "Lock flash". (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderLockFlash(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the line clear. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderLineClear(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the ARE. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderARE(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the "Ending start" screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderEndingStart(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the "Custom" screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderCustom(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the "EXCELLENT!" screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderExcellent(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the Game Over screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderGameOver(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the end-of-game stats screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called during the field editor screen. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderFieldEdit(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called if the player's input is being displayed. (For rendering)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderInput(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called when a block is cleared.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param blk      Block
	 */
	@Override
	public void blockBreak(GameEngine engine, int playerID, int x, int y, Block blk) {
	}

	/**
	 * It will be called when the game mode is going to calculate score. Please note
	 * it will be called even if no lines are cleared.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param lines    Number of lines cleared (0 if no line clear happened)
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
	}

	/**
	 * After Soft Drop is used
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param fall     Number of rows the piece falled by Soft Drop
	 */
	@Override
	public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
	}

	/**
	 * After Hard Drop is used
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param fall     Number of rows the piece falled by Hard Drop
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
	}

	/**
	 * It will be called when the player exit the field editor.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void fieldEditExit(GameEngine engine, int playerID) {
	}

	/**
	 * It will be called when the piece has locked. (after calcScore)
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 * @param lines    Number of lines to be cleared (can be 0)
	 */
	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
	}

	/**
	 * It will be called at the end of line-clear phase.
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public boolean lineClearEnd(GameEngine engine, int playerID) {
		return false;
	}

	@Override
	public String getName() {
		return "Empty Mode";
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
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		// TODO Auto-generated method stub
	}

	@Override
	public void loadReplay(GameEngine engine, int playerID, CustomProperties prop) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isNetplayMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVSMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void netplayInit(Object obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void netplayUnload(Object obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void netplayOnRetryKey(GameEngine engine, int playerID) {
		// TODO Auto-generated method stub

	}
}

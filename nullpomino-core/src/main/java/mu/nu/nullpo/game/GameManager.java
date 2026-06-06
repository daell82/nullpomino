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
package mu.nu.nullpo.game;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.BGImageStatus;
import mu.nu.nullpo.game.component.BGMusicStatus;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.event.Renderer;
import mu.nu.nullpo.game.modes.GameMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

/**
 * GameManager: The container of the game
 */
@Log4j
public class GameManager implements Serializable {

	/** @generated */
	private static final long serialVersionUID = 3555624847180077434L;

	/** Game Mode */
	public GameMode mode;

	/** Properties used by game mode */
	public CustomProperties modeConfig;

	/** Properties for replay file */
	public CustomProperties replayProp;

	/** true if replay mode */
	public boolean replayMode;

	/** true if replay rerecording */
	public boolean replayRerecord;

	/** true if display menus only (No game screens) */
	public boolean menuOnly;

	/** renders everything to the screen */
	public final Renderer<?> renderer;

	/** BGMStatus: Manages the status of background music */
	public BGMusicStatus bgmStatus;

	/** BackgroundStatus: Manages the status of background image */
	public BGImageStatus backgroundStatus;

	/** GameEngine: This is where the most action takes place */
	public GameEngine[] engines;

	/** true to show invisible blocks in replay */
	public boolean replayShowInvisible;

	/** Show input */
	public boolean showInput;

	public EventReceiver receiver = new EventReceiver();

	/**
	 * Normal constructor
	 *
	 * @param renderer the rendering engine for drawing the game
	 */
	public GameManager(Renderer<?> renderer) {
		this.renderer = renderer;
	}

	/**
	 * Initialize the game
	 */
	public void init() {
		log.debug("GameManager init()");

		modeConfig = CustomProperties.load("config/setting/mode.cfg");

		if (replayProp == null) {
			replayProp = new CustomProperties();
			replayMode = false;
		}

		replayRerecord = false;
		menuOnly = false;

		bgmStatus = new BGMusicStatus();
		backgroundStatus = new BGImageStatus();

		int players = 1;
		if (mode != null) {
			mode.setOwner(this);
			mode.modeInit(this);
			players = mode.getPlayers();
		}
		engines = new GameEngine[players];
		for (int i = 0; i < engines.length; i++) {
			engines[i] = new GameEngine(this, i);
		}
	}

	/**
	 * Reset the game
	 */
	public void reset() {
		log.debug("GameManager reset()");

		menuOnly = false;
		bgmStatus.reset();
		backgroundStatus.reset();
		if (!replayMode) {
			replayProp = new CustomProperties();
		}
		for (GameEngine engine : engines) {
			engine.init();
		}
	}

	/**
	 * Shutdown the game
	 */
	public void shutdown() {
		log.debug("GameManager shutdown()");

		try {
			for (int i = 0; i < engines.length; i++) {
				engines[i].shutdown();
				engines[i] = null;
			}
			engines = null;
			mode = null;
			modeConfig = null;
			replayProp = null;
			bgmStatus = null;
			backgroundStatus = null;
		} catch (Throwable e) {
			log.debug("Caught Throwable on shutdown", e);
		}
	}

	/**
	 * Get number of players
	 *
	 * @return Number of players
	 */
	public int getPlayers() {
		return engines != null ? engines.length : 0;
	}

	/**
	 * Check if quit flag is true in any GameEngine object
	 *
	 * @return true if the game should quit
	 */
	public boolean getQuitFlag() {
		if (engines == null) {
			return false;
		}
		for (GameEngine engine : engines) {
			if (engine != null && engine.quitflag) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if at least 1 game is active
	 *
	 * @return true if there is a active GameEngine
	 */
	public boolean isGameActive() {
		if (engines != null) {
			for (GameEngine engine : engines) {
				if (engine != null && engine.gameActive) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Get winner ID
	 *
	 * @return Player ID of last survivor. -1 in single player game. -2 in tied
	 *         game.
	 */
	public int getWinner() {
		if (engines.length < 2) {
			return -1;
		}

		for (int i = 0; i < engines.length; i++) {
			if (engines[i].stat != GameEngine.Status.GAMEOVER) {
				return i;
			}
		}

		return -2;
	}

	/**
	 * Update every GameEngine
	 */
	public void updateAll() {
		for (GameEngine engine : engines) {
			engine.update();
		}
		bgmStatus.fadeUpdate();
		backgroundStatus.fadeUpdate();
	}

	/**
	 * Dispatches all render events to EventReceiver
	 */
	public void renderAll() {
		for (GameEngine engine : engines) {
			engine.render();
		}
	}

	/**
	 * Replay save routine
	 */
	public void saveReplay() {
		replayProp = new CustomProperties();
		for (GameEngine engine : engines) {
			engine.saveReplay();
		}
		saveReplay(replayProp, "replay");
	}

	/**
	 * Called when saving replay (This is main body)
	 *
	 * @param owner      GameManager
	 * @param prop       CustomProperties where the replay is going to stored
	 * @param foldername Replay folder name
	 */
	public void saveReplay(CustomProperties prop, String foldername) {
		if (mode.isNetplayMode()) {
			return;
		}

		String filename = foldername + "/" + GeneralUtil.getReplayFilename();
		try {
			File repfolder = new File(foldername);
			if (!repfolder.exists()) {
				if (repfolder.mkdir()) {
					log.info("Created replay folder: " + foldername);
				} else {
					log.info("Couldn't create replay folder at " + foldername);
				}
			}
			prop.save(filename, "NullpoMino Replay");
			log.info("Saved replay file: " + filename);
		} catch (IOException e) {
			log.error("Couldn't save replay file to " + filename, e);
		}
	}
}

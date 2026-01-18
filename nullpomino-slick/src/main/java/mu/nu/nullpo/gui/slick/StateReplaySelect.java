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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.Statistics;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

/**
 * State selection screen replay
 */
@Log4j
public class StateReplaySelect extends DummyMenuScrollState {

	/** This state's ID */
	public static final int ID = 4;

	/** 1Displayed on the screenMaximumFilecount */
	public static final int PAGE_HEIGHT = 20;

	/** Mode name */
	protected List<String> modeNames;

	/** Rule name */
	protected List<String> ruleNames;

	/** ScoreInformation such as the */
	protected Statistics[] statsistics;

	public StateReplaySelect() {
		pageHeight = PAGE_HEIGHT;
		nullError = "REPLAY DIRECTORY NOT FOUND";
		emptyError = "NO REPLAY FILE";
	}

	/*
	 * Fetch this state's ID
	 */
	@Override
	public int getID() {
		return ID;
	}

	/*
	 * State initialization
	 */
	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
	}

	/*
	 * Called when entering this state
	 */
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		list = getReplayFileList();

		if (list != null) {
			maxCursor = list.size() - 1;
		}

		setReplayRuleAndModeList();
	}

	/**
	 * Gets the list of files replay
	 *
	 * @return Replay fileFilenameArray of. If there is no directorynull
	 */
	protected List<String> getReplayFileList() {
		// Get file list
		File dir = new File(NullpoMinoSlick.propGlobal.getProperty("custom.replay.directory", "replay"));

		FilenameFilter filter = (_, name) -> name.endsWith(".rep");

		List<String> list = Arrays.asList(dir.list(filter));
		if (!System.getProperty("os.name").startsWith("Windows")) {
			// Sort if not windows
			Collections.sort(list);
		}

		return list;
	}

	/**
	 * Set the details of replay
	 */
	protected void setReplayRuleAndModeList() {
		if (list == null) {
			return;
		}
		var length = list.size();
		var directory = NullpoMinoSlick.propGlobal.getProperty("custom.replay.directory", "replay");

		modeNames = new ArrayList<>(length);
		ruleNames = new ArrayList<>(length);
		statsistics = new Statistics[length];

		for (int i = 0; i < length; i++) {
			String replayFile = list.get(i);
			CustomProperties prop = new CustomProperties();
			try (var in = new FileInputStream(directory + "/" + replayFile)) {
				prop.load(in);
			} catch (IOException e) {
				log.error("Failed to load replay file (" + replayFile + ")", e);
			}

			modeNames.add(i, prop.getProperty("name.mode", ""));
			ruleNames.add(i, prop.getProperty("name.rule", ""));

			statsistics[i] = new Statistics();
			statsistics[i].readProperty(prop, 0);
		}
	}

	@Override
	protected void onRenderSuccess(GameContainer container, StateBasedGame game, Graphics graphics) {
		String title = "SELECT REPLAY FILE";
		title += " (" + (cursor + 1) + "/" + list.size() + ")";
		NormalFontSlick.printFontGrid(1, 1, title, NormalFontSlick.COLOR_ORANGE);

		NormalFontSlick.printFontGrid(1, 24, "MODE:" + modeNames.get(cursor) + " RULE:" + ruleNames.get(cursor),
				NormalFontSlick.COLOR_CYAN);
		NormalFontSlick.printFontGrid(1, 25,
				"SCORE:" + statsistics[cursor].score + " LINE:" + statsistics[cursor].lines,
				NormalFontSlick.COLOR_CYAN);
		NormalFontSlick.printFontGrid(1, 26, "LEVEL:" + (statsistics[cursor].level + statsistics[cursor].levelDispAdd)
				+ " TIME:" + GeneralUtil.getTime(statsistics[cursor].time), NormalFontSlick.COLOR_CYAN);
		NormalFontSlick.printFontGrid(1, 27,
				"GAME RATE:"
						+ (statsistics[cursor].gamerate == 0f ? "UNKNOWN" : 100 * statsistics[cursor].gamerate + "%"),
				NormalFontSlick.COLOR_CYAN);
	}

	@Override
	protected boolean onDecide(GameContainer container, StateBasedGame game, int delta) {
		ResourceHolderSlick.soundManager.play("decide");
		String dir = NullpoMinoSlick.propGlobal.getProperty("custom.replay.directory", "replay");
		CustomProperties prop = CustomProperties.load(dir + "/" + list.get(cursor));
		if(prop.isEmpty()) {
			return true;
		}
		NullpoMinoSlick.stateInGame.startReplayGame(prop);
		game.enterState(StateInGame.ID);
		return false;
	}

	@Override
	protected boolean onCancel(GameContainer container, StateBasedGame game, int delta) {
		game.enterState(StateTitle.ID);
		return false;
	}
}

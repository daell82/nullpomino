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
package mu.nu.nullpo.gui.slick.states;

import java.util.LinkedList;
import java.util.List;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.gui.slick.DummyMenuScrollState;
import mu.nu.nullpo.gui.slick.NormalFontSlick;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.ResourceHolderSlick;
import mu.nu.nullpo.util.Colors;

/**
 * Mode select screen
 */
@Log4j
public class StateSelectMode extends DummyMenuScrollState {

	/** This state's ID */
	public static final int ID = 3;

	/** Number of game modes in one page */
	public static final int PAGE_HEIGHT = 24;

	/** true if top-level folder */
	// XXX rework this
	static boolean isTopLevel;

	/** Current folder name */
	private String currentFolder;

	/**
	 * Constructor
	 */
	public StateSelectMode() {
		super();
		pageHeight = PAGE_HEIGHT;
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
	public void init(GameContainer container, StateBasedGame game) {
	}

	/**
	 * Prepare mode list
	 */
	protected void prepareModeList() {
		currentFolder = StateSelectModeFolder.currentFolder;

		// Get mode list
		if (isTopLevel) {
			list = new LinkedList<>(StateSelectModeFolder.topLevelModes);
			list.add("[MORE...]");
		} else {
			List<String> modes = StateSelectModeFolder.mapFolder.get(currentFolder);
			if (modes != null) {
				list = new LinkedList<>(modes);
			} else {
				list = NullpoMinoSlick.modeManager.getModeNames(false);
			}
		}
		maxCursor = list.size() - 1;

		// Set cursor postion
		String lastmode = null;
		if (isTopLevel) {
			lastmode = NullpoMinoSlick.propGlobal.getProperty("name.mode.toplevel", null);
		} else if (currentFolder != null && !currentFolder.isEmpty()) {
			lastmode = NullpoMinoSlick.propGlobal.getProperty("name.mode." + currentFolder, null);
		} else {
			lastmode = NullpoMinoSlick.propGlobal.getProperty("name.mode", null);
		}
		cursor = getIDbyName(lastmode);
		if (cursor < 0) {
			cursor = 0;
		}
		if (cursor > maxCursor) {
			cursor = maxCursor;
		}
	}

	/**
	 * Get mode ID (not including netplay modes)
	 *
	 * @param name Name of mode
	 * @return ID (-1 if not found)
	 */
	protected int getIDbyName(String name) {
		return list.indexOf(name);
	}

	/**
	 * Get game mode description
	 *
	 * @param str Mode name
	 * @return Description
	 */
	protected String getModeDesc(String str) {
		String str2 = str.replace(' ', '_');
		str2 = str2.replace('(', 'l');
		str2 = str2.replace(')', 'r');
		String result = NullpoMinoSlick.propModeDesc.getProperty(str2);
		if (result == null) {
			result = NullpoMinoSlick.propDefaultModeDesc.getProperty(str2, str2);
		}
		return result;
	}

	/*
	 * Enter
	 */
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		prepareModeList();
	}

	/*
	 * Render screen
	 */
	@Override
	public void onRenderSuccess(GameContainer container, StateBasedGame game, Graphics graphics) {
		if (!isTopLevel && !currentFolder.isEmpty()) {
			NormalFontSlick.printFontGrid(1, 1, currentFolder + " (" + (cursor + 1) + "/" + list.size() + ")",
					Colors.FONT_ORANGE);
		} else {
			NormalFontSlick.printFontGrid(1, 1, "MODE SELECT (" + (cursor + 1) + "/" + list.size() + ")",
					Colors.FONT_ORANGE);
		}

		NormalFontSlick.printTTFFont(16, 440, getModeDesc(list.get(cursor)));
	}

	/*
	 * Decide
	 */
	@Override
	protected boolean onDecide(GameContainer container, StateBasedGame game, int delta) {
		ResourceHolderSlick.soundManager.play("decide");
		String mode = list.get(cursor);
		if (isTopLevel && cursor == list.size() - 1) {
			// More...
			NullpoMinoSlick.propGlobal.setProperty("name.mode.toplevel", mode);
			game.enterState(StateSelectModeFolder.ID);
		} else {
			// Go to rule selector
			if (isTopLevel) {
				NullpoMinoSlick.propGlobal.setProperty("name.mode.toplevel", mode);
			}
			if (!currentFolder.isEmpty()) {
				NullpoMinoSlick.propGlobal.setProperty("name.mode." + currentFolder, mode);
			}
			NullpoMinoSlick.propGlobal.setProperty("name.mode", mode);
			NullpoMinoSlick.saveConfig();
			game.enterState(StateSelectRuleFromList.ID);
		}

		return false;
	}

	/*
	 * Cancel
	 */
	@Override
	protected boolean onCancel(GameContainer container, StateBasedGame game, int delta) {
		if (isTopLevel) {
			game.enterState(StateTitle.ID);
		} else {
			game.enterState(StateSelectModeFolder.ID);
		}
		return false;
	}
}

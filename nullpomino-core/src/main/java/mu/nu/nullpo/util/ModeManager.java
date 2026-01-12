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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.subsystem.mode.GameMode;

/**
 * Mode Management class
 */
@Log4j
public class ModeManager {

	/** Mode Dynamic array of */
	private List<GameMode> gameModes = new ArrayList<>();

	/**
	 * All that has been readMode nameGet the
	 *
	 * @return Mode nameAn array of
	 */
	protected List<String> getAllModeNames() {
		return gameModes.stream().map(GameMode::getName).toList();
	}

	/**
	 * Get the names of loaded game modes
	 *
	 * @param netplay whether normal mode or netplay modes
	 * @return list of {@link GameMode} names
	 */
	public List<String> getModeNames(boolean netplay) {
		return gameModes.stream().filter(m -> m.isNetplayMode() == netplay).map(GameMode::getName).toList();
	}

	/**
	 * Get a game mode by its name
	 *
	 * @param name of the game mode
	 * @return {@link GameMode} if found or {@code null}
	 */
	public GameMode getMode(String name) {
		return gameModes.stream().filter(m -> m.getName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * Game from the list that was written to a text fileMode Read
	 *
	 * @param bf I read a text fileBufferedReader
	 */
	public void loadGameModes(String filename) {
		try {
			List<String> lines = Files.readAllLines(new File(filename).toPath());
			for (String name : lines) {
				if (name.isEmpty() || name.startsWith("#")) {
					continue;
				}
				Class<?> modeClass = Class.forName(name);
				GameMode modeObject = (GameMode) modeClass.getConstructor().newInstance();
				gameModes.add(modeObject);
			}
		} catch (ReflectiveOperationException roe) {
			log.warn("failed to load mode", roe);
		} catch (IOException e) {
			log.warn("Failed to load game modes", e);
		}
	}
}

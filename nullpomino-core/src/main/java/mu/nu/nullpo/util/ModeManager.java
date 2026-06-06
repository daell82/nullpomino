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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.modes.GameMode;

/**
 * Mode Management class
 */
@Log4j
public class ModeManager {

	/** All loaded {@link GameMode game modes} */
	private List<GameMode> gameModes = new LinkedList<>();

	/**
	 * Get the names of all loaded game modes
	 *
	 * @return List with game modes names
	 */
	protected List<String> getAllModeNames() {
		return gameModes.stream().map(GameMode::getName).toList();
	}

	/**
	 * Get the names of loaded game modes
	 *
	 * @param netplay whether normal or netplay modes
	 * @return list of {@link GameMode} names
	 * @deprecated use {@link #getNetplayModeNames()} or {@link #getNormalModeNames()} instead
	 */
	@Deprecated(since = "8.0")
	public List<String> getModeNames(boolean netplay) {
		return netplay ? getNetplayModeNames() : getNormalModeNames();
	}

	/**
	 * Get the names of all normal (non-netplay) game modes
	 *
	 * @return list of normal game modes names
	 */
	public List<String> getNormalModeNames() {
		return gameModes.stream().filter(m -> !m.isNetplayMode()).map(GameMode::getName).toList();
	}

	/**
	 * Get the names of all netplay game modes
	 *
	 * @return list of netplay game modes names
	 */
	public List<String> getNetplayModeNames() {
		return gameModes.stream().filter(GameMode::isNetplayMode).map(GameMode::getName).toList();
	}

	/**
	 * Get a game mode by its name
	 *
	 * @param name of the game mode
	 * @return {@link GameMode} if found or {@code null}
	 */
	public Optional<GameMode> getMode(String name) {
		return gameModes.stream().filter(m -> m.getName().equals(name)).findFirst();
	}

	/**
	 * Game from the list that was written to a text fileMode Read
	 *
	 * @param bf I read a text fileBufferedReader
	 */
	public void loadGameModes(String filename) {
		try {
			for (String name : Files.readAllLines(new File(filename).toPath())) {
				if (name.isEmpty() || name.startsWith("#")) {
					continue;
				}
				Class<?> clazz = Class.forName(name);
				GameMode mode = (GameMode) clazz.getConstructor().newInstance();
				gameModes.add(mode);
			}
		} catch (ReflectiveOperationException roe) {
			log.warn("failed to load mode", roe);
		} catch (IOException e) {
			log.warn("failed to load game modes", e);
		}
	}
}

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

import java.util.HashMap;
import java.util.Map;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.openal.SoundStore;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.event.SoundManager;

/**
 * Sound effectsManager
 */
@Log4j
public class SlickSoundManager implements SoundManager {

	/** You can registerWAVE file OfMaximumcount */
	protected int maxClips;

	/** WAVE file data (Name-> dataBody) */
	protected Map<String, Sound> clips;

	/**
	 * Constructor
	 */
	public SlickSoundManager() {
		this(128);
	}

	/**
	 * Constructor
	 *
	 * @param maxClips You can registerWAVE file OfMaximumcount
	 */
	public SlickSoundManager(int maxClips) {
		this.maxClips = maxClips;
		clips = HashMap.newHashMap(maxClips);
	}

	/**
	 * Load WAVE file
	 *
	 * @param name     Registered name
	 * @param filename Filename (String)
	 * @return true if successful, false if failed
	 */
	@Override
	public void load(String name, String filename) {
		if (clips.size() >= maxClips) {
			log.error("No more wav files can be loaded (" + maxClips + ")");
			return;
		}
		try {
			clips.put(name, new Sound(filename));
		} catch (SlickException e) {
			log.error("Failed to load wav file", e);
		}
	}

	/**
	 * Playback
	 *
	 * @param name Registered name
	 */
	@Override
	public void play(String name) {
		Sound clip = clips.get(name);
		if (clip == null) {
			return;
		}
		clip.play();
	}

	@Override
	public void setVolume(float volume) {
		SoundStore.get().setSoundVolume(volume);
	}
}

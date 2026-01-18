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
package mu.nu.nullpo.game.component;

import java.io.Serializable;

/**
 * Class that manages the state of music playback
 */
public class BGMStatus implements Serializable {

	/** Serial version ID */
	private static final long serialVersionUID = -1003092972570497408L;

	/** Constant musiccount */
	public static final int BGM_NOTHING = -1;
	public static final int BGM_NORMAL1 = 0;
	public static final int BGM_NORMAL2 = 1;
	public static final int BGM_NORMAL3 = 2;
	public static final int BGM_NORMAL4 = 3;
	public static final int BGM_NORMAL5 = 4;
	public static final int BGM_NORMAL6 = 5;
	public static final int BGM_PUZZLE1 = 6;
	public static final int BGM_PUZZLE2 = 7;
	public static final int BGM_PUZZLE3 = 8;
	public static final int BGM_PUZZLE4 = 9;
	public static final int BGM_ENDING1 = 10;
	public static final int BGM_ENDING2 = 11;
	public static final int BGM_SPECIAL1 = 12;
	public static final int BGM_SPECIAL2 = 13;
	public static final int BGM_SPECIAL3 = 14;
	public static final int BGM_SPECIAL4 = 15;

	/** MusicalMaximumcount */
	public static final int BGM_COUNT = 16;

	/** Current BGM number */
	public int bgm = BGM_NOTHING;

	/** Volume (1f=100%, 0.5f=50%) */
	public float volume = 1f;

	/** BGM fadeoutSwitch */
	public boolean fadesw = false;

	/**
	 * Constructor
	 */
	public BGMStatus() {
		reset();
	}

	/**
	 * Reset to defaults
	 */
	public void reset() {
		bgm = BGM_NOTHING;
		volume = 1f;
		fadesw = false;
	}

	/**
	 * BGM fadeUpdate of state and volume
	 */
	public void fadeUpdate() {
		if (fadesw) {
			if (volume > 0f) {
				volume -= 0.005f;
			} else if (volume < 0f) {
				volume = 0f;
			}
		} else if (volume < 1f) {
			volume = 1f;
		}
	}
}

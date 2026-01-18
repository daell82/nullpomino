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

import lombok.experimental.UtilityClass;

/**
 * Class holding version information of the game
 */
@UtilityClass
public class Version {

	private final float MAJOR_VERSION = 8.0f;

	private final int MINOR_VERSION = 0;

	private final boolean DEV_BUILD = true;

	/**
	 * Get major version
	 *
	 * @return Major version
	 */
	public static float getMajorVersion() {
		return MAJOR_VERSION;
	}

	/**
	 * Get minor version
	 *
	 * @return Minor version
	 */
	public static int getMinorVersion() {
		return MINOR_VERSION;
	}

	/**
	 * Get minor version (For compatibility with old replays)
	 *
	 * @return Minor version
	 */
	public static float getMinorVersionOld() {
		return MINOR_VERSION;
	}

	/**
	 * Get version information as String
	 *
	 * @return Version information
	 */
	public static String getVersionString() {
		return MAJOR_VERSION + "." + MINOR_VERSION + (DEV_BUILD ? "D" : "");
	}

	/**
	 * Is this development build?
	 *
	 * @return true if dev build
	 */
	public static boolean isDevBuild() {
		return DEV_BUILD;
	}

	/**
	 * Get build type as string
	 *
	 * @return Build type as String
	 */
	public static String getBuildType() {
		return DEV_BUILD ? "Development" : "Release";
	}

	/**
	 * Get build type name
	 *
	 * @param type Build type (false:Release true:Development)
	 * @return Build type as String
	 */
	public static String getBuildType(boolean type) {
		return type ? "Development" : "Release";
	}

}

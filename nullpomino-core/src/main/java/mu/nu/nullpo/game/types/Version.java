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
package mu.nu.nullpo.game.types;

import lombok.NonNull;

/**
 * Record for providing version information about the program
 *
 * @author daell
 */
public record Version(int major, int minor, int micro, boolean isDevBuild, String version)
		implements Comparable<Version> {

	private static final Version CURRENT_VERSION = Version.of("8.0.0D");

	public static Version getCurrent() {
		return CURRENT_VERSION;
	}

	/**
	 * Parses the given String and extracts necessary version information.
	 *
	 * @param version of scheme 1[.2[.3]][dD]
	 * @return a new version instance
	 */
	public static Version of(@NonNull String version) {
		char last = version.charAt(version.length() - 1);
		boolean dev = last == 'd' || last == 'D';
		String[] parts = version.split("\\.");
		return switch (parts.length) {
		case 0 -> throw new IllegalArgumentException("Invalid version format: " + version);
		case 1 -> new Version(parseInt(parts[0]), 0, 0, dev, version);
		case 2 -> new Version(parseInt(parts[0]), parseInt(parts[1]), 0, dev, version);
		default -> new Version(parseInt(parts[0]), parseInt(parts[1]), parseInt(parts[2]), dev, version);
		};
	}

	/**
	 * Creates a new {@link Version} based on the given parameter. A call to this
	 * method builds up the version string and creates the new instance without
	 * parsing
	 *
	 * @param major version part (must be greater of equal to 0)
	 * @param minor version part (must be greater of equal to 0)
	 * @param micro version part (must be greater of equal to 0)
	 * @param dev   whether this is a development version (considered lower than
	 *              release version)
	 * @return a new version instance
	 * @throws IllegalArgumentException if any part of the version is negative
	 */
	public static Version of(int major, int minor, int micro, boolean dev) throws IllegalArgumentException {
		if (major < 0 || minor < 0 || micro < 0) {
			throw new IllegalArgumentException("numeric places of a version must be positive");
		}
		String version = major + "." + minor + "." + micro;
		if (dev) {
			version += "D";
		}
		return new Version(major, minor, micro, dev, version);
	}

	/**
	 * Creates a new {@link Version} based on the given parameter. A call to this
	 * method builds up the version string and creates the new instance without
	 * parsing. All parameters must not be negative.
	 *
	 * @param major version part
	 * @param minor version part
	 * @param micro version part
	 * @return a new version instance
	 * @throws IllegalArgumentException if any part of the version is negative
	 */
	public static Version of(int major, int minor, int micro) throws IllegalArgumentException {
		return of(major, minor, micro, false);
	}

	/**
	 * Creates a new {@link Version} based on the given parameter. A call to this
	 * method builds up the version string and creates the new instance without
	 * parsing. The micro-version is considered a 0. All parameters must not be
	 * negative.
	 *
	 * @param major version part
	 * @param minor version part
	 * @return a new version instance
	 * @throws IllegalArgumentException if any part of the version is negative
	 */
	public static Version of(int major, int minor) throws IllegalArgumentException {
		return of(major, minor, 0);
	}

	/**
	 * Parses a string value for numeric values. The methods reads numeric values
	 * from the given string until either other characters occur or the end of the
	 * string is reached. Non-numeric values are ignored:
	 *
	 * <pre>
	 * 12  -> 12
	 * 12a -> 12
	 * 1.a -> 1
	 * a1  -> 0
	 * ''  -> 0
	 * </pre>
	 *
	 * @param s string value to process
	 * @return numeric value obtained from the string
	 */
	private static int parseInt(String s) {
		int result = 0;
		if (s == null || s.isBlank()) {
			return result;
		}
		for (int i = 0; i < s.length(); i++) {
			char value = s.charAt(i);
			if (!Character.isDigit(value)) {
				break;
			}
			result *= 10;
			result += Character.digit(value, 10);
		}
		return result;
	}

	/**
	 * Check whether this version is older / lower than the combination of
	 * major.minor.micro i.e. a version of 1.2.3 is considered to be lower than
	 * 1.2.4 but not lower than 1.1.9
	 *
	 * @param major version (1st place)
	 * @param minor version (2nd place)
	 * @param micro version (3rd place)
	 * @return whether this version is lower
	 */
	public boolean isLower(int major, int minor, int micro) {
		if (major() > major) {
			return false;
		} else if (major() == major && minor() > minor) {
			return false;
		} else if (major() == major && minor() == minor && micro() > micro) {
			return false;
		}
		return true;
	}

	/**
	 * Check whether this version is newer / greater than the combination of
	 * major.minor.micro i.e. a version of 1.2.3 is considered to be greater than
	 * 1.2.1 but not greater than 1.2.9
	 *
	 * @param major version (1st place)
	 * @param minor version (2nd place)
	 * @param micro version (3rd place)
	 * @return whether this version is greater
	 */
	public boolean isGreater(int major, int minor, int micro) {
		if (major() < major) {
			return false;
		} else if (major() == major && minor() < minor) {
			return false;
		} else if (major() == major && minor() == minor && micro() < micro) {
			return false;
		}
		return true;
	}

	/**
	 * Checks whether this version is compatible to a combination of major.minor.
	 * The version is considered as compatible if the major and the minor parts are equal.
	 *
	 * @param major version part
	 * @param minor version part
	 * @return whether this version is compatible
	 */
	public boolean isCompatible(int major, int minor) {
		return major() == major && minor() == minor;
	}

	/**
	 * Get the major and minor part of this version as string
	 *
	 * @return major.minor
	 * @see #major()
	 * @see #minor()
	 */
	public String majorMinor() {
		return major() + "." + minor();
	}

	/**
	 * Get build type as string
	 *
	 * @return Build type as String
	 */
	public String getBuildType() {
		return isDevBuild() ? "Development" : "Release";
	}

	/**
	 * compares two versions by weighting the places: major * 100, minor * 10, patch
	 * as is. If the argument is {@code null}, the method returns 1.
	 */
	@Override
	public int compareTo(Version other) {
		if (other == null) {
			return 1;
		}
		if (other == this) {
			return 0;
		}
		if (major != other.major) {
			return 100 * (major - other.major);
		}
		if (minor != other.minor) {
			return 10 * (minor - other.minor);
		}
		if (micro != minor) {
			return micro - other.micro;
		}
		if (isDevBuild()) {
			return other.isDevBuild() ? 0 : -1;
		}
		return 1;
	}

	@Override
	public String toString() {
		return version;
	}
}
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
 * @author daell
 */
public record Version(int major, int minor, int micro, boolean dev, String version) implements Comparable<Version> {

	private static final Version BUILD_VERSION = Version.of("8.0.0D");

	public static Version getVersion() {
		return BUILD_VERSION;
	}

	/**
	 * Parses the given String and extracts necessary version information.
	 *
	 * @param version of scheme 1[.2[.3]]
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

	public boolean isNewer(Version version) {
		if(version == null) {
			return false;
		}
		return compareTo(version) > 0;
	}

	public boolean isOlder(Version version) {
		if(version == null) {
			return false;
		}
		return compareTo(version) < 0;
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
		if (dev()) {
			return other.dev() ? 0 : -1;
		}
		return 1;
	}

	@Override
	public String toString() {
		return version;
	}
}
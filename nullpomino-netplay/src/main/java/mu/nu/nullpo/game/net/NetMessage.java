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
package mu.nu.nullpo.game.net;

import java.util.Arrays;
import java.util.Calendar;

import mu.nu.nullpo.util.GeneralUtil;

/**
 * Record for providing information for client and server communication
 *
 * @author daell
 */
public record NetMessage(NetCmd command, String[] data) {

	/**
	 * Create a new message based on the string data provided. The method assumes
	 * the first entry as string representation of {@link NetCmd#command()}
	 *
	 * @param parts to process
	 * @return a {@link NetMessage} instance
	 * @throws IllegalArgumentException if parts is {@code null} or empty of if the
	 *                                  {@link NetCmd} in not valid
	 */
	public static NetMessage of(String... parts) {
		if (parts == null || parts.length < 1) {
			throw new IllegalArgumentException("parts of the message must not be null or empty: " + parts);
		}
		NetCmd cmd = NetCmd.of(parts[0]);
		if (cmd == null) {
			throw new IllegalArgumentException("unrecognized command: " + parts[0]);
		}
		return new NetMessage(cmd, Arrays.copyOfRange(parts, 1, parts.length));
	}

	public String text(int index) {
		ensureIndexInRange(index);
		return data[index];
	}

	public int asInt(int index) {
		ensureIndexInRange(index);
		return Integer.parseInt(data[index]);
	}

	public long asLong(int index) {
		ensureIndexInRange(index);
		return Long.parseLong(data[index]);
	}

	public float asFloat(int index) {
		ensureIndexInRange(index);
		return Float.parseFloat(data[index]);
	}

	public double asDouble(int index) {
		ensureIndexInRange(index);
		return Double.parseDouble(data[index]);
	}

	public boolean asBool(int index) {
		ensureIndexInRange(index);
		return Boolean.parseBoolean(data[index]);
	}

	public String urlDecoded(int index) {
		ensureIndexInRange(index);
		return NetUtil.urlDecode(data[index]);
	}

	public String decompressed(int index) {
		ensureIndexInRange(index);
		return NetUtil.decompressString(data[index]);
	}

	public Calendar asDate(int index) {
		ensureIndexInRange(index);
		return GeneralUtil.importCalendarString(data[index]);
	}

	public String asTime(int index) {
		int time = asInt(index);
		return GeneralUtil.getTime(time);
	}

	private String ensureIndexInRange(int index) {
		if (index < 0 || index >= data.length) {
			throw new IllegalArgumentException("index out of range: " + index);
		}
		return data[index];
	}

	public boolean isEmpty() {
		return data.length == 0;
	}

	/**
	 * @return the length of the {@link #data} compartment
	 */
	public int length() {
		return data.length;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		return switch (obj) {
		case NetMessage(var cmd, var data2) -> command == cmd && Arrays.equals(data, data2);
		case null -> false;
		default -> false;
		};
	}

	@Override
	public final int hashCode() {
		int prime = 37;
		return prime * command.ordinal() + prime * Arrays.hashCode(data);
	}

	@Override
	public final String toString() {
		return "NetMessage(command: " + command() + ", data: " + Arrays.toString(data()) + ")";
	}
}

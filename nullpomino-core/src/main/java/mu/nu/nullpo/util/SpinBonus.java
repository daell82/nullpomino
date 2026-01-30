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

/**
 * @author daell
 */
public class SpinBonus {

	/** Spin detection type */
	public static final int SPINTYPE_4POINT = 0;
	/** Spin detection type */
	public static final int SPINTYPE_IMMOBILE = 1;

	/** New Spin bonusUse coordinate dataA(X-coordinate) */
	public static final int[][][] HIGH_X = {
			{ { 1, 2, 2, 1 }, { 1, 3, 1, 3 }, { 1, 2, 2, 1 }, { 0, 2, 0, 2 } }, // I
			{ { 1, 0 }, { 2, 2 }, { 1, 2 }, { 0, 0 } }, // L
			{ {}, {}, {}, {} }, // O
			{ { 2, 0 }, { 2, 1 }, { 0, 2 }, { 0, 1 } }, // Z
			{ { 0, 2 }, { 2, 2 }, { 0, 2 }, { 0, 0 } }, // T
			{ { 1, 2 }, { 2, 2 }, { 1, 0 }, { 0, 0 } }, // J
			{ { 0, 2 }, { 1, 2 }, { 2, 0 }, { 1, 0 } }, // S
			{ {}, {}, {}, {} }, // I1
			{ {}, {}, {}, {} }, // I2
			{ {}, {}, {}, {} }, // I3
			{ {}, {}, {}, {} }, // L3
	};
	/** New Spin bonusUse coordinate dataA(Y-coordinate) */
	public static final int[][][] HIGH_Y = {
			{ { 0, 2, 0, 2 }, { 1, 2, 2, 1 }, { 1, 3, 1, 3 }, { 1, 2, 2, 1 } }, // I
			{ { 0, 0 }, { 1, 0 }, { 2, 2 }, { 1, 2 } }, // L
			{ {}, {}, {}, {} }, // O
			{ { 0, 1 }, { 2, 0 }, { 2, 1 }, { 0, 2 } }, // Z
			{ { 0, 0 }, { 0, 2 }, { 2, 2 }, { 0, 2 } }, // T
			{ { 0, 0 }, { 1, 2 }, { 2, 2 }, { 1, 0 } }, // J
			{ { 0, 1 }, { 2, 0 }, { 2, 1 }, { 0, 2 } }, // S
			{ {}, {}, {}, {} }, // I1
			{ {}, {}, {}, {} }, // I2
			{ {}, {}, {}, {} }, // I3
			{ {}, {}, {}, {} }, // L3
	};
	/** New Spin bonusUse coordinate dataB(X-coordinate) */
	public static final int[][][] LOW_X = {
			{ { -1, 4, -1, 4 }, { 2, 2, 2, 2 }, { -1, 4, -1, 4 }, { 1, 1, 1, 1 } }, // I
			{ { 2, 0 }, { 0, 0 }, { 0, 2 }, { 2, 2 } }, // L
			{ {}, {}, {}, {} }, // O
			{ { -1, 3 }, { 2, 1 }, { 3, -1 }, { 0, 1 } }, // Z
			{ { 0, 2 }, { 0, 0 }, { 0, 2 }, { 2, 2 } }, // T
			{ { 0, 2 }, { 0, 0 }, { 2, 0 }, { 2, 2 } }, // J
			{ { 3, -1 }, { 1, 2 }, { -1, 3 }, { 1, 0 } }, // S
			{ {}, {}, {}, {} }, // I1
			{ {}, {}, {}, {} }, // I2
			{ {}, {}, {}, {} }, // I3
			{ {}, {}, {}, {} }, // L3
	};

	/** New Spin bonusUse coordinate dataB(Y-coordinate) */
	public static final int[][][] LOW_Y = {
			{ { 1, 1, 1, 1 }, { -1, 4, -1, 4 }, { 2, 2, 2, 2 }, { -1, 4, -1, 4 } }, // I
			{ { 2, 2 }, { 2, 0 }, { 0, 0 }, { 0, 3 } }, // L
			{ {}, {}, {}, {} }, // O
			{ { 0, 1 }, { -1, 3 }, { 2, 1 }, { 3, -1 } }, // Z
			{ { 2, 2 }, { 0, 2 }, { 0, 0 }, { 0, 2 } }, // T
			{ { 2, 2 }, { 0, 2 }, { 0, 0 }, { 2, 0 } }, // J
			{ { 0, 1 }, { -1, 3 }, { 2, 1 }, { 3, -1 } }, // S
			{ {}, {}, {}, {} }, // I1
			{ {}, {}, {}, {} }, // I2
			{ {}, {}, {}, {} }, // I3
			{ {}, {}, {}, {} }, // L3
	};

}

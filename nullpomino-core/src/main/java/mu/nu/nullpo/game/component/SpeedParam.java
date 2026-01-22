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
 * BlockWait and emergence rate of fall of the piece timeSuch as data
 */
public class SpeedParam implements Serializable {

	/** Serial version ID */
	private static final long serialVersionUID = -955934100998757270L;

	/** Fall velocity */
	public int gravity = 4;

	/** Denominator of the rate of fall (gravity==denominatorIf1GBecome) */
	public int denominator = 256;

	/** Entry delay for the next block */
	public int are = 24;

	/** Line clearAfter waiting for the emergence of time */
	public int areLine = 24;

	/** Line clear time */
	public int lineDelay = 40;

	/** Fixation time */
	public int lockDelay = 30;

	/** Lateral motion time */
	public int das = 14;

	/**
	 * Constructor
	 */
	public SpeedParam() {

	}

	/**
	 * Copy constructor
	 *
	 * @param s Copy source
	 */
	public SpeedParam(SpeedParam s) {
		gravity = s.gravity;
		denominator = s.denominator;
		are = s.are;
		areLine = s.areLine;
		lineDelay = s.lineDelay;
		lockDelay = s.lockDelay;
		das = s.das;
	}
}

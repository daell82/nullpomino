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
package mu.nu.nullpo.game.subsystem.ai;

import lombok.Getter;
import lombok.Setter;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * DummyAI - Base class for AI players
 */
public class DummyAI implements AIPlayer {

	/** AI move delay */
	@Getter
	@Setter
	private int moveDelay;

	/** AI think delay (Only when using thread) */
	@Getter
	@Setter
	private int thinkDelay;

	/** Use thread for AI */
	@Getter
	@Setter
	private boolean useThread;

	/** Show Hint with AI */
	@Getter
	@Setter
	private boolean showHint;

	/** Prethink with AI */
	@Getter
	@Setter
	private boolean prethink;

	/** Show internal state of AI */
	@Getter
	@Setter
	private boolean showState;

	/** AI Hint piece (copy of current or hold) */
	@Getter
	@Setter
	private Piece hintPiece;

	/** Hold will be used */
	public boolean bestHold;

	/** Plan to putX-coordinate */
	public int bestX;

	/** Plan to putY-coordinate */
	public int bestY;

	/** Plan to putDirection */
	public int bestRt;

	/** Hold Force */
	public boolean forceHold;

	/** Current piece number */
	public int thinkCurrentPieceNo;

	/** Peace of thoughts is finished number */
	public int thinkLastPieceNo;

	/** Did the thinking thread finish successfully? */
	public boolean thinkComplete;

	public boolean isHintReady() {
		return thinkComplete || thinkCurrentPieceNo > 0 && thinkCurrentPieceNo <= thinkLastPieceNo;
	}

	@Override
	public String getName() {
		return "DummyAI";
	}

	@Override
	public void init(GameEngine engine, int playerID) {
	}

	@Override
	public void newPiece(GameEngine engine, int playerID) {
	}

	@Override
	public void onFirst(GameEngine engine, int playerID) {
	}

	@Override
	public void onLast(GameEngine engine, int playerID) {
	}

	@Override
	public void renderState(GameEngine engine, int playerID) {
	}

	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
	}

	@Override
	public void shutdown(GameEngine engine, int playerID) {
	}

	@Override
	public void renderHint(GameEngine engine, int playerID) {
	}
}

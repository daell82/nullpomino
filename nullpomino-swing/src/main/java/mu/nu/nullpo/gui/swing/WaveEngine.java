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
package mu.nu.nullpo.gui.swing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import lombok.extern.log4j.Log4j;

/**
 * Sound engine <a href=
 * "http://javagame.skr.jp/index.php?%A5%B5%A5%A6%A5%F3%A5%C9%A5%A8%A5%F3%A5%B8%A5%F3">Reprint
 * yuan</a>
 */
@Log4j
public class WaveEngine implements LineListener {

	/** You can registerWAVE file OfMaximumcount */
	private final int maxClips;

	/** WAVE file data (Name-> dataBody) */
	private final Map<String, Clip> clipMap;

	/** Volume */
	private double volume = 1.0;

	/**
	 * Constructor
	 */
	public WaveEngine() {
		this(128);
	}

	/**
	 * Constructor
	 *
	 * @param maxClips You can registerWAVE file OfMaximumcount
	 */
	public WaveEngine(int maxClips) {
		this.maxClips = maxClips;
		clipMap = HashMap.newHashMap(maxClips);
	}

	public void initSounds() {
		if (!clipMap.isEmpty()) {
			return;
		}
		String resourcesDir = NullpoMinoSwing.propConfig.getProperty("custom.skin.directory", "res");
		load("cursor", resourcesDir + "/se/cursor.wav");
		load("decide", resourcesDir + "/se/decide.wav");
		load("erase1", resourcesDir + "/se/erase1.wav");
		load("erase2", resourcesDir + "/se/erase2.wav");
		load("erase3", resourcesDir + "/se/erase3.wav");
		load("erase4", resourcesDir + "/se/erase4.wav");
		load("died", resourcesDir + "/se/died.wav");
		load("gameover", resourcesDir + "/se/gameover.wav");
		load("hold", resourcesDir + "/se/hold.wav");
		load("holdfail", resourcesDir + "/se/holdfail.wav");
		load("initialhold", resourcesDir + "/se/initialhold.wav");
		load("initialrotate", resourcesDir + "/se/initialrotate.wav");
		load("levelup", resourcesDir + "/se/levelup.wav");
		load("linefall", resourcesDir + "/se/linefall.wav");
		load("lock", resourcesDir + "/se/lock.wav");
		load("move", resourcesDir + "/se/move.wav");
		load("pause", resourcesDir + "/se/pause.wav");
		load("rotate", resourcesDir + "/se/rotate.wav");
		load("step", resourcesDir + "/se/step.wav");
		load("piece0", resourcesDir + "/se/piece0.wav");
		load("piece1", resourcesDir + "/se/piece1.wav");
		load("piece2", resourcesDir + "/se/piece2.wav");
		load("piece3", resourcesDir + "/se/piece3.wav");
		load("piece4", resourcesDir + "/se/piece4.wav");
		load("piece5", resourcesDir + "/se/piece5.wav");
		load("piece6", resourcesDir + "/se/piece6.wav");
		load("piece7", resourcesDir + "/se/piece7.wav");
		load("piece8", resourcesDir + "/se/piece8.wav");
		load("piece9", resourcesDir + "/se/piece9.wav");
		load("piece10", resourcesDir + "/se/piece10.wav");
		load("harddrop", resourcesDir + "/se/harddrop.wav");
		load("softdrop", resourcesDir + "/se/softdrop.wav");
		load("levelstop", resourcesDir + "/se/levelstop.wav");
		load("endingstart", resourcesDir + "/se/endingstart.wav");
		load("excellent", resourcesDir + "/se/excellent.wav");
		load("b2b_start", resourcesDir + "/se/b2b_start.wav");
		load("b2b_continue", resourcesDir + "/se/b2b_continue.wav");
		load("b2b_end", resourcesDir + "/se/b2b_end.wav");
		load("gradeup", resourcesDir + "/se/gradeup.wav");
		load("countdown", resourcesDir + "/se/countdown.wav");
		load("tspin0", resourcesDir + "/se/tspin0.wav");
		load("tspin1", resourcesDir + "/se/tspin1.wav");
		load("tspin2", resourcesDir + "/se/tspin2.wav");
		load("tspin3", resourcesDir + "/se/tspin3.wav");
		load("ready", resourcesDir + "/se/ready.wav");
		load("go", resourcesDir + "/se/go.wav");
		load("movefail", resourcesDir + "/se/movefail.wav");
		load("rotfail", resourcesDir + "/se/rotfail.wav");
		load("medal", resourcesDir + "/se/medal.wav");
		load("change", resourcesDir + "/se/change.wav");
		load("bravo", resourcesDir + "/se/bravo.wav");
		load("cool", resourcesDir + "/se/cool.wav");
		load("regret", resourcesDir + "/se/regret.wav");
		load("garbage", resourcesDir + "/se/garbage.wav");
		load("stageclear", resourcesDir + "/se/stageclear.wav");
		load("stagefail", resourcesDir + "/se/stagefail.wav");
		load("gem", resourcesDir + "/se/gem.wav");
		load("danger", resourcesDir + "/se/danger.wav");
		load("matchend", resourcesDir + "/se/matchend.wav");
		load("hurryup", resourcesDir + "/se/hurryup.wav");
		load("square_s", resourcesDir + "/se/square_s.wav");
		load("square_g", resourcesDir + "/se/square_g.wav");

		for (int i = 1; i < 21; i++) {
			load("combo" + i, resourcesDir + "/se/combo" + i + ".wav");
		}
		setVolume(NullpoMinoSwing.propConfig.getProperty("option.sevolume", 0.5));
	}

	/**
	 * WAVE file Read
	 *
	 * @param name     Registered name
	 * @param filename Filename
	 */
	private void load(String name, String filename) {
		load(name, new File(filename));
	}

	/**
	 * WAVE file Read
	 *
	 * @param name Registered name
	 * @param file to load
	 */
	private void load(String name, File file) {
		if (clipMap.size() >= maxClips) {
			log.warn(name + " : No more files can be loaded (Max: " + maxClips + ")");
			return;
		}
		try {
			// Open the audio stream
			AudioInputStream stream = AudioSystem.getAudioInputStream(file);

			// Obtains the audio format
			AudioFormat format = stream.getFormat();

			// ULAW/ALAW If the format is PCM Change the format
			if (format.getEncoding() == AudioFormat.Encoding.ULAW
					|| format.getEncoding() == AudioFormat.Encoding.ALAW) {
				AudioFormat newFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
						format.getSampleSizeInBits() * 2, format.getChannels(), format.getFrameSize() * 2,
						format.getFrameRate(), true);
				stream = AudioSystem.getAudioInputStream(newFormat, stream);
				format = newFormat;
			}

			// LinesGet information
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			// Create an empty clip
			Clip clip = (Clip) AudioSystem.getLine(info);
			// Clip event Monitoring
			clip.addLineListener(this);
			// Opened as a clip the audio stream
			clip.open(stream);
			// Submit a clip
			clipMap.put(name, clip);
			// Close the stream
			stream.close();
		} catch (LineUnavailableException e) {
			log.warn(name + " : Failed to open line", e);
		} catch (UnsupportedAudioFileException e) {
			log.warn(name + " : This is not a wave file", e);
		} catch (IOException e) {
			log.warn(name + " : Load failed", e);
		}
	}

	/**
	 * Current Get the volume setting
	 *
	 * @return Current Volume setting (1.0 The default)
	 */
	public double getVolume() {
		return volume;
	}

	/**
	 * Set the volume
	 *
	 * @param vol New configuration volume (1.0The default )
	 */
	public void setVolume(double vol) {
		volume = vol;
		for (Clip clip : clipMap.values()) {
			clip.flush();
			try {
				FloatControl ctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				ctrl.setValue((float) Math.log10(volume) * 20);
			} catch (Exception e) {
				// ignore
			}
		}
	}

	/**
	 * Playback
	 *
	 * @param name Registered name
	 */
	public void play(String name) {
		Clip clip = clipMap.get(name);
		if (clip != null) {
			stop(clip);
			// Playback
			clip.start();
		}
	}

	/**
	 * Stop
	 *
	 * @param name Registered name
	 */
	private void stop(Clip clip) {
		// Stop
		clip.stop();
		// Playback position back to the beginning
		clip.setFramePosition(0);
	}

	/*
	 * Lines stateChange
	 */
	@Override
	public void update(LineEvent event) {
		// If you stop playback or to the end
		if (event.getType() == LineEvent.Type.STOP) {
			stop((Clip) event.getSource());
		}
	}
}
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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.play.SoundManager;
import mu.nu.nullpo.util.Sounds;

/**
 * Class to the management of image and sound
 */
@Log4j
@Getter
public class ResourceHolderSwing {

	/** BackgroundOfcount */
	public static final int BACKGROUND_MAX = 20;

	/** Number of images for block spatter animation during line clears */
	public static final int BLOCK_BREAK_MAX = 8;

	/** Number of image splits for block spatter animation during line clears */
	public static final int BLOCK_BREAK_SEGMENTS = 2;

	/** Number of gem block clear effects */
	public static final int PERASE_MAX = 7;

	private static final ResourceHolderSwing INSTANCE = new ResourceHolderSwing();

	/** Block images */
	private final List<Image> normalBlockImages = new LinkedList<>();
	private final List<Image> smallBlockImages = new LinkedList<>();
	private final List<Image> bigBlockImages = new LinkedList<>();

	/** Block sticky flag */
	private final List<Boolean> blockStickyFlags = new LinkedList<>();

	/** Regular font */
	private Image imgFont;
	private Image imgFontSmall;

	/** Field frame */
	private Image imgFrame;

	/** Field background */
	private Image imgFieldbg;

	private Image imgFieldbg2;

	private Image imgFieldbg2Small;

	private Image imgFieldbg2Big;

	/** Block spatter animation during line clears */
	private Image[][] imgBreak;

	/** Effects for clearing gem blocks */
	private Image[] imgPErase;

	/** In playBackground */
	private Image[] imgPlayBG;

	/** Audio file management */
	private final SoundManager soundManager = new WaveEngine();

	private ResourceHolderSwing() {

	}

	/**
	 * @return the singleton instance
	 */
	public static ResourceHolderSwing getInstance() {
		return INSTANCE;
	}

	/**
	 * Loading images and sound files
	 *
	 * @param skinDir the resource directory for graphical skins
	 */
	public void load(String skinDir) {

		// Blocks
		int numBlocks = 0;
		File file = null;
		while (true) {
			file = new File(skinDir + "/graphics/blockskin/normal/n" + numBlocks + ".png");
			if (file.canRead()) {
				numBlocks++;
			} else {
				break;
			}
		}
		log.debug(numBlocks + " block skins found");

		for (int i = 0; i < numBlocks; i++) {
			Image imgNormal = loadImage(skinDir + "/graphics/blockskin/normal/n" + i + ".png");
			normalBlockImages.add(imgNormal);
			smallBlockImages.add(loadImage(skinDir + "/graphics/blockskin/small/s" + i + ".png"));
			bigBlockImages.add(loadImage(skinDir + "/graphics/blockskin/big/b" + i + ".png"));

			if (imgNormal.getWidth(null) >= 400 && imgNormal.getHeight(null) >= 304) {
				blockStickyFlags.add(Boolean.TRUE);
			} else {
				blockStickyFlags.add(Boolean.FALSE);
			}
		}

		// Other images
		imgFont = loadImage(skinDir + "/graphics/font.png");
		imgFontSmall = loadImage(skinDir + "/graphics/font_small.png");
		imgFrame = loadImage(skinDir + "/graphics/frame.png");
		imgFieldbg = loadImage(skinDir + "/graphics/fieldbg.png");
		imgFieldbg2 = loadImage(skinDir + "/graphics/fieldbg2.png");
		imgFieldbg2Small = loadImage(skinDir + "/graphics/fieldbg2_small.png");
		imgFieldbg2Big = loadImage(skinDir + "/graphics/fieldbg2_big.png");

		if (NullpoMinoSwing.propConfig.getProperty("option.showlineeffect", false)) {
			loadLineClearEffectImages();
		}
		if (NullpoMinoSwing.propConfig.getProperty("option.showbg", true)) {
			loadBackgroundImages();
		}

		// Sound effects
		if (NullpoMinoSwing.propConfig.getProperty("option.se", true)) {
			initSounds();
		}
	}

	public void initSounds() {
		String resourcesDir = NullpoMinoSwing.propConfig.getProperty("custom.skin.directory", "res");
		soundManager.load(Sounds.CURSOR, resourcesDir + "/se/cursor.wav");
		soundManager.load(Sounds.DECIDE, resourcesDir + "/se/decide.wav");
		soundManager.load(Sounds.ERASE1, resourcesDir + "/se/erase1.wav");
		soundManager.load(Sounds.ERASE2, resourcesDir + "/se/erase2.wav");
		soundManager.load(Sounds.ERASE3, resourcesDir + "/se/erase3.wav");
		soundManager.load(Sounds.ERASE4, resourcesDir + "/se/erase4.wav");
		soundManager.load(Sounds.DIED, resourcesDir + "/se/died.wav");
		soundManager.load(Sounds.GAME_OVER, resourcesDir + "/se/gameover.wav");
		soundManager.load(Sounds.HOLD, resourcesDir + "/se/hold.wav");
		soundManager.load(Sounds.HOLD_FAIL, resourcesDir + "/se/holdfail.wav");
		soundManager.load(Sounds.INITIAL_HOLD, resourcesDir + "/se/initialhold.wav");
		soundManager.load(Sounds.INITIAL_ROTATE, resourcesDir + "/se/initialrotate.wav");
		soundManager.load(Sounds.LEVEL_UP, resourcesDir + "/se/levelup.wav");
		soundManager.load(Sounds.LINE_FALL, resourcesDir + "/se/linefall.wav");
		soundManager.load(Sounds.LOCK, resourcesDir + "/se/lock.wav");
		soundManager.load(Sounds.MOVE, resourcesDir + "/se/move.wav");
		soundManager.load(Sounds.PAUSE, resourcesDir + "/se/pause.wav");
		soundManager.load(Sounds.ROTATE, resourcesDir + "/se/rotate.wav");
		soundManager.load(Sounds.STEP, resourcesDir + "/se/step.wav");
		soundManager.load(Sounds.PIECE_I, resourcesDir + "/se/piece0.wav");
		soundManager.load(Sounds.PIECE_L, resourcesDir + "/se/piece1.wav");
		soundManager.load(Sounds.PIECE_O, resourcesDir + "/se/piece2.wav");
		soundManager.load(Sounds.PIECE_Z, resourcesDir + "/se/piece3.wav");
		soundManager.load(Sounds.PIECE_T, resourcesDir + "/se/piece4.wav");
		soundManager.load(Sounds.PIECE_J, resourcesDir + "/se/piece5.wav");
		soundManager.load(Sounds.PIECE_S, resourcesDir + "/se/piece6.wav");
		soundManager.load(Sounds.PIECE_I1, resourcesDir + "/se/piece7.wav");
		soundManager.load(Sounds.PIECE_I2, resourcesDir + "/se/piece8.wav");
		soundManager.load(Sounds.PIECE_I3, resourcesDir + "/se/piece9.wav");
		soundManager.load(Sounds.PIECE_L3, resourcesDir + "/se/piece10.wav");
		soundManager.load(Sounds.HARDDROP, resourcesDir + "/se/harddrop.wav");
		soundManager.load(Sounds.SOFTDROP, resourcesDir + "/se/softdrop.wav");
		soundManager.load(Sounds.LEVEL_STOP, resourcesDir + "/se/levelstop.wav");
		soundManager.load(Sounds.ENDING_START, resourcesDir + "/se/endingstart.wav");
		soundManager.load(Sounds.EXCELLENT, resourcesDir + "/se/excellent.wav");
		soundManager.load(Sounds.B2B_START, resourcesDir + "/se/b2b_start.wav");
		soundManager.load(Sounds.B2B_CONTINUE, resourcesDir + "/se/b2b_continue.wav");
		soundManager.load(Sounds.B2B_END, resourcesDir + "/se/b2b_end.wav");
		soundManager.load(Sounds.GRADE_UP, resourcesDir + "/se/gradeup.wav");
		soundManager.load(Sounds.COUNTDOWN, resourcesDir + "/se/countdown.wav");
		soundManager.load(Sounds.TSPIN0, resourcesDir + "/se/tspin0.wav");
		soundManager.load(Sounds.TSPIN1, resourcesDir + "/se/tspin1.wav");
		soundManager.load(Sounds.TSPIN2, resourcesDir + "/se/tspin2.wav");
		soundManager.load(Sounds.TSPIN3, resourcesDir + "/se/tspin3.wav");
		soundManager.load(Sounds.READY, resourcesDir + "/se/ready.wav");
		soundManager.load(Sounds.GO, resourcesDir + "/se/go.wav");
		soundManager.load(Sounds.MOVE_FAIL, resourcesDir + "/se/movefail.wav");
		soundManager.load(Sounds.ROTATE_FAIL, resourcesDir + "/se/rotfail.wav");
		soundManager.load(Sounds.MEDAL, resourcesDir + "/se/medal.wav");
		soundManager.load(Sounds.CHANGE, resourcesDir + "/se/change.wav");
		soundManager.load(Sounds.BRAVO, resourcesDir + "/se/bravo.wav");
		soundManager.load(Sounds.COOL, resourcesDir + "/se/cool.wav");
		soundManager.load(Sounds.REGRET, resourcesDir + "/se/regret.wav");
		soundManager.load(Sounds.GARBAGE, resourcesDir + "/se/garbage.wav");
		soundManager.load(Sounds.STAGE_CLEAR, resourcesDir + "/se/stageclear.wav");
		soundManager.load(Sounds.STAGE_FAIL, resourcesDir + "/se/stagefail.wav");
		soundManager.load(Sounds.GEM, resourcesDir + "/se/gem.wav");
		soundManager.load(Sounds.DANGER, resourcesDir + "/se/danger.wav");
		soundManager.load(Sounds.MATCH_END, resourcesDir + "/se/matchend.wav");
		soundManager.load(Sounds.HURRY_UP, resourcesDir + "/se/hurryup.wav");
		soundManager.load(Sounds.SQUARE_SILVER, resourcesDir + "/se/square_s.wav");
		soundManager.load(Sounds.SQUARE_GOLD, resourcesDir + "/se/square_g.wav");
		soundManager.load(Sounds.SLIDE, resourcesDir + "/se/slide.wav");

		for (int i = 1; i < 21; i++) {
			soundManager.load("combo" + i, resourcesDir + "/se/combo" + i + ".wav");
		}
		soundManager.setVolume(NullpoMinoSwing.propConfig.getProperty("option.sevolume", 0.5f));
	}

	/**
	 * Load background images.
	 */
	public void loadBackgroundImages() {
		if (imgPlayBG == null) {
			imgPlayBG = new Image[BACKGROUND_MAX];

			String skindir = NullpoMinoSwing.propConfig.getProperty("custom.skin.directory", "res");
			for (int i = 0; i < BACKGROUND_MAX; i++) {
				imgPlayBG[i] = loadImage(skindir + "/graphics/back" + i + ".png");
			}
		}
	}

	/**
	 * Load line clear effect images.
	 */
	public void loadLineClearEffectImages() {
		String skindir = NullpoMinoSwing.propConfig.getProperty("custom.skin.directory", "res");

		if (imgBreak == null) {
			imgBreak = new Image[BLOCK_BREAK_MAX][BLOCK_BREAK_SEGMENTS];

			for (int i = 0; i < BLOCK_BREAK_MAX; i++) {
				for (int j = 0; j < BLOCK_BREAK_SEGMENTS; j++) {
					imgBreak[i][j] = loadImage(skindir + "/graphics/break" + i + "_" + j + ".png");
				}
			}
		}
		if (imgPErase == null) {
			imgPErase = new Image[PERASE_MAX];
			for (int i = 0; i < imgPErase.length; i++) {
				imgPErase[i] = loadImage(skindir + "/graphics/perase" + i + ".png");
			}
		}
	}

	/**
	 * Load an image
	 *
	 * @param file Image filesURL
	 * @return Image file (Failurenull)
	 */
	protected static Image loadImage(String file) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(file));
		} catch (IOException e) {
			log.error("Failed to load image " + file, e);
			img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		}
		return img;
	}
}

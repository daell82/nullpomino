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
package mu.nu.nullpo.gui.slick;

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.newdawn.slick.BigImage;
import org.newdawn.slick.Image;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.font.effects.ShadowEffect;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.BGMusicStatus;
import mu.nu.nullpo.game.event.SoundManager;
import mu.nu.nullpo.util.Sounds;

/**
 * Class to the management of image and sound
 */
@Log4j
@UtilityClass
public class ResourceHolderSlick {

	/** BackgroundOfcount */
	public static final int BACKGROUND_MAX = 20;

	/** Number of images for block spatter animation during line clears */
	public static final int BLOCK_BREAK_MAX = 8;

	/** Number of image splits for block spatter animation during line clears */
	public static final int BLOCK_BREAK_SEGMENTS = 2;

	/** Number of gem block clear effects */
	public static final int PERASE_MAX = 7;

	/** Block images */
	public static List<Image> imgNormalBlockList, imgSmallBlockList, imgBigBlockList;

	/** Block sticky flag */
	public static List<Boolean> blockStickyFlagList;

	/** Regular font */
	public static Image imgFont, imgFontSmall;

	/** Title */
	public static Image imgTitle;

	/** Menu Background */
	public static Image imgMenu;

	/** Field frame */
	public static Image imgFrame;

	/** Field background */
	public static Image imgFieldbg2, imgFieldbg2Small, imgFieldbg2Big;
	// public static Image imgFieldbg;

	/** Block spatter animation during line clears */
	public static Image[][] imgBreak;

	/** Effects for clearing gem blocks */
	public static Image[] imgPErase;

	/** In playBackground */
	public static Image[] imgPlayBG;

	/** TTF font */
	public static UnicodeFont ttfFont;

	/** Sound effects */
	public static SoundManager soundManager;

	/** BGM */
	private static Music[] bgm;

	/** Current BGM number */
	public static int bgmPlaying;

	/**
	 * Loading images and sound files
	 *
	 * @throws SlickException Failed to load
	 */
	@SuppressWarnings("unchecked")
	public static void load() throws SlickException {
		String skindir = NullpoMinoSlick.propConfig.getProperty("custom.skin.directory", "res");

		log.info("Loading Image");

		// Blocks
		int numBlocks = 0;
		File file = null;
		while (true) {
			file = new File(skindir + "/graphics/blockskin/normal/n" + numBlocks + ".png");
			if (file.canRead()) {
				numBlocks++;
			} else {
				break;
			}
		}
		log.debug(numBlocks + " block skins found");

		imgNormalBlockList = new LinkedList<>();
		imgSmallBlockList = new LinkedList<>();
		imgBigBlockList = new LinkedList<>();
		blockStickyFlagList = new LinkedList<>();

		for (int i = 0; i < numBlocks; i++) {
			Image imgNormal = loadImage(skindir + "/graphics/blockskin/normal/n" + i + ".png");
			imgNormalBlockList.add(imgNormal);
			imgSmallBlockList.add(loadImage(skindir + "/graphics/blockskin/small/s" + i + ".png"));
			imgBigBlockList.add(loadImage(skindir + "/graphics/blockskin/big/b" + i + ".png"));

			if (imgNormal.getWidth() >= 400 && imgNormal.getHeight() >= 304) {
				blockStickyFlagList.add(Boolean.TRUE);
			} else {
				blockStickyFlagList.add(Boolean.FALSE);
			}
		}

		// Other images
		imgFont = loadImage(skindir + "/graphics/font.png");
		imgFontSmall = loadImage(skindir + "/graphics/font_small.png");
		imgTitle = loadImage(skindir + "/graphics/title.png");
		imgMenu = loadImage(skindir + "/graphics/menu.png");
		imgFrame = loadImage(skindir + "/graphics/frame.png");
		imgFieldbg2 = loadImage(skindir + "/graphics/fieldbg2.png");
		imgFieldbg2Small = loadImage(skindir + "/graphics/fieldbg2_small.png");
		imgFieldbg2Big = loadImage(skindir + "/graphics/fieldbg2_big.png");

		if (NullpoMinoSlick.propConfig.getProperty("option.showlineeffect", true)) {
			loadLineClearEffectImages();
		}
		if (NullpoMinoSlick.propConfig.getProperty("option.showbg", true)) {
			loadBackgroundImages();
		}

		// Font
		try {
			ttfFont = new UnicodeFont(skindir + "/font/font.ttf", 16, true, false);
			ttfFont.getEffects().add(new ShadowEffect(Color.black, 1, 1, 1));
			ttfFont.getEffects().add(new ColorEffect(Color.white));
		} catch (SlickException e) {
			log.error("TTF Font load failed", e);
			ttfFont = null;
		}

		// Sound effects
		soundManager = new SlickSoundManager();
		if (NullpoMinoSlick.propConfig.getProperty("option.se", true)) {
			log.info("Loading Sound Effect");
			soundManager.load(Sounds.CURSOR, skindir + "/se/cursor.wav");
			soundManager.load(Sounds.DECIDE, skindir + "/se/decide.wav");
			soundManager.load(Sounds.ERASE1, skindir + "/se/erase1.wav");
			soundManager.load(Sounds.ERASE2, skindir + "/se/erase2.wav");
			soundManager.load(Sounds.ERASE3, skindir + "/se/erase3.wav");
			soundManager.load(Sounds.ERASE4, skindir + "/se/erase4.wav");
			soundManager.load(Sounds.DIED, skindir + "/se/died.wav");
			soundManager.load(Sounds.GAME_OVER, skindir + "/se/gameover.wav");
			soundManager.load(Sounds.HOLD, skindir + "/se/hold.wav");
			soundManager.load(Sounds.HOLD_FAIL, skindir + "/se/holdfail.wav");
			soundManager.load(Sounds.INITIAL_HOLD, skindir + "/se/initialhold.wav");
			soundManager.load(Sounds.INITIAL_ROTATE, skindir + "/se/initialrotate.wav");
			soundManager.load(Sounds.LEVEL_UP, skindir + "/se/levelup.wav");
			soundManager.load(Sounds.LINE_FALL, skindir + "/se/linefall.wav");
			soundManager.load(Sounds.LOCK, skindir + "/se/lock.wav");
			soundManager.load(Sounds.MOVE, skindir + "/se/move.wav");
			soundManager.load(Sounds.PAUSE, skindir + "/se/pause.wav");
			soundManager.load(Sounds.ROTATE, skindir + "/se/rotate.wav");
			soundManager.load(Sounds.STEP, skindir + "/se/step.wav");
			soundManager.load(Sounds.PIECE_I, skindir + "/se/piece0.wav");
			soundManager.load(Sounds.PIECE_L, skindir + "/se/piece1.wav");
			soundManager.load(Sounds.PIECE_O, skindir + "/se/piece2.wav");
			soundManager.load(Sounds.PIECE_Z, skindir + "/se/piece3.wav");
			soundManager.load(Sounds.PIECE_T, skindir + "/se/piece4.wav");
			soundManager.load(Sounds.PIECE_J, skindir + "/se/piece5.wav");
			soundManager.load(Sounds.PIECE_S, skindir + "/se/piece6.wav");
			soundManager.load(Sounds.PIECE_I1, skindir + "/se/piece7.wav");
			soundManager.load(Sounds.PIECE_I2, skindir + "/se/piece8.wav");
			soundManager.load(Sounds.PIECE_I3, skindir + "/se/piece9.wav");
			soundManager.load(Sounds.PIECE_L3, skindir + "/se/piece10.wav");
			soundManager.load(Sounds.HARDDROP, skindir + "/se/harddrop.wav");
			soundManager.load(Sounds.SOFTDROP, skindir + "/se/softdrop.wav");
			soundManager.load(Sounds.LEVEL_STOP, skindir + "/se/levelstop.wav");
			soundManager.load(Sounds.ENDING_START, skindir + "/se/endingstart.wav");
			soundManager.load(Sounds.EXCELLENT, skindir + "/se/excellent.wav");
			soundManager.load(Sounds.B2B_START, skindir + "/se/b2b_start.wav");
			soundManager.load(Sounds.B2B_CONTINUE, skindir + "/se/b2b_continue.wav");
			soundManager.load(Sounds.B2B_END, skindir + "/se/b2b_end.wav");
			soundManager.load(Sounds.GRADE_UP, skindir + "/se/gradeup.wav");
			soundManager.load(Sounds.COUNTDOWN, skindir + "/se/countdown.wav");
			soundManager.load(Sounds.TSPIN0, skindir + "/se/tspin0.wav");
			soundManager.load(Sounds.TSPIN1, skindir + "/se/tspin1.wav");
			soundManager.load(Sounds.TSPIN2, skindir + "/se/tspin2.wav");
			soundManager.load(Sounds.TSPIN3, skindir + "/se/tspin3.wav");
			soundManager.load(Sounds.READY, skindir + "/se/ready.wav");
			soundManager.load(Sounds.GO, skindir + "/se/go.wav");
			soundManager.load(Sounds.MOVE_FAIL, skindir + "/se/movefail.wav");
			soundManager.load(Sounds.ROTATE_FAIL, skindir + "/se/rotfail.wav");
			soundManager.load(Sounds.MEDAL, skindir + "/se/medal.wav");
			soundManager.load(Sounds.CHANGE, skindir + "/se/change.wav");
			soundManager.load(Sounds.BRAVO, skindir + "/se/bravo.wav");
			soundManager.load(Sounds.COOL, skindir + "/se/cool.wav");
			soundManager.load(Sounds.REGRET, skindir + "/se/regret.wav");
			soundManager.load(Sounds.GARBAGE, skindir + "/se/garbage.wav");
			soundManager.load(Sounds.STAGE_CLEAR, skindir + "/se/stageclear.wav");
			soundManager.load(Sounds.STAGE_FAIL, skindir + "/se/stagefail.wav");
			soundManager.load(Sounds.GEM, skindir + "/se/gem.wav");
			soundManager.load(Sounds.DANGER, skindir + "/se/danger.wav");
			soundManager.load(Sounds.MATCH_END, skindir + "/se/matchend.wav");
			soundManager.load(Sounds.HURRY_UP, skindir + "/se/hurryup.wav");
			soundManager.load(Sounds.SQUARE_SILVER, skindir + "/se/square_s.wav");
			soundManager.load(Sounds.SQUARE_GOLD, skindir + "/se/square_g.wav");
			soundManager.load(Sounds.SLIDE, skindir + "/se/slide.wav");

			for (int i = 1; i < 21; i++) {
				soundManager.load("combo" + i, skindir + "/se/combo" + i + ".wav");
			}
		}

		// Music
		bgm = new Music[BGMusicStatus.BGM_COUNT];
		bgmPlaying = -1;

		if (NullpoMinoSlick.propConfig.getProperty("option.bgmpreload", false)) {
			for (int i = 0; i < BGMusicStatus.BGM_COUNT; i++) {
				bgmLoad(i, false);
			}
		}
	}

	/**
	 * Load background images.
	 */
	public static void loadBackgroundImages() {
		if (imgPlayBG == null) {
			imgPlayBG = new Image[BACKGROUND_MAX];

			String skindir = NullpoMinoSlick.propConfig.getProperty("custom.skin.directory", "res");
			for (int i = 0; i < imgPlayBG.length; i++) {
				imgPlayBG[i] = loadImage(skindir + "/graphics/back" + i + ".png");
			}
		}
	}

	/**
	 * Load line clear effect images.
	 */
	public static void loadLineClearEffectImages() {
		String skindir = NullpoMinoSlick.propConfig.getProperty("custom.skin.directory", "res");

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
	 * Load image
	 *
	 * @param filename Filename
	 * @return Image data
	 */
	public static Image loadImage(String filename) {
		if (NullpoMinoSlick.useBigImageTextureLoad) {
			return loadBigImage(filename);
		}
		return loadNormalImage(filename);
	}

	/**
	 * Load image (uses normal regular Image)
	 *
	 * @param filename Filename
	 * @return Image data
	 */
	public static Image loadNormalImage(String filename) {
		log.debug("Loading image from " + filename);

		Image img = null;
		try {
			img = new Image(filename);
		} catch (Throwable e) {
			log.error("Failed to load image from " + filename, e);
			try {
				img = new Image(256, 256);
			} catch (Throwable e2) {
			}
		}

		return img;
	}

	/**
	 * Load image (uses normal BigImage)
	 *
	 * @param filename Filename
	 * @return Image data
	 */
	public static BigImage loadBigImage(String filename) {
		log.debug("Loading big image from " + filename);

		BigImage bigImg = null;
		try {
			bigImg = new BigImage(filename);
		} catch (Throwable e) {
			log.error("Failed to load big image from " + filename, e);
		}

		return bigImg;
	}

	/**
	 * Specified numberOfBGMRead into memory
	 *
	 * @param no      BGM number
	 * @param showerr displayed on the console when an exception occurs
	 */
	public static void bgmLoad(int no, boolean showerr) {
		if (!NullpoMinoSlick.propConfig.getProperty("option.bgm", false)) {
			return;
		}

		if (bgm[no] == null) {
			if (showerr) {
				log.info("Loading BGM" + no);
			}

			try {
				String filename = NullpoMinoSlick.propMusic.getProperty("music.filename." + no, null);
				if (filename == null || filename.isEmpty()) {
					if (showerr) {
						log.info("BGM" + no + " not available");
					}
					return;
				}

				boolean streaming = NullpoMinoSlick.propConfig.getProperty("option.bgmstreaming", true);

				bgm[no] = new Music(filename, streaming);

				if (!showerr) {
					log.info("Loaded BGM" + no);
				}
			} catch (Throwable e) {
				if (showerr) {
					log.error("BGM " + no + " load failed", e);
				} else {
					log.warn("BGM " + no + " load failed");
				}
			}
		}
	}

	/**
	 * Specified numberOfBGMPlay
	 *
	 * @param no BGM number
	 */
	public static void bgmStart(int no) {
		if (!NullpoMinoSlick.propConfig.getProperty("option.bgm", false)) {
			return;
		}

		bgmStop();

		float bgmvolume = NullpoMinoSlick.propConfig.getProperty("option.bgmvolume", 128f);
		NullpoMinoSlick.appGameContainer.setMusicVolume(bgmvolume / 128f);

		if (no >= 0) {
			if (bgm[no] == null) {
				bgmLoad(no, true);
			}

			if (bgm[no] != null) {
				try {
					if (NullpoMinoSlick.propMusic.getProperty("music.noloop." + no, false)) {
						bgm[no].play();
					} else {
						bgm[no].loop();
					}
				} catch (Throwable e) {
					log.error("Failed to play music " + no, e);
				}
			}

			bgmPlaying = no;
		} else {
			bgmPlaying = -1;
		}
	}

	/**
	 * Current BGMPause
	 */
	public static void bgmPause() {
		if (bgmPlaying >= 0) {
			if (bgm[bgmPlaying] != null) {
				bgm[bgmPlaying].pause();
			}
		}
	}

	/**
	 * PausedBGMResumes
	 */
	public static void bgmResume() {
		if (bgmPlaying >= 0) {
			if (bgm[bgmPlaying] != null) {
				bgm[bgmPlaying].resume();
			}
		}
	}

	/**
	 * BGMWhether during playback
	 *
	 * @return If during playbacktrue
	 */
	public static boolean bgmIsPlaying() {
		if (bgmPlaying >= 0) {
			if (bgm[bgmPlaying] != null) {
				return bgm[bgmPlaying].playing();
			}
		}

		return false;
	}

	/**
	 * BGMStop
	 */
	public static void bgmStop() {
		for (int i = 0; i < BGMusicStatus.BGM_COUNT; i++) {
			if (bgm[i] != null) {
				bgm[i].pause();
				bgm[i].stop();
			}
		}
	}

	/**
	 * AllBGMFreed from memory
	 */
	public static void bgmUnloadAll() {
		for (int i = 0; i < BGMusicStatus.BGM_COUNT; i++) {
			if (bgm[i] == null) {
				continue;
			}
			bgm[i].stop();
			if (!NullpoMinoSlick.propConfig.getProperty("option.bgmpreload", false)) {
				bgm[i] = null;
			}
		}
	}
}

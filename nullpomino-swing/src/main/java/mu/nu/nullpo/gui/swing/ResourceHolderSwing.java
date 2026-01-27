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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

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
	private final WaveEngine soundManager = new WaveEngine();

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
			Image imgNormal = loadImage(getURL(skinDir + "/graphics/blockskin/normal/n" + i + ".png"));
			normalBlockImages.add(imgNormal);
			smallBlockImages.add(loadImage(getURL(skinDir + "/graphics/blockskin/small/s" + i + ".png")));
			bigBlockImages.add(loadImage(getURL(skinDir + "/graphics/blockskin/big/b" + i + ".png")));

			if (imgNormal.getWidth(null) >= 400 && imgNormal.getHeight(null) >= 304) {
				blockStickyFlags.add(Boolean.TRUE);
			} else {
				blockStickyFlags.add(Boolean.FALSE);
			}
		}

		// Other images
		imgFont = loadImage(getURL(skinDir + "/graphics/font.png"));
		imgFontSmall = loadImage(getURL(skinDir + "/graphics/font_small.png"));
		imgFrame = loadImage(getURL(skinDir + "/graphics/frame.png"));
		imgFieldbg = loadImage(getURL(skinDir + "/graphics/fieldbg.png"));
		imgFieldbg2 = loadImage(getURL(skinDir + "/graphics/fieldbg2.png"));
		imgFieldbg2Small = loadImage(getURL(skinDir + "/graphics/fieldbg2_small.png"));
		imgFieldbg2Big = loadImage(getURL(skinDir + "/graphics/fieldbg2_big.png"));

		if (NullpoMinoSwing.propConfig.getProperty("option.showlineeffect", false)) {
			loadLineClearEffectImages();
		}
		if (NullpoMinoSwing.propConfig.getProperty("option.showbg", true)) {
			loadBackgroundImages();
		}

		// Sound effects
		if (NullpoMinoSwing.propConfig.getProperty("option.se", true)) {
			soundManager.initSounds();
		}
	}

	/**
	 * Load background images.
	 */
	public void loadBackgroundImages() {
		if (imgPlayBG == null) {
			imgPlayBG = new Image[BACKGROUND_MAX];

			String skindir = NullpoMinoSwing.propConfig.getProperty("custom.skin.directory", "res");
			for (int i = 0; i < BACKGROUND_MAX; i++) {
				imgPlayBG[i] = loadImage(getURL(skindir + "/graphics/back" + i + ".png"));
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
					imgBreak[i][j] = loadImage(getURL(skindir + "/graphics/break" + i + "_" + j + ".png"));
				}
			}
		}
		if (imgPErase == null) {
			imgPErase = new Image[PERASE_MAX];
			for (int i = 0; i < imgPErase.length; i++) {
				imgPErase[i] = loadImage(getURL(skindir + "/graphics/perase" + i + ".png"));
			}
		}
	}

	/**
	 * Load an image
	 *
	 * @param url Image filesURL
	 * @return Image file (Failurenull)
	 */
	protected static BufferedImage loadImage(URL url) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(url);
		} catch (IOException e) {
			log.error("Failed to load image " + url, e);
			img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		}
		return img;
	}

	/**
	 * Resource FilesURLReturns
	 *
	 * @param filename Filename
	 * @return Resource FilesURL
	 */
	public static URL getURL(String filename) {
		try {
			String file = filename.replace(File.separator, "/");
			if (!file.startsWith("/")) {
				String dir = System.getProperty("user.dir");
				dir = dir.replace(File.separator, "/") + "/";
				if (!dir.startsWith("/")) {
					dir = "/" + dir;
				}
				file = dir + file;
			}
			return new File(file).toURI().toURL();
		} catch (MalformedURLException e) {
			log.warn("Invalid URL: " + filename, e);
			return null;
		}
	}
}

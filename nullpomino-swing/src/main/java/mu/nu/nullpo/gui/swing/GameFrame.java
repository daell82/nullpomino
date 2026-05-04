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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.RenderedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.net.NetObserverClient;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.gui.GameKeyDummy;
import mu.nu.nullpo.util.Colors;

/**
 * Game screen frame
 */
@Log4j
public class GameFrame extends JFrame implements Runnable {

	private static final long ONE_SECOND_IN_NS = 1_000_000_000L;

	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Parent window */
	protected NullpoMinoSwing owner = null;

	/** The size of the border and title bar */
	protected Insets insets = null;

	/** BufferStrategy */
	protected BufferStrategy bufferStrategy = null;

	/** trueThread moves between */
	protected volatile boolean running = false;

	/** FPSFor calculation */
	protected long calcInterval = 0;

	/** FPSFor calculation */
	protected long prevCalcTime = 0;

	/** frame count */
	protected long frameCount = 0;

	/** MaximumFPS (Setting) */
	protected int maxfps;

	/** Current MaximumFPS */
	protected int maxfpsCurrent = 0;

	/** Current Pause time */
	protected long periodCurrent = 0;

	/** ActualFPS */
	protected double actualFPS = 0.0;

	/** FPSDisplayDecimalFormat */
	protected DecimalFormat df = new DecimalFormat("0.0");

	/** Used by perfect fps mode */
	protected long perfectFPSDelay = 0;

	/** True to use perfect FPS */
	protected boolean perfectFPSMode = false;

	/** Execute Thread.yield() during Perfect FPS mode */
	protected boolean perfectYield = true;

	/**
	 * True if execute Toolkit.getDefaultToolkit().sync() at the end of each frame
	 */
	protected boolean syncDisplay = true;

	/** Screen width */
	protected int screenWidth = 640;

	/** Screen height */
	protected int screenHeight = 480;

	/** Pause state */
	protected boolean pause = false;

	/** Pose hidden message */
	protected boolean pauseMessageHide = false;

	/** Pause menuOfCursor position */
	protected int cursor = 0;

	/** Number of frames remaining until pause key can be used */
	protected int pauseFrame = 0;

	/** Double speedMode */
	protected int fastforward = 0;

	/** ScreenshotCreating flag */
	protected boolean ssflag = false;

	/** ScreenshotUseImage */
	protected Image ssImage = null;

	/** frame Step is enabled flag */
	protected boolean enableframestep = false;

	/** FPSDisplay */
	protected boolean showfps = true;

	/** Ingame flag */
	protected boolean[] isInGame;

	/** If net playtrue */
	protected boolean isNetPlay = false;

	/** Mode name to enter (null=Exit) */
	protected String strModeToEnter = "";

	/** Previous ingame flag (Used by title-bar text change) */
	protected boolean prevInGameFlag = false;

	private final ResourceHolderSwing resourceManager = ResourceHolderSwing.getInstance();

	/**
	 * Constructor
	 *
	 * @param owner Parent window
	 * @throws HeadlessException the Exceptions such as Keyboard, Mouse, if there is
	 *                           no display
	 */
	public GameFrame(NullpoMinoSwing owner) throws HeadlessException {
		super();
		this.owner = owner;

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(NullpoMinoSwing.getUIText("Title_Game"));
		setBackground(Color.black);
		setResizable(false);
		setIgnoreRepaint(true);

		addWindowListener(new GameFrameWindowEvent());
		addKeyListener(new GameFrameKeyEvent());

		maxfps = NullpoMinoSwing.propConfig.getProperty("option.maxfps", 60);

		log.debug("GameFrame created");
	}

	/**
	 * Display the game window
	 */
	public void displayWindow() {
		setVisible(true);

		screenWidth = NullpoMinoSwing.propConfig.getProperty("option.screenwidth", 640);
		screenHeight = NullpoMinoSwing.propConfig.getProperty("option.screenheight", 480);
		insets = getInsets();
		int width = screenWidth + insets.left + insets.right;
		int height = screenHeight + insets.top + insets.bottom;
		setSize(width, height);

		if (!running) {
			new Thread(this, "Game Thread").start();
		}
	}

	/**
	 * End processing
	 */
	public void shutdown() {
		if (ssImage != null) {
			ssImage.flush();
			ssImage = null;
		}
		if (isNetPlay) {
			if (owner.netLobby != null) {
				try {
					owner.netLobby.shutdown();
				} catch (Exception e) {
					log.debug("Exception on NetLobby shutdown", e);
				}
				owner.netLobby = null;
			}

			// Reload global config (because it can change rules)
			NullpoMinoSwing.loadGlobalConfig();
		}
		running = false;
		owner.setVisible(true);
		setVisible(false);
	}

	/**
	 * Processing of the thread
	 */
	@Override
	public void run() {
		boolean sleepFlag;
		long beforeTime, afterTime, timeDiff, sleepTime, sleepTimeInMillis;
		long overSleepTime = 0L;
		int noDelays = 0;

		// Initialization
		maxfpsCurrent = maxfps;
		periodCurrent = (long) (1.0 / maxfpsCurrent * ONE_SECOND_IN_NS);
		log.debug("current period: " + periodCurrent);
		beforeTime = System.nanoTime();
		prevCalcTime = beforeTime;
		pause = false;
		pauseMessageHide = false;
		fastforward = 0;
		cursor = 0;
		prevInGameFlag = false;
		isInGame = new boolean[2];
		GameKeySwing.gamekey[0].clear();
		GameKeySwing.gamekey[1].clear();
		updateTitleBarCaption();

		// Settings to take effect
		enableframestep = NullpoMinoSwing.propConfig.getProperty("option.enableframestep", false);
		showfps = NullpoMinoSwing.propConfig.getProperty("option.showfps", true);
		perfectFPSMode = NullpoMinoSwing.propConfig.getProperty("option.perfectFPSMode", false);
		perfectYield = NullpoMinoSwing.propConfig.getProperty("option.perfectYield", true);
		syncDisplay = NullpoMinoSwing.propConfig.getProperty("option.syncDisplay", true);

		// ObserverStart
		if (!isNetPlay) {
			owner.startObserverClient();
		}

		// Main loop
		log.debug("Game thread start");
		running = true;
		perfectFPSDelay = System.nanoTime();
		while (running) {
			if (isNetPlay) {
				gameUpdateNet();
				gameRenderNet();
			} else if (isVisible() && isActive()) {
				gameUpdate();
				gameRender();
			} else {
				GameKeySwing.gamekey[0].clear();
				GameKeySwing.gamekey[1].clear();
			}

			// FPS cap
			sleepFlag = false;

			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;

			sleepTime = periodCurrent - timeDiff - overSleepTime;
			sleepTimeInMillis = sleepTime / 1_000_000L;

			if (sleepTimeInMillis >= 4 && !perfectFPSMode) {
				// If it is possible to use sleep
				if (maxfps > 0) {
					try {
						Thread.sleep(sleepTimeInMillis);
					} catch (InterruptedException e) {
						log.debug("Game thread interrupted", e);
					}
				}
				// sleep() oversleep
				overSleepTime = System.nanoTime() - afterTime - sleepTime;
				perfectFPSDelay = System.nanoTime();
				sleepFlag = true;
			} else if (perfectFPSMode || sleepTime > 0) {
				// Perfect FPS
				overSleepTime = 0L;
				if (maxfpsCurrent > maxfps + 5) {
					maxfpsCurrent = maxfps + 5;
				}
				if (perfectYield) {
					while (System.nanoTime() < perfectFPSDelay + ONE_SECOND_IN_NS / maxfps) {
						Thread.yield();
					}
				} else {
					while (System.nanoTime() < perfectFPSDelay + ONE_SECOND_IN_NS / maxfps) {
					}
				}
				perfectFPSDelay += 1_000_000_000 / maxfps;

				// Don't run in super fast after the heavy slowdown
				if (System.nanoTime() > (perfectFPSDelay + 2 * ONE_SECOND_IN_NS) / maxfps) {
					perfectFPSDelay = System.nanoTime();
				}

				sleepFlag = true;
			}

			if (!sleepFlag) {
				// Impossible to sleep!
				overSleepTime = 0L;
				if (++noDelays >= 16) {
					Thread.yield();
					noDelays = 0;
				}
				perfectFPSDelay = System.nanoTime();
			}

			beforeTime = System.nanoTime();
			calcFPS(periodCurrent);
		}

		GameManager gameManager = owner.getGameManager();
		if (gameManager != null && gameManager.mode != null) {
			gameManager.shutdown();
		}

		if (!isNetPlay) {
			owner.stopObserverClient();
		}

		log.debug("Game thread end");
	}

	/**
	 * Update game state
	 */
	protected void gameUpdate() {
		GameManager gameManager = owner.getGameManager();
		if (gameManager == null || gameManager.engines == null) {
			return;
		}

		// Set ingame flag
		for (int i = 0; i < 2; i++) {
			boolean prevInGame = isInGame[i];

			if (gameManager.engines.length > i) {
				isInGame[i] = gameManager.engines[i].isInGame;
			}
			if (pause && !enableframestep) {
				isInGame[i] = false;
			}

			if (prevInGame != isInGame[i]) {
				GameKeySwing.gamekey[i].clear();
			}
		}

		GameKeySwing.gamekey[0].update();
		GameKeySwing.gamekey[1].update();

		// Title bar update
		if (gameManager.engines.length > 0 && gameManager.engines[0] != null) {
			boolean nowInGame = gameManager.engines[0].isInGame;
			if (prevInGameFlag != nowInGame) {
				prevInGameFlag = nowInGame;
				updateTitleBarCaption();
			}
		}

		// Pause button
		if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_PAUSE)
				|| GameKeySwing.gamekey[1].isPushKey(GameKeyDummy.BUTTON_PAUSE)) {
			if (!pause) {
				if (gameManager.isGameActive() && pauseFrame <= 0) {
					resourceManager.getSoundManager().play("pause");
					pause = true;
					if (!enableframestep) {
						pauseFrame = 5;
					}
					cursor = 0;
				}
			} else {
				resourceManager.getSoundManager().play("pause");
				pause = false;
				pauseFrame = 0;
			}
			updateTitleBarCaption();
		}
		// Pause menu
		if (pause && !enableframestep && !pauseMessageHide) {
			// Cursor movement
			if (GameKeySwing.gamekey[0].isMenuRepeatKey(GameKeyDummy.BUTTON_UP)) {
				resourceManager.getSoundManager().play("cursor");
				cursor--;

				if (cursor < 0) {
					if (gameManager.replayMode && !gameManager.replayRerecord) {
						cursor = 3;
					} else {
						cursor = 2;
					}
				}
			}
			if (GameKeySwing.gamekey[0].isMenuRepeatKey(GameKeyDummy.BUTTON_DOWN)) {
				resourceManager.getSoundManager().play("cursor");
				cursor++;
				if (cursor > 3) {
					cursor = 0;
				}

				if ((!gameManager.replayMode || gameManager.replayRerecord) && cursor > 2) {
					cursor = 0;
				}
			}

			// Confirm
			if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_A)) {
				resourceManager.getSoundManager().play("decide");
				switch (cursor) {
				case 0 -> {
					// Resumption
					pause = false;
					pauseFrame = 0;
					GameKeySwing.gamekey[0].clear();
				}
				case 1 -> {
					// Retry
					pause = false;
					gameManager.reset();
				}
				case 2 -> shutdown(); // End
				case 3 -> {
					// Replay re-record
					gameManager.replayRerecord = true;
					cursor = 0;
				}
				default -> { // nothing
				}
				}
				updateTitleBarCaption();
			}
			// Unpause by cancel key
			else if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_B) && pauseFrame <= 0) {
				resourceManager.getSoundManager().play("pause");
				pause = false;
				pauseFrame = 5;
				GameKeySwing.gamekey[0].clear();
				updateTitleBarCaption();
			}
		}
		if (pauseFrame > 0) {
			pauseFrame--;
		}

		// Hide pause menu
		pauseMessageHide = GameKeySwing.gamekey[0].isPressKey(GameKeyDummy.BUTTON_C);

		if (gameManager.replayMode && !gameManager.replayRerecord && gameManager.engines[0].gameActive) {
			// Replay speed
			if (GameKeySwing.gamekey[0].isMenuRepeatKey(GameKeyDummy.BUTTON_LEFT) && fastforward > 0) {
				fastforward--;
			}

			if (GameKeySwing.gamekey[0].isMenuRepeatKey(GameKeyDummy.BUTTON_RIGHT) && fastforward < 98) {
				fastforward++;
			}

			// Replay re-record
			if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_D)) {
				gameManager.replayRerecord = true;
				cursor = 0;
			}
			// Show invisible blocks in replay
			if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_E)) {
				gameManager.replayShowInvisible = !gameManager.replayShowInvisible;
				cursor = 0;
			}
		} else {
			fastforward = 0;
		}

		// Execute game loops
		if (!pause || GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_FRAMESTEP) && enableframestep) {
			for (int i = 0; i < Math.min(gameManager.getPlayers(), 2); i++) {
				if (!gameManager.replayMode || gameManager.replayRerecord || !gameManager.engines[i].gameActive) {
					GameKeySwing.gamekey[i].inputStatusUpdate(gameManager.engines[i].ctrl);
				}
			}

			for (int i = 0; i <= fastforward; i++) {
				gameManager.updateAll();
			}
		}

		// Retry button
		if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_RETRY)
				|| GameKeySwing.gamekey[1].isPushKey(GameKeyDummy.BUTTON_RETRY)) {
			pause = false;
			gameManager.reset();
		}

		// Return to title
		if (gameManager.getQuitFlag() || GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_GIVEUP)
				|| GameKeySwing.gamekey[1].isPushKey(GameKeyDummy.BUTTON_GIVEUP)) {
			shutdown();
			return;
		}

		// Screenshot button
		if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_SCREENSHOT)
				|| GameKeySwing.gamekey[1].isPushKey(GameKeyDummy.BUTTON_SCREENSHOT)) {
			ssflag = true;
		}

		// Quit button
		if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_QUIT)
				|| GameKeySwing.gamekey[1].isPushKey(GameKeyDummy.BUTTON_QUIT)) {
			shutdown();
			owner.shutdown();
		}
	}

	/**
	 * Update game state (for netplay)
	 */
	protected void gameUpdateNet() {
		GameManager gameManager = owner.getGameManager();
		if (gameManager == null || gameManager.engines == null) {
			return;
		}

		try {
			// Set ingame flag
			boolean prevInGame = isInGame[0];

			if (gameManager.engines.length > 0) {
				isInGame[0] = gameManager.engines[0].isInGame;
			}
			if (pause && !enableframestep) {
				isInGame[0] = false;
			}

			if (prevInGame != isInGame[0]) {
				GameKeySwing.gamekey[0].clear();
			}

			// Update button inputs
			if (isVisible() && isActive()) {
				GameKeySwing.gamekey[0].update();
			} else {
				GameKeySwing.gamekey[0].clear();
			}

			// Title bar update
			if (gameManager.engines.length > 0 && gameManager.engines[0] != null) {
				boolean nowInGame = gameManager.engines[0].isInGame;
				if (prevInGameFlag != nowInGame) {
					prevInGameFlag = nowInGame;
					updateTitleBarCaption();
				}
			}

			// Execute game loops
			if (gameManager.mode != null) {
				GameKeySwing.gamekey[0].inputStatusUpdate(gameManager.engines[0].ctrl);
				gameManager.updateAll();

				// Return to title
				if (gameManager.getQuitFlag()) {
					shutdown();
					return;
				}

				// Retry button
				if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_RETRY)) {
					gameManager.mode.netplayOnRetryKey(gameManager.engines[0], 0);
				}
			}

			// Screenshot button
			if (GameKeySwing.gamekey[0].isPushKey(GameKeyDummy.BUTTON_SCREENSHOT)
					|| GameKeySwing.gamekey[1].isPushKey(GameKeyDummy.BUTTON_SCREENSHOT)) {
				ssflag = true;
			}

			// Enter to new mode
			if (strModeToEnter == null) {
				owner.enterNewMode(null);
				strModeToEnter = "";
			} else if (!strModeToEnter.isEmpty()) {
				owner.enterNewMode(strModeToEnter);
				strModeToEnter = "";
			}
		} catch (NullPointerException e) {
			try {
				if (gameManager.getQuitFlag()) {
					shutdown();
					return;
				} else {
					log.error("update NPE", e);
				}
			} catch (Throwable e2) {
			}
		} catch (Exception e) {
			try {
				if (gameManager.getQuitFlag()) {
					shutdown();
					return;
				} else {
					log.error("update fail", e);
				}
			} catch (Throwable e2) {
			}
		}
	}

	/**
	 * Rendering
	 */
	protected void gameRender() {
		GameManager gameManager = owner.getGameManager();
		if (gameManager == null) {
			return;
		}

		// Prepare the screen
		if (ssImage == null) {
			ssImage = createImage(640, 480);
		}
		if (bufferStrategy == null || bufferStrategy.contentsLost()) {
			try {
				createBufferStrategy(2);
				bufferStrategy = getBufferStrategy();
			} catch (Exception _) {
				return;
			}
		}

		Graphics2D g = null;
		if (ssflag || screenWidth != 640 || screenHeight != 480) {
			g = (Graphics2D) ssImage.getGraphics();
		} else {
			g = (Graphics2D) bufferStrategy.getDrawGraphics();
			if (insets != null) {
				g.translate(insets.left, insets.top);
			}
		}

		// Game screen
		NormalFontSwing.graphics = g;
		((RendererSwing) gameManager.renderer).setGraphics(g);
		gameManager.renderAll();

		if (gameManager.engines.length > 0 && gameManager.engines[0] != null) {
			int offsetX = gameManager.renderer.getFieldDisplayPositionX(gameManager.engines[0], 0);
			int offsetY = gameManager.renderer.getFieldDisplayPositionY(gameManager.engines[0], 0);

			// Pause menu
			if (pause && !enableframestep && !pauseMessageHide) {
				NormalFontSwing.printFont(offsetX + 12, offsetY + 188 + cursor * 16, "b", Colors.FONT_RED);
				NormalFontSwing.printFont(offsetX + 28, offsetY + 188, "CONTINUE", cursor == 0);
				NormalFontSwing.printFont(offsetX + 28, offsetY + 204, "RETRY", cursor == 1);
				NormalFontSwing.printFont(offsetX + 28, offsetY + 220, "END", cursor == 2);
				if (gameManager.replayMode && !gameManager.replayRerecord) {
					NormalFontSwing.printFont(offsetX + 28, offsetY + 236, "RERECORD", cursor == 3);
				}
			}

			// Fast forward
			if (fastforward != 0) {
				NormalFontSwing.printFont(offsetX, offsetY + 376, "e" + (fastforward + 1), Colors.FONT_ORANGE);
			}
			if (gameManager.replayShowInvisible) {
				NormalFontSwing.printFont(offsetX, offsetY + 392, "SHOW INVIS", Colors.FONT_ORANGE);
			}
		}

		// FPSDisplay
		if (showfps) {
			if (perfectFPSMode) {
				NormalFontSwing.printFont(0, 480 - 16, df.format(actualFPS), Colors.FONT_BLUE, 1.0f);
			} else {
				NormalFontSwing.printFont(0, 480 - 16, df.format(actualFPS) + "/" + maxfpsCurrent, Colors.FONT_BLUE,
						1.0f);
			}
		}

		// ObserverInformation
		NetObserverClient obClient = owner.getObserverClient();
		if (obClient != null && obClient.isConnected()) {
			int observerCount = obClient.getObserverCount();
			int playerCount = obClient.getPlayerCount();
			int fontcolor = Colors.FONT_BLUE;
			if (observerCount > 1) {
				fontcolor = Colors.FONT_GREEN;
			}
			if (observerCount > 0 && playerCount > 0) {
				fontcolor = Colors.FONT_RED;
			}
			String strObserverInfo = String.format("%d/%d", observerCount, playerCount);
			String strObserverString = String.format("%40s", strObserverInfo);
			NormalFontSwing.printFont(0, 480 - 16, strObserverString, fontcolor);
		}

		// Displayed on the screen /ScreenshotCreating
		g.dispose();
		if (ssflag || screenWidth != 640 || screenHeight != 480) {
			if (ssflag) {
				saveScreenShot();
			}

			if (insets != null) {
				Graphics g2 = getGraphics();
				if (screenWidth != 640 || screenHeight != 480) {
					g2.drawImage(ssImage, insets.left, insets.top, screenWidth, screenHeight, null);
				} else {
					g2.drawImage(ssImage, insets.left, insets.top, null);
				}
				g2.dispose();
				if (syncDisplay) {
					Toolkit.getDefaultToolkit().sync();
				}
			}

			ssflag = false;
		} else if (bufferStrategy != null && !bufferStrategy.contentsLost()) {
			bufferStrategy.show();
			if (syncDisplay) {
				Toolkit.getDefaultToolkit().sync();
			}
		}
	}

	/**
	 * Rendering(For net play)
	 */
	protected void gameRenderNet() {
		GameManager gameManager = owner.getGameManager();
		if (gameManager == null) {
			return;
		}

		// Prepare the screen
		if (ssImage == null) {
			ssImage = createImage(640, 480);
		}
		if (bufferStrategy == null || bufferStrategy.contentsLost()) {
			try {
				createBufferStrategy(2);
				bufferStrategy = getBufferStrategy();
			} catch (Exception e) {
				return;
			}
		}

		Graphics2D g = null;
		if (ssflag || screenWidth != 640 || screenHeight != 480) {
			g = (Graphics2D) ssImage.getGraphics();
		} else {
			g = (Graphics2D) bufferStrategy.getDrawGraphics();
			if (insets != null) {
				g.translate(insets.left, insets.top);
			}
		}

		// Game screen
		try {
			NormalFontSwing.graphics = g;
			((RendererSwing) gameManager.renderer).setGraphics(g);
			gameManager.renderAll();
		} catch (Exception e) {
			log.error("render fail", e);
		}

		// FPSDisplay
		if (showfps) {
			NormalFontSwing.printFont(0, 480 - 16, df.format(actualFPS) + "/" + maxfpsCurrent, Colors.FONT_BLUE, 1.0f);
		}

		// Displayed on the screen /ScreenshotCreating
		g.dispose();
		if (ssflag || screenWidth != 640 || screenHeight != 480) {
			if (ssflag) {
				saveScreenShot();
			}

			if (insets != null) {
				Graphics g2 = getGraphics();
				if (screenWidth != 640 || screenHeight != 480) {
					g2.drawImage(ssImage, insets.left, insets.top, screenWidth, screenHeight, null);
				} else {
					g2.drawImage(ssImage, insets.left, insets.top, null);
				}
				g2.dispose();
				if (syncDisplay) {
					Toolkit.getDefaultToolkit().sync();
				}
			}

			ssflag = false;
		} else if (bufferStrategy != null && !bufferStrategy.contentsLost()) {
			bufferStrategy.show();
			if (syncDisplay) {
				Toolkit.getDefaultToolkit().sync();
			}
		}
	}

	/**
	 * Calculation of FPS
	 *
	 * @param period to calculate the FPSInterval
	 */
	protected void calcFPS(long period) {
		frameCount++;
		calcInterval += period;

		// 1Second intervalsFPSRecalculate the
		if (calcInterval >= ONE_SECOND_IN_NS) {
			long timeNow = System.nanoTime();

			// Actual elapsed timeMeasure
			long realElapsedTime = timeNow - prevCalcTime; // Unit: ns

			// FPSCalculate the
			// realElapsedTimeThe unit ofnsSosConverted to
			actualFPS = (double) frameCount / realElapsedTime * ONE_SECOND_IN_NS;

			frameCount = 0L;
			calcInterval = 0L;
			prevCalcTime = timeNow;

			// Set new target fps
			if (maxfps > 0 && !perfectFPSMode) {
				if (actualFPS < maxfps - 1) {
					// Too slow
					maxfpsCurrent++;
					if (maxfpsCurrent > maxfps + 20) {
						maxfpsCurrent = maxfps + 20;
					}
					periodCurrent = (long) (1.0 / maxfpsCurrent * ONE_SECOND_IN_NS);
				} else if (actualFPS > maxfps + 1) {
					// Too fast
					maxfpsCurrent--;
					if (maxfpsCurrent < maxfps - 0) {
						maxfpsCurrent = maxfps - 0;
					}
					if (maxfpsCurrent < 0) {
						maxfpsCurrent = 0;
					}
					periodCurrent = (long) (1.0 / maxfpsCurrent * ONE_SECOND_IN_NS);
				}
			}
		}
	}

	/**
	 * Save a screen shot
	 */
	protected void saveScreenShot() {
		// Create filename
		String dir = NullpoMinoSwing.propGlobal.getProperty("custom.screenshot.directory", "ss");
		Calendar c = Calendar.getInstance();
		DateFormat dfm = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String filename = dir + "/" + dfm.format(c.getTime()) + ".png";
		log.info("Saving screenshot to " + filename);

		// Create ss folder if not exist
		File ssfolder = new File(dir);
		if (!ssfolder.exists()) {
			if (ssfolder.mkdir()) {
				log.info("Created screenshot folder: " + dir);
			} else {
				log.info("Couldn't create screenshot folder at " + dir);
			}
		}
		// Write
		try {
			ImageIO.write((RenderedImage) ssImage, "PNG", new File(filename));
		} catch (Exception e) {
			log.warn("Failed to save screenshot to " + filename, e);
		}
	}

	/**
	 * Update title bar text
	 */
	public void updateTitleBarCaption() {
		String strModeName = null;
		GameManager gameManager = owner.getGameManager();
		if (gameManager != null && gameManager.mode != null) {
			strModeName = gameManager.mode.getName();
		}

		String baseTitle = "NullpoMino - " + strModeName;
		if (isNetPlay) {
			baseTitle = "NullpoMino NetPlay - " + strModeName;
		}

		String title = baseTitle;

		if (isNetPlay && "NET-DUMMY".equals(strModeName)) {
			title = "NullpoMino NetPlay";
		} else if (gameManager != null && gameManager.engines != null && gameManager.engines.length > 0
				&& gameManager.engines[0] != null) {
			if (pause && !enableframestep) {
				title = "[PAUSE] " + baseTitle;
			} else if (gameManager.engines[0].isInGame && !gameManager.replayMode && !gameManager.replayRerecord) {
				title = "[PLAY] " + baseTitle;
			} else if (gameManager.replayMode && gameManager.replayRerecord) {
				title = "[RERECORD] " + baseTitle;
			} else if (gameManager.replayMode && !gameManager.replayRerecord) {
				title = "[REPLAY] " + baseTitle;
			} else {
				title = "[MENU] " + baseTitle;
			}
		}
		setTitle(title);
	}

	/**
	 * Window event Processing
	 */
	protected class GameFrameWindowEvent extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			shutdown();
		}
	}

	/**
	 * Keyboard event Processing
	 */
	protected class GameFrameKeyEvent extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			setButtonPressedState(e.getKeyCode(), true);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			setButtonPressedState(e.getKeyCode(), false);
		}

		protected void setButtonPressedState(int keyCode, boolean pressed) {
			for (int playerID = 0; playerID < GameKeySwing.gamekey.length; playerID++) {
				int[] kmap = isInGame[playerID] ? GameKeySwing.gamekey[playerID].keymap
						: GameKeySwing.gamekey[playerID].keymapNav;

				for (int i = 0; i < GameKeyDummy.MAX_BUTTON; i++) {
					if (keyCode == kmap[i]) {
						// log.debug("KeyCode:" + keyCode + " pressed:" + pressed + " button:" + i);
						GameKeySwing.gamekey[playerID].setPressState(i, pressed);
					}
				}
			}
		}
	}
}

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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import mu.nu.nullpo.game.GameEngine;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.types.DisplaySize;
import mu.nu.nullpo.gui.EffectObject;
import mu.nu.nullpo.gui.common.AbstractRenderer;
import mu.nu.nullpo.util.Colors;

/**
 * Game event Processing and rendering process (SwingVersion)
 */
public class RendererSwing extends AbstractRenderer<Graphics2D> {

	/** Effect objects */
	protected List<EffectObject> effects;

	/** Line clear effect enabled flag */
	protected boolean showlineeffect;

	/** fieldOfBlockShow (falseBorder appears only if) */
	protected boolean showfieldblockgraphics;

	/** OperationBlockTo simplify the design of */
	protected boolean simpleblock;

	/** Show field BG grid */
	protected boolean showfieldbggrid;

	/** Dark piece preview area */
	protected boolean darknextarea;

	/** NEXT display on top of the ghost piece */
	protected boolean nextShadow;

	/** Line clear effect speed */
	protected int lineEffectSpeed;

	private final ResourceHolderSwing resourceManager;

	/**
	 * Constructor
	 */
	public RendererSwing(Graphics2D graphics) {
		super(graphics);
		resourceManager = ResourceHolderSwing.getInstance();

		effects = new ArrayList<>(10 * 4);
		showbg = NullpoMinoSwing.propConfig.getProperty("option.showbg", true);
		showlineeffect = NullpoMinoSwing.propConfig.getProperty("option.showlineeffect", false);
		showMeter = NullpoMinoSwing.propConfig.getProperty("option.showmeter", true);
		showfieldblockgraphics = NullpoMinoSwing.propConfig.getProperty("option.showfieldblockgraphics", true);
		simpleblock = NullpoMinoSwing.propConfig.getProperty("option.simpleblock", false);
		showfieldbggrid = NullpoMinoSwing.propConfig.getProperty("option.showfieldbggrid", true);
		darknextarea = NullpoMinoSwing.propConfig.getProperty("option.darknextarea", true);
		nextShadow = NullpoMinoSwing.propConfig.getProperty("option.nextshadow", false);
		lineEffectSpeed = NullpoMinoSwing.propConfig.getProperty("option.lineeffectspeed", 0);
		outlineGhost = NullpoMinoSwing.propConfig.getProperty("option.outlineghost", false);
		sidenext = NullpoMinoSwing.propConfig.getProperty("option.sidenext", false);
		bigsidenext = NullpoMinoSwing.propConfig.getProperty("option.bigsidenext", false);
	}

	/*
	 * Sound effectsPlayback
	 */
	@Override
	public void playSE(String name) {
		resourceManager.getSoundManager().play(name);
	}

	/*
	 * Menu Drawing a string for
	 */
	@Override
	public void drawMenuFont(GameEngine engine, int playerID, int x, int y, String str, int color, float scale) {
		int x2 = scale == 0.5f ? x * 8 : x * 16;
		int y2 = scale == 0.5f ? y * 8 : y * 16;
		if (!engine.owner.menuOnly) {
			x2 += getFieldDisplayPositionX(engine, playerID) + 4;
			if (engine.displaySize == DisplaySize.SMALL) {
				y2 += getFieldDisplayPositionY(engine, playerID) + 4;
			} else {
				y2 += getFieldDisplayPositionY(engine, playerID) + 52;
			}
		}
		NormalFontSwing.printFont(x2, y2, str, color, scale);
	}

	/*
	 * Render scoreFor font Draw a
	 */
	@Override
	public void drawScoreFont(GameEngine engine, int playerID, int x, int y, String str, int color, float scale) {
		if (engine.owner.menuOnly) {
			return;
		}
		int size = scale == 0.5f ? 8 : 16;
		int x2 = getScoreDisplayPositionX(engine, playerID) + x * size;
		int y2 = getScoreDisplayPositionY(engine, playerID) + y * size;
		NormalFontSwing.printFont(x2, y2, str, color, scale);
	}

	/*
	 * I can draw directly to the specified coordinatesTTF font Draw a
	 */
	@Override
	public void drawTTFDirectFont(GameEngine engine, int playerID, int x, int y, String str, int color) {
		var font = resourceManager.getTtfFont();
		if (font != null) {
			graphics.setFont(font);
		}
		graphics.setColor(SwingColors.getFontColor(color));
		graphics.drawString(str, x, y + 4);
		graphics.setColor(Color.white);
	}

	/*
	 * Render scoreFor font ATTF font Drawing on
	 */
	@Override
	public void drawTTFScoreFont(GameEngine engine, int playerID, int x, int y, String str, int color) {
		if (engine.owner.menuOnly) {
			return;
		}
		int x2 = getScoreDisplayPositionX(engine, playerID) + x * 16;
		int y2 = getScoreDisplayPositionY(engine, playerID) + y * 16;
		graphics.setColor(SwingColors.getFontColor(color));
		var font = resourceManager.getTtfFont();
		if (font != null) {
			graphics.setFont(font);
		}
		graphics.drawString(str, x2, y2 + 4);
		graphics.setColor(Color.white);
	}

	/*
	 * Menu A string forTTF font Drawing on
	 */
	@Override
	public void drawTTFMenuFont(GameEngine engine, int playerID, int x, int y, String str, int color) {
		int x2 = x * 16;
		int y2 = y * 16 + 12;
		if (!engine.owner.menuOnly) {
			x2 += getFieldDisplayPositionX(engine, playerID) + 4;
			if (engine.displaySize == DisplaySize.SMALL) {
				y2 += getFieldDisplayPositionY(engine, playerID) + 4;
			} else {
				y2 += getFieldDisplayPositionY(engine, playerID) + 52;
			}
		}
		var font = resourceManager.getTtfFont();
		if (font != null) {
			graphics.setFont(font);
		}
		graphics.setColor(SwingColors.getFontColor(color));
		graphics.drawString(str, x2, y2 + 4);
		graphics.setColor(Color.white);
	}

	/*
	 * Draws the string to the specified coordinates I direct
	 */
	@Override
	public void drawDirectFont(GameEngine engine, int playerID, int x, int y, String str, int color, float scale) {
		NormalFontSwing.printFont(x, y, str, color, scale);
	}

	/*
	 * SpeedMeterDraw a
	 */
	@Override
	public void drawSpeedMeter(GameEngine engine, int playerID, int x, int y, int speed) {
		if (graphics == null || engine.owner.menuOnly) {
			return;
		}
		int dx1 = getScoreDisplayPositionX(engine, playerID) + 6 + x * 16;
		int dy1 = getScoreDisplayPositionY(engine, playerID) + 6 + y * 16;

		graphics.setColor(Color.black);
		graphics.drawRect(dx1, dy1, 41, 3);
		graphics.setColor(Color.green);
		graphics.fillRect(dx1 + 1, dy1 + 1, 40, 2);

		int tempSpeedMeter = speed;
		if (tempSpeedMeter < 0 || tempSpeedMeter > 40) {
			tempSpeedMeter = 40;
		}
		if (tempSpeedMeter > 0) {
			graphics.setColor(Color.red);
			graphics.fillRect(dx1 + 1, dy1 + 1, tempSpeedMeter + 1, 3);
		}
		graphics.setColor(Color.white);
	}

	/*
	 * TTFAvailable
	 */
	@Override
	public boolean isTTFSupport() {
		return resourceManager.getTtfFont() != null;
	}

	/*
	 * Get key name by button ID
	 */
	@Override
	public String getKeyNameByButtonID(GameEngine engine, int btnID) {
		int[] keymap = engine.isInGame ? GameKeySwing.gamekey[engine.playerID].keymap
				: GameKeySwing.gamekey[engine.playerID].keymapNav;
		if (btnID >= 0 && btnID < keymap.length) {
			int keycode = keymap[btnID];
			return KeyEvent.getKeyText(keycode);
		}
		return "";
	}

	/*
	 * Is the skin sticky?
	 */
	@Override
	public boolean isStickySkin(int skin) {
		if (skin >= 0 && skin < resourceManager.getBlockStickyFlags().size()) {
			return resourceManager.getBlockStickyFlags().get(skin);
		}
		return false;
	}

	/**
	 * Draw a block
	 *
	 * @param x        X pos
	 * @param y        Y pos
	 * @param color    Color
	 * @param skin     Skin
	 * @param bone     true to use bone block ([][][][])
	 * @param darkness Darkness or brightness
	 * @param alpha    Alpha
	 * @param scale    Size (0.5f, 1.0f, 2.0f)
	 * @param attr     Attribute
	 */
	protected void drawBlock(int x, int y, int color, int skin, boolean bone, float darkness, float alpha, float scale,
			int attr) {
		if (graphics == null || color <= Colors.BLOCK_COLOR_INVALID) {
			return;
		}
		if (skin >= resourceManager.getNormalBlockImages().size()) {
			skin = 0;
		}

		boolean isSpecialBlocks = color >= Colors.BLOCK_COLOR_COUNT;
		boolean isSticky = resourceManager.getBlockStickyFlags().get(skin);

		int size = (int) (16 * scale);
		Image img = null;
		if (scale == 0.5f) {
			img = resourceManager.getSmallBlockImages().get(skin);
		} else if (scale == 2.0f) {
			img = resourceManager.getBigBlockImages().get(skin);
		} else {
			img = resourceManager.getNormalBlockImages().get(skin);
		}

		int sx = color * size;
		if (bone) {
			sx += 9 * size;
		}
		int sy = 0;
		if (isSpecialBlocks) {
			sx = (color - Colors.BLOCK_COLOR_COUNT + 18) * size;
		}

		if (isSticky) {
			if (isSpecialBlocks) {
				sx = (color - Colors.BLOCK_COLOR_COUNT) * size;
				sy = 18 * size;
			} else {
				sx = 0;
				if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0) {
					sx |= 0x1;
				}
				if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0) {
					sx |= 0x2;
				}
				if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0) {
					sx |= 0x4;
				}
				if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0) {
					sx |= 0x8;
				}
				sx *= size;
				sy = color * size;
				if (bone) {
					sy += 9 * size;
				}
			}
		}

		int imageWidth = img.getWidth(null);
		if (sx >= imageWidth && imageWidth != -1) {
			sx = 0;
		}
		int imageHeight = img.getHeight(null);
		if (sy >= imageHeight && imageHeight != -1) {
			sy = 0;
		}

		Composite backupComposite = graphics.getComposite();

		if (alpha >= 0f && alpha < 1f && !showbg) {
			AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
			graphics.setComposite(composite);
		}

		if (simpleblock) {
			graphics.setColor(SwingColors.getSimpleBlockColor(color));
			graphics.drawRect(x, y, size - 1, size - 1);
			if (showbg) {
				graphics.setColor(Color.black);
				graphics.fillRect(x + 1, y + 1, size - 2, size - 2);
			}
		} else {
			graphics.drawImage(img, x, y, x + size, y + size, sx, sy, sx + size, sy + size, null);

			if (isSticky && !isSpecialBlocks) {
				int d = 16 * size;
				int h = size / 2;

				if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0
						&& (attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0) {
					graphics.drawImage(img, x, y, x + h, y + h, d, sy, d + h, sy + h, null);
				}
				if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_UP) != 0
						&& (attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0) {
					graphics.drawImage(img, x + h, y, x + h + h, y + h, d + h, sy, d + h + h, sy + h, null);
				}
				if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0
						&& (attr & Block.BLOCK_ATTRIBUTE_CONNECT_LEFT) != 0) {
					graphics.drawImage(img, x, y + h, x + h, y + h + h, d, sy + h, d + h, sy + h + h, null);
				}
				if ((attr & Block.BLOCK_ATTRIBUTE_CONNECT_DOWN) != 0
						&& (attr & Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT) != 0) {
					graphics.drawImage(img, x + h, y + h, x + h + h, y + h + h, d + h, sy + h, d + h + h, sy + h + h,
							null);
				}
			}
		}

		graphics.setComposite(backupComposite);

		if (darkness != 0 || alpha >= 0f && alpha < 1f && showbg) {
			Color backupColor = graphics.getColor();

			Color filterColor;
			if (alpha >= 0f && alpha < 1f && showbg) {
				filterColor = new Color(0f, 0f, 0f, alpha);
			} else if (darkness > 0) {
				filterColor = new Color(0f, 0f, 0f, darkness);
			} else {
				filterColor = new Color(1f, 1f, 1f, -darkness);
			}

			graphics.setColor(filterColor);
			graphics.fillRect(x, y, size, size);
			graphics.setColor(backupColor);
		}
	}

	/**
	 * BlockDraw a
	 *
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param color    Color
	 * @param skin     Pattern
	 * @param bone     BoneBlock
	 * @param darkness Lightness or darkness
	 * @param alpha    Transparency
	 * @param scale    Enlargement factor
	 */
	protected void drawBlock(int x, int y, int color, int skin, boolean bone, float darkness, float alpha,
			float scale) {
		drawBlock(x, y, color, skin, bone, darkness, alpha, scale, 0);
	}

	/**
	 * draws the block at given coordinates
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param block to draw
	 */
	protected void drawBlock(int x, int y, Block block) {
		drawBlock(x, y, block.getDrawColor(), block.skin, block.isBone(), block.darkness, block.alpha, 1.0f,
				block.attribute);
	}

	/**
	 * BlockUsing an instance of the classBlockDraw a (You can specify the
	 * magnification)
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param block BlockInstance of a class
	 * @param scale Enlargement factor
	 */
	protected void drawBlock(int x, int y, Block block, float scale) {
		drawBlock(x, y, block.getDrawColor(), block.skin, block.isBone(), block.darkness, block.alpha, scale,
				block.attribute);
	}

	/**
	 * BlockUsing an instance of the classBlockDraw a (You can specify the
	 * magnification and dark)
	 *
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param block    BlockInstance of a class
	 * @param scale    Enlargement factor
	 * @param darkness Lightness or darkness
	 */
	protected void drawBlock(int x, int y, Block block, float scale, float darkness) {
		drawBlock(x, y, block.getDrawColor(), block.skin, block.isBone(), darkness, block.alpha, scale,
				block.attribute);
	}

	protected void drawBlockForceVisible(int x, int y, Block block, float scale) {
		drawBlock(x, y, block.getDrawColor(), block.skin, block.isBone(), block.darkness, 0.5f * block.alpha + 0.5f,
				scale, block.attribute);
	}

	/**
	 * BlockDraw a piece
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param piece Peace to draw
	 */
	protected void drawPiece(int x, int y, Piece piece) {
		drawPiece(x, y, piece, 1.0f);
	}

	/**
	 * BlockDraw a piece (You can specify the magnification)
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param piece Peace to draw
	 * @param scale Enlargement factor
	 */
	protected void drawPiece(int x, int y, Piece piece, float scale) {
		drawPiece(x, y, piece, scale, 0f);
	}

	/**
	 * BlockDraw a piece (You can specify the brightness or darkness)
	 *
	 * @param x        X-coordinate
	 * @param y        Y-coordinate
	 * @param piece    Peace to draw
	 * @param scale    Enlargement factor
	 * @param darkness Lightness or darkness
	 */
	protected void drawPiece(int x, int y, Piece piece, float scale, float darkness) {
		for (int i = 0; i < piece.getMaxBlock(); i++) {
			int x2 = x + (int) (piece.dataX[piece.direction][i] * 16 * scale);
			int y2 = y + (int) (piece.dataY[piece.direction][i] * 16 * scale);

			Block block = new Block(piece.block[i]);
			block.darkness = darkness;

			drawBlock(x2, y2, block, scale);
		}
	}

	/**
	 * Currently working onBlockDraw a piece (Y-coordinateThe0MoreBlockDisplay only)
	 *
	 * @param x      X-coordinate
	 * @param y      Y-coordinate
	 * @param engine GameEngineInstance of
	 * @param scale  Display magnification
	 */
	protected void drawCurrentPiece(int x, int y, GameEngine engine, float scale) {
		Piece piece = engine.nowPieceObject;
		if (piece == null) {
			return;
		}
		int blockSize = (int) (16 * scale);
		for (int i = 0; i < piece.getMaxBlock(); i++) {
			if (!piece.big) {
				int x2 = engine.nowPieceX + piece.dataX[piece.direction][i];
				int y2 = engine.nowPieceY + piece.dataY[piece.direction][i];

				if (y2 >= 0) {
					Block blkTemp = piece.block[i];
					if (engine.nowPieceColorOverride >= 0) {
						blkTemp = new Block(piece.block[i]);
						blkTemp.color = engine.nowPieceColorOverride;
					}
					drawBlock(x + x2 * blockSize, y + y2 * blockSize, blkTemp, scale);
				}
			} else {
				int x2 = engine.nowPieceX + piece.dataX[piece.direction][i] * 2;
				int y2 = engine.nowPieceY + piece.dataY[piece.direction][i] * 2;

				Block blkTemp = piece.block[i];
				if (engine.nowPieceColorOverride >= 0) {
					blkTemp = new Block(piece.block[i]);
					blkTemp.color = engine.nowPieceColorOverride;
				}
				drawBlock(x + x2 * blockSize, y + y2 * blockSize, blkTemp, scale * 2.0f);
			}
		}
	}

	/**
	 * Currently working onBlockOf Peaceghost Draw a
	 *
	 * @param x      X-coordinate
	 * @param y      Y-coordinate
	 * @param engine GameEngineInstance of
	 * @param scale  Display magnification
	 */
	protected void drawGhostPiece(int x, int y, GameEngine engine, float scale) {
		Piece piece = engine.nowPieceObject;
		if (piece == null) {
			return;
		}
		int blksize = (int) (16 * scale);

		for (int i = 0; i < piece.getMaxBlock(); i++) {
			if (!piece.big) {
				int x2 = engine.nowPieceX + piece.dataX[piece.direction][i];
				int y2 = engine.nowPieceBottomY + piece.dataY[piece.direction][i];

				if (y2 >= 0) {
					if (outlineGhost) {
						Block block = piece.block[i];
						int x3 = x + x2 * blksize;
						int y3 = y + y2 * blksize;
						int ls = blksize - 1;

						int colorID = block.getDrawColor();
						if (block.isBone()) {
							colorID = -1;
						}
						Color color = SwingColors.getBlockColor(colorID);
						graphics.setColor(color);
						graphics.fillRect(x3, y3, blksize, blksize);
						graphics.setColor(Color.white);

						if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
							graphics.drawLine(x3, y3, x3 + ls, y3);
							graphics.drawLine(x3, y3 + 1, x3 + ls, y3 + 1);
						}
						if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
							graphics.drawLine(x3, y3 + ls, x3 + ls, y3 + ls);
							graphics.drawLine(x3, y3 - 1 + ls, x3 + ls, y3 - 1 + ls);
						}
						if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) {
							graphics.drawLine(x3, y3, x3, y3 + ls);
							graphics.drawLine(x3 + 1, y3, x3 + 1, y3 + ls);
						}
						if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) {
							graphics.drawLine(x3 + ls, y3, x3 + ls, y3 + ls);
							graphics.drawLine(x3 - 1 + ls, y3, x3 - 1 + ls, y3 + ls);
						}
						if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT | Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
							graphics.fillRect(x3, y3, 2, 2);
						}
						if (block.getAttribute(
								Block.BLOCK_ATTRIBUTE_CONNECT_LEFT | Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
							graphics.fillRect(x3, y3 + blksize - 2, 2, 2);
						}
						if (block
								.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT | Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
							graphics.fillRect(x3 + blksize - 2, y3, 2, 2);
						}
						if (block.getAttribute(
								Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT | Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
							graphics.fillRect(x3 + blksize - 2, y3 + blksize - 2, 2, 2);
						}
					} else {
						Block block = new Block(piece.block[i]);
						block.darkness = 0.3f;
						if (engine.nowPieceColorOverride >= 0) {
							block.color = engine.nowPieceColorOverride;
						}
						drawBlock(x + x2 * blksize, y + y2 * blksize, block, scale);
					}
				}
			} else {
				int x2 = engine.nowPieceX + piece.dataX[piece.direction][i] * 2;
				int y2 = engine.nowPieceBottomY + piece.dataY[piece.direction][i] * 2;

				if (outlineGhost) {
					Block block = piece.block[i];
					int x3 = x + x2 * blksize;
					int y3 = y + y2 * blksize;
					int ls = blksize * 2 - 1;

					int colorID = block.getDrawColor();
					if (block.isBone()) {
						colorID = -1;
					}
					Color color = SwingColors.getBlockColor(colorID);
					graphics.setColor(color);
					graphics.fillRect(x3, y3, blksize * 2, blksize * 2);
					graphics.setColor(Color.white);

					if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
						graphics.drawLine(x3, y3, x3 + ls, y3);
						graphics.drawLine(x3, y3 + 1, x3 + ls, y3 + 1);
					}
					if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
						graphics.drawLine(x3, y3 + ls, x3 + ls, y3 + ls);
						graphics.drawLine(x3, y3 - 1 + ls, x3 + ls, y3 - 1 + ls);
					}
					if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) {
						graphics.drawLine(x3, y3, x3, y3 + ls);
						graphics.drawLine(x3 + 1, y3, x3 + 1, y3 + ls);
					}
					if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) {
						graphics.drawLine(x3 + ls, y3, x3 + ls, y3 + ls);
						graphics.drawLine(x3 - 1 + ls, y3, x3 - 1 + ls, y3 + ls);
					}
					if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT | Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
						graphics.fillRect(x3, y3, 2, 2);
					}
					if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT | Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
						graphics.fillRect(x3, y3 + blksize * 2 - 2, 2, 2);
					}
					if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT | Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
						graphics.fillRect(x3 + blksize * 2 - 2, y3, 2, 2);
					}
					if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT | Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
						graphics.fillRect(x3 + blksize * 2 - 2, y3 + blksize * 2 - 2, 2, 2);
					}
				} else {
					Block block = new Block(piece.block[i]);
					block.darkness = 0.3f;
					if (engine.nowPieceColorOverride >= 0) {
						block.color = engine.nowPieceColorOverride;
					}
					drawBlock(x + x2 * blksize, y + y2 * blksize, block, scale * 2.0f);
				}
			}
		}
	}

	protected void drawHintPiece(int x, int y, GameEngine engine, float scale) {
		Piece piece = engine.ai != null ? engine.ai.getHintPiece() : null;
		if (piece == null) {
			return;
		}
		piece.direction = engine.ai.bestRt;
		piece.updateConnectData();
		int blksize = (int) (16 * scale);
		if (!piece.big) {
			drawHintPieceNormal(x, y, engine, piece, blksize);
		} else {
			drawHintPieceBig(x, y, engine, piece, blksize);
		}
	}

	protected void drawHintPieceNormal(int x, int y, GameEngine engine, Piece piece, int blksize) {
		for (int i = 0; i < piece.getMaxBlock(); i++) {
			int x2 = engine.ai.bestX + piece.dataX[piece.direction][i];
			int y2 = engine.ai.bestY + piece.dataY[piece.direction][i];
			if (y2 < 0) {
				continue;
			}
			Block block = piece.block[i];
			int x3 = x + x2 * blksize;
			int y3 = y + y2 * blksize;
			int ls = blksize - 1;

			int colorID = block.getDrawColor();
			if (block.isBone()) {
				colorID = -1;
			}
			Color color = SwingColors.getBlockColorBright(colorID);
			graphics.setColor(color);

			if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
				graphics.fillRect(x3, y3, ls, 2);
			}
			if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
				graphics.fillRect(x3, y3 + ls - 1, ls, 2);
			}
			if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) {
				graphics.fillRect(x3, y3, 2, ls);
			}
			if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) {
				graphics.fillRect(x3 + ls - 1, y3, 2, ls);
			}
			if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT | Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
				graphics.fillRect(x3, y3, 2, 2);
			}
			if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT | Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
				graphics.fillRect(x3, y3 + blksize - 2, 2, 2);
			}
			if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT | Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
				graphics.fillRect(x3 + blksize - 2, y3, 2, 2);
			}
			if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT | Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
				graphics.fillRect(x3 + blksize - 2, y3 + blksize - 2, 2, 2);
			}
		}
	}

	protected void drawHintPieceBig(int x, int y, GameEngine engine, Piece piece, int blksize) {
		for (int i = 0; i < piece.getMaxBlock(); i++) {
			int x2 = engine.ai.bestX + piece.dataX[piece.direction][i] * 2;
			int y2 = engine.ai.bestY + piece.dataY[piece.direction][i] * 2;

			Block blkTemp = piece.block[i];
			int x3 = x + x2 * blksize;
			int y3 = y + y2 * blksize;
			int ls = blksize * 2 - 1;

			int colorID = blkTemp.getDrawColor();
			if (blkTemp.isBone()) {
				colorID = -1;
			}
			Color color = SwingColors.getBlockColor(colorID);
			graphics.setColor(color);

			if (!blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
				graphics.drawLine(x3, y3, x3 + ls, y3);
				graphics.drawLine(x3, y3 + 1, x3 + ls, y3 + 1);
			}
			if (!blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
				graphics.drawLine(x3, y3 + ls, x3 + ls, y3 + ls);
				graphics.drawLine(x3, y3 - 1 + ls, x3 + ls, y3 - 1 + ls);
			}
			if (!blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) {
				graphics.drawLine(x3, y3, x3, y3 + ls);
				graphics.drawLine(x3 + 1, y3, x3 + 1, y3 + ls);
			}
			if (!blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) {
				graphics.drawLine(x3 + ls, y3, x3 + ls, y3 + ls);
				graphics.drawLine(x3 - 1 + ls, y3, x3 - 1 + ls, y3 + ls);
			}
			if (blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT | Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
				graphics.fillRect(x3, y3, 2, 2);
			}
			if (blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT | Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
				graphics.fillRect(x3, y3 + blksize * 2 - 2, 2, 2);
			}
			if (blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT | Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
				graphics.fillRect(x3 + blksize * 2 - 2, y3, 2, 2);
			}
			if (blkTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT | Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
				graphics.fillRect(x3 + blksize * 2 - 2, y3 + blksize * 2 - 2, 2, 2);
			}
		}
	}

	static final Stroke DOUBLE_WITDH = new BasicStroke(2f);
	static final Color OUTLINE_COLOR = new Color(232, 232, 232);

	/**
	 * fieldOfBlockDraw a
	 *
	 * @param x      X-coordinate
	 * @param y      Y-coordinate
	 * @param engine GameEngineInstance of
	 */
	protected void drawField(int x, int y, GameEngine engine) {
		if (graphics == null) {
			return;
		}

		int blksize = engine.displaySize.getBlockSize();
		float scale = engine.displaySize.getScale();
		Field field = engine.field;

		int width = 10;
		int height = 20;
		int viewHeight = 20;

		if (field != null) {
			width = field.getWidth();
			viewHeight = height = field.getHeight();
		}
		if (engine.heboHiddenEnable && engine.gameActive && field != null) {
			viewHeight -= engine.heboHiddenYNow;
		}

		int outlineType = engine.blockOutlineType;
		if (engine.owBlockOutlineType != -1) {
			outlineType = engine.owBlockOutlineType;
		}

		for (int i = 0; i < viewHeight; i++) {
			for (int j = 0; j < width; j++) {
				int x2 = x + j * blksize;
				int y2 = y + i * blksize;

				Block block = null;
				if (field != null) {
					block = field.getBlock(j, i);
				}

				if (field != null && block != null && block.color > Colors.BLOCK_COLOR_NONE) {
					if (block.getAttribute(Block.BLOCK_ATTRIBUTE_WALL)) {
						drawBlock(x2, y2, Colors.BLOCK_COLOR_NONE, block.skin, block.isBone(), block.darkness,
								block.alpha, scale, block.attribute);
					} else if (showfieldblockgraphics && engine.owner.replayMode && engine.owner.replayShowInvisible) {
						drawBlockForceVisible(x2, y2, block, scale);
					} else if (showfieldblockgraphics && block.getAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE)) {
						drawBlock(x2, y2, block, scale);
					} else if (width > 10 && height > 20 || !showfieldbggrid) {
						int sx = i % 2 == 0 && j % 2 == 0 || i % 2 != 0 && j % 2 != 0 ? 0 : 16;
						graphics.drawImage(resourceManager.getImgFieldbg(), x2, y2, x2 + blksize, y2 + blksize, sx, 0,
								sx + 16, 16, null);
					}

					if (block.getAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE) && !block.isBone()) {
						graphics.setColor(OUTLINE_COLOR);
						Stroke oldStroke = graphics.getStroke();
						graphics.setStroke(DOUBLE_WITDH);
						int ls = blksize - 1;
						switch (outlineType) {
						case GameEngine.BLOCK_OUTLINE_NORMAL:
							if (field.getBlockColor(j, i - 1) == Colors.BLOCK_COLOR_NONE) {
								graphics.drawLine(x2, y2, x2 + ls, y2);
							}
							if (field.getBlockColor(j, i + 1) == Colors.BLOCK_COLOR_NONE) {
								graphics.drawLine(x2, y2 + ls, x2 + ls, y2 + ls);
							}
							if (field.getBlockColor(j - 1, i) == Colors.BLOCK_COLOR_NONE) {
								graphics.drawLine(x2, y2, x2, y2 + ls);
							}
							if (field.getBlockColor(j + 1, i) == Colors.BLOCK_COLOR_NONE) {
								graphics.drawLine(x2 + ls, y2, x2 + ls, y2 + ls);
							}
							break;
						case GameEngine.BLOCK_OUTLINE_CONNECT:
							if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
								graphics.drawLine(x2, y2, x2 + ls, y2);
							}
							if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
								graphics.drawLine(x2, y2 + ls, x2 + ls, y2 + ls);
							}
							if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) {
								graphics.drawLine(x2, y2, x2, y2 + ls);
							}
							if (!block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) {
								graphics.drawLine(x2 + ls, y2, x2 + ls, y2 + ls);
							}
							break;
						case GameEngine.BLOCK_OUTLINE_SAMECOLOR:
							if (field.getBlockColor(j, i - 1) != block.color) {
								graphics.drawLine(x2, y2, x2 + ls, y2);
							}
							if (field.getBlockColor(j, i + 1) != block.color) {
								graphics.drawLine(x2, y2 + ls, x2 + ls, y2 + ls);
							}
							if (field.getBlockColor(j - 1, i) != block.color) {
								graphics.drawLine(x2, y2, x2, y2 + ls);
							}
							if (field.getBlockColor(j + 1, i) != block.color) {
								graphics.drawLine(x2 + ls, y2, x2 + ls, y2 + ls);
							}
							break;
						default:
							break;
						}
						graphics.setStroke(oldStroke);
					}
				} else if (width > 10 && height > 20 || !showfieldbggrid) {
					int sx = i % 2 == 0 && j % 2 == 0 || i % 2 != 0 && j % 2 != 0 ? 0 : 16;
					graphics.drawImage(resourceManager.getImgFieldbg(), x2, y2, x2 + blksize, y2 + blksize, sx, 0,
							sx + 16, 16, null);
				}
			}
		}

		// BunglerHIDDEN
		if (engine.heboHiddenEnable && engine.gameActive && field != null) {
			int maxY = engine.heboHiddenYNow;
			if (maxY > height) {
				maxY = height;
			}
			for (int y2 = 0; y2 < maxY; y2++) {
				for (int x2 = 0; x2 < width; x2++) {
					drawBlock(x + x2 * blksize, y + (height - 1 - y2) * blksize, Colors.BLOCK_COLOR_GRAY, 0, false,
							0.0f, 1.0f, scale);
				}
			}
		}
	}

	/**
	 * Draw the Field frame
	 *
	 * @param x      X-coordinate
	 * @param y      Y-coordinate
	 * @param engine Instance of GameEngine
	 */
	protected void drawFrame(int x, int y, GameEngine engine) {
		if (graphics == null) {
			return;
		}

		int size = (int) (16 * engine.displaySize.getScale());

		int width = 10;
		int height = 20;
		int offsetX = 0;

		if (engine.field != null) {
			width = engine.field.getWidth();
			height = engine.field.getHeight();
		}
		offsetX = engine.framecolor * 16;

		// Field Background
		if (width <= 10 && height <= 20 && showfieldbggrid) {
			Image grid = switch (engine.displaySize) {
			case SMALL -> resourceManager.getImgFieldbg2Small();
			case NORMAL -> resourceManager.getImgFieldbg2();
			case BIG -> resourceManager.getImgFieldbg2Big();
			};
			graphics.drawImage(grid, x + 4, y + 4, x + 4 + width * size, y + 4 + height * size, 0, 0, width * size,
					height * size, null);
		}

		// UpAnd the lower
		int maxWidth = width * size;
		if (showMeter) {
			maxWidth = width * size + 2 * 4;
		}

		int tmpX = 0;
		int tmpY = 0;

		tmpX = x + 4;
		tmpY = y;

		Image frame = resourceManager.getImgFrame();

		graphics.drawImage(frame, tmpX, tmpY, tmpX + maxWidth, tmpY + 4, offsetX + 4, 0, offsetX + 4 + 4, 4, null);
		tmpY = y + height * size + 4;
		graphics.drawImage(frame, tmpX, tmpY, tmpX + maxWidth, tmpY + 4, offsetX + 4, 8, offsetX + 4 + 4, 8 + 4, null);

		// Left and Right
		tmpX = x;
		tmpY = y + 4;
		graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + height * size, offsetX, 4, offsetX + 4, 4 + 4, null);

		if (showMeter) {
			tmpX = x + width * size + 12;
		} else {
			tmpX = x + width * size + 4;
		}
		graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + height * size, offsetX + 8, 4, offsetX + 8 + 4, 4 + 4,
				null);

		// Upper left
		tmpX = x;
		tmpY = y;
		graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + 4, offsetX, 0, offsetX + 4, 4, null);

		// Lower left
		tmpX = x;
		tmpY = y + height * size + 4;
		graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + 4, offsetX, 8, offsetX + 4, 8 + 4, null);

		if (showMeter) {
			// ON When the upper right corner of the Meter
			tmpX = x + width * size + 12;
			tmpY = y;
			graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + 4, offsetX + 8, 0, offsetX + 8 + 4, 4, null);

			// ON When the lower-right corner of Meter
			tmpX = x + width * size + 12;
			tmpY = y + height * size + 4;
			graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + 4, offsetX + 8, 8, offsetX + 8 + 4, 8 + 4, null);

			// RightMeterFrame
			tmpX = x + width * size + 4;
			tmpY = y + 4;
			graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + height * size, offsetX + 12, 4, offsetX + 12 + 4,
					4 + 4, null);

			tmpX = x + width * size + 4;
			tmpY = y;
			graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + 4, offsetX + 12, 0, offsetX + 12 + 4, 4, null);

			tmpX = x + width * size + 4;
			tmpY = y + height * size + 4;
			graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + 4, offsetX + 12, 8, offsetX + 12 + 4, 8 + 4, null);

			// RightMeter
			int maxHeight = height * size;
			if (engine.meterValueSub > 0 || engine.meterValue > 0) {
				maxHeight -= Math.max(engine.meterValue, engine.meterValueSub);
			}

			tmpX = x + width * size + 8;
			tmpY = y + 4;

			if (maxHeight > 0) {
				graphics.setColor(Color.black);
				graphics.fillRect(tmpX, tmpY, 4, maxHeight);
				graphics.setColor(Color.white);
			}

			if (engine.meterValueSub > Math.max(engine.meterValue, 0)) {
				int value = engine.meterValueSub;
				if (value > height * size) {
					value = height * size;
				}
				if (value > 0) {
					tmpX = x + width * size + 8;
					tmpY = y + height * size + 3 - (value - 1);

					graphics.setColor(SwingColors.getMeterColor(engine.meterColorSub));
					graphics.fillRect(tmpX, tmpY, 4, value);
					graphics.setColor(Color.white);
				}
			}
			if (engine.meterValue > 0) {
				int value = engine.meterValue;
				if (value > height * size) {
					value = height * size;
				}
				if (value > 0) {
					tmpX = x + width * size + 8;
					tmpY = y + height * size + 3 - (value - 1);

					graphics.setColor(SwingColors.getMeterColor(engine.meterColor));
					graphics.fillRect(tmpX, tmpY, 4, value);
					graphics.setColor(Color.white);
				}
			}
		} else {
			// MeterOFFWhen the upper right corner of the
			tmpX = x + width * size + 4;
			tmpY = y;
			graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + 4, offsetX + 8, 0, offsetX + 8 + 4, 4, null);

			// MeterOFFWhen the lower-right corner of
			tmpX = x + width * size + 4;
			tmpY = y + height * size + 4;
			graphics.drawImage(frame, tmpX, tmpY, tmpX + 4, tmpY + 4, offsetX + 8, 8, offsetX + 8 + 4, 8 + 4, null);
		}
	}

	/**
	 * NEXTDraw a
	 *
	 * @param x      X-coordinate
	 * @param y      Y-coordinate
	 * @param engine GameEngineInstance of
	 */
	protected void drawNext(int x, int y, GameEngine engine) {
		if (graphics == null) {
			return;
		}

		int fldWidth = 10;
		int fldBlkSize = 16;
		int meterWidth = showMeter ? 8 : 0;
		if (engine != null && engine.field != null) {
			fldWidth = engine.field.getWidth();
			if (engine.displaySize == DisplaySize.BIG) {
				fldBlkSize = engine.displaySize.getBlockSize();
			}
		}

		// NEXT area background
		if (showbg && darknextarea) {
			graphics.setColor(Color.black);

			if (getNextDisplayType() == 2) {
				int x2 = x + 8 + fldWidth * fldBlkSize + meterWidth;
				int maxNext = engine.isNextVisible ? engine.ruleopt.nextDisplay : 0;

				// HOLD area
				if (engine.ruleopt.holdEnable && engine.isHoldVisible) {
					graphics.fillRect(x - 64, y + 48, 64, 64);
				}
				// NEXT area
				if (maxNext > 0) {
					graphics.fillRect(x2, y + 48, 64, 64 * maxNext);
				}
			} else if (getNextDisplayType() == 1) {
				int x2 = x + 8 + fldWidth * fldBlkSize + meterWidth;
				int maxNext = engine.isNextVisible ? engine.ruleopt.nextDisplay : 0;

				// HOLD area
				if (engine.ruleopt.holdEnable && engine.isHoldVisible) {
					graphics.fillRect(x - 32, y + 48, 32, 32);
				}
				// NEXT area
				if (maxNext > 0) {
					graphics.fillRect(x2, y + 48, 32, 32 * maxNext);
				}
			} else {
				int w = fldWidth * fldBlkSize + 15;

				graphics.fillRect(x, y, w, 48);
			}

			graphics.setColor(Color.white);
		}

		if (engine.isNextVisible) {
			if (getNextDisplayType() == 2) {
				if (engine.ruleopt.nextDisplay >= 1) {
					int x2 = x + 8 + fldWidth * fldBlkSize + meterWidth;
					NormalFontSwing.printFont(x2 + 16, y + 40, NullpoMinoSwing.getUIText("InGame_Next"),
							Colors.FONT_ORANGE, 0.5f);

					for (int i = 0; i < engine.ruleopt.nextDisplay; i++) {
						Piece piece = engine.getNextObject(engine.nextPieceCount + i);

						if (piece != null) {
							int centerX = (64 - piece.getWidth() * 16) / 2 - piece.getMinimumBlockX() * 16;
							int centerY = (64 - piece.getHeight() * 16) / 2 - piece.getMinimumBlockY() * 16;
							drawPiece(x2 + centerX, y + 48 + i * 64 + centerY, piece, 1.0f);
						}
					}
				}
			} else if (getNextDisplayType() == 1) {
				if (engine.ruleopt.nextDisplay >= 1) {
					int x2 = x + 8 + fldWidth * fldBlkSize + meterWidth;
					NormalFontSwing.printFont(x2, y + 40, NullpoMinoSwing.getUIText("InGame_Next"), Colors.FONT_ORANGE,
							0.5f);

					for (int i = 0; i < engine.ruleopt.nextDisplay; i++) {
						Piece piece = engine.getNextObject(engine.nextPieceCount + i);

						if (piece != null) {
							int centerX = (32 - piece.getWidth() * 8) / 2 - piece.getMinimumBlockX() * 8;
							int centerY = (32 - piece.getHeight() * 8) / 2 - piece.getMinimumBlockY() * 8;
							drawPiece(x2 + centerX, y + 48 + i * 32 + centerY, piece, 0.5f);
						}
					}
				}
			} else {
				// NEXT1
				if (engine.ruleopt.nextDisplay >= 1) {
					NormalFontSwing.printFont(x + 60, y, NullpoMinoSwing.getUIText("InGame_Next"), Colors.FONT_ORANGE,
							0.5f);
					Piece piece = engine.getNextObject(engine.nextPieceCount);
					if (piece != null) {
						// int x2 = x + 4 + ((-1 + (engine.field.getWidth() - piece.getWidth() + 1) / 2)
						// * 16);
						int x2 = x + 4 + engine.getSpawnPosX(engine.field, piece) * fldBlkSize; // Rules with spawn x
																								// modified were
																								// misaligned.
						int y2 = y + 48 - (piece.getMaximumBlockY() + 1) * 16;
						drawPiece(x2, y2, piece);
					}
				}

				// NEXT2·3
				for (int i = 0; i < engine.ruleopt.nextDisplay - 1 && i < 2; i++) {
					Piece piece = engine.getNextObject(engine.nextPieceCount + i + 1);

					if (piece != null) {
						drawPiece(x + 124 + i * 40, y + 48 - (piece.getMaximumBlockY() + 1) * 8, piece, 0.5f);
					}
				}

				// NEXT4~
				for (int i = 0; i < engine.ruleopt.nextDisplay - 3; i++) {
					Piece piece = engine.getNextObject(engine.nextPieceCount + i + 3);
					if (piece == null) {
						continue;
					}
					if (showMeter) {
						drawPiece(x + 176, y + i * 40 + 88 - (piece.getMaximumBlockY() + 1) * 8, piece, 0.5f);
					} else {
						drawPiece(x + 168, y + i * 40 + 88 - (piece.getMaximumBlockY() + 1) * 8, piece, 0.5f);
					}
				}
			}
		}

		if (engine.isHoldVisible) {
			drawHold(x, y, engine);
		}
	}

	protected void drawHold(int x, int y, GameEngine engine) {
		// HOLD
		int holdRemain = engine.ruleopt.holdLimit - engine.holdUsedCount;
		int x2 = sidenext ? x - 32 : x;
		int y2 = sidenext ? y + 40 : y;
		if (getNextDisplayType() == 2) {
			x2 = x - 48;
		}

		if (engine.ruleopt.holdEnable && (engine.ruleopt.holdLimit < 0 || holdRemain > 0)) {
			int tempColor = Colors.FONT_GREEN;
			if (engine.holdDisable) {
				tempColor = Colors.FONT_WHITE;
			}

			if (engine.ruleopt.holdLimit < 0) {
				NormalFontSwing.printFont(x2, y2, NullpoMinoSwing.getUIText("InGame_Hold"), tempColor, 0.5f);
			} else {
				if (!engine.holdDisable) {
					if (holdRemain > 0 && holdRemain <= 10) {
						tempColor = Colors.FONT_YELLOW;
					}
					if (holdRemain > 0 && holdRemain <= 5) {
						tempColor = Colors.FONT_RED;
					}
				}

				NormalFontSwing.printFont(x2, y2, NullpoMinoSwing.getUIText("InGame_Hold") + "\ne " + holdRemain,
						tempColor, 0.5f);
			}

			if (engine.holdPieceObject != null) {
				float dark = 0f;
				if (engine.holdDisable) {
					dark = 0.3f;
				}
				Piece piece = new Piece(engine.holdPieceObject);
				piece.resetOffsetArray();

				if (getNextDisplayType() == 2) {
					int centerX = (64 - piece.getWidth() * 16) / 2 - piece.getMinimumBlockX() * 16;
					int centerY = (64 - piece.getHeight() * 16) / 2 - piece.getMinimumBlockY() * 16;
					drawPiece(x - 64 + centerX, y + 48 + centerY, piece, 1.0f, dark);
				} else if (getNextDisplayType() == 1) {
					int centerX = (32 - piece.getWidth() * 8) / 2 - piece.getMinimumBlockX() * 8;
					int centerY = (32 - piece.getHeight() * 8) / 2 - piece.getMinimumBlockY() * 8;
					drawPiece(x2 + centerX, y + 48 + centerY, piece, 0.5f, dark);
				} else {
					drawPiece(x2, y + 48 - (piece.getMaximumBlockY() + 1) * 8, piece, 0.5f, dark);
				}
			}
		}
	}

	/**
	 * Draw shadow nexts
	 *
	 * @param x      X coord
	 * @param y      Y coord
	 * @param engine GameEngine
	 * @param scale  Display size of piece
	 * @author Wojtek
	 */
	protected void drawShadowNexts(int x, int y, GameEngine engine, float scale) {
		Piece piece = engine.nowPieceObject;
		if (piece == null) {
			return;
		}
		int blksize = (int) (16 * scale);
		int shadowX = engine.nowPieceX;
		int shadowY = engine.nowPieceBottomY + piece.getMinimumBlockY();
		int maxShadows = Math.min(3, engine.ruleopt.nextDisplay);
		for (int i = 0; i <= maxShadows; i++) {
			Piece next = engine.getNextObject(engine.nextPieceCount + i);
			if (next == null) {
				continue;
			}
			int shadowCenter = blksize * piece.getMinimumBlockX() + blksize * piece.getWidth() / 2;
			int nextCenter = blksize / 2 * next.getMinimumBlockX() + blksize / 2 * next.getWidth() / 2;
			int vPos = blksize * shadowY - (i + 1) * 24 - 8;

			if (vPos >= -blksize / 2) {
				drawPiece(x + blksize * shadowX + shadowCenter - nextCenter, y + vPos, next, 0.5f * scale, 0.1f);
			}
		}
	}

	/**
	 * Each frame Drawing process of the first
	 *
	 * @param engine   GameEngine
	 * @param playerID Player ID
	 */
	@Override
	public void renderFirst(GameEngine engine, int playerID) {
		if (graphics == null) {
			return;
		}

		if (engine.playerID == 0) {
			// Background
			if (!showbg || engine.owner.menuOnly) {
				graphics.setColor(Color.black);
				graphics.fillRect(0, 0, 640, 480);
			} else {
				int bg = engine.owner.backgroundStatus.bg;
				if (engine.owner.backgroundStatus.fadesw) {
					bg = engine.owner.backgroundStatus.fadebg;
				}
				if (resourceManager.getImgPlayBG() != null && bg >= 0 && bg < ResourceHolderSwing.BACKGROUND_MAX) {
					graphics.drawImage(resourceManager.getImgPlayBG()[bg], 0, 0, null);
				}
			}
		}

		// NEXTなど
		if (engine.owner.menuOnly || !engine.isVisible) {
			return;
		}
		int offsetX = getFieldDisplayPositionX(engine, playerID);
		int offsetY = getFieldDisplayPositionY(engine, playerID);

		if (engine.displaySize != DisplaySize.SMALL) {
			drawNext(offsetX, offsetY, engine);
			drawFrame(offsetX, offsetY + 48, engine);
			drawField(offsetX + 4, offsetY + 52, engine);
		} else {
			drawFrame(offsetX, offsetY, engine);
			drawField(offsetX + 4, offsetY + 4, engine);
		}
	}

	/*
	 * ReadyProcess of drawing the screen
	 */
	@Override
	public void renderReady(GameEngine engine, int playerID) {
		int status = engine.statc_0(); // XXX clarify what status means
		if (graphics == null || !engine.allowTextRenderByReceiver || status <= 0) {
			return;
		}

		int offsetX = getFieldDisplayPositionX(engine, playerID);
		int offsetY = getFieldDisplayPositionY(engine, playerID);

		if (engine.displaySize != DisplaySize.SMALL) {
			if (status >= engine.readyStart && status < engine.readyEnd) {
				NormalFontSwing.printFont(offsetX + 44, offsetY + 204, "READY", Colors.FONT_WHITE, 1.0f);
			} else if (status >= engine.goStart && status < engine.goEnd) {
				NormalFontSwing.printFont(offsetX + 62, offsetY + 204, "GO!", Colors.FONT_WHITE, 1.0f);
			}
		} else if (status >= engine.readyStart && status < engine.readyEnd) {
			NormalFontSwing.printFont(offsetX + 24, offsetY + 80, "READY", Colors.FONT_WHITE, 0.5f);
		} else if (status >= engine.goStart && status < engine.goEnd) {
			NormalFontSwing.printFont(offsetX + 32, offsetY + 160, "GO!", Colors.FONT_WHITE, 0.5f);
		}
	}

	/*
	 * BlockHandling when moving piece
	 */
	@Override
	public void renderMove(GameEngine engine, int playerID) {
		if (!engine.isVisible || engine.statc_0() <= 1 && !engine.ruleopt.moveFirstFrame) {
			return;
		}

		var size = engine.displaySize;
		float scale = size.getScale();

		int offsetX = getFieldDisplayPositionX(engine, playerID) + 4;
		int offsetY = getFieldDisplayPositionY(engine, playerID) + (size == DisplaySize.SMALL ? 4 : 52);

		if (nextShadow && size != DisplaySize.SMALL) {
			drawShadowNexts(offsetX, offsetY, engine, scale);
		}
		if (engine.ghost && engine.ruleopt.ghost) {
			drawGhostPiece(offsetX, offsetY, engine, scale);
		}
		if (engine.ai != null && engine.ai.isShowHint() && engine.ai.isHintReady()) {
			drawHintPiece(offsetX, offsetY, engine, scale);
		}
		drawCurrentPiece(offsetX, offsetY, engine, scale);
	}

	/*
	 * Block break
	 */
	@Override
	public void blockBreak(GameEngine engine, int playerID, int x, int y, Block block) {
		if (!showlineeffect || block == null || engine.displaySize == DisplaySize.SMALL) {
			return;
		}
		int color = block.getDrawColor();
		int x2 = getFieldDisplayPositionX(engine, playerID) + 4 + x * 16;
		int y2 = getFieldDisplayPositionY(engine, playerID) + 52 + y * 16;
		// Normal Block
		if (block.isNormalBlock() && !block.isBone()) {
			effects.add(new EffectObject(1, x2, y2, color));
		}
		// Gem Block
		else if (block.isGemBlock()) {
			effects.add(new EffectObject(2, x2, y2, color));
		}
	}

	/*
	 * Process of drawing the EXCELLENT screen
	 */
	@Override
	public void renderExcellent(GameEngine engine, int playerID) {
		if (graphics == null || !engine.isVisible || !engine.allowTextRenderByReceiver) {
			return;
		}
		int offsetX = getFieldDisplayPositionX(engine, playerID);
		int offsetY = getFieldDisplayPositionY(engine, playerID);

		if (engine.displaySize != DisplaySize.SMALL) {
			if (engine.statc_1() == 0) {
				NormalFontSwing.printFont(offsetX + 4, offsetY + 204, "EXCELLENT!", Colors.FONT_ORANGE, 1.0f);
			} else if (engine.owner.getPlayers() < 3) {
				NormalFontSwing.printFont(offsetX + 52, offsetY + 204, "WIN!", Colors.FONT_ORANGE, 1.0f);
			} else {
				NormalFontSwing.printFont(offsetX + 4, offsetY + 204, "1ST PLACE!", Colors.FONT_ORANGE, 1.0f);
			}
		} else if (engine.statc_1() == 0) {
			NormalFontSwing.printFont(offsetX + 4, offsetY + 80, "EXCELLENT!", Colors.FONT_ORANGE, 0.5f);
		} else if (engine.owner.getPlayers() < 3) {
			NormalFontSwing.printFont(offsetX + 33, offsetY + 80, "WIN!", Colors.FONT_ORANGE, 0.5f);
		} else {
			NormalFontSwing.printFont(offsetX + 4, offsetY + 80, "1ST PLACE!", Colors.FONT_ORANGE, 0.5f);
		}
	}

	/*
	 * game overProcess of drawing the screen
	 */
	@Override
	public void renderGameOver(GameEngine engine, int playerID) {
		if (graphics == null) {
			return;
		}
		if (!engine.isVisible || !engine.allowTextRenderByReceiver) {
			return;
		}

		if (engine.statc_0() >= engine.field.getHeight() + 1 && engine.statc_0() < engine.field.getHeight() + 1 + 180) {
			int offsetX = getFieldDisplayPositionX(engine, playerID);
			int offsetY = getFieldDisplayPositionY(engine, playerID);

			if (engine.displaySize != DisplaySize.SMALL) {
				if (engine.owner.getPlayers() < 2) {
					NormalFontSwing.printFont(offsetX + 12, offsetY + 204, "GAME OVER", Colors.FONT_WHITE, 1.0f);
				} else if (engine.owner.getWinner() == -2) {
					NormalFontSwing.printFont(offsetX + 52, offsetY + 204, "DRAW", Colors.FONT_GREEN, 1.0f);
				} else if (engine.owner.getPlayers() < 3) {
					NormalFontSwing.printFont(offsetX + 52, offsetY + 204, "LOSE", Colors.FONT_WHITE, 1.0f);
				}
			} else if (engine.owner.getPlayers() < 2) {
				NormalFontSwing.printFont(offsetX + 4, offsetY + 80, "GAME OVER", Colors.FONT_WHITE, 0.5f);
			} else if (engine.owner.getWinner() == -2) {
				NormalFontSwing.printFont(offsetX + 28, offsetY + 80, "DRAW", Colors.FONT_GREEN, 0.5f);
			} else if (engine.owner.getPlayers() < 3) {
				NormalFontSwing.printFont(offsetX + 28, offsetY + 80, "LOSE", Colors.FONT_WHITE, 0.5f);
			}
		}
	}

	/*
	 * Render results screenProcessing
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		if (graphics == null) {
			return;
		}
		if (!engine.allowTextRenderByReceiver || !engine.isVisible) {
			return;
		}

		int tempColor;

		if (engine.statc_0() == 0) {
			tempColor = Colors.FONT_RED;
		} else {
			tempColor = Colors.FONT_WHITE;
		}
		NormalFontSwing.printFont(getFieldDisplayPositionX(engine, playerID) + 12,
				getFieldDisplayPositionY(engine, playerID) + 340, "RETRY", tempColor, 1.0f);

		if (engine.statc_0() == 1) {
			tempColor = Colors.FONT_RED;
		} else {
			tempColor = Colors.FONT_WHITE;
		}
		NormalFontSwing.printFont(getFieldDisplayPositionX(engine, playerID) + 108,
				getFieldDisplayPositionY(engine, playerID) + 340, "END", tempColor, 1.0f);
	}

	/*
	 * fieldDrawing process of edit screen
	 */
	@Override
	public void renderFieldEdit(GameEngine engine, int playerID) {
		if (graphics == null) {
			return;
		}
		int x = getFieldDisplayPositionX(engine, playerID) + 4 + engine.fldeditX * 16;
		int y = getFieldDisplayPositionY(engine, playerID) + 52 + engine.fldeditY * 16;
		float bright = engine.fldeditFrames % 60 >= 30 ? -0.5f : -0.2f;
		drawBlock(x, y, engine.fldeditColor, engine.getSkin(), false, bright, 1.0f, 1.0f);
	}

	/*
	 * Executed at the end of the frame (for update)
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		if (playerID == engine.owner.getPlayers() - 1) {
			effectUpdate();
		}
	}

	/*
	 * Executed at the end of the frame (for render)
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if (playerID == engine.owner.getPlayers() - 1) {
			effectRender(engine);
		}
	}

	/**
	 * Update effects
	 */
	protected void effectUpdate() {
		boolean emptyflag = true;

		for (EffectObject effect : effects) {
			if (effect.effect != 0) {
				emptyflag = false;
			}

			// Normal Block
			if (effect.effect == 1) {
				effect.anim += lineEffectSpeed + 1;
				if (effect.anim >= 36) {
					effect.effect = 0;
				}
			}
			// Gem Block
			if (effect.effect == 2) {
				effect.anim += lineEffectSpeed + 1;
				if (effect.anim >= 60) {
					effect.effect = 0;
				}
			}
		}
		if (emptyflag) {
			effects.clear();
		}
	}

	/**
	 * Render effects
	 */
	protected void effectRender(GameEngine engine) {
		for (EffectObject effect : effects) {
			// Normal Block
			if (effect.effect == 1) {
				int x = effect.x - 40;
				int y = effect.y - 15;
				int color = effect.param - Colors.BLOCK_COLOR_GRAY;

				if (effect.anim < 30) {
					int srcx = (effect.anim - 1) % 6 * 96;
					int srcy = (effect.anim - 1) / 6 * 96;
					try {
						graphics.drawImage(resourceManager.getImgBreak()[color][0], x, y, x + 96, y + 96, srcx, srcy,
								srcx + 96, srcy + 96, null);
					} catch (Exception e) {
					}
				} else {
					int srcx = (effect.anim - 30) % 6 * 96;
					int srcy = (effect.anim - 30) / 6 * 96;
					try {
						graphics.drawImage(resourceManager.getImgBreak()[color][1], x, y, x + 96, y + 96, srcx, srcy,
								srcx + 96, srcy + 96, null);
					} catch (Exception e) {
					}
				}
			}
			// Gem Block
			if (effect.effect == 2) {
				int x = effect.x - 8;
				int y = effect.y - 8;
				int srcx = (effect.anim - 1) % 10 * 32;
				int srcy = (effect.anim - 1) / 10 * 32;
				int color = effect.param - Colors.BLOCK_COLOR_GEM_RED;

				try {
					graphics.drawImage(resourceManager.getImgPErase()[color], x, y, x + 32, y + 32, srcx, srcy,
							srcx + 32, srcy + 32, null);
				} catch (Exception e) {
				}
			}
		}
	}
}

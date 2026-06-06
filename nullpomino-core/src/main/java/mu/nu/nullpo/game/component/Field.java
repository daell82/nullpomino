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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.GameEngine;
import mu.nu.nullpo.util.Colors;
import mu.nu.nullpo.util.CustomProperties;

/**
 * Gamefield
 */
@Log4j
public class Field implements Serializable {

	/** Serial version ID */
	private static final long serialVersionUID = 7745183278794213487L;

	/** constant for invalid row selection */
	private static final Block[] INVALID_ROW = new Block[0];

	/** The default width of the field (value is 10) */
	public static final int DEFAULT_WIDTH = 10;

	/** The default height of the field (value is 20) */
	public static final int DEFAULT_HEIGHT = 20;

	/** The default height of the unseen parts of the field (value is 3) */
	public static final int DEFAULT_HIDDEN_HEIGHT = 3;

	/** Attributes of the coordinate (Usually) */
	public static final int COORD_NORMAL = 0;

	/** Attributes of the coordinate (Invisible part) */
	public static final int COORD_HIDDEN = 1;

	/** Attributes of the coordinate (placed Block Disappears) */
	public static final int COORD_VANISH = 2;

	/** Attributes of the coordinate (Wall) */
	public static final int COORD_WALL = 3;

	/** fieldThe width of the */
	@Getter
	protected int width;

	/** Field height */
	@Getter
	protected int height;

	/** The height of the invisible part of the field above */
	@Getter
	protected int hiddenHeight;

	/*
	 * Oct. 6th, 2010: Changed block_field[][] and block_hidden[][] to [row][column]
	 * format, and updated all relevant functions to match. This should facilitate
	 * referencing rows within the field. It appears the unfinished flipVertical()
	 * was written assuming this was possible, and it would greatly ease some of the
	 * work I'm currently doing without having any visible effects outside this
	 * function. -Kitaru
	 */

	/** fieldOfBlock */
	protected Block[][] fieldBlocks;

	/** fieldInvisible on the part ofBlock */
	protected Block[][] hiddenBlocks;

	/** Line clear flag */
	protected boolean[] lineflagField;

	/** I do not look the part ofLine clear flag */
	protected boolean[] lineflagHidden;

	/** count HURRY UP lines (unbreakable for shrinking the field) */
	@Getter
	protected int hurryupFloorLines;

	/** Presence or absence of a ceiling */
	protected boolean ceiling;

	/** Number of total blocks above minimum required in color clears */
	public int colorClearExtraCount;

	/** Number of different colors in simultaneous color clears */
	public int colorsCleared;

	/** Number of gems cleared in last color or line color clear */
	public int gemsCleared;

	/** Number of garbage blocks cleared in last color clear */
	public int garbageCleared;

	/** List of colors of lines cleared in most recent line color clear */
	public final List<Integer> lineColorsCleared = new ArrayList<>();

	/** List of last rows cleared in most recent horizontal line clear. */
	private final List<Block[]> lastLinesCleared = new ArrayList<>();

	/** Used for TGM garbage, can later be extended to all types */
	// public List<Block[]> pendingGarbage;

	/**
	 * Constructor with parameters
	 *
	 * @param w  The width of the field
	 * @param h  Field height
	 * @param hh The height of the invisible part of the above field
	 */
	public Field(int w, int h, int hh) {
		this(w, h, hh, false);
	}

	/**
	 * Constructor with parameters
	 *
	 * @param w  The width of the field
	 * @param h  Field height
	 * @param hh The height of the invisible part of the field above
	 * @param c  Presence or absence of a ceiling
	 */
	public Field(int w, int h, int hh, boolean c) {
		width = w;
		height = h;
		hiddenHeight = hh;
		ceiling = c;
		reset();
	}

	/**
	 * Default constructor
	 */
	public Field() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_HIDDEN_HEIGHT);
	}

	/**
	 * Copy constructor
	 *
	 * @param f Copy source
	 */
	public Field(Field f) {
		copy(f);
	}

	/**
	 * Called at initialization
	 */
	public void reset() {
		fieldBlocks = new Block[height][width];
		hiddenBlocks = new Block[hiddenHeight][width];
		lineflagField = new boolean[height];
		lineflagHidden = new boolean[hiddenHeight];
		hurryupFloorLines = 0;

		colorClearExtraCount = 0;
		colorsCleared = 0;
		gemsCleared = 0;
		lineColorsCleared.clear();
		lastLinesCleared.clear();
		garbageCleared = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				fieldBlocks[y][x] = new Block();
			}
			for (int y = 0; y < hiddenHeight; y++) {
				hiddenBlocks[y][x] = new Block();
			}
		}
	}

	/**
	 * AnotherFieldCopied from the
	 *
	 * @param f Copy source
	 */
	public void copy(Field f) {
		width = f.width;
		height = f.height;
		hiddenHeight = f.hiddenHeight;
		ceiling = f.ceiling;

		fieldBlocks = new Block[height][width];
		hiddenBlocks = new Block[hiddenHeight][width];
		lineflagField = new boolean[height];
		lineflagHidden = new boolean[hiddenHeight];
		hurryupFloorLines = f.hurryupFloorLines;

		colorClearExtraCount = f.colorClearExtraCount;
		colorsCleared = f.colorsCleared;
		gemsCleared = f.gemsCleared = 0;
		lineColorsCleared.clear();
		lineColorsCleared.addAll(f.lineColorsCleared);
		lastLinesCleared.clear();
		lastLinesCleared.addAll(f.lastLinesCleared);
		garbageCleared = f.garbageCleared;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				fieldBlocks[y][x] = new Block(f.getBlock(x, y));
			}
			for (int y = 0; y < hiddenHeight; y++) {
				hiddenBlocks[y][x] = new Block(f.getBlock(x, -y - 1));
			}
		}
	}

	/**
	 * Stored in the property set
	 *
	 * @param p  Property Set
	 * @param id AppropriateID
	 */
	public void writeProperty(CustomProperties p, int id) {
		for (int y = 0; y < height; y++) {
			StringBuilder row = new StringBuilder();
			for (int x = 0; x < width; x++) {
				row.append(String.valueOf(getBlockColor(x, y)));
				if (x < width - 1) {
					row.append(',');
				}
			}
			p.setProperty(id + ".field.map." + y, row.toString());
		}
	}

	/**
	 * Read from the property set
	 *
	 * @param p  Property Set
	 * @param id AppropriateID
	 */
	public void readProperty(CustomProperties p, int id) {
		for (int y = 0; y < height; y++) {
			String mapStr = p.getProperty(id + ".field.map." + y, "");
			String[] mapArray = mapStr.split(",");

			for (int x = 0; x < mapArray.length && x < width; x++) {
				int blkColor = Colors.BLOCK_COLOR_NONE;

				try {
					blkColor = Integer.parseInt(mapArray[x]);
				} catch (NumberFormatException e) {
				}
				Block block = getBlock(x, y);
				block.color = blkColor;
				block.elapsedFrames = -1;
			}
		}
	}

	/**
	 * Gets the attributes of the specified coordinates
	 *
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Attributes of the coordinate
	 */
	public int getCoordAttribute(int x, int y) {
		// Ceiling
		if (y < 0 && ceiling || y >= height) {
			return COORD_WALL;
		}

		// Wall
		if (x < 0 || x >= width) {
			return COORD_WALL;
		}

		// Usually
		if (y >= 0) {
			return COORD_NORMAL;
		}

		// Invisible part
		int y2 = y * -1 - 1;
		if (y2 < hiddenHeight) {
			return COORD_HIDDEN;
		}

		// Was placedBlockDisappears
		return COORD_VANISH;
	}

	/**
	 * @param y height of the row in the field
	 * @return a reference to the row
	 */
	@NonNull
	private Block[] getRow(int y) {
		if (y >= 0) {
			return y < height ? fieldBlocks[y] : INVALID_ROW;
		}
		// check hidden parts
		int y2 = -y - 1;
		if (y2 >= 0 && y2 < hiddenHeight) {
			return hiddenBlocks[y2];
		}
		return INVALID_ROW;
	}

	/**
	 * Get the Block located at the specified coordinates
	 *
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Block located at the specified coordinates or {@code null}
	 */
	public Block getBlock(int x, int y) {
		Block[] row = getRow(y);
		if (x >= 0 && x < row.length) {
			return row[x];
		}
		return null;
	}

	/**
	 * Set block to specific location
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param block Block
	 * @return true if successful, false if failed
	 */
	public boolean setBlock(int x, int y, Block otherBlock) {
		Block block = getBlock(x, y);
		if (block == null) {
			return false;
		}
		block.copy(otherBlock);
		return true;
	}

	/**
	 * Get the Block color located at the specified coordinates
	 *
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return Block color located at the specified coordinates or
	 *         {@link Colors#BLOCK_COLOR_INVALID} on invalid values
	 *
	 */
	public int getBlockColor(int x, int y) {
		Block block = getBlock(x, y);
		return block != null ? block.color : Colors.BLOCK_COLOR_INVALID;
	}

	/**
	 * Get the Block color located at the specified coordinates
	 *
	 * @param x       X-coordinate
	 * @param y       Y-coordinate
	 * @param gemSame If true, a gem block will return the color of the
	 *                corresponding normal block.
	 * @return Block color located at the specified coordinates or
	 *         {@link Colors#BLOCK_COLOR_INVALID} on invalid values
	 */
	public int getBlockColor(int x, int y, boolean gemSame) {
		var color = getBlockColor(x, y);
		return gemSame ? Block.gemToNormalColor(color) : color;
	}

	/**
	 * Change the Block color located at the specified coordinates
	 *
	 * @param x     X-coordinate
	 * @param y     Y-coordinate
	 * @param color Color
	 * @return true if successful, false if failed
	 */
	public boolean setBlockColor(int x, int y, int color) {
		Block block = getBlock(x, y);
		if (block == null) {
			return false;
		}
		block.color = color;
		return true;
	}

	/**
	 * Get the line clear flag
	 *
	 * @param y Y-coordinate
	 * @return true If the column is to disappear, Otherwise (If the coordinates are
	 *         out of range) or false
	 */
	public boolean getLineFlag(int y) {
		// check visible field
		if (y >= 0 && y < height) {
			return lineflagField[y];
		}
		// check hidden field
		int y2 = -y - 1;
		if (y2 >= 0 && y2 < hiddenHeight) {
			return lineflagHidden[y2];
		}
		return false;
	}

	/**
	 * Determine whether the block is an empty space at the specified coordinates
	 *
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return whether the block is an empty space at the specified coordinates
	 */
	public boolean getBlockEmpty(int x, int y) {
		Block block = getBlock(x, y);
		return block == null || block.isEmpty();
	}

	/**
	 * Set the Line clear flag
	 *
	 * @param y    Y-coordinate
	 * @param flag SetLine clear flag
	 * @return true if successful, false if failed
	 */
	public boolean setLineFlag(int y, boolean flag) {
		// fieldIn
		if (y >= 0) {
			if (y < height) {
				lineflagField[y] = flag;
				return true;
			}
			return false;
		}
		// fieldOutside
		int y2 = -y - 1;
		if (y2 >= 0 && y2 < hiddenHeight) {
			lineflagHidden[y2] = flag;
			return true;
		}
		return false;
	}

	/**
	 * Check the field for line which will be cleared (i.e. a line with all
	 * non-empty and non-wall blocks).
	 * <p>
	 * When a vanishing line is found, the method will add a copy into
	 * {@link #lastLinesCleared} and set the {@link Block#BLOCK_ATTRIBUTE_ERASE}
	 * flag to the original blocks in the field.
	 *
	 * @return the amount of lines that will be erased
	 */
	public int checkLineClears() {
		lastLinesCleared.clear();

		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			boolean lineClear = true;
			for (int x = 0; x < width; x++) {
				Block block = getBlock(x, y);
				if (block == null || block.isEmpty() || block.getAttribute(Block.BLOCK_ATTRIBUTE_WALL)) {
					lineClear = false;
					break;
				}
			}
			setLineFlag(y, lineClear);

			if (lineClear) {
				Block[] fieldRow = getRow(y);
				Block[] clearedRow = new Block[width];
				for (int i = 0; i < width; i++) {
					clearedRow[i] = new Block(fieldRow[i]);
					fieldRow[i].setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
				}
				lastLinesCleared.add(clearedRow);
			}
		}
		return lastLinesCleared.size();
	}

	/**
	 * Check the field for lines which will be cleared (i.e. a line with all
	 * non-empty and non-wall blocks).
	 * <p>
	 * The method does not modify the state of the field.
	 *
	 * @return the amount of lines that will be erased
	 * @see #checkLineClears()
	 */
	public int countLineClear() {
		int lines = 0;
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			boolean lineClear = true;
			for (int x = 0; x < width; x++) {
				Block block = getBlock(x, y);
				if (block == null || block.isEmpty() || block.getAttribute(Block.BLOCK_ATTRIBUTE_WALL)) {
					lineClear = false;
					break;
				}
			}
			if (lineClear) {
				lines++;
			}
		}
		return lines;
	}

	/**
	 * LinesDisappear
	 *
	 * @return Had disappearedLinescount
	 */
	public int clearLine() {
		int lines = 0;

		// fieldIn
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			if (getLineFlag(y)) {
				lines++;

				for (int x = 0; x < width; x++) {
					Block block = getBlock(x, y);
					if (block == null) {
						continue;
					}
					if (block.hard > 0) {
						block.hard--;
						setLineFlag(y, false);
					} else {
						block.color = Colors.BLOCK_COLOR_NONE;
					}
				}
			}
		}

		// Had disappearedLinesThe top and bottom of theBlockClear the binding of
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			if (getLineFlag(y - 1)) {
				for (int x = 0; x < width; x++) {
					Block blk = getBlock(x, y);

					if (blk != null && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
						blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
						setBlockLinkBroken(x, y);
					}
				}
			}
			if (getLineFlag(y + 1)) {
				for (int x = 0; x < width; x++) {
					Block blk = getBlock(x, y);

					if (blk != null && blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
						blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
						setBlockLinkBroken(x, y);
					}
				}
			}
		}

		return lines;
	}

	/**
	 * Was on theBlockDown to the bottom all the
	 *
	 * @return Had disappearedLinescount
	 */
	public int downFloatingBlocks() {
		int lines = 0;
		int y = getHeightWithoutHurryupFloor() - 1;

		for (int i = hiddenHeight * -1; i < getHeightWithoutHurryupFloor(); i++) {
			if (getLineFlag(y)) {
				lines++;

				// BlockA1Copied from the rows above
				for (int k = y; k > hiddenHeight * -1; k--) {
					for (int l = 0; l < width; l++) {
						Block blk = getBlock(l, k - 1);
						if (blk == null) {
							blk = new Block();
						}
						setBlock(l, k, blk);
						setLineFlag(k, getLineFlag(k - 1));
					}
				}

				// Blank to the top
				for (int l = 0; l < width; l++) {
					Block blk = new Block();
					setBlock(l, hiddenHeight * -1, blk);
				}
				setLineFlag(hiddenHeight * -1, false);
			} else {
				y--;
			}
		}

		return lines;
	}

	/**
	 * Was on theBlockA1Only step down
	 */
	public void downFloatingBlocksSingleLine() {
		int y = getHeightWithoutHurryupFloor() - 1;

		for (int i = hiddenHeight * -1; i < getHeightWithoutHurryupFloor(); i++) {
			if (getLineFlag(y)) {
				// BlockA1Copied from the rows above
				for (int k = y; k > hiddenHeight * -1; k--) {
					for (int l = 0; l < width; l++) {
						Block blk = getBlock(l, k - 1);
						if (blk == null) {
							blk = new Block();
						}
						setBlock(l, k, blk);
						setLineFlag(k, getLineFlag(k - 1));
					}
				}

				// Blank to the top
				for (int l = 0; l < width; l++) {
					Block blk = new Block();
					setBlock(l, hiddenHeight * -1, blk);
				}
				setLineFlag(hiddenHeight * -1, false);
				return;
			} else {
				y--;
			}
		}
	}

	/**
	 * VanishLinescountAcountObtained
	 *
	 * @return Linescount
	 */
	public int getLines() {
		int lines = 0;

		for (int i = hiddenHeight * -1; i < getHeightWithoutHurryupFloor(); i++) {
			if (getLineFlag(i)) {
				lines++;
			}
		}

		return lines;
	}

	/**
	 * All clearIf it&#39;s the casetrue
	 *
	 * @return All clearIf it&#39;s the casetrue
	 */
	public boolean isEmpty() {
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			if (getLineFlag(y)) {
				continue;
			}
			for (int x = 0; x < width; x++) {
				if (!getBlockEmpty(x, y)) {
					Block b = getBlock(x, y);
					if (!b.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Check if specified line is completely empty
	 *
	 * @param y Y coord
	 * @return <code>true</code> if the specified line is completely empty,
	 *         <code>false</code> otherwise.
	 */
	public boolean isEmptyLine(int y) {
		for (int x = 0; x < width; x++) {
			if (!getBlockEmpty(x, y)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * T-SpinIf it was the terrain to betrue
	 *
	 * @param x   X-coordinate
	 * @param y   Y-coordinate
	 * @param big BigWhether
	 * @return T-SpinIf it was the terrain to betrue
	 */
	public boolean isTSpinSpot(int x, int y, boolean big) {
		// Sets the coordinates for determining the relative
		int[] tx;
		int[] ty;
		if (big) {
			tx = new int[] { 1, 4, 1, 4 };
			ty = new int[] { 1, 1, 4, 4 };
		} else {
			tx = new int[] { 0, 2, 0, 2 };
			ty = new int[] { 0, 0, 2, 2 };
		}

		// Judgment
		int count = 0;

		for (int i = 0; i < tx.length; i++) {
			if (getBlockColor(x + tx[i], y + ty[i]) != Colors.BLOCK_COLOR_NONE) {
				count++;
			}
		}
		return count >= 3;
	}

	/**
	 * Examine What in the number ofBlockwhether there is
	 *
	 * @return fieldAre withinBlockOfcount
	 */
	public int getHowManyBlocks() {
		int count = 0;

		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			if (getLineFlag(y)) {
				continue;
			}
			for (int x = 0; x < width; x++) {
				if (!getBlockEmpty(x, y)) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Get Block the At the top Of Y-coordinate
	 *
	 * @return At the topBlockOfY-coordinate
	 */
	public int getHighestBlockY() {
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			if (getLineFlag(y)) {
				continue;
			}
			for (int x = 0; x < width; x++) {
				if (!getBlockEmpty(x, y)) {
					return y;
				}
			}
		}
		return height;
	}

	/**
	 * At the topBlockOfY-coordinateGet the (X-coordinateWe can specifyVersion)
	 *
	 * @param x X-coordinate
	 * @return At the topBlockOfY-coordinate
	 */
	public int getHighestBlockY(int x) {
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			if (!getLineFlag(y) && !getBlockEmpty(x, y)) {
				return y;
			}
		}
		return height;
	}

	/**
	 * garbage blockFirst occurrence of theY-coordinateGet the
	 *
	 * @return garbage blockFirst occurrence of theY-coordinate
	 */
	public int getHighestGarbageBlockY() {
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			if (!getLineFlag(y)) {
				continue;
			}
			for (int x = 0; x < width; x++) {
				Block block = getBlock(x, y);
				if (block != null && !block.isEmpty() && block.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
					return y;
				}
			}
		}

		return height;
	}

	/**
	 * I will see if there is a gap under the specified coordinates
	 *
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @return If there is a gap under the specified coordinatestrue
	 */
	public boolean isHoleBelow(int x, int y) {
		return !getBlockEmpty(x, y) && getBlockEmpty(x, y + 1);
	}

	/**
	 * fieldThe gap in thecountExamine the
	 *
	 * @return fieldThe gap in thecount
	 */
	public int getHowManyHoles() {
		int hole = 0;
		boolean samehole = false;

		for (int x = 0; x < width; x++) {
			samehole = false;
			for (int y = getHighestBlockY(); y < getHeightWithoutHurryupFloor(); y++) {
				if (getLineFlag(y)) {
					continue;
				}
				if (isHoleBelow(x, y)) {
					samehole = true;
				} else if (samehole && getBlockEmpty(x, y)) {
					hole++;
				} else {
					samehole = false;
				}
			}
		}

		return hole;
	}

	/**
	 * Many pieces on top of the gapBlockI find out that you have stacked
	 *
	 * @return Are stackedBlockOfcount
	 */
	public int getHowManyLidAboveHoles() {
		int blocks = 0;

		for (int x = 0; x < width; x++) {
			int count = 0;
			for (int y = getHighestBlockY(); y < getHeightWithoutHurryupFloor() - 1; y++) {
				if (getLineFlag(y)) {
					continue;
				}
				if (isHoleBelow(x, y)) {
					count++;
					blocks += count;
					count = 0;
				} else if (!getBlockEmpty(x, y)) {
					count++;
				}
			}
		}
		return blocks;
	}

	/**
	 * Every valley (■ ■ which returns the total depth of the) terrain that is (The
	 * return value is large enough to have many deep valleys)
	 *
	 * @return I shall be the sum of the depth of the valley all
	 */
	public int getTotalValleyDepth() {
		int depth = 0;
		for (int x = 0; x < width; x++) {
			int d = getValleyDepth(x);
			if (d >= 2) {
				depth += d;
			}
		}
		return depth;
	}

	/**
	 * IValley type is required (Depth3Or more)countReturns
	 *
	 * @return IValley type is requiredcount
	 */
	public int getTotalValleyNeedIPiece() {
		int count = 0;
		for (int j = 0; j < width; j++) {
			if (getValleyDepth(j) >= 3) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Valley (■ ■ examine the depth of the) terrain that is
	 *
	 * @param x ExamineX-coordinate
	 * @return The depth of the valley (If you do not have0)
	 */
	private int getValleyDepth(int x) {
		int depth = 0;

		int highest = getHighestBlockY(x - 1);
		highest = Math.min(highest, getHighestBlockY(x));
		highest = Math.min(highest, getHighestBlockY(x + 1));

		for (int i = highest; i < getHeightWithoutHurryupFloor(); i++) {
			if (!getLineFlag(i) && (getBlockEmpty(x - 1, i) || x <= 0) && !getBlockEmpty(x, i)
					&& (getBlockEmpty(x + 1, i) || x >= width - 1)) {
				depth++;
			}
		}
		return depth;
	}

	/**
	 * fieldSlide on the entire
	 *
	 * @param lines shifting stagecount
	 */
	public void pushUp(int lines) {
		for (int k = 0; k < lines; k++) {
			for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor() - 1; y++) {
				// BlockA1Copy from the bottom stage
				for (int x = 0; x < width; x++) {
					Block blk = getBlock(x, y + 1);
					if (blk == null) {
						blk = new Block();
					}
					setBlock(x, y, blk);
					setLineFlag(y, getLineFlag(y + 1));
				}
			}

			// Blank to the bottom
			for (int x = 0; x < width; x++) {
				int y = getHeightWithoutHurryupFloor() - 1;
				setBlock(x, y, new Block());
				setLineFlag(y, false);
			}
		}
	}

	/**
	 * fieldThe entire1Staggered rows above
	 */
	public void pushUp() {
		pushUp(1);
	}

	/**
	 * fieldSlide down the entire
	 *
	 * @param lines shifting stagecount
	 */
	public void pushDown(int lines) {
		for (int k = 0; k < lines; k++) {
			for (int i = getHeightWithoutHurryupFloor() - 1; i > hiddenHeight * -1; i--) {
				// BlockA1Copied from the rows above
				for (int j = 0; j < width; j++) {
					Block blk = getBlock(j, i - 1);
					if (blk == null) {
						blk = new Block();
					}
					setBlock(j, i, blk);
					setLineFlag(i, getLineFlag(i + 1));
				}
			}

			// Blank to the top
			for (int j = 0; j < width; j++) {
				setBlock(j, hiddenHeight * -1, new Block());
				setLineFlag(hiddenHeight * -1, false);
			}
		}
	}

	/**
	 * fieldThe entire1Slide down the stage
	 */
	public void pushDown() {
		pushDown(1);
	}

	/**
	 * Cut the specified line(s) then push down all things above
	 *
	 * @param y     Y coord
	 * @param lines Number of lines to cut
	 */
	public void cutLine(int y, int lines) {
		for (int k = 0; k < lines; k++) {
			for (int i = y; i > hiddenHeight * -1; i--) {
				for (int x = 0; x < width; x++) {
					Block block = getBlock(x, i - 1);
					if (block == null) {
						block = new Block();
					}
					setBlock(x, i, block);
				}
				setLineFlag(i, getLineFlag(i + 1));
			}
			for (int x = 0; x < width; x++) {
				setBlock(x, hiddenHeight * -1, new Block());
				setLineFlag(hiddenHeight * -1, false);
			}
		}
	}

	/**
	 * Holes1I only place opengarbage blockAdded to the bottom of the
	 *
	 * @param hole      Hole position (-1If no hole)
	 * @param color     garbage block color
	 * @param skin      garbage blockPicture of
	 * @param attribute garbage blockAttributes
	 * @param lines     Addgarbage blockOfLinescount
	 * @deprecated unused method
	 */
	@Deprecated(since = "7.6")
	protected void addSingleHoleGarbage(int hole, int color, int skin, int attribute, int lines) {
		for (int k = 0; k < lines; k++) {
			pushUp(1);
			for (int x = 0; x < width; x++) {
				if (x == hole) {
					continue;
				}
				Block blk = new Block();
				blk.color = color;
				blk.skin = skin;
				blk.attribute = attribute;
				setBlock(x, getHeightWithoutHurryupFloor() - 1, blk);
			}
		}
	}

	/**
	 * Add a single hole garbage (Attributes are automatically set)
	 *
	 * @param hole  Hole position
	 * @param color Color
	 * @param skin  Skin
	 * @param lines Number of garbage lines to add
	 */
	public void addSingleHoleGarbage(int hole, int color, int skin, int lines) {
		for (int k = 0; k < lines; k++) {
			pushUp(1);

			int y = getHeightWithoutHurryupFloor() - 1;

			for (int x = 0; x < width; x++) {
				if (x == hole) {
					continue;
				}
				Block blk = new Block();
				blk.color = color;
				blk.skin = skin;
				blk.attribute = Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE
						| Block.BLOCK_ATTRIBUTE_GARBAGE;
				setBlock(x, y, blk);
			}

			// Set connections
			for (int x = 0; x < width; x++) {
				if (x == hole) {
					continue;
				}
				Block blk = getBlock(x, y);
				if (x > 0 && !getBlockEmpty(x - 1, y)) {
					blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, true);
				}
				if (x < width - 1 && !getBlockEmpty(x + 1, y)) {
					blk.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, true);
				}
			}
		}
	}

	/**
	 * BottommostLinesI copied the shape of thegarbage blockAdded to the bottom of
	 * the
	 *
	 * @param color     garbage block color
	 * @param skin      garbage blockPicture of
	 * @param attribute garbage blockAttributes
	 * @param lines     Addgarbage blockOfLinescount
	 */
	public void addBottomCopyGarbage(int color, int skin, int attribute, int lines) {
		for (int k = 0; k < lines; k++) {
			pushUp(1);

			for (int j = 0; j < width; j++) {
				boolean empty = getBlockEmpty(j, height - 2);

				if (!empty) {
					Block blk = new Block();
					blk.color = color;
					blk.skin = skin;
					blk.attribute = attribute;
					setBlock(j, getHeightWithoutHurryupFloor() - 1, blk);
				}
			}
		}
	}

	/**
	 * 裏段位を取得 (from NullpoMino Unofficial Expansion build 091309)
	 *
	 * @author Zircean
	 * @return Dan back
	 */
	public int getSecretGrade() {
		int holeLoc;
		int rows = 0;
		boolean rowCheck;

		for (int i = height - 1; i > 0; i--) {
			holeLoc = -Math.abs(i - height / 2) + height / 2 - 1;
			if (getBlockEmpty(holeLoc, i) && !getBlockEmpty(holeLoc, i - 1)) {
				rowCheck = true;
				for (int j = 0; j < width; j++) {
					if (j != holeLoc && getBlockEmpty(j, i)) {
						rowCheck = false;
						break;
					}
				}
				if (rowCheck) {
					rows++;
				} else {
					break;
				}
			} else {
				break;
			}
		}

		return rows;
	}

	/**
	 * AllBlockChange the attributes of the
	 *
	 * @param attr   I want to change the attributes
	 * @param status After the change state
	 */
	public void setAllAttribute(int attr, boolean status) {
		for (int y = hiddenHeight * -1; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Block block = getBlock(x, y);
				block.setAttribute(attr, status);
			}
		}
	}

	/**
	 * AllBlockChange the pattern of
	 *
	 * @param skin Picture
	 */
	public void setAllSkin(int skin) {
		for (int y = hiddenHeight * -1; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Block block = getBlock(x, y);
				block.skin = skin;
			}
		}
	}

	/**
	 * Get the number of gem blocks in the field
	 *
	 * @return the number of gem blocks in the field
	 */
	public int getHowManyGems() {
		int gems = 0;
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			for (int x = 0; x < width; x++) {
				Block block = getBlock(x, y);
				if (block.isGemBlock()) {
					gems++;
				}
			}
		}
		return gems;
	}

	/**
	 * Get the number of disappeared Gem Blocks
	 *
	 * @return the number of disappeared gem blocks
	 */
	public int getHowManyGemClears() {
		int gems = 0;

		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			if (getLineFlag(y)) {
				for (int x = 0; x < width; x++) {
					Block block = getBlock(x, y);
					if (block.isGemBlock()) {
						gems++;
					}
				}
			}
		}
		return gems;
	}

	/**
	 * Clear line colors of sufficient size.
	 *
	 * @param size      Minimum length of line for a clear
	 * @param diagonals <code>true</code> to check diagonals, <code>false</code> to
	 *                  check only vertical and horizontal
	 * @param gemSame   <code>true</code> to check gem blocks
	 * @return Total number of blocks cleared.
	 */
	public int clearLineColor(int size, boolean diagonals, boolean gemSame) {
		int total = 0;
		Block block;
		Block bAdj;
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			for (int x = 0; x < width; x++) {
				block = getBlock(x, y);
				if (block == null || !block.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE)) {
					continue;
				}
				total++;
				if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
					bAdj = getBlock(x, y + 1);
					if (bAdj != null) {
						bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
						bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
					}
				}
				if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
					bAdj = getBlock(x, y - 1);
					if (bAdj != null) {
						bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
						bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
					}
				}
				if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) {
					bAdj = getBlock(x - 1, y);
					if (bAdj != null) {
						bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
						bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
					}
				}
				if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) {
					bAdj = getBlock(x + 1, y);
					if (bAdj != null) {
						bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
						bAdj.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
					}
				}
				block.color = Colors.BLOCK_COLOR_NONE;
			}
		}
		return total;
	}

	/**
	 * Check for line color clears of sufficient size.
	 *
	 * @param size      Minimum length of line for a clear
	 * @param flag      <code>true</code> to set BLOCK_ATTRIBUTE_ERASE to true on
	 *                  blocks to be cleared.
	 * @param diagonals <code>true</code> to check diagonals, <code>false</code> to
	 *                  check only vertical and horizontal
	 * @param gemSame   <code>true</code> to check gem blocks
	 * @return Total number of blocks that would be cleared.
	 */
	public int checkLineColor(int size, boolean flag, boolean diagonals, boolean gemSame) {
		if (size < 1) {
			return 0;
		}
		if (flag) {
			setAllAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);
			gemsCleared = 0;
		}
		int total = 0;
		int count;
		int lineColor;
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			for (int x = 0; x < width; x++) {
				lineColor = getBlockColor(x, y, gemSame);
				if (lineColor == Colors.BLOCK_COLOR_NONE || lineColor == Colors.BLOCK_COLOR_INVALID) {
					continue;
				}
				for (int dir = 0; dir < (diagonals ? 3 : 2); dir++) {
					count = countLineColor(x, y, lineColor, dir, gemSame);
					if (count >= size) {
						total += count;
						if (!flag) {
							continue;
						}
						if (count == size) {
							lineColorsCleared.add(lineColor);
						}
						clearLineColor(x, y, lineColor, dir, gemSame);
					}
				}
			}
		}
		return total;
	}

	private int countLineColor(int x, int y, int lineColor, int dir, boolean gemSame) {
		int count = 0;
		int blockColor = lineColor;
		while (lineColor == blockColor) {
			count++;
			if (dir != 1) {
				y++;
			}
			if (dir != 0) {
				x++;
			}
			blockColor = getBlockColor(x, y, gemSame);
		}
		return count;
	}

	private void clearLineColor(int x, int y, int lineColor, int dir, boolean gemSame) {
		int blockColor = lineColor;
		while (lineColor == blockColor) {
			Block b = getBlock(x, y);
			if (b.hard > 0) {
				b.hard--;
			} else if (!b.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE)) {
				if (b.isGemBlock()) {
					gemsCleared++;
				}
				b.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
			}
			if (dir != 1) {
				y++;
			}
			if (dir != 0) {
				x++;
			}
			blockColor = getBlockColor(x, y, gemSame);
		}
	}

	/**
	 * Performs all color clears of sufficient size containing at least one gem
	 * block.
	 *
	 * @param size         Minimum size of cluster for a clear
	 * @param garbageClear <code>true</code> to clear garbage blocks adjacent to
	 *                     cleared clusters
	 * @return Total number of blocks cleared.
	 */
	public int gemClearColor(int size, boolean garbageClear, boolean ignoreHidden) {
		Field temp = new Field(this);
		int total = 0;
		Block b;

		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			for (int x = 0; x < width; x++) {
				b = getBlock(x, y);
				if (b == null || !b.isGemBlock()) {
					continue;
				}
				int clear = temp.clearColor(x, y, false, garbageClear, true, ignoreHidden);
				if (clear >= size) {
					total += clear;
					clearColor(x, y, false, garbageClear, true, ignoreHidden);
				}
			}
		}
		return total;
	}

	public int gemClearColor(int size, boolean garbageClear) {
		return gemClearColor(size, garbageClear, false);
	}

	/**
	 * Performs all color clears of sufficient size.
	 *
	 * @param size         Minimum size of cluster for a clear
	 * @param garbageClear <code>true</code> to clear garbage blocks adjacent to
	 *                     cleared clusters
	 * @param gemSame      <code>true</code> to check gem blocks
	 * @return Total number of blocks cleared.
	 */
	public int clearColor(int size, boolean garbageClear, boolean gemSame, boolean ignoreHidden) {
		Field temp = new Field(this);
		int total = 0;
		for (int y = ignoreHidden ? 0 : hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			for (int x = 0; x < width; x++) {
				int clear = temp.clearColor(x, y, false, garbageClear, gemSame, ignoreHidden);
				if (clear >= size) {
					total += clear;
					clearColor(x, y, false, garbageClear, gemSame, ignoreHidden);
				}
			}
		}
		return total;
	}

	public int clearColor(int size, boolean garbageClear, boolean gemSame) {
		return clearColor(size, garbageClear, gemSame, false);
	}

	/**
	 * Clears the block at the given position as well as all adjacent blocks of the
	 * same color, and any garbage blocks adjacent to the group if garbageClear is
	 * true.
	 *
	 * @param x            x-coordinate
	 * @param y            y-coordinate
	 * @param flag         <code>true</code> to set BLOCK_ATTRIBUTE_ERASE to true on
	 *                     cleared blocks.
	 * @param garbageClear <code>true</code> to clear garbage blocks adjacent to
	 *                     cleared clusters
	 * @param gemSame      <code>true</code> to check gem blocks
	 * @return The number of blocks cleared.
	 */
	public int clearColor(int x, int y, boolean flag, boolean garbageClear, boolean gemSame, boolean ignoreHidden) {
		int blockColor = getBlockColor(x, y, gemSame);
		if (blockColor == Colors.BLOCK_COLOR_NONE || blockColor == Colors.BLOCK_COLOR_INVALID) {
			return 0;
		}
		Block b = getBlock(x, y);
		if (b == null) {
			return 0;
		}
		if (b.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
			return 0;
		} else {
			return clearColor(x, y, blockColor, flag, garbageClear, gemSame, ignoreHidden);
		}
	}

	/**
	 * Note: This method is private because calling it with a targetColor parameter
	 * of BLOCK_COLOR_NONE or BLOCK_COLOR_INVALID may cause an infinite loop and
	 * crash the game. This check is handled by the above public method so as to
	 * avoid redundant checks.
	 */
	private int clearColor(int x, int y, int targetColor, boolean flag, boolean garbageClear, boolean gemSame,
			boolean ignoreHidden) {
		if (ignoreHidden && y < 0) {
			return 0;
		}
		int blockColor = getBlockColor(x, y, gemSame);
		if (blockColor == Colors.BLOCK_COLOR_INVALID) {
			return 0;
		}
		Block block = getBlock(x, y);
		if (flag && block.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE)) {
			return 0;
		}
		if (garbageClear && block.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)
				&& !block.getAttribute(Block.BLOCK_ATTRIBUTE_WALL)) {
			if (flag) {
				block.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
				garbageCleared++;
			} else if (block.hard > 0) {
				block.hard--;
			} else {
				block.color = Colors.BLOCK_COLOR_NONE;
			}
		}
		if (blockColor != targetColor) {
			return 0;
		}
		if (flag) {
			block.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
		} else if (block.hard > 0) {
			block.hard--;
		} else {
			block.color = Colors.BLOCK_COLOR_NONE;
		}
		return 1 + clearColor(x + 1, y, targetColor, flag, garbageClear, gemSame, ignoreHidden)
				+ clearColor(x - 1, y, targetColor, flag, garbageClear, gemSame, ignoreHidden)
				+ clearColor(x, y + 1, targetColor, flag, garbageClear, gemSame, ignoreHidden)
				+ clearColor(x, y - 1, targetColor, flag, garbageClear, gemSame, ignoreHidden);
	}

	/**
	 * Clears all blocks of the same color
	 *
	 * @param targetColor The color to clear
	 * @param flag        <code>true</code> to set BLOCK_ATTRIBUTE_ERASE to true on
	 *                    cleared blocks.
	 * @param gemSame     <code>true</code> to check gem blocks
	 * @return The number of blocks cleared.
	 */
	public int allClearColor(int targetColor, boolean flag, boolean gemSame) {
		if (targetColor < 0) {
			return 0;
		}
		if (gemSame) {
			targetColor = Block.gemToNormalColor(targetColor);
		}
		int total = 0;
		for (int y = -1 * hiddenHeight; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Block block = getBlock(x, y);
				if (block == null) {
					continue;
				}
				if (block.color == targetColor) {
					total++;
					if (flag) {
						block.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
					} else {
						block.color = Colors.BLOCK_COLOR_NONE;
					}
				}
			}
		}
		return total;
	}

	public boolean doCascadeGravity(GameEngine.LineGravity type) {
		setAllAttribute(Block.BLOCK_ATTRIBUTE_LAST_COMMIT, false);
		if (type == GameEngine.LineGravity.CASCADE_SLOW) {
			return doCascadeSlow();
		} else {
			return doCascadeGravity();
		}
	}

	/**
	 * Main routine for cascade gravity.
	 *
	 * @return <code>true</code> if something falls. <code>false</code> if nothing
	 *         falls.
	 */
	public boolean doCascadeGravity() {
		boolean result = false;

		setAllAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, false);
		for (int y = getHeightWithoutHurryupFloor() - 1; y >= hiddenHeight * -1; y--) {
			for (int x = 0; x < width; x++) {
				Block blk = getBlock(x, y);
				if (blk == null || blk.isEmpty() || blk.getAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY)) {
					continue;
				}
				checkBlockLink(x, y);
				if (checkCascadeFall()) {
					doCascadeFall();
					result = true;
				}
			}
		}

		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		setAllAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, false);
		return result;
	}

	/**
	 * Routine for cascade gravity which checks from the top down for a slower fall
	 * animation.
	 *
	 * @return <code>true</code> if something falls. <code>false</code> if nothing
	 *         falls.
	 */
	public boolean doCascadeSlow() {
		boolean result = false;

		setAllAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, false);

		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			for (int x = 0; x < width; x++) {
				Block blk = getBlock(x, y);
				if (blk == null || blk.isEmpty() || blk.getAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY)) {
					continue;
				}
				checkBlockLink(x, y);
				if (checkCascadeFall()) {
					doCascadeFall();
					result = true;
				}
			}
		}

		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		setAllAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, false);

		return result;
	}

	private boolean checkCascadeFall() {
		for (int y = getHeightWithoutHurryupFloor() - 1; y >= hiddenHeight * -1; y--) {
			for (int x = 0; x < width; x++) {
				Block block = getBlock(x, y);

				if (block != null && !block.isEmpty() && block.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)
						&& !block.getAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL)) {
					Block bBelow = getBlock(x, y + 1);

					if (getCoordAttribute(x, y + 1) == COORD_WALL || bBelow != null && !bBelow.isEmpty()
							&& !bBelow.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean canCascade() {
		for (int y = getHeightWithoutHurryupFloor() - 1; y >= hiddenHeight * -1; y--) {
			for (int x = 0; x < width; x++) {
				Block blk = getBlock(x, y);

				if (blk != null && !blk.isEmpty() && !blk.getAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY)) {
					boolean fall = true;
					checkBlockLink(x, y);

					for (int y2 = getHeightWithoutHurryupFloor() - 1; y2 >= hiddenHeight * -1; y2--) {
						for (int x2 = 0; x2 < width; x2++) {
							Block bTemp = getBlock(x2, y2);

							if (bTemp != null && !bTemp.isEmpty() && bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)
									&& !bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL)) {
								Block bBelow = getBlock(x2, y2 + 1);

								if (getCoordAttribute(x2, y2 + 1) == COORD_WALL || bBelow != null && !bBelow.isEmpty()
										&& !bBelow.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)) {
									fall = false;
								}
							}
						}
					}

					if (fall) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void doCascadeFall() {
		for (int y = getHeightWithoutHurryupFloor() - 1; y >= hiddenHeight * -1; y--) {
			for (int x = 0; x < width; x++) {
				Block bTemp = getBlock(x, y);
				Block bBelow = getBlock(x, y + 1);

				if (getCoordAttribute(x, y + 1) != COORD_WALL && bTemp != null && !bTemp.isEmpty() && bBelow != null
						&& bBelow.isEmpty() && bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)
						&& !bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL)) {
					bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
					bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CASCADE_FALL, true);
					if (bTemp.getAttribute(Block.BLOCK_ATTRIBUTE_IGNORE_BLOCKLINK)) {
						bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
						bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
						bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
						bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
					}
					bTemp.setAttribute(Block.BLOCK_ATTRIBUTE_LAST_COMMIT, true);
					setBlock(x, y + 1, bTemp);
					setBlock(x, y, new Block());
				}
			}
		}
	}

	/**
	 * Checks the connection of blocks and set "mark" to each block.
	 *
	 * @param x X coord
	 * @param y Y coord
	 */
	public void checkBlockLink(int x, int y) {
		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		checkBlockLinkSub(x, y);
	}

	/**
	 * Subroutine for checkBlockLink.
	 *
	 * @param x X coord
	 * @param y Y coord
	 */
	protected void checkBlockLinkSub(int x, int y) {
		Block block = getBlock(x, y);
		if (block == null || block.isEmpty() || block.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)) {
			return;
		}
		block.setAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, true);

		if (block.getAttribute(Block.BLOCK_ATTRIBUTE_IGNORE_BLOCKLINK)) {
			return;
		}
		if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
			checkBlockLinkSub(x, y - 1);
		}
		if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
			checkBlockLinkSub(x, y + 1);
		}
		if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) {
			checkBlockLinkSub(x - 1, y);
		}
		if (block.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) {
			checkBlockLinkSub(x + 1, y);
		}
	}

	/**
	 * Checks the connection of blocks and set the "broken" flag to each block. It
	 * only affects to normal blocks. (ex. not square or gems)
	 *
	 * @param x X coord
	 * @param y Y coord
	 */
	public void setBlockLinkBroken(int x, int y) {
		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		setBlockLinkBrokenSub(x, y);
	}

	/**
	 * Subroutine for setBlockLinkBrokenSub.
	 *
	 * @param x X coord
	 * @param y Y coord
	 */
	protected void setBlockLinkBrokenSub(int x, int y) {
		Block blk = getBlock(x, y);
		if (blk != null && !blk.isEmpty() && !blk.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)
				&& blk.isNormalBlock()) {
			blk.setAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, true);
			blk.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
			if (blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP)) {
				setBlockLinkBrokenSub(x, y - 1);
			}
			if (blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN)) {
				setBlockLinkBrokenSub(x, y + 1);
			}
			if (blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT)) {
				setBlockLinkBrokenSub(x - 1, y);
			}
			if (blk.getAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT)) {
				setBlockLinkBrokenSub(x + 1, y);
			}
		}
	}

	/**
	 * Checks the color of blocks and set the connection flags to each block.
	 */
	public void setBlockLinkByColor() {
		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			for (int x = 0; x < width; x++) {
				setBlockLinkByColor(x, y);
			}
		}
	}

	/**
	 * Checks the color of blocks and set the connection flags to each block.
	 *
	 * @param x X coord
	 * @param y Y coord
	 */
	protected void setBlockLinkByColor(int x, int y) {
		setAllAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, false);
		setBlockLinkByColorSub(x, y);
	}

	/**
	 * Subroutine for setBlockLinkByColorSub.
	 *
	 * @param x X coord
	 * @param y Y coord
	 */
	protected void setBlockLinkByColorSub(int x, int y) {
		Block block = getBlock(x, y);
		if (block != null && !block.isEmpty() && !block.getAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK)
				&& !block.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE) && block.isNormalBlock()) {
			block.setAttribute(Block.BLOCK_ATTRIBUTE_TEMP_MARK, true);
			if (getBlockColor(x, y - 1) == block.color) {
				block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, true);
				setBlockLinkByColorSub(x, y - 1);
			} else {
				block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
			}
			if (getBlockColor(x, y + 1) == block.color) {
				block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, true);
				setBlockLinkByColorSub(x, y + 1);
			} else {
				block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
			}
			if (getBlockColor(x - 1, y) == block.color) {
				block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, true);
				setBlockLinkByColorSub(x - 1, y);
			} else {
				block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
			}
			if (getBlockColor(x + 1, y) == block.color) {
				block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, true);
				setBlockLinkByColorSub(x + 1, y);
			} else {
				block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
			}
		}
	}

	/**
	 * HURRY UPAdded to the bottom of the ground
	 *
	 * @param lines RaiseLinescount
	 * @param skin  Picture of the ground
	 */
	public void addHurryupFloor(int lines, int skin) {
		for (int k = 0; k < lines; k++) {
			pushUp(1);

			for (int j = 0; j < width; j++) {
				Block block = new Block();
				block.color = Colors.BLOCK_COLOR_GRAY;
				block.skin = skin;
				block.attribute = Block.BLOCK_ATTRIBUTE_WALL | Block.BLOCK_ATTRIBUTE_GARBAGE
						| Block.BLOCK_ATTRIBUTE_VISIBLE;
				setBlock(j, getHeightWithoutHurryupFloor() - 1, block);
			}

			hurryupFloorLines++;
		}
	}

	/**
	 * Returns the remaining Field height without the HURRY UP ground
	 *
	 * @return Field height without the HURRY UP ground
	 */
	public int getHeightWithoutHurryupFloor() {
		return height - hurryupFloorLines;
	}

	/**
	 * @param row Row of blocks
	 * @return a String representing the row
	 */
	public String rowToString(Block[] row) {
		return Stream.of(row).map(Block::toString).collect(Collectors.joining());
	}

	/**
	 * fieldConverted to a string
	 *
	 * @return It was converted to a stringfield
	 */
	public String fieldToString() {
		StringBuilder builder = new StringBuilder();
		for (int i = getHeight() - 1; i >= Math.max(-1, getHighestBlockY()); i--) {
			builder.append(rowToString(getRow(i)));
		}
		// Remove trailing zeros
		while (builder.charAt(builder.length() - 1) == '0') {
			builder = builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

	/**
	 * @param str
	 * @return
	 * @deprecated unused method
	 */
	@Deprecated(forRemoval = true)
	protected Block[] stringToRow(String str) {
		return stringToRow(str, 0, false, false);
	}

	/**
	 * @param str       String representing field state
	 * @param skin      Block skin being used in this field
	 * @param isGarbage Row is a garbage row
	 * @param isWall    Row is a wall (i.e. hurry-up rows)
	 * @return The row array
	 */
	private Block[] stringToRow(String str, int skin, boolean isGarbage, boolean isWall) {
		Block[] row = new Block[getWidth()];
		for (int j = 0; j < getWidth(); j++) {

			int blkColor = Colors.BLOCK_COLOR_NONE;

			/*
			 * NullNoname's original approach from the old stringToField: If a character
			 * outside the row string is referenced, default to an empty block by ignoring
			 * the exception.
			 */
			try {
				char c = str.charAt(j);
				blkColor = Block.charToBlockColor(c);
			} catch (Exception e) {
			}

			row[j] = new Block();
			row[j].color = blkColor;
			row[j].skin = skin;
			row[j].elapsedFrames = -1;
			row[j].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			row[j].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);

			if (isGarbage) { // TODO: This may need extension when garbage does not only sport one hole (i.e.
								// TGM garbage)
				row[j].setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
			}
			if (isWall) {
				row[j].setAttribute(Block.BLOCK_ATTRIBUTE_WALL, true);
			}
		}

		return row;
	}

	/**
	 * Based on a stringfieldChange
	 *
	 * @param str String
	 */
	public void stringToField(String str) {
		stringToField(str, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Based on a stringfieldChange
	 *
	 * @param str             String
	 * @param skin            BlockPicture of
	 * @param highestGarbageY The highestgarbage blockThe position of the
	 * @param highestWallY    The highestHurryupBlockThe position of the
	 */
	public void stringToField(String str, int skin, int highestGarbageY, int highestWallY) {
		for (int y = -1; y < getHeight(); y++) {
			int index = (getHeight() - 1 - y) * getWidth();
			/*
			 * Much like NullNoname's try/catch from the old stringToField that is now in
			 * stringToRow, we need to skip over substrings referenced outside the field
			 * string -- empty rows.
			 */
			try {
				String substr = str.substring(index, Math.min(str.length(), index + getWidth()));
				Block[] row = stringToRow(substr, skin, y >= highestGarbageY, y >= highestWallY);
				for (int x = 0; x < getWidth(); x++) {
					setBlock(x, y, row[x]);
				}
			} catch (Exception e) {
				for (int x = 0; x < getWidth(); x++) {
					setBlock(x, y, new Block(Colors.BLOCK_COLOR_NONE));
				}
			}
		}
	}

	/**
	 * @param row Row of blocks
	 * @return a String representing the row with attributes
	 */
	private String attrRowToString(Block[] row) {
		String strResult = "";

		for (Block element : row) {
			strResult += Integer.toString(element.color, 16) + "/";
			strResult += Integer.toString(element.attribute, 16) + ";";
		}

		return strResult;
	}

	/**
	 * Convert this field to a String with attributes
	 *
	 * @return a String representing the field with attributes
	 */
	public String attrFieldToString() {
		String strResult = "";

		for (int i = getHeight() - 1; i >= Math.max(-1, getHighestBlockY()); i--) {
			strResult += attrRowToString(getRow(i));
		}
		while (strResult.endsWith("0/0;")) {
			strResult = strResult.substring(0, strResult.length() - 4);
		}

		return strResult;
	}

	/**
	 * @param str
	 * @param skin
	 * @return
	 * @deprecated unused method
	 */
	@Deprecated(forRemoval = true)
	protected Block[] attrStringToRow(String str, int skin) {
		return attrStringToRow(str.split(";"), skin);
	}

	private Block[] attrStringToRow(String[] strArray, int skin) {
		Block[] row = new Block[getWidth()];

		for (int j = 0; j < getWidth(); j++) {
			int blkColor = Colors.BLOCK_COLOR_NONE;
			int attr = 0;

			try {
				String[] strSubArray = strArray[j].split("/");
				if (strSubArray.length > 0) {
					blkColor = Integer.parseInt(strSubArray[0], 16);
				}
				if (strSubArray.length > 1) {
					attr = Integer.parseInt(strSubArray[1], 16);
				}
			} catch (Exception e) {
			}

			row[j] = new Block();
			row[j].color = blkColor;
			row[j].skin = skin;
			row[j].elapsedFrames = -1;
			row[j].attribute = attr;
			row[j].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			row[j].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
		}

		return row;
	}

	public void attrStringToField(String str, int skin) {
		String[] strArray = str.split(";");

		for (int i = -1; i < getHeight(); i++) {
			int index = (getHeight() - 1 - i) * getWidth();

			try {
				String[] strArray2 = new String[getWidth()];
				for (int j = 0; j < getWidth(); j++) {
					if (index + j < strArray.length) {
						strArray2[j] = strArray[index + j];
					} else {
						strArray2[j] = "";
					}
				}

				Block[] row = attrStringToRow(strArray2, skin);
				for (int j = 0; j < getWidth(); j++) {
					setBlock(j, i, row[j]);
				}
			} catch (Exception e) {
				for (int j = 0; j < getWidth(); j++) {
					setBlock(j, i, new Block(Colors.BLOCK_COLOR_NONE));
				}
			}
		}
	}

	/**
	 * fieldGets the string representation of the
	 */
	@Override
	public String toString() {
		String str = getClass().getName() + "@" + Integer.toHexString(hashCode()) + "\n";

		for (int i = hiddenHeight * -1; i < height; i++) {
			str += String.format("%3d:", i);

			for (int j = 0; j < width; j++) {
				int color = getBlockColor(j, i);

				if (color < 0) {
					str += "*";
				} else if (color >= 10) {
					str += "+";
				} else {
					str += Integer.toString(color);
				}
			}

			str += "\n";
		}

		return str;
	}

	public int checkColor(int size, boolean flag, boolean garbageClear, boolean gemSame, boolean ignoreHidden) {
		Field temp = new Field(this);
		int total = 0;
		boolean[] colorsClearedArray = new boolean[7];
		if (flag) {
			setAllAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);
			garbageCleared = 0;
			colorClearExtraCount = 0;
			colorsCleared = 0;
			for (int i = 0; i < 7; i++) {
				colorsClearedArray[i] = false;
			}
		}

		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			for (int x = 0; x < width; x++) {
				int clear = temp.clearColor(x, y, false, garbageClear, gemSame, ignoreHidden);
				if (clear >= size) {
					total += clear;
					if (flag) {
						int blockColor = getBlockColor(x, y, gemSame);
						clearColor(x, y, true, garbageClear, gemSame, ignoreHidden);
						colorClearExtraCount += clear - size;
						if (blockColor >= 2 && blockColor <= 8) {
							colorsClearedArray[blockColor - 2] = true;
						}
					}
				}
			}
		}
		if (flag) {
			for (int i = 0; i < 7; i++) {
				if (colorsClearedArray[i]) {
					colorsCleared++;
				}
			}
		}
		return total;
	}

	public void garbageDrop(GameEngine engine, int drop, boolean big) {
		garbageDrop(engine, drop, big, 0, 0, -1, Colors.BLOCK_COLOR_GRAY);
	}

	public void garbageDrop(GameEngine engine, int drop, boolean big, int hard) {
		garbageDrop(engine, drop, big, hard, 0, -1, Colors.BLOCK_COLOR_GRAY);
	}

	public void garbageDrop(GameEngine engine, int drop, boolean big, int hard, int countdown) {
		garbageDrop(engine, drop, big, hard, countdown, -1, Colors.BLOCK_COLOR_GRAY);
	}

	public void garbageDrop(GameEngine engine, int drop, boolean big, int hard, int countdown, int avoidColumn) {
		garbageDrop(engine, drop, big, hard, countdown, avoidColumn, Colors.BLOCK_COLOR_GRAY);
	}

	private void garbageDrop(GameEngine engine, int drop, boolean big, int hard, int countdown, int avoidColumn,
			int color) {
		int y = -1 * hiddenHeight;
		int actualWidth = width;
		if (big) {
			actualWidth >>= 1;
		}
		int bigMove = big ? 2 : 1;
		while (drop >= actualWidth) {
			drop -= actualWidth;
			for (int x = 0; x < actualWidth; x += bigMove) {
				garbageDropPlace(x, y, big, hard, color, countdown);
			}
			y += bigMove;
		}
		if (drop == 0) {
			return;
		}
		boolean[] placeBlock = new boolean[actualWidth];
		int j;
		if (drop > actualWidth >> 1) {
			for (int x = 0; x < actualWidth; x++) {
				placeBlock[x] = true;
			}
			int start = actualWidth;
			if (avoidColumn >= 0 && avoidColumn < actualWidth) {
				start--;
				placeBlock[avoidColumn] = false;
			}
			for (int i = start; i > drop; i--) {
				do {
					j = engine.random.nextInt(actualWidth);
				} while (!placeBlock[j]);
				placeBlock[j] = false;
			}
		} else {
			for (int x = 0; x < actualWidth; x++) {
				placeBlock[x] = false;
			}
			for (int i = 0; i < drop; i++) {
				do {
					j = engine.random.nextInt(actualWidth);
				} while (placeBlock[j] && j != avoidColumn);
				placeBlock[j] = true;
			}
		}

		for (int x = 0; x < actualWidth; x++) {
			if (placeBlock[x]) {
				garbageDropPlace(x * bigMove, y, big, hard, color, countdown);
			}
		}
	}

	public boolean garbageDropPlace(int x, int y, boolean big, int hard) {
		return garbageDropPlace(x, y, big, hard, Colors.BLOCK_COLOR_GRAY, 0);
	}

	public boolean garbageDropPlace(int x, int y, boolean big, int hard, int color) {
		return garbageDropPlace(x, y, big, hard, color, 0);
	}

	private boolean garbageDropPlace(int x, int y, boolean big, int hard, int color, int countdown) {
		Block block = getBlock(x, y);
		if (block == null) {
			return false;
		}
		if (big) {
			garbageDropPlace(x + 1, y, false, hard);
			garbageDropPlace(x, y + 1, false, hard);
			garbageDropPlace(x + 1, y + 1, false, hard);
		}
		if (!block.isEmpty()) {
			block.color = color;
			block.setAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY, false);
			block.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
			block.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
			block.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
			block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
			block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
			block.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
			block.hard = hard;
			block.secondaryColor = 0;
			block.countdown = countdown;
			return true;
		}
		return false;
	}

	public void shuffleColors(int[] blockColors, int numColors, Random rand) {
		blockColors = blockColors.clone();
		int maxX = Math.min(blockColors.length, numColors);
		int temp, j;
		int i = maxX;
		while (i > 1) {
			j = rand.nextInt(i);
			i--;
			if (j != i) {
				temp = blockColors[i];
				blockColors[i] = blockColors[j];
				blockColors[j] = temp;
			}
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				temp = getBlockColor(x, y) - 1;
				if (numColors == 3 && temp >= 3) {
					temp--;
				}
				if (temp >= 0 && temp < maxX) {
					setBlockColor(x, y, blockColors[temp]);
				}
			}
		}
	}

	public int gemColorCheck(int size, boolean flag, boolean garbageClear, boolean ignoreHidden) {
		if (flag) {
			setAllAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);
		}

		Field temp = new Field(this);
		int total = 0;
		Block b;

		for (int y = hiddenHeight * -1; y < getHeightWithoutHurryupFloor(); y++) {
			for (int x = 0; x < width; x++) {
				b = getBlock(x, y);
				if (b == null || !b.isGemBlock()) {
					continue;
				}
				int clear = temp.clearColor(x, y, false, garbageClear, true, ignoreHidden);
				if (clear >= size) {
					total += clear;
					if (flag) {
						clearColor(x, y, true, garbageClear, true, ignoreHidden);
					}
				}
			}
		}
		return total;
	}
}

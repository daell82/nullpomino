/**
 *
 */
package mu.nu.nullpo.util;

import java.util.Random;

import lombok.experimental.UtilityClass;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * Contains various methods for modifying a {@link Field}
 *
 * @author daell
 */
@UtilityClass
public class FieldUtil {

	public static void addRandomHoverBlocks(GameEngine engine, Field field, int count, int[] colors, int minY,
			boolean avoidLines) {
		addRandomHoverBlocks(engine, field, count, colors, minY, avoidLines, false);
	}

	public static void addRandomHoverBlocks(GameEngine engine, Field field, int count, int[] colors, int minY,
			boolean avoidLines, boolean flashMode) {
		Random posRand = new Random(engine.random.nextLong());
		Random colorRand = new Random(engine.random.nextLong());
		int height = field.getHeight();
		int width = field.getWidth();
		int placeHeight = height - minY;
		int placeSize = placeHeight * width;
		boolean[][] placeBlock = new boolean[field.getWidth()][placeHeight];
		int[] colorCounts = new int[colors.length];
		for (int i = 0; i < colorCounts.length; i++) {
			colorCounts[i] = 0;
		}

		int blockColor;
		if (count < placeSize >> 1) {
			int colorShift = colorRand.nextInt(colors.length);
			int x, y;
			for (y = 0; y < placeHeight; y++) {
				for (x = 0; x < width; x++) {
					placeBlock[x][y] = false;
				}
			}
			for (int i = 0; i < count; i++) {
				x = posRand.nextInt(width);
				y = posRand.nextInt(placeHeight);
				if (!field.getBlockEmpty(x, y + minY)) {
					i--;
				} else {
					blockColor = (i + colorShift) % colors.length;
					colorCounts[blockColor]++;
					addHoverBlock(x, y + minY, colors[blockColor], field);
					placeBlock[x][y] = true;
				}
			}
		} else {
			int x, y;
			for (y = 0; y < placeHeight; y++) {
				for (x = 0; x < width; x++) {
					placeBlock[x][y] = true;
				}
			}
			for (int i = placeSize; i > count; i--) {
				x = posRand.nextInt(width);
				y = posRand.nextInt(placeHeight);
				if (placeBlock[x][y]) {
					placeBlock[x][y] = false;
				} else {
					i++;
				}
			}
			for (y = 0; y < placeHeight; y++) {
				for (x = 0; x < width; x++) {
					if (placeBlock[x][y]) {
						blockColor = colorRand.nextInt(colors.length);
						colorCounts[blockColor]++;
						addHoverBlock(x, y + minY, colors[blockColor], field);
					}
				}
			}
		}
		if (!avoidLines || colors.length == 1) {
			return;
		}
		int colorUp, colorLeft, cIndex;
		for (int y = minY; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (placeBlock[x][y - minY]) {
					colorUp = field.getBlockColor(x, y - 2);
					colorLeft = field.getBlockColor(x - 2, y);
					blockColor = field.getBlockColor(x, y);
					if (blockColor != colorUp && blockColor != colorLeft) {
						continue;
					}

					cIndex = -1;
					for (int i = 0; i < colorCounts.length; i++) {
						if (colors[i] == blockColor) {
							cIndex = i;
							break;
						}
					}

					if (colors.length == 2) {
						if (colors[0] == colorUp && colors[1] != colorLeft
								|| colors[0] == colorLeft && colors[1] != colorUp) {
							colorCounts[1]++;
							colorCounts[cIndex]--;
							field.setBlockColor(x, y, colors[1]);
						} else if (colors[1] == colorUp && colors[0] != colorLeft
								|| colors[1] == colorLeft && colors[0] != colorUp) {
							colorCounts[0]++;
							colorCounts[cIndex]--;
							field.setBlockColor(x, y, colors[0]);
						}
					} else {
						int newColor;
						do {
							newColor = colorRand.nextInt(colors.length);
						} while (colors[newColor] == colorUp || colors[newColor] == colorLeft);
						colorCounts[cIndex]--;
						colorCounts[newColor]++;
						field.setBlockColor(x, y, colors[newColor]);
					}
				}
			}
		}
		boolean[] canSwitch = new boolean[colors.length];
		int minCount = count / colors.length;
		int maxCount = (count + colors.length - 1) / colors.length;
		boolean done = true;
		for (int colorCount : colorCounts) {
			if (colorCount > maxCount) {
				done = false;
				break;
			}
		}
		int colorSide, bestSwitch, bestSwitchCount;
		int excess = 0;
		boolean fill = false;
		while (!done) {
			done = true;
			for (int y = minY; y < height; y++) {
				for (int x = 0; x < width; x++) {
					blockColor = field.getBlockColor(x, y);
					fill = blockColor == Colors.BLOCK_COLOR_NONE;
					cIndex = -1;
					if (!fill) {
						if (!placeBlock[x][y - minY]) {
							continue;
						}
						for (int i = 0; i < colorCounts.length; i++) {
							if (colors[i] == blockColor) {
								cIndex = i;
								break;
							}
						}
						if (cIndex == -1) {
							continue;
						}
						if (colorCounts[cIndex] <= maxCount) {
							continue;
						}
					}
					for (int i = 0; i < colorCounts.length; i++) {
						canSwitch[i] = colorCounts[i] < maxCount;
					}

					colorSide = field.getBlockColor(x, y - 2);
					for (int i = 0; i < colors.length; i++) {
						if (colors[i] == colorSide) {
							canSwitch[i] = false;
							break;
						}
					}
					colorSide = field.getBlockColor(x, y + 2);
					for (int i = 0; i < colors.length; i++) {
						if (colors[i] == colorSide) {
							canSwitch[i] = false;
							break;
						}
					}
					colorSide = field.getBlockColor(x - 2, y);
					for (int i = 0; i < colors.length; i++) {
						if (colors[i] == colorSide) {
							canSwitch[i] = false;
							break;
						}
					}
					colorSide = field.getBlockColor(x + 2, y);
					for (int i = 0; i < colors.length; i++) {
						if (colors[i] == colorSide) {
							canSwitch[i] = false;
							break;
						}
					}
					bestSwitch = -1;
					bestSwitchCount = Integer.MAX_VALUE;
					for (int i = 0; i < colorCounts.length; i++) {
						if (canSwitch[i] && colorCounts[i] < bestSwitchCount) {
							bestSwitch = i;
							bestSwitchCount = colorCounts[i];
						}
					}
					if (bestSwitch != -1) {
						if (fill) {
							excess++;
							addHoverBlock(x, y, colors[bestSwitch], field);
							placeBlock[x][y - minY] = true;
						} else {
							colorCounts[cIndex]--;
							field.setBlockColor(x, y, colors[bestSwitch]);
						}
						colorCounts[bestSwitch]++;
						done = false;
					}
				}
			}
			while (excess > 0) {
				int x = posRand.nextInt(width);
				int y = posRand.nextInt(placeHeight) + minY;
				if (!placeBlock[x][y - minY]) {
					continue;
				}
				blockColor = field.getBlockColor(x, y);
				for (int i = 0; i < colors.length; i++) {
					if (colors[i] == blockColor) {
						if (colorCounts[i] > minCount) {
							field.setBlockColor(x, y, Colors.BLOCK_COLOR_NONE);
							colorCounts[i]--;
							excess--;
							placeBlock[x][y - minY] = false;
						}
						break;
					}
				}
			}
			boolean balanced = true;
			for (int colorCount : colorCounts) {
				if (colorCount > maxCount) {
					balanced = false;
					break;
				}
			}
			if (balanced) {
				done = true;
			}
		}
		if (!flashMode) {
			return;
		}
		done = true;
		boolean[] gemNeeded = new boolean[colors.length];
		for (int i = 0; i < colors.length; i++) {
			if (colors[i] >= 2 && colors[i] <= 8 && colorCounts[i] > 0) {
				gemNeeded[i] = true;
				done = false;
			} else {
				gemNeeded[i] = false;
			}
		}
		while (!done) {
			int x = posRand.nextInt(width);
			int y = posRand.nextInt(placeHeight) + minY;
			if (!placeBlock[x][y - minY]) {
				continue;
			}
			blockColor = field.getBlockColor(x, y);
			for (int i = 0; i < colors.length; i++) {
				if (colors[i] == blockColor) {
					if (gemNeeded[i]) {
						field.setBlockColor(x, y, blockColor + 7);
						gemNeeded[i] = false;
					}
					break;
				}
			}
			done = true;
			for (int i = 0; i < colors.length; i++) {
				if (gemNeeded[i]) {
					done = false;
				}
			}
		}
	}

	public static boolean addHoverBlock(int x, int y, int color, Field field) {
		Block b = field.getBlock(x, y);
		if (b == null) {
			return false;
		}
		b.color = color;
		b.setAttribute(Block.BLOCK_ATTRIBUTE_ANTIGRAVITY, true);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_BROKEN, true);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_UP, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_DOWN, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_LEFT, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_CONNECT_RIGHT, false);
		b.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);
		return true;
	}

}

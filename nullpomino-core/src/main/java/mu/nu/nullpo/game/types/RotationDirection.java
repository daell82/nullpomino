/**
 *
 */
package mu.nu.nullpo.game.types;

import lombok.RequiredArgsConstructor;

/**
 *
 */
@RequiredArgsConstructor
public enum RotationDirection {

	UP(0),
	RIGHT(1),
	DOWN(2),
	LEFT(3);

	final int direction;

	public RotationDirection rotateLeft() {
		return switch(this) {
		case UP -> LEFT;
		case LEFT -> DOWN;
		case DOWN -> RIGHT;
		case RIGHT -> UP;
		};
	}

	public RotationDirection rotateRight() {
		return switch(this) {
		case UP -> RIGHT;
		case RIGHT -> DOWN;
		case DOWN -> LEFT;
		case LEFT -> UP;
		};
	}

	public RotationDirection rotateDouble() {
		return switch(this) {
		case UP -> DOWN;
		case DOWN -> UP;
		case RIGHT -> LEFT;
		case LEFT -> RIGHT;
		};
	}


}

package mu.nu.nullpo.game.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PieceTest {

	@Test
	void testPieceI_Widths() {
		Piece piece = new Piece(Piece.PIECE_I);

		assertEquals(Piece.DIRECTION_UP, piece.direction);
		assertEquals(4, piece.getWidth());
		assertEquals(0, piece.getMinimumBlockX());
		assertEquals(3, piece.getMaximumBlockX());

		piece.direction = Piece.DIRECTION_RIGHT;
		assertEquals(1, piece.getWidth());
		assertEquals(2, piece.getMinimumBlockX());
		assertEquals(2, piece.getMaximumBlockX());

		piece.big = true;
		piece.direction = Piece.DIRECTION_UP;
		assertEquals(8, piece.getWidth());
		assertEquals(0, piece.getMinimumBlockX());
		assertEquals(6, piece.getMaximumBlockX());

		piece.direction = Piece.DIRECTION_RIGHT;
		assertEquals(2, piece.getWidth());
		assertEquals(4, piece.getMinimumBlockX());
		assertEquals(4, piece.getMaximumBlockX());

	}

	@Test
	void testPieceI_Heights() {
		Piece piece = new Piece(Piece.PIECE_I);

		assertEquals(Piece.DIRECTION_UP, piece.direction);
		assertEquals(1, piece.getHeight());
		assertEquals(1, piece.getMinimumBlockY());
		assertEquals(1, piece.getMaximumBlockY());

		piece.direction = Piece.DIRECTION_RIGHT;
		assertEquals(4, piece.getHeight());
		assertEquals(0, piece.getMinimumBlockY());
		assertEquals(3, piece.getMaximumBlockY());

		piece.big = true;
		piece.direction = Piece.DIRECTION_UP;
		assertEquals(2, piece.getHeight());
		assertEquals(2, piece.getMinimumBlockY());
		assertEquals(2, piece.getMaximumBlockY());

		piece.direction = Piece.DIRECTION_RIGHT;
		assertEquals(8, piece.getHeight());
		assertEquals(0, piece.getMinimumBlockY());
		assertEquals(6, piece.getMaximumBlockY());

	}

}

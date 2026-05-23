package net.omegaboshi.nullpomino.game.subsystem.randomizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.Piece;

@Log4j
public class FixedSequenceRandomizer extends Randomizer {

	private int[] sequence;
	private int id = -1;

	public FixedSequenceRandomizer() {
		super();
	}

	public FixedSequenceRandomizer(boolean[] pieceEnable, long seed) {
		super(pieceEnable, seed);

	}

	@Override
	public void init() {
		StringBuilder builder = new StringBuilder();
		try (var lines = Files.lines(Paths.get("sequence.txt"))) {
			lines.forEach(builder::append);
		} catch (IOException e) {
			log.error("failed to load sequence data", e);
		}
		String data = builder.toString();
		sequence = new int[data.length()];
		for (int i = 0; i < sequence.length; i++) {
			sequence[i] = pieceCharToId(data.charAt(i));
		}
		log.debug("obtained sequence: " + Arrays.toString(sequence));
	}

	private int pieceCharToId(char c) {
		int i = 0;
		for (i = 0; i < Piece.PIECE_STANDARD_COUNT; i++) {
			if (c == Piece.PIECE_NAMES[i].charAt(0)) {
				break;
			}
		}
		return i;
	}

	@Override
	public int next() {
		id = id + 1;
		return sequence[id % sequence.length];
	}

}

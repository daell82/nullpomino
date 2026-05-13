package net.omegaboshi.nullpomino.game.subsystem.randomizer;

public class LinearDistWeightRandomizer extends DistanceWeightRandomizer {

	public LinearDistWeightRandomizer() {
		super();
	}

	public LinearDistWeightRandomizer(boolean[] pieceEnable, long seed) {
		super(pieceEnable, seed);
	}

	@Override
	public int getWeight(int i) {
		return weights[i];
	}

	@Override
	public boolean isAtDistanceLimit(int i) {
		return false;
	}

}

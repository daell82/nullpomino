package net.omegaboshi.nullpomino.game.subsystem.randomizer;

public class QuadraticDistWeightRandomizer extends DistanceWeightRandomizer {

	public QuadraticDistWeightRandomizer() {
		super();
	}

	public QuadraticDistWeightRandomizer(boolean[] pieceEnable, long seed) {
		super(pieceEnable, seed);
	}

	@Override
	public int getWeight(int i) {
		return weights[i] * weights[i];
	}

	@Override
	public boolean isAtDistanceLimit(int i) {
		return false;
	}

}

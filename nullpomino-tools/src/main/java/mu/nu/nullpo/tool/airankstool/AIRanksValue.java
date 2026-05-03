package mu.nu.nullpo.tool.airankstool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import mu.nu.nullpo.game.subsystem.ai.util.AIRanksConstants;
import mu.nu.nullpo.game.subsystem.ai.util.Ranks;

public class AIRanksValue {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Ranks ranks;
		String inputFile = AIRanksConstants.RANKSAI_DIR + "ranks20";

		if (inputFile.trim().isEmpty()) {
			ranks = new Ranks(4, 9);
		} else {
			try (var in = new ObjectInputStream(new FileInputStream(inputFile))) {
				ranks = (Ranks) in.readObject();
				int[] surface1 = { 0, 1, 1, -1, -1, 1, -3, -2 };
				int[] surface2 = { 0, 1, 1, -1, -1, 4, -4, 2 };

				int rank1 = ranks.getRankValue(ranks.encode(surface1));
				int rank2 = ranks.getRankValue(ranks.encode(surface2));
				System.out.println(rank1);
				System.out.println(rank2);
			} catch (FileNotFoundException _) {
				ranks = new Ranks(4, 9);
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}

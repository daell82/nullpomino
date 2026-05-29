package mu.nu.nullpo.gui.menu;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class AlphaMenuItem extends NumericMenuItem {

	@SuppressWarnings("unused")
	private int index;

	@SuppressWarnings("unused")
	private List<String> choices;

	public AlphaMenuItem(String name, int color, List<String> choiceList) {
		super(name, color, 0, 0, choiceList.size(), -1, ARITHSTYLE_MODULAR);
		this.choices = choiceList;
		state = 0;
	}

	public AlphaMenuItem(String name, int color, String[] choiceList) {
		this(name, color, new Vector<>(Arrays.asList(choiceList)));
	}
}

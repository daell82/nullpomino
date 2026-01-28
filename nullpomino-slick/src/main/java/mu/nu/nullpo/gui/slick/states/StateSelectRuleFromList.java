package mu.nu.nullpo.gui.slick.states;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import mu.nu.nullpo.gui.slick.DummyMenuScrollState;
import mu.nu.nullpo.gui.slick.NormalFontSlick;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.ResourceHolderSlick;
import mu.nu.nullpo.util.CustomProperties;

/**
 * Rule select (after mode selection)
 */
public class StateSelectRuleFromList extends DummyMenuScrollState {
	/** Log */
	static Logger log = Logger.getLogger(StateSelectRuleFromList.class);

	/** This state's ID */
	public static final int ID = 18;

	/** Number of rules in one page */
	public static final int PAGE_HEIGHT = 24;

	/** HashMap of rules (ModeName->RuleEntry) */
	protected Map<String, RuleEntry> ruleEntries;

	/** Current mode */
	protected String currentMode;

	/**
	 * Constructor
	 */
	public StateSelectRuleFromList() {
		super();
		pageHeight = PAGE_HEIGHT;
	}

	/*
	 * Fetch this state's ID
	 */
	@Override
	public int getID() {
		return ID;
	}

	/*
	 * State initialization
	 */
	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		loadRecommendedRuleList();
	}

	/**
	 * Load list file
	 */
	protected void loadRecommendedRuleList() {
		ruleEntries = new HashMap<>();

		try {
			BufferedReader in = new BufferedReader(new FileReader("config/list/recommended_rules.lst"));
			String strMode = "";

			String str;
			while ((str = in.readLine()) != null) {
				str = str.trim(); // Trim the space

				if (str.startsWith("#")) {
					// Commment-line. Ignore it.
				} else if (str.startsWith(":")) {
					// Mode change
					strMode = str.substring(1);
				} else {
					// File Path
					File file = new File(str);
					if (file.exists() && file.isFile()) {
						try {
							FileInputStream ruleIn = new FileInputStream(file);
							CustomProperties propRule = new CustomProperties();
							propRule.load(ruleIn);
							ruleIn.close();

							String strRuleName = propRule.getProperty("0.ruleopt.strRuleName", "");
							if (strRuleName.length() > 0) {
								RuleEntry entry = ruleEntries.get(strMode);
								if (entry == null) {
									entry = new RuleEntry();
									ruleEntries.put(strMode, entry);
								}
								entry.listName.add(strRuleName);
								entry.listPath.add(str);
							}
						} catch (IOException e2) {
							log.error("File " + str + " doesn't exist", e2);
						}
					}
				}
			}

			in.close();
		} catch (IOException e) {
			log.error("Failed to load recommended rules list", e);
		}
	}

	/**
	 * Prepare rule list
	 */
	protected void prepareRuleList() {
		currentMode = NullpoMinoSlick.propGlobal.getProperty("name.mode", "");
		if (currentMode != null) {
			RuleEntry entry = ruleEntries.get(currentMode);
			if (entry == null) {
				list = Arrays.asList("(CURRENT RULE)");
			} else {
				list = new ArrayList<>(entry.listName.size() + 1);
				list.addFirst("(CURRENT RULE)");
				list.addAll(entry.listName);
				maxCursor = list.size() - 1;
			}
		} else {
			list = Arrays.asList("(CURRENT RULE)");
			maxCursor = 0;
		}

		int defaultCursor = 0;
		String strLastRule = NullpoMinoSlick.propGlobal.getProperty("lastrule." + currentMode);
		if (strLastRule != null && !strLastRule.isEmpty()) {
			defaultCursor = list.indexOf(strLastRule);
		}
		cursor = defaultCursor;
	}

	/*
	 * When the player enters this state
	 */
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		prepareRuleList();
	}

	/*
	 * Render screen
	 */
	@Override
	protected void onRenderSuccess(GameContainer container, StateBasedGame game, Graphics graphics) {
		NormalFontSlick.printFontGrid(1, 1, currentMode + " (" + (cursor + 1) + "/" + list.size() + ")",
				NormalFontSlick.COLOR_ORANGE);
	}

	/*
	 * Decide
	 */
	@Override
	protected boolean onDecide(GameContainer container, StateBasedGame game, int delta) {
		ResourceHolderSlick.soundManager.play("decide");
		if (cursor >= 1) {
			NullpoMinoSlick.propGlobal.setProperty("lastrule." + currentMode, list.get(cursor));
		} else {
			NullpoMinoSlick.propGlobal.setProperty("lastrule." + currentMode, "");
		}
		NullpoMinoSlick.saveConfig();

		String strRulePath = null;
		if (cursor >= 1) {
			RuleEntry entry = ruleEntries.get(currentMode);
			strRulePath = entry.listPath.get(cursor - 1);
		}

		NullpoMinoSlick.stateInGame.startNewGame(strRulePath);
		game.enterState(StateInGame.ID);
		return false;
	}

	/*
	 * Cancel
	 */
	@Override
	protected boolean onCancel(GameContainer container, StateBasedGame game, int delta) {
		game.enterState(StateSelectMode.ID);
		return false;
	}

	/**
	 * RuleEntry
	 */
	protected class RuleEntry {
		public LinkedList<String> listPath = new LinkedList<>();
		public LinkedList<String> listName = new LinkedList<>();
	}
}

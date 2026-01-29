package mu.nu.nullpo.gui.slick.states;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import mu.nu.nullpo.game.types.GameStyle;
import mu.nu.nullpo.gui.slick.DummyMenuChooseState;
import mu.nu.nullpo.gui.slick.NormalFontSlick;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.ResourceHolderSlick;
import mu.nu.nullpo.util.Colors;

/**
 * Style select menu
 */
public class StateConfigRuleStyleSelect extends DummyMenuChooseState {
	/** This state's ID */
	public static final int ID = 15;

	/** Player number */
	protected int player = 0;

	public StateConfigRuleStyleSelect() {
		super();
		maxCursor = GameStyle.numStyles() - 1;
		minChoiceY = 3;
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
	}

	/*
	 * Draw the screen
	 */
	@Override
	protected void renderImpl(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		// Background
		g.drawImage(ResourceHolderSlick.imgMenu, 0, 0);

		// Menu
		NormalFontSlick.printFontGrid(1, 1, "SELECT " + (player+1) + "P STYLE", Colors.FONT_ORANGE);
		NormalFontSlick.printFontGrid(1, 3 + cursor, "b", Colors.FONT_RED);

		for(GameStyle style : GameStyle.values()) {
			int i = style.getMode();
			NormalFontSlick.printFontGrid(2, 3 + i, style.getName(), cursor == i);
		}
	}

	/*
	 * Decide
	 */
	@Override
	protected boolean onDecide(GameContainer container, StateBasedGame game, int delta) {
		ResourceHolderSlick.soundManager.play("decide");
		NullpoMinoSlick.stateConfigRuleSelect.player = player;
		NullpoMinoSlick.stateConfigRuleSelect.style = cursor;
		game.enterState(StateConfigRuleSelect.ID);
		return false;
	}

	/*
	 * Cancel
	 */
	@Override
	protected boolean onCancel(GameContainer container, StateBasedGame game, int delta) {
		game.enterState(StateConfigMainMenu.ID);
		return false;
	}
}

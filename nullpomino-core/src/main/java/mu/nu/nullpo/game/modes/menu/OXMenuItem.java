package mu.nu.nullpo.game.modes.menu;

import mu.nu.nullpo.util.GeneralUtil;

public class OXMenuItem extends BooleanMenuItem {
	public OXMenuItem(String name, String displayName, int color, boolean defaultValue) {
		super(name, displayName, color, defaultValue);
	}

	@Override
	public String getValueString() {
		return GeneralUtil.getOorX(value);
	}
}

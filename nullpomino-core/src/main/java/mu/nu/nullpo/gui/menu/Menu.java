package mu.nu.nullpo.gui.menu;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class Menu {

	@SuppressWarnings("unused")
	private final String title;

	@SuppressWarnings("unused")
	private final String subTitle;

	private int selectedIndex;

	private final List<MenuItem> menuItems = new LinkedList<>();

	public Menu(String title, String subTitle, List<MenuItem> menuItems) {
		this.title = title;
		this.subTitle = subTitle;
		this.menuItems.addAll(menuItems);
		selectedIndex = 0;
	}

	public Menu(String title, String subTitle) {
		this(title, subTitle, new Vector<>());
	}

	public Menu(String title, List<MenuItem> menuItems) {
		this(title, "", menuItems);
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	public void addMenuItem(MenuItem menuItem) {
		menuItems.add(menuItem);

	}

	public void incIndex() {
		if (selectedIndex <= menuItems.size() - 2) {
			selectedIndex++;
		}
	}

	public void decIndex() {
		if (selectedIndex >= 1) {
			selectedIndex--;
		}
	}

}

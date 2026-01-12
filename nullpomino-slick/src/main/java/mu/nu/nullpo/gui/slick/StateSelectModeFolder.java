package mu.nu.nullpo.gui.slick;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.util.Colors;

/**
 * Mode folder select
 */
@Log4j
public class StateSelectModeFolder extends DummyMenuScrollState {

	/** This state's ID */
	public static final int ID = 19;

	/** Number of folders in one page */
	public static final int PAGE_HEIGHT = 24;

	/** Top-level mode list */
	public static final List<String> topLevelModes = new LinkedList<>();

	/** Folder names list */
	public static final List<String> folders = new LinkedList<>();

	/** Map of mode folder (FolderName->ModeNames) */
	public static final Map<String, List<String>> mapFolder = new HashMap<>();

	/** Current folder name */
	public static String currentFolder;

	/**
	 * Constructor
	 */
	public StateSelectModeFolder() {
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
		loadFolderListFile();
		prepareFolderList();
	}

	/**
	 * Load folder list file
	 */
	public void loadFolderListFile() {
		topLevelModes.clear();
		folders.clear();
		mapFolder.clear();

		currentFolder = NullpoMinoSlick.propGlobal.getProperty("name.folder", "");
		List<String> lines;
		try {
			lines = Files.readAllLines(new File("config/list/modefolder.lst").toPath());
		} catch (IOException e) {
			log.error("Failed to load mode folder list file", e);
			return;
		}
		String folder = "";
		for (String line : lines) {
			line = line.trim(); // Trim the space
			if (line.startsWith("#")) {
				// Commment-line. Ignore it.
			} else if (line.startsWith(":")) {
				// New folder
				folder = line.substring(1);
				if (!folders.contains(folder)) {
					folders.add(folder);
					mapFolder.put(folder, new LinkedList<>());
				}
			} else if (!line.isEmpty()) {
				// Mode name
				if (folder.isEmpty()) {
					log.debug("(top-level)." + line);
					topLevelModes.add(line);
				} else {
					List<String> listMode = mapFolder.get(folder);
					if (listMode != null && !listMode.contains(line)) {
						log.debug(folder + "." + line);
						listMode.add(line);
					}
				}
			}
		}
	}

	/**
	 * Prepare folder list
	 */
	protected void prepareFolderList() {
		list = new ArrayList<>(folders.size() + 1);

		for (int i = 0; i < folders.size(); i++) {
			String folder = folders.get(i);
			list.add(i, folder);
			if (currentFolder.equals(folder)) {
				cursor = i;
			}
		}
		list.add("[ALL MODES]");
		maxCursor = list.size() - 1;
	}

	/**
	 * Get folder description
	 *
	 * @param str Folder name
	 * @return Description
	 */
	protected String getFolderDesc(String str) {
		String str2 = str.replace(' ', '_');
		str2 = str2.replace('(', 'l');
		str2 = str2.replace(')', 'r');
		String result = NullpoMinoSlick.propModeDesc.getProperty("Folder_" + str2);
		if (result == null) {
			result = NullpoMinoSlick.propDefaultModeDesc.getProperty("Folder_" + str2, "Folder_" + str2);
		}
		return result;
	}

	/*
	 * Render screen
	 */
	@Override
	protected void onRenderSuccess(GameContainer container, StateBasedGame game, Graphics graphics) {
		NormalFontSlick.printFontGrid(1, 1, "SELECT MODE FOLDER (" + (cursor + 1) + "/" + list.size() + ")",
				Colors.FONT_ORANGE);
		NormalFontSlick.printTTFFont(16, 440, getFolderDesc(list.get(cursor)));
	}

	/*
	 * Decide
	 */
	@Override
	protected boolean onDecide(GameContainer container, StateBasedGame game, int delta) {
		ResourceHolderSlick.soundManager.play("decide");
		if (cursor < folders.size()) {
			currentFolder = list.get(cursor);
		} else {
			currentFolder = "";
		}
		NullpoMinoSlick.propGlobal.setProperty("name.folder", currentFolder);
		NullpoMinoSlick.saveConfig();
		StateSelectMode.isTopLevel = false;
		game.enterState(StateSelectMode.ID);
		return false;
	}

	/*
	 * Cancel
	 */
	@Override
	protected boolean onCancel(GameContainer container, StateBasedGame game, int delta) {
		StateSelectMode.isTopLevel = true;
		game.enterState(StateSelectMode.ID);
		return false;
	}
}

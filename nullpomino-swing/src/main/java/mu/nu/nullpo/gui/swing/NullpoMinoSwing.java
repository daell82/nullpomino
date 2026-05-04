/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package mu.nu.nullpo.gui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.PropertyConfigurator;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.net.NetBaseClient;
import mu.nu.nullpo.game.net.NetObserverClient;
import mu.nu.nullpo.game.net.NetPlayerClient;
import mu.nu.nullpo.game.net.NetRoomInfo;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;
import mu.nu.nullpo.game.subsystem.mode.GameMode;
import mu.nu.nullpo.game.subsystem.mode.NetDummyMode;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;
import mu.nu.nullpo.game.types.GameStyle;
import mu.nu.nullpo.game.types.Version;
import mu.nu.nullpo.gui.net.NetLobbyFrame;
import mu.nu.nullpo.gui.net.NetLobbyListener;
import mu.nu.nullpo.gui.net.UpdateChecker;
import mu.nu.nullpo.gui.net.UpdateCheckerListener;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import mu.nu.nullpo.util.ModeManager;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

/**
 * NullpoMino SwingVersion
 */
@Log4j
public class NullpoMinoSwing extends JFrame implements ActionListener, NetLobbyListener, UpdateCheckerListener {

	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Of the game window frame */
	private GameFrame gameFrame;

	/** Key Configuration screen frame */
	private KeyConfigFrame keyConfigFrame;

	/** Rules of selection screen frame */
	private RuleSelectFrame ruleSelectFrame;

	/** AISelection screen frame */
	private AISelectFrame aiSelectFrame;

	/** Other Settings screen frame */
	private GeneralConfigFrame generalConfigFrame;

	/** Tuning Settings screen frame */
	private GameTuningFrame gameTuningFrame;

	/** Update check Setting screen frame */
	private UpdateCheckFrame updateCheckFrame;

	/** Command that was passed to the programLinesArgumentcount */
	private static String[] programArgs;

	/** Save settingsUseProperty file */
	public static CustomProperties propConfig;

	/** Save settingsUseProperty file (AllVersionCommon) */
	public static CustomProperties propGlobal;

	/** ObserverFor the functionProperty file */
	private static CustomProperties propObserver;

	/** Default language file */
	private static CustomProperties propLangDefault;

	/** Language file */
	private static CustomProperties propLang;

	/** Default game mode description file */
	private static CustomProperties propDefaultModeDesc;

	/** Game mode description file */
	private static CustomProperties propModeDesc;

	/** Mode Management */
	private ModeManager modeManager;

	/** The main class of the game */
	@Getter
	private GameManager gameManager;

	/** GameMode nameAn array of */
	private List<String> modeList;

	/** Mode Selection list box */
	private JList<String> listboxMode;

	/** Rule select listmodel */
	private DefaultListModel<String> listmodelRule;

	/** Rule select listbox */
	private JList<String> listboxRule;

	/** Replay file selection dialog */
	private JFileChooser replayFileChooser;

	/** Lobby screen */
	public NetLobbyFrame netLobby;

	/** ObserverClient */
	private NetObserverClient netObserverClient;

	/** Mode Select the on-screen label(NewVersionIf there is it switches) */
	private JLabel lModeSelect;

	/** HashMap of rules (ModeName->RuleEntry) */
	protected Map<String, RuleEntry> ruleEntries;

	/**
	 * Main functioncount
	 *
	 * @param args Command that was passed to the programLinesArgumentcount
	 */
	public static void main(String[] args) {
		programArgs = args;

		PropertyConfigurator.configure("config/etc/log_swing.cfg");
		log.debug("NullpoMinoSwing Start");

		// Read configuration file
		propConfig = load("config/setting/swing.cfg");
		propGlobal = new CustomProperties();
		loadGlobalConfig();

		// Read language file
		propLangDefault = load("config/lang/swing_default.properties");
		propLang = load("config/lang/swing_" + Locale.getDefault().getCountry() + ".properties");
		// Game mode description
		propDefaultModeDesc = load("config/lang/modedesc_default.properties");
		propModeDesc = load("config/lang/modedesc_" + Locale.getDefault().getCountry() + ".properties");
		// Set default rule selections
		CustomProperties propDefaultRule = load("config/list/global_defaultrule.properties");
		for (int pl = 0; pl < 2; pl++) {
			for (int i = 0; i < GameStyle.numStyles(); i++) {
				// TETROMINO
				if (i == 0) {
					if (propGlobal.getProperty(pl + ".rule") == null) {
						propGlobal.setProperty(pl + ".rule", propDefaultRule.getProperty("default.rule", ""));
						propGlobal.setProperty(pl + ".rulefile", propDefaultRule.getProperty("default.rulefile", ""));
						propGlobal.setProperty(pl + ".rulename", propDefaultRule.getProperty("default.rulename", ""));
					}
				} else if (propGlobal.getProperty(pl + ".rule." + i) == null) {
					propGlobal.setProperty(pl + ".rule." + i, propDefaultRule.getProperty("default.rule." + i, ""));
					propGlobal.setProperty(pl + ".rulefile." + i,
							propDefaultRule.getProperty("default.rulefile." + i, ""));
					propGlobal.setProperty(pl + ".rulename." + i,
							propDefaultRule.getProperty("default.rulename." + i, ""));
				}
			}
		}

		// Load keyboard settings
		GameKeySwing.initGlobalGameKeySwing();
		GameKeySwing.gamekey[0].loadConfig(propConfig);
		GameKeySwing.gamekey[1].loadConfig(propConfig);

		// Look&Feel
		if (propConfig.getProperty("option.usenativelookandfeel", true)) {
			try {
				UIManager.getInstalledLookAndFeels();
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				log.warn("Failed to set native look&feel", e);
			}
		}

		// Load images
		try {
			ResourceHolderSwing resourceManager = ResourceHolderSwing.getInstance();
			String skinDir = propConfig.getProperty("custom.skin.directory", "res");
			resourceManager.load(skinDir);
		} catch (Exception e) {
			log.error("Image load failed", e);
		}

		// First run?
		if (propConfig.getProperty("option.firstSetupMode", true)) {
			// Set various default settings here
			GameKeySwing.gamekey[0].loadDefaultKeymap();
			GameKeySwing.gamekey[0].saveConfig(propConfig);
			propConfig.setProperty("option.firstSetupMode", false);

			// Set default rotation button setting (only for first run)
			if (propGlobal.getProperty("global.firstSetupMode", true)) {
				for (int pl = 0; pl < 2; pl++) {
					if (propGlobal.getProperty(pl + ".tuning.owRotateButtonDefaultRight") == null) {
						propGlobal.setProperty(pl + ".tuning.owRotateButtonDefaultRight", 0);
					}
				}
				propGlobal.setProperty("global.firstSetupMode", false);
			}

			// Save settings
			saveConfig();
		}

		// Create and display main window
		SwingUtilities.invokeLater(NullpoMinoSwing::new);
	}

	static CustomProperties load(String filename) {
		var result = new CustomProperties();
		File file = new File(filename);
		if (file.exists()) {
			try (var in = new FileInputStream(file)) {
				result.load(in);
			} catch (IOException e) {
				log.error("Couldn't load properties from " + filename, e);
			}
		}
		return result;
	}

	/**
	 * PosttranslationalUIGets a string of
	 *
	 * @param str String
	 * @return PosttranslationalUIString (If you do not acceptstrReturns)
	 */
	public static String getUIText(String str) {
		String result = propLang.getProperty(str);
		if (result == null) {
			result = propLangDefault.getProperty(str, str);
		}
		return result;
	}

	/**
	 * Save the configuration file
	 */
	public static void saveConfig() {
		try {
			propConfig.save("config/setting/swing.cfg", "NullpoMino Swing-frontend Config");
			propGlobal.save("config/setting/global.cfg", "NullpoMino Global Config");
		} catch (IOException e) {
			log.error("Failed to save Swing-specific config", e);
		}
	}

	/**
	 * (Re-)Load global config file
	 */
	public static void loadGlobalConfig() {
		try {
			FileInputStream in = new FileInputStream("config/setting/global.cfg");
			propGlobal.load(in);
			in.close();
		} catch (IOException e) {
		}
	}

	/**
	 * TextfieldFromintGets the value of the type
	 *
	 * @param value  TextfieldValue when Failed to get the value from
	 * @param txtfld Textfield
	 * @return TextfieldIf you can get the value from its value, FailedvalueReturns
	 *         the raw
	 */
	public static int getIntTextField(int value, JTextField txtfld) {
		int v = value;

		try {
			v = Integer.parseInt(txtfld.getText());
		} catch (NumberFormatException e) {
		}

		return v;
	}

	/**
	 * TextfieldFromdoubleGets the value of the type
	 *
	 * @param value  TextfieldValue when Failed to get the value from
	 * @param txtfld Textfield
	 * @return TextfieldIf you can get the value from its value, FailedvalueReturns
	 *         the raw
	 */
	public static double getDoubleTextField(double value, JTextField txtfld) {
		double v = value;

		try {
			v = Double.parseDouble(txtfld.getText());
		} catch (NumberFormatException e) {
		}

		return v;
	}

	/**
	 * TextfieldFromfloatGets the value of the type
	 *
	 * @param value  TextfieldValue when Failed to get the value from
	 * @param txtfld Textfield
	 * @return TextfieldIf you can get the value from its value, FailedvalueReturns
	 *         the raw
	 */
	public static float getFloatTextField(float value, JTextField txtfld) {
		float v = value;

		try {
			v = Float.parseFloat(txtfld.getText());
		} catch (NumberFormatException e) {
		}

		return v;
	}

	/**
	 * Get game mode description
	 *
	 * @param str Mode name
	 * @return Description
	 */
	protected static String getModeDesc(String str) {
		String str2 = str.replace(' ', '_');
		str2 = str2.replace('(', 'l');
		str2 = str2.replace(')', 'r');
		String result = propModeDesc.getProperty(str2);
		if (result == null) {
			result = propDefaultModeDesc.getProperty(str2, str2);
		}
		return result;
	}

	/**
	 * Constructor
	 *
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if
	 *                           there is no
	 */
	public NullpoMinoSwing() throws HeadlessException {
		super();

		// ModeRead
		modeManager = new ModeManager();
		modeManager.loadGameModes("config/list/mode.lst");
		modeList = modeManager.getNormalModeNames();

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				shutdown();
			}
		});

		setTitle(getUIText("Title_Main") + " version " + Version.getCurrent());
		loadRecommendedRuleList();

		initUI();
		pack();

		int width = propConfig.getProperty("mainwindow.width", 500);
		int height = propConfig.getProperty("mainwindow.height", 470);
		int x = propConfig.getProperty("mainwindow.x", 0);
		int y = propConfig.getProperty("mainwindow.y", 0);

		setBounds(x, y, width, height);
		setVisible(true);

		// NewVersion check
		if (propGlobal.getProperty("updatechecker.enable", true)) {
			int startupCount = propGlobal.getProperty("updatechecker.startupCount", 0);
			int startupMax = propGlobal.getProperty("updatechecker.startupMax", 20);

			if (startupCount >= startupMax) {
				String strURL = propGlobal.getProperty("updatechecker.url", "");
				UpdateChecker.addListener(this);
				UpdateChecker.startCheckForUpdates(strURL);
				startupCount = 0;
			} else {
				startupCount++;
			}

			if (startupMax >= 1) {
				propGlobal.setProperty("updatechecker.startupCount", startupCount);
				saveConfig();
			}
		}

		// CommandLinesReplay from reproduction
		if (programArgs != null && programArgs.length > 0) {
			startReplayGame(programArgs[0]);
		}
	}

	/**
	 * GUIOfInitialization
	 */
	protected void initUI() {
		setLayout(new CardLayout());

		// Top screen
		JPanel panelTop = new JPanel();
		initTopScreenUI(panelTop);
		this.add(panelTop, "top");
	}

	/**
	 * Init top screen
	 */
	protected void initTopScreenUI(JComponent parent) {
		parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));

		// Label
		lModeSelect = new JLabel(getUIText("Top_ModeSelect"));
		lModeSelect.setAlignmentX(0f);
		parent.add(lModeSelect);

		// Mode & rule select panel
		JPanel subpanelModeSelect = new JPanel(new BorderLayout());
		subpanelModeSelect.setBorder(new EtchedBorder());
		subpanelModeSelect.setAlignmentX(0f);
		parent.add(subpanelModeSelect);

		// * Mode select listbox
		listboxMode = new JList<>(modeList.toArray(i -> new String[i]));
		listboxMode.addMouseListener(new ListboxModeMouseAdapter());
		listboxMode.addListSelectionListener(_ -> {
			String mode = listboxMode.getSelectedValue();
			lModeSelect.setText(getModeDesc(mode));
			prepareRuleList(mode);
		});

		JScrollPane scpaneListboxMode = new JScrollPane(listboxMode);
		scpaneListboxMode.setPreferredSize(new Dimension(280, 375));
		subpanelModeSelect.add(scpaneListboxMode, BorderLayout.WEST);

		// * Rule select listbox
		listmodelRule = new DefaultListModel<>();
		listboxRule = new JList<>(listmodelRule);
		listboxRule.addMouseListener(new ListboxModeMouseAdapter());
		JScrollPane scpaneListBoxRule = new JScrollPane(listboxRule);
		scpaneListBoxRule.setPreferredSize(new Dimension(150, 375));
		subpanelModeSelect.add(scpaneListBoxRule, BorderLayout.CENTER);

		// * Set default selected index
		listboxMode.setSelectedValue(propGlobal.getProperty("name.mode", ""), true);
		if (listboxMode.getSelectedIndex() == -1) {
			listboxMode.setSelectedIndex(0);
		}
		prepareRuleList(listboxMode.getSelectedValue());

		// Start button
		JButton buttonStartOffline = new JButton(getUIText("Top_StartOffline"));
		buttonStartOffline.setMnemonic('S');
		buttonStartOffline.addActionListener(this);
		buttonStartOffline.setActionCommand("Top_StartOffline");
		buttonStartOffline.setAlignmentX(0f);
		buttonStartOffline.setMaximumSize(new Dimension(Short.MAX_VALUE, buttonStartOffline.getMaximumSize().height));
		parent.add(buttonStartOffline);
		getRootPane().setDefaultButton(buttonStartOffline);

		// Menu
		initMenu();
	}

	/**
	 * Menu OfInitialization
	 */
	protected void initMenu() {
		JMenuBar menubar = new JMenuBar();
		setJMenuBar(menubar);

		// FileMenu
		JMenu menuFile = new JMenu(getUIText("Menu_File"));
		menuFile.setMnemonic('F');
		menubar.add(menuFile);

		// Open the replay
		JMenuItem miOpen = new JMenuItem(getUIText("Menu_Open"));
		miOpen.setMnemonic('O');
		miOpen.addActionListener(this);
		miOpen.setActionCommand("Menu_Open");
		menuFile.add(miOpen);

		// NetPlay start
		JMenuItem miNetPlay = new JMenuItem(getUIText("Menu_NetPlay"));
		miNetPlay.setMnemonic('N');
		miNetPlay.addActionListener(this);
		miNetPlay.setActionCommand("Menu_NetPlay");
		menuFile.add(miNetPlay);

		// End
		JMenuItem miExit = new JMenuItem(getUIText("Menu_Exit"));
		miExit.setMnemonic('X');
		miExit.addActionListener(this);
		miExit.setActionCommand("Menu_Exit");
		menuFile.add(miExit);

		// SettingMenu
		JMenu menuConfig = new JMenu(getUIText("Menu_Config"));
		menuConfig.setMnemonic('C');
		menubar.add(menuConfig);

		// Selection rules
		JMenuItem miRuleSelect = new JMenuItem(getUIText("Menu_RuleSelect"));
		miRuleSelect.setMnemonic('R');
		miRuleSelect.addActionListener(this);
		miRuleSelect.setActionCommand("Menu_RuleSelect");
		menuConfig.add(miRuleSelect);

		// Selection rules(2P)
		JMenuItem miRuleSelect2P = new JMenuItem(getUIText("Menu_RuleSelect2P"));
		miRuleSelect2P.setMnemonic('S');
		miRuleSelect2P.addActionListener(this);
		miRuleSelect2P.setActionCommand("Menu_RuleSelect2P");
		menuConfig.add(miRuleSelect2P);

		// Tuning settings
		JMenuItem miGameTuning = new JMenuItem(getUIText("Menu_GameTuning"));
		miGameTuning.setMnemonic('T');
		miGameTuning.addActionListener(this);
		miGameTuning.setActionCommand("Menu_GameTuning");
		menuConfig.add(miGameTuning);

		// Tuning settings(2P)
		JMenuItem miGameTuning2P = new JMenuItem(getUIText("Menu_GameTuning2P"));
		miGameTuning2P.setMnemonic('U');
		miGameTuning2P.addActionListener(this);
		miGameTuning2P.setActionCommand("Menu_GameTuning2P");
		menuConfig.add(miGameTuning2P);

		// AISetting
		JMenuItem miAIConfig = new JMenuItem(getUIText("Menu_AIConfig"));
		miAIConfig.setMnemonic('A');
		miAIConfig.addActionListener(this);
		miAIConfig.setActionCommand("Menu_AIConfig");
		menuConfig.add(miAIConfig);

		// AISetting(2P)
		JMenuItem miAIConfig2P = new JMenuItem(getUIText("Menu_AIConfig2P"));
		miAIConfig2P.setMnemonic('Z');
		miAIConfig2P.addActionListener(this);
		miAIConfig2P.setActionCommand("Menu_AIConfig2P");
		menuConfig.add(miAIConfig2P);

		// Key settings
		JMenuItem miKeyConfig = new JMenuItem(getUIText("Menu_KeyConfig"));
		miKeyConfig.setMnemonic('K');
		miKeyConfig.addActionListener(this);
		miKeyConfig.setActionCommand("Menu_KeyConfig");
		menuConfig.add(miKeyConfig);

		// Key settings(2P)
		JMenuItem miKeyConfig2P = new JMenuItem(getUIText("Menu_KeyConfig2P"));
		miKeyConfig2P.setMnemonic('E');
		miKeyConfig2P.addActionListener(this);
		miKeyConfig2P.setActionCommand("Menu_KeyConfig2P");
		menuConfig.add(miKeyConfig2P);

		// Update check Setting
		JMenuItem miUpdateCheck = new JMenuItem(getUIText("Menu_UpdateCheck"));
		miUpdateCheck.setMnemonic('D');
		miUpdateCheck.addActionListener(this);
		miUpdateCheck.setActionCommand("Menu_UpdateCheck");
		menuConfig.add(miUpdateCheck);

		// Other Settings
		JMenuItem miGeneralConfig = new JMenuItem(getUIText("Menu_GeneralConfig"));
		miGeneralConfig.setMnemonic('G');
		miGeneralConfig.addActionListener(this);
		miGeneralConfig.setActionCommand("Menu_GeneralConfig");
		menuConfig.add(miGeneralConfig);
	}

	/**
	 * Load list file
	 */
	protected void loadRecommendedRuleList() {
		ruleEntries = new HashMap<>();

		try {
			List<String> lines = Files.readAllLines(Path.of("config/list/recommended_rules.lst"));
			String gameMode = "";
			for (String line : lines) {
				line = line.trim(); // Trim the space

				if (line.isBlank() || line.startsWith("#")) {
					// Commment-line. Ignore it.
				} else if (line.startsWith(":")) {
					// Mode change
					gameMode = line.substring(1);
				} else {
					// File Path
					File file = new File(line);
					if (!file.exists() || !file.isFile()) {
						continue;
					}
					CustomProperties propRule = CustomProperties.load(file);

					String ruleName = propRule.getProperty("0.ruleopt.strRuleName", "");
					if (!ruleName.isBlank()) {
						RuleEntry entry = ruleEntries.computeIfAbsent(gameMode, _ -> new RuleEntry());
						entry.names.add(ruleName);
						entry.paths.add(line);
					}
				}
			}
		} catch (IOException e) {
			log.error("Failed to load recommended rules list", e);
		}
	}

	/**
	 * Prepare rule list
	 */
	protected void prepareRuleList(String currentMode) {
		listmodelRule.clear();
		listmodelRule.addElement(getUIText("Top_CurrentRule"));
		if (currentMode != null) {
			RuleEntry entry = ruleEntries.get(currentMode);
			if (entry != null) {
				for (String name : entry.names) {
					listmodelRule.addElement(name);
				}
			}
		}

		listboxRule.setSelectedIndex(0);
		String strLastRule = propGlobal.getProperty("lastrule." + currentMode);
		if (strLastRule != null && strLastRule.length() > 0) {
			listboxRule.setSelectedValue(strLastRule, true);
		}
	}

	/**
	 * OffLinesStart game buttonWhen is pressed
	 */
	protected void onStartOfflineClicked() {
		String strMode = listboxMode.getSelectedValue();
		propGlobal.setProperty("name.mode", strMode);

		String strRulePath = null;
		if (listboxRule.getSelectedIndex() >= 1) {
			int index = listboxRule.getSelectedIndex();
			String strRuleName = listboxRule.getSelectedValue();
			RuleEntry entry = ruleEntries.get(strMode);
			if (entry != null) {
				strRulePath = entry.paths.get(index - 1);
				propGlobal.setProperty("lastrule." + strMode, strRuleName);
			}
		} else {
			propGlobal.setProperty("lastrule." + strMode, "");
		}

		saveConfig();

		startNewGame(strRulePath);
		if (gameFrame == null) {
			gameFrame = new GameFrame(this);
		}
		if (gameManager != null && gameManager.mode != null) {
			gameFrame.setTitle(getUIText("Title_Game") + " - " + gameManager.mode.getName());
			gameFrame.maxfps = propConfig.getProperty("option.maxfps", 60);
			gameFrame.isNetPlay = false;
		}
		hideAllSubWindows();
		setVisible(false);
		gameFrame.displayWindow();
	}

	/**
	 * Shutdown this application
	 */
	public void shutdown() {
		log.debug("Main shutdown() called");
		propConfig.setProperty("mainwindow.width", getSize().width);
		propConfig.setProperty("mainwindow.height", getSize().height);
		propConfig.setProperty("mainwindow.x", getLocation().x);
		propConfig.setProperty("mainwindow.y", getLocation().y);
		saveConfig();
		System.exit(0);
	}

	/*
	 * Menu What Happens at Runtime
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int player = e.getActionCommand().endsWith("2P") ? 1 : 0;

		switch (e.getActionCommand()) {
		case "Top_StartOffline" -> onStartOfflineClicked();
		case "Menu_Open" -> openReplay();
		case "Menu_NetPlay" -> startNetPlay();
		case "Menu_RuleSelect", "Menu_RuleSelect2P" -> {
			if (ruleSelectFrame == null) {
				ruleSelectFrame = new RuleSelectFrame(this);
			}
			ruleSelectFrame.load(player);
			ruleSelectFrame.setVisible(true);
		}
		case "Menu_KeyConfig", "Menu_KeyConfig2P" -> {
			if (keyConfigFrame == null) {
				keyConfigFrame = new KeyConfigFrame(this);
			}
			keyConfigFrame.load(player);
			keyConfigFrame.setVisible(true);
		}
		case "Menu_AIConfig", "Menu_AIConfig2P" -> {
			if (aiSelectFrame == null) {
				aiSelectFrame = new AISelectFrame(this);
			}
			aiSelectFrame.load(player);
			aiSelectFrame.setVisible(true);
		}
		case "Menu_GameTuning", "Menu_GameTuning2P" -> {
			if (gameTuningFrame == null) {
				gameTuningFrame = new GameTuningFrame(this);
			}
			gameTuningFrame.load(player);
			gameTuningFrame.setVisible(true);
		}
		case "Menu_UpdateCheck" -> {
			if (updateCheckFrame == null) {
				updateCheckFrame = new UpdateCheckFrame(this);
			}
			updateCheckFrame.load();
			updateCheckFrame.setVisible(true);
		}
		case "Menu_GeneralConfig" -> {
			if (generalConfigFrame == null) {
				generalConfigFrame = new GeneralConfigFrame(this);
			}
			generalConfigFrame.load();
			generalConfigFrame.setVisible(true);
		}
		case "Menu_Exit" -> shutdown();
		default -> { // nothing
		}
		}
	}

	private void openReplay() {
		if (replayFileChooser == null) {
			File dir = new File(propGlobal.getProperty("custom.replay.directory", "replay"));
			replayFileChooser = new JFileChooser(dir);
			replayFileChooser.addChoosableFileFilter(new ReplayFileFilter());
		}
		if (replayFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			startReplayGame(replayFileChooser.getSelectedFile().getPath());
			if (gameFrame == null) {
				gameFrame = new GameFrame(this);
			}
			if (gameManager != null && gameManager.mode != null) {
				gameFrame.setTitle(getUIText("Title_Game") + " - " + gameManager.mode.getName() + " (Replay)");
				gameFrame.maxfps = propConfig.getProperty("option.maxfps", 60);
				gameFrame.isNetPlay = false;
			}
			hideAllSubWindows();
			setVisible(false);
			gameFrame.displayWindow();
		}
	}

	private void startNetPlay() {
		startNetPlayGame();
		if (gameFrame == null) {
			gameFrame = new GameFrame(this);
		}
		if (gameManager != null && gameManager.mode != null) {
			gameFrame.setTitle(getUIText("Title_Game") + " - " + gameManager.mode.getName());
			gameFrame.maxfps = 60;
			gameFrame.isNetPlay = true;
		}
		hideAllSubWindows();
		setVisible(false);
		gameFrame.displayWindow();
	}

	/**
	 * Hide all subwindows
	 */
	public void hideAllSubWindows() {
		if (keyConfigFrame != null) {
			keyConfigFrame.setVisible(false);
		}
		if (ruleSelectFrame != null) {
			ruleSelectFrame.setVisible(false);
		}
		if (aiSelectFrame != null) {
			aiSelectFrame.setVisible(false);
		}
		if (generalConfigFrame != null) {
			generalConfigFrame.setVisible(false);
		}
		if (gameTuningFrame != null) {
			gameTuningFrame.setVisible(false);
		}
		if (updateCheckFrame != null) {
			updateCheckFrame.setVisible(false);
		}
	}

	/**
	 * Start a new game (Rule will be user-selected one))
	 */
	public void startNewGame() {
		startNewGame(null);
	}

	/**
	 * Start a new game
	 *
	 * @param strRulePath Rule file path (null if you want to use user-selected one)
	 */
	public void startNewGame(String strRulePath) {
		gameManager = new GameManager(new RendererSwing((Graphics2D) getGraphics()));

		// Mode
		String modeName = propGlobal.getProperty("name.mode", "");
		GameMode modeObj = modeManager.getMode(modeName);
		if (modeObj == null) {
			log.error("Couldn't find mode:" + modeName);
		} else {
			gameManager.mode = modeObj;
		}

		gameManager.init();

		// Initialization for each player
		for (int i = 0; i < gameManager.getPlayers(); i++) {
			// Tuning settings
			gameManager.engines[i].owRotateButtonDefaultRight = propGlobal
					.getProperty(i + ".tuning.owRotateButtonDefaultRight", -1);
			gameManager.engines[i].owSkin = propGlobal.getProperty(i + ".tuning.owSkin", -1);
			gameManager.engines[i].owMinDAS = propGlobal.getProperty(i + ".tuning.owMinDAS", -1);
			gameManager.engines[i].owMaxDAS = propGlobal.getProperty(i + ".tuning.owMaxDAS", -1);
			gameManager.engines[i].owDasDelay = propGlobal.getProperty(i + ".tuning.owDasDelay", -1);
			gameManager.engines[i].owReverseUpDown = propGlobal.getProperty(i + ".tuning.owReverseUpDown", false);
			gameManager.engines[i].owMoveDiagonal = propGlobal.getProperty(i + ".tuning.owMoveDiagonal", -1);
			gameManager.engines[i].owBlockOutlineType = propGlobal.getProperty(i + ".tuning.owBlockOutlineType", -1);
			gameManager.engines[i].owBlockShowOutlineOnly = propGlobal.getProperty(i + ".tuning.owBlockShowOutlineOnly",
					-1);

			// Rule
			RuleOptions ruleopt = null;
			String rulename = strRulePath;
			if (rulename == null) {
				rulename = propGlobal.getProperty(i + ".rule", "");
				if (gameManager.mode.getGameStyle().getMode() > 0) {
					rulename = propGlobal.getProperty(i + ".rule." + gameManager.mode.getGameStyle(), "");
				}
			}
			if (rulename != null && !rulename.isEmpty()) {
				log.debug("Load rule options from " + rulename);
				ruleopt = GeneralUtil.loadRule(rulename);
			} else {
				log.debug("Load rule options from setting file");
				ruleopt = new RuleOptions();
				ruleopt.readProperty(propGlobal, i);
			}
			gameManager.engines[i].ruleopt = ruleopt;

			// NEXTOrder generation algorithm
			if (ruleopt.strRandomizer != null && !ruleopt.strRandomizer.isEmpty()) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer);
				gameManager.engines[i].randomizer = randomizerObject;
			}

			// Wallkick
			if (ruleopt.strWallkick != null && !ruleopt.strWallkick.isEmpty()) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engines[i].wallkick = wallkickObject;
			}

			// AI
			String aiName = propGlobal.getProperty(i + ".ai", "");
			if (!aiName.isEmpty()) {
				DummyAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engines[i].ai = aiObj;
				gameManager.engines[i].aiMoveDelay = propGlobal.getProperty(i + ".aiMoveDelay", 0);
				gameManager.engines[i].aiThinkDelay = propGlobal.getProperty(i + ".aiThinkDelay", 0);
				gameManager.engines[i].aiUseThread = propGlobal.getProperty(i + ".aiUseThread", true);
				gameManager.engines[i].aiShowHint = propGlobal.getProperty(i + ".aiShowHint", false);
				gameManager.engines[i].aiPrethink = propGlobal.getProperty(i + ".aiPrethink", false);
				gameManager.engines[i].aiShowState = propGlobal.getProperty(i + ".aiShowState", false);
			}
			gameManager.showInput = propConfig.getProperty("option.showInput", false);

			// Called at initialization
			gameManager.engines[i].init();
		}
	}

	/**
	 * Load and play the replay
	 *
	 * @param filename Replay dataOfFilename
	 */
	public void startReplayGame(String filename) {
		log.info("Loading Replay:" + filename);
		CustomProperties prop = new CustomProperties();

		try (var stream = new FileInputStream(filename)) {
			prop.load(stream);
		} catch (IOException e) {
			log.error("Couldn't load replay file from " + filename, e);
			return;
		}

		gameManager = new GameManager(new RendererSwing((Graphics2D) getGraphics()));
		gameManager.replayMode = true;
		gameManager.replayProp = prop;

		// Mode
		String modeName = prop.getProperty("name.mode", "");
		GameMode modeObj = modeManager.getMode(modeName);
		if (modeObj == null) {
			log.error("Couldn't find mode:" + modeName);
		} else {
			gameManager.mode = modeObj;
		}

		gameManager.init();

		// Initialization for each player
		for (int i = 0; i < gameManager.getPlayers(); i++) {
			// Rule
			RuleOptions ruleopt = new RuleOptions();
			ruleopt.readProperty(prop, i);
			gameManager.engines[i].ruleopt = ruleopt;

			// NEXTOrder generation algorithm
			if (ruleopt.strRandomizer != null && !ruleopt.strRandomizer.isEmpty()) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer);
				gameManager.engines[i].randomizer = randomizerObject;
			}

			// Wallkick
			if (ruleopt.strWallkick != null && !ruleopt.strWallkick.isEmpty()) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engines[i].wallkick = wallkickObject;
			}

			// AI (For added replay)
			String aiName = propGlobal.getProperty(i + ".ai", "");
			if (!aiName.isEmpty()) {
				DummyAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engines[i].ai = aiObj;
				gameManager.engines[i].aiMoveDelay = propGlobal.getProperty(i + ".aiMoveDelay", 0);
				gameManager.engines[i].aiThinkDelay = propGlobal.getProperty(i + ".aiThinkDelay", 0);
				gameManager.engines[i].aiUseThread = propGlobal.getProperty(i + ".aiUseThread", true);
				gameManager.engines[i].aiShowHint = propGlobal.getProperty(i + ".aiShowHint", false);
				gameManager.engines[i].aiPrethink = propGlobal.getProperty(i + ".aiPrethink", false);
				gameManager.engines[i].aiShowState = propGlobal.getProperty(i + ".aiShowState", false);
			}
			gameManager.showInput = propConfig.getProperty("option.showInput", false);

			// Called at initialization
			gameManager.engines[i].init();
		}
	}

	/**
	 * NetPlay start processing
	 */
	public void startNetPlayGame() {
		// gameManager Initialization
		gameManager = new GameManager(new RendererSwing((Graphics2D) getGraphics()));

		// Lobby Initialization
		netLobby = new NetLobbyFrame();
		netLobby.addListener(this);

		// Mode initialization
		enterNewMode(null);

		// Lobby start
		netLobby.init();
		netLobby.setVisible(true);
	}

	/**
	 * Enter to a new mode in netplay
	 *
	 * @param modeName Mode name
	 */
	public void enterNewMode(String modeName) {
		loadGlobalConfig(); // Reload global config file

		GameMode previousMode = gameManager.mode;
		GameMode newModeTemp = modeName == null ? new NetDummyMode() : modeManager.getMode(modeName);

		if (newModeTemp == null) {
			log.error("Cannot find a mode:" + modeName);
		} else if (newModeTemp instanceof NetDummyMode newMode) {
			log.info("Enter new mode:" + newModeTemp.getName());

			if (previousMode != null) {
				if (gameManager.engines[0].ai != null) {
					gameManager.engines[0].ai.shutdown(gameManager.engines[0], 0);
				}
				previousMode.netplayUnload(netLobby);
			}
			gameManager.mode = newMode;
			gameManager.init();

			// Tuning
			gameManager.engines[0].owRotateButtonDefaultRight = propGlobal
					.getProperty(0 + ".tuning.owRotateButtonDefaultRight", -1);
			gameManager.engines[0].owSkin = propGlobal.getProperty(0 + ".tuning.owSkin", -1);
			gameManager.engines[0].owMinDAS = propGlobal.getProperty(0 + ".tuning.owMinDAS", -1);
			gameManager.engines[0].owMaxDAS = propGlobal.getProperty(0 + ".tuning.owMaxDAS", -1);
			gameManager.engines[0].owDasDelay = propGlobal.getProperty(0 + ".tuning.owDasDelay", -1);
			gameManager.engines[0].owReverseUpDown = propGlobal.getProperty(0 + ".tuning.owReverseUpDown", false);
			gameManager.engines[0].owMoveDiagonal = propGlobal.getProperty(0 + ".tuning.owMoveDiagonal", -1);
			gameManager.engines[0].owBlockOutlineType = propGlobal.getProperty(0 + ".tuning.owBlockOutlineType", -1);
			gameManager.engines[0].owBlockShowOutlineOnly = propGlobal.getProperty(0 + ".tuning.owBlockShowOutlineOnly",
					-1);

			// Rule
			RuleOptions ruleOptions = null;
			String rulename = propGlobal.getProperty(0 + ".rule", "");
			if (gameManager.mode.getGameStyle().getMode() > 0) {
				rulename = propGlobal.getProperty(0 + ".rule." + gameManager.mode.getGameStyle(), "");
			}
			if (rulename != null && !rulename.isEmpty()) {
				log.info("Load rule options from " + rulename);
				ruleOptions = GeneralUtil.loadRule(rulename);
			} else {
				log.info("Load rule options from setting file");
				ruleOptions = new RuleOptions();
				ruleOptions.readProperty(propGlobal, 0);
			}
			gameManager.engines[0].ruleopt = ruleOptions;

			// Randomizer
			if (ruleOptions.strRandomizer != null && !ruleOptions.strRandomizer.isEmpty()) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleOptions.strRandomizer);
				gameManager.engines[0].randomizer = randomizerObject;
			}

			// Wallkick
			if (ruleOptions.strWallkick != null && !ruleOptions.strWallkick.isEmpty()) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleOptions.strWallkick);
				gameManager.engines[0].wallkick = wallkickObject;
			}

			// AI
			String aiName = propGlobal.getProperty(0 + ".ai", "");
			if (!aiName.isEmpty()) {
				DummyAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engines[0].ai = aiObj;
				gameManager.engines[0].aiMoveDelay = propGlobal.getProperty(0 + ".aiMoveDelay", 0);
				gameManager.engines[0].aiThinkDelay = propGlobal.getProperty(0 + ".aiThinkDelay", 0);
				gameManager.engines[0].aiUseThread = propGlobal.getProperty(0 + ".aiUseThread", true);
				gameManager.engines[0].aiShowHint = propGlobal.getProperty(0 + ".aiShowHint", false);
				gameManager.engines[0].aiPrethink = propGlobal.getProperty(0 + ".aiPrethink", false);
				gameManager.engines[0].aiShowState = propGlobal.getProperty(0 + ".aiShowState", false);
			}
			gameManager.showInput = propConfig.getProperty("option.showInput", false);

			// Initialization for each player
			for (int i = 0; i < gameManager.getPlayers(); i++) {
				gameManager.engines[i].init();
			}

			newMode.netplayInit(netLobby);
		} else {
			log.error("This mode does not support netplay:" + modeName);
		}

		if (gameFrame != null) {
			gameFrame.updateTitleBarCaption();
		}
	}

	/**
	 * ObserverStart the client
	 */
	public synchronized void startObserverClient() {
		log.debug("startObserverClient called");

		propObserver = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/netobserver.cfg");
			propObserver.load(in);
			in.close();
		} catch (IOException e) {
		}

		if (!propObserver.getProperty("observer.enable", false)
				|| netObserverClient != null && netObserverClient.isConnected()) {
			return;
		}

		String host = propObserver.getProperty("observer.host", "");
		int port = propObserver.getProperty("observer.port", NetBaseClient.DEFAULT_PORT);

		if (host.length() > 0 && port > 0) {
			netObserverClient = new NetObserverClient(host, port);
			netObserverClient.start();
			log.debug("Observer started");
		}
	}

	/**
	 * ObserverStop the client
	 */
	public synchronized void stopObserverClient() {
		log.debug("stopObserverClient called");

		if (netObserverClient != null) {
			if (netObserverClient.isConnected()) {
				netObserverClient.send("disconnect\n");
			}
			netObserverClient.threadRunning = false;
			netObserverClient.connectedFlag = false;
			netObserverClient = null;
			log.debug("Observer stoped");
		}
	}

	/**
	 * ObserverClient acquisition
	 *
	 * @return ObserverClient
	 */
	public synchronized NetObserverClient getObserverClient() {
		return netObserverClient;
	}

	@Override
	public void netlobbyOnDisconnect(NetLobbyFrame lobby, NetPlayerClient client, Throwable ex) {
		if (gameFrame != null) {
			gameFrame.strModeToEnter = null;
		}
	}

	@Override
	public void netlobbyOnExit(NetLobbyFrame lobby) {
		if (gameManager != null) {
			gameManager.engines[0].quitflag = true;
		}
	}

	@Override
	public void netlobbyOnInit(NetLobbyFrame lobby) {
	}

	@Override
	public void netlobbyOnLoginOK(NetLobbyFrame lobby, NetPlayerClient client) {
	}

	@Override
	public void netlobbyOnMessage(NetLobbyFrame lobby, NetPlayerClient client, String[] message) throws IOException {
	}

	@Override
	public void netlobbyOnRoomJoin(NetLobbyFrame lobby, NetPlayerClient client, NetRoomInfo roomInfo) {
		// enterNewMode(roomInfo.strMode);
		if (gameFrame != null) {
			gameFrame.strModeToEnter = roomInfo.strMode;
		}
	}

	@Override
	public void netlobbyOnRoomLeave(NetLobbyFrame lobby, NetPlayerClient client) {
		// enterNewMode(null);
		if (gameFrame != null) {
			gameFrame.strModeToEnter = null;
		}
	}

	@Override
	public void onUpdateCheckerStart() {
	}

	@Override
	public void onUpdateCheckerEnd(int status) {
		if (UpdateChecker.isNewVersionAvailable()) {
			SwingUtilities.invokeLater(() -> {
				if (lModeSelect != null) {
					String strTemp = String.format(getUIText("Top_NewVersion"),
							UpdateChecker.getLatestVersionFullString(), UpdateChecker.getReleaseDate());
					lModeSelect.setText(strTemp);
				}
			});
		}
	}

	/**
	 * Filter for selecting files replay
	 */
	protected class ReplayFileFilter extends FileFilter {
		/*
		 * Decision whether or not to display the file
		 */
		@Override
		public boolean accept(File f) {
			// If the directory displayed unconditional
			// Or the end of the file is.rep If it was displayed
			return f.isDirectory() || f.getName().endsWith(".rep");
		}

		/*
		 * Returns the display name for this filter
		 */
		@Override
		public String getDescription() {
			return getUIText("FileChooser_ReplayFile");
		}
	}

	/**
	 * Mode For selection list boxMouseAdapter
	 */
	protected class ListboxModeMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
				onStartOfflineClicked();
			}
		}
	}

	/**
	 * RuleEntry
	 */
	protected class RuleEntry {
		public final List<String> paths = new LinkedList<>();
		public final List<String> names = new LinkedList<>();
	}
}

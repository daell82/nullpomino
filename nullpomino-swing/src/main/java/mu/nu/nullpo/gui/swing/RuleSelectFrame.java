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

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;

/**
 * Rules of selection screen frame
 */
@Log4j
public class RuleSelectFrame extends JFrame implements ActionListener {

	/** Serial version ID */
	private static final long serialVersionUID = 1L;


	/** Owner window */
	protected NullpoMinoSwing owner;

	/** Player number */
	protected int playerID;

	/** Current Rules file */
	private String[] strCurrentFileName;

	/** Current Rule name */
	private String[] strCurrentRuleName;

	/** Rule entries */
	private List<RuleEntry> ruleEntries;

	/** Rule select listbox */
	private List<JList<String>> listboxRule;

	/** Tab */
	private JTabbedPane tabPane;

	/**
	 * Constructor
	 *
	 * @param owner Owner window
	 * @throws HeadlessException If GUI cannot be used
	 */
	public RuleSelectFrame(NullpoMinoSwing owner) {
		super();
		this.owner = owner;
		createRuleEntries();

		// GUI Initialization
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		initUI();
		pack();
	}

	/**
	 * Setup rule selector
	 *
	 * @param pl Player number
	 */
	public void load(int pl) {
		playerID = pl;

		setTitle(NullpoMinoSwing.getUIText("Title_RuleSelect") + " (" + (playerID + 1) + "P)");

		strCurrentFileName = new String[GameEngine.MAX_GAMESTYLE];
		strCurrentRuleName = new String[GameEngine.MAX_GAMESTYLE];

		for (int i = 0; i < GameEngine.MAX_GAMESTYLE; i++) {
			if (i == 0) {
				strCurrentFileName[i] = NullpoMinoSwing.propGlobal.getProperty(playerID + ".rulefile", "");
				strCurrentRuleName[i] = NullpoMinoSwing.propGlobal.getProperty(playerID + ".rulename", "");
			} else {
				strCurrentFileName[i] = NullpoMinoSwing.propGlobal.getProperty(playerID + ".rulefile." + i, "");
				strCurrentRuleName[i] = NullpoMinoSwing.propGlobal.getProperty(playerID + ".rulename." + i, "");
			}

			List<RuleEntry> subEntries = getSubsetEntries(i);

			for (int j = 0; j < subEntries.size(); j++) {
				if (subEntries.get(j).filename.equals(strCurrentFileName[i])) {
					listboxRule.get(i).setSelectedIndex(j);
				}
			}
		}

		EventQueue.invokeLater(() -> listboxRule.get(0).requestFocusInWindow());
	}

	/**
	 * GUIAInitialization
	 */
	protected void initUI() {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Tab
		tabPane = new JTabbedPane();
		tabPane.setAlignmentX(LEFT_ALIGNMENT);
		this.add(tabPane);

		// Rules
		listboxRule = new ArrayList<>(GameEngine.MAX_GAMESTYLE);
		for (int i = 0; i < GameEngine.MAX_GAMESTYLE; i++) {
			var jList = new JList<>(extractRuleListFromRuleEntries(i));
			listboxRule.add(i, jList);
			JScrollPane scpaneRule = new JScrollPane(jList);
			scpaneRule.setPreferredSize(new Dimension(380, 250));
			scpaneRule.setAlignmentX(LEFT_ALIGNMENT);
			tabPane.addTab(GameEngine.GAMESTYLE_NAMES[i], scpaneRule);
		}

		// default Back to button
		JButton btnUseDefault = new JButton(NullpoMinoSwing.getUIText("RuleSelect_UseDefault"));
		btnUseDefault.setMnemonic('D');
		btnUseDefault.addActionListener(this);
		btnUseDefault.setActionCommand("RuleSelect_UseDefault");
		btnUseDefault.setAlignmentX(LEFT_ALIGNMENT);
		btnUseDefault.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		btnUseDefault.setVisible(false);
		this.add(btnUseDefault);

		// buttonKind
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new BoxLayout(pButtons, BoxLayout.X_AXIS));
		pButtons.setAlignmentX(LEFT_ALIGNMENT);
		this.add(pButtons);

		JButton btnOK = new JButton(NullpoMinoSwing.getUIText("RuleSelect_OK"));
		btnOK.setMnemonic('O');
		btnOK.addActionListener(this);
		btnOK.setActionCommand("RuleSelect_OK");
		btnOK.setAlignmentX(LEFT_ALIGNMENT);
		btnOK.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		pButtons.add(btnOK);
		getRootPane().setDefaultButton(btnOK);

		JButton btnCancel = new JButton(NullpoMinoSwing.getUIText("RuleSelect_Cancel"));
		btnCancel.setMnemonic('C');
		btnCancel.addActionListener(this);
		btnCancel.setActionCommand("RuleSelect_Cancel");
		btnCancel.setAlignmentX(LEFT_ALIGNMENT);
		btnCancel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		pButtons.add(btnCancel);
	}

	/**
	 * Create rule entries
	 *
	 * @param filelist Rule file list
	 */
	private void createRuleEntries() {
		ruleEntries = new LinkedList<>();
		File dir = new File("config/rule");
		File[] ruleFiles = dir.listFiles((_, name) -> name.endsWith(".rul"));
		if (ruleFiles == null) {
			log.error("Rule directory not found: " + dir.getAbsolutePath());
			return;
		}
		int style;
		String rulename;
		for (File file : ruleFiles) {
			CustomProperties prop = new CustomProperties();
			try (var in = new FileInputStream(file)) {
				prop.load(in);
				rulename = prop.getProperty("0.ruleopt.strRuleName", "");
				style = prop.getProperty("0.ruleopt.style", 0);
			} catch (IOException e) {
				log.debug("failed to load file " + file, e);
				rulename = "";
				style = -1;
			}
			ruleEntries.add(new RuleEntry(file.getName(), file.getPath(), rulename, style));
		}
	}

	/**
	 * Get subset of rule entries
	 *
	 * @param currentStyle Current style
	 * @return Subset of rule entries
	 */
	private List<RuleEntry> getSubsetEntries(int currentStyle) {
		List<RuleEntry> subEntries = new LinkedList<>();
		for (RuleEntry ruleEntry : ruleEntries) {
			if (ruleEntry.style() == currentStyle) {
				subEntries.add(ruleEntry);
			}
		}
		return subEntries;
	}

	/**
	 * Get rule name + file name list as String[]
	 *
	 * @param currentStyle Current style
	 * @return Rule name + file name list
	 */
	private String[] extractRuleListFromRuleEntries(int currentStyle) {
		List<RuleEntry> subEntries = getSubsetEntries(currentStyle);

		String[] result = new String[subEntries.size()];
		for (int i = 0; i < subEntries.size(); i++) {
			RuleEntry entry = subEntries.get(i);
			result[i] = entry.rulename + " (" + entry.filename + ")";
		}

		return result;
	}

	/*
	 * Menu What Happens at Runtime
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "RuleSelect_OK") {
			for (int i = 0; i < GameEngine.MAX_GAMESTYLE; i++) {
				int id = listboxRule.get(i).getSelectedIndex();
				List<RuleEntry> subEntries = getSubsetEntries(i);
				RuleEntry entry = subEntries.get(id);

				if (i == 0) {
					if (id >= 0) {
						NullpoMinoSwing.propGlobal.setProperty(playerID + ".rule", entry.filepath);
						NullpoMinoSwing.propGlobal.setProperty(playerID + ".rulefile", entry.filename);
						NullpoMinoSwing.propGlobal.setProperty(playerID + ".rulename", entry.rulename);
					} else {
						NullpoMinoSwing.propGlobal.setProperty(playerID + ".rule", "");
						NullpoMinoSwing.propGlobal.setProperty(playerID + ".rulefile", "");
						NullpoMinoSwing.propGlobal.setProperty(playerID + ".rulename", "");
					}
				} else if (id >= 0) {
					NullpoMinoSwing.propGlobal.setProperty(playerID + ".rule." + i, entry.filepath);
					NullpoMinoSwing.propGlobal.setProperty(playerID + ".rulefile." + i, entry.filename);
					NullpoMinoSwing.propGlobal.setProperty(playerID + ".rulename." + i, entry.rulename);
				} else {
					NullpoMinoSwing.propGlobal.setProperty(playerID + ".rule." + i, "");
					NullpoMinoSwing.propGlobal.setProperty(playerID + ".rulefile." + i, "");
					NullpoMinoSwing.propGlobal.setProperty(playerID + ".rulename." + i, "");
				}
			}
			NullpoMinoSwing.saveConfig();
			setVisible(false);
		} else if (e.getActionCommand() == "RuleSelect_UseDefault") {
			int id = tabPane.getSelectedIndex();
			if (id >= 0 && id < listboxRule.size()) {
				listboxRule.get(id).clearSelection();
			}
		} else if (e.getActionCommand() == "RuleSelect_Cancel") {
			setVisible(false);
		}
	}

	/**
	 * Rule entry
	 */
	private record RuleEntry(String filename, String filepath, String rulename, int style) {
	}
}

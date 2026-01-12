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
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import lombok.extern.log4j.Log4j;
import mu.nu.nullpo.game.subsystem.ai.AIPlayer;

/**
 * AISelection screen frame
 */
@Log4j
public class AISelectFrame extends JFrame implements ActionListener {

	/** Serial version ID */
	private static final long serialVersionUID = 1L;

	/** Parent window */
	protected NullpoMinoSwing owner;

	/** Player number */
	protected int playerID;

	/** AIList of classes */
	protected List<String> aiClasses;

	/** AIOfNameList */
	protected List<String> aiNames;

	/** Current AIClass of */
	protected String currentAI;

	/** AIMovement interval of */
	protected int aiMoveDelay = 0;

	/** AIThinking of waiting time */
	protected int aiThinkDelay = 0;

	/** AIUsing threads in */
	protected boolean aiUseThread = false;

	protected boolean aiShowHint = false;

	protected boolean aiPrethink = false;

	protected boolean aiShowState = false;

	/** AIList list box */
	protected JList<String> listboxAI;

	/** AIText box of the movement interval */
	protected JTextField txtfldAIMoveDelay;

	/** AIThinking of waiting timeText box */
	protected JTextField txtfldAIThinkDelay;

	/** AIThread Usage in check Box */
	protected JCheckBox chkboxAIUseThread;

	protected JCheckBox chkBoxAIShowHint;

	protected JCheckBox chkBoxAIPrethink;

	protected JCheckBox chkBoxAIShowState;

	/**
	 * Constructor
	 *
	 * @param owner Parent window
	 * @throws HeadlessException Keyboard, Mouse, Exceptions such as the display if
	 *                           there is no
	 */
	public AISelectFrame(NullpoMinoSwing owner) throws HeadlessException {
		super();
		this.owner = owner;

		aiClasses = loadAIList();
		aiNames = loadAINames(aiClasses);

		// GUIOfInitialization
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		initUI();
		pack();
	}

	/**
	 * This frame Action to take when you view the
	 *
	 * @param player Player number
	 */
	public void load(int player) {
		playerID = player;

		setTitle(NullpoMinoSwing.getUIText("Title_AISelect") + " (" + (playerID + 1) + "P)");

		currentAI = NullpoMinoSwing.propGlobal.getProperty(playerID + ".ai", "");
		aiMoveDelay = NullpoMinoSwing.propGlobal.getProperty(playerID + ".aiMoveDelay", 0);
		aiThinkDelay = NullpoMinoSwing.propGlobal.getProperty(playerID + ".aiThinkDelay", 0);
		aiUseThread = NullpoMinoSwing.propGlobal.getProperty(playerID + ".aiUseThread", true);
		aiShowHint = NullpoMinoSwing.propGlobal.getProperty(playerID + ".aiShowHint", false);
		aiPrethink = NullpoMinoSwing.propGlobal.getProperty(playerID + ".aiPrethink", false);
		aiShowState = NullpoMinoSwing.propGlobal.getProperty(playerID + ".aiShowState", false);

		listboxAI.clearSelection();
		listboxAI.setSelectedIndex(aiClasses.indexOf(currentAI));

		txtfldAIMoveDelay.setText(String.valueOf(aiMoveDelay));
		txtfldAIThinkDelay.setText(String.valueOf(aiThinkDelay));
		chkboxAIUseThread.setSelected(aiUseThread);
		chkBoxAIShowHint.setSelected(aiShowHint);
		chkBoxAIPrethink.setSelected(aiPrethink);
		chkBoxAIShowState.setSelected(aiShowState);
	}

	/**
	 * AIReads the list
	 *
	 * @param bf To read from a text file
	 * @return AIList
	 */
	private List<String> loadAIList() {
		try {
			List<String> lines = Files.readAllLines(new File("config/list/ai.lst").toPath());
			return lines.stream().filter(l -> !l.isBlank() && !l.startsWith("#")).toList();
		} catch (IOException ioe) {
			log.error("failed to load AI", ioe);
			return Collections.emptyList();
		}
	}

	/**
	 * AIOfNameCreate a list
	 *
	 * @param aiClassNames AIList of classes
	 * @return AIOfNameList
	 */
	private List<String> loadAINames(List<String> aiClassNames) {
		List<String> aiPlayers = new ArrayList<>(aiClassNames.size());
		for (String clazz : aiClassNames) {
			try {
				Class<?> aiClass = Class.forName(clazz);
				AIPlayer aiPlayer = (AIPlayer) aiClass.getConstructor().newInstance();
				aiPlayers.add(aiPlayer.getName());
			} catch (ClassNotFoundException e) {
				log.warn("AI class " + clazz + " not found", e);
			} catch (ReflectiveOperationException e) {
				log.warn("AI class " + clazz + " load failed", e);
			}

		}
		return aiPlayers;
	}

	/**
	 * GUIAInitialization
	 */
	protected void initUI() {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// AIList
		JPanel panelAIList = new JPanel();
		panelAIList.setLayout(new BorderLayout());
		panelAIList.setAlignmentX(LEFT_ALIGNMENT);
		this.add(panelAIList);

		String[] strList = new String[aiClasses.size()];
		for (int i = 0; i < strList.length; i++) {
			strList[i] = aiNames.get(i) + " (" + aiClasses.get(i) + ")";
		}
		listboxAI = new JList<>(strList);

		JScrollPane scpaneAI = new JScrollPane(listboxAI);
		scpaneAI.setPreferredSize(new Dimension(400, 250));
		panelAIList.add(scpaneAI, BorderLayout.CENTER);

		JButton btnNoUse = new JButton(NullpoMinoSwing.getUIText("AISelect_NoUse"));
		btnNoUse.setMnemonic('N');
		btnNoUse.addActionListener(this);
		btnNoUse.setActionCommand("AISelect_NoUse");
		btnNoUse.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		panelAIList.add(btnNoUse, BorderLayout.SOUTH);

		// AIText box of the movement interval
		JPanel panelTxtfldAIMoveDelay = new JPanel();
		panelTxtfldAIMoveDelay.setLayout(new BorderLayout());
		panelTxtfldAIMoveDelay.setAlignmentX(LEFT_ALIGNMENT);
		this.add(panelTxtfldAIMoveDelay);

		panelTxtfldAIMoveDelay.add(new JLabel(NullpoMinoSwing.getUIText("AISelect_LabelAIMoveDelay")),
				BorderLayout.WEST);

		txtfldAIMoveDelay = new JTextField(20);
		panelTxtfldAIMoveDelay.add(txtfldAIMoveDelay, BorderLayout.EAST);

		// AIText box of the movement interval
		JPanel panelTxtfldAIThinkDelay = new JPanel();
		panelTxtfldAIThinkDelay.setLayout(new BorderLayout());
		panelTxtfldAIThinkDelay.setAlignmentX(LEFT_ALIGNMENT);
		this.add(panelTxtfldAIThinkDelay);

		panelTxtfldAIThinkDelay.add(new JLabel(NullpoMinoSwing.getUIText("AISelect_LabelAIThinkDelay")),
				BorderLayout.WEST);

		txtfldAIThinkDelay = new JTextField(20);
		panelTxtfldAIThinkDelay.add(txtfldAIThinkDelay, BorderLayout.EAST);

		// AIThread use check Box
		chkboxAIUseThread = new JCheckBox(NullpoMinoSwing.getUIText("AISelect_CheckboxAIUseThread"));
		chkboxAIUseThread.setAlignmentX(LEFT_ALIGNMENT);
		chkboxAIUseThread.setMnemonic('T');
		this.add(chkboxAIUseThread);

		chkBoxAIShowHint = new JCheckBox(NullpoMinoSwing.getUIText("AISelect_CheckboxAIShowHint"));
		chkBoxAIShowHint.setAlignmentX(LEFT_ALIGNMENT);
		chkBoxAIShowHint.setMnemonic('H');
		this.add(chkBoxAIShowHint);

		chkBoxAIPrethink = new JCheckBox(NullpoMinoSwing.getUIText("AISelect_CheckboxAIPrethink"));
		chkBoxAIPrethink.setAlignmentX(LEFT_ALIGNMENT);
		chkBoxAIPrethink.setMnemonic('P');
		this.add(chkBoxAIPrethink);

		chkBoxAIShowState = new JCheckBox(NullpoMinoSwing.getUIText("AISelect_CheckboxAIShowState"));
		chkBoxAIShowState.setAlignmentX(LEFT_ALIGNMENT);
		chkBoxAIShowState.setMnemonic('S');
		this.add(chkBoxAIShowState);

		// buttonKind
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
		panelButtons.setAlignmentX(LEFT_ALIGNMENT);
		this.add(panelButtons);

		JButton btnOK = new JButton(NullpoMinoSwing.getUIText("AISelect_OK"));
		btnOK.setMnemonic('O');
		btnOK.addActionListener(this);
		btnOK.setActionCommand("AISelect_OK");
		btnOK.setAlignmentX(LEFT_ALIGNMENT);
		btnOK.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		panelButtons.add(btnOK);
		getRootPane().setDefaultButton(btnOK);

		JButton btnCancel = new JButton(NullpoMinoSwing.getUIText("AISelect_Cancel"));
		btnCancel.setMnemonic('C');
		btnCancel.addActionListener(this);
		btnCancel.setActionCommand("AISelect_Cancel");
		btnCancel.setAlignmentX(LEFT_ALIGNMENT);
		btnCancel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		panelButtons.add(btnCancel);
	}

	/*
	 * Called when button clicked
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// AIUnused button
		if (e.getActionCommand() == "AISelect_NoUse") {
			listboxAI.clearSelection();
		}
		// OK
		else if (e.getActionCommand() == "AISelect_OK") {
			try {
				aiMoveDelay = Integer.parseInt(txtfldAIMoveDelay.getText());
			} catch (NumberFormatException e2) {
				aiMoveDelay = -1;
			}
			try {
				aiThinkDelay = Integer.parseInt(txtfldAIThinkDelay.getText());
			} catch (NumberFormatException e2) {
				aiThinkDelay = 0;
			}
			aiUseThread = chkboxAIUseThread.isSelected();
			aiShowHint = chkBoxAIShowHint.isSelected();
			aiPrethink = chkBoxAIPrethink.isSelected();
			aiShowState = chkBoxAIShowState.isSelected();

			int aiID = listboxAI.getSelectedIndex();
			if (aiID >= 0) {
				NullpoMinoSwing.propGlobal.setProperty(playerID + ".ai", aiClasses.get(aiID));
			} else {
				NullpoMinoSwing.propGlobal.setProperty(playerID + ".ai", "");
			}
			NullpoMinoSwing.propGlobal.setProperty(playerID + ".aiMoveDelay", aiMoveDelay);
			NullpoMinoSwing.propGlobal.setProperty(playerID + ".aiThinkDelay", aiThinkDelay);
			NullpoMinoSwing.propGlobal.setProperty(playerID + ".aiUseThread", aiUseThread);
			NullpoMinoSwing.propGlobal.setProperty(playerID + ".aiShowHint", aiShowHint);
			NullpoMinoSwing.propGlobal.setProperty(playerID + ".aiPrethink", aiPrethink);
			NullpoMinoSwing.propGlobal.setProperty(playerID + ".aiShowState", aiShowState);
			NullpoMinoSwing.saveConfig();

			setVisible(false);
		}
		// Cancel
		else if (e.getActionCommand() == "AISelect_Cancel") {
			setVisible(false);
		}
	}
}

/*
 * Copyright (C) 2003 Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * JPanel.java
 *
 * Created on 19 August 2003, 22:19
 */

package org.railz.client.view;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Enumeration;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.railz.client.common.ActionAdapter;
import org.railz.client.model.ModelRoot;
import org.railz.client.model.TrackBuildModel;
import org.railz.client.renderer.ViewLists;

/**
 * 
 * @author bob
 */
class TrackBuildJPanel extends javax.swing.JPanel {

	private int numberOfButtons = 0;
	private int widthOfButton = 30;

	private TrackBuildModel buildModel;
	private ButtonGroup buttonGroup;
	private ButtonGroup buildModeButtonGroup;
	// TODO newly created temp test.
	private ButtonGroup buildType;

	@Override
	public void validate() {
		super.validate();

	}

	/**
	 * Workaround for completely broken FlowLayout behaviour.
	 */
	private final ComponentAdapter sizeListener = new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent e) {

			/* determine max number of cols */
			Dimension d = trackBuildModesSP.getViewport().getSize();
			int numCols = (int) (d.getWidth() / (widthOfButton + 5));
			if (numCols < 1) {
				numCols = 1;
			}
			int numRows = numberOfButtons / numCols + 1;
			d.setSize(d.getWidth(), numRows * (widthOfButton + 5));
			trackBuildModesPanel.setPreferredSize(d);
			trackBuildModesPanel.revalidate();
		}
	};

	void setup(ViewLists vl, ModelRoot modelRoot) {

		buildModel = modelRoot.getTrackBuildModel();

		// GridBagConstraints gbc = new GridBagConstraints();
		buttonGroup = new ButtonGroup();

		setupTrackRules(buildModel.getTrackRuleAdapter());

		setupBuildMode(buildModel.getBuildModeActionAdapter(),
				trackBuildModesPanel);

		// Do at end of actions
		trackBuildModesPanel.revalidate();
	}

	private void setupTrackRules(ActionAdapter trackRuleAdapter) {
		Enumeration enumButtonModels = trackRuleAdapter.getButtonModels();

		Enumeration trackRuleActionsEnum = trackRuleAdapter.getActions();

		while (trackRuleActionsEnum.hasMoreElements()) {
			TrackRuleButton button = new TrackRuleButton(
					(Action) trackRuleActionsEnum.nextElement());
			button.setModel((ButtonModel) enumButtonModels.nextElement());
			Dimension d = button.getSize();
			Dimension s = trackBuildModesPanel.getSize();
			// XXX verify
			// int columns = (int) (s.getWidth() / d.getWidth());
			buttonGroup.add(button);
			trackBuildModesPanel.add(button);
			numberOfButtons++;
			/* this is OK since all buttons are same width */
			widthOfButton = (int) button.getPreferredSize().getWidth();
		}
	}

	private void setupBuildMode(ActionAdapter buildModeActionAdapter,
			JPanel panel) {

		trackModeCB.setModel(buildModeActionAdapter);

		ButtonGroup buttonGroup = new ButtonGroup();

		Enumeration buildModeActions = buildModeActionAdapter.getActions();
		Enumeration buildModeModels = buildModeActionAdapter.getButtonModels();

		while (buildModeActions.hasMoreElements()) {
			TrackRuleButton button = new TrackRuleButton(
					(Action) buildModeActions.nextElement());
			button.setModel((ButtonModel) buildModeModels.nextElement());

			buttonGroup.add(button);
			jPanel1.add(button);

			// TODO readd in
			// numberOfButtons++;
		}

		buildModeButtonGroup = buttonGroup;
		jPanel1.revalidate();
		// Enumeration trackRuleActionsEnum =
		// trackModeActionAdapter.getActions();

		// TODO section below here is modified
		// Add action name and an int key to the action adaptor
		// trackRuleAdapter - Here are the business logic items from the world.

		// Buttons constructed with actions
		// action maps and sets mode in trackMoveProducer from actionPerformed
		// Buttons have setModel();

		// TODO Button group holds buttons Why? What does it do?
		// Button also added to panel

		// Enumeration enumButtonModels = buildModel.getTrackRuleAdapter()
		// .getButtonModels();
		// while (e.hasMoreElements()) {
		//
		// }
		// new ButtonModels
		// TrackRuleButton
		// JToggleButton button = new JToggleButton(
		// (Action) trackRuleActionsEnum.nextElement());
		// // button.setModel((ButtonModel) enumButtonModels.nextElement());
		// Dimension d = button.getSize();
		// Dimension s = trackBuildModesPanel.getSize();
		// buildType.add(button);

	}

	private void setupTrackComponents() {
		/*
		 * setup the "mode" combo box
		 */

		trackBuildModesSP.setViewportView(trackBuildModesPanel);
		trackBuildModesSP.addComponentListener(sizeListener);
	}

	/** Creates new form JPanel */
	public TrackBuildJPanel() {
		initComponents();
		setupTrackComponents();
	}

	// TODO should be a more generic name as no special functionality.
	/**
	 * represents a track rule - contains a small icon representing the track
	 * and a text label
	 */
	private class TrackRuleButton extends JToggleButton {
		public TrackRuleButton(Action a) {
			super(a);
			setMargin(new Insets(0, 0, 0, 0));
			setText(null);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;

		jPanel1 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		trackModeCB = new javax.swing.JComboBox();
		jSeparator1 = new javax.swing.JSeparator();
		trackBuildModesSP = new javax.swing.JScrollPane();
		trackBuildModesPanel = new javax.swing.JPanel();

		setLayout(new java.awt.GridBagLayout());

		jLabel2.setText("Mode:");
		jPanel1.add(jLabel2);

		jPanel1.add(trackModeCB);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(jPanel1, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		add(jSeparator1, gridBagConstraints);

		trackBuildModesSP
				.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		trackBuildModesSP.setViewportView(trackBuildModesPanel);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(trackBuildModesSP, gridBagConstraints);

	}// GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel jLabel2;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JPanel trackBuildModesPanel;
	private javax.swing.JScrollPane trackBuildModesSP;
	private javax.swing.JComboBox trackModeCB;
	// End of variables declaration//GEN-END:variables

}

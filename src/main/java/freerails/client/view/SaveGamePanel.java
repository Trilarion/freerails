/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * SaveGamePanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.network.MessageToServer;
import freerails.network.SaveGameMessageToServer;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * 
 */
public class SaveGamePanel extends JPanel implements View {

    private static final long serialVersionUID = 4031907071040752589L;
    private ModelRoot modelRoot;
    private ActionListener close;
    private JButton cancelButton;
    private JTextField fileNameTextField;
    private JLabel label1;
    private JButton oKButton;

    /**
     * Creates new form SaveGamePanel
     */
    public SaveGamePanel() {
        initComponents();
    }

    private static void fileNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        System.out.println("fileNameTextFieldActionPerformed" + evt.toString());
    }


    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        label1 = new JLabel();
        fileNameTextField = new JTextField();
        oKButton = new JButton();
        cancelButton = new JButton();

        setLayout(new java.awt.GridBagLayout());

        label1.setText("Please enter a name for the save game.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        add(label1, gridBagConstraints);

        fileNameTextField.setText("savegame");
        fileNameTextField.addActionListener(SaveGamePanel::fileNameTextFieldActionPerformed);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        add(fileNameTextField, gridBagConstraints);

        oKButton.setText("OK");
        oKButton.addActionListener(this::oKButtonActionPerformed);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        add(oKButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        add(cancelButton, gridBagConstraints);

    }

    private void oKButtonActionPerformed(java.awt.event.ActionEvent evt) {

        String filename = fileNameTextField.getText();
        // Save the current game using the string
        modelRoot.setProperty(Property.QUICK_MESSAGE, "Saved game " + filename);
        MessageToServer message2 = new SaveGameMessageToServer(1, filename + ".sav");

        modelRoot.sendCommand(message2);
        close.actionPerformed(evt);
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        close.actionPerformed(evt);
    }

    /**
     * @param modelRoot
     * @param vl
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot vl, Action closeAction) {
        close = closeAction;
        this.modelRoot = modelRoot;
    }


}

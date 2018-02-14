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
import freerails.network.message.MessageToServer;
import freerails.network.message.SaveGameMessageToServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 
 */
public class SaveGamePanel extends JPanel implements View {

    private static final long serialVersionUID = 4031907071040752589L;
    private ModelRoot modelRoot;
    private ActionListener close;
    private JTextField fileNameTextField;

    /**
     * Creates new form SaveGamePanel
     */
    public SaveGamePanel() {
        GridBagConstraints gridBagConstraints;

        JLabel label1 = new JLabel();
        fileNameTextField = new JTextField();
        JButton oKButton = new JButton();
        JButton cancelButton = new JButton();

        setLayout(new GridBagLayout());

        label1.setText("Please enter a name for the save game.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(label1, gridBagConstraints);

        fileNameTextField.setText("savegame");
        fileNameTextField.addActionListener(SaveGamePanel::fileNameTextFieldActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(fileNameTextField, gridBagConstraints);

        oKButton.setText("OK");
        oKButton.addActionListener(this::oKButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(oKButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(cancelButton, gridBagConstraints);
    }

    private static void fileNameTextFieldActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
        System.out.println("fileNameTextFieldActionPerformed" + evt.toString());
    }


    private void oKButtonActionPerformed(ActionEvent evt) {

        String filename = fileNameTextField.getText();
        // Save the current game using the string
        modelRoot.setProperty(Property.QUICK_MESSAGE, "Saved game " + filename);
        MessageToServer message2 = new SaveGameMessageToServer(1, filename + ".sav");

        modelRoot.sendCommand(message2);
        close.actionPerformed(evt);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        close.actionPerformed(evt);
    }

    /**
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        close = closeAction;
        this.modelRoot = modelRoot;
    }


}

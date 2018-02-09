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
 * LoadGameJPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.network.LoadGameMessageToServer;
import freerails.network.MessageToServer;
import freerails.network.RefreshListOfGamesMessageToServer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 */
public class LoadGameJPanel extends JPanel implements View {

    private static final long serialVersionUID = -6810248272441137826L;
    private ModelRoot modelRoot;
    private ActionListener close;

    private JList list1;
    private JButton okButton;
    private List<String> lastFiles;

    /**
     * Creates new form LoadGameJPanel
     */
    public LoadGameJPanel() {
        GridBagConstraints gridBagConstraints;

        JScrollPane jScrollPane1 = new JScrollPane();
        list1 = new JList();
        JLabel label1 = new JLabel();
        okButton = new JButton();
        JButton cancelButton = new JButton();
        JButton refreshButton = new JButton();

        setLayout(new GridBagLayout());

        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.addListSelectionListener(this::list1ValueChanged);

        jScrollPane1.setViewportView(list1);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(jScrollPane1, gridBagConstraints);

        label1.setText("Please select a game to load.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(label1, gridBagConstraints);

        okButton.setText("OK");
        okButton.addActionListener(this::okButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(cancelButton, gridBagConstraints);

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(this::refreshButtonActionPerformed);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(7, 7, 7, 7);
        add(refreshButton, gridBagConstraints);
    }


    private void refreshButtonActionPerformed(ActionEvent evt) {
        MessageToServer refreshGames = new RefreshListOfGamesMessageToServer(2);
        modelRoot.sendCommand(refreshGames);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        if (null != close) close.actionPerformed(evt);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        String filename = (String) list1.getSelectedValue();
        MessageToServer message2 = new LoadGameMessageToServer(1, filename);
        modelRoot.sendCommand(message2);

        if (null != close) close.actionPerformed(evt);
    }

    private void list1ValueChanged(ListSelectionEvent evt) {
        okButton.setEnabled(list1.getSelectedIndex() != -1);
    }

    /**
     * @param modelRoot
     * @param vl
     * @param closeAction
     */
    public void setup(ModelRoot modelRoot, RendererRoot vl, Action closeAction) {
        close = closeAction;
        this.modelRoot = modelRoot;
        updateListOfFiles();
    }

    private void updateListOfFiles() {
        List<String> files = (List<String>) modelRoot.getProperty(Property.SAVED_GAMES_LIST);
        Object[] saves = new Object[files.size()];
        for (int i = 0; i < files.size(); i++) {
            saves[i] = files.get(i);
        }
        list1.setListData(saves);
        okButton.setEnabled(list1.getSelectedIndex() != -1);
        lastFiles = files;
    }

    @Override
    protected void paintComponent(Graphics g) {
        List<String> files = (List<String>) modelRoot.getProperty(Property.SAVED_GAMES_LIST);
        if (!lastFiles.equals(files)) {
            updateListOfFiles();
        }
        super.paintComponent(g);
    }


}

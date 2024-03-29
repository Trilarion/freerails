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
 * SelectEnginePanel.java
 *
 */

package freerails.client.view;

import freerails.client.StaticListModel;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.TrainCellRenderer;
import freerails.client.ModelRoot;
import freerails.model.train.Engine;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Lets the user select an engine from a list.
 */
public class SelectEnginePanel extends JPanel implements View {

    private static final long serialVersionUID = 4122537730158179638L;
    private JButton cancelButton;
    private JList<Engine> list1;
    private JButton okButton;

    public SelectEnginePanel() {
        GridBagConstraints gridBagConstraints;

        okButton = new JButton();
        cancelButton = new JButton();
        JScrollPane jScrollPane1 = new JScrollPane();
        list1 = new JList<>();

        setLayout(new GridBagLayout());

        setPreferredSize(new Dimension(400, 350));
        okButton.setText("OK");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 10, 9, 10);
        add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);
        add(cancelButton, gridBagConstraints);

        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.addListSelectionListener(this::list1ValueChanged);

        jScrollPane1.setViewportView(list1);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        list1ValueChanged(null); // Disable the success button if no engine type
        // is selected.
    }


    private void list1ValueChanged(ListSelectionEvent evt) {
        // We need to disable the OK button if no engine type is selected.

        okButton.setEnabled(-1 != list1.getSelectedIndex());
    }


    /**
     * @param modelRoot
     * @param rendererRoot
     * @param closeAction
     */

    @Override
    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {

        list1.setModel(new StaticListModel<>(modelRoot.getWorld().getEngines()));
        // list1.setModel(new WorldToListModelAdapter(modelRoot.getWorld(), SharedKey.EngineTypes));
        list1.setCellRenderer(new TrainCellRenderer(rendererRoot));
        okButton.addActionListener(closeAction);
    }

    /**
     * Removes any existing ActionListener listeners from the cancel button,
     * then adds the specified one.
     */
    void setCancelButtonActionListener(ActionListener l) {
        ActionListener[] oldListeners = cancelButton.getActionListeners();
        for (ActionListener oldListener : oldListeners) {
            cancelButton.removeActionListener(oldListener);
        }
        cancelButton.addActionListener(l);
    }

    /**
     * Returns the number of the currently selected engine type.
     */
    public int getSelectedEngineId() {
        return list1.getSelectedValue().getId();
    }

}

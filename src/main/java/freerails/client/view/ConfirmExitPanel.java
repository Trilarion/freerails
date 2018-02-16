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
 * ConfirmExitPanel.java
 *
 */

package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.client.ModelRoot;

import javax.swing.*;
import java.awt.*;

/**
 * Displays confirmation of exiting, used when the exit menu item is
 * selected or x is pressed.
 */
public class ConfirmExitPanel extends JPanel implements View {

    private static final long serialVersionUID = 3256728398394110517L;
    private JButton closeJButton;

    /**
     * Creates new form ConfirmExitPanel.
     */
    public ConfirmExitPanel() {
        GridBagConstraints gridBagConstraints;

        JPanel jPanel1 = new JPanel();
        JLabel label1 = new JLabel();
        JPanel jPanel2 = new JPanel();
        JButton confirmExit = new JButton();
        closeJButton = new JButton();

        setLayout(new GridBagLayout());

        setPreferredSize(new Dimension(240, 140));
        label1.setText("Are you sure you want to Exit?");
        label1.setHorizontalTextPosition(SwingConstants.LEFT);
        jPanel1.add(label1);

        add(jPanel1, new GridBagConstraints());

        jPanel2.setLayout(new GridBagLayout());

        confirmExit.setText("Exit");
        confirmExit.setContentAreaFilled(false);
        confirmExit.addActionListener(evt -> System.exit(0));

        jPanel2.add(confirmExit, new GridBagConstraints());

        closeJButton.setText("Cancel");
        jPanel2.add(closeJButton, new GridBagConstraints());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 15;
        gridBagConstraints.insets = new Insets(3, 0, 0, 0);
        add(jPanel2, gridBagConstraints);
    }

    public void setup(ModelRoot modelRoot, RendererRoot rendererRoot, Action closeAction) {
        closeJButton.setAction(closeAction);
    }
}

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
 * MyGlassPanel.java
 *
 */
package freerails.client.common;

import javax.swing.*;
import java.awt.*;

/**
 * A transparent JPanel that catches key presses and mouse clicks.
 */
public class MyGlassPanel extends JPanel {

    private static final long serialVersionUID = 3976735856986239795L;

    /**
     *
     */
    public MyGlassPanel() {
        JComponent contentPanel = new JPanel();
        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints1;
        setOpaque(false);

        contentPanel.setPreferredSize(new Dimension(60, 40));
        contentPanel.setMinimumSize(new Dimension(60, 40));
        contentPanel.setBackground(Color.red);
        contentPanel.setMaximumSize(new Dimension(60, 40));

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 1;
        add(contentPanel, gridBagConstraints1);
    }

}
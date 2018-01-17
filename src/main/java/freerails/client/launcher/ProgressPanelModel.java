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
 * ProgressPanelModel.java
 *
 */

package freerails.client.launcher;

import freerails.client.ProgressMonitorModel;

import javax.swing.*;
import java.awt.*;

/**
 * Displays a splash screen and a progress bar.
 */
public class ProgressPanelModel extends JPanel implements ProgressMonitorModel {

    private static final int numSteps = 5;
    private static final long serialVersionUID = 3256445798203273776L;
    private final LauncherInterface owner;
    private int step;
    private int stepSize;
    private JProgressBar progressBar;

    /**
     * Creates new form ProgressPanelModel
     */
    public ProgressPanelModel(LauncherInterface owner) {
        this.owner = owner;
        GridBagConstraints gridBagConstraints;

        progressBar = new JProgressBar();
        JLabel splashImage = new JLabel();

        setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(3, 7, 3, 7);
        add(progressBar, gridBagConstraints);

        splashImage.setHorizontalAlignment(SwingConstants.CENTER);
        splashImage.setIcon(new ImageIcon(getClass().getResource("/freerails/client/graphics/splash_screen.jpg")));
        add(splashImage, new GridBagConstraints());
        progressBar.setMaximum(numSteps * 100);
    }

    /**
     * @param i
     */
    public void setValue(int i) {
        int value = i * 100 / stepSize;
        value += 100 * step;
        progressBar.setValue(value);
    }

    /**
     * @param max
     */
    public void nextStep(int max) {

        // So that the waiting for game to start message
        // goes away.
        owner.hideAllMessages();

        step++;
        stepSize = max;
        if (numSteps < step) throw new IllegalStateException();
    }

    /**
     *
     */
    public void finished() {
        if (numSteps - 1 != step) throw new IllegalStateException(numSteps + "!=" + step);

        getTopLevelAncestor().setVisible(false);
    }

}

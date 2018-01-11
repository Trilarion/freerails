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

/**
 * A JPanel that displays a splash screen and a progress bar.
 */
public class ProgressPanelModel extends javax.swing.JPanel implements ProgressMonitorModel {

    static final int numSteps = 5;
    private static final long serialVersionUID = 3256445798203273776L;
    final LauncherInterface owner;
    int step, stepSize;
    javax.swing.JProgressBar progressBar;
    javax.swing.JLabel splashImage;

    /**
     * Creates new form ProgressPanelModel
     */
    public ProgressPanelModel(LauncherInterface owner) {
        this.owner = owner;
        initComponents();
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

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        progressBar = new javax.swing.JProgressBar();
        splashImage = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 3, 7);
        add(progressBar, gridBagConstraints);

        splashImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        splashImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/freerails/client/graphics/splash_screen.jpg")));
        add(splashImage, new java.awt.GridBagConstraints());
    }
}

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
 * ClientFrame.java
 *
 */

package freerails.client.view;

import freerails.client.GUIComponentFactory;
import freerails.client.GUIComponentFactoryTestImpl;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The JFrame that you see while you are playing the game.
 */
public class ClientFrame extends JFrame {

    private static final long serialVersionUID = 3834868100742265142L;
    private GUIComponentFactory gUIComponentFactory;

    /**
     * Creates new form ClientFrame.
     */
    public ClientFrame(GUIComponentFactory gcf) {
        setup(gcf);
    }

    /**
     * @param args
     */
    public static void main(String args[]) {
        new ClientFrame(new GUIComponentFactoryTestImpl()).setVisible(true);
    }

    /**
     * Exit the Application.
     */

    private static void exitForm(WindowEvent evt) {
        System.exit(0);
    }

    private void setup(GUIComponentFactory gcf) {
        gUIComponentFactory = gcf;
        initComponents();
        gUIComponentFactory.createDateJLabel();
    }


    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JPanel rhsjPanel = new javax.swing.JPanel();
        javax.swing.JPanel mapOverview = gUIComponentFactory.createOverviewMap();
        javax.swing.JTabbedPane trainsJTabPane1 = gUIComponentFactory.createTrainsJTabPane();

        javax.swing.JPanel lhsjPanel = new javax.swing.JPanel();
        javax.swing.JScrollPane mainMapView = gUIComponentFactory.createMainMap();
        javax.swing.JPanel statusjPanel = new javax.swing.JPanel();
        javax.swing.JLabel datejLabel = gUIComponentFactory.createDateJLabel();
        javax.swing.JLabel cashjLabel = gUIComponentFactory.createCashJLabel();
        javax.swing.JMenuBar jMenuBar1 = new javax.swing.JMenuBar();
        javax.swing.JMenu gameMenu = gUIComponentFactory.createGameMenu();
        javax.swing.JMenu buildMenu = gUIComponentFactory.createBuildMenu();
        javax.swing.JMenu brokerMenu1 = gUIComponentFactory.createBrokerMenu();
        javax.swing.JMenu displayMenu = gUIComponentFactory.createDisplayMenu();
        javax.swing.JMenu reportsMenu = gUIComponentFactory.createReportsMenu();
        javax.swing.JMenu helpMenu = gUIComponentFactory.createHelpMenu();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        addWindowListener(new MyWindowAdapter());

        rhsjPanel.setLayout(new java.awt.GridBagLayout());

        rhsjPanel.add(mapOverview, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        rhsjPanel.add(trainsJTabPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(rhsjPanel, gridBagConstraints);

        lhsjPanel.setLayout(new java.awt.GridBagLayout());

        mainMapView.setAlignmentX(0.0F);
        mainMapView.setAlignmentY(0.0F);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        lhsjPanel.add(mainMapView, gridBagConstraints);

        statusjPanel.add(datejLabel);

        statusjPanel.add(cashjLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        lhsjPanel.add(statusjPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(lhsjPanel, gridBagConstraints);

        gameMenu.setText("Game");
        jMenuBar1.add(gameMenu);

        buildMenu.setText("Build");
        jMenuBar1.add(buildMenu);

        brokerMenu1.setText("Broker");
        jMenuBar1.add(brokerMenu1);

        displayMenu.setText("Display");
        jMenuBar1.add(displayMenu);

        reportsMenu.setText("Reports");
        jMenuBar1.add(reportsMenu);

        helpMenu.setText("Help");
        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }

    private static class MyWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            exitForm(e);
        }
    }
}

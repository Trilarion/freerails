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

import freerails.client.componentfactory.GUIComponentFactory;
import freerails.client.componentfactory.GUIComponentFactoryTestImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The JFrame that you see while you are playing the game.
 */
public class ClientFrame extends JFrame {

    private static final long serialVersionUID = 3834868100742265142L;

    /**
     * Creates new form ClientFrame.
     */
    public ClientFrame(GUIComponentFactory guiComponentFactory) {
        GridBagConstraints gridBagConstraints;

        JPanel rhsjPanel = new JPanel();
        JPanel mapOverview = guiComponentFactory.createOverviewMap();
        JTabbedPane trainsJTabPane1 = guiComponentFactory.createTrainsJTabPane();

        JPanel lhsjPanel = new JPanel();
        JScrollPane mainMapView = guiComponentFactory.createMainMap();
        JPanel statusjPanel = new JPanel();
        JLabel datelabel = guiComponentFactory.createDateJLabel();
        JLabel cashlabel = guiComponentFactory.createCashJLabel();
        JMenuBar jMenuBar1 = new JMenuBar();
        JMenu gameMenu = guiComponentFactory.createGameMenu();
        JMenu buildMenu = guiComponentFactory.createBuildMenu();
        JMenu brokerMenu1 = guiComponentFactory.createBrokerMenu();
        JMenu displayMenu = guiComponentFactory.createDisplayMenu();
        JMenu reportsMenu = guiComponentFactory.createReportsMenu();
        JMenu helpMenu = guiComponentFactory.createHelpMenu();

        getContentPane().setLayout(new GridBagLayout());

        addWindowListener(new MyWindowAdapter());

        rhsjPanel.setLayout(new GridBagLayout());

        rhsjPanel.add(mapOverview, new GridBagConstraints());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        rhsjPanel.add(trainsJTabPane1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(rhsjPanel, gridBagConstraints);

        lhsjPanel.setLayout(new GridBagLayout());

        mainMapView.setAlignmentX(0.0F);
        mainMapView.setAlignmentY(0.0F);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        lhsjPanel.add(mainMapView, gridBagConstraints);

        statusjPanel.add(datelabel);

        statusjPanel.add(cashlabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        lhsjPanel.add(statusjPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
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
        guiComponentFactory.createDateJLabel();
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


    private static class MyWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            exitForm(e);
        }
    }
}

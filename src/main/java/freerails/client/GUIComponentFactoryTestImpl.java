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
 * GUIComponentFactoryTestImpl.java
 *
 */
package freerails.client;

import javax.swing.*;

/**
 * Implementation of GUIComponentFactory that returns 'blank' components - used
 * for testing the layout of ClientJFrame.
 */
public class GUIComponentFactoryTestImpl implements GUIComponentFactory {
    private final JLabel datejLabel;

    private final JLabel cashjLabel;

    private final JTabbedPane trainsJPanel;

    private final JMenu displayMenu;

    private final JScrollPane mainMapView;

    private final JMenu buildMenu;

    private final JMenu gameMenu;

    private final JPanel mapOverview;

    private final JMenu helpMenu;

    private final JMenu brokerMenu;

    /**
     * Creates a new instance of GUIComponentFactoryTestImpl.
     */
    public GUIComponentFactoryTestImpl() {
        JPanel mainmapjPanel;

        trainsJPanel = new JTabbedPane();
        datejLabel = new JLabel();
        mapOverview = new JPanel();
        cashjLabel = new JLabel();
        mainMapView = new JScrollPane();
        mainmapjPanel = new JPanel();
        JLabel messageJLabel = new JLabel();
        gameMenu = new JMenu();
        buildMenu = new JMenu();
        displayMenu = new JMenu();
        helpMenu = new JMenu();
        brokerMenu = new JMenu();

        trainsJPanel.setBackground(new java.awt.Color(255, 51, 51));
        datejLabel.setText("Jun, 1840");
        mapOverview.setBackground(new java.awt.Color(0, 204, 255));
        mapOverview.setPreferredSize(new java.awt.Dimension(100, 100));
        cashjLabel.setText("$100,000");
        mainmapjPanel.setBackground(new java.awt.Color(153, 244, 51));
        mainMapView.setViewportView(mainmapjPanel);
        messageJLabel.setText("message");
    }

    /**
     * @return
     */
    public JMenu createReportsMenu() {
        return new JMenu("Reports");
    }

    /**
     * @return
     */
    public JMenu createBuildMenu() {
        return buildMenu;
    }

    /**
     * @return
     */
    public JLabel createCashJLabel() {
        return cashjLabel;
    }

    /**
     * @return
     */
    public JLabel createDateJLabel() {
        return datejLabel;
    }

    /**
     * @return
     */
    public JMenu createDisplayMenu() {
        return displayMenu;
    }

    /**
     * @return
     */
    public JMenu createGameMenu() {
        return gameMenu;
    }

    /**
     * @return
     */
    public JMenu createHelpMenu() {
        return helpMenu;
    }

    /**
     * @return
     */
    public JScrollPane createMainMap() {
        return mainMapView;
    }

    /**
     * @return
     */
    public JPanel createOverviewMap() {
        return mapOverview;
    }

    /**
     * @return
     */
    public JTabbedPane createTrainsJTabPane() {
        return trainsJPanel;
    }

    /**
     * @return
     */
    public JMenu createBrokerMenu() {
        return brokerMenu;
    }
}
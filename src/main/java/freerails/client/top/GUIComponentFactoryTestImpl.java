/*
 * GUIComponentFactoryTestImpl.java
 *
 * Created on 01 June 2003, 17:30
 */
package freerails.client.top;

import javax.swing.*;

/**
 * Implementation of GUIComponentFactory that returns 'blank' components - used
 * for testing the layout of ClientJFrame.
 *
 * @author Luke
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

    private final JLabel messageJLabel;

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
        messageJLabel = new JLabel();
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

    public JMenu createReportsMenu() {
        return new JMenu("Reports");
    }

    public JMenu createBuildMenu() {
        return buildMenu;
    }

    public JLabel createCashJLabel() {
        return cashjLabel;
    }

    public JLabel createDateJLabel() {
        return datejLabel;
    }

    public JMenu createDisplayMenu() {
        return displayMenu;
    }

    public JMenu createGameMenu() {
        return gameMenu;
    }

    public JMenu createHelpMenu() {
        return helpMenu;
    }

    public JScrollPane createMainMap() {
        return mainMapView;
    }

    public JPanel createOverviewMap() {
        return mapOverview;
    }

    public JTabbedPane createTrainsJTabPane() {
        return trainsJPanel;
    }

    public JMenu createBrokerMenu() {
        return brokerMenu;
    }
}
package freerails.client.top;

import javax.swing.*;

/**
 * Defines methods that create the GUI components used by the game.
 *
 * @author Luke
 */
public interface GUIComponentFactory {
    JPanel createOverviewMap();

    JTabbedPane createTrainsJTabPane();

    JScrollPane createMainMap();

    JLabel createCashJLabel();

    JLabel createDateJLabel();

    JMenu createBuildMenu();

    JMenu createReportsMenu();

    JMenu createGameMenu();

    JMenu createDisplayMenu();

    JMenu createHelpMenu();

    JMenu createBrokerMenu();
}
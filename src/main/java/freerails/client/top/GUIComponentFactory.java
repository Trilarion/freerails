package freerails.client.top;

import javax.swing.*;

/**
 * Defines methods that create the GUI components used by the game.
 *
 */
public interface GUIComponentFactory {

    /**
     *
     * @return
     */
    JPanel createOverviewMap();

    /**
     *
     * @return
     */
    JTabbedPane createTrainsJTabPane();

    /**
     *
     * @return
     */
    JScrollPane createMainMap();

    /**
     *
     * @return
     */
    JLabel createCashJLabel();

    /**
     *
     * @return
     */
    JLabel createDateJLabel();

    /**
     *
     * @return
     */
    JMenu createBuildMenu();

    /**
     *
     * @return
     */
    JMenu createReportsMenu();

    /**
     *
     * @return
     */
    JMenu createGameMenu();

    /**
     *
     * @return
     */
    JMenu createDisplayMenu();

    /**
     *
     * @return
     */
    JMenu createHelpMenu();

    /**
     *
     * @return
     */
    JMenu createBrokerMenu();
}
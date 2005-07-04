package jfreerails.client.top;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

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
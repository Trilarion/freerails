package jfreerails.client.top;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;

public interface GUIComponentFactory {

	JFrame createClientJFrame();

	JPanel createOverviewMap();

	JTabbedPane createTrainsJTabPane();

	JScrollPane createMainMap();

	JLabel createMessagePanel();

	JLabel createCashJLabel();

	JLabel createDateJLabel();

	JMenu createBuildMenu();

	JMenu createGameMenu();

	JMenu createDisplayMenu();

	JMenu createHelpMenu();

}

package jfreerails.client.top;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;


public interface GUIComponentFactory {
    JPanel createOverviewMap();

    JTabbedPane createTrainsJTabPane();

    JScrollPane createMainMap();

    JLabel createCashJLabel();

    JLabel createDateJLabel();

    JMenu createBuildMenu();

    JMenu createGameMenu();

    JMenu createDisplayMenu();

    JMenu createHelpMenu();
}
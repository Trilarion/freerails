package jfreerails.client.top;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;

public interface GUIComponentFactory {
	
	JFrame createClientJFrame();
	
	JComponent createOverviewMap();
	
	JComponent createMainMap();
	
	JLabel createMessagePanel();
	
	JMenu createBuildMenu();
	
	JMenu createGameMenu();
	
	JMenu createDisplayMenu();
	
}

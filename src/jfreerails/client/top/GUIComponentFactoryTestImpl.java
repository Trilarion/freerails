/*
 * GUIComponentFactoryTestImpl.java
 *
 * Created on 01 June 2003, 17:30
 */

package jfreerails.client.top;
import javax.swing.*;
/**
 *
 * @author  Luke
 */
public class GUIComponentFactoryTestImpl implements GUIComponentFactory {
    
    private javax.swing.JLabel datejLabel;
    private javax.swing.JLabel cashjLabel;
    private javax.swing.JTabbedPane trainsJPanel;
    private javax.swing.JMenu displayMenu;
    private javax.swing.JScrollPane mainMapView;
    private javax.swing.JMenu buildMenu;
    private javax.swing.JMenu gameMenu;
    private javax.swing.JPanel mapOverview;
    private javax.swing.JPanel rhsjPanel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel messageJLabel;
    private javax.swing.JMenuBar jMenuBar1;
    
    
    
    /** Creates a new instance of GUIComponentFactoryTestImpl */
    public GUIComponentFactoryTestImpl() {
         javax.swing.JPanel mainmapjPanel;
        
        trainsJPanel = new javax.swing.JTabbedPane();
        datejLabel = new javax.swing.JLabel();
        mapOverview = new javax.swing.JPanel();
        cashjLabel = new javax.swing.JLabel();
        mainMapView = new javax.swing.JScrollPane();
        mainmapjPanel = new javax.swing.JPanel();
        messageJLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        gameMenu = new javax.swing.JMenu();
        buildMenu = new javax.swing.JMenu();
        displayMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        
        trainsJPanel.setBackground(new java.awt.Color(255, 51, 51));
        datejLabel.setText("Jun, 1840");
        mapOverview.setBackground(new java.awt.Color(0, 204, 255));
        mapOverview.setPreferredSize(new java.awt.Dimension(100, 100));
        cashjLabel.setText("$100,000");
        mainmapjPanel.setBackground(new java.awt.Color(153, 244, 51));
        mainMapView.setViewportView(mainmapjPanel);
        messageJLabel.setText("message");
    }
    
    public JMenu createBuildMenu() {
        return buildMenu;
    }
    
    public JLabel createCashJLabel() {
        return cashjLabel;
    }
    
    public JFrame createClientJFrame() {
        return null;
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
    
    public JLabel createMessagePanel() {
        return messageJLabel;
    }
    
    public JPanel createOverviewMap() {
        return mapOverview;
    }
    
    public JTabbedPane createTrainsJTabPane() {
        return trainsJPanel;
    }
    
}

/*
 * SimpleComponentFactoryImpl2.java
 *
 * Created on 23 June 2002, 02:36
 */
package experimental;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import jfreerails.client.renderer.BlankMapRenderer;
import jfreerails.client.view.MainMapAndOverviewMapMediator;
import jfreerails.client.view.MapViewJComponentConcrete;
import jfreerails.client.view.OverviewMapJComponent;


/**
 *        This GUIComponentFactory creates simple components that can be used to test the layout of the client jFrame without
 *running the whole game.
 *
 * @author  Luke Lindsay
 */
public class SimpleComponentFactoryImpl2
    implements jfreerails.client.top.GUIComponentFactory {
    private OverviewMapJComponent overviewMap;
    private JScrollPane mainMapScrollPane1;
    private MapViewJComponentConcrete mainMap;
    private MainMapAndOverviewMapMediator mediator;
    private Rectangle r = new Rectangle();

    /** Creates new SimpleComponentFactoryImpl */
    public SimpleComponentFactoryImpl2() {
    }

    public JMenu createBuildMenu() {
        return new JMenu("Build");
    }

    public JMenu createGameMenu() {
        return new JMenu("Game");
    }

    public JMenu createDisplayMenu() {
        JMenu displayMenu = new JMenu("Display");

        addMainmapzoomMenuItem(displayMenu, 5);
        addMainmapzoomMenuItem(displayMenu, 10);

        addOverviewmapzoomMenuItem(displayMenu, 0.2F);
        addOverviewmapzoomMenuItem(displayMenu, 0.6F);

        return displayMenu;
    }

    private void addOverviewmapzoomMenuItem(JMenu displayMenu, final float scale) {
        String menuItemName = "Set overview map scale=" + scale;
        JMenuItem menuItem = new JMenuItem(menuItemName);
        menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    overviewMap.setup(new BlankMapRenderer(scale));
                }
            });
        displayMenu.add(menuItem);
    }

    private void addMainmapzoomMenuItem(JMenu displayMenu, final float scale) {
        String menuItemName = "Set main map scale=" + scale;
        JMenuItem menuItem = new JMenuItem(menuItemName);
        menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Rectangle visRect = mainMap.getVisibleRect();

                    int oldWidth = mainMap.getWidth();
                    mainMap.setup(new BlankMapRenderer(scale));

                    int newWidth = mainMap.getPreferredSize().width;

                    int oldCenterX = visRect.x + (visRect.width / 2);
                    int newCenterX = oldCenterX * newWidth / oldWidth;
                    visRect.x = newCenterX - visRect.width / 2;

                    int oldCenterY = visRect.y + (visRect.height / 2);
                    int newCenterY = oldCenterY * newWidth / oldWidth;
                    visRect.y = newCenterY - visRect.height / 2;

                    /* LL: I'm not sure why the 'if' is necessary in the following, but
                     * the view does not center on the right spot without it.
                     */
                    if (oldWidth < newWidth) {
                        mainMap.setSize(mainMap.getPreferredSize());
                        mainMap.scrollRectToVisible(visRect);
                    } else {
                        mainMap.scrollRectToVisible(visRect);
                        mainMap.setSize(mainMap.getPreferredSize());
                    }
                }
            });
        displayMenu.add(menuItem);
    }

    public JScrollPane createMainMap() {
        if (null == this.mainMap) {
            //this.mainMap = new MapJPanel();
            this.mainMap = new MapViewJComponentConcrete();
            mainMapScrollPane1 = new JScrollPane();
            mainMapScrollPane1.setViewportView(this.mainMap);
            addMainMapAndOverviewMapMediatorIfNecessary();
        }

        return mainMapScrollPane1;
    }

    public JPanel createOverviewMap() {
        if (null == this.overviewMap) {
            //this.overviewMap = new OverviewMapJPanel();
            this.overviewMap = new OverviewMapJComponent(r);
            this.overviewMap.setup(new BlankMapRenderer(0.4F));
            addMainMapAndOverviewMapMediatorIfNecessary();
        }

        return overviewMap;
        // return new TestPanel();
    }

    private void addMainMapAndOverviewMapMediatorIfNecessary() {
        if (this.mainMap != null && this.overviewMap != null &&
                null == this.mediator) {
            //Rectangle r = this.overviewMap.getMainMapVisibleRect();
            this.mediator = new MainMapAndOverviewMapMediator(overviewMap,
                    mainMapScrollPane1.getViewport(), mainMap, r);
        }
    }

    public JLabel createCashJLabel() {
        return null;
    }

    public JLabel createDateJLabel() {
        return null;
    }

    public JMenu createHelpMenu() {
        return null;
    }

    public JTabbedPane createTrainsJTabPane() {
        return null;
    }
}
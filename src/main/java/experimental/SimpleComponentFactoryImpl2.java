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
 * SimpleComponentFactoryImpl2.java
 *
 */
package experimental;

import freerails.client.GUIComponentFactory;
import freerails.client.renderer.BlankMapRenderer;
import freerails.client.view.MainMapAndOverviewMapMediator;
import freerails.client.view.MapViewComponentConcrete;
import freerails.client.view.OverviewMapComponent;

import javax.swing.*;
import java.awt.*;

/**
 * This GUIComponentFactory creates simple components that can be used to test
 * the layout of the client jFrame without running the whole game.
 */
public class SimpleComponentFactoryImpl2 implements GUIComponentFactory {
    private final Rectangle r = new Rectangle();
    private OverviewMapComponent overviewMap;
    private JScrollPane mainMapScrollPane1;
    private MapViewComponentConcrete mainMap;
    private MainMapAndOverviewMapMediator mediator;

    /**
     * Creates new SimpleComponentFactoryImpl
     */
    public SimpleComponentFactoryImpl2() {
    }

    /**
     * @return
     */
    public JMenu createBuildMenu() {
        return new JMenu("Build");
    }

    /**
     * @return
     */
    public JMenu createGameMenu() {
        return new JMenu("Game");
    }

    /**
     * @return
     */
    public JMenu createDisplayMenu() {
        JMenu displayMenu = new JMenu("Display");

        addMainmapzoomMenuItem(displayMenu, 5);
        addMainmapzoomMenuItem(displayMenu, 10);

        addOverviewmapzoomMenuItem(displayMenu, 0.2F);
        addOverviewmapzoomMenuItem(displayMenu, 0.6F);

        return displayMenu;
    }

    /**
     * @return
     */
    public JMenu createBrokerMenu() {
        return new JMenu("Broker");
    }

    private void addOverviewmapzoomMenuItem(JMenu displayMenu, final float scale) {
        String menuItemName = "Set overview map scale=" + scale;
        JMenuItem menuItem = new JMenuItem(menuItemName);
        menuItem.addActionListener(e -> overviewMap.setup(new BlankMapRenderer(scale)));
        displayMenu.add(menuItem);
    }

    private void addMainmapzoomMenuItem(JMenu displayMenu, final float scale) {
        String menuItemName = "Set main map scale=" + scale;
        JMenuItem menuItem = new JMenuItem(menuItemName);
        menuItem.addActionListener(e -> {
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

            /*
             * LL: I'm not sure why the 'if' is necessary in the following,
             * but the view does not center on the right spot without it.
             */
            if (oldWidth < newWidth) {
                mainMap.setSize(mainMap.getPreferredSize());
                mainMap.scrollRectToVisible(visRect);
            } else {
                mainMap.scrollRectToVisible(visRect);
                mainMap.setSize(mainMap.getPreferredSize());
            }
        });
        displayMenu.add(menuItem);
    }

    /**
     * @return
     */
    public JScrollPane createMainMap() {
        if (null == mainMap) {
            mainMap = new MapViewComponentConcrete();
            mainMapScrollPane1 = new JScrollPane();
            mainMapScrollPane1.setViewportView(mainMap);
            addMainMapAndOverviewMapMediatorIfNecessary();
        }

        return mainMapScrollPane1;
    }

    /**
     * @return
     */
    public JPanel createOverviewMap() {
        if (null == overviewMap) {
            // this.overviewMap = new OverviewMapJPanel();
            overviewMap = new OverviewMapComponent(r);
            overviewMap.setup(new BlankMapRenderer(0.4F));
            addMainMapAndOverviewMapMediatorIfNecessary();
        }

        return overviewMap;
        // return new TestPanel();
    }

    private void addMainMapAndOverviewMapMediatorIfNecessary() {
        if (mainMap != null && overviewMap != null && null == mediator) {
            // Rectangle r = this.overviewMap.getMainMapVisibleRect();
            mediator = new MainMapAndOverviewMapMediator(overviewMap, mainMapScrollPane1.getViewport(), mainMap, r);
        }
    }

    /**
     * @return
     */
    public JLabel createCashJLabel() {
        return null;
    }

    /**
     * @return
     */
    public JLabel createDateJLabel() {
        return null;
    }

    /**
     * @return
     */
    public JMenu createHelpMenu() {
        return null;
    }

    /**
     * @return
     */
    public JTabbedPane createTrainsJTabPane() {
        return null;
    }

    /**
     * @return
     */
    public JMenu createReportsMenu() {
        // TODO Auto-generated method stub
        return null;
    }
}
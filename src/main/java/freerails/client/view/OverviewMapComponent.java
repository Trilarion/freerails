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

package freerails.client.view;

import freerails.client.renderer.BlankMapRenderer;
import freerails.client.renderer.MapRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel that displays the overview map and a rectangle showing the region of
 * the map currently displayed on the main view.
 */
public class OverviewMapComponent extends JPanel {

    private static final long serialVersionUID = 3258697585148376888L;
    private final Rectangle mainMapVisRect;
    private MapRenderer mapView = new BlankMapRenderer(0.4F);

    /**
     * @param r
     */
    public OverviewMapComponent(Rectangle r) {
        setPreferredSize(mapView.getMapSizeInPixels());
        mainMapVisRect = r;
    }

    /**
     * @param mv
     */
    public void setup(MapRenderer mv) {
        mapView = mv;
        setPreferredSize(mapView.getMapSizeInPixels());
        setMinimumSize(getPreferredSize());
        setSize(getPreferredSize());

        if (null != getParent()) {
            getParent().validate();
        }
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        java.awt.Rectangle r = getVisibleRect();
        mapView.paintRect(g2, r);
        g2.setColor(Color.WHITE);
        g2.drawRect(mainMapVisRect.x, mainMapVisRect.y, mainMapVisRect.width, mainMapVisRect.height);
    }

    @Override
    public Dimension getPreferredSize() {
        return mapView.getMapSizeInPixels();
    }
}
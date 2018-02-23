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

import freerails.client.renderer.map.BlankMapRenderer;
import freerails.client.renderer.map.MapRenderer;
import freerails.util.Vector2D;

import javax.swing.*;
import java.awt.*;

/**
* Displays the overview map and a rectangle showing the region of
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
        setPreferredSize(Vector2D.toDimension(mapView.getMapSizeInPixels()));
        mainMapVisRect = r;
    }

    /**
     * @param mapRenderer
     */
    public void setup(MapRenderer mapRenderer) {
        mapView = mapRenderer;
        setPreferredSize(Vector2D.toDimension(mapView.getMapSizeInPixels()));
        setMinimumSize(getPreferredSize());
        setSize(getPreferredSize());

        if (null != getParent()) {
            getParent().validate();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Rectangle r = getVisibleRect();
        mapView.paintRect(g2, r);
        g2.setColor(Color.WHITE);
        g2.drawRect(mainMapVisRect.x, mainMapVisRect.y, mainMapVisRect.width, mainMapVisRect.height);
    }

    @Override
    public Dimension getPreferredSize() {
        return Vector2D.toDimension(mapView.getMapSizeInPixels());
    }
}
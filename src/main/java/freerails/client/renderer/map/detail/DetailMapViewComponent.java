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
 *  MapViewComponent.java
 *
 *  Created on 06 August 2001, 14:12
 */
package freerails.client.renderer.map.detail;

import freerails.client.renderer.map.MapRenderer;
import freerails.util.Vec2D;

import javax.swing.*;
import java.awt.*;

// TODO What is the abstraction here?
/**
* Displays the map and provides methods to handle scrolling.
 */
public abstract class DetailMapViewComponent extends JPanel implements Scrollable, MapRenderer {

    private static final long serialVersionUID = 3588200012170257744L;
    private MapRenderer mapRenderer;

    /**
     *
     */
    public DetailMapViewComponent() {
        setAutoscrolls(true);
    }

    /**
     * @return
     */
    @Override
    public float getScale() {
        return mapRenderer.getScale();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Rectangle r = getVisibleRect();
        mapRenderer.paintRect(g2, r);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (int) mapRenderer.getScale();
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (SwingConstants.VERTICAL == orientation) {
            int best = (int) ((visibleRect.height / mapRenderer.getScale() - 2) * mapRenderer.getScale());

            if (best > 0) {
                return best;
            }
            return visibleRect.height;
        }
        float f = (visibleRect.width / mapRenderer.getScale() - 2) * mapRenderer.getScale();
        int best = (int) f;

        if (best > 0) {
            return best;
        }
        return visibleRect.width;
    }

    /**
     * Gets the scrollableTracksViewportHeight attribute of the
     * MapViewComponent object.
     *
     * @return The scrollableTracksViewportHeight value
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Gets the preferredScrollableViewportSize attribute of the
     * MapViewComponent object.
     *
     * @return The preferredScrollableViewportSize value
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * @param tile
     */
    public void centerOnTile(Vec2D tile) {
        float scale = mapRenderer.getScale();
        Rectangle visRect = new Rectangle(getVisibleRect());
        visRect.x = (int) (tile.x * scale - visRect.width / 2);
        visRect.y = (int) (tile.y * scale - visRect.height / 2);
        scrollRectToVisible(visRect);
    }

    /**
     * @return
     */
    @Override
    public Vec2D getMapSizeInPixels() {
        return mapRenderer.getMapSizeInPixels();
    }

    @Override
    public Dimension getPreferredSize() {
        return Vec2D.toDimension(getMapSizeInPixels());
    }

    public MapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public void setMapRenderer(MapRenderer mapRenderer) {
        this.mapRenderer = mapRenderer;
    }
}
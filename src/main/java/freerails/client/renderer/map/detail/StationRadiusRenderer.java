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

package freerails.client.renderer.map.detail;

import freerails.util.ui.Painter;
import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.client.ModelRootValue;
import freerails.model.WorldConstants;

import java.awt.*;

/**
 * Draws the radius of a station on the map.
 */
public class StationRadiusRenderer implements Painter {

    /**
     * Border colour to use when placement is OK.
     */
    public static final Color COLOR_OK = Color.WHITE;

    /**
     * Border colour to use when placement is not allowed.
     */
    public static final Color COLOR_CANNOT_BUILD = Color.RED;
    private static final int tileSize = WorldConstants.TILE_SIZE;
    private final ModelRoot modelRoot;
    /**
     * Colour of the highlighted border.
     */
    private Color borderColor = COLOR_OK;
    private int radius = 2;
    private int x;
    private int y;

    /**
     * @param modelRoot
     */
    public StationRadiusRenderer(ModelRoot modelRoot) {
        this.modelRoot = modelRoot;
    }

    /**
     * @param c
     */
    public void setBorderColor(Color c) {
        borderColor = c;
    }

    /**
     * @param x
     * @param y
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param radius
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     *
     */
    public void show() {
        if (!modelRoot.getProperty(ModelRootProperty.CURSOR_MODE).equals(ModelRootValue.PLACE_STATION_CURSOR_MODE)) {
            modelRoot.setProperty(ModelRootProperty.PREVIOUS_CURSOR_MODE, modelRoot.getProperty(ModelRootProperty.CURSOR_MODE));
            modelRoot.setProperty(ModelRootProperty.CURSOR_MODE, ModelRootValue.PLACE_STATION_CURSOR_MODE);
            modelRoot.setProperty(ModelRootProperty.IGNORE_KEY_EVENTS, Boolean.TRUE);
        }
    }

    /**
     *
     */
    public void hide() {
        ModelRootValue lastCursorMode = (ModelRootValue) modelRoot.getProperty(ModelRootProperty.PREVIOUS_CURSOR_MODE);

        assert lastCursorMode != ModelRootValue.PLACE_STATION_CURSOR_MODE;

        modelRoot.setProperty(ModelRootProperty.CURSOR_MODE, lastCursorMode);
        modelRoot.setProperty(ModelRootProperty.IGNORE_KEY_EVENTS, Boolean.FALSE);
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        if (modelRoot.getProperty(ModelRootProperty.CURSOR_MODE).equals(ModelRootValue.PLACE_STATION_CURSOR_MODE)) {
            g.setStroke(new BasicStroke(2.0f));
            g.setColor(borderColor);

            g.drawRect(tileSize * (x - radius), tileSize * (y - radius), tileSize * (2 * radius + 1), tileSize * (2 * radius + 1));
        }
    }
}
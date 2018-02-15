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

package freerails.client.renderer;

import freerails.util.ui.Painter;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.controller.ModelRoot.Value;
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
        if (!modelRoot.is(Property.CURSOR_MODE, Value.PLACE_STATION_CURSOR_MODE)) {
            modelRoot.setProperty(Property.PREVIOUS_CURSOR_MODE, modelRoot.getProperty(Property.CURSOR_MODE));
            modelRoot.setProperty(Property.CURSOR_MODE, Value.PLACE_STATION_CURSOR_MODE);
            modelRoot.setProperty(Property.IGNORE_KEY_EVENTS, Boolean.TRUE);
        }
    }

    /**
     *
     */
    public void hide() {
        ModelRoot.Value lastCursorMode = (ModelRoot.Value) modelRoot.getProperty(ModelRoot.Property.PREVIOUS_CURSOR_MODE);

        assert lastCursorMode != Value.PLACE_STATION_CURSOR_MODE;

        modelRoot.setProperty(ModelRoot.Property.CURSOR_MODE, lastCursorMode);
        modelRoot.setProperty(Property.IGNORE_KEY_EVENTS, Boolean.FALSE);
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        if (modelRoot.getProperty(ModelRoot.Property.CURSOR_MODE).equals(Value.PLACE_STATION_CURSOR_MODE)) {
            g.setStroke(new BasicStroke(2.0f));
            g.setColor(borderColor);

            g.drawRect(tileSize * (x - radius), tileSize * (y - radius), tileSize * (2 * radius + 1), tileSize * (2 * radius + 1));
        }
    }
}
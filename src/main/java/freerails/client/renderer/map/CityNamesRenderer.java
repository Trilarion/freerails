/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
 *
 * Class to render the city names on the game map. Names are retrieved
 * from the KEY.CITIES object.
 */
package freerails.client.renderer.map;

import freerails.model.terrain.city.City;
import freerails.client.ClientConstants;
import freerails.client.renderer.map.MapCoordinateAdapter;
import freerails.util.ui.Painter;
import freerails.util.Vector2D;
import freerails.model.world.UnmodifiableWorld;

import java.awt.*;

/**
 * Paints the city names on the map.
 */
public class CityNamesRenderer implements Painter {

    private final UnmodifiableWorld world;

    /**
     * Creates a city names renderer instance.
     *
     * @param world the game world being queried
     */
    public CityNamesRenderer(UnmodifiableWorld world) {
        this.world = world;
    }

    /**
     * Performs the paint operation.
     *
     * @param g the graphics context used for drawing
     * @param newVisibleRectangle the new visible rectangle parameter
     */
    @Override
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));

        // draw city names onto map
        for (City city: world.getCities()) {
            Vector2D location = city.getLocation();
            Vector2D tileOrigin = MapCoordinateAdapter.tileOrigin(location);
            final int xpos = tileOrigin.x();
            final int ypos = tileOrigin.y() + 10;
            Rectangle cityNameBox = new Rectangle(xpos, ypos, ClientConstants.TILE_PIXEL_SIZE * 8, 20);
            if (newVisibleRectangle != null && !newVisibleRectangle.intersects(cityNameBox)) {
                continue;
            }
            g.drawString(city.getName(), xpos, ypos);
        }
    }
}
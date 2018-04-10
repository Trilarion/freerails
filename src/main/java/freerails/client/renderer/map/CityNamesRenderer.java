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
 *
 * Class to render the city names on the game map. Names are retrieved
 * from the KEY.CITIES object.
 */
package freerails.client.renderer.map;

import freerails.util.ui.Painter;
import freerails.util.Vec2D;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.model.WorldConstants;
import freerails.model.terrain.City;

import java.awt.*;

/**
 * Paints the city names on the map.
 */
public class CityNamesRenderer implements Painter {

    private final ReadOnlyWorld world;

    /**
     * @param world
     */
    public CityNamesRenderer(ReadOnlyWorld world) {
        this.world = world;
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));

        // draw city names onto map
        for (int i = 0; i < world.size(SharedKey.Cities); i++) {
            City city = (City) world.get(SharedKey.Cities, i);
            Vec2D location = city.getLocation();
            final int xpos = location.x * WorldConstants.TILE_SIZE;
            final int ypos = location.y * WorldConstants.TILE_SIZE + 10;
            Rectangle cityNameBox = new Rectangle(xpos, ypos, WorldConstants.TILE_SIZE * 8, 20);
            if (newVisibleRectangle != null && !newVisibleRectangle.intersects(cityNameBox)) {
                continue;
            }
            g.drawString(city.getName(), xpos, ypos);
        }
    }
}
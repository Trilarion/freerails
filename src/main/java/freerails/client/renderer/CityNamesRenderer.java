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
package freerails.client.renderer;

import freerails.client.ClientConstants;
import freerails.client.common.Painter;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.terrain.City;

import java.awt.*;

/**
 * Paints the city names on the map.
 */
public class CityNamesRenderer implements Painter {
    private final ReadOnlyWorld w;

    /**
     * @param world
     */
    public CityNamesRenderer(ReadOnlyWorld world) {
        this.w = world;
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", 0, 20));

        // draw city names onto map
        int size = w.size(SKEY.CITIES);
        for (int i = 0; i < size; i++) {
            City tempCity = (City) w.get(SKEY.CITIES, i);
            final int xpos = tempCity.getCityX() * ClientConstants.TILE_SIZE;
            final int ypos = tempCity.getCityY() * ClientConstants.TILE_SIZE + 10;
            Rectangle cityNameBox = new Rectangle(xpos, ypos,
                    ClientConstants.TILE_SIZE * 8, 20);
            if (newVisibleRectangle != null
                    && !newVisibleRectangle.intersects(cityNameBox)) {
                continue;
            }
            g.drawString(tempCity.getCityName(), xpos, ypos);
        }
    }
}
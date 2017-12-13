/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.client.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import org.railz.client.common.Painter;


/** This class draws the radius of a station on the map */
public class StationRadiusRenderer implements Painter {
    /**
     * Border colour to use when placement is OK
     */
    public static final Color COLOR_OK = Color.WHITE;

    /**
     * Border colour to use when placement is not allowed
     */
    public static final Color COLOR_CANNOT_BUILD = Color.RED;

    /**
     * Colour of the highlighted border
     */
    private Color borderColor = COLOR_OK;
    int radius = 2;
    int x;
    int y;
    boolean show = false;

    public void setBorderColor(Color c) {
        borderColor = c;
    }

    public void show() {
        this.show = true;
    }

    public void hide() {
        this.show = false;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void paint(Graphics2D g) {
        if (show) {
            g.setStroke(new BasicStroke(2f));
            g.setColor(borderColor);

            g.drawRect(TileRenderer.TILE_SIZE.width * (x - radius), TileRenderer.TILE_SIZE.height * (y - radius),
                TileRenderer.TILE_SIZE.width * (2 * radius + 1), TileRenderer.TILE_SIZE.height * (2 * radius + 1));
        }
    }
}

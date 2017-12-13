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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;


public class BlankMapRenderer implements MapRenderer {
    private final float scale;

    public BlankMapRenderer(float s) {
        scale = s;
    }

    public float getScale() {
        return scale;
    }

    public Dimension getMapSizeInPixels() {
        int height = (int)(400 * scale);
        int width = (int)(400 * scale);

        return new Dimension(height, width);
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
        paintRect(g, null);
    }

    public void refreshTile(int x, int y) {
    }

    public void paintRect(Graphics g, Rectangle visibleRect) {
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, (int)(scale * 400), (int)(scale * 400));
        g.setColor(Color.blue);

        int x = (int)(100 * scale);
        int y = (int)(100 * scale);
        int height = (int)(200 * scale);
        int width = (int)(200 * scale);
        g.fillRect(x, y, height, width);
    }
}

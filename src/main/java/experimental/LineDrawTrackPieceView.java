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
 * LineDrawTrackPieceView.java
 *
 * Created on 09 October 2001, 23:53
 */
package experimental;

import freerails.client.common.ImageManager;

import java.awt.*;

/**
 * This TrackPieceRenderer renders track pieces by drawing lines so avoids the
 * need to load images.
 */
public class LineDrawTrackPieceView implements
        freerails.client.renderer.TrackPieceRenderer {
    private final int[] xx = {-1, 0, 1, -1, 0, 1, -1, 0, 1};

    private final int[] yy = {-1, -1, -1, 0, 0, 0, 1, 1, 1};

    /**
     * @param trackTemplate
     * @return
     */
    public java.awt.Image getTrackPieceIcon(int trackTemplate) {
        return null;
    }

    /**
     * @param trackTemplate
     * @param g
     * @param x
     * @param y
     * @param tileSize
     */
    public void drawTrackPieceIcon(int trackTemplate, java.awt.Graphics g,
                                   int x, int y, java.awt.Dimension tileSize) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new java.awt.BasicStroke(8.0f));
        g2.setColor(java.awt.Color.red);

        if (0 != trackTemplate) {
            int drawX = x * tileSize.width;
            int drawY = y * tileSize.height;

            // g.drawLine(drawX-10,drawY-10,drawX+10,drawY+10);
            for (int i = 0; i < 9; i++) {
                if ((trackTemplate & (1 << i)) == (1 << i)) {
                    g2.drawLine(drawX + 15, drawY + 15,
                            drawX + 15 + 15 * xx[i], drawY + 15 + 15 * yy[i]);
                }
            }
        }
    }

    public void dumpImages(ImageManager imageManager) {
        // TODO Auto-generated method stub
    }
}
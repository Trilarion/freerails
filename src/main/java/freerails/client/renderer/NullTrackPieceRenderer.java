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

import java.awt.*;

/**
 * Implements the TrackPieceView interface, but intentionally does
 * nothing. Its methods are called when drawing tiles with no track.
 */
public final class NullTrackPieceRenderer implements TrackPieceRenderer {

    /**
     *
     */
    public static final NullTrackPieceRenderer instance = new NullTrackPieceRenderer();

    private NullTrackPieceRenderer() {
    }

    /*
     * @see TrackPieceView#getTrackPieceIcon(int)
     */

    /**
     * @param trackTemplate
     * @return
     */

    public Image getTrackPieceIcon(int trackTemplate) {
        return null;
    }

    /*
     * @see TrackPieceView#drawTrackPieceIcon(int, Graphics, int, int,
     *      Dimension)
     */

    /**
     * @param trackTemplate
     * @param g
     * @param x
     * @param y
     * @param tileSize
     */

    public void drawTrackPieceIcon(int trackTemplate, Graphics g, int x, int y,
                                   Dimension tileSize) {
        // Draw nothing since there no track here.
    }

}
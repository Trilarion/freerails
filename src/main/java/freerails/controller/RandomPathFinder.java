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

package freerails.controller;

import freerails.client.ClientConstants;
import freerails.util.IntLine;
import freerails.world.track.PathIterator;
import freerails.world.train.PositionOnTrack;

/**
 * Returns a random path along the track.
 */
@SuppressWarnings("unused")
public class RandomPathFinder implements PathIterator {
    private static final long serialVersionUID = 3832906571880608313L;
    private static final int tileSize = ClientConstants.TILE_SIZE;
    private final FlatTrackExplorer trackExplorer;
    private final PositionOnTrack p1 = new PositionOnTrack();
    private final PositionOnTrack p2 = new PositionOnTrack();

    /**
     * @param tx
     */
    public RandomPathFinder(FlatTrackExplorer tx) {
        trackExplorer = tx;
    }

    public boolean hasNext() {
        return trackExplorer.hasNextEdge();
    }

    public void nextSegment(IntLine line) {
        p1.setValuesFromInt(trackExplorer.getPosition());
        line.x1 = p1.getX() * tileSize + tileSize / 2;
        line.y1 = p1.getY() * tileSize + tileSize / 2;
        trackExplorer.nextEdge();
        trackExplorer.moveForward();
        p2.setValuesFromInt(trackExplorer.getPosition());
        line.x2 = p2.getX() * tileSize + tileSize / 2;
        line.y2 = p2.getY() * tileSize + tileSize / 2;
    }
}
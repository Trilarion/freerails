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

package freerails.world.train;

import freerails.client.ClientConstants;
import freerails.util.IntLine;
import freerails.world.track.PathIterator;

import java.util.Iterator;

/**
 * Exposes the path of a train. TODO needs better comment
 */
@SuppressWarnings("unused")
public class TrainPathIterator implements PathIterator {

    private static final long serialVersionUID = 3256999977816502584L;
    private static final int tileSize = ClientConstants.TILE_SIZE;
    private final Iterator<Integer> intIterator;
    private final PositionOnTrack p1 = new PositionOnTrack();
    private final PositionOnTrack p2 = new PositionOnTrack();

    /**
     * @param i
     */
    public TrainPathIterator(Iterator<Integer> i) {
        intIterator = i;
        p2.setValuesFromInt(intIterator.next());
    }

    public boolean hasNext() {
        return intIterator.hasNext();
    }

    public void nextSegment(IntLine line) {
        p1.setValuesFromInt(p2.toInt());
        line.x1 = p1.getX() * tileSize + tileSize / 2;
        line.y1 = p1.getY() * tileSize + tileSize / 2;
        p2.setValuesFromInt(intIterator.next());
        line.x2 = p2.getX() * tileSize + tileSize / 2;
        line.y2 = p2.getY() * tileSize + tileSize / 2;
    }
}
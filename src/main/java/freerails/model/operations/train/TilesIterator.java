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
package freerails.model.operations.train;

import freerails.util.IntPoint2D;
import freerails.model.terrain.TileTransition;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Iterator over the tile coordinates visited by a path. */
final class TilesIterator implements Iterator<IntPoint2D> {
    private final List<TileTransition> vectors;
    private int index;
    private IntPoint2D next;

    /**
     * Creates an iterator over the start tile and each tile reached by a transition.
     *
     * @param start starting tile coordinates
     * @param vectors path transitions, in traversal order
     */
    TilesIterator(IntPoint2D start, List<TileTransition> vectors) {
        this.next = start;
        this.vectors = vectors;
    }

    /** Returns whether another visited tile remains. */
    @Override
    public boolean hasNext() {
        return next != null;
    }

    /**
     * Returns the next visited tile.
     *
     * @return next tile coordinate
     * @throws NoSuchElementException if all tiles have been returned
     */
    @Override
    public IntPoint2D next() {
        if (next == null) throw new NoSuchElementException();

        IntPoint2D returnValue = next;
        int x = next.x();
        int y = next.y();
        if (index < vectors.size()) {
            TileTransition transition = vectors.get(index);
            x += transition.deltaX;
            y += transition.deltaY;
            next = new IntPoint2D(x, y);
        } else {
            next = null;
        }
        index++;

        return returnValue;
    }

    /** Removal is unsupported because this iterator does not modify the path. */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

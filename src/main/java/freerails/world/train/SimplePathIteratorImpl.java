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

import freerails.util.ImInts;
import freerails.util.IntLine;
import freerails.world.common.FreerailsPathIterator;

import java.util.NoSuchElementException;

/**
 * Exposes a path stored as an array of x points and an array of y points.
 */
public class SimplePathIteratorImpl implements FreerailsPathIterator {
    private static final long serialVersionUID = 3618420406261003576L;

    private final ImInts x;

    private final ImInts y;

    private int position = 0;

    /**
     * @param xpoints
     * @param ypoints
     */
    public SimplePathIteratorImpl(ImInts xpoints, ImInts ypoints) {
        x = xpoints;
        y = ypoints;

        if (x.size() != y.size()) {
            throw new IllegalArgumentException(
                    "The array length of the array must be even");
        }
    }

    /**
     * @param xpoints
     * @param ypoints
     */
    public SimplePathIteratorImpl( /* =const */
            int[] xpoints, /* =const */
            int[] ypoints) {
        x = new ImInts(xpoints);
        y = new ImInts(ypoints); // defensive copy.

        if (x.size() != y.size()) {
            throw new IllegalArgumentException(
                    "The array length of the array must be even");
        }
    }

    public void nextSegment(IntLine line) {
        if (hasNext()) {
            line.x1 = x.get(position);
            line.y1 = y.get(position);
            line.x2 = x.get(position + 1);
            line.y2 = y.get(position + 1);
            position++;
        } else {
            throw new NoSuchElementException();
        }
    }

    public boolean hasNext() {
        return (position + 1) < x.size();
    }
}
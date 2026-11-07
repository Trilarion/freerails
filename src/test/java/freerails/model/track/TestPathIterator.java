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

/*
 *
 */
package freerails.model.track;

import freerails.util.Segment;
import freerails.util.IntPoint2D;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Lets the caller access a series of Points as a series of IntLines.
 */
public class TestPathIterator implements PathIterator {

    private static final long serialVersionUID = 3258411750679720758L;
    private final boolean forwards;
    private final List<IntPoint2D> points;
    private int position;

    /**
     * Creates an instance of test path iterator.
     * @param l the l to use in the test.
     * @param f the f to use in the test.
     */
    public TestPathIterator(List<IntPoint2D> l, boolean f) {
        points = l;
        forwards = f;

        if (forwards) {
            position = 0;
        } else {
            position = l.size() - 1; // The last element of a list of
            // size 7 is at position 6.
        }
    }

    /**
     * Performs forwards iterator.
     * @param l the l to use in the test.
     * @return the result of forwards iterator.
     */
    public static PathIterator forwardsIterator(List<IntPoint2D> l) {
        return new TestPathIterator(l, true);
    }

    /**
     * Indicates whether next is present.
     * @return whether next is present.
     */
    @Override
    public boolean hasNext() {
        if (forwards) {
            return position + 1 < points.size();
        }
        return position - 1 >= 0;
    }

    /**
     * Advances to the next segment.
     * @return the next segment.
     */
    @Override
    public Segment nextSegment() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        IntPoint2D a;
        IntPoint2D b;

        if (forwards) {
            position++;
            a = points.get(position - 1);
            b = points.get(position);
        } else {
            position--;
            a = points.get(position + 1);
            b = points.get(position);
        }

        return new Segment(a, b);
    }
}
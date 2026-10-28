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

package freerails.model.track;


import freerails.util.Segment;
import freerails.util.Vec2D;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterates over the line segments between consecutive points in a path.
 */
public class SimplePathIteratorImpl implements PathIterator {

    private static final long serialVersionUID = 3618420406261003576L;
    private final List<Vec2D> points;
    private int position = 0;

    /**
     * @param points ordered path points; each consecutive pair defines a segment
     */
    public SimplePathIteratorImpl(List<Vec2D> points) {
        this.points = points;
    }

    /**
     * Returns the segment between the current point and its successor, then
     * advances by one point.
     *
     * @return the next path segment
     * @throws NoSuchElementException if fewer than two unconsumed points remain
     */
    @Override
    public Segment nextSegment() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        Segment line = new Segment(points.get(position), points.get(position+1));
        position++;
        return line;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return position + 1 < points.size();
    }
}
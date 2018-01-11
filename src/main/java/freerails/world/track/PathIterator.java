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

package freerails.world.track;

import freerails.util.LineSegment;
import freerails.world.FreerailsMutableSerializable;

/**
 * This interface lets the caller retrieve a path made up of a series of
 * straight lines. E.g. it lets the path a train takes across a section of track
 * be retrieved without revealing the underlying objects that represent the
 * track.
 */
// TODO what is it good for?
public interface PathIterator extends FreerailsMutableSerializable {

    /**
     * Tests whether the path has another segment.
     */
    boolean hasNext();

    /**
     * Gets the next segment of the path and places its coordinates in the
     * specified LineSegment; then moves the iterator forwards by one path segment.
     * (The coordinates are placed the passed-in LineSegment rather than a new
     * object to avoid the cost of object creation.)
     */
    void nextSegment(LineSegment line);

}
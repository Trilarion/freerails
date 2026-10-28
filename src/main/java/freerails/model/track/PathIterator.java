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

// TODO what is it good for?
/**
 * This interface lets the caller retrieve a path made up of a series of
 * straight lines. E.g. it lets the path a train takes across a section of track
 * be retrieved without revealing the underlying objects that represent the
 * track.
 */
public interface PathIterator {

    /**
     * Tests whether another segment is available.
     *
     * @return {@code true} if a subsequent call to {@link #nextSegment()} can
     *         return a segment
     */
    boolean hasNext();

    /**
     * Returns the next segment and advances the iterator.
     *
     * @return the next segment in the path
     * @throws java.util.NoSuchElementException if no segment remains
     */
    Segment nextSegment();

}
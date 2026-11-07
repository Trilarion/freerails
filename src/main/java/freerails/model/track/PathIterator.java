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

/**
 * Exposes a path as a sequence of straight line segments without revealing the
 * underlying track objects that define it.
 *
 * <p>This is useful for algorithms that need to traverse a route step by step,
 * such as advancing a train along the track, measuring distance along the path,
 * or rendering the path as a series of connected segments. Each call to
 * {@link #nextSegment()} returns the next segment between two consecutive points
 * in the path, while {@link #hasNext()} reports whether more segments remain.
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
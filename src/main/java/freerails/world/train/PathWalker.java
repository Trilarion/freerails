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

import freerails.world.FreerailsMutableSerializable;
import freerails.world.track.PathIterator;

/**
 * This interface lets the caller retrieve a path broken into a series of steps,
 * whose length the caller specifies. E.g. it could be used to get the sub
 * section of a path that a train travels during an given time interval.
 */
public interface PathWalker extends PathIterator,
        FreerailsMutableSerializable {
    /**
     * Returns true if we have not reached the end of the path.
     *
     * @return
     */
    boolean canStepForward();

    /**
     * Moves this path walker forward by the specified distance along the path
     * and returns a path iterator to retrieve the section of the path travelled
     * during this move.
     *
     * @param distance
     */
    void stepForward(double distance);
}
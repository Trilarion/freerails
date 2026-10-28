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
package freerails.model.track.pathfinding;

/**
 * Contract for a pathfinder whose search can be run in time-limited steps.
 */
public interface IncrementalPathFinder {

    /**
     * Returns the current state of the most recently configured search.
     *
     * @return current pathfinding status
     */
    PathFinderStatus getStatus();

    /**
     * Continues the search for at most the requested duration when the limit is
     * positive; a non-positive value allows the search to run to completion.
     *
     * @param maxDuration maximum duration of this search call in milliseconds,
     *                    or a non-positive value for no time limit
     * @throws PathNotFoundException if the search has no path; implementations
     *                               may also use it to report that a limited
     *                               search call has paused
     */
    void search(long maxDuration) throws PathNotFoundException;

    /**
     * Clears the current search state and discards any partial or completed path.
     */
    void abandonSearch();
}
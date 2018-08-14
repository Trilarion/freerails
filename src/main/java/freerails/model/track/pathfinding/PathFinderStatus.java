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

package freerails.model.track.pathfinding;

/**
 *
 */
public enum PathFinderStatus {

    PATH_NOT_FOUND(Integer.MIN_VALUE), PATH_FOUND(Integer.MIN_VALUE + 1), SEARCH_PAUSED(Integer.MIN_VALUE + 2), SEARCH_NOT_STARTED(Integer.MIN_VALUE + 3);

    public final int id;

    PathFinderStatus(int id) {
        this.id = id;
    }


}

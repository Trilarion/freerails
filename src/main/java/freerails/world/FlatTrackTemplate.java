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

package freerails.world;

import java.io.Serializable;

/**
 * Defines methods that encode a track configuration as an int.
 */
public interface FlatTrackTemplate extends Serializable {
    /**
     * @param ftt the FlatTrackTemplate which may be a subset of this
     *            FlatTrackTemplate.
     * @return true if the vectors represented by this FlatTrackTemplate are a
     * superset of the vectors of the specified FlatTrackTemplate
     */
    @SuppressWarnings("unused")
    boolean contains(FlatTrackTemplate ftt);

    /**
     * @return the integer representing the vector(s) of this object.
     */
    int get9bitTemplate();
}
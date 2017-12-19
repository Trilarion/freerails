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

package freerails.world.player;

import freerails.world.FreerailsSerializable;

import java.security.Principal;

/**
 * This interface identifies a principal. This interface may be extended in the
 * future in order to provide faster lookups, rather than using name
 * comparisons.
 *
 * A principal represents an entity which can view or alter the game world. A
 * principal usually corresponds to a player's identity, but may also represent
 * an authorititative server, or a another game entity such as a corporation.
 * All entities which may own game world objects must be represented by a
 * principal.
 */
public abstract class FreerailsPrincipal implements Principal, FreerailsSerializable {
    private final int worldIndex;

    /**
     *
     * @param worldIndex
     */
    public FreerailsPrincipal(int worldIndex) {
        this.worldIndex = worldIndex;
    }

    /**
     * returns -1 if it's not a player
     *
     * @return the index in the world structures
     */
    public int getWorldIndex() {
        return worldIndex;
    }

}
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

package freerails.model.player;

import freerails.util.Utils;

import java.io.Serializable;
import java.security.Principal;

/**
 * This interface identifies a principal. This interface may be extended in the
 * future in order to provide faster lookups, rather than using name
 * comparisons.
 *
 * A principal represents an entity which can view or alter the game world. A
 * principal usually corresponds to a player's identity, but may also represent
 * an authoritative server, or a another game entity such as a corporation.
 * All entities which may own game world objects must be represented by a
 * principal.
 */
public class FreerailsPrincipal implements Serializable {

    private static final long serialVersionUID = 4673561105333981501L;
    // TODO what is the meaning of the world index/id? (position in player list and the other lists)
    private final int id;
    private final String name;

    /**
     * @param id
     */
    FreerailsPrincipal(int id, String name) {
        this.id = id;
        this.name = Utils.verifyNotNull(name);
    }

    /**
     * returns -1 if it's not a player
     *
     * @return the index in the world structures
     */
    public int getWorldIndex() {
        return id;
    }


    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Principal " + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FreerailsPrincipal)) return false;

        final FreerailsPrincipal other = (FreerailsPrincipal) obj;

        if (!(id == other.id)) return false;
        return name.equals(other.name);
    }

}
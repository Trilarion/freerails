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

package freerails.model.player;

import freerails.util.Identifiable;
import freerails.util.Utils;

import java.io.Serial;

// TODO on the client side, one should not create a player but a add new player move or so...
/**
 * Identifies a player.
 * <p>
 * A player represents an entity which can view or alter the game world. A
 * player usually corresponds to a user's identity, but may also represent
 * an authoritative server, or a another game entity such as a corporation.
 * All entities which may own game world objects must be represented by a
 * player.
 */
public class Player extends Identifiable {

    @Serial
    private static final long serialVersionUID = 4673561105333981501L;
    public static final Player AUTHORITATIVE = new Player(-1, "Authoritative player");
    private final String name;

    /**
     * @param id stable player identifier
     * @param name display name
     */
    public Player(int id, String name) {
        super(id);
        this.name = Utils.verifyNotNull(name);
    }

    /** Returns the player's display name. */
    public String getName() {
        return name;
    }

    /** Returns a label containing this player's name and identifier. */
    @Override
    public String toString() {
        return "Player: " + name + " " + getId();
    }
}
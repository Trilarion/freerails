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

/**
 * FreerailsPrincipal that is a player in the game.
 */
public class PlayerPrincipal extends FreerailsPrincipal {

    private static final long serialVersionUID = 3257563997099537459L;
    private final int id;
    private final String name;

    /**
     * @param id
     * @param name
     */
    public PlayerPrincipal(int id, String name) {
        super(id);
        this.id = id;
        this.name = name;
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
        return "Player " + id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PlayerPrincipal && id == ((PlayerPrincipal) o).id;

    }
}
/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.world.player;

public class PlayerPrincipal extends FreerailsPrincipal {
    private final int id;

    public PlayerPrincipal(int id) {
        this.id = id;
    }

    public String getName() {
        return "Player " + id;
    }

    public int hashCode() {
        return id;
    }

    public String toString() {
        return "Player " + id;
    }

    /**
     * @return an integer unique to this PlayerPrincipal
     */
    public int getId() {
        return id;
    }

    public boolean equals(Object o) {
        if (!(o instanceof PlayerPrincipal)) {
            return false;
        }

        return id == ((PlayerPrincipal)o).id;
    }
}

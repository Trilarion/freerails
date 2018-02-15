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

/*
 *
 */
package freerails.move.listmove;

import freerails.move.MoveStatus;
import freerails.model.world.PlayerKey;
import freerails.model.world.World;
import freerails.model.player.FreerailsPrincipal;

import java.io.Serializable;

/**
 * All moves that add an item to a list should extend this class.
 */
public class AddItemToListMove implements ListMove {

    private static final long serialVersionUID = 3256721779916747824L;
    private final PlayerKey listPlayerKey;
    private final int index;
    private final FreerailsPrincipal principal;
    private final Serializable item;

    /**
     * @param playerKey
     * @param i
     * @param item
     * @param p
     */
    public AddItemToListMove(PlayerKey playerKey, int i, Serializable item, FreerailsPrincipal p) {
        listPlayerKey = playerKey;
        index = i;
        this.item = item;
        principal = p;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        int result;
        result = listPlayerKey.hashCode();
        result = 29 * result + index;
        result = 29 * result + principal.hashCode();
        result = 29 * result + (item != null ? item.hashCode() : 0);

        return result;
    }

    public PlayerKey getKey() {
        return listPlayerKey;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        if (world.size(this.principal, listPlayerKey) != index) {
            return MoveStatus.moveFailed("Expected size of " + listPlayerKey.toString() + " list is " + index + " but actual size is " + world.size(this.principal, listPlayerKey));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        int expectListSize = index + 1;

        if (world.size(this.principal, listPlayerKey) != expectListSize) {
            return MoveStatus.moveFailed("Expected size of " + listPlayerKey.toString() + " list is " + expectListSize + " but actual size is " + world.size(this.principal, listPlayerKey));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryDoMove(world, principal);

        if (moveStatus.succeeds()) {
            world.add(this.principal, listPlayerKey, item);
        }

        return moveStatus;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus moveStatus = tryUndoMove(world, principal);

        if (moveStatus.succeeds()) {
            world.removeLast(this.principal, listPlayerKey);
        }

        return moveStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddItemToListMove) {
            AddItemToListMove test = (AddItemToListMove) obj;

            if (null == item) {
                if (null != test.item) {
                    return false;
                }
            } else if (!item.equals(test.item)) {
                return false;
            }

            if (index != test.index) {
                return false;
            }

            return listPlayerKey == test.listPlayerKey;
        }
        return false;
    }

    public Serializable getAfter() {
        return item;
    }

    @Override
    public String toString() {

        return getClass().getName() + "\n list=" + listPlayerKey.toString() + "\n index =" + index + "\n item =" + item;
    }

    /**
     * @return
     */
    public FreerailsPrincipal getPrincipal() {
        return principal;
    }
}
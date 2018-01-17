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
package freerails.move;

import freerails.world.KEY;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;

import java.io.Serializable;

/**
 * All moves that remove an item from a list should extend this class.
 */
public class RemoveItemFromListMove implements ListMove {

    private static final long serialVersionUID = 3906091169698953521L;
    private final Serializable item;
    private final KEY listKey;
    private final int index;
    private final FreerailsPrincipal principal;

    RemoveItemFromListMove(KEY k, int i, Serializable item, FreerailsPrincipal p) {
        this.item = item;
        listKey = k;
        index = i;
        principal = p;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        int result;
        result = (item != null ? item.hashCode() : 0);
        result = 29 * result + listKey.hashCode();
        result = 29 * result + index;
        result = 29 * result + principal.hashCode();

        return result;
    }

    public KEY getKey() {
        return listKey;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        if (world.size(this.principal, listKey) < (index + 1)) {
            return MoveStatus.moveFailed("world.size(listKey)=" + world.size(this.principal, listKey) + " but index =" + index);
        }

        Serializable item2remove = world.get(this.principal, listKey, index);

        if (null == item2remove) {
            return MoveStatus.moveFailed("The item at position " + index + " has already been removed.");
        }

        if (!item.equals(item2remove)) {
            String reason = "The item at position " + index + " in the list (" + item2remove.toString() + ") is not the expected item (" + item.toString() + ").";

            return MoveStatus.moveFailed(reason);
        }
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        if (world.size(this.principal, listKey) < (index + 1)) {
            return MoveStatus.moveFailed("world.size(listKey)=" + world.size(this.principal, listKey) + " but index =" + index);
        }

        if (null != world.get(this.principal, listKey, index)) {
            String reason = "The item at position " + index + " in the list (" + world.get(this.principal, listKey, index).toString() + ") is not the expected item (null).";

            return MoveStatus.moveFailed(reason);
        }
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus ms = tryDoMove(world, principal);

        if (ms.isStatus()) {
            world.set(this.principal, listKey, index, null);
        }

        return ms;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus ms = tryUndoMove(world, principal);

        if (ms.isStatus()) {
            world.set(this.principal, listKey, index, item);
        }

        return ms;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RemoveItemFromListMove) {
            RemoveItemFromListMove test = (RemoveItemFromListMove) obj;

            if (!item.equals(test.item)) {
                return false;
            }

            if (index != test.index) {
                return false;
            }

            return listKey == test.listKey;
        }
        return false;
    }

    /**
     * @return
     */
    public FreerailsPrincipal getPrincipal() {
        return principal;
    }
}
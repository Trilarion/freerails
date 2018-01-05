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

import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;

import java.io.Serializable;

/**
 * All moves that add an item to a shared list should extend this class.
 */
@SuppressWarnings("unused")
public class AddItemToSharedListMove implements Move {
    private static final long serialVersionUID = 3762256352759722807L;
    private final SKEY listKey;
    private final int index;
    private final Serializable item;

    /**
     * @param key
     * @param i
     * @param item
     */
    protected AddItemToSharedListMove(SKEY key, int i,
                                      Serializable item) {
        this.listKey = key;
        this.index = i;
        this.item = item;
    }

    /**
     * @return
     */
    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        int result;
        result = listKey.hashCode();
        result = 29 * result + index;
        result = 29 * result + (item != null ? item.hashCode() : 0);

        return result;
    }

    /**
     * @return
     */
    public SKEY getKey() {
        return listKey;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        if (world.size(listKey) != index) {
            return MoveStatus.moveFailed("Expected size of "
                    + listKey.toString() + " list is " + index
                    + " but actual size is " + world.size(listKey));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        int expectListSize = index + 1;

        if (world.size(listKey) != expectListSize) {
            return MoveStatus.moveFailed("Expected size of "
                    + listKey.toString() + " list is " + expectListSize
                    + " but actual size is " + world.size(listKey));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus ms = tryDoMove(world, principal);

        if (ms.isOk()) {
            world.add(listKey, this.item);
        }

        return ms;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus ms = tryUndoMove(world, principal);

        if (ms.isOk()) {
            world.removeLast(listKey);
        }

        return ms;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AddItemToSharedListMove) {
            AddItemToSharedListMove test = (AddItemToSharedListMove) o;

            if (!this.item.equals(test.item)) {
                return false;
            }

            if (this.index != test.index) {
                return false;
            }

            return this.listKey == test.listKey;
        }
        return false;
    }

    /**
     * @return
     */
    public Serializable getBefore() {
        return null;
    }

    /**
     * @return
     */
    public Serializable getAfter() {
        return item;
    }

    @Override
    public String toString() {

        return this.getClass().getName() + "\nlist=" +
                listKey.toString() +
                "\n index =" +
                this.index +
                "\n item =" +
                this.item.toString();
    }
}
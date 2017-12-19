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
 * Created on 13-Apr-2003
 *
 */
package freerails.move;

import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.SKEY;
import freerails.world.top.World;

import java.io.Serializable;

/**
 * All moves that add an item to a shared list should extend this class.
 */
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

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.size(listKey) != index) {
            return MoveStatus.moveFailed("Expected size of "
                    + listKey.toString() + " list is " + index
                    + " but actual size is " + w.size(listKey));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        int expectListSize = index + 1;

        if (w.size(listKey) != expectListSize) {
            return MoveStatus.moveFailed("Expected size of "
                    + listKey.toString() + " list is " + expectListSize
                    + " but actual size is " + w.size(listKey));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.isOk()) {
            w.add(listKey, this.item);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.isOk()) {
            w.removeLast(listKey);
        }

        return ms;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AddItemToSharedListMove) {
            AddItemToSharedListMove test = (AddItemToSharedListMove) o;

            if (!this.item.equals(test.getAfter())) {
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
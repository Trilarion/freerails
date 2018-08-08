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
package freerails.model.world;

import freerails.util.Utils;
import freerails.model.player.Player;

import java.io.Serializable;
import java.util.NoSuchElementException;

// TODO why is it important to only return non-null elements
// TODO can we remove it please, we should not have null elements there anyway
/**
 * Iterates over one of the lists on the world object only returning non null
 * elements.
 */
public class NonNullElementWorldIterator implements WorldIterator {

    private final PlayerKey playerKey;
    private final UnmodifiableWorld world;
    private final Player player;
    private int index = BEFORE_FIRST;
    private int row = BEFORE_FIRST;
    private int size = -1;

    /**
     * @param k
     * @param world
     * @param player
     */
    public NonNullElementWorldIterator(PlayerKey k, UnmodifiableWorld world, Player player) {
        playerKey = Utils.verifyNotNull(k);
        this.world = Utils.verifyNotNull(world);
        this.player = Utils.verifyNotNull(player);
    }

    /**
     * @param world
     * @param playerKey
     * @param player
     * @param row
     * @return
     */
    public static int rowToIndex(UnmodifiableWorld world, PlayerKey playerKey, Player player, int row) {
        int count = 0;
        for (int i = 0; i < world.size(player, playerKey); i++) {

            if (world.get(player, playerKey, i) != null) {
                if (count == row) {
                    return i;
                }
                count++;
            }
        }

        throw new NoSuchElementException(String.valueOf(row));
    }

    public boolean next() {
        int nextIndex = index; // this is used to look ahead.

        do {
            nextIndex++;

            if (nextIndex >= listSize()) {
                return false;
            }
        } while (!testCondition(nextIndex));

        row++;
        index = nextIndex;

        return true;
    }

    public void reset() {
        index = -1;
        row = -1;
        size = -1;
    }

    public Serializable getElement() {
        return listGet(index);
    }

    private Serializable listGet(int i) {
        return world.get(player, playerKey, i);
    }

    private int listSize() {
        return world.size(player, playerKey);
    }

    public int getIndex() {
        return index;
    }

    public int getRowID() {
        return row;
    }

    public int size() {
        if (-1 == size) { // lazy loading, if we have already calculated the
            // size don't do it again.

            int tempSize = 0;

            for (int i = 0; i < listSize(); i++) {
                if (null != listGet(i)) {
                    tempSize++;
                }
            }

            size = tempSize;
        }

        return size;
    }

    public boolean previous() {
        int previousIndex = index; // this is used to look back.

        do {
            previousIndex--;

            if (previousIndex < 0) {
                return false;
            }
        } while (!testCondition(previousIndex));

        row--;
        index = previousIndex;

        return true;
    }

    /**
     * Moves the cursor to the specified index.
     */
    public void gotoIndex(int i) {
        int newRow = -1;

        for (int j = 0; j < listSize(); j++) {
            if (testCondition(j)) {
                newRow++;

                if (i == j) {
                    reset();
                    index = i;
                    row = newRow;

                    return;
                }
            }
        }

        throw new NoSuchElementException("Index:" + String.valueOf(i) + " Size:" + listSize() + " Row:" + newRow);
    }

    /**
     * @param i
     * @return
     */
    private boolean testCondition(int i) {
        return null != listGet(i);
    }

    public int getNaturalNumber() {
        return row + 1;
    }

    public void gotoRow(int row) {
        if (this.row == row) {
            return;
        }
        if (this.row < row) {
            while (this.row != row) {
                next();
            }
        } else {
            while (this.row != row) {
                previous();
            }
        }
    }
}
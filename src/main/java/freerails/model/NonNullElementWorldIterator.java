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
package freerails.model;

import freerails.model.world.WorldSharedKey;
import freerails.model.world.WorldKey;
import freerails.util.Utils;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.world.ReadOnlyWorld;

import java.io.Serializable;
import java.util.NoSuchElementException;

// TODO why is it important to only return non-null elements

/**
 * Iterates over one of the lists on the world object only returning non null
 * elements.
 */
public class NonNullElementWorldIterator implements WorldIterator {

    private final WorldKey worldKey;
    private final WorldSharedKey worldSharedKey;
    private final ReadOnlyWorld world;
    private final FreerailsPrincipal principal;
    private int index = BEFORE_FIRST;
    private int row = BEFORE_FIRST;
    private int size = -1;

    /**
     * @param k
     * @param world
     */
    public NonNullElementWorldIterator(WorldSharedKey k, ReadOnlyWorld world) {
        worldKey = null;
        principal = null;
        worldSharedKey = Utils.verifyNotNull(k);
        this.world = Utils.verifyNotNull(world);
    }

    /**
     * @param k
     * @param world
     * @param p
     */
    public NonNullElementWorldIterator(WorldKey k, ReadOnlyWorld world, FreerailsPrincipal p) {
        worldKey = Utils.verifyNotNull(k);
        this.world = Utils.verifyNotNull(world);
        principal = Utils.verifyNotNull(p);
        worldSharedKey = null;
    }

    /**
     * @param world
     * @param worldKey
     * @param principal
     * @param row
     * @return
     */
    public static int rowToIndex(ReadOnlyWorld world, WorldKey worldKey, FreerailsPrincipal principal, int row) {
        int count = 0;
        for (int i = 0; i < world.size(principal, worldKey); i++) {

            if (world.get(principal, worldKey, i) != null) {
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
        if (null == worldSharedKey) {
            return world.get(principal, worldKey, i);
        }
        return world.get(worldSharedKey, i);
    }

    private int listSize() {
        if (null == worldSharedKey) {
            return world.size(principal, worldKey);
        }
        return world.size(worldSharedKey);
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
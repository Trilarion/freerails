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
package freerails.world;

import freerails.world.player.FreerailsPrincipal;

import java.io.Serializable;
import java.util.NoSuchElementException;

// TODO why is it important to only return non-null elements
/**
 * Iterates over one of the lists on the world object only returning non null
 * elements.
 */
public class NonNullElementWorldIterator implements WorldIterator {

    private final KEY key;
    private final SKEY skey;
    private final ReadOnlyWorld w;
    private final FreerailsPrincipal principal;
    private int index = BEFORE_FIRST;
    private int row = BEFORE_FIRST;
    private int size = -1;

    /**
     * @param k
     * @param world
     */
    public NonNullElementWorldIterator(SKEY k, ReadOnlyWorld world) {
        if (null == k) {
            throw new NullPointerException();
        }

        if (null == world) {
            throw new NullPointerException();
        }

        key = null;
        principal = null;
        skey = k;
        w = world;
    }

    /**
     * @param k
     * @param world
     * @param p
     */
    public NonNullElementWorldIterator(KEY k, ReadOnlyWorld world, FreerailsPrincipal p) {
        key = k;
        w = world;
        principal = p;
        skey = null;

        if (null == k) {
            throw new NullPointerException();
        }

        if (null == world) {
            throw new NullPointerException();
        }

        if (null == p) {
            throw new NullPointerException();
        }
    }

    /**
     * @param w
     * @param key
     * @param p
     * @param row
     * @return
     */
    public static int row2index(ReadOnlyWorld w, KEY key, FreerailsPrincipal p,
                                int row) {
        int count = 0;
        for (int i = 0; i < w.size(p, key); i++) {

            if (w.get(p, key, i) != null) {
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
        if (null == skey) {
            return w.get(principal, key, i);
        }
        return w.get(skey, i);
    }

    private int listSize() {
        if (null == skey) {
            return w.size(principal, key);
        }
        return w.size(skey);
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
     *
     * @param i
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

        throw new NoSuchElementException("Index:" + String.valueOf(i)
                + " Size:" + listSize() + " Row:" + newRow);
    }

    /**
     * @param i
     * @return
     */
    protected boolean testCondition(int i) {
        return null != listGet(i);
    }

    public int getNaturalNumber() {
        return row + 1;
    }

    public void gotoRow(int newRow) {
        if (row == newRow) {
            return;
        }
        if (row < newRow) {
            while (row != newRow) {
                next();
            }
        } else {
            while (row != newRow) {
                previous();
            }

        }
    }
}
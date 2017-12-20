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

import freerails.util.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An implementation of World that only stores differences relative to an
 * underlying world object. Below is some stylised code showing what this class
 * does. The {@code key} object could be a location on the map, a
 * position in a list etc. <code><pre>
 * HashMap underlyingWorldObject;
 *
 * HashMap differences;
 *
 * public void put(Object key, Object value) {
 *     if (underlyingWorldObject.get(key).equals(value)) {
 *         if (differences.containsKey(key)) {
 *             differences.remove(key);
 *         }
 *     } else {
 *         differences.put(key, value);
 *     }
 * }
 *
 * public Object get(Object key) {
 *     if (differences.containsKey(key)) {
 *         return differences.get(key);
 *     } else {
 *         return underlyingWorldObject.get(key);
 *     }
 * }
 * </code></pre>
 *
 * The advantages of using an instance of this class instead of a copy of the
 * world object are:
 * <ol>
 * <li> Uses less memory.</li>
 * <li> Lets you pinpoint where differences on the map are, so you don't need to
 * check every tile. </li>
 * </ol>
 */
public class WorldDiffs extends WorldImpl {

    private static final long serialVersionUID = -5993786533926919956L;
    private final SortedMap<ListKey, Object> listDiff;
    /**
     * Stores the differences on the map, ImPoint are used as keys.
     */
    private final HashMap<ImPoint, Object> mapDiff;
    private final WorldImpl underlying;

    /**
     * @param row
     */
    public WorldDiffs(ReadOnlyWorld row) {

        listDiff = new TreeMap<>();
        mapDiff = new HashMap<>();

        // Bit of a hack but it's not clear there is a better way, LL
        underlying = (WorldImpl) row;

        activityLists = new List3DDiff<>(listDiff,
                underlying.activityLists, LISTID.ACTIVITY_LISTS);
        bankAccounts = new List2DDiff<>(listDiff,
                underlying.bankAccounts, LISTID.BANK_ACCOUNTS);
        currentBalance = new List1DDiff<>(listDiff,
                underlying.currentBalance, LISTID.CURRENT_BALANCE);
        items = new List1DDiff<>(listDiff,
                underlying.items, LISTID.ITEMS);
        lists = new List3DDiff<>(listDiff,
                underlying.lists, LISTID.LISTS);
        players = new List1DDiff<>(listDiff, underlying.players,
                LISTID.PLAYERS);
        sharedLists = new List2DDiff<>(listDiff,
                underlying.sharedLists, LISTID.SHARED_LISTS);
        time = underlying.time;
    }

    /**
     * The iterator returns instances of java.awt.Point that store the
     * coordinates of tiles that are different to the underlying world object.
     *
     * @return
     */
    public Iterator<ImPoint> getMapDiffs() {
        return mapDiff.keySet().iterator();
    }

    /**
     * @return
     */
    public Iterator<ListKey> getListDiffs() {
        return listDiff.keySet().iterator();
    }

    /**
     * @param key
     * @return
     */
    public Object getDiff(ListKey key) {
        return listDiff.get(key);
    }

    @Override
    public int getMapHeight() {
        return underlying.getMapHeight();
    }

    @Override
    public int getMapWidth() {
        return underlying.getMapWidth();
    }

    @Override
    public Serializable getTile(int x, int y) {
        ImPoint p = new ImPoint(x, y);

        if (this.mapDiff.containsKey(p)) {
            return (Serializable) this.mapDiff.get(p);
        }
        return underlying.getTile(x, y);
    }

    /**
     * Used by unit tests.
     *
     * @return
     */
    public int numberOfMapDifferences() {
        return this.mapDiff.size();
    }

    /**
     * Used by unit tests.
     *
     * @return
     */
    public int listDiffs() {
        return listDiff.size();
    }

    /**
     * After this method returns, all differences are cleared and calls to
     * methods on this object should produce the same results as calls the the
     * corresponding methods on the underlying world object.
     */
    public void reset() {
        time = underlying.currentTime();
        mapDiff.clear();
        listDiff.clear();
    }

    @Override
    public void setTile(int x, int y, Serializable tile) {
        ImPoint p = new ImPoint(x, y);

        if (Utils.equal(underlying.getTile(x, y), tile)) {
            if (this.mapDiff.containsKey(p)) {
                this.mapDiff.remove(p);

            }
        } else {
            this.mapDiff.put(p, tile);
        }
    }

    /**
     * @return
     */
    public boolean isDifferent() {
        return (mapDiff.size() != 0) || (listDiff.size() != 0);
    }

    /**
     * @return
     */
    public ReadOnlyWorld getUnderlying() {
        return underlying;
    }

    /**
     *
     */
    public enum LISTID {

        /**
         *
         */
        ACTIVITY_LISTS,

        /**
         *
         */
        BANK_ACCOUNTS,

        /**
         *
         */
        CURRENT_BALANCE,

        /**
         *
         */
        ITEMS,

        /**
         *
         */
        LISTS,

        /**
         *
         */
        PLAYERS,

        /**
         *
         */
        SHARED_LISTS
    }

}

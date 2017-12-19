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

package freerails.world;

import freerails.util.*;
import freerails.world.finances.EconomicClimate;
import freerails.world.finances.Money;
import freerails.world.finances.Transaction;
import freerails.world.finances.TransactionWithTimeStamp;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.terrain.FreerailsTile;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An implementation of World that uses standard java.util collections
 * internally.
 */
public class WorldImpl implements World {

    private static final long serialVersionUID = 3544393612684505393L;
    /**
     * A 3D list: D1 is player, D2 is train id, D3 is train position.
     */
    List3D<ActivityAndTime> activityLists;
    /**
     * A 2D list: D1 is player, D2 is transaction.
     */
    List2D<TransactionWithTimeStamp> bankAccounts;
    List1D<Money> currentBalance;
    List1D<Serializable> items;
    /**
     * A 3D list: D1 is player, D2 is type, D3 is element.
     */
    List3D<Serializable> lists;
    Serializable[][] map;
    List1D<Player> players;
    /**
     * A 2D list: D1 is type, D2 is element.
     */
    List2D<Serializable> sharedLists;
    GameTime time = GameTime.BIG_BANG;

    /**
     *
     */
    public WorldImpl() {
        this(0, 0);
    }

    /**
     * @param mapWidth
     * @param mapHeight
     */
    public WorldImpl(int mapWidth, int mapHeight) {
        activityLists = new List3DImpl<>(0, 0);
        bankAccounts = new List2DImpl<>(0);
        currentBalance = new List1DImpl<>();
        items = new List1DImpl<>(ITEM.getNumberOfKeys());
        lists = new List3DImpl<>(0, KEY.getNumberOfKeys());
        players = new List1DImpl<>();
        sharedLists = new List2DImpl<>(SKEY
                .getNumberOfKeys());
        time = GameTime.BIG_BANG;
        setupItems();
        setupMap(mapWidth, mapHeight);

    }

    /**
     * @param p
     * @param index
     * @param element
     */
    @SuppressWarnings("unchecked")
    public void add(FreerailsPrincipal p, int index, Activity element) {
        int playerIndex = p.getWorldIndex();
        int lastID = activityLists.sizeD3(playerIndex, index) - 1;
        ActivityAndTime last = activityLists.get(playerIndex, index, lastID);
        double duration = last.act.duration();
        double lastFinishTime = last.startTime + duration;
        double thisStartTime = Math.max(lastFinishTime, currentTime()
                .getTicks());
        ActivityAndTime ant = new ActivityAndTime(element, thisStartTime);
        activityLists.addD3(playerIndex, index, ant);
    }

    public int add(FreerailsPrincipal p, KEY key, Serializable element) {
        int playerIndex = p.getWorldIndex();
        return lists.addD3(playerIndex, key.getKeyID(), element);
    }

    public int add(SKEY key, Serializable element) {
        return sharedLists.addD2(key.getKeyID(), element);
    }

    /**
     * @param p
     * @param element
     * @return
     */
    public int addActiveEntity(FreerailsPrincipal p, Activity element) {
        int playerIndex = p.getWorldIndex();
        int index = activityLists.addD2(playerIndex);
        ActivityAndTime ant = new ActivityAndTime(element, currentTime()
                .getTicks());
        activityLists.addD3(playerIndex, index, ant);
        return index;
    }

    /**
     * @param player Player to add
     * @return index of the player
     */
    public int addPlayer(Player player) {
        if (null == player) {
            throw new NullPointerException();
        }

        int index = players.add(player);
        bankAccounts.addD1();
        currentBalance.add(new Money(0));

        lists.addD1();
        for (int i = 0; i < KEY.getNumberOfKeys(); i++) {
            lists.addD2(index);
        }
        activityLists.addD1();

        return index;
    }

    public void addTransaction(FreerailsPrincipal p, Transaction t) {
        int playerIndex = p.getWorldIndex();
        TransactionWithTimeStamp tats = new TransactionWithTimeStamp(t, time);
        bankAccounts.addD2(playerIndex, tats);
        Money oldBalance = currentBalance.get(playerIndex);
        Money newBalance = new Money(t.deltaCash().getAmount()
                + oldBalance.getAmount());
        currentBalance.set(playerIndex, newBalance);
    }

    /**
     * @param p
     * @param k
     * @param index
     * @return
     */
    public boolean boundsContain(FreerailsPrincipal p, KEY k, int index) {
        if (!isPlayer(p)) {
            return false;
        } else return index >= 0 && index < this.size(p, k);
    }

    /**
     * @param x
     * @param y
     * @return
     */
    public boolean boundsContain(int x, int y) {
        return x >= 0 && x < getMapWidth() && y >= 0 && y < getMapHeight();
    }

    /**
     * @param k
     * @param index
     * @return
     */
    public boolean boundsContain(SKEY k, int index) {
        return (index >= 0 && index < this.size(k));
    }

    /**
     * @return
     */
    public GameTime currentTime() {
        return time;
    }

    public World defensiveCopy() {
        return (World) Utils.cloneBySerialisation(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WorldImpl) {
            WorldImpl test = (WorldImpl) o;

            // Compare players
            int numberOfPlayers = getNumberOfPlayers();
            if (numberOfPlayers != test.getNumberOfPlayers())
                return false;

            for (int i = 0; i < numberOfPlayers; i++) {
                if (!getPlayer(i).equals(test.getPlayer(i)))
                    return false;
            }

            // Compare lists
            if (!lists.equals(test.lists)) {
                return false;
            }
            if (!sharedLists.equals(test.sharedLists)) {
                return false;
            }
            if (!activityLists.equals(test.activityLists)) {
                return false;
            }
            if (!items.equals(test.items)) {
                return false;
            }
            if (!bankAccounts.equals(test.bankAccounts)) {
                return false;
            }

            // Compare maps
            if ((this.getMapWidth() != test.getMapWidth())
                    || (this.getMapHeight() != test.getMapHeight())) {
                return false;
            }
            for (int x = 0; x < this.getMapWidth(); x++) {
                for (int y = 0; y < this.getMapHeight(); y++) {
                    if (!getTile(x, y).equals(test.getTile(x, y))) {
                        return false;
                    }
                }
            }

            // phew!
            return true;
        }
        return false;
    }

    public Serializable get(FreerailsPrincipal p, KEY key, int index) {
        int playerIndex = p.getWorldIndex();
        return lists.get(playerIndex, key.getKeyID(), index);
    }

    public Serializable get(ITEM item) {
        return items.get(item.getKeyID());
    }

    public Serializable get(SKEY key, int index) {
        return sharedLists.get(key.getKeyID(), index);
    }

    /**
     * @param p
     * @param index
     * @return
     */
    public ActivityIterator getActivities(final FreerailsPrincipal p, int index) {
        final int playerIndex = p.getWorldIndex();
        return new ActivityIteratorImpl(playerIndex, index);
    }

    /**
     * @param p
     * @return
     */
    public Money getCurrentBalance(FreerailsPrincipal p) {
        int playerIndex = p.getWorldIndex();
        return currentBalance.get(playerIndex);
    }

    /**
     * @param p
     * @return
     */
    public int getID(FreerailsPrincipal p) {
        return p.getWorldIndex();
    }

    public int getMapHeight() {
        if (map.length == 0) {
            // When the map size is 0*0 we get a
            // java.lang.ArrayIndexOutOfBoundsException: 0
            // if we don't have the check above.
            return 0;
        }
        return map[0].length;
    }

    public int getMapWidth() {
        return map.length;
    }

    /**
     * @return
     */
    public int getNumberOfPlayers() {
        return players.size();
    }

    /**
     * @param p
     * @return
     */
    public int getNumberOfTransactions(FreerailsPrincipal p) {
        int playerIndex = p.getWorldIndex();
        return bankAccounts.sizeD2(playerIndex);
    }

    /**
     * @param i
     * @return
     */
    public Player getPlayer(int i) {
        return players.get(i);
    }

    public Serializable getTile(int x, int y) {
        return map[x][y];
    }

    /**
     * @param p
     * @param i
     * @return
     */
    public Transaction getTransaction(FreerailsPrincipal p, int i) {
        int playerIndex = p.getWorldIndex();
        TransactionWithTimeStamp tats = bankAccounts.get(playerIndex, i);
        return tats.getTransaction();
    }

    /**
     * @param p
     * @param i
     * @return
     */
    public GameTime getTransactionTimeStamp(FreerailsPrincipal p, int i) {
        int playerIndex = p.getWorldIndex();
        TransactionWithTimeStamp tats = bankAccounts.get(playerIndex, i);
        return tats.getTimestamp();
    }

    /**
     * @param p
     * @param i
     * @return
     */
    public Pair<Transaction, GameTime> getTransactionAndTimeStamp(
            FreerailsPrincipal p, int i) {
        int playerIndex = p.getWorldIndex();
        TransactionWithTimeStamp tats = bankAccounts.get(playerIndex, i);
        return new Pair<>(tats.getTransaction(), tats.getTimestamp());
    }

    @Override
    public int hashCode() {
        int result;
        result = players.size();

        return result;
    }

    /**
     * @param p
     * @return
     */
    public boolean isPlayer(FreerailsPrincipal p) {
        return p.getWorldIndex() >= 0 && p.getWorldIndex() < players.size();
    }

    public Serializable removeLast(FreerailsPrincipal p, KEY key) {
        int playerIndex = p.getWorldIndex();
        return lists.removeLastD3(playerIndex, key.getKeyID());
    }

    public Serializable removeLast(SKEY key) {

        return sharedLists.removeLastD2(key.getKeyID());
    }

    /**
     * @param p
     * @return
     */
    public Activity removeLastActiveEntity(FreerailsPrincipal p) {
        int playerIndex = p.getWorldIndex();
        int lastID = activityLists.sizeD2(playerIndex) - 1;
        Activity act = activityLists.removeLastD3(playerIndex, lastID).act;
        activityLists.removeLastD2(playerIndex);
        return act;
    }

    /**
     * @param p
     * @param index
     * @return
     */
    public Activity removeLastActivity(FreerailsPrincipal p, int index) {
        int playerIndex = p.getWorldIndex();
        if (activityLists.sizeD3(playerIndex, index) < 2)
            throw new IllegalStateException();

        return activityLists.removeLastD3(playerIndex, index).act;
    }

    /**
     * Removes the last player to be added.
     *
     * @return the player that was removed.
     * @throws IllegalStateException if any elements belonging to the player have not been
     *                               removed.
     */
    public Player removeLastPlayer() {

        int playerID = bankAccounts.removeLastD1();
        while (lists.sizeD2(playerID) > 0)
            lists.removeLastD2(playerID);

        lists.removeLastD1();
        currentBalance.removeLast();
        activityLists.removeLastD1();

        return players.removeLast();
    }

    public Transaction removeLastTransaction(FreerailsPrincipal p) {
        int playerIndex = p.getWorldIndex();
        TransactionWithTimeStamp tats = bankAccounts.removeLastD2(playerIndex);
        Money oldBalance = currentBalance.get(playerIndex);
        Money newBalance = new Money(oldBalance.getAmount()
                - tats.getTransaction().deltaCash().getAmount());
        currentBalance.set(playerIndex, newBalance);
        return tats.getTransaction();
    }

    public void set(FreerailsPrincipal p, KEY key, int index,
                    Serializable element) {
        int playerIndex = p.getWorldIndex();
        lists.set(playerIndex, key.getKeyID(), index, element);
    }

    public void set(ITEM item, Serializable element) {
        items.set(item.getKeyID(), element);
    }

    public void set(SKEY key, int index, Serializable element) {
        sharedLists.set(key.getKeyID(), index, element);
    }

    public void setTile(int x, int y, Serializable element) {
        map[x][y] = element;
    }

    /**
     * @param t
     */
    public void setTime(GameTime t) {
        time = t;

    }

    void setupItems() {
        this.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        time = new GameTime(0);
        this.set(ITEM.ECONOMIC_CLIMATE, EconomicClimate.MODERATION);
    }

    /**
     * @param mapWidth
     * @param mapHeight
     */
    public void setupMap(int mapWidth, int mapHeight) {
        map = new Serializable[mapWidth][mapHeight];

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y] = FreerailsTile.NULL;
            }
        }
    }

    public int size(FreerailsPrincipal p) {
        int playerIndex = p.getWorldIndex();
        return activityLists.sizeD2(playerIndex);
    }

    public int size(FreerailsPrincipal p, KEY key) {
        int playerIndex = p.getWorldIndex();
        return lists.sizeD3(playerIndex, key.getKeyID());
    }

    public int size(SKEY key) {
        return sharedLists.sizeD2(key.getKeyID());
    }

    /**
     * @param p
     * @return
     */
    public int getNumberOfActiveEntities(FreerailsPrincipal p) {
        int playerIndex = p.getWorldIndex();
        return activityLists.sizeD2(playerIndex);
    }

    /**
     *
     */
    public static class ActivityAndTime implements Serializable {

        private static final long serialVersionUID = -5149207279086814649L;

        /**
         *
         */
        public final Activity act;

        /**
         *
         */
        public final double startTime;

        ActivityAndTime(Activity act, double time) {
            this.act = act;
            startTime = time;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ActivityAndTime))
                return false;

            final ActivityAndTime activityAndTime = (ActivityAndTime) o;

            if (!act.equals(activityAndTime.act))
                return false;
            return !(startTime != activityAndTime.startTime);
        }

        @Override
        public int hashCode() {
            int result;
            result = act.hashCode();
            result = 29 * result + (int) startTime;
            return result;
        }

    }

    /**
     *
     */
    public class ActivityIteratorImpl implements ActivityIterator {

        /**
         *
         */
        public final int size;
        private final List<ActivityAndTime> currentList;

        /**
         *
         */
        public int activityIndex = 0;
        private ActivityAndTime ant;

        /**
         * @param playerIndex
         * @param index
         */
        public ActivityIteratorImpl(int playerIndex, int index) {
            currentList = activityLists.get(playerIndex, index);
            size = currentList.size();
            ant = currentList.get(activityIndex);
        }

        public double absoluteToRelativeTime(double t) {
            double dt = t - ant.startTime;
            dt = Math.min(dt, ant.act.duration());
            return dt;
        }

        /**
         * @return
         */
        public Activity getActivity() {
            return ant.act;
        }

        /**
         * @return
         */
        public double getDuration() {
            return ant.act.duration();
        }

        public double getFinishTime() {
            return ant.startTime + ant.act.duration();
        }

        public double getStartTime() {
            return ant.startTime;
        }

        /**
         * @param t
         * @return
         */
        public Serializable getState(double t) {
            double dt = absoluteToRelativeTime(t);
            return ant.act.getState(dt);
        }

        /**
         * @return
         */
        public boolean hasNext() {
            return (activityIndex + 1) < size;
        }

        /**
         *
         */
        public void nextActivity() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            activityIndex++;
            ant = currentList.get(activityIndex);
        }

        /**
         *
         */
        public void gotoLastActivity() {
            activityIndex = size - 1;
            ant = currentList.get(activityIndex);
        }

        /**
         * @return
         */
        public boolean hasPrevious() {
            return activityIndex >= 1;
        }

        /**
         * @throws NoSuchElementException
         */
        public void previousActivity() throws NoSuchElementException {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            activityIndex--;
            ant = currentList.get(activityIndex);

        }

    }
}

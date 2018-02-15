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

package freerails.model.world;

import freerails.util.*;
import freerails.model.*;
import freerails.model.finances.EconomicClimate;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionRecord;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameTime;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;
import freerails.model.terrain.FullTerrainTile;

import java.io.Serializable;

/**
 * An implementation of World that uses standard java.util collections
 * internally.
 */
public class FullWorld implements World {

    private static final long serialVersionUID = 3544393612684505393L;
    /**
     * A 3D list: D1 is player, D2 is train id, D3 is train position.
     */
    public List3D<ActivityAndTime> activityLists;
    /**
     * A 2D list: D1 is player, D2 is transaction.
     */
    public List2D<TransactionRecord> bankAccounts;
    public List1D<Money> currentBalance;
    public List1D<Serializable> items;
    /**
     * A 3D list: D1 is player, D2 is type, D3 is element.
     */
    public List3D<Serializable> lists;
    private Serializable[][] map;
    public List1D<Player> players;
    /**
     * A 2D list: D1 is type, D2 is element.
     */
    public List2D<Serializable> sharedLists;
    public GameTime time = GameTime.BIG_BANG;

    /**
     *
     */
    public FullWorld() {
        this(0, 0);
    }

    /**
     * @param mapWidth
     * @param mapHeight
     */
    public FullWorld(int mapWidth, int mapHeight) {
        activityLists = new List3DImpl<>(0, 0);
        bankAccounts = new List2DImpl<>(0);
        currentBalance = new List1DImpl<>();
        items = new List1DImpl<>(ITEM.getNumberOfKeys());
        lists = new List3DImpl<>(0, WorldKey.getNumberOfKeys());
        players = new List1DImpl<>();
        sharedLists = new List2DImpl<>(WorldSharedKey.getNumberOfKeys());
        time = GameTime.BIG_BANG;
        setupItems();
        setupMap(mapWidth, mapHeight);
    }

    /**
     * @param principal
     * @param index
     * @param element
     */
    public void add(FreerailsPrincipal principal, int index, Activity element) {
        int playerIndex = principal.getWorldIndex();
        int lastID = activityLists.sizeD3(playerIndex, index) - 1;
        ActivityAndTime last = activityLists.get(playerIndex, index, lastID);
        double duration = last.act.duration();
        double lastFinishTime = last.startTime + duration;
        double thisStartTime = Math.max(lastFinishTime, currentTime().getTicks());
        ActivityAndTime ant = new ActivityAndTime(element, thisStartTime);
        activityLists.addD3(playerIndex, index, ant);
    }

    public int add(FreerailsPrincipal principal, WorldKey worldKey, Serializable element) {
        int playerIndex = principal.getWorldIndex();
        return lists.addD3(playerIndex, worldKey.getId(), element);
    }

    public int add(WorldSharedKey key, Serializable element) {
        return sharedLists.addD2(key.getId(), element);
    }

    /**
     * @param principal
     * @param element
     * @return
     */
    public int addActiveEntity(FreerailsPrincipal principal, Activity element) {
        int playerIndex = principal.getWorldIndex();
        int index = activityLists.addD2(playerIndex);
        ActivityAndTime ant = new ActivityAndTime(element, currentTime().getTicks());
        activityLists.addD3(playerIndex, index, ant);
        return index;
    }

    /**
     * @param player Player to add
     * @return index of the player
     */
    public int addPlayer(Player player) {
        Utils.verifyNotNull(player);
        int index = players.add(player);
        bankAccounts.addD1();
        currentBalance.add(new Money(0));

        lists.addD1();
        for (int i = 0; i < WorldKey.getNumberOfKeys(); i++) {
            lists.addD2(index);
        }
        activityLists.addD1();

        return index;
    }

    public void addTransaction(FreerailsPrincipal principal, Transaction transaction) {
        int playerIndex = principal.getWorldIndex();
        TransactionRecord transactionRecord = new TransactionRecord(transaction, time);
        bankAccounts.addD2(playerIndex, transactionRecord);
        Money oldBalance = currentBalance.get(playerIndex);
        Money newBalance = Money.add(transaction.price(), oldBalance);
        currentBalance.set(playerIndex, newBalance);
    }

    /**
     * @param p
     * @param k
     * @param index
     * @return
     */
    public boolean boundsContain(FreerailsPrincipal p, WorldKey k, int index) {
        if (!isPlayer(p)) {
            return false;
        } else return index >= 0 && index < size(p, k);
    }

    /**
     * @param p
     * @return
     */
    public boolean boundsContain(Vector2D p) {
        // TODO use compareTo instead
        return p.x >= 0 && p.x < getMapWidth() && p.y >= 0 && p.y < getMapHeight();
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
    public boolean equals(Object obj) {
        if (obj instanceof FullWorld) {
            FullWorld test = (FullWorld) obj;

            // Compare players
            int numberOfPlayers = getNumberOfPlayers();
            if (numberOfPlayers != test.getNumberOfPlayers()) return false;

            for (int i = 0; i < numberOfPlayers; i++) {
                if (!getPlayer(i).equals(test.getPlayer(i))) return false;
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
            if ((getMapWidth() != test.getMapWidth()) || (getMapHeight() != test.getMapHeight())) {
                return false;
            }
            for (int x = 0; x < getMapWidth(); x++) {
                for (int y = 0; y < getMapHeight(); y++) {
                    Vector2D p = new Vector2D(x, y);
                    if (!getTile(p).equals(test.getTile(p))) {
                        return false;
                    }
                }
            }

            // phew!
            return true;
        }
        return false;
    }

    public Serializable get(FreerailsPrincipal p, WorldKey worldKey, int index) {
        int playerIndex = p.getWorldIndex();
        return lists.get(playerIndex, worldKey.getId(), index);
    }

    public Serializable get(ITEM item) {
        return items.get(item.getKeyID());
    }

    public Serializable get(WorldSharedKey key, int index) {
        return sharedLists.get(key.getId(), index);
    }

    /**
     * @param p
     * @param index
     * @return
     */
    public ActivityIterator getActivities(final FreerailsPrincipal p, int index) {
        final int playerIndex = p.getWorldIndex();
        return new ActivityIteratorImpl(this, playerIndex, index);
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

    public Serializable getTile(Vector2D p) {
        return map[p.x][p.y];
    }

    /**
     * @param p
     * @param i
     * @return
     */
    public Transaction getTransaction(FreerailsPrincipal p, int i) {
        int playerIndex = p.getWorldIndex();
        TransactionRecord tats = bankAccounts.get(playerIndex, i);
        return tats.getTransaction();
    }

    /**
     * @param p
     * @param i
     * @return
     */
    public GameTime getTransactionTimeStamp(FreerailsPrincipal p, int i) {
        int playerIndex = p.getWorldIndex();
        TransactionRecord tats = bankAccounts.get(playerIndex, i);
        return tats.getTimestamp();
    }

    /**
     * @param p
     * @param i
     * @return
     */
    public Pair<Transaction, GameTime> getTransactionAndTimeStamp(FreerailsPrincipal p, int i) {
        int playerIndex = p.getWorldIndex();
        TransactionRecord tats = bankAccounts.get(playerIndex, i);
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

    public Serializable removeLast(FreerailsPrincipal principal, WorldKey worldKey) {
        int playerIndex = principal.getWorldIndex();
        return lists.removeLastD3(playerIndex, worldKey.getId());
    }

    public Serializable removeLast(WorldSharedKey key) {

        return sharedLists.removeLastD2(key.getId());
    }

    /**
     * @param principal
     * @return
     */
    public Activity removeLastActiveEntity(FreerailsPrincipal principal) {
        int playerIndex = principal.getWorldIndex();
        int lastID = activityLists.sizeD2(playerIndex) - 1;
        Activity act = activityLists.removeLastD3(playerIndex, lastID).act;
        activityLists.removeLastD2(playerIndex);
        return act;
    }

    /**
     * @param principal
     * @param index
     * @return
     */
    public void removeLastActivity(FreerailsPrincipal principal, int index) {
        int playerIndex = principal.getWorldIndex();
        if (activityLists.sizeD3(playerIndex, index) < 2) throw new IllegalStateException();

        activityLists.removeLastD3(playerIndex, index);
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
        while (lists.sizeD2(playerID) > 0) lists.removeLastD2(playerID);

        lists.removeLastD1();
        currentBalance.removeLast();
        activityLists.removeLastD1();

        return players.removeLast();
    }

    public Transaction removeLastTransaction(FreerailsPrincipal principal) {
        int playerIndex = principal.getWorldIndex();
        TransactionRecord transactionRecord = bankAccounts.removeLastD2(playerIndex);
        Money oldBalance = currentBalance.get(playerIndex);
        Money newBalance = Money.subtract(oldBalance, transactionRecord.getTransaction().price());
        currentBalance.set(playerIndex, newBalance);
        return transactionRecord.getTransaction();
    }

    public void set(FreerailsPrincipal principal, WorldKey worldKey, int index, Serializable element) {
        int playerIndex = principal.getWorldIndex();
        lists.set(playerIndex, worldKey.getId(), index, element);
    }

    public void set(ITEM item, Serializable element) {
        items.set(item.getKeyID(), element);
    }

    public void set(WorldSharedKey key, int index, Serializable element) {
        sharedLists.set(key.getId(), index, element);
    }

    public void setTile(Vector2D p, Serializable tile) {
        map[p.x][p.y] = tile;
    }

    /**
     * @param t
     */
    public void setTime(GameTime t) {
        time = t;
    }

    private void setupItems() {
        set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        time = new GameTime(0);
        set(ITEM.ECONOMIC_CLIMATE, EconomicClimate.MODERATION);
    }

    /**
     * @param mapWidth
     * @param mapHeight
     */
    public void setupMap(int mapWidth, int mapHeight) {
        map = new Serializable[mapWidth][mapHeight];

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y] = FullTerrainTile.NULL;
            }
        }
    }

    public int size(FreerailsPrincipal p) {
        int playerIndex = p.getWorldIndex();
        return activityLists.sizeD2(playerIndex);
    }

    public int size(FreerailsPrincipal p, WorldKey worldKey) {
        int playerIndex = p.getWorldIndex();
        return lists.sizeD3(playerIndex, worldKey.getId());
    }

    public int size(WorldSharedKey key) {
        return sharedLists.sizeD2(key.getId());
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

        private ActivityAndTime(Activity act, double time) {
            this.act = act;
            startTime = time;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ActivityAndTime)) return false;

            final ActivityAndTime activityAndTime = (ActivityAndTime) obj;

            if (!act.equals(activityAndTime.act)) return false;
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

}

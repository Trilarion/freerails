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
 * An implementation of World that uses standard java.util collections internally.
 */
public class FullWorld implements World {

    private static final long serialVersionUID = 3544393612684505393L;
    /**
     * A 3D list: D1 is player, D2 is train id, D3 is train position.
     */
    public List3D<ActivityAndTime> activityLists = new List3DImpl<>(0, 0);
    /**
     * A 2D list: D1 is player, D2 is transaction.
     */
    public List2D<TransactionRecord> bankAccounts = new List2DImpl<>(0);
    public List1D<Money> currentBalance = new List1DImpl<>();
    public List1D<Serializable> items = new List1DImpl<>(WorldItem.getNumberOfKeys());
    /**
     * A 3D list: D1 is player, D2 is type, D3 is element.
     */
    public List3D<Serializable> lists = new List3DImpl<>(0, PlayerKey.getNumberOfKeys());
    private Serializable[][] map;
    public List1D<Player> players = new List1DImpl<>();
    /**
     * A 2D list: D1 is type, D2 is element.
     */
    public List2D<Serializable> sharedLists = new List2DImpl<>(SharedKey.getNumberOfKeys());
    public GameTime time = new GameTime(0);

    /**
     *
     */
    public FullWorld() {
        this(Vector2D.ZERO);
    }

    /**
     * @param mapSize
     */
    public FullWorld(Vector2D mapSize) {
        set(WorldItem.Calendar, new GameCalendar(1200, 1840));
        set(WorldItem.EconomicClimate, EconomicClimate.MODERATION);
        setupMap(mapSize);
    }

    /**
     * @param principal
     * @param index
     * @param activity
     */
    public void addActivity(FreerailsPrincipal principal, int index, Activity activity) {
        int playerIndex = principal.getWorldIndex();
        int lastID = activityLists.sizeD3(playerIndex, index) - 1;
        ActivityAndTime last = activityLists.get(playerIndex, index, lastID);
        double duration = last.act.duration();
        double lastFinishTime = last.startTime + duration;
        double thisStartTime = Math.max(lastFinishTime, currentTime().getTicks());
        ActivityAndTime ant = new ActivityAndTime(activity, thisStartTime);
        activityLists.addD3(playerIndex, index, ant);
    }

    public int add(FreerailsPrincipal principal, PlayerKey playerKey, Serializable element) {
        int playerIndex = principal.getWorldIndex();
        return lists.addD3(playerIndex, playerKey.getId(), element);
    }

    public int add(SharedKey key, Serializable element) {
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
        currentBalance.add(Money.ZERO);

        lists.addD1();
        for (int i = 0; i < PlayerKey.getNumberOfKeys(); i++) {
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
     * @param principal
     * @param key
     * @param index
     * @return
     */
    public boolean boundsContain(FreerailsPrincipal principal, PlayerKey key, int index) {
        if (!isPlayer(principal)) {
            return false;
        } else return index >= 0 && index < size(principal, key);
    }

    /**
     * @param location
     * @return
     */
    public boolean boundsContain(Vector2D location) {
        // TODO use compareTo instead
        Vector2D mapSize = getMapSize();
        return location.x >= 0 && location.x < mapSize.x && location.y >= 0 && location.y < mapSize.y;
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
            FullWorld other = (FullWorld) obj;

            // Compare players
            int numberOfPlayers = getNumberOfPlayers();
            if (numberOfPlayers != other.getNumberOfPlayers()) return false;

            for (int i = 0; i < numberOfPlayers; i++) {
                if (!getPlayer(i).equals(other.getPlayer(i))) return false;
            }

            // Compare lists
            if (!lists.equals(other.lists)) {
                return false;
            }
            if (!sharedLists.equals(other.sharedLists)) {
                return false;
            }
            if (!activityLists.equals(other.activityLists)) {
                return false;
            }
            if (!items.equals(other.items)) {
                return false;
            }
            if (!bankAccounts.equals(other.bankAccounts)) {
                return false;
            }

            // Compare maps
            Vector2D mapSize = getMapSize();
            if (!mapSize.equals(other.getMapSize())) {
                return false;
            }
            for (int x = 0; x < mapSize.x; x++) {
                for (int y = 0; y < mapSize.y; y++) {
                    Vector2D p = new Vector2D(x, y);
                    if (!getTile(p).equals(other.getTile(p))) {
                        return false;
                    }
                }
            }

            // phew!
            return true;
        }
        return false;
    }

    public Serializable get(FreerailsPrincipal principal, PlayerKey key, int index) {
        int playerIndex = principal.getWorldIndex();
        return lists.get(playerIndex, key.getId(), index);
    }

    public Serializable get(WorldItem item) {
        return items.get(item.getId());
    }

    public Serializable get(SharedKey key, int index) {
        return sharedLists.get(key.getId(), index);
    }

    /**
     * @param principal
     * @param index
     * @return
     */
    public ActivityIterator getActivities(final FreerailsPrincipal principal, int index) {
        final int playerIndex = principal.getWorldIndex();
        return new ActivityIteratorImpl(this, playerIndex, index);
    }

    /**
     * @param principal
     * @return
     */
    public Money getCurrentBalance(FreerailsPrincipal principal) {
        int playerIndex = principal.getWorldIndex();
        return currentBalance.get(playerIndex);
    }

    /**
     * @param principal
     * @return
     */
    public int getID(FreerailsPrincipal principal) {
        return principal.getWorldIndex();
    }

    public Vector2D getMapSize() {
        if (map.length == 0) {
            return Vector2D.ZERO;
        }
        // When the map size is 0*0 we get a
        // java.lang.ArrayIndexOutOfBoundsException: 0
        // if we don't have the check above.
        return new Vector2D(map.length, map[0].length);
    }

    /**
     * @return
     */
    public int getNumberOfPlayers() {
        return players.size();
    }

    /**
     * @param principal
     * @return
     */
    public int getNumberOfTransactions(FreerailsPrincipal principal) {
        int playerIndex = principal.getWorldIndex();
        return bankAccounts.sizeD2(playerIndex);
    }

    /**
     * @param i
     * @return
     */
    public Player getPlayer(int i) {
        return players.get(i);
    }

    public Serializable getTile(Vector2D location) {
        return map[location.x][location.y];
    }

    /**
     * @param principal
     * @param i
     * @return
     */
    public Transaction getTransaction(FreerailsPrincipal principal, int i) {
        int playerIndex = principal.getWorldIndex();
        TransactionRecord tats = bankAccounts.get(playerIndex, i);
        return tats.getTransaction();
    }

    /**
     * @param principal
     * @param i
     * @return
     */
    public GameTime getTransactionTimeStamp(FreerailsPrincipal principal, int i) {
        int playerIndex = principal.getWorldIndex();
        TransactionRecord tats = bankAccounts.get(playerIndex, i);
        return tats.getTimestamp();
    }

    /**
     * @param principal
     * @param i
     * @return
     */
    public Pair<Transaction, GameTime> getTransactionAndTimeStamp(FreerailsPrincipal principal, int i) {
        int playerIndex = principal.getWorldIndex();
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
     * @param principal
     * @return
     */
    public boolean isPlayer(FreerailsPrincipal principal) {
        return principal.getWorldIndex() >= 0 && principal.getWorldIndex() < players.size();
    }

    public Serializable removeLast(FreerailsPrincipal principal, PlayerKey playerKey) {
        int playerIndex = principal.getWorldIndex();
        return lists.removeLastD3(playerIndex, playerKey.getId());
    }

    public Serializable removeLast(SharedKey key) {

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

    public void set(FreerailsPrincipal principal, PlayerKey playerKey, int index, Serializable element) {
        int playerIndex = principal.getWorldIndex();
        lists.set(playerIndex, playerKey.getId(), index, element);
    }

    public void set(WorldItem worldItem, Serializable element) {
        items.set(worldItem.getId(), element);
    }

    public void set(SharedKey key, int index, Serializable element) {
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

    /**
     * @param mapSize
     */
    public void setupMap(Vector2D mapSize) {
        map = new Serializable[mapSize.x][mapSize.y];

        for (int x = 0; x < mapSize.x; x++) {
            for (int y = 0; y < mapSize.y; y++) {
                map[x][y] = FullTerrainTile.NULL;
            }
        }
    }

    public int size(FreerailsPrincipal principal) {
        int playerIndex = principal.getWorldIndex();
        return activityLists.sizeD2(playerIndex);
    }

    public int size(FreerailsPrincipal principal, PlayerKey key) {
        int playerIndex = principal.getWorldIndex();
        return lists.sizeD3(playerIndex, key.getId());
    }

    public int size(SharedKey key) {
        return sharedLists.sizeD2(key.getId());
    }

    /**
     * @param principal
     * @return
     */
    public int getNumberOfActiveEntities(FreerailsPrincipal principal) {
        int playerIndex = principal.getWorldIndex();
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

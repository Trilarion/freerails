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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of World that uses standard java.util collections internally.
 */
public class FullWorld implements World {

    private static final long serialVersionUID = 3544393612684505393L;
    public Map<FreerailsPrincipal, Map<Integer, List<ActivityAndTime>>> activities = new HashMap<>();
    public Map<FreerailsPrincipal, List<TransactionRecord>> transactionLogs = new HashMap<>();
    public List<Money> currentBalance = new ArrayList<>();
    public List<Serializable> items = new ArrayList<>();

    /**
     * A 3D list: D1 is player, D2 is type, D3 is element.
     */
    public Map<FreerailsPrincipal, Map<PlayerKey, List<Serializable>>> playerLists = new HashMap<>();
    private Serializable[][] map;
    public List<Player> players = new ArrayList<>();
    public Map<SharedKey, List<Serializable>> sharedKeyLists = new HashMap<>();
    public GameTime time = new GameTime(0);

    /**
     *
     */
    public FullWorld() {
        this(Vec2D.ZERO);
    }

    /**
     * @param mapSize
     */
    public FullWorld(Vec2D mapSize) {
        for (int i = 0; i < WorldItem.getNumberOfKeys(); i++) {
            items.add(null);
        }
        for (SharedKey key: SharedKey.values()) {
            sharedKeyLists.put(key, new ArrayList<>());
        }

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
        int lastId = activities.get(principal).get(index).size() - 1;
        ActivityAndTime last = activities.get(principal).get(index).get(lastId);
        double duration = last.act.duration();
        double lastFinishTime = last.startTime + duration;
        double thisStartTime = Math.max(lastFinishTime, currentTime().getTicks());
        ActivityAndTime ant = new ActivityAndTime(activity, thisStartTime);
        activities.get(principal).get(index).add(ant);
    }

    public int add(FreerailsPrincipal principal, PlayerKey playerKey, Serializable element) {
        List<Serializable> serializables = playerLists.get(principal).get(playerKey);
        serializables.add(element);
        return serializables.size() - 1;
    }

    public int add(SharedKey key, Serializable element) {
        List<Serializable> serializables = sharedKeyLists.get(key);
        serializables.add(element);
        return serializables.size() - 1;
    }

    /**
     * @param principal
     * @param element
     * @return
     */
    public int addActiveEntity(FreerailsPrincipal principal, Activity element) {
        int index = activities.get(principal).size();
        activities.get(principal).put(index, new ArrayList<>());
        ActivityAndTime ant = new ActivityAndTime(element, currentTime().getTicks());
        activities.get(principal).get(index).add(ant);
        return index;
    }

    /**
     * @param player Player to add
     * @return index of the player
     */
    public int addPlayer(Player player) {
        Utils.verifyNotNull(player);

        players.add(player);
        int index = players.size() - 1;

        transactionLogs.put(player.getPrincipal(), new ArrayList<>());
        currentBalance.add(Money.ZERO);

        playerLists.put(player.getPrincipal(), new HashMap<>());
        for (PlayerKey key: PlayerKey.values()) {
            playerLists.get(player.getPrincipal()).put(key, new ArrayList<>());
        }

        activities.put(player.getPrincipal(), new HashMap<>());

        return index;
    }

    public void addTransaction(FreerailsPrincipal principal, Transaction transaction) {
        int playerIndex = principal.getWorldIndex();
        TransactionRecord transactionRecord = new TransactionRecord(transaction, time);
        transactionLogs.get(principal).add(transactionRecord);
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
    public boolean boundsContain(Vec2D location) {
        // TODO use compareTo instead
        Vec2D mapSize = getMapSize();
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
            if (!playerLists.equals(other.playerLists)) {
                return false;
            }
            if (!sharedKeyLists.equals(other.sharedKeyLists)) {
                return false;
            }
            if (!activities.equals(other.activities)) {
                return false;
            }
            if (!items.equals(other.items)) {
                return false;
            }
            if (!transactionLogs.equals(other.transactionLogs)) {
                return false;
            }

            // Compare maps
            Vec2D mapSize = getMapSize();
            if (!mapSize.equals(other.getMapSize())) {
                return false;
            }
            for (int x = 0; x < mapSize.x; x++) {
                for (int y = 0; y < mapSize.y; y++) {
                    Vec2D p = new Vec2D(x, y);
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
        return playerLists.get(principal).get(key).get(index);
    }

    public Serializable get(WorldItem item) {
        return items.get(item.getId());
    }

    public Serializable get(SharedKey key, int index) {
        return sharedKeyLists.get(key).get(index);
    }

    /**
     * @param principal
     * @param index
     * @return
     */
    public ActivityIterator getActivities(final FreerailsPrincipal principal, int index) {
        return new ActivityIteratorImpl(this, principal, index);
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

    public Vec2D getMapSize() {
        if (map.length == 0) {
            return Vec2D.ZERO;
        }
        // When the map size is 0*0 we get a
        // java.lang.ArrayIndexOutOfBoundsException: 0
        // if we don't have the check above.
        return new Vec2D(map.length, map[0].length);
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
        return transactionLogs.get(principal).size();
    }

    /**
     * @param i
     * @return
     */
    public Player getPlayer(int i) {
        return players.get(i);
    }

    public Serializable getTile(Vec2D location) {
        return map[location.x][location.y];
    }

    /**
     * @param principal
     * @param i
     * @return
     */
    public Transaction getTransaction(FreerailsPrincipal principal, int i) {
        TransactionRecord transactionRecord = transactionLogs.get(principal).get(i);
        return transactionRecord.getTransaction();
    }

    /**
     * @param principal
     * @param i
     * @return
     */
    public GameTime getTransactionTimeStamp(FreerailsPrincipal principal, int i) {
        TransactionRecord transactionRecord = transactionLogs.get(principal).get(i);
        return transactionRecord.getTimestamp();
    }

    /**
     * @param principal
     * @param i
     * @return
     */
    public Pair<Transaction, GameTime> getTransactionAndTimeStamp(FreerailsPrincipal principal, int i) {
        TransactionRecord transactionRecord = transactionLogs.get(principal).get(i);
        return new Pair<>(transactionRecord.getTransaction(), transactionRecord.getTimestamp());
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
        List<Serializable> serializables = playerLists.get(principal).get(playerKey);
        return serializables.remove(serializables.size() - 1);
    }

    public Serializable removeLast(SharedKey key) {
        List<Serializable> serializables = sharedKeyLists.get(key);
        return serializables.remove(serializables.size() - 1);
    }

    /**
     * @param principal
     * @return
     */
    public Activity removeLastActiveEntity(FreerailsPrincipal principal) {
        int lastId = activities.get(principal).size() - 1;
        List<ActivityAndTime> serializables = activities.get(principal).get(lastId);
        Activity act = serializables.remove(serializables.size() - 1).act;
        activities.get(principal).remove(lastId);
        return act;
    }

    /**
     * @param principal
     * @param index
     * @return
     */
    public void removeLastActivity(FreerailsPrincipal principal, int index) {
        if (activities.get(principal).get(index).size() < 2) {
            throw new IllegalStateException();
        }
        List<ActivityAndTime> list = activities.get(principal).get(index);
        list.remove(list.size() - 1);
      }

    /**
     * Removes the last player to be added.
     *
     * @return the player that was removed.
     * @throws IllegalStateException if any elements belonging to the player have not been
     *                               removed.
     */
    public Player removeLastPlayer() {

        // TODO this does not work, because we should indicate which player we want to remove
        /*
        int playerID = bankAccounts.removeLastD1();
        while (lists.sizeD2(playerID) > 0) lists.removeLastD2(playerID);

        lists.removeLastD1();
        currentBalance.remove(currentBalance.size() - 1);
        activityLists.removeLastD1();

        return players.remove(players.size() - 1);
        */
        throw new UnsupportedOperationException();
    }

    public Transaction removeLastTransaction(FreerailsPrincipal principal) {
        int playerIndex = principal.getWorldIndex();

        List<TransactionRecord> transactions = transactionLogs.get(principal);
        TransactionRecord transactionRecord = transactions.remove(transactions.size()-1);

        Money oldBalance = currentBalance.get(playerIndex);
        Money newBalance = Money.subtract(oldBalance, transactionRecord.getTransaction().price());
        currentBalance.set(playerIndex, newBalance);
        return transactionRecord.getTransaction();
    }

    public void set(FreerailsPrincipal principal, PlayerKey playerKey, int index, Serializable element) {
        playerLists.get(principal).get(playerKey).set(index, element);
    }

    public void set(WorldItem worldItem, Serializable element) {
        items.set(worldItem.getId(), element);
    }

    public void set(SharedKey key, int index, Serializable element) {
        sharedKeyLists.get(key).set(index, element);
    }

    public void setTile(Vec2D p, Serializable tile) {
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
    public void setupMap(Vec2D mapSize) {
        map = new Serializable[mapSize.x][mapSize.y];

        for (int x = 0; x < mapSize.x; x++) {
            for (int y = 0; y < mapSize.y; y++) {
                map[x][y] = FullTerrainTile.NULL;
            }
        }
    }

    public int size(FreerailsPrincipal principal) {
        return activities.get(principal).size();
    }

    public int size(FreerailsPrincipal principal, PlayerKey key) {
        return playerLists.get(principal).get(key).size();
    }

    public int size(SharedKey key) {
        return sharedKeyLists.get(key).size();
    }

    /**
     * @param principal
     * @return
     */
    public int getNumberOfActiveEntities(FreerailsPrincipal principal) {
        return activities.get(principal).size();
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

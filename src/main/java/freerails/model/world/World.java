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

import freerails.model.Identifiable;
import freerails.model.activity.Activity;
import freerails.model.activity.ActivityAndTime;
import freerails.model.activity.ActivityIterator;
import freerails.model.activity.ActivityIteratorImpl;
import freerails.model.cargo.Cargo;
import freerails.model.game.GameRules;
import freerails.model.game.GameSpeed;
import freerails.model.station.Station;
import freerails.model.terrain.City;
import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.model.train.Engine;
import freerails.model.train.Train;
import freerails.util.*;
import freerails.model.finances.EconomicClimate;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionRecord;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameTime;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An implementation of World that uses standard java.util collections internally.
 *
 * Implements methods which can be used to alter the world. Notice
 * that in contrast to, say, {@code java.util.List} there is no remove()
 * method that shifts any subsequent elements to the left (subtracts one from
 * their indices). This means that an elements' position in a list can be used
 * as an address space independent way to reference the element. If you want to
 * remove an element from a list, you should set it to null, e.g.
 *
 * {@code world.set(KEY.Trains, 5, null, player);}
 *
 * Code that loops through lists should handle null values gracefully
 *
 */
public class World implements UnmodifiableWorld {

    private static final long serialVersionUID = 3544393612684505393L;
    public Map<Player, Map<Integer, List<ActivityAndTime>>> activities = new HashMap<>();
    public Map<Player, List<TransactionRecord>> transactionLogs = new HashMap<>();
    public List<Money> currentBalance = new ArrayList<>();

    private Vec2D mapSize;
    private TerrainTile[] map;
    public List<Player> players = new ArrayList<>();
    public GameTime time = new GameTime(0);

    // global lists
    private final SortedSet<Engine> engines;
    private final SortedSet<City> cities;
    private final SortedSet<Cargo> cargos;
    private final SortedSet<Terrain> terrainTypes;
    private final SortedSet<TrackType> trackTypes;

    // player specific lists
    private final Map<Player, SortedSet<Train>> trains; // a list of trains by player
    private final Map<Player, SortedSet<Station>> stations; // list of stations by player

    // single instance objects in the game world
    private final GameCalendar calendar;
    private EconomicClimate economicClimate;
    private GameRules gameRules;
    private GameSpeed gameSpeed;

    public static class Builder {

        private SortedSet<Engine> engines = new TreeSet<>();
        private SortedSet<City> cities = new TreeSet<>();
        private SortedSet<Cargo> cargos = new TreeSet<>();
        private SortedSet<Terrain> terrainTypes = new TreeSet<>();
        private SortedSet<TrackType> trackTypes = new TreeSet<>();
        private Map<Player, SortedSet<Train>> trains = new HashMap<>();
        private Map<Player, SortedSet<Station>> stations = new HashMap<>();
        private Vec2D mapSize = Vec2D.ZERO;

        public Builder setEngines(SortedSet<Engine> engines) {
            this.engines = Utils.verifyNotNull(engines);
            return this;
        }

        public Builder setCities(SortedSet<City> cities) {
            this.cities = Utils.verifyNotNull(cities);
            return this;
        }

        public Builder setCargos(SortedSet<Cargo> cargos) {
            this.cargos = Utils.verifyNotNull(cargos);
            return this;
        }

        public Builder setTerrainTypes(SortedSet<Terrain> terrainTypes) {
            this.terrainTypes = Utils.verifyNotNull(terrainTypes);
            return this;
        }

        public Builder setTrackTypes(SortedSet<TrackType> trackTypes) {
            this.trackTypes = trackTypes;
            return this;
        }

        public Builder setMapSize(Vec2D mapSize) {
            this.mapSize = mapSize;
            return this;
        }

        public World build() {
            return new World(this);
        }
    }

    public World(Builder builder) {
        engines = builder.engines;
        cities = builder.cities;
        cargos = builder.cargos;
        terrainTypes = builder.terrainTypes;
        trackTypes = builder.trackTypes;
        trains = builder.trains;
        stations = builder.stations;

        calendar = new GameCalendar(1200, 1840);
        economicClimate = EconomicClimate.MODERATION;
        gameRules = GameRules.DEFAULT_RULES;
        gameSpeed = new GameSpeed(10);
        setupMap(builder.mapSize);
    }

    // TODO getNumberXXX

    // TODO unmodifiable collection?
    public Collection<Engine> getEngines() {
        return engines;
    }

    public Engine getEngine(int id) {
        return get(id, engines);
    }

    // TODO unmodifiable collection
    public Collection<City> getCities() {
        return cities;
    }

    public City getCity(int id) {
        return get(id, cities);
    }

    // TODO unmodifiable collection?
    public Collection<Cargo> getCargos() {
        return cargos;
    }

    public Cargo getCargo(int id) {
        return get(id, cargos);
    }

    // TODO unmodifiable collection?
    public Collection<Terrain> getTerrains() {
        return terrainTypes;
    }

    public Terrain getTerrain(int id) {
        return get(id, terrainTypes);
    }

    // TODO unmodifiable collection?
    public Collection<TrackType> getTrackTypes() {
        return trackTypes;
    }

    public TrackType getTrackType(int id) {
        return get(id, trackTypes);
    }

    // TODO unmodifiable collection?
    public Collection<Train> getTrains(Player player) {
        return trains.get(player);
    }

    public Train getTrain(Player player, int id) {
        return get(id, trains.get(player));
    }

    public void addTrain(Player player, Train train) {
        if (contains(train.getId(), trains.get(player))) {
            throw new IllegalArgumentException("Train with id already existing. Cannot add.");
        }
        trains.get(player).add(train);
    }

    public void removeTrain(Player player, int id) {
        trains.get(player).remove(get(id, trains.get(player)));
    }

    // TODO unmodifiable collection?
    public Collection<Station> getStations(Player player) {
        return stations.get(player);
    }

    public Station getStation(Player player, int id) {
        return get(id, stations.get(player));
    }

    public void addStation(Player player, Station station) {
        if (contains(station.getId(), stations.get(player))) {
            throw new IllegalArgumentException("Station with id already existing. Cannot add.");
        }
        stations.get(player).add(station);
    }

    public void removeStation(Player player, int id) {
        stations.get(player).remove(get(id, stations.get(player)));
    }

    /**
     *
     * @param id
     * @param c
     * @param <E>
     * @return
     */
    private <E extends Identifiable> E get(final int id, @NotNull final Collection<E> c) {
        for (E e: c) {
            if (e.getId() == id) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("Element with Id=%d not existing in collection.", id));
    }

    /**
     *
     * @param id
     * @param c
     * @param <E>
     * @return
     */
    public static <E extends Identifiable> boolean contains(final int id, @NotNull final Collection<E> c) {
        for (E e: c) {
            if (e.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public GameCalendar getCalendar() {
        return calendar;
    }

    public EconomicClimate getEconomicClimate() {
        return economicClimate;
    }

    public GameRules getGameRules() {
        return gameRules;
    }

    public void setGameRules(@NotNull GameRules gameRules) {
        this.gameRules = gameRules;
    }

    public GameSpeed getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(@NotNull GameSpeed gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    /**
     * @param player
     * @param index
     * @param activity
     */
    public void addActivity(Player player, int index, Activity activity) {
        int lastId = activities.get(player).get(index).size() - 1;
        ActivityAndTime last = activities.get(player).get(index).get(lastId);
        double duration = last.act.duration();
        double lastFinishTime = last.startTime + duration;
        double thisStartTime = Math.max(lastFinishTime, currentTime().getTicks());
        ActivityAndTime ant = new ActivityAndTime(activity, thisStartTime);
        activities.get(player).get(index).add(ant);
    }

    /**
     * @param player
     * @param activity
     * @return
     */
    public int addActiveEntity(Player player, Activity activity) {
        int index = activities.get(player).size();
        activities.get(player).put(index, new ArrayList<>());
        ActivityAndTime ant = new ActivityAndTime(activity, currentTime().getTicks());
        activities.get(player).get(index).add(ant);
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

        transactionLogs.put(player, new ArrayList<>());
        currentBalance.add(Money.ZERO);

        // add trains
        if (trains.containsKey(player)) {
            throw new RuntimeException("something wrong");
        }
        trains.put(player, new TreeSet<>());

        // add stations
        if (stations.containsKey(player)) {
            throw new RuntimeException("something wrong");
        }
        stations.put(player, new TreeSet<>());

        activities.put(player, new HashMap<>());

        return index;
    }

    /**
     * Adds the specified transaction to the specified player's bank account.
     */
    public void addTransaction(Player player, Transaction transaction) {
        int playerIndex = player.getId();
        TransactionRecord transactionRecord = new TransactionRecord(transaction, time);
        transactionLogs.get(player).add(transactionRecord);
        Money oldBalance = currentBalance.get(playerIndex);
        Money newBalance = Money.add(transaction.price(), oldBalance);
        currentBalance.set(playerIndex, newBalance);
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof World) {
            World other = (World) obj;

            // Compare players
            if (!players.equals(other.players)) {
                return false;
            }

            // Compare lists
            if (!activities.equals(other.activities)) {
                return false;
            }
            if (!transactionLogs.equals(other.transactionLogs)) {
                return false;
            }

            // TODO compare calender, gamespeed, economic climate, ...

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

    /**
     * @param player
     * @param index
     * @return
     */
    public ActivityIterator getActivities(final Player player, int index) {
        return new ActivityIteratorImpl(this, player, index);
    }

    /**
     * @param player
     * @return
     */
    public Money getCurrentBalance(Player player) {
        int playerIndex = player.getId();
        return currentBalance.get(playerIndex);
    }

    /**
     * @param player
     * @return
     */
    public int getID(Player player) {
        return player.getId();
    }

    public Vec2D getMapSize() {
        return mapSize;
    }

    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(players);
    }

    /**
     * @param player
     * @return
     */
    public int getNumberOfTransactions(Player player) {
        return transactionLogs.get(player).size();
    }

    /**
     * @param i
     * @return
     */
    public Player getPlayer(int i) {
        return players.get(i);
    }

    private int getMapIndex(Vec2D location) {
        return location.x * mapSize.y + location.y;
    }

    public TerrainTile getTile(Vec2D location) {
        return map[getMapIndex(location)];
    }

    /**
     * @param player
     * @param i
     * @return
     */
    public Transaction getTransaction(Player player, int i) {
        TransactionRecord transactionRecord = transactionLogs.get(player).get(i);
        return transactionRecord.getTransaction();
    }

    /**
     * @param player
     * @param i
     * @return
     */
    public GameTime getTransactionTimeStamp(Player player, int i) {
        TransactionRecord transactionRecord = transactionLogs.get(player).get(i);
        return transactionRecord.getTimestamp();
    }

    /**
     * @param player
     * @param i
     * @return
     */
    public Pair<Transaction, GameTime> getTransactionAndTimeStamp(Player player, int i) {
        TransactionRecord transactionRecord = transactionLogs.get(player).get(i);
        return new Pair<>(transactionRecord.getTransaction(), transactionRecord.getTimestamp());
    }

    @Override
    public int hashCode() {
        int result;
        result = players.size();

        return result;
    }

    /**
     * @param player
     * @return
     */
    public boolean isPlayer(Player player) {
        return player.getId() >= 0 && player.getId() < players.size();
    }

    /**
     * @param player
     * @return
     */
    public Activity removeLastActiveEntity(Player player) {
        int lastId = activities.get(player).size() - 1;
        List<ActivityAndTime> serializables = activities.get(player).get(lastId);
        Activity act = serializables.remove(serializables.size() - 1).act;
        activities.get(player).remove(lastId);
        return act;
    }

    /**
     * @param player
     * @param index
     * @return
     */
    public void removeLastActivity(Player player, int index) {
        if (activities.get(player).get(index).size() < 2) {
            throw new IllegalStateException();
        }
        List<ActivityAndTime> list = activities.get(player).get(index);
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

    /**
     * Removes and returns the last transaction added the the specified
     * player's bank account. This method is only here so that moves that add
     * transactions can be undone.
     */
    public Transaction removeLastTransaction(Player player) {
        int playerIndex = player.getId();

        List<TransactionRecord> transactions = transactionLogs.get(player);
        TransactionRecord transactionRecord = transactions.remove(transactions.size()-1);

        Money oldBalance = currentBalance.get(playerIndex);
        Money newBalance = Money.subtract(oldBalance, transactionRecord.getTransaction().price());
        currentBalance.set(playerIndex, newBalance);
        return transactionRecord.getTransaction();
    }

    /**
     * Replaces the tile at the specified position on the map with the specified
     * tile.
     */
    public void setTile(Vec2D location, TerrainTile tile) {
        map[getMapIndex(location)] = tile;
    }

    // TODO instead of setting a new time, call advance on the time.
    /**
     * @param t
     */
    public void setTime(GameTime t) {
        time = t;
    }

    // TODO inline in constructor, we do not change the map size afterwards
    /**
     * @param mapSize
     */
    public void setupMap(Vec2D mapSize) {
        this.mapSize = mapSize;
        map = new TerrainTile[mapSize.x * mapSize.y];

        // TODO what is a good default initializer here or should we stay with null?
        // TODO terrainTypeId 0 must also exist as terrain, how to make sure
        for (int i = 0; i < map.length; i++) {
            map[i] = new TerrainTile(0);
        }
    }

    public int size(Player player) {
        return activities.get(player).size();
    }

}

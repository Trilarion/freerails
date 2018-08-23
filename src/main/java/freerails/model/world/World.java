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
import freerails.model.train.activity.Activity;
import freerails.model.cargo.Cargo;
import freerails.model.finance.transaction.Transaction;
import freerails.model.game.*;
import freerails.model.station.Station;
import freerails.model.terrain.city.City;
import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.model.train.Engine;
import freerails.model.train.Train;
import freerails.util.*;
import freerails.model.finance.Money;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// TODO for deserializing we might want to use playerId instead of Player as index in Maps
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
    public Map<Player, List<Transaction>> transactions = new HashMap<>();
    public List<Money> currentBalance = new ArrayList<>();

    // single instances
    private final Clock clock;
    private Speed speed = Speed.MODERATE;
    private Rules rules;
    private final Vec2D mapSize;
    private final TerrainTile[] map;
    private final List<Player> players = new ArrayList<>();

    // global lists
    private final SortedSet<Engine> engines;
    private final SortedSet<City> cities;
    private final SortedSet<Cargo> cargos;
    private final SortedSet<Terrain> terrainTypes;
    private final SortedSet<TrackType> trackTypes;
    private final SortedSet<Sentiment> sentiments;

    // player specific lists
    private final Map<Player, List<Train>> trains; // a list of trains by player
    private final Map<Player, SortedSet<Station>> stations; // list of stations by player



    // TODO this should also be set by the builder
    private int currentSentimentId = 2;

    // TODO need something to fill the Builder in loading scenario as well as going the other way (when writing out scenario)
    // TODO set trains?
    public static class Builder {

        private SortedSet<Engine> engines = new TreeSet<>();
        private SortedSet<City> cities = new TreeSet<>();
        private SortedSet<Cargo> cargos = new TreeSet<>();
        private SortedSet<Terrain> terrainTypes = new TreeSet<>();
        private SortedSet<TrackType> trackTypes = new TreeSet<>();
        private SortedSet<Sentiment> sentiments = new TreeSet<>();
        private Map<Player, List<Train>> trains = new HashMap<>();
        private Map<Player, SortedSet<Station>> stations = new HashMap<>();
        private Rules rules = null;
        private Vec2D mapSize = null;
        private Clock clock = new Clock(1840, 1200, new Time(0));

        public Builder setEngines(@NotNull SortedSet<Engine> engines) {
            this.engines = Utils.verifyNotNull(engines);
            return this;
        }

        public Builder setCities(@NotNull SortedSet<City> cities) {
            this.cities = Utils.verifyNotNull(cities);
            return this;
        }

        public Builder setCargos(@NotNull SortedSet<Cargo> cargos) {
            this.cargos = Utils.verifyNotNull(cargos);
            return this;
        }

        public Builder setTerrainTypes(@NotNull SortedSet<Terrain> terrainTypes) {
            this.terrainTypes = Utils.verifyNotNull(terrainTypes);
            return this;
        }

        public Builder setTrackTypes(@NotNull SortedSet<TrackType> trackTypes) {
            this.trackTypes = trackTypes;
            return this;
        }

        public Builder setMapSize(@NotNull Vec2D mapSize) {
            this.mapSize = mapSize;
            return this;
        }

        public Builder setRules(@NotNull Rules rules) {
            this.rules = rules;
            return this;
        }

        public Builder setSentiments(@NotNull SortedSet<Sentiment> sentiments) {
            this.sentiments = sentiments;
            return this;
        }

        public World build() {
            if (rules == null) {
                throw new RuntimeException("Rules not specified");
            }
            if (mapSize == null) {
                throw new RuntimeException("Map size not specified");
            }
            if (mapSize.x <= 0 | mapSize.y <= 0) {
                throw new RuntimeException("Map size must have positive entries");
            }
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
        rules = builder.rules;
        sentiments = builder.sentiments;

        clock = builder.clock;

        // setup map
        mapSize = builder.mapSize;
        map = new TerrainTile[builder.mapSize.x * builder.mapSize.y];

        // TODO what is a good default initializer here or should we stay with null?
        // TODO terrainTypeId 0 must also exist as terrain, how to make sure
        for (int i = 0; i < map.length; i++) {
            map[i] = new TerrainTile(0);
        }
    }

    // TODO getNumberXXX

    // TODO unmodifiable collection?
    @Override
    public Collection<Engine> getEngines() {
        return engines;
    }

    @Override
    public Engine getEngine(int id) {
        return get(id, engines);
    }

    // TODO unmodifiable collection
    @Override
    public Collection<City> getCities() {
        return cities;
    }

    @Override
    public City getCity(int id) {
        return get(id, cities);
    }

    // TODO unmodifiable collection?
    @Override
    public Collection<Cargo> getCargos() {
        return cargos;
    }

    @Override
    public Cargo getCargo(int id) {
        return get(id, cargos);
    }

    // TODO unmodifiable collection?
    @Override
    public Collection<Terrain> getTerrains() {
        return terrainTypes;
    }

    @Override
    public Terrain getTerrain(int id) {
        return get(id, terrainTypes);
    }

    // TODO unmodifiable collection?
    @Override
    public Collection<TrackType> getTrackTypes() {
        return trackTypes;
    }

    @Override
    public TrackType getTrackType(int id) {
        return get(id, trackTypes);
    }

    // TODO unmodifiable collection?
    @Override
    public Collection<Train> getTrains(Player player) {
        return trains.get(player);
    }

    @Override
    public Train getTrain(Player player, int id) {
        return get(id, trains.get(player));
    }

    public void addTrain(Player player, Train train) {
        if (Utils.containsId(train.getId(), trains.get(player))) {
            throw new IllegalArgumentException("Train with id already existing. Cannot add.");
        }
        trains.get(player).add(train);
    }

    public void removeTrain(Player player, int id) {
        trains.get(player).remove(get(id, trains.get(player)));
    }

    // TODO unmodifiable collection?
    @Override
    public Collection<Station> getStations(Player player) {
        return stations.get(player);
    }

    @Override
    public Station getStation(Player player, int id) {
        return get(id, stations.get(player));
    }

    public void addStation(Player player, Station station) {
        if (Utils.containsId(station.getId(), stations.get(player))) {
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

    @Override
    public Sentiment getSentiment() {
        return get(currentSentimentId, sentiments);
    }

    @Override
    public Rules getRules() {
        return rules;
    }

    // TODO undo this later, just for not breaking the tests
    public void setRules(@NotNull Rules rules) {
        this.rules = rules;
    }

    /**
     * @param player
     * @param trainId
     * @param activity
     */
    public void addActivity(Player player, int trainId, Activity activity) {
        Train train = getTrain(player, trainId);
        activity.setStartTime(clock.getCurrentTime().getTicks());
        train.addActivity(activity);
    }

    /**
     * @param player Player to add
     * @return index of the player
     */
    public int addPlayer(Player player) {
        Utils.verifyNotNull(player);

        players.add(player);
        int index = players.size() - 1;

        transactions.put(player, new ArrayList<>());
        currentBalance.add(Money.ZERO);

        // add trains
        if (trains.containsKey(player)) {
            throw new RuntimeException("something wrong");
        }
        trains.put(player, new ArrayList<>());

        // add stations
        if (stations.containsKey(player)) {
            throw new RuntimeException("something wrong");
        }
        stations.put(player, new TreeSet<>());

        return index;
    }

    // TODO check that transaction time is not in the future and not older than last stored transaction
    /**
     * Adds the specified transaction to the specified player's bank account.
     */
    public void addTransaction(Player player, Transaction transaction) {
        int playerIndex = player.getId();
        transactions.get(player).add(transaction);

        // adjust balance
        Money newBalance = Money.add(transaction.getAmount(), currentBalance.get(playerIndex));
        currentBalance.set(playerIndex, newBalance);
    }

    /**
     * @param location
     * @return
     */
    @Override
    public boolean boundsContain(Vec2D location) {
        // TODO use compareTo instead
        Vec2D mapSize = getMapSize();
        return location.x >= 0 && location.x < mapSize.x && location.y >= 0 && location.y < mapSize.y;
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Override
    public Speed getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(@NotNull Speed speed) {
        this.speed = speed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof World)) {
            return false;
        }
        World o = (World) obj;

        boolean equal = Objects.equals(players, o.players) && Objects.equals(clock, o.clock) && Objects.equals(speed, o.speed);
        equal = equal && Objects.equals(rules, o.rules) && Objects.equals(engines, o.engines) && Objects.equals(cities, o.cities);
        equal = equal && Objects.equals(cargos, o.cargos) && Objects.equals(trains, o.trains) && Objects.equals(stations, o.stations);
        equal = equal && Objects.equals(transactions, o.transactions);


        // Compare maps
        Vec2D mapSize = getMapSize();
        if (!mapSize.equals(o.getMapSize())) {
            return false;
        }
        for (int x = 0; x < mapSize.x; x++) {
            for (int y = 0; y < mapSize.y; y++) {
                Vec2D p = new Vec2D(x, y);
                if (!getTile(p).equals(o.getTile(p))) {
                    return false;
                }
            }
        }

        return equal;
    }

    /**
     * @param player
     * @return
     */
    @Override
    public Money getCurrentBalance(Player player) {
        int playerIndex = player.getId();
        return currentBalance.get(playerIndex);
    }

    @Override
    public Vec2D getMapSize() {
        return mapSize;
    }

    @Override
    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(players);
    }

    /**
     * @param i
     * @return
     */
    @Override
    public Player getPlayer(int i) {
        return players.get(i);
    }

    private int getMapIndex(Vec2D location) {
        if (location.x < 0 || location.x >= mapSize.x || location.y < 0 || location.y >= mapSize.y) {
            throw new IndexOutOfBoundsException();
        }
        return location.x * mapSize.y + location.y;
    }

    @Override
    public TerrainTile getTile(Vec2D location) {
        return map[getMapIndex(location)];
    }

    /**
     * @param player
     * @param i
     * @return
     */
    @Override
    public Transaction getTransaction(Player player, int i) {
        return transactions.get(player).get(i);
    }

    // TODO wrap in unmodifiable list
    @Override
    public Collection<Transaction> getTransactions(Player player) {
        return transactions.get(player);
    }

    @Override
    public int hashCode() {
        int result;
        result = players.size();

        return result;
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

    // TODO remove this, when undoing moves is eliminated
    /**
     * Removes and returns the last transaction added the the specified
     * player's bank account. This method is only here so that moves that add
     * transaction can be undone.
     */
    public Transaction removeLastTransaction(Player player) {
        int playerIndex = player.getId();

        List<Transaction> transactions = this.transactions.get(player);
        Transaction transactionRecord = transactions.remove(transactions.size()-1);

        Money oldBalance = currentBalance.get(playerIndex);
        Money newBalance = Money.subtract(oldBalance, transactionRecord.getAmount());
        currentBalance.set(playerIndex, newBalance);
        return transactionRecord;
    }

    /**
     * Replaces the tile at the specified position on the map with the specified
     * tile.
     */
    public void setTile(Vec2D location, TerrainTile tile) {
        map[getMapIndex(location)] = tile;
    }
}

/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.ModelConstants;
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.*;

// TODO for deserializing we might want to use playerId instead of Player as index in Maps
/**
 * Mutable implementation of {@link UnmodifiableWorld}, backed by Java
 * collections and a flat array of map tiles. It owns world definitions,
 * player-specific trains and stations, player finances, and simulation state.
 */
public class World implements UnmodifiableWorld {

    @Serial
    private static final long serialVersionUID = 3544393612684505393L;
    private Map<Player, List<Transaction>> transactions = new HashMap<>();
    private List<Money> currentBalance = new ArrayList<>();

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

    private transient Map<Integer, Integer> cargoIndexById;
    private transient List<Cargo> cargosByIndex;
    private transient Map<Integer, Integer> trackTypeIndexById;
    private transient List<TrackType> trackTypesByIndex;
    private transient Map<Integer, Integer> playerIndexById;

    // TODO this should also be set by the builder
    private int currentSentimentId = 2;

    // TODO need something to fill the Builder in loading scenario as well as going the other way (when writing out scenario)
    // TODO set trains?
    /**
     * Collects the initial world definitions and required settings before
     * constructing a {@link World}.
     */
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

        /**
         * Sets the engine definitions used by the world.
         *
         * @param engines sorted engine definitions
         * @return this builder
         */
        public Builder setEngines(@NotNull SortedSet<Engine> engines) {
            this.engines = Utils.verifyNotNull(engines);
            return this;
        }

        /**
         * Sets the cities included in the world.
         *
         * @param cities sorted city definitions
         * @return this builder
         */
        public Builder setCities(@NotNull SortedSet<City> cities) {
            this.cities = Utils.verifyNotNull(cities);
            return this;
        }

        /**
         * Sets the cargo definitions included in the world.
         *
         * @param cargos sorted cargo definitions
         * @return this builder
         */
        public Builder setCargos(@NotNull SortedSet<Cargo> cargos) {
            this.cargos = Utils.verifyNotNull(cargos);
            return this;
        }

        /**
         * Sets the terrain definitions included in the world.
         *
         * @param terrainTypes sorted terrain definitions
         * @return this builder
         */
        public Builder setTerrainTypes(@NotNull SortedSet<Terrain> terrainTypes) {
            this.terrainTypes = Utils.verifyNotNull(terrainTypes);
            return this;
        }

        /**
         * Sets the track type definitions included in the world.
         *
         * @param trackTypes sorted track type definitions
         * @return this builder
         */
        public Builder setTrackTypes(@NotNull SortedSet<TrackType> trackTypes) {
            this.trackTypes = Utils.verifyNotNull(trackTypes);
            return this;
        }

        /**
         * Sets map width and height in tiles.
         *
         * @param mapSize width and height, each of which must be positive and
         *        within the configured map limits
         * @return this builder
         */
        public Builder setMapSize(@NotNull Vec2D mapSize) {
            this.mapSize = new Vec2D(Utils.verifyNotNull(mapSize));
            return this;
        }

        /**
         * Sets the rules required to build the world.
         *
         * @param rules game rules
         * @return this builder
         */
        public Builder setRules(@NotNull Rules rules) {
            this.rules = Utils.verifyNotNull(rules);
            return this;
        }

        /**
         * Sets the economic sentiments available to the world.
         *
         * @param sentiments sorted sentiment definitions
         * @return this builder
         */
        public Builder setSentiments(@NotNull SortedSet<Sentiment> sentiments) {
            this.sentiments = Utils.verifyNotNull(sentiments);
            return this;
        }

        /**
         * Builds a world from the configured data.
         *
         * @return the initialized world
         * @throws IllegalStateException if rules or map size have not been set
         * @throws IllegalArgumentException if the map size is outside supported limits
         */
        public World build() {
            if (rules == null) {
                throw new IllegalStateException("Rules not specified");
            }
            if (mapSize == null) {
                throw new IllegalStateException("Map size not specified");
            }
            validateMapSize(mapSize);
            return new World(this);
        }
    }

    /**
     * Creates a world from builder configuration and initializes its map.
     *
     * @param builder configuration to copy
     * @throws NullPointerException if the builder or its map size is null
     * @throws IllegalArgumentException if the map dimensions exceed supported limits
     */
    public World(Builder builder) {
        Objects.requireNonNull(builder, "builder");
        validateMapSize(Objects.requireNonNull(builder.mapSize, "mapSize"));
        engines = new TreeSet<>(builder.engines);
        cities = new TreeSet<>(builder.cities);
        cargos = new TreeSet<>(builder.cargos);
        terrainTypes = new TreeSet<>(builder.terrainTypes);
        trackTypes = new TreeSet<>(builder.trackTypes);
        trains = new HashMap<>(builder.trains);
        stations = new HashMap<>(builder.stations);
        rules = builder.rules;
        sentiments = new TreeSet<>(builder.sentiments);

        clock = builder.clock;

        // setup map
        mapSize = new Vec2D(builder.mapSize);
        map = new TerrainTile[Math.toIntExact((long) mapSize.x() * mapSize.y())];

        // TODO what is a good default initializer here or should we stay with null?
        // TODO terrainTypeId 0 must also exist as terrain, how to make sure
        for (int i = 0; i < map.length; i++) {
            map[i] = new TerrainTile(0);
        }
        initializeIndexes();
    }

    /**
     * Validates positive map dimensions and the configured width, height, and
     * tile-count limits.
     *
     * @param mapSize dimensions to validate
     * @throws IllegalArgumentException if any dimension or the tile count is invalid
     */
    private static void validateMapSize(Vec2D mapSize) {
        if (mapSize.x() <= 0 || mapSize.y() <= 0
                || mapSize.x() > ModelConstants.MAX_MAP_DIMENSION
                || mapSize.y() > ModelConstants.MAX_MAP_DIMENSION) {
            throw new IllegalArgumentException("Map dimensions must be between 1 and "
                    + ModelConstants.MAX_MAP_DIMENSION + " tiles.");
        }
        long tileCount = (long) mapSize.x() * mapSize.y();
        if (tileCount > ModelConstants.MAX_MAP_TILE_COUNT) {
            throw new IllegalArgumentException("Map cannot contain more than "
                    + ModelConstants.MAX_MAP_TILE_COUNT + " tiles.");
        }
    }

    /** Rebuilds the transient stable-ID lookup tables from the world lists. */
    private void initializeIndexes() {
        cargosByIndex = new ArrayList<>(cargos);
        cargoIndexById = createIndex(cargosByIndex);
        trackTypesByIndex = new ArrayList<>(trackTypes);
        trackTypeIndexById = createIndex(trackTypesByIndex);
        playerIndexById = createIndex(players);
    }

    /**
     * Builds an ID-to-position map for an ordered list.
     *
     * @param values ordered identifiable objects to index
     * @param <T> indexed object type
     * @return map from stable IDs to zero-based list positions
     * @throws IllegalArgumentException if identifiers are duplicated
     */
    private static <T extends Identifiable> Map<Integer, Integer> createIndex(List<T> values) {
        Map<Integer, Integer> indexById = new HashMap<>();
        for (int index = 0; index < values.size(); index++) {
            int id = values.get(index).getId();
            if (indexById.put(id, index) != null) {
                throw new IllegalArgumentException("Duplicate model ID: " + id);
            }
        }
        return indexById;
    }

    /**
     * Looks up an identifier in an index map.
     *
     * @param id stable identifier to find
     * @param indexById identifier-to-position map
     * @param type name used in the unknown-ID error
     * @return the identifier's zero-based position
     * @throws IllegalArgumentException if the identifier is absent
     */
    private static int getIndex(int id, Map<Integer, Integer> indexById, String type) {
        Integer index = indexById.get(id);
        if (index == null) {
            throw new IllegalArgumentException("Unknown " + type + " ID: " + id);
        }
        return index;
    }

    /**
     * Restores serialized world state and reconstructs transient lookup indexes.
     *
     * @param input serialized data source
     * @throws IOException if reading the serialized object fails
     * @throws ClassNotFoundException if a serialized class cannot be resolved
     */
    @Serial
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        initializeIndexes();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Engine> getEngines() {
        return Collections.unmodifiableSortedSet(engines);
    }

    /** {@inheritDoc} */
    @Override
    public Engine getEngine(int id) {
        return get(id, engines);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<City> getCities() {
        return Collections.unmodifiableSortedSet(cities);
    }

    /** {@inheritDoc} */
    @Override
    public City getCity(int id) {
        return get(id, cities);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Cargo> getCargos() {
        return Collections.unmodifiableList(cargosByIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Cargo getCargo(int id) {
        return get(id, cargos);
    }

    /** {@inheritDoc} */
    @Override
    public int getCargoIndex(int cargoId) {
        return getIndex(cargoId, cargoIndexById, "cargo");
    }

    /** {@inheritDoc} */
    @Override
    public Cargo getCargoAtIndex(int index) {
        return cargosByIndex.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Terrain> getTerrains() {
        return Collections.unmodifiableSortedSet(terrainTypes);
    }

    /** {@inheritDoc} */
    @Override
    public Terrain getTerrain(int id) {
        return get(id, terrainTypes);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<TrackType> getTrackTypes() {
        return Collections.unmodifiableList(trackTypesByIndex);
    }

    /** {@inheritDoc} */
    @Override
    public TrackType getTrackType(int id) {
        return get(id, trackTypes);
    }

    /** {@inheritDoc} */
    @Override
    public int getTrackTypeIndex(int trackTypeId) {
        return getIndex(trackTypeId, trackTypeIndexById, "track type");
    }

    /** {@inheritDoc} */
    @Override
    public TrackType getTrackTypeAtIndex(int index) {
        return trackTypesByIndex.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Train> getTrains(Player player) {
        return Collections.unmodifiableList(trains.get(player));
    }

    /** {@inheritDoc} */
    @Override
    public Train getTrain(Player player, int id) {
        return get(id, trains.get(player));
    }

    /** {@inheritDoc} */
    @Override
    public int getTrainIndex(Player player, int trainId) {
        List<Train> playerTrains = trains.get(player);
        for (int index = 0; index < playerTrains.size(); index++) {
            if (playerTrains.get(index).getId() == trainId) {
                return index;
            }
        }
        throw new IllegalArgumentException("Unknown train ID " + trainId + " for player " + player.getId());
    }

    /**
     * Adds a train to a player's list.
     *
     * @param player owner of the train
     * @param train train to add
     * @throws IllegalArgumentException if the player already has a train with
     *         the same identifier
     */
    public void addTrain(Player player, Train train) {
        if (Utils.containsId(train.getId(), trains.get(player))) {
            throw new IllegalArgumentException("Train with id already existing. Cannot add.");
        }
        trains.get(player).add(train);
    }

    /**
     * Removes the identified train from a player's list.
     *
     * @param player owner of the train
     * @param id identifier of the train to remove
     * @throws IllegalArgumentException if no matching train exists
     */
    public void removeTrain(Player player, int id) {
        trains.get(player).remove(get(id, trains.get(player)));
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Station> getStations(Player player) {
        return Collections.unmodifiableSortedSet(stations.get(player));
    }

    /** {@inheritDoc} */
    @Override
    public Station getStation(Player player, int id) {
        return get(id, stations.get(player));
    }

    /**
     * Adds a station to a player's station set.
     *
     * @param player owner of the station
     * @param station station to add
     * @throws IllegalArgumentException if the player already has a station
     *         with the same identifier
     */
    public void addStation(Player player, Station station) {
        if (Utils.containsId(station.getId(), stations.get(player))) {
            throw new IllegalArgumentException("Station with id already existing. Cannot add.");
        }
        stations.get(player).add(station);
    }

    /**
     * Removes a station by identifier from a player's station set.
     *
     * @param player owner of the station
     * @param id identifier of the station to remove
     * @throws IllegalArgumentException if no matching station exists
     */
    public void removeStation(Player player, int id) {
        stations.get(player).remove(get(id, stations.get(player)));
    }

    /**
     * Finds an object by stable identifier in a collection.
     *
     * @param id identifier to find
     * @param c collection to search
     * @param <E> identifiable object type
     * @return the matching object
     * @throws IllegalArgumentException if no matching object is present
     */
    private <E extends Identifiable> E get(final int id, @NotNull final Collection<E> c) {
        for (E e: c) {
            if (e.getId() == id) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("Element with Id=%d not existing in collection.", id));
    }

    /** Returns the sentiment currently selected by this world. */
    @Override
    public Sentiment getSentiment() {
        return get(currentSentimentId, sentiments);
    }

    /** {@inheritDoc} */
    @Override
    public Rules getRules() {
        return rules;
    }

    // TODO undo this later, just for not breaking the tests
    /**
     * Replaces the world's track and gameplay rules.
     *
     * @param rules rules to use
     */
    public void setRules(@NotNull Rules rules) {
        this.rules = rules;
    }

    /**
     * Records an activity on a train and sets its start time to the current
     * world tick.
     *
     * @param player owner of the train
     * @param trainId identifier of the train
     * @param activity activity to attach
     */
    public void addActivity(Player player, int trainId, Activity activity) {
        Train train = getTrain(player, trainId);
        activity.setStartTime(clock.getCurrentTime().ticks());
        train.addActivity(activity);
    }

    /**
     * Adds a player and initializes that player's trains, stations, account,
     * and transaction list.
     *
     * @param player player to add
     * @return the new player's zero-based position
     * @throws IllegalArgumentException if the player identifier already exists
     * @throws IllegalStateException if player-specific state already exists
     *         for this player object
     */
    public int addPlayer(Player player) {
        Utils.verifyNotNull(player);
        if (playerIndexById.containsKey(player.getId())) {
            throw new IllegalArgumentException("Player with ID already exists: " + player.getId());
        }

        if (trains.containsKey(player) || stations.containsKey(player) || transactions.containsKey(player)) {
            throw new IllegalStateException("World already contains state for player " + player.getId());
        }
        int index = players.size();
        players.add(player);
        playerIndexById.put(player.getId(), index);
        transactions.put(player, new ArrayList<>());
        currentBalance.add(Money.ZERO);
        trains.put(player, new ArrayList<>());
        stations.put(player, new TreeSet<>());

        return index;
    }

    // TODO check that transaction time is not in the future
    /**
     * Adds a transaction to the player's history and updates the current
     * balance by its amount. History preserves append order and does not
     * require transaction timestamps to be chronological.
     *
     * @param player account owner
     * @param transaction transaction to record
     * @throws NullPointerException if {@code transaction} is null
     * @throws IllegalArgumentException if the player is not in this world
     */
    public void addTransaction(Player player, Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction");
        int playerIndex = getPlayerIndex(player.getId());
        List<Transaction> playerTransactions = transactions.get(player);
        if (playerTransactions == null) {
            throw new IllegalArgumentException("Player is not part of this world: " + player.getId());
        }

        // adjust balance
        Money newBalance = Money.add(transaction.getAmount(), currentBalance.get(playerIndex));
        playerTransactions.add(transaction);
        currentBalance.set(playerIndex, newBalance);
    }

    /**
     * Tests whether a tile coordinate lies inside the map.
     *
     * @param location coordinate to test
     * @return {@code true} if the coordinate is within the map boundaries
     */
    @Override
    public boolean boundsContain(Vec2D location) {
        // TODO use compareTo instead
        Vec2D mapSize = getMapSize();
        return location.x() >= 0 && location.x() < mapSize.x() && location.y() >= 0 && location.y() < mapSize.y();
    }

    /** {@inheritDoc} */
    @Override
    public Clock getClock() {
        return clock;
    }

    /** {@inheritDoc} */
    @Override
    public Speed getSpeed() {
        return speed;
    }

    /** {@inheritDoc} */
    @Override
    public void setSpeed(@NotNull Speed speed) {
        this.speed = speed;
    }

    /**
     * Compares the complete game state, including map tiles and player data.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is a world with equal state
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof World o)) {
            return false;
        }

        return Objects.equals(players, o.players)
                && Objects.equals(clock, o.clock)
                && speed == o.speed
                && Objects.equals(rules, o.rules)
                && Objects.equals(engines, o.engines)
                && Objects.equals(cities, o.cities)
                && Objects.equals(cargos, o.cargos)
                && Objects.equals(terrainTypes, o.terrainTypes)
                && Objects.equals(trackTypes, o.trackTypes)
                && Objects.equals(sentiments, o.sentiments)
                && currentSentimentId == o.currentSentimentId
                && Objects.equals(trains, o.trains)
                && Objects.equals(stations, o.stations)
                && Objects.equals(transactions, o.transactions)
                && Objects.equals(currentBalance, o.currentBalance)
                && Objects.equals(mapSize, o.mapSize)
                && Arrays.equals(map, o.map);
    }

    /**
     * Returns the current account balance for a player.
     *
     * @param player account owner
     * @return player's current balance
     */
    @Override
    public Money getCurrentBalance(Player player) {
        return currentBalance.get(getPlayerIndex(player.getId()));
    }

    /** {@inheritDoc} */
    @Override
    public Vec2D getMapSize() {
        return mapSize;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(players);
    }

    /**
     * Returns the player at a zero-based list position.
     *
     * @param i player position
     * @return player at that position
     */
    @Override
    public Player getPlayer(int i) {
        return players.get(i);
    }

    /**
     * Converts a valid map coordinate to the backing array index.
     *
     * @param location map coordinate
     * @return corresponding map array index
     * @throws IndexOutOfBoundsException if the coordinate is outside the map
     */
    private int getMapIndex(Vec2D location) {
        if (location.x() < 0 || location.x() >= mapSize.x() || location.y() < 0 || location.y() >= mapSize.y()) {
            throw new IndexOutOfBoundsException();
        }
        return location.x() * mapSize.y() + location.y();
    }

    /** {@inheritDoc} */
    @Override
    public TerrainTile getTile(Vec2D location) {
        return map[getMapIndex(location)];
    }

    /**
     * Returns a player's transaction at a zero-based history position.
     *
     * @param player transaction owner
     * @param i transaction position
     * @return the transaction at that position
     */
    @Override
    public Transaction getTransaction(Player player, int i) {
        return transactions.get(player).get(i);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Transaction> getTransactions(Player player) {
        return Collections.unmodifiableList(transactions.get(player));
    }

    /**
     * Returns a hash code derived from the complete world state.
     *
     * @return hash code consistent with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return 31 * Objects.hash(players, clock, speed, rules, engines, cities, cargos, terrainTypes,
                trackTypes, sentiments, currentSentimentId, trains, stations, transactions, currentBalance, mapSize)
                + Arrays.hashCode(map);
    }

    /**
     * Placeholder for removing the most recently added player; removal is not
     * currently supported.
     *
     * @return the removed player
     * @throws UnsupportedOperationException always, because this operation is
     *         not implemented
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
     * Removes and returns the last transaction in a player's history, restoring
     * the previous balance. This supports undoing transaction moves.
     *
     * @param player account owner
     * @return removed transaction
     * @throws IllegalStateException if the player has no transactions
     */
    public Transaction removeLastTransaction(Player player) {
        int playerIndex = getPlayerIndex(player.getId());

        List<Transaction> transactions = this.transactions.get(player);
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalStateException("Player has no transactions to remove.");
        }
        Transaction transactionRecord = transactions.removeLast();

        Money oldBalance = currentBalance.get(playerIndex);
        Money newBalance = Money.subtract(oldBalance, transactionRecord.getAmount());
        currentBalance.set(playerIndex, newBalance);
        return transactionRecord;
    }

    /**
     * Returns the player's zero-based position in the world.
     *
     * @param playerId stable player identifier
     * @return player position
     * @throws IllegalArgumentException if the player identifier is unknown
     */
    @Override
    public int getPlayerIndex(int playerId) {
        return getIndex(playerId, playerIndexById, "player");
    }

    /**
     * Replaces the tile at the specified position on the map.
     *
     * @param location map coordinate of the tile
     * @param tile replacement tile
     * @throws IndexOutOfBoundsException if the coordinate is outside the map
     */
    public void setTile(Vec2D location, TerrainTile tile) {
        map[getMapIndex(location)] = tile;
    }
}

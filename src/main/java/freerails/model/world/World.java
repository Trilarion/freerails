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
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.building.EngineShop;
import freerails.model.operations.cargo.Cargo;
import freerails.model.economy.transaction.Transaction;
import freerails.model.game.*;
import freerails.model.simulation.*;
import freerails.model.operations.station.Station;
import freerails.model.terrain.city.City;
import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.model.operations.train.Engine;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.TrainTemplate;
import freerails.util.*;
import freerails.model.economy.Money;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.*;

/**
 * Mutable implementation of {@link UnmodifiableWorld}, backed by Java
 * collections and a flat array of map tiles. It owns world definitions,
 * player-specific trains, stations, and engine shops, player finances, and
 * simulation state.
 */
public class World implements UnmodifiableWorld {

    @Serial
    private static final long serialVersionUID = 3544393612684505393L;
    private Map<Integer, List<Transaction>> transactions = new HashMap<>();
    private Map<Integer, Money> currentBalance = new HashMap<>();

    // single instances
    private final Clock clock;
    private boolean cityGrowthEnabled = true;
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
    private final Map<Integer, List<Train>> trains; // a list of trains by player ID
    private final Map<Integer, SortedSet<Station>> stations; // list of stations by player ID
    private Map<Integer, SortedSet<EngineShop>> engineShops;

    private transient Map<Integer, Integer> cargoIndexById;
    private transient List<Cargo> cargosByIndex;
    private transient Map<Integer, Integer> trackTypeIndexById;
    private transient List<TrackType> trackTypesByIndex;
    private transient Map<Integer, Integer> playerIndexById;
    private transient Map<Integer, Engine> engineById;
    private transient Map<Integer, City> cityById;
    private transient Map<Integer, Cargo> cargoById;
    private transient Map<Integer, Terrain> terrainById;
    private transient Map<Integer, TrackType> trackTypeById;
    private transient Map<Integer, Sentiment> sentimentById;
    private transient Map<Integer, Map<Integer, Integer>> trainIndexByIdByPlayer;
    private transient Map<Integer, Map<Integer, Station>> stationByIdByPlayer;
    private transient Map<Integer, Map<Integer, EngineShop>> engineShopByIdByPlayer;

    private int currentSentimentId = 2;

    // Player-specific assets are initialized by game setup, not scenario definitions.
    /**
     * Collects the initial world definitions and required settings before
     * constructing a {@link World}. Collection setters make defensive copies
     * immediately, so later changes to a caller's set do not alter this
     * builder's configuration.
     */
    public static class Builder {

        private SortedSet<Engine> engines = new TreeSet<>();
        private SortedSet<City> cities = new TreeSet<>();
        private SortedSet<Cargo> cargos = new TreeSet<>();
        private SortedSet<Terrain> terrainTypes = new TreeSet<>();
        private SortedSet<TrackType> trackTypes = new TreeSet<>();
        private SortedSet<Sentiment> sentiments = new TreeSet<>();
        private Map<Integer, List<Train>> trains = new HashMap<>();
        private Map<Integer, SortedSet<Station>> stations = new HashMap<>();
        private Map<Integer, SortedSet<EngineShop>> engineShops = new HashMap<>();
        private Rules rules = null;
        private Vec2D mapSize = null;
        private Clock clock = new Clock(1840, 1200, new Time(0));
        private int currentSentimentId = 2;
        private boolean cityGrowthEnabled = true;

        /**
         * Sets the engine definitions used by the world.
         *
         * @param engines sorted engine definitions
         * @return this builder
         */
        public Builder setEngines(@NotNull SortedSet<Engine> engines) {
            this.engines = new TreeSet<>(Utils.verifyNotNull(engines));
            return this;
        }

        /**
         * Sets the cities included in the world.
         *
         * @param cities sorted city definitions
         * @return this builder
         */
        public Builder setCities(@NotNull SortedSet<City> cities) {
            this.cities = new TreeSet<>(Utils.verifyNotNull(cities));
            return this;
        }

        /**
         * Sets the cargo definitions included in the world.
         *
         * @param cargos sorted cargo definitions
         * @return this builder
         */
        public Builder setCargos(@NotNull SortedSet<Cargo> cargos) {
            this.cargos = new TreeSet<>(Utils.verifyNotNull(cargos));
            return this;
        }

        /**
         * Sets the terrain definitions included in the world.
         *
         * @param terrainTypes sorted terrain definitions
         * @return this builder
         */
        public Builder setTerrainTypes(@NotNull SortedSet<Terrain> terrainTypes) {
            this.terrainTypes = new TreeSet<>(Utils.verifyNotNull(terrainTypes));
            return this;
        }

        /**
         * Sets the track type definitions included in the world.
         *
         * @param trackTypes sorted track type definitions
         * @return this builder
         */
        public Builder setTrackTypes(@NotNull SortedSet<TrackType> trackTypes) {
            this.trackTypes = new TreeSet<>(Utils.verifyNotNull(trackTypes));
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
            this.sentiments = new TreeSet<>(Utils.verifyNotNull(sentiments));
            return this;
        }

        /**
         * Sets the clock configuration used by the world.
         *
         * @param clock simulation clock
         * @return this builder
         */
        public Builder setClock(@NotNull Clock clock) {
            Clock value = Utils.verifyNotNull(clock);
            this.clock = new Clock(value.getStartYear(), value.getTicksPerYear(), value.getCurrentTime());
            return this;
        }

        /**
         * Sets the identifier of the currently selected economic sentiment.
         *
         * @param currentSentimentId sentiment identifier
         * @return this builder
         */
        public Builder setCurrentSentimentId(int currentSentimentId) {
            this.currentSentimentId = currentSentimentId;
            return this;
        }

        /**
         * Sets whether cities expand at the end of each year.
         *
         * @param cityGrowthEnabled whether yearly city expansion is enabled
         * @return this builder
         */
        public Builder setCityGrowthEnabled(boolean cityGrowthEnabled) {
            this.cityGrowthEnabled = cityGrowthEnabled;
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
        engineShops = new HashMap<>(builder.engineShops);
        rules = builder.rules;
        sentiments = new TreeSet<>(builder.sentiments);

        clock = new Clock(builder.clock.getStartYear(), builder.clock.getTicksPerYear(),
                builder.clock.getCurrentTime());
        currentSentimentId = builder.currentSentimentId;
        cityGrowthEnabled = builder.cityGrowthEnabled;

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
        engineById = createIdLookup(engines);
        cityById = createIdLookup(cities);
        cargoById = createIdLookup(cargos);
        terrainById = createIdLookup(terrainTypes);
        trackTypeById = createIdLookup(trackTypes);
        sentimentById = createIdLookup(sentiments);
        cargosByIndex = new ArrayList<>(cargos);
        cargoIndexById = createIndex(cargosByIndex);
        trackTypesByIndex = new ArrayList<>(trackTypes);
        trackTypeIndexById = createIndex(trackTypesByIndex);
        playerIndexById = createIndex(players);

        trainIndexByIdByPlayer = new HashMap<>();
        trains.forEach((playerId, playerTrains) ->
                trainIndexByIdByPlayer.put(playerId, createIndex(playerTrains)));
        stationByIdByPlayer = new HashMap<>();
        stations.forEach((playerId, playerStations) ->
                stationByIdByPlayer.put(playerId, createIdLookup(playerStations)));
        engineShops = engineShops == null ? new HashMap<>() : engineShops;
        engineShopByIdByPlayer = new HashMap<>();
        players.forEach(player -> {
            int playerId = player.getId();
            SortedSet<EngineShop> shops = engineShops.computeIfAbsent(playerId, ignored -> new TreeSet<>());
            engineShopByIdByPlayer.put(playerId, createIdLookup(shops));
        });
    }

    /**
     * Builds an ID-to-object lookup for an identifiable collection.
     *
     * @param values identifiable objects to index
     * @param <T> indexed object type
     * @return map from stable IDs to their objects
     * @throws IllegalArgumentException if identifiers are duplicated
     */
    private static <T extends Identifiable> Map<Integer, T> createIdLookup(Collection<T> values) {
        Map<Integer, T> valueById = new HashMap<>();
        for (T value : values) {
            if (valueById.put(value.getId(), value) != null) {
                throw new IllegalArgumentException("Duplicate model ID: " + value.getId());
            }
        }
        return valueById;
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
     * Looks up an object by its stable identifier.
     *
     * @param id stable identifier to find
     * @param valuesById identifier-to-object lookup
     * @param <T> indexed object type
     * @return the matching object
     * @throws IllegalArgumentException if the identifier is absent
     */
    private static <T> T get(int id, Map<Integer, T> valuesById) {
        T value = valuesById.get(id);
        if (value == null) {
            throw new IllegalArgumentException(
                    String.format("Element with Id=%d not existing in collection.", id));
        }
        return value;
    }

    /**
     * Restores serialized world state, migrates legacy station production, and
     * reconstructs transient lookup indexes.
     *
     * @param input serialized data source
     * @throws IOException if reading the serialized object fails
     * @throws ClassNotFoundException if a serialized class cannot be resolved
     */
    @Serial
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        migrateLegacyEngineShopProduction();
        initializeIndexes();
    }

    private void migrateLegacyEngineShopProduction() {
        if (engineShops == null) {
            engineShops = new HashMap<>();
        }
        for (Player player : players) {
            SortedSet<EngineShop> shops = engineShops.computeIfAbsent(player.getId(), ignored -> new TreeSet<>());
            int nextId = shops.stream().mapToInt(EngineShop::getId).max().orElse(-1) + 1;
            for (Station station : stations.getOrDefault(player.getId(), new TreeSet<>())) {
                List<TrainTemplate> legacyProduction = station.takeLegacyProductionForMigration();
                if (legacyProduction.isEmpty()) {
                    continue;
                }

                EngineShop shop = shops.stream()
                        .filter(existing -> existing.getLocation().equals(station.getLocation()))
                        .findFirst()
                        .orElse(null);
                if (shop == null) {
                    shop = new EngineShop(nextId++, station.getLocation());
                    shops.add(shop);
                }
                List<TrainTemplate> production = new ArrayList<>(shop.getProduction());
                production.addAll(legacyProduction);
                shop.setProduction(production);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Engine> getEngines() {
        return Collections.unmodifiableSortedSet(engines);
    }

    /** {@inheritDoc} */
    @Override
    public Engine getEngine(int id) {
        return get(id, engineById);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<City> getCities() {
        return Collections.unmodifiableSortedSet(cities);
    }

    /** {@inheritDoc} */
    @Override
    public City getCity(int id) {
        return get(id, cityById);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Cargo> getCargos() {
        return Collections.unmodifiableList(cargosByIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Cargo getCargo(int id) {
        return get(id, cargoById);
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
        return get(id, terrainById);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<TrackType> getTrackTypes() {
        return Collections.unmodifiableList(trackTypesByIndex);
    }

    /** {@inheritDoc} */
    @Override
    public TrackType getTrackType(int id) {
        return get(id, trackTypeById);
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
        return Collections.unmodifiableList(trains.get(getPlayerId(player)));
    }

    /** {@inheritDoc} */
    @Override
    public Train getTrain(Player player, int id) {
        int playerId = getPlayerId(player);
        return trains.get(playerId).get(getTrainIndex(playerId, id));
    }

    /** {@inheritDoc} */
    @Override
    public int getTrainIndex(Player player, int trainId) {
        int playerId = getPlayerId(player);
        return getTrainIndex(playerId, trainId);
    }

    private int getTrainIndex(int playerId, int trainId) {
        Integer index = trainIndexByIdByPlayer.get(playerId).get(trainId);
        if (index == null) {
            throw new IllegalArgumentException("Unknown train ID " + trainId + " for player " + playerId);
        }
        return index;
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
        int playerId = getPlayerId(player);
        List<Train> playerTrains = trains.get(playerId);
        Map<Integer, Integer> trainIndexes = trainIndexByIdByPlayer.get(playerId);
        if (trainIndexes.containsKey(train.getId())) {
            throw new IllegalArgumentException("Train with id already existing. Cannot add.");
        }
        playerTrains.add(train);
        trainIndexes.put(train.getId(), playerTrains.size() - 1);
    }

    /**
     * Removes the identified train from a player's list.
     *
     * @param player owner of the train
     * @param id identifier of the train to remove
     * @throws IllegalArgumentException if no matching train exists
     */
    public void removeTrain(Player player, int id) {
        int playerId = getPlayerId(player);
        List<Train> playerTrains = trains.get(playerId);
        Map<Integer, Integer> trainIndexes = trainIndexByIdByPlayer.get(playerId);
        int index = getTrainIndex(playerId, id);
        playerTrains.remove(index);
        trainIndexes.remove(id);
        for (int i = index; i < playerTrains.size(); i++) {
            trainIndexes.put(playerTrains.get(i).getId(), i);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Station> getStations(Player player) {
        return Collections.unmodifiableSortedSet(stations.get(getPlayerId(player)));
    }

    /** {@inheritDoc} */
    @Override
    public Station getStation(Player player, int id) {
        return get(id, stationByIdByPlayer.get(getPlayerId(player)));
    }

    /** {@inheritDoc} */
    @Override
    public Collection<EngineShop> getEngineShops(Player player) {
        return Collections.unmodifiableSortedSet(engineShops.get(getPlayerId(player)));
    }

    /** {@inheritDoc} */
    @Override
    public EngineShop getEngineShop(Player player, int id) {
        return get(id, engineShopByIdByPlayer.get(getPlayerId(player)));
    }

    /**
     * Adds an engine shop to a player's facilities.
     *
     * @param player owner of the shop
     * @param engineShop shop to add
     * @throws IllegalArgumentException if the player already has a shop with
     *         the same identifier
     */
    public void addEngineShop(Player player, EngineShop engineShop) {
        int playerId = getPlayerId(player);
        SortedSet<EngineShop> playerShops = engineShops.get(playerId);
        Map<Integer, EngineShop> shopsById = engineShopByIdByPlayer.get(playerId);
        if (shopsById.containsKey(engineShop.getId())) {
            throw new IllegalArgumentException("Engine shop with id already existing. Cannot add.");
        }
        if (!playerShops.add(engineShop)) {
            throw new IllegalArgumentException("Engine shop cannot be added to the player's collection.");
        }
        shopsById.put(engineShop.getId(), engineShop);
    }

    /**
     * Removes an engine shop by identifier.
     *
     * @param player owner of the shop
     * @param id identifier of the shop to remove
     */
    public void removeEngineShop(Player player, int id) {
        int playerId = getPlayerId(player);
        SortedSet<EngineShop> playerShops = engineShops.get(playerId);
        Map<Integer, EngineShop> shopsById = engineShopByIdByPlayer.get(playerId);
        EngineShop engineShop = get(id, shopsById);
        playerShops.remove(engineShop);
        shopsById.remove(id);
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
        int playerId = getPlayerId(player);
        SortedSet<Station> playerStations = stations.get(playerId);
        Map<Integer, Station> stationsById = stationByIdByPlayer.get(playerId);
        if (stationsById.containsKey(station.getId())) {
            throw new IllegalArgumentException("Station with id already existing. Cannot add.");
        }
        if (!playerStations.add(station)) {
            throw new IllegalArgumentException("Station cannot be added to the player's collection.");
        }
        stationsById.put(station.getId(), station);
    }

    /**
     * Removes a station by identifier from a player's station set.
     *
     * @param player owner of the station
     * @param id identifier of the station to remove
     * @throws IllegalArgumentException if no matching station exists
     */
    public void removeStation(Player player, int id) {
        int playerId = getPlayerId(player);
        SortedSet<Station> playerStations = stations.get(playerId);
        Map<Integer, Station> stationsById = stationByIdByPlayer.get(playerId);
        Station station = get(id, stationsById);
        engineShops.get(playerId).stream()
                .filter(engineShop -> engineShop.getLocation().equals(station.getLocation()))
                .map(EngineShop::getId)
                .toList()
                .forEach(engineShopId -> removeEngineShop(player, engineShopId));
        playerStations.remove(station);
        stationsById.remove(id);
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
    /** Returns the sentiment currently selected by this world. */
    @Override
    public Sentiment getSentiment() {
        return get(currentSentimentId, sentimentById);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Sentiment> getSentiments() {
        return Collections.unmodifiableSortedSet(sentiments);
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
     * Records an activity on a train with a start time of at least the current
     * world tick.
     *
     * @param player owner of the train
     * @param trainId identifier of the train
     * @param activity activity to attach
     */
    public void addActivity(Player player, int trainId, TrainMotion activity) {
        Train train = getTrain(player, trainId);
        train.getRuntimeState().addActivity(activity, clock.getCurrentTime().ticks());
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

        if (trains.containsKey(player.getId())
                || stations.containsKey(player.getId())
                || transactions.containsKey(player.getId())) {
            throw new IllegalStateException("World already contains state for player " + player.getId());
        }
        int index = players.size();
        players.add(player);
        playerIndexById.put(player.getId(), index);
        transactions.put(player.getId(), new ArrayList<>());
        currentBalance.put(player.getId(), Money.ZERO);
        trains.put(player.getId(), new ArrayList<>());
        stations.put(player.getId(), new TreeSet<>());
        engineShops.put(player.getId(), new TreeSet<>());
        trainIndexByIdByPlayer.put(player.getId(), new HashMap<>());
        stationByIdByPlayer.put(player.getId(), new HashMap<>());
        engineShopByIdByPlayer.put(player.getId(), new HashMap<>());

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
        int playerId = getPlayerId(player);
        List<Transaction> playerTransactions = transactions.get(playerId);

        // adjust balance
        Money newBalance = Money.add(transaction.getAmount(), currentBalance.get(playerId));
        playerTransactions.add(transaction);
        currentBalance.put(playerId, newBalance);
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
    public boolean isCityGrowthEnabled() {
        return cityGrowthEnabled;
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
                && cityGrowthEnabled == o.cityGrowthEnabled
                && Objects.equals(trains, o.trains)
                && Objects.equals(stations, o.stations)
                && engineShopsEqual(engineShops, o.engineShops)
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
        return currentBalance.get(getPlayerId(player));
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
        return transactions.get(getPlayerId(player)).get(i);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Transaction> getTransactions(Player player) {
        return Collections.unmodifiableList(transactions.get(getPlayerId(player)));
    }

    /**
     * Returns a hash code derived from the complete world state.
     *
     * @return hash code consistent with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return 31 * Objects.hash(players, clock, speed, rules, engines, cities, cargos, terrainTypes,
                trackTypes, sentiments, currentSentimentId, cityGrowthEnabled, trains, stations, transactions,
                engineShopsHashCode(engineShops), currentBalance, mapSize)
                + Arrays.hashCode(map);
    }

    private static boolean engineShopsEqual(Map<Integer, SortedSet<EngineShop>> first,
                                            Map<Integer, SortedSet<EngineShop>> second) {
        if (!first.keySet().equals(second.keySet())) {
            return false;
        }
        for (Integer playerId : first.keySet()) {
            SortedSet<EngineShop> firstShops = first.get(playerId);
            SortedSet<EngineShop> secondShops = second.get(playerId);
            if (firstShops.size() != secondShops.size()) {
                return false;
            }
            for (EngineShop firstShop : firstShops) {
                EngineShop secondShop = secondShops.stream()
                        .filter(shop -> shop.getId() == firstShop.getId())
                        .findFirst()
                        .orElse(null);
                if (secondShop == null
                        || !Objects.equals(firstShop.getLocation(), secondShop.getLocation())
                        || !Objects.equals(firstShop.getProduction(), secondShop.getProduction())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int engineShopsHashCode(Map<Integer, SortedSet<EngineShop>> shopsByPlayer) {
        int result = 0;
        for (Map.Entry<Integer, SortedSet<EngineShop>> entry : shopsByPlayer.entrySet()) {
            int shopsHash = 0;
            for (EngineShop shop : entry.getValue()) {
                shopsHash += Objects.hash(shop.getId(), shop.getLocation(), shop.getProduction());
            }
            result += Objects.hash(entry.getKey(), shopsHash);
        }
        return result;
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
        int playerId = getPlayerId(player);
        List<Transaction> transactions = this.transactions.get(playerId);
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalStateException("Player has no transactions to remove.");
        }
        Transaction transactionRecord = transactions.removeLast();

        Money oldBalance = currentBalance.get(playerId);
        Money newBalance = Money.subtract(oldBalance, transactionRecord.getAmount());
        currentBalance.put(playerId, newBalance);
        return transactionRecord;
    }

    /**
     * Validates a player reference and returns its stable identifier.
     *
     * @param player player to validate
     * @return the player's stable identifier
     * @throws IllegalArgumentException if the player identifier is unknown
     */
    private int getPlayerId(Player player) {
        int playerId = Objects.requireNonNull(player, "player").getId();
        getPlayerIndex(playerId);
        return playerId;
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

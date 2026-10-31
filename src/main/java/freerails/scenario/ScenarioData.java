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

package freerails.scenario;

import freerails.model.operations.cargo.Cargo;
import freerails.model.simulation.Clock;
import freerails.model.game.Rules;
import freerails.model.game.Sentiment;
import freerails.model.simulation.Time;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.city.City;
import freerails.model.track.TrackType;
import freerails.model.operations.train.Engine;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.util.Array2D;
import freerails.util.Vec2D;

import java.util.*;

/**
 * Scenario configuration, separate from the mutable game world. The map
 * contains terrain type IDs; generated track and player state are not part of
 * scenario configuration.
 */
public final class ScenarioData {

    private static final Clock DEFAULT_CLOCK = new Clock(1840, 1200, new Time(0));
    private static final int DEFAULT_SENTIMENT_ID = 2;

    private final SortedSet<Engine> engines;
    private final SortedSet<City> cities;
    private final SortedSet<Cargo> cargos;
    private final SortedSet<Terrain> terrainTypes;
    private final SortedSet<TrackType> trackTypes;
    private final SortedSet<Sentiment> sentiments;
    private final Rules rules;
    private final Array2D terrainMap;
    private final Clock clock;
    private final int currentSentimentId;
    private final boolean cityGrowthEnabled;

    /**
     * Creates a scenario data from the supplied values.
     *
     * @param engines the engines
     * @param cities the cities
     * @param cargos the cargos
     * @param terrainTypes the terrain types
     * @param trackTypes the track types
     * @param sentiments the sentiments
     * @param rules the rules
     * @param terrainMap the terrain map
     * @param clock the clock
     * @param currentSentimentId the identifier to use
     */
    public ScenarioData(SortedSet<Engine> engines, SortedSet<City> cities, SortedSet<Cargo> cargos,
                        SortedSet<Terrain> terrainTypes, SortedSet<TrackType> trackTypes,
                        SortedSet<Sentiment> sentiments, Rules rules, Array2D terrainMap,
                        Clock clock, int currentSentimentId) {
        this(engines, cities, cargos, terrainTypes, trackTypes, sentiments, rules, terrainMap,
                clock, currentSentimentId, true);
    }

    /**
     * Creates a scenario data from the supplied values.
     *
     * @param engines the engines
     * @param cities the cities
     * @param cargos the cargos
     * @param terrainTypes the terrain types
     * @param trackTypes the track types
     * @param sentiments the sentiments
     * @param rules the rules
     * @param terrainMap the terrain map
     * @param clock the clock
     * @param currentSentimentId the identifier to use
     * @param cityGrowthEnabled the city growth enabled
     */
    public ScenarioData(SortedSet<Engine> engines, SortedSet<City> cities, SortedSet<Cargo> cargos,
                        SortedSet<Terrain> terrainTypes, SortedSet<TrackType> trackTypes,
                        SortedSet<Sentiment> sentiments, Rules rules, Array2D terrainMap,
                        Clock clock, int currentSentimentId, boolean cityGrowthEnabled) {
        this.engines = copySet(engines);
        this.cities = copySet(cities);
        this.cargos = copySet(cargos);
        this.terrainTypes = copySet(terrainTypes);
        this.trackTypes = copySet(trackTypes);
        this.sentiments = copySet(sentiments);
        this.rules = Objects.requireNonNull(rules, "rules");
        this.terrainMap = copyArray(terrainMap);
        this.clock = copyClock(clock);
        this.currentSentimentId = currentSentimentId;
        this.cityGrowthEnabled = cityGrowthEnabled;
        validate();
    }

    /**
     * Creates the world builder for this scenario's definitions and settings.
     * Terrain tiles are applied after building so scenario map randomization
     * remains the responsibility of the scenario initializer.
     */
    public World.Builder toWorldBuilder() {
        return new World.Builder()
                .setEngines(getEngines())
                .setCities(getCities())
                .setCargos(getCargos())
                .setTerrainTypes(getTerrainTypes())
                .setTrackTypes(getTrackTypes())
                .setSentiments(getSentiments())
                .setRules(rules)
                .setMapSize(terrainMap.getSize())
                .setClock(clock)
                .setCurrentSentimentId(currentSentimentId)
                .setCityGrowthEnabled(cityGrowthEnabled);
    }

    /** Captures scenario configuration and terrain IDs from an existing world. */
    public static ScenarioData fromWorld(UnmodifiableWorld world) {
        Objects.requireNonNull(world, "world");
        Vec2D size = world.getMapSize();
        Array2D map = new Array2D(size.x(), size.y());
        for (int x = 0; x < size.x(); x++) {
            for (int y = 0; y < size.y(); y++) {
                TerrainTile tile = world.getTile(new Vec2D(x, y));
                map.set(x, y, tile.terrainTypeId());
            }
        }
        return new ScenarioData(
                new TreeSet<>(world.getEngines()),
                new TreeSet<>(world.getCities()),
                new TreeSet<>(world.getCargos()),
                new TreeSet<>(world.getTerrains()),
                new TreeSet<>(world.getTrackTypes()),
                new TreeSet<>(world.getSentiments()),
                world.getRules(),
                map,
                world.getClock(),
                world.getSentiment().getId(),
                world.isCityGrowthEnabled());
    }

    /**
     * Validates the current state and reports invalid values.
     */
    private void validate() {
        if (terrainMap.getWidth() <= 0 || terrainMap.getHeight() <= 0) {
            throw new IllegalArgumentException("Scenario map dimensions must be positive.");
        }
        Set<Integer> terrainIds = new HashSet<>();
        for (Terrain terrain : terrainTypes) {
            terrainIds.add(terrain.getId());
        }
        if (!terrainIds.contains(0)) {
            throw new IllegalArgumentException("Scenario terrain definitions must include terrain ID 0.");
        }
        for (int y = 0; y < terrainMap.getHeight(); y++) {
            for (int x = 0; x < terrainMap.getWidth(); x++) {
                if (!terrainIds.contains(terrainMap.get(x, y))) {
                    throw new IllegalArgumentException(
                            "Scenario map references unknown terrain ID: " + terrainMap.get(x, y));
                }
            }
        }
        boolean sentimentExists = sentiments.stream()
                .anyMatch(sentiment -> sentiment.getId() == currentSentimentId);
        if (!sentimentExists) {
            throw new IllegalArgumentException("Scenario references unknown sentiment ID: " + currentSentimentId);
        }
    }

    /**
     * Returns the set.
     *
     * @param <T> the type parameter
     * @param values the values to use
     * @return the set
     */
    private static <T> SortedSet<T> copySet(SortedSet<T> values) {
        return Collections.unmodifiableSortedSet(new TreeSet<>(Objects.requireNonNull(values)));
    }

    /**
     * Returns the array.
     *
     * @param source the source value to copy
     * @return the array
     */
    private static Array2D copyArray(Array2D source) {
        Objects.requireNonNull(source, "terrainMap");
        Array2D copy = new Array2D(source.getWidth(), source.getHeight());
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                copy.set(x, y, source.get(x, y));
            }
        }
        return copy;
    }

    /**
     * Returns the clock.
     *
     * @param source the source value to copy
     * @return the clock
     */
    private static Clock copyClock(Clock source) {
        Objects.requireNonNull(source, "clock");
        return new Clock(source.getStartYear(), source.getTicksPerYear(), source.getCurrentTime());
    }

    /**
     * Returns the engines.
     *
     * @return the engines
     */
    public SortedSet<Engine> getEngines() {
        return copySet(engines);
    }

    /**
     * Returns the cities.
     *
     * @return the cities
     */
    public SortedSet<City> getCities() {
        return copySet(cities);
    }

    /**
     * Returns the cargos.
     *
     * @return the cargos
     */
    public SortedSet<Cargo> getCargos() {
        return copySet(cargos);
    }

    /**
     * Returns the terrain types.
     *
     * @return the terrain types
     */
    public SortedSet<Terrain> getTerrainTypes() {
        return copySet(terrainTypes);
    }

    /**
     * Returns the track types.
     *
     * @return the track types
     */
    public SortedSet<TrackType> getTrackTypes() {
        return copySet(trackTypes);
    }

    /**
     * Returns the sentiments.
     *
     * @return the sentiments
     */
    public SortedSet<Sentiment> getSentiments() {
        return copySet(sentiments);
    }

    /**
     * Returns the rules.
     *
     * @return the rules
     */
    public Rules getRules() {
        return rules;
    }

    /**
     * Returns the terrain map.
     *
     * @return the terrain map
     */
    public Array2D getTerrainMap() {
        return copyArray(terrainMap);
    }

    /**
     * Returns the clock.
     *
     * @return the clock
     */
    public Clock getClock() {
        return copyClock(clock);
    }

    /**
     * Returns the current sentiment id.
     *
     * @return the current sentiment id
     */
    public int getCurrentSentimentId() {
        return currentSentimentId;
    }

    /** Returns whether cities expand at the end of each year in this scenario. */
    public boolean isCityGrowthEnabled() {
        return cityGrowthEnabled;
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ScenarioData other)) {
            return false;
        }
        return currentSentimentId == other.currentSentimentId
                && cityGrowthEnabled == other.cityGrowthEnabled
                && engines.equals(other.engines)
                && cities.equals(other.cities)
                && cargos.equals(other.cargos)
                && terrainTypes.equals(other.terrainTypes)
                && trackTypes.equals(other.trackTypes)
                && sentiments.equals(other.sentiments)
                && rules.equals(other.rules)
                && terrainMap.equals(other.terrainMap)
                && clock.equals(other.clock);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(engines, cities, cargos, terrainTypes, trackTypes, sentiments,
                rules, terrainMap, clock, currentSentimentId, cityGrowthEnabled);
    }
}

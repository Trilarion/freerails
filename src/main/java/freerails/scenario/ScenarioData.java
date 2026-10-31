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

    public ScenarioData(SortedSet<Engine> engines, SortedSet<City> cities, SortedSet<Cargo> cargos,
                        SortedSet<Terrain> terrainTypes, SortedSet<TrackType> trackTypes,
                        SortedSet<Sentiment> sentiments, Rules rules, Array2D terrainMap,
                        Clock clock, int currentSentimentId) {
        this(engines, cities, cargos, terrainTypes, trackTypes, sentiments, rules, terrainMap,
                clock, currentSentimentId, true);
    }

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

    private static <T> SortedSet<T> copySet(SortedSet<T> values) {
        return Collections.unmodifiableSortedSet(new TreeSet<>(Objects.requireNonNull(values)));
    }

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

    private static Clock copyClock(Clock source) {
        Objects.requireNonNull(source, "clock");
        return new Clock(source.getStartYear(), source.getTicksPerYear(), source.getCurrentTime());
    }

    public SortedSet<Engine> getEngines() {
        return copySet(engines);
    }

    public SortedSet<City> getCities() {
        return copySet(cities);
    }

    public SortedSet<Cargo> getCargos() {
        return copySet(cargos);
    }

    public SortedSet<Terrain> getTerrainTypes() {
        return copySet(terrainTypes);
    }

    public SortedSet<TrackType> getTrackTypes() {
        return copySet(trackTypes);
    }

    public SortedSet<Sentiment> getSentiments() {
        return copySet(sentiments);
    }

    public Rules getRules() {
        return rules;
    }

    public Array2D getTerrainMap() {
        return copyArray(terrainMap);
    }

    public Clock getClock() {
        return copyClock(clock);
    }

    public int getCurrentSentimentId() {
        return currentSentimentId;
    }

    /** Returns whether cities expand at the end of each year in this scenario. */
    public boolean isCityGrowthEnabled() {
        return cityGrowthEnabled;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(engines, cities, cargos, terrainTypes, trackTypes, sentiments,
                rules, terrainMap, clock, currentSentimentId, cityGrowthEnabled);
    }
}

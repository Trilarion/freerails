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

package freerails.util;

import freerails.io.GsonManager;
import freerails.model.operations.cargo.Cargo;
import freerails.model.operations.cargo.CargoCategory;
import freerails.model.operations.cargo.CargoConversion;
import freerails.model.operations.cargo.CargoProductionOrConsumption;
import freerails.model.economy.Money;
import freerails.model.game.Rules;
import freerails.model.game.Sentiment;
import freerails.model.player.Player;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.city.City;
import freerails.model.track.TrackCategory;
import freerails.model.track.TrackProperty;
import freerails.model.track.TrackType;
import freerails.model.operations.train.Engine;
import freerails.model.world.World;
import freerails.io.scenario.ClasspathScenarioSource;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Provides test support for world generator.
 */
public final class WorldGenerator {

    /**
     * Only subclasses should use these constants.
     */
    public static final Player TEST_PLAYER = new Player(0, "test player");

    /**
     * Creates a world generator.
     */
    private WorldGenerator() {}

    /**
     * Performs minimal world.
     * @return the result of minimal world.
     */
    public static World minimalWorld() {
        return minimalWorld(new IntPoint2D(20, 20));
    }

    /**
     * Performs minimal world.
     * @param mapSize the map size to use in the test.
     * @return the result of minimal world.
     */
    public static World minimalWorld(IntPoint2D mapSize) {
        World.Builder builder = new World.Builder();
        try {
            builder.setRules(getUnrestrictedRules());
            builder.setSentiments(getSentiments());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        builder.setMapSize(mapSize);
        SortedSet<Terrain> terrainTypes = new TreeSet<>();
        terrainTypes.add(new Terrain(0, "Clear", TerrainCategory.COUNTRY,
                Money.ZERO, Money.ZERO, List.of(), List.of(), List.of()));
        builder.setTerrainTypes(terrainTypes);
        return builder.build();
    }

    /**
     * Performs default world.
     * @return the result of default world.
     */
    public static World defaultWorld() {
        try {
            return defaultWorld(getDefaultRules());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a default world with the supplied rules.
     *
     * @param rules game rules
     * @return the initialized world
     */
    public static World defaultWorld(Rules rules) {
        World.Builder builder = new World.Builder();
        builder.setMapSize(new IntPoint2D(200, 200));
        builder.setRules(rules);
        try {
            builder.setEngines(getEngines());
            builder.setCargos(getCargos());
            builder.setTerrainTypes(getTerrainTypes());
            builder.setTrackTypes(getTrackTypes());
            builder.setSentiments(getSentiments());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder.build();
    }

    /**
     * Tests world behavior.
     * @param useDefaultRules the use default rules to use in the test.
     * @return the result of test world.
     */
    public static World testWorld(boolean useDefaultRules) {
        return testWorld(useDefaultRules, new TreeSet<>());
    }

    /**
     * Creates a test world with the supplied rules.
     *
     * @param rules game rules
     * @return the initialized test world
     */
    public static World testWorld(Rules rules) {
        return testWorld(rules, new TreeSet<>());
    }

    /**
     * Tests world behavior.
     * @param useDefaultRules the use default rules to use in the test.
     * @param cities the cities to use in the test.
     * @return the result of test world.
     */
    public static World testWorld(boolean useDefaultRules, SortedSet<City> cities) {
        try {
            Rules rules = useDefaultRules ? getDefaultRules() : getUnrestrictedRules();
            return testWorld(rules, cities);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a test world with the supplied rules and cities.
     *
     * @param rules the rules to use
     * @param cities the cities to use
     * @return the initialized test world
     */
    public static World testWorld(Rules rules, SortedSet<City> cities) {
        World.Builder builder = new World.Builder();
        builder.setRules(rules);
        builder.setTrackTypes(testTrackTypes());
        builder.setTerrainTypes(testTerrainTypes());
        builder.setCargos(testCargos());
        builder.setCities(cities);
        builder.setMapSize(new IntPoint2D(10, 10));
        return builder.build();
    }

    /**
     * Returns default rules.
     * @return the result of get default rules.
     * @throws Exception if the operation fails.
     */
    private static Rules getDefaultRules()  throws Exception {
        return getRules("/freerails/data/scenario/rules.json");
    }

    /**
     * Returns unrestricted rules.
     * @return the result of get unrestricted rules.
     * @throws Exception if the operation fails.
     */
    private static Rules getUnrestrictedRules() throws Exception {
        return getRules("/rules.without_restrictions.json");
    }

    /**
     * Returns rules.
     * @param location the location used by this test.
     * @return the result of get rules.
     * @throws Exception if the operation fails.
     */
    private static Rules getRules(String location) throws Exception {
        // load rules
        URL url = WorldGenerator.class.getResource(location);
        File file = null;
        file = new File(url.toURI());
        return GsonManager.load(file, Rules.class);
    }

    /**
     * Returns engines.
     * @return the result of get engines.
     * @throws Exception if the operation fails.
     */
    private static SortedSet<Engine> getEngines() throws Exception {
        // load engines
        URL url = WorldGenerator.class.getResource("/freerails/data/scenario/engines.json");
        File file = null;
        file = new File(url.toURI());
        return GsonManager.loadEngines(file);
    }

    /**
     * Returns cargos.
     * @return the result of get cargos.
     * @throws Exception if the operation fails.
     */
    private static SortedSet<Cargo> getCargos() throws Exception {
        // load cargo types
        URL url = WorldGenerator.class.getResource("/freerails/data/scenario/cargo_types.json");
        File file = null;
        file = new File(url.toURI());
        return GsonManager.loadCargoTypes(file);
    }

    /**
     * Returns terrain types.
     * @return the result of get terrain types.
     * @throws Exception if the operation fails.
     */
    private static SortedSet<Terrain> getTerrainTypes() throws Exception {
        // load terrain types
        URL url = WorldGenerator.class.getResource("/freerails/data/scenario/terrain_types.json");
        File file = null;
        file = new File(url.toURI());
        return GsonManager.loadTerrainTypes(file);
    }

    /**
     * Returns track types.
     * @return the result of get track types.
     * @throws Exception if the operation fails.
     */
    private static SortedSet<TrackType> getTrackTypes() throws Exception {
        // load track types
        URL url = WorldGenerator.class.getResource("/freerails/data/scenario/track_types.json");
        File file = null;
        file = new File(url.toURI());
        return GsonManager.loadTrackTypes(file);
    }

    /**
     * Returns sentiments.
     * @return the result of get sentiments.
     * @throws Exception if the operation fails.
     */
    private static SortedSet<Sentiment> getSentiments() throws Exception {
        // load sentiments
        URL url = ClasspathScenarioSource.class.getResource("/freerails/data/scenario/sentiments.json");
        File file = new File(url.toURI());
        return GsonManager.loadSentiments(file);
    }

    /**
     * Tests track types behavior.
     * @return the result of test track types.
     */
    public static SortedSet<TrackType> testTrackTypes() {
        SortedSet<TrackType> trackTypes = new TreeSet<>();

        // everywhere except on ocean
        Set<TerrainCategory> validTerrainCategories = EnumSet.allOf(TerrainCategory.class);
        validTerrainCategories.remove(TerrainCategory.OCEAN);

        // 1st track type..
        String[] trackTemplates0 = {"00010000", "00010001", "01001100", "01100100", "00010100", "00001100", "01001000"};
        Set<TrackProperty> trackProperties = EnumSet.of(TrackProperty.SINGLE);
        TrackType trackType = new TrackType(0, "type0", TrackCategory.TRACK, trackProperties, Money.ZERO, Money.ZERO, validTerrainCategories, new HashSet<>(Arrays.asList(trackTemplates0)));
        trackType.prepare();
        trackTypes.add(trackType);

        // 2nd track type..
        String[] trackTemplates1 = {"00010000", "00010001"};
        trackType = new TrackType(1, "type1", TrackCategory.TRACK, trackProperties, Money.ZERO, Money.ZERO, validTerrainCategories, new HashSet<>(Arrays.asList(trackTemplates1)));
        trackType.prepare();
        trackTypes.add(trackType);

        // 3rd track type..
        String[] trackTemplates2 = {"00010000"};
        trackType = new TrackType(2, "type2", TrackCategory.TRACK, trackProperties, Money.ZERO, Money.ZERO, validTerrainCategories, new HashSet<>(Arrays.asList(trackTemplates2)));
        trackType.prepare();
        trackTypes.add(trackType);

        return trackTypes;
    }

    /**
     * Adds hard coded terrain types new style
     */
    private static SortedSet<Terrain> testTerrainTypes() {
        SortedSet<Terrain> terrainTypes = new TreeSet<>();
        List<CargoProductionOrConsumption> x = Utils.immutableList(new ArrayList<>());
        List<CargoConversion> y = Utils.immutableList(new ArrayList<>());
        terrainTypes.add(new Terrain(0, "Grassland", TerrainCategory.COUNTRY, Money.ZERO, Money.ZERO, x, y, x));
        terrainTypes.add(new Terrain(1, "City", TerrainCategory.URBAN, Money.ZERO, Money.ZERO, x, y, x));
        terrainTypes.add(new Terrain(2, "Mine", TerrainCategory.RESOURCE, Money.ZERO, Money.ZERO, x, y, x));
        terrainTypes.add(new Terrain(3, "Factory", TerrainCategory.INDUSTRY, Money.ZERO, Money.ZERO, x, y, x));
        terrainTypes.add(new Terrain(4, "Ocean", TerrainCategory.OCEAN, Money.ZERO, Money.ZERO, x, y, x));
        return terrainTypes;
    }

    /**
     * Tests cargos behavior.
     * @return the result of test cargos.
     */
    private static SortedSet<Cargo> testCargos() {
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(0, "Mail", CargoCategory.MAIL, 1));
        cargos.add(new Cargo(1, "Passengers", CargoCategory.PASSENGER, 1));
        cargos.add(new Cargo(2, "Goods", CargoCategory.FAST_FREIGHT, 2));
        cargos.add(new Cargo(3, "Steel", CargoCategory.SLOW_FREIGHT, 4));
        cargos.add(new Cargo(4, "Coal", CargoCategory.BULK_FREIGHT, 8));
        return cargos;
    }
}

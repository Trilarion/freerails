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

package freerails.util;

import freerails.io.GsonManager;
import freerails.model.cargo.Cargo;
import freerails.model.cargo.CargoCategory;
import freerails.model.cargo.CargoConversion;
import freerails.model.cargo.CargoProductionOrConsumption;
import freerails.model.finances.Money;
import freerails.model.game.Rules;
import freerails.model.player.Player;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.track.TrackCategory;
import freerails.model.track.TrackProperty;
import freerails.model.track.TrackType;
import freerails.model.train.Engine;
import freerails.model.world.World;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public final class WorldGenerator {

    /**
     * Only subclasses should use these constants.
     */
    public static final Player TEST_PLAYER = new Player(0, "test player");

    private WorldGenerator() {}

    /**
     * Get a minimal world for basic testing.
     * @return
     */
    public static World minimalWorld() {
        World.Builder builder = new World.Builder();
        builder.setRules(getUnrestrictedRules());
        builder.setMapSize(new Vec2D(20, 20));
        return builder.build();
    }

    /**
     * Resembles a real used world.
     * @return
     */
    public static World defaultWorld() {
        World.Builder builder = new World.Builder();
        builder.setRules(getDefaultRules());
        builder.setMapSize(new Vec2D(200, 200));
        builder.setEngines(getEngines());
        builder.setCargos(getCargos());
        builder.setTerrainTypes(getTerrainTypes());
        builder.setTrackTypes(getTrackTypes());
        return builder.build();
    }

    /**
     * Get a customized world for testing.
     * @return
     */
    public static World testWorld(boolean useDefaultRules) {
        World.Builder builder = new World.Builder();
        if (useDefaultRules) {
            builder.setRules(getDefaultRules());
        } else {
            builder.setRules(getUnrestrictedRules());
        }
        builder.setTrackTypes(testTrackTypes());
        builder.setTerrainTypes(testTerrainTypes());
        builder.setCargos(testCargos());
        builder.setMapSize(new Vec2D(10, 10));
        return builder.build();
    }

    private static Rules getDefaultRules() {
        return getRules("/freerails/data/scenario/rules.json");
    }

    private static Rules getUnrestrictedRules() {
        return getRules("/rules.without_restrictions.json");
    }

    private static Rules getRules(String location) {
        // load rules
        URL url = WorldGenerator.class.getResource(location);
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
            return GsonManager.load(file, Rules.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SortedSet<Engine> getEngines() {
        // load engines
        URL url = WorldGenerator.class.getResource("/freerails/data/scenario/engines.json");
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
            return GsonManager.loadEngines(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SortedSet<Cargo> getCargos() {
        // load cargo types
        URL url = WorldGenerator.class.getResource("/freerails/data/scenario/cargo_types.json");
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
            return GsonManager.loadCargoTypes(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SortedSet<Terrain> getTerrainTypes() {
        // load terrain types
        URL url = WorldGenerator.class.getResource("/freerails/data/scenario/terrain_types.json");
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
            return GsonManager.loadTerrainTypes(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SortedSet<TrackType> getTrackTypes() {
        // load track types
        URL url = WorldGenerator.class.getResource("/freerails/data/scenario/track_types.json");
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
            return GsonManager.loadTrackTypes(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     */
    public static SortedSet<TrackType> testTrackTypes() {
        SortedSet<TrackType> trackTypes = new TreeSet<>();

        // everywhere except on ocean
        Set<TerrainCategory> validTerrainCategories = EnumSet.allOf(TerrainCategory.class);
        validTerrainCategories.remove(TerrainCategory.OCEAN);

        // 1st track type..
        String[] trackTemplates0 = {"000010000", "010010000", "010010010", "100111000", "001111000", "010110000", "100110000", "100011000"};
        Set<TrackProperty> trackProperties = EnumSet.of(TrackProperty.SINGLE);
        TrackType trackType = new TrackType(0, "type0", TrackCategory.TRACK, trackProperties, Money.ZERO, Money.ZERO, validTerrainCategories, new HashSet<>(Arrays.asList(trackTemplates0)));
        trackType.prepare();
        trackTypes.add(trackType);

        // 2nd track type..
        String[] trackTemplates1 = {"000010000", "010010000", "010010010"};
        trackType = new TrackType(1, "type1", TrackCategory.TRACK, trackProperties, Money.ZERO, Money.ZERO, validTerrainCategories, new HashSet<>(Arrays.asList(trackTemplates1)));
        trackType.prepare();
        trackTypes.add(trackType);

        // 3rd track type..
        String[] trackTemplates2 = {"000010000"};
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

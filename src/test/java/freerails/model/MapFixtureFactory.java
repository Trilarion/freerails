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

package freerails.model;

import freerails.model.cargo.CargoCategory;
import freerails.model.cargo.Cargo;
import freerails.model.cargo.CargoConversion;
import freerails.model.cargo.CargoProductionOrConsumption;
import freerails.model.finances.Money;
import freerails.model.terrain.Terrain;
import freerails.util.Utils;
import freerails.util.Vec2D;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TerrainCategory;
import freerails.model.track.*;
import freerails.model.world.World;

import java.io.IOException;
import java.util.*;

/**
 * Is used to generate fixtures for Junit tests.
 */
public class MapFixtureFactory {

    /**
     * Only subclasses should use these constants.
     */
    public static final Player TEST_PLAYER = new Player("test player", 0);

    /**
     *
     */
    public static final FreerailsPrincipal TEST_PRINCIPAL = TEST_PLAYER.getPrincipal();

    private MapFixtureFactory() {
    }

    /**
     * Returns a world object with a map of the specified size with the terrain
     * and cargo types setup.
     *
     * @param mapSize
     * @return
     */
    public static World getWorld(Vec2D mapSize) throws IOException {
        TerrainTile tile = new TerrainTile(0);
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(0, "Mail", CargoCategory.MAIL, 1));
        cargos.add(new Cargo(1, "Passengers", CargoCategory.PASSENGER, 1));
        cargos.add(new Cargo(2, "Goods", CargoCategory.FAST_FREIGHT, 2));
        cargos.add(new Cargo(3, "Steel", CargoCategory.SLOW_FREIGHT, 4));
        cargos.add(new Cargo(4, "Coal", CargoCategory.BULK_FREIGHT, 8));

        World world = new World.Builder().setMapSize(mapSize).setCargos(cargos).setTerrainTypes(generateTerrainTypesListNew()).build();

        for (int x = 0; x < mapSize.x; x++) {
            for (int y = 0; y < mapSize.y; y++) {
                world.setTile(new Vec2D(x, y), tile);
            }
        }

        return world;
    }

    /**
     */
    public static SortedSet<TrackType> generateTrackRuleList() {
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
    private static SortedSet<Terrain> generateTerrainTypesListNew() {
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
}
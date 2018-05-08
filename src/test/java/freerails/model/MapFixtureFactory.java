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
import freerails.model.terrain.TerrainType2;
import freerails.model.world.SharedKey;
import freerails.util.Vec2D;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainType;
import freerails.model.track.*;
import freerails.model.world.World;

import java.io.IOException;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

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
        generateTerrainTypesList(world);

        for (int x = 0; x < mapSize.x; x++) {
            for (int y = 0; y < mapSize.y; y++) {
                world.setTile(new Vec2D(x, y), tile);
            }
        }

        return world;
    }

    /**
     * @param world
     */
    public static void generateTrackRuleList(World world) {
        TrackRule[] trackRulesArray = new TrackRule[3];
        TrackRuleProperties[] trackRuleProperties = new TrackRuleProperties[3];
        ValidTrackConfigurations[] validTrackConfigurations = new ValidTrackConfigurations[3];
        ValidTrackPlacement[] validTrackPlacement = new ValidTrackPlacement[3];
        HashSet<TerrainCategory> cannotBuildOnTheseTerrainTypes = new HashSet<>();
        cannotBuildOnTheseTerrainTypes.add(TerrainCategory.OCEAN);

        // 1st track type..
        String[] trackTemplates0 = {"000010000", "010010000", "010010010",
                "100111000", "001111000", "010110000", "100110000", "100011000"};

        validTrackConfigurations[0] = new ValidTrackConfigurations(-1,
                trackTemplates0);
        trackRuleProperties[0] = new TrackRuleProperties(1, false, "type0",
                TrackCategory.track, 0, 0, 10, 0);
        validTrackPlacement[0] = new ValidTrackPlacement(
                cannotBuildOnTheseTerrainTypes,
                PlacementRule.ANYWHERE_EXCEPT_ON_THESE);
        trackRulesArray[0] = new TrackRuleImpl(trackRuleProperties[0],
                validTrackConfigurations[0], validTrackPlacement[0]);

        // 2nd track type..
        String[] trackTemplates1 = {"000010000", "010010000", "010010010"};
        validTrackConfigurations[1] = new ValidTrackConfigurations(-1,
                trackTemplates1);
        trackRuleProperties[1] = new TrackRuleProperties(2, false, "type1",
                TrackCategory.track, 0, 0, 20, 0);

        validTrackPlacement[1] = new ValidTrackPlacement(
                cannotBuildOnTheseTerrainTypes,
                PlacementRule.ANYWHERE_EXCEPT_ON_THESE);
        trackRulesArray[1] = new TrackRuleImpl(trackRuleProperties[1],
                validTrackConfigurations[1], validTrackPlacement[1]);

        // 3rd track type..
        trackRuleProperties[2] = new TrackRuleProperties(3, false, "type2",
                TrackCategory.track, 0, 0, 30, 0);

        String[] trackTemplates2 = {"000010000"};
        validTrackConfigurations[2] = new ValidTrackConfigurations(-1,
                trackTemplates2);
        validTrackPlacement[2] = new ValidTrackPlacement(
                cannotBuildOnTheseTerrainTypes,
                PlacementRule.ANYWHERE_EXCEPT_ON_THESE);
        trackRulesArray[2] = new TrackRuleImpl(trackRuleProperties[2],
                validTrackConfigurations[2], validTrackPlacement[2]);

        // Add track rules to world
        for (TrackRule aTrackRulesArray : trackRulesArray) {
            world.add(SharedKey.TrackRules, aTrackRulesArray);
        }

        // Add the terrain types if necessary.
        if (world.size(SharedKey.TerrainTypes) == 0) {
            generateTerrainTypesList(world);
        }
    }

    /**
     * Adds hard coded terrain types new style
     */
    private static SortedSet<TerrainType2> generateTerrainTypesListNew() {
        SortedSet<TerrainType2> terrainTypes = new TreeSet<>();
        terrainTypes.add(new TerrainType2(0, "Grassland", TerrainCategory.COUNTRY));
        terrainTypes.add(new TerrainType2(1, "City", TerrainCategory.URBAN));
        terrainTypes.add(new TerrainType2(2, "Mine", TerrainCategory.RESOURCE));
        terrainTypes.add(new TerrainType2(3, "Factory", TerrainCategory.INDUSTRY));
        terrainTypes.add(new TerrainType2(4, "Ocean", TerrainCategory.OCEAN));
        return terrainTypes;
    }

    /**
     * Adds hard coded terrain types.
     */
    private static void generateTerrainTypesList(World world) {
        world.add(SharedKey.TerrainTypes, new TerrainType(
                TerrainCategory.COUNTRY, "Grassland"));
        world.add(SharedKey.TerrainTypes, new TerrainType(
                TerrainCategory.URBAN, "City"));
        world.add(SharedKey.TerrainTypes, new TerrainType(
                TerrainCategory.RESOURCE, "Mine"));
        world.add(SharedKey.TerrainTypes, new TerrainType(
                TerrainCategory.INDUSTRY, "Factory"));
        world.add(SharedKey.TerrainTypes, new TerrainType(
                TerrainCategory.OCEAN, "Ocean"));
    }
}
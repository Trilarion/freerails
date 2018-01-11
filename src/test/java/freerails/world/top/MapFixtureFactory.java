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

package freerails.world.top;

import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.WorldImpl;
import freerails.world.cargo.CargoCategory;
import freerails.world.cargo.CargoType;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TerrainCategory;
import freerails.world.terrain.TileTypeImpl;
import freerails.world.track.*;

import java.util.HashSet;

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
    public static final FreerailsPrincipal TEST_PRINCIPAL = TEST_PLAYER
            .getPrincipal();

    private MapFixtureFactory() {
    }

    /**
     * Returns a world object with a map of the specified size with the terrain
     * and cargo types setup.
     *
     * @param w
     * @param h
     * @return
     */
    public static World getWorld(int w, int h) {
        FullTerrainTile tile = FullTerrainTile.getInstance(0);
        World world = new WorldImpl(w, h);
        generateTerrainTypesList(world);
        generateCargoTypesList(world);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < w; y++) {
                world.setTile(x, y, tile);
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
        cannotBuildOnTheseTerrainTypes.add(TerrainCategory.Ocean);

        // 1st track type..
        String[] trackTemplates0 = {"000010000", "010010000", "010010010",
                "100111000", "001111000", "010110000", "100110000", "100011000"};

        validTrackConfigurations[0] = new ValidTrackConfigurations(-1,
                trackTemplates0);
        trackRuleProperties[0] = new TrackRuleProperties(1, false, "type0",
                TrackCategories.track, 0, 0, 10, 0);
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
                TrackCategories.track, 0, 0, 20, 0);

        validTrackPlacement[1] = new ValidTrackPlacement(
                cannotBuildOnTheseTerrainTypes,
                PlacementRule.ANYWHERE_EXCEPT_ON_THESE);
        trackRulesArray[1] = new TrackRuleImpl(trackRuleProperties[1],
                validTrackConfigurations[1], validTrackPlacement[1]);

        // 3rd track type..
        trackRuleProperties[2] = new TrackRuleProperties(3, false, "type2",
                TrackCategories.track, 0, 0, 30, 0);

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
            world.add(SKEY.TRACK_RULES, aTrackRulesArray);
        }

        // Add the terrain types if necessary.
        if (world.size(SKEY.TERRAIN_TYPES) == 0) {
            generateTerrainTypesList(world);
        }
    }

    /**
     * Adds hard coded cargo types.
     *
     * @param world
     */
    public static void generateCargoTypesList(World world) {
        world.add(SKEY.CARGO_TYPES, new CargoType(0, "Mail", CargoCategory.Mail));
        world.add(SKEY.CARGO_TYPES, new CargoType(0, "Passengers",
                CargoCategory.Passengers));
        world.add(SKEY.CARGO_TYPES, new CargoType(0, "Goods",
                CargoCategory.Fast_Freight));
        world.add(SKEY.CARGO_TYPES, new CargoType(0, "Steel",
                CargoCategory.Slow_Freight));
        world.add(SKEY.CARGO_TYPES, new CargoType(0, "Coal",
                CargoCategory.Bulk_Freight));
    }

    /**
     * Adds hard coded terrain types.
     */
    private static void generateTerrainTypesList(World world) {
        world.add(SKEY.TERRAIN_TYPES, new TileTypeImpl(
                TerrainCategory.Country, "Grassland"));
        world.add(SKEY.TERRAIN_TYPES, new TileTypeImpl(
                TerrainCategory.Urban, "City"));
        world.add(SKEY.TERRAIN_TYPES, new TileTypeImpl(
                TerrainCategory.Resource, "Mine"));
        world.add(SKEY.TERRAIN_TYPES, new TileTypeImpl(
                TerrainCategory.Industry, "Factory"));
        world.add(SKEY.TERRAIN_TYPES, new TileTypeImpl(
                TerrainCategory.Ocean, "Ocean"));
    }
}
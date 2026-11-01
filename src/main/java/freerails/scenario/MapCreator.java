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

import freerails.model.terrain.*;
import freerails.model.terrain.city.CityInitializer;
import freerails.model.world.World;
import freerails.util.Array2D;
import freerails.util.Vector2D;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Builds a new game world from a scenario map resource.
 */
public class MapCreator {

    /**
     * Creates a map creator from the supplied values.
     */
    private MapCreator() {}

    // TODO This would be better implemented in a config file, or better still dynamically determined by scanning the directory.
    /**
     * Returns the available map names.
     *
     * @return the available map names
     */
    public static String[] getAvailableMapNames() {
        return new String[]{"South America", "Small South America"};
    }

    /**
     * Note, the map name is converted to lower case and any spaces are replaced
     * with underscores.
     *
     * @param filePath the path to the scenario map resource
     * @return a new game world initialized from the specified map resource
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    public static World newMap(String filePath) throws URISyntaxException, IOException {
        String mapName = filePath;

        mapName = mapName.toLowerCase();
        mapName = mapName.replace(' ', '_');

        URL url = MapCreator.class.getResource("/freerails/data/scenario/");
        File directory = new File(url.toURI());
        ScenarioData scenario = ScenarioDataLoader.load(directory, mapName);

        // Load the terrain map
        Array2D map = scenario.terrainMap();
        World world = scenario.toWorldBuilder().build();

        // TODO what is the purpose of the following section (randomization)
        final List<Integer> countryTypes = new ArrayList<>();
        final List<Integer> non_countryTypes = new ArrayList<>();

        for (Terrain terrainType: world.getTerrains()) {

            if (terrainType.getCategory() == TerrainCategory.COUNTRY) {
                if (!terrainType.getName().equals("Clear")) {
                    countryTypes.add(terrainType.getId());
                }
            }

            if (terrainType.getCategory() == TerrainCategory.OCEAN || terrainType.getCategory() == TerrainCategory.RIVER || terrainType.getCategory() == TerrainCategory.HILL) {
                non_countryTypes.add(terrainType.getId());
            }
        }

        /*
         * create vector to keep track of terrain randomisation 'clumping'
         */
        List<TerrainAtLocation> locations = new ArrayList<>();

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                TerrainTile tile;
                int type = map.get(x, y);

                tile = new TerrainTile(TerrainUtils.randomizeTerrain(type, countryTypes, non_countryTypes));
                Vector2D location = new Vector2D(x, y);
                if (countryTypes.contains(tile.terrainTypeId())) {
                    locations.add(new TerrainAtLocation(location, tile.terrainTypeId()));
                }

                world.setTile(location, tile);
            }
        }

        // TODO another probability, should this also be part of TerrainUtils.randomizeTerrain?
        for (TerrainAtLocation terrainAtLocation : locations) {
            TerrainTile tile = new TerrainTile(terrainAtLocation.terrainType());

            Vector2D location = terrainAtLocation.location();
            int val = 3;
            Vector2D v = new Vector2D(val, val);

            double prob = 0.75;

            if (world.boundsContain(Vector2D.subtract(location, v)) && world.boundsContain(Vector2D.add(location, v))) {
                for (int m = location.x() - val; m < location.x() + val; m++) {
                    for (int n = location.y() - val; n < location.y() + val; n++) {
                        if (Math.random() > prob) {
                            Vector2D p = new Vector2D(m, n);
                            if (!non_countryTypes.contains(world.getTile(p).terrainTypeId())) {
                                world.setTile(p, tile);
                            }
                        }
                    }
                }
            }
        }

        // Randomly position the city tiles
        CityInitializer cityInitializer = new CityInitializer(world);
        cityInitializer.initializeCities();

        /*
         * Note, money used to get added to player finance here, now it is done
         * when players are added. See AddPlayerMove
         */
        return world;
    }

}

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

package freerails.model.scenario;

import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainAtLocation;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TerrainUtils;
import freerails.model.terrain.city.CityInitializer;
import freerails.model.world.World;
import freerails.util.Array2D;
import freerails.util.Vector2D;

import java.util.ArrayList;
import java.util.List;

/** Builds a game world from scenario configuration. */
public final class ScenarioWorldInitializer {

    private ScenarioWorldInitializer() {
    }

    /**
     * Creates and initializes a world from scenario definitions and terrain.
     *
     * @param scenario the scenario configuration
     * @return the initialized world
     */
    public static World createWorld(ScenarioData scenario) {
        Array2D map = scenario.terrainMap();
        World world = scenario.toWorldBuilder().build();

        List<Integer> countryTypes = new ArrayList<>();
        List<Integer> nonCountryTypes = new ArrayList<>();
        for (Terrain terrainType : world.getTerrains()) {
            if (terrainType.getCategory() == TerrainCategory.COUNTRY
                    && !terrainType.getName().equals("Clear")) {
                countryTypes.add(terrainType.getId());
            }
            if (terrainType.getCategory() == TerrainCategory.OCEAN
                    || terrainType.getCategory() == TerrainCategory.RIVER
                    || terrainType.getCategory() == TerrainCategory.HILL) {
                nonCountryTypes.add(terrainType.getId());
            }
        }

        List<TerrainAtLocation> locations = new ArrayList<>();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int type = map.get(x, y);
                TerrainTile tile = new TerrainTile(
                        TerrainUtils.randomizeTerrain(type, countryTypes, nonCountryTypes));
                Vector2D location = new Vector2D(x, y);
                if (countryTypes.contains(tile.terrainTypeId())) {
                    locations.add(new TerrainAtLocation(location, tile.terrainTypeId()));
                }
                world.setTile(location, tile);
            }
        }

        for (TerrainAtLocation terrainAtLocation : locations) {
            TerrainTile tile = new TerrainTile(terrainAtLocation.terrainType());
            Vector2D location = terrainAtLocation.location();
            int radius = 3;
            Vector2D offset = new Vector2D(radius, radius);
            if (world.boundsContain(Vector2D.subtract(location, offset))
                    && world.boundsContain(Vector2D.add(location, offset))) {
                for (int x = location.x() - radius; x < location.x() + radius; x++) {
                    for (int y = location.y() - radius; y < location.y() + radius; y++) {
                        if (Math.random() > 0.75) {
                            Vector2D point = new Vector2D(x, y);
                            if (!nonCountryTypes.contains(world.getTile(point).terrainTypeId())) {
                                world.setTile(point, tile);
                            }
                        }
                    }
                }
            }
        }

        new CityInitializer(world).initializeCities();
        return world;
    }
}

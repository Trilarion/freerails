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

package freerails.model.terrain;

import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Lets the server analyse and alter cities.
 */
public class CityModel {

    /**
     * Map location -> terrainTypeId
     */
    public final Map<Vec2D, Integer> urbanCityTiles = new HashMap<>();
    public final Map<Vec2D, Integer> industryCityTiles = new HashMap<>();
    public final List<Terrain> industriesNotAtCity = new ArrayList<>();
    private final Map<Vec2D, Integer> resourceCityTiles = new HashMap<>();
    public final List<Vec2D> clearTiles = new ArrayList<>();
    /**
     * The number of stations within this city's bounds.
     */
    public int stations = 0;

    private static void writeTile(World world, Vec2D location, int terrainTypeId) {
        TerrainTile terrainTile = world.getTile(location);
        terrainTile = new TerrainTile(terrainTypeId, terrainTile.getTrackPiece());
        world.setTile(location, terrainTile);
    }

    public void addTile(Terrain terrainType) {
        Random rand = new Random();

        // Pick a spot at random at which to place the tile.
        if (!clearTiles.isEmpty()) {
            int tilePos = rand.nextInt(clearTiles.size());
            Vec2D p = clearTiles.remove(tilePos);

            switch (terrainType.getCategory()) {
                case URBAN:
                    urbanCityTiles.put(p, terrainType.getId());
                    break;
                case INDUSTRY:
                    industryCityTiles.put(p, terrainType.getId());
                    industriesNotAtCity.remove(terrainType);
                    break;
                case COUNTRY:
                    throw new IllegalArgumentException("call remove(.) to replace a city tile with a country tile!");
                case RESOURCE:
                    resourceCityTiles.put(p, terrainType.getId());
                    break;
            }
        }
    }

    public void loadFromMap(UnmodifiableWorld world, int cityID) {
        // Reset lists of tiles.
        urbanCityTiles.clear();
        industryCityTiles.clear();
        clearTiles.clear();
        resourceCityTiles.clear();

        // Set up the list of industries not at the city.
        industriesNotAtCity.clear();

        for (Terrain terrainType: world.getTerrains()) {
            if (terrainType.getCategory().equals(TerrainCategory.INDUSTRY)) {
                industriesNotAtCity.add(terrainType);
            }
        }

        stations = 0;

        // Identify city's bounds.
        Vec2D mapSize = world.getMapSize();
        Rectangle mapRect = new Rectangle(0, 0, mapSize.x, mapSize.y);
        City city = world.getCity(cityID);
        Vec2D topleft = Vec2D.subtract(city.getLocation(), new Vec2D(-3,-3));
        Rectangle cityArea = new Rectangle(topleft.x, topleft.y, 7, 7);
        cityArea = cityArea.intersection(mapRect);

        // Count tile types.
        for (int x = cityArea.x; x < cityArea.x + cityArea.width; x++) {
            for (int y = cityArea.y; y < cityArea.y + cityArea.height; y++) {
                TerrainTile tile = (TerrainTile) world.getTile(new Vec2D(x, y));

                // Count the number of stations at the city.
                if (tile.getTrackPiece() != null && tile.getTrackPiece().getTrackRule().isStation()) {
                    stations++;
                }

                int terrainTypeId = tile.getTerrainTypeId();
                Terrain type = world.getTerrain(terrainTypeId);

                Vec2D location = new Vec2D(x, y);
                switch (type.getCategory()) {
                    case URBAN:
                        urbanCityTiles.put(location, type.getId());
                        break;
                    case INDUSTRY:
                        industryCityTiles.put(location, type.getId());
                        industriesNotAtCity.remove(type);
                        break;
                    case COUNTRY:
                        clearTiles.add(location);
                        break;
                    case RESOURCE:
                        resourceCityTiles.put(location, type.getId());
                        break;
                }
            }
        }
    }

    public int size() {
        return urbanCityTiles.size() + industryCityTiles.size() + resourceCityTiles.size();
    }

    public void writeToMap(World world) {
        BiConsumer<Vec2D, Integer> f = (location, terrainTypeId) -> writeTile(world, location, terrainTypeId);
        urbanCityTiles.forEach(f);
        industryCityTiles.forEach(f);
        resourceCityTiles.forEach(f);
    }

}
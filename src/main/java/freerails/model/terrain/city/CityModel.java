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

package freerails.model.terrain.city;

import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainTile;
import freerails.util.Vector2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Collects the terrain tiles and station count in a city area, then writes
 * tile changes back to the world.
 */
public class CityModel {

    private final Map<Vector2D, Integer> urbanCityTiles = new HashMap<>();
    private final Map<Vector2D, Integer> industryCityTiles = new HashMap<>();
    private final List<Terrain> industriesNotAtCity = new ArrayList<>();
    private final Map<Vector2D, Integer> resourceCityTiles = new HashMap<>();
    private final List<Vector2D> clearTiles = new ArrayList<>();
    private int stations = 0;

    /**
     * Replaces a randomly selected available clear tile with the specified
     * urban, industry, or resource terrain type.
     *
     * @param terrainType terrain definition to place
     * @throws IllegalArgumentException if the terrain is a country type
     */
    public void addTile(Terrain terrainType) {
        Random rand = new Random();

        // Pick a spot at random at which to place the tile.
        if (!getClearTiles().isEmpty()) {
            int tilePos = rand.nextInt(getClearTiles().size());
            Vector2D p = getClearTiles().remove(tilePos);

            switch (terrainType.getCategory()) {
                case URBAN:
                    getUrbanCityTiles().put(p, terrainType.getId());
                    break;
                case INDUSTRY:
                    getIndustryCityTiles().put(p, terrainType.getId());
                    getIndustriesNotAtCity().remove(terrainType);
                    break;
                case COUNTRY:
                    throw new IllegalArgumentException("call remove(.) to replace a city tile with a country tile!");
                case RESOURCE:
                    getResourceCityTiles().put(p, terrainType.getId());
                    break;
            }
        }
    }

    /**
     * Rebuilds the model's tile collections from the clipped 7-by-7 area around
     * a city and counts stations there.
     *
     * @param world world supplying map, city, and terrain data
     * @param cityID identifier of the city whose area is analyzed
     */
    public void loadFromMap(UnmodifiableWorld world, int cityID) {
        // Reset lists of tiles.
        getUrbanCityTiles().clear();
        getIndustryCityTiles().clear();
        getClearTiles().clear();
        getResourceCityTiles().clear();

        // Set up the list of industries not at the city.
        getIndustriesNotAtCity().clear();

        for (Terrain terrainType : world.getTerrains()) {
            if (terrainType.getCategory().equals(TerrainCategory.INDUSTRY)) {
                getIndustriesNotAtCity().add(terrainType);
            }
        }

        stations = 0;

        // Identify city's bounds.
        Vector2D mapSize = world.getMapSize();
        Rectangle mapRect = new Rectangle(0, 0, mapSize.x(), mapSize.y());
        City city = world.getCity(cityID);
        Vector2D topleft = Vector2D.subtract(city.getLocation(), new Vector2D(3, 3));
        Rectangle cityArea = new Rectangle(topleft.x(), topleft.y(), 7, 7);
        cityArea = cityArea.intersection(mapRect);

        // Count tile types.
        for (int x = cityArea.x; x < cityArea.x + cityArea.width; x++) {
            for (int y = cityArea.y; y < cityArea.y + cityArea.height; y++) {
                TerrainTile tile = world.getTile(new Vector2D(x, y));

                // Count the number of stations at the city.
                if (tile.trackPiece() != null && tile.trackPiece().trackType().isStation()) {
                    stations++;
                }

                int terrainTypeId = tile.terrainTypeId();
                Terrain type = world.getTerrain(terrainTypeId);

                Vector2D location = new Vector2D(x, y);
                switch (type.getCategory()) {
                    case URBAN:
                        getUrbanCityTiles().put(location, type.getId());
                        break;
                    case INDUSTRY:
                        getIndustryCityTiles().put(location, type.getId());
                        getIndustriesNotAtCity().remove(type);
                        break;
                    case COUNTRY:
                        getClearTiles().add(location);
                        break;
                    case RESOURCE:
                        getResourceCityTiles().put(location, type.getId());
                        break;
                }
            }
        }
    }

    /**
     * Writes the urban, industry, and resource assignments to the world while
     * preserving each affected tile's track piece.
     *
     * @param world mutable world to update
     */
    public void writeToMap(World world) {
        BiConsumer<Vector2D, Integer> f = (location, terrainTypeId) -> {
            TerrainTile terrainTile = world.getTile(location);
            terrainTile = new TerrainTile(terrainTypeId, terrainTile.trackPiece());
            world.setTile(location, terrainTile);
        };
        getUrbanCityTiles().forEach(f);
        getIndustryCityTiles().forEach(f);
        getResourceCityTiles().forEach(f);
    }

    /**
     * Returns the mutable map of urban tile locations to terrain identifiers.
     *
     * @return urban tile locations mapped to terrain identifiers
     */
    public Map<Vector2D, Integer> getUrbanCityTiles() {
        return urbanCityTiles;
    }

    /**
     * Returns the mutable map of industry tile locations to terrain identifiers.
     *
     * @return industry tile locations mapped to terrain identifiers
     */
    public Map<Vector2D, Integer> getIndustryCityTiles() {
        return industryCityTiles;
    }

    /**
     * Returns the mutable list of industry types not currently placed here.
     *
     * @return industry terrain types not placed in the city
     */
    public List<Terrain> getIndustriesNotAtCity() {
        return industriesNotAtCity;
    }

    /**
     * Returns the mutable map of resource tile locations to terrain identifiers.
     *
     * @return resource tile locations mapped to terrain identifiers
     */
    public Map<Vector2D, Integer> getResourceCityTiles() {
        return resourceCityTiles;
    }

    /**
     * Returns the mutable list of clear locations available for city growth.
     *
     * @return clear map locations available for development
     */
    public List<Vector2D> getClearTiles() {
        return clearTiles;
    }

    /**
     * Returns the number of stations within this city's bounds.
     *
     * @return the number of stations
     */
    public int getStations() {
        return stations;
    }
}
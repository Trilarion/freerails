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

package freerails.model.terrain.city;

import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainTile;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

// TODO only used from CityTilePositioner
/**
 * Lets the server analyse and alter cities.
 */
public class CityModel {

    private final Map<Vec2D, Integer> urbanCityTiles = new HashMap<>();
    private final Map<Vec2D, Integer> industryCityTiles = new HashMap<>();
    private final List<Terrain> industriesNotAtCity = new ArrayList<>();
    private final Map<Vec2D, Integer> resourceCityTiles = new HashMap<>();
    private final List<Vec2D> clearTiles = new ArrayList<>();
    private int stations = 0;

    public void addTile(Terrain terrainType) {
        Random rand = new Random();

        // Pick a spot at random at which to place the tile.
        if (!getClearTiles().isEmpty()) {
            int tilePos = rand.nextInt(getClearTiles().size());
            Vec2D p = getClearTiles().remove(tilePos);

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
        Vec2D mapSize = world.getMapSize();
        Rectangle mapRect = new Rectangle(0, 0, mapSize.x, mapSize.y);
        City city = world.getCity(cityID);
        Vec2D topleft = Vec2D.subtract(city.getLocation(), new Vec2D(3, 3));
        Rectangle cityArea = new Rectangle(topleft.x, topleft.y, 7, 7);
        cityArea = cityArea.intersection(mapRect);

        // Count tile types.
        for (int x = cityArea.x; x < cityArea.x + cityArea.width; x++) {
            for (int y = cityArea.y; y < cityArea.y + cityArea.height; y++) {
                TerrainTile tile = world.getTile(new Vec2D(x, y));

                // Count the number of stations at the city.
                if (tile.getTrackPiece() != null && tile.getTrackPiece().getTrackType().isStation()) {
                    stations++;
                }

                int terrainTypeId = tile.getTerrainTypeId();
                Terrain type = world.getTerrain(terrainTypeId);

                Vec2D location = new Vec2D(x, y);
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

    public void writeToMap(World world) {
        BiConsumer<Vec2D, Integer> f = (location, terrainTypeId) -> {
            TerrainTile terrainTile = world.getTile(location);
            terrainTile = new TerrainTile(terrainTypeId, terrainTile.getTrackPiece());
            world.setTile(location, terrainTile);
        };
        getUrbanCityTiles().forEach(f);
        getIndustryCityTiles().forEach(f);
        getResourceCityTiles().forEach(f);
    }

    /**
     * Map location -> terrainTypeId
     */
    public Map<Vec2D, Integer> getUrbanCityTiles() {
        return urbanCityTiles;
    }

    public Map<Vec2D, Integer> getIndustryCityTiles() {
        return industryCityTiles;
    }

    public List<Terrain> getIndustriesNotAtCity() {
        return industriesNotAtCity;
    }

    public Map<Vec2D, Integer> getResourceCityTiles() {
        return resourceCityTiles;
    }

    public List<Vec2D> getClearTiles() {
        return clearTiles;
    }

    /**
     * The number of stations within this city's bounds.
     */
    public int getStations() {
        return stations;
    }
}
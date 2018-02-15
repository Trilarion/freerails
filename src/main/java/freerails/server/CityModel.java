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

package freerails.server;

import freerails.model.terrain.CityTile;
import freerails.util.Vector2D;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.SKEY;
import freerails.model.world.World;
import freerails.model.terrain.City;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TerrainType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Lets the server analyse and alter cities.
 */
public class CityModel {

    public final Collection<CityTile> urbanCityTiles = new ArrayList<>();
    public final Collection<CityTile> industryCityTiles = new ArrayList<>();
    public final List<TerrainType> industriesNotAtCity = new ArrayList<>();
    private final Collection<CityTile> resourceCityTiles = new ArrayList<>();
    public final List<Vector2D> clearTiles = new ArrayList<>();
    /**
     * The number of stations within this city's bounds.
     */
    public int stations = 0;

    private static void writeTile(World world, CityTile cityTile) {
        int type = 0;

        while (!world.get(SKEY.TERRAIN_TYPES, type).equals(cityTile.terrainType)) {
            type++;
        }

        FullTerrainTile fTile = (FullTerrainTile) world.getTile(cityTile.location);
        fTile = FullTerrainTile.getInstance(type, fTile.getTrackPiece());
        world.setTile(cityTile.location, fTile);
    }

    public void addTile(TerrainType type) {
        Random rand = new Random();

        // Pick a spot at random at which to place the tile.
        if (!clearTiles.isEmpty()) {
            int tilePos = rand.nextInt(clearTiles.size());
            Vector2D p = clearTiles.remove(tilePos);

            switch (type.getCategory()) {
                case Urban:
                    urbanCityTiles.add(new CityTile(p, type));
                    break;
                case Industry:
                    industryCityTiles.add(new CityTile(p, type));
                    industriesNotAtCity.remove(type);
                    break;
                case Country:
                    throw new IllegalArgumentException("call remove(.) to replace a city tile with a country tile!");
                case Resource:
                    resourceCityTiles.add(new CityTile(p, type));
                    break;
            }
        }
    }

    public void loadFromMap(ReadOnlyWorld world, int cityID) {
        // Reset lists of tiles.
        urbanCityTiles.clear();
        industryCityTiles.clear();
        clearTiles.clear();
        resourceCityTiles.clear();

        // Set up the list of industries not at the city.
        industriesNotAtCity.clear();

        for (int i = 0; i < world.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType type = (TerrainType) world.get(SKEY.TERRAIN_TYPES, i);

            if (type.getCategory() == TerrainCategory.Industry) {
                industriesNotAtCity.add(type);
            }
        }

        stations = 0;

        // Identify city's bounds.
        Rectangle mapRect = new Rectangle(0, 0, world.getMapWidth(), world.getMapHeight());
        City city = (City) world.get(SKEY.CITIES, cityID);
        Vector2D topleft = Vector2D.subtract(city.getLocation(), new Vector2D(-3,-3));
        Rectangle cityArea = new Rectangle(topleft.x, topleft.y, 7, 7);
        cityArea = cityArea.intersection(mapRect);

        // Count tile types.
        for (int x = cityArea.x; x < cityArea.x + cityArea.width; x++) {
            for (int y = cityArea.y; y < cityArea.y + cityArea.height; y++) {
                FullTerrainTile tile = (FullTerrainTile) world.getTile(new Vector2D(x, y));

                // Count the number of stations at the city.
                if (tile.getTrackPiece().getTrackRule().isStation()) {
                    stations++;
                }

                int terrainTypeNumber = tile.getTerrainTypeID();
                TerrainType type = (TerrainType) world.get(SKEY.TERRAIN_TYPES, terrainTypeNumber);

                Vector2D location = new Vector2D(x, y);
                switch (type.getCategory()) {
                    case Urban:
                        urbanCityTiles.add(new CityTile(location, type));
                        break;
                    case Industry:
                        industryCityTiles.add(new CityTile(location, type));
                        industriesNotAtCity.remove(type);
                        break;
                    case Country:
                        clearTiles.add(location);
                        break;
                    case Resource:
                        resourceCityTiles.add(new CityTile(location, type));
                        break;
                }
            }
        }
    }

    public int size() {
        return urbanCityTiles.size() + industryCityTiles.size() + resourceCityTiles.size();
    }

    public void writeToMap(World w) {
        for (CityTile urbanCityTile : urbanCityTiles) {
            writeTile(w, urbanCityTile);
        }

        for (CityTile industryCityTile : industryCityTiles) {
            writeTile(w, industryCityTile);
        }

        for (CityTile resourceCityTile : resourceCityTiles) {
            writeTile(w, resourceCityTile);
        }
    }

}
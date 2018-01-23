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

import freerails.util.Point2D;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.terrain.City;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TerrainCategory;
import freerails.world.terrain.TerrainType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Lets the server analyse and alter cities.
 */
class CityModel {

    final Collection<CityTile> urbanCityTiles = new ArrayList<>();
    final Collection<CityTile> industryCityTiles = new ArrayList<>();
    final List<TerrainType> industriesNotAtCity = new ArrayList<>();
    private final Collection<CityTile> resourceCityTiles = new ArrayList<>();
    final List<Point> clearTiles = new ArrayList<>();
    /**
     * The number of stations within this city's bounds.
     */
    int stations = 0;

    private static void writeTile(World w, CityTile cityTile) {
        int type = 0;

        while (!w.get(SKEY.TERRAIN_TYPES, type).equals(cityTile.type)) {
            type++;
        }

        Point2D p = new Point2D(cityTile.p);
        FullTerrainTile fTile = (FullTerrainTile) w.getTile(p);
        fTile = FullTerrainTile.getInstance(type, fTile.getTrackPiece());
        w.setTile(p, fTile);
    }

    void addTile(TerrainType type) {
        Random rand = new Random();

        // Pick a spot at random at which to place the tile.
        if (!clearTiles.isEmpty()) {
            int tilePos = rand.nextInt(clearTiles.size());
            Point p = clearTiles.remove(tilePos);

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

    void loadFromMap(ReadOnlyWorld w, int cityID) {
        // Reset lists of tiles.
        urbanCityTiles.clear();
        industryCityTiles.clear();
        clearTiles.clear();
        resourceCityTiles.clear();

        // Set up the list of industries not at the city.
        industriesNotAtCity.clear();

        for (int i = 0; i < w.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType type = (TerrainType) w.get(SKEY.TERRAIN_TYPES, i);

            if (type.getCategory() == TerrainCategory.Industry) {
                industriesNotAtCity.add(type);
            }
        }

        stations = 0;

        // Identify city's bounds.
        Rectangle mapRect = new Rectangle(0, 0, w.getMapWidth(), w.getMapHeight());
        City city = (City) w.get(SKEY.CITIES, cityID);
        Rectangle cityArea = new Rectangle(city.getX() - 3, city.getY() - 3, 7, 7);
        cityArea = cityArea.intersection(mapRect);

        // Count tile types.
        for (int x = cityArea.x; x < cityArea.x + cityArea.width; x++) {
            for (int y = cityArea.y; y < cityArea.y + cityArea.height; y++) {
                FullTerrainTile tile = (FullTerrainTile) w.getTile(new Point2D(x, y));

                // Count the number of stations at the city.
                if (tile.getTrackPiece().getTrackRule().isStation()) {
                    stations++;
                }

                int terrainTypeNumber = tile.getTerrainTypeID();
                TerrainType type = (TerrainType) w.get(SKEY.TERRAIN_TYPES, terrainTypeNumber);

                switch (type.getCategory()) {
                    case Urban:
                        urbanCityTiles.add(new CityTile(new Point(x, y), type));
                        break;
                    case Industry:
                        industryCityTiles.add(new CityTile(new Point(x, y), type));
                        industriesNotAtCity.remove(type);
                        break;
                    case Country:
                        clearTiles.add(new Point(x, y));
                        break;
                    case Resource:
                        resourceCityTiles.add(new CityTile(new Point(x, y), type));
                        break;
                }
            }
        }
    }

    int size() {
        return urbanCityTiles.size() + industryCityTiles.size() + resourceCityTiles.size();
    }

    void writeToMap(World w) {
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
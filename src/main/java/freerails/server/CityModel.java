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

import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.terrain.City;
import freerails.world.terrain.FreerailsTile;
import freerails.world.terrain.TerrainCategory;
import freerails.world.terrain.TerrainType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Lets the server analyse and alter cities.
 */
class CityModel {

    final ArrayList<Tile> urbanTiles = new ArrayList<>();
    final ArrayList<Tile> industryTiles = new ArrayList<>();
    final ArrayList<TerrainType> industriesNotAtCity = new ArrayList<>();
    final ArrayList<Tile> resourceTiles = new ArrayList<>();
    final ArrayList<Point> clearTiles = new ArrayList<>();
    /**
     * The number of stations within this city's bounds.
     */
    int stations = 0;

    void addTile(TerrainType type) {
        Random rand = new Random();

        // Pick a spot at random at which to place the tile.
        if (clearTiles.size() > 0) {
            int tilePos = rand.nextInt(clearTiles.size());
            Point p = clearTiles.remove(tilePos);

            if (type.getCategory().equals(TerrainCategory.Urban)) {
                urbanTiles.add(new Tile(p, type));
            } else if (type.getCategory().equals(TerrainCategory.Industry)) {
                industryTiles.add(new Tile(p, type));
                industriesNotAtCity.remove(type);
            } else if (type.getCategory().equals(TerrainCategory.Country)) {
                throw new IllegalArgumentException(
                        "call remove(.) to replace a city tile with a country tile!");
            } else if (type.getCategory().equals(TerrainCategory.Resource)) {
                resourceTiles.add(new Tile(p, type));
            }
        }
    }

    void loadFromMap(ReadOnlyWorld w, int cityID) {
        /* Reset lists of tiles. */
        urbanTiles.clear();
        industryTiles.clear();
        clearTiles.clear();
        resourceTiles.clear();

        /* Set up the list of industries not at the city. */
        industriesNotAtCity.clear();

        for (int i = 0; i < w.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType type = (TerrainType) w.get(SKEY.TERRAIN_TYPES, i);

            if (type.getCategory().equals(TerrainCategory.Industry)) {
                industriesNotAtCity.add(type);
            }
        }

        stations = 0;

        /* Identify city's bounds. */
        Rectangle mapRect = new Rectangle(0, 0, w.getMapWidth(), w
                .getMapHeight());
        City city = (City) w.get(SKEY.CITIES, cityID);
        Rectangle cityArea = new Rectangle(city.getCityX() - 3,
                city.getCityY() - 3, 7, 7);
        cityArea = cityArea.intersection(mapRect);

        /* Count tile types. */
        for (int x = cityArea.x; x < cityArea.x + cityArea.width; x++) {
            for (int y = cityArea.y; y < cityArea.y + cityArea.height; y++) {
                FreerailsTile tile = (FreerailsTile) w.getTile(x, y);

                /* Count the number of stations at the city. */
                if (tile.getTrackPiece().getTrackRule().isStation()) {
                    stations++;
                }

                int terrainTypeNumber = tile.getTerrainTypeID();
                TerrainType type = (TerrainType) w.get(SKEY.TERRAIN_TYPES,
                        terrainTypeNumber);

                if (type.getCategory().equals(TerrainCategory.Urban)) {
                    urbanTiles.add(new Tile(new Point(x, y), type));
                } else if (type.getCategory().equals(
                        TerrainCategory.Industry)) {
                    industryTiles.add(new Tile(new Point(x, y), type));
                    industriesNotAtCity.remove(type);
                } else if (type.getCategory().equals(
                        TerrainCategory.Country)) {
                    clearTiles.add(new Point(x, y));
                } else if (type.getCategory().equals(
                        TerrainCategory.Resource)) {
                    resourceTiles.add(new Tile(new Point(x, y), type));
                }
            }
        }
    }

    int size() {
        return this.urbanTiles.size() + this.industryTiles.size()
                + this.resourceTiles.size();
    }

    void writeToMap(World w) {
        for (Tile urbanTile : urbanTiles) {
            writeTile(w, urbanTile);
        }

        for (Tile industryTile : industryTiles) {
            writeTile(w, industryTile);
        }

        for (Tile resourceTile : resourceTiles) {
            writeTile(w, resourceTile);
        }
    }

    private void writeTile(World w, Tile tile) {
        int type = 0;

        while (!w.get(SKEY.TERRAIN_TYPES, type).equals(tile.type)) {
            type++;
        }

        int x = tile.p.x;
        int y = tile.p.y;
        FreerailsTile fTile = (FreerailsTile) w.getTile(x, y);
        fTile = FreerailsTile.getInstance(type, fTile.getTrackPiece());
        w.setTile(x, y, fTile);
    }

    /**
     * Stores a tile type and its location.
     */
    private class Tile {
        final Point p;

        final TerrainType type;

        public Tile(final Point p, final TerrainType type) {
            this.p = p;
            this.type = type;
        }
    }
}
/*
 * Created on Jul 9, 2004
 */
package jfreerails.server;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;
import jfreerails.world.terrain.CityModel;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;


/**
 * This class is lets the server analyse and alter cities. 
 * 
 * @author Luke
 *  
 */
class CityEconomicModel {
    /** Stores a tile type and its location. */
    private class Tile {
        final Point p;
        final TerrainType type;

        public Tile(final Point p, final TerrainType type) {
            this.p = p;
            this.type = type;
        }
    }

    final ArrayList urbanTiles = new ArrayList();
    final ArrayList industryTiles = new ArrayList();
    final ArrayList industriesNotAtCity = new ArrayList();
    final ArrayList resourceTiles = new ArrayList();
    final ArrayList clearTiles = new ArrayList();

    /** The number of stations within this city's bounds. */
    int stations = 0;

    void addTile(TerrainType type) {
        Random rand = new Random();

        //Pick a spot at random at which to place the tile.
        if (clearTiles.size() > 0) {
            int tilePos = rand.nextInt(clearTiles.size());
            Point p = (Point)clearTiles.remove(tilePos);

            if (type.getTerrainCategory().equals("Urban")) {
                urbanTiles.add(new Tile(p, type));
            } else if (type.getTerrainCategory().equals("Industry")) {
                industryTiles.add(new Tile(p, type));
                industriesNotAtCity.remove(type);
            } else if (type.getTerrainCategory().equals("Country")) {
                throw new IllegalArgumentException(
                    "call remove(.) to replace a city tile with a country tile!");
            } else if (type.getTerrainCategory().equals("Resource")) {
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

        /* Set up the list of industries not at the city.*/
        industriesNotAtCity.clear();

        for (int i = 0; i < w.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType type = (TerrainType)w.get(SKEY.TERRAIN_TYPES, i);

            if (type.getTerrainCategory().equals("Industry")) {
                industriesNotAtCity.add(type);
            }
        }

        stations = 0;

        /* Identify city's bounds. */
        Rectangle mapRect = new Rectangle(0, 0, w.getMapWidth(),
                w.getMapHeight());
        CityModel city = (CityModel)w.get(SKEY.CITIES, cityID);
        Rectangle cityArea = new Rectangle(city.getCityX() - 3,
                city.getCityY() - 3, 7, 7);
        cityArea = cityArea.intersection(mapRect);

        /* Count tile types. */
        for (int x = cityArea.x; x < cityArea.x + cityArea.width; x++) {
            for (int y = cityArea.y; y < cityArea.y + cityArea.height; y++) {
                FreerailsTile tile = (FreerailsTile)w.getTile(x, y);

                /* Count the number of stations at the city. */
                if (tile.getTrackRule().isStation()) {
                    stations++;
                }

                int terrainTypeNumber = tile.getTerrainTypeNumber();
                TerrainType type = (TerrainType)w.get(SKEY.TERRAIN_TYPES,
                        terrainTypeNumber);

                if (type.getTerrainCategory().equals("Urban")) {
                    urbanTiles.add(new Tile(new Point(x, y), type));
                } else if (type.getTerrainCategory().equals("Industry")) {
                    industryTiles.add(new Tile(new Point(x, y), type));
                    industriesNotAtCity.remove(type);
                } else if (type.getTerrainCategory().equals("Country")) {
                    clearTiles.add(new Point(x, y));
                } else if (type.getTerrainCategory().equals("Resource")) {
                    resourceTiles.add(new Tile(new Point(x, y), type));
                }
            }
        }
    }

    int size() {
        return this.urbanTiles.size() + this.industryTiles.size() +
        this.resourceTiles.size();
    }

    void write2map(World w) {
        for (int i = 0; i < urbanTiles.size(); i++) {
            writeTile(w, (Tile)urbanTiles.get(i));
        }

        for (int i = 0; i < industryTiles.size(); i++) {
            writeTile(w, (Tile)industryTiles.get(i));
        }

        for (int i = 0; i < resourceTiles.size(); i++) {
            writeTile(w, (Tile)resourceTiles.get(i));
        }
    }

    private void writeTile(World w, Tile tile) {
        int type = 0;

        while (!w.get(SKEY.TERRAIN_TYPES, type).equals(tile.type)) {
            type++;
        }

        int x = tile.p.x;
        int y = tile.p.y;
        FreerailsTile fTile = (FreerailsTile)w.getTile(x, y);
        fTile = FreerailsTile.getInstance(type, fTile.getTrackPiece());
        w.setTile(x, y, fTile);
    }
}
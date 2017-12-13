/**
 * @author Scott Bennett
 * Date: 7th April 2003
 *
 * Class to randomly position the city tiles on the game map, within a 5x5 tile
 * area around a city. A random number of between 1 and 6 tiles are initially
 * chosen with the idea to have these increase over the period of a game.
 */

/*
 * Updated 2nd November 2003 by Scott Bennett
 *
 * Class now randomly positions 1-6 urban tiles, 0-2 industry tiles and 0-2
 * resource tiles within the 5x5 grid that is a city. Subtypes of each of these
 * categories are randomly chosen also. The maximums for these categories
 * are currently hard-coded, another solution would be preferable i think.
 */
package jfreerails.world.city;

import java.util.ArrayList;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;


public class CityTilePositioner {
    private World w;
    private double PROBABILITY_MULTIPLIER = 0.04; //Represents a 1/25 probability, ie. 1 tile, based on a 5x5 city
    private TerrainType type;
    private FreerailsTile tile;
    private ArrayList urbanTerrainTypes;
    private ArrayList industryTerrainTypes;
    private ArrayList resourceTerrainTypes;

    public CityTilePositioner(World world) {
        this.w = world;
        urbanTerrainTypes = new ArrayList();
        industryTerrainTypes = new ArrayList();
        resourceTerrainTypes = new ArrayList();

        //get the different types of Urban/Industry/Resource terrain
        for (int i = 0; i < w.size(KEY.TERRAIN_TYPES); i++) {
            type = (TerrainType)w.get(KEY.TERRAIN_TYPES, i);

            if (type.getTerrainCategory().equals("Urban")) {
                urbanTerrainTypes.add(new Integer(i));
            } else if (type.getTerrainCategory().equals("Industry")) {
                industryTerrainTypes.add(new Integer(i));
            } else if (type.getTerrainCategory().equals("Resource")) {
                resourceTerrainTypes.add(new Integer(i));
            }
        }

        doTilePositioning(6, 4, 2);
        //hard-coded limits at the moment (urban, industry, resource)
    }

    public void doTilePositioning(int urbMax, int indMax, int resMax) {
        for (int i = 0; i < w.size(KEY.CITIES); i++) {
            CityModel tempCity = (CityModel)w.get(KEY.CITIES, i);

            calculateAndPositionTiles(tempCity.getCityX(), tempCity.getCityY(),
                calcNumberOfInitialTiles(urbMax),
                calcNumberOfInitialTiles(indMax + 1) - 1,
                calcNumberOfInitialTiles(resMax + 1) - 1);
        }
    }

    public int calcNumberOfInitialTiles(int max) {
        int max_tiles = max;
        double low = 0;
        double high;
        double myRand = Math.random();

        for (int i = 1; i < max_tiles + 1; i++) {
            high = (double)i / max_tiles;

            if ((myRand >= low) && (myRand < high)) {
                return i;
            }

            low = high;
        }

        return 1;
    }

    public int randomSelector(int max, double randValue) {
        double low = 0;
        double high;

        for (int i = 1; i < max + 1; i++) {
            high = (double)i / max;

            if ((randValue >= low) && (randValue < high)) {
                return i;
            }

            low = high;
        }

        return 1;
    }

    public String getCategoryForTile(int x, int y) {
        int tileTypeNumber = w.getTile(x, y).getTerrainTypeNumber();
        String category = ((TerrainType)w.get(KEY.TERRAIN_TYPES, tileTypeNumber)).getTerrainCategory();

        return category;
    }

    public void calculateAndPositionTiles(int x, int y, int urbNo, int indNo,
        int resNo) {
        int cityX = x;
        int cityY = y;
        int urbanTiles = urbNo;
        int industryTiles = indNo;
        int resourceTiles = resNo;

        ArrayList industriesNotAtCity = new ArrayList(this.industryTerrainTypes);

        double tileProbability = (double)PROBABILITY_MULTIPLIER * (urbanTiles +
            industryTiles + resourceTiles);

        /*
         * loop until the correct amount of tiles have been built, sometimes
         * all the tiles may not get built due to ocean or something else
         * getting in the way, looping round tries a couple more times.
         */
        int loopCount = 0;

        while (((urbanTiles + industryTiles + resourceTiles) > 0) &&
                (loopCount < 3)) {
            for (int Y = cityY - 2; Y < cityY + 3; Y++) {
                for (int X = cityX - 2; X < cityX + 3; X++) {
                    if (w.boundsContain(X, Y)) {
                        if (Math.random() < tileProbability) {
                            //tile is selected, now select a tile to build
                            //if the tile has a Country terrain, then build a tile on it, otherwise ignore
                            if (getCategoryForTile(X, Y).equals("Country")) {
                                Integer typeToAdd = null;
                                int tileTypeToBuild = randomSelector(3,
                                        Math.random());
                                double myRand = Math.random();

                                if ((tileTypeToBuild == 1) && (urbanTiles > 0)) {
                                    urbanTiles--;
                                    typeToAdd = (Integer)urbanTerrainTypes.get(randomSelector(
                                                urbanTerrainTypes.size(), myRand) -
                                            1);
                                } else if ((tileTypeToBuild == 2) &&
                                        (industryTiles > 0) &&
                                        industriesNotAtCity.size() > 0) {
                                    /* We only want one of any industry type in the city.*/
                                    int i = randomSelector(industriesNotAtCity.size(),
                                            myRand) - 1;
                                    typeToAdd = (Integer)industriesNotAtCity.remove(i);
                                    industryTiles--;
                                } else if ((tileTypeToBuild == 3) &&
                                        (resourceTiles > 0)) {
                                    resourceTiles--;
                                    typeToAdd = (Integer)resourceTerrainTypes.get(randomSelector(
                                                resourceTerrainTypes.size(),
                                                myRand) - 1);
                                }

                                if (typeToAdd != null) {
                                    tile = new FreerailsTile(typeToAdd.intValue());
                                    w.setTile(X, Y, tile);
                                }
                            }

                            //end 'Country' check
                        }
                    }

                    //end bounds check
                }

                //end inner loop
            }

            //end outer loop
            loopCount += 1;
        }

        //end while
    }
    //end calculateAndPositionTiles method
} //end CityTilePositioner class

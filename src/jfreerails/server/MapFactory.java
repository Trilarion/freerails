/*
 * Created on 22-Mar-2003
 *
 */
package jfreerails.server;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.FreerailsTile;


/**
 * This class has a static method that converts an image file into a map.
 * @author Luke
 * @author Scott Bennett   (Updated 23rd Jan 2004)
 *
 * Implemented Terrain Randomisation to randomly position the terrain types
 * for each tile on the map.
 */
public class MapFactory {
    /*
     * create a vector to keep track of what terrain types to 'clump'
     */
    private static final Vector countryTypes = new Vector();
    private static final Vector non_countryTypes = new Vector();
    private static WorldImpl world;

    public static void setupMap(URL map_url, WorldImpl w,
        FreerailsProgressMonitor pm) {
        //Setup progress monitor..
        pm.setMessage("Setting up map.");
        pm.setValue(0);

        world = w;

        Image mapImage = (new javax.swing.ImageIcon(map_url)).getImage();
        Rectangle mapRect = new java.awt.Rectangle(0, 0,
                mapImage.getWidth(null), mapImage.getHeight(null));
        BufferedImage mapBufferedImage = new BufferedImage(mapRect.width,
                mapRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = mapBufferedImage.getGraphics();
        g.drawImage(mapImage, 0, 0, null);
        w.setupMap(mapRect.width, mapRect.height);

        pm.setMax(mapRect.width);

        HashMap rgb2TerrainType = new HashMap();

        for (int i = 0; i < w.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType tilemodel = (TerrainType)w.get(SKEY.TERRAIN_TYPES, i);
            rgb2TerrainType.put(new Integer(tilemodel.getRGB()), new Integer(i));
        }

        TerrainType terrainTypeTile;

        for (int c = 0; c < w.size(SKEY.TERRAIN_TYPES); c++) {
            terrainTypeTile = (TerrainType)w.get(SKEY.TERRAIN_TYPES, c);

            if (terrainTypeTile.getTerrainCategory().equals("Country")) {
                if ((!terrainTypeTile.getTerrainTypeName().equals("Clear"))) {
                    countryTypes.add(new Integer(c));
                }
            }

            if (terrainTypeTile.getTerrainCategory().equals("Ocean") ||
                    terrainTypeTile.getTerrainCategory().equals("River") ||
                    terrainTypeTile.getTerrainCategory().equals("Hill")) {
                non_countryTypes.add(new Integer(c));
            }
        }

        TerrainRandomiser terrainRandomiser = new TerrainRandomiser(countryTypes,
                non_countryTypes);

        /*
         * create vector to keep track of terrain randomisation 'clumping'
         */
        Vector locations = new Vector();

        for (int x = 0; x < mapRect.width; x++) {
            pm.setValue(x);

            for (int y = 0; y < mapRect.height; y++) {
                int rgb = mapBufferedImage.getRGB(x, y);
                FreerailsTile tile;
                Integer type = (Integer)rgb2TerrainType.get(new Integer(rgb));

                if (null == type) {
                    throw new NullPointerException(
                        "There is no terrain type mapped to rgb value " + rgb +
                        " at location " + x + ", " + y);
                }

                tile = FreerailsTile.getInstance(terrainRandomiser.getNewType(
                            type.intValue()));

                if (countryTypes.contains(
                            new Integer(tile.getTerrainTypeNumber()))) {
                    locations.add(new RandomTerrainValue(x, y,
                            tile.getTerrainTypeNumber()));
                }

                w.setTile(x, y, tile);
            }
        }

        for (int i = 0; i < locations.size(); i++) {
            RandomTerrainValue rtv = (RandomTerrainValue)locations.elementAt(i);
            FreerailsTile tile = FreerailsTile.getInstance(rtv.getType());

            int x = rtv.getX();
            int y = rtv.getY();
            int val = 3;

            double prob = 0.75;

            if (w.boundsContain(x - val, y - val) &&
                    w.boundsContain(x + val, y + val)) {
                for (int m = x - val; m < x + val; m++) {
                    for (int n = y - val; n < y + val; n++) {
                        if (Math.random() > prob) {
                            setTile(m, n, tile);
                        }
                    }
                }
            }
        }
    }

    private static void setTile(int x, int y, FreerailsTile tile) {
        if (!non_countryTypes.contains(
                    new Integer(
                        ((FreerailsTile)world.getTile(x, y)).getTerrainTypeNumber()))) {
            world.setTile(x, y, tile);
        }
    }
}
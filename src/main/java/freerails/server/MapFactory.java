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

/*
 * Created on 22-Mar-2003
 *
 */
package freerails.server;

import freerails.util.FreerailsProgressMonitor;
import freerails.world.terrain.TerrainType;
import freerails.world.top.SKEY;
import freerails.world.top.WorldImpl;
import freerails.world.track.FreerailsTile;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class has a static method that converts an image file into a map.
 *
 *
 * Implemented Terrain Randomisation to randomly position the terrain types for
 * each tile on the map.
 */
public class MapFactory {
    /*
     * create a vector to keep track of what terrain types to 'clump'
     */
    private static final Vector<Integer> countryTypes = new Vector<>();

    private static final Vector<Integer> non_countryTypes = new Vector<>();

    private static WorldImpl world;

    /**
     *
     * @param map_url
     * @param w
     * @param pm
     */
    public static void setupMap(URL map_url, WorldImpl w,
                                FreerailsProgressMonitor pm) {
        // Setup progress monitor..
        pm.setValue(0);

        world = w;

        Image mapImage = (new ImageIcon(map_url)).getImage();
        Rectangle mapRect = new java.awt.Rectangle(0, 0, mapImage
                .getWidth(null), mapImage.getHeight(null));
        BufferedImage mapBufferedImage = new BufferedImage(mapRect.width,
                mapRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = mapBufferedImage.getGraphics();
        g.drawImage(mapImage, 0, 0, null);
        w.setupMap(mapRect.width, mapRect.height);

        pm.nextStep(mapRect.width);

        HashMap<Integer, Integer> rgb2TerrainType = new HashMap<>();

        for (int i = 0; i < w.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType tilemodel = (TerrainType) w.get(SKEY.TERRAIN_TYPES, i);
            rgb2TerrainType.put(tilemodel.getRGB(), i);
        }

        TerrainType terrainTypeTile;

        for (int c = 0; c < w.size(SKEY.TERRAIN_TYPES); c++) {
            terrainTypeTile = (TerrainType) w.get(SKEY.TERRAIN_TYPES, c);

            if (terrainTypeTile.getCategory().equals(
                    TerrainType.Category.Country)) {
                if ((!terrainTypeTile.getTerrainTypeName().equals("Clear"))) {
                    countryTypes.add(c);
                }
            }

            if (terrainTypeTile.getCategory()
                    .equals(TerrainType.Category.Ocean)
                    || terrainTypeTile.getCategory().equals(
                    TerrainType.Category.River)
                    || terrainTypeTile.getCategory().equals(
                    TerrainType.Category.Hill)) {
                non_countryTypes.add(c);
            }
        }

        TerrainRandomiser terrainRandomiser = new TerrainRandomiser(
                countryTypes, non_countryTypes);

        /*
         * create vector to keep track of terrain randomisation 'clumping'
         */
        Vector<RandomTerrainValue> locations = new Vector<>();

        for (int x = 0; x < mapRect.width; x++) {
            pm.setValue(x);

            for (int y = 0; y < mapRect.height; y++) {
                int rgb = mapBufferedImage.getRGB(x, y);
                FreerailsTile tile;
                Integer type = rgb2TerrainType.get(rgb);

                if (null == type) {
                    throw new NullPointerException(
                            "There is no terrain type mapped to rgb value "
                                    + rgb + " at location " + x + ", " + y);
                }

                tile = FreerailsTile.getInstance(terrainRandomiser
                        .getNewType(type));

                if (countryTypes.contains(tile.getTerrainTypeID())) {
                    locations.add(new RandomTerrainValue(x, y, tile
                            .getTerrainTypeID()));
                }

                w.setTile(x, y, tile);
            }
        }

        for (int i = 0; i < locations.size(); i++) {
            RandomTerrainValue rtv = locations.elementAt(i);
            FreerailsTile tile = FreerailsTile.getInstance(rtv.getType());

            int x = rtv.getX();
            int y = rtv.getY();
            int val = 3;

            double prob = 0.75;

            if (w.boundsContain(x - val, y - val)
                    && w.boundsContain(x + val, y + val)) {
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
        if (!non_countryTypes.contains(((FreerailsTile) world
                .getTile(x, y)).getTerrainTypeID())) {
            world.setTile(x, y, tile);
        }
    }
}
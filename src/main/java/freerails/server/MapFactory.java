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
 *
 */
package freerails.server;

import freerails.util.Point2D;
import freerails.world.SKEY;
import freerails.world.FullWorld;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TerrainCategory;
import freerails.world.terrain.TerrainType;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Has a static method that converts an image file into a map.
 *
 * Implemented Terrain Randomisation to randomly position the terrain types for
 * each tile on the map.
 */
final class MapFactory {
    /*
     * create a vector to keep track of what terrain types to 'clump'
     */
    private static final List<Integer> countryTypes = new ArrayList();
    private static final List<Integer> non_countryTypes = new ArrayList();
    private static FullWorld world;

    private MapFactory() {
    }

    /**
     * @param map_url
     * @param w
     * @param pm
     */
    public static void setupMap(URL map_url, FullWorld w, ProgressMonitorModel pm) {
        // Setup progress monitor..
        pm.setValue(0);

        world = w;

        Image mapImage = (new ImageIcon(map_url)).getImage();
        Rectangle mapRect = new java.awt.Rectangle(0, 0, mapImage.getWidth(null), mapImage.getHeight(null));
        BufferedImage mapBufferedImage = new BufferedImage(mapRect.width, mapRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = mapBufferedImage.getGraphics();
        g.drawImage(mapImage, 0, 0, null);
        w.setupMap(mapRect.width, mapRect.height);

        pm.nextStep(mapRect.width);

        Map<Integer, Integer> rgb2TerrainType = new HashMap<>();

        for (int i = 0; i < w.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType tilemodel = (TerrainType) w.get(SKEY.TERRAIN_TYPES, i);
            rgb2TerrainType.put(tilemodel.getRGB(), i);
        }

        TerrainType terrainTypeTile;

        for (int c = 0; c < w.size(SKEY.TERRAIN_TYPES); c++) {
            terrainTypeTile = (TerrainType) w.get(SKEY.TERRAIN_TYPES, c);

            if (terrainTypeTile.getCategory() == TerrainCategory.Country) {
                if ((!terrainTypeTile.getTerrainTypeName().equals("Clear"))) {
                    countryTypes.add(c);
                }
            }

            if (terrainTypeTile.getCategory() == TerrainCategory.Ocean || terrainTypeTile.getCategory() == TerrainCategory.River || terrainTypeTile.getCategory() == TerrainCategory.Hill) {
                non_countryTypes.add(c);
            }
        }

        TerrainRandomizer terrainRandomizer = new TerrainRandomizer(countryTypes, non_countryTypes);

        /*
         * create vector to keep track of terrain randomisation 'clumping'
         */
        List<TerrainAtLocation> locations = new ArrayList();

        for (int x = 0; x < mapRect.width; x++) {
            pm.setValue(x);

            for (int y = 0; y < mapRect.height; y++) {
                int rgb = mapBufferedImage.getRGB(x, y);
                FullTerrainTile tile;
                Integer type = rgb2TerrainType.get(rgb);

                if (null == type) {
                    throw new NullPointerException("There is no terrain type mapped to rgb value " + rgb + " at location " + x + ", " + y);
                }

                tile = FullTerrainTile.getInstance(terrainRandomizer.getNewType(type));
                Point2D location = new Point2D(x, y);
                if (countryTypes.contains(tile.getTerrainTypeID())) {
                    locations.add(new TerrainAtLocation(location, tile.getTerrainTypeID()));
                }

                w.setTile(location, tile);
            }
        }

        for (TerrainAtLocation terrainAtLocation : locations) {
            FullTerrainTile tile = FullTerrainTile.getInstance(terrainAtLocation.getType());

            Point2D location = terrainAtLocation.getLocation();
            int val = 3;

            double prob = 0.75;

            // TODO Point2D arigthmetics
            if (w.boundsContain(new Point2D(location.x - val, location.y - val)) && w.boundsContain(new Point2D(location.x + val, location.y + val))) {
                for (int m = location.x - val; m < location.x + val; m++) {
                    for (int n = location.y - val; n < location.y + val; n++) {
                        if (Math.random() > prob) {
                            setTile(new Point2D(m, n), tile);
                        }
                    }
                }
            }
        }
    }

    private static void setTile(Point2D p, Serializable tile) {
        if (!non_countryTypes.contains(((FullTerrainTile) world.getTile(p)).getTerrainTypeID())) {
            world.setTile(p, tile);
        }
    }
}
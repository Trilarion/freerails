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
package freerails.client;

import freerails.client.renderer.TileRenderer;
import freerails.client.renderer.TileRendererList;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.terrain.TerrainType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of TileRendererList, for testing purposes only.
 */

public class QuickRGBTileRendererList implements TileRendererList {

    private static final java.awt.GraphicsConfiguration defaultConfiguration = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    /**
     * @param world
     */
    public QuickRGBTileRendererList(ReadOnlyWorld world) {
        int numberOfTerrainTypes = world.size(SKEY.TERRAIN_TYPES);
        int[] rgbValues = new int[numberOfTerrainTypes];
        Image[] images = new Image[numberOfTerrainTypes];

        for (int i = 0; i < numberOfTerrainTypes; i++) {
            TerrainType t = (TerrainType) world.get(SKEY.TERRAIN_TYPES, i);
            rgbValues[i] = t.getRGB();
            images[i] = createImageFor(t);
            Map<Integer, Integer> rgb2index = new HashMap<>();
            rgb2index.put(t.getRGB(), i);
        }
    }

    /**
     * @param t
     * @return
     */
    public static Image createImageFor(TerrainType t) {
        Image image = defaultConfiguration.createCompatibleImage(ClientConfig.TILE_SIZE, ClientConfig.TILE_SIZE);
        Color c = new Color(t.getRGB());
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, ClientConfig.TILE_SIZE, ClientConfig.TILE_SIZE);
        g.dispose();

        return image;
    }

    /**
     * @param i
     * @return
     */
    public TileRenderer getTileViewWithNumber(int i) {
        throw new UnsupportedOperationException();
    }

    public boolean validate(ReadOnlyWorld world) {
        return true;
    }

}
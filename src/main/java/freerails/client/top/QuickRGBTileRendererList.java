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
 * Created on 28-Apr-2003
 *
 */
package freerails.client.top;

import freerails.client.ClientConstants;
import freerails.client.common.ImageManager;
import freerails.client.renderer.TileRenderer;
import freerails.client.renderer.TileRendererList;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.terrain.TerrainType;

import java.awt.*;
import java.util.HashMap;

/**
 * Simple implementation of TileRendererList, for testing purposes only.
 */
public class QuickRGBTileRendererList implements TileRendererList {

    private static final java.awt.GraphicsConfiguration defaultConfiguration = java.awt.GraphicsEnvironment
            .getLocalGraphicsEnvironment().getDefaultScreenDevice()
            .getDefaultConfiguration();
    private final Image[] images;
    private final HashMap<Integer, Integer> rgb2index = new HashMap<>();
    private final SimpleTileRenderer simpleTileRenderer = new SimpleTileRenderer();

    /**
     * @param w
     */
    public QuickRGBTileRendererList(ReadOnlyWorld w) {
        int numberOfTerrainTypes = w.size(SKEY.TERRAIN_TYPES);
        int[] rgbValues = new int[numberOfTerrainTypes];
        images = new Image[numberOfTerrainTypes];

        for (int i = 0; i < numberOfTerrainTypes; i++) {
            TerrainType t = (TerrainType) w.get(SKEY.TERRAIN_TYPES, i);
            rgbValues[i] = t.getRGB();
            images[i] = createImageFor(t);
            rgb2index.put(t.getRGB(), i);
        }
    }

    /**
     * @param t
     * @return
     */
    public static Image createImageFor(TerrainType t) {
        Image image = defaultConfiguration.createCompatibleImage(
                ClientConstants.TILE_SIZE, ClientConstants.TILE_SIZE);
        Color c = new Color(t.getRGB());
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, ClientConstants.TILE_SIZE, ClientConstants.TILE_SIZE);
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

    /**
     * @param rgb
     * @return
     */
    public TileRenderer getTileViewWithRGBValue(int rgb) {
        Integer i = rgb2index.get(rgb);
        this.simpleTileRenderer.setImage(images[i]);

        return simpleTileRenderer;
    }

    public boolean validate(ReadOnlyWorld world) {
        return true;
    }

    class SimpleTileRenderer implements TileRenderer {
        Image i;

        public SimpleTileRenderer() {
        }

        public void setImage(Image i) {
            this.i = i;
        }

        public Image getDefaultIcon() {
            return i;
        }

        public void renderTile(Graphics g, int renderX, int renderY, int mapX,
                               int mapY, ReadOnlyWorld w) {
            g.drawImage(i, renderX, renderY, null);
        }

        public void dumpImages(ImageManager imageManager) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException();
        }
    }
}
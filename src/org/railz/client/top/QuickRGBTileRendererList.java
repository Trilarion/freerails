/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 28-Apr-2003
 *
 */
package org.railz.client.top;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import org.railz.client.common.ImageManager;
import org.railz.client.renderer.TileRenderer;
import org.railz.client.renderer.TileRendererList;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;


/**
 * Simple implementation of TileRendererList, for testing purposes only.
 *
 * @author Luke
 *
 */
public class QuickRGBTileRendererList implements TileRendererList {
    private int[] rgbValues;
    private BufferedImage[] images;
    private HashMap rgb2index = new HashMap();
    private SimpleTileRenderer simpleTileRenderer = new SimpleTileRenderer();
    private static java.awt.GraphicsConfiguration defaultConfiguration = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                                                     .getDefaultScreenDevice()
                                                                                                     .getDefaultConfiguration();

    public QuickRGBTileRendererList(ReadOnlyWorld w) {
        int numberOfTerrainTypes = w.size(KEY.TERRAIN_TYPES);
        rgbValues = new int[numberOfTerrainTypes];
        images = new BufferedImage[numberOfTerrainTypes];

        for (int i = 0; i < numberOfTerrainTypes; i++) {
            TerrainType t = (TerrainType)w.get(KEY.TERRAIN_TYPES, i);
            rgbValues[i] = t.getRGB();
            images[i] = createImageFor(t);
            rgb2index.put(new Integer(t.getRGB()), new Integer(i));
        }
    }

    public static BufferedImage createImageFor(TerrainType t) {
	BufferedImage image = defaultConfiguration.createCompatibleImage(30,
		30);
        Color c = new Color(t.getRGB());
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, 30, 30);
        g.dispose();

        return image;
    }

    public TileRenderer getTileViewWithNumber(int i) {
        throw new UnsupportedOperationException();
    }

    public TileRenderer getTileViewWithRGBValue(int rgb) {
        Integer i = (Integer)rgb2index.get(new Integer(rgb));
        this.simpleTileRenderer.setImage(images[i.intValue()]);

        return simpleTileRenderer;
    }

    public boolean validate(ReadOnlyWorld world) {
        return true;
    }

    class SimpleTileRenderer implements TileRenderer {
        BufferedImage i;

        public SimpleTileRenderer() {
        }

        public void setImage(BufferedImage i) {
            this.i = i;
        }

        public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
            return 0;
        }

        public int getTileWidth() {
            return 30;
        }

        public int getTileHeight() {
            return 30;
        }

        public BufferedImage getIcon(int x, int y, ReadOnlyWorld w) {
            return i;
        }

        public BufferedImage getDefaultIcon() {
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

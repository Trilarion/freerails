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

import freerails.client.renderer.tile.TileRenderer;
import freerails.client.renderer.tile.TileRendererList;
import freerails.world.world.ReadOnlyWorld;
import freerails.world.WorldConstants;
import freerails.world.terrain.TerrainType;

import java.awt.*;

/**
 * Simple implementation of TileRendererList, for testing purposes only.
 */

public class QuickRGBTileRendererList implements TileRendererList {

    private static final GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    /**
     * @param t
     * @return
     */
    public static Image createImageFor(TerrainType t) {
        Image image = defaultConfiguration.createCompatibleImage(WorldConstants.TILE_SIZE, WorldConstants.TILE_SIZE);
        Color c = new Color(t.getRGB());
        Graphics g = image.getGraphics();
        g.setColor(c);
        g.fillRect(0, 0, WorldConstants.TILE_SIZE, WorldConstants.TILE_SIZE);
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
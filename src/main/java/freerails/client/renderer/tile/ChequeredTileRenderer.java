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
 * ChequeredTileView.java
 *
 */
package freerails.client.renderer.tile;

import freerails.util.ui.ImageManager;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.terrain.TerrainType;

import java.awt.*;
import java.io.IOException;

/**
 * Paints 2 variations of a tile icon a chequered pattern.
 */
public class ChequeredTileRenderer extends AbstractTileRenderer {

    /**
     * @param imageManager
     * @param rgbValues
     * @param tileModel
     * @param world
     * @throws IOException
     */
    public ChequeredTileRenderer(ImageManager imageManager, int[] rgbValues, TerrainType tileModel, ReadOnlyWorld world) throws IOException {
        super(tileModel, rgbValues, world);
        setTileIcons(new Image[2]);
        getTileIcons()[0] = imageManager.getImage(generateRelativeFileName(0));
        getTileIcons()[1] = imageManager.getImage(generateRelativeFileName(1));
    }

    /**
     * @param x
     * @param y
     * @param world
     * @return
     */
    @Override
    public int selectTileIcon(int x, int y, ReadOnlyWorld world) {
        return (x + y) % 2;
    }

    /**
     * @param i
     * @return
     */
    @Override
    protected String generateFileNameNumber(int i) {
        return String.valueOf(i);
    }
}
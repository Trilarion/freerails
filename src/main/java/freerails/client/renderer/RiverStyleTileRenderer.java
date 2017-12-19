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
 * RiverStyleTileIconSelecter.java
 *
 * Created on 07 July 2001, 12:36
 */
package freerails.client.renderer;

import freerails.client.common.BinaryNumberFormatter;
import freerails.client.common.ImageManager;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;

import java.awt.*;
import java.io.IOException;

/**
 * Selects a tile icon to use based on the type of the tiles to the North, East,
 * South and West.
 */
final public class RiverStyleTileRenderer extends
        freerails.client.renderer.AbstractTileRenderer {
    private static final int[] Y_LOOK_AT = {0, 1, 0, -1};

    private static final int[] X_LOOK_AT = {-1, 0, 1, 0};

    /**
     * @param imageManager
     * @param rgbValues
     * @param tileModel
     * @param w
     * @throws IOException
     */
    public RiverStyleTileRenderer(ImageManager imageManager, int[] rgbValues,
                                  TerrainType tileModel, ReadOnlyWorld w) throws IOException {
        super(tileModel, rgbValues, w);
        this.setTileIcons(new Image[16]);

        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            this.getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    /**
     * 666 optimize cache
     *
     * @param x
     * @param w
     * @param y
     * @return
     */
    @Override
    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        int iconNumber = 0;

        for (int i = 0; i < 4; i++) {
            iconNumber = iconNumber << 1;
            iconNumber = iconNumber
                    | checkTile(x + X_LOOK_AT[i], y + Y_LOOK_AT[i], w);
        }

        return iconNumber;
    }

    @Override
    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.getTileIcons().length; i++) {
            imageManager.setImage(generateRelativeFileName(i), this
                    .getTileIcons()[i]);
        }
    }

    /**
     * @param i
     * @return
     */
    @Override
    protected String generateFileNameNumber(int i) {
        return BinaryNumberFormatter.formatWithLowBitOnLeft(i, 4);
    }
}
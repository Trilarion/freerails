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
 * ForestStyleTileView.java
 *
 */
package freerails.client.renderer.tile;

import freerails.util.BinaryNumberFormatter;
import freerails.util.Vector2D;
import freerails.util.ui.ImageManager;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.terrain.TerrainType;

import java.awt.*;
import java.io.IOException;

/**
 * Looks to see whether the tiles to the left and right of the same type when deciding which tile icon to use.
 */
public class ForestStyleTileRenderer extends AbstractTileRenderer {

    // TODO better naming maybe (also express this as Vector2D)
    private static final int[] X_LOOK_AT = {-1, 1};
    private static final int[] Y_LOOK_AT = {0, 0};

    /**
     * @param imageManager
     * @param rgbValues
     * @param tileModel
     * @throws IOException
     */
    public ForestStyleTileRenderer(ImageManager imageManager, int[] rgbValues, TerrainType tileModel) throws IOException {
        super(tileModel, rgbValues, 4);

        for (int i = 0; i < getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    /**
     * @param mapLocation
     * @param world
     * @return
     */
    @Override
    public int selectTileIconIndex(Vector2D mapLocation, ReadOnlyWorld world) {
        int iconNumber = 0;

        for (int i = 0; i < 2; i++) {
            iconNumber = iconNumber | checkTile( Vector2D.add(mapLocation, new Vector2D(X_LOOK_AT[i], Y_LOOK_AT[i])), world);
            iconNumber = iconNumber << 1;
        }

        iconNumber = iconNumber >> 1;

        return iconNumber;
    }

    /**
     * @param i
     * @return
     */
    @Override
    protected String generateFileNameNumber(int i) {
        return BinaryNumberFormatter.format(i, 2);
    }
}
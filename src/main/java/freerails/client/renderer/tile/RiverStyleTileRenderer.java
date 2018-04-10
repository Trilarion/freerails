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
package freerails.client.renderer.tile;

import freerails.util.BinaryNumberFormatter;
import freerails.util.Vec2D;
import freerails.util.ui.ImageManager;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.terrain.TerrainType;

import java.io.IOException;

/**
 * Selects a tile icon to use based on the type of the tiles to the North, East, South and West.
 */
public class RiverStyleTileRenderer extends AbstractTileRenderer {

    // TODO use Vector2D instead (see ForestStyleTileRenderer)
    private static final int[] Y_LOOK_AT = {0, 1, 0, -1};
    private static final int[] X_LOOK_AT = {-1, 0, 1, 0};

    /**
     * @param imageManager
     * @param rgbValues
     * @param tileModel
     * @throws IOException
     */
    public RiverStyleTileRenderer(ImageManager imageManager, int[] rgbValues, TerrainType tileModel) throws IOException {
        super(tileModel, rgbValues, 16);

        for (int i = 0; i < getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    /**
     * TODO optimize cache
     */
    @Override
    public int selectTileIconIndex(Vec2D mapLocation, ReadOnlyWorld world) {
        int iconNumber = 0;

        for (int i = 0; i < 4; i++) {
            iconNumber = iconNumber << 1;
            iconNumber = iconNumber | checkTile(Vec2D.add(mapLocation, new Vec2D(X_LOOK_AT[i], Y_LOOK_AT[i])), world);
        }

        return iconNumber;
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
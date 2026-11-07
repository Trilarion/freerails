/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.terrain.Terrain;
import freerails.util.BinaryNumberFormatter;
import freerails.util.IntPoint2D;
import freerails.util.ui.ImageManager;
import freerails.model.world.UnmodifiableWorld;

import java.io.IOException;
import java.util.List;

/**
 * Selects a tile icon to use based on the type of the tiles to the North, East, South and West.
 */
public class RiverStyleTileRenderer extends AbstractTileRenderer {

    // TODO use IntPoint2D instead (see ForestStyleTileRenderer)
    private static final int[] Y_LOOK_AT = {0, 1, 0, -1};
    private static final int[] X_LOOK_AT = {-1, 0, 1, 0};

    /**
     * Creates a river style tile renderer instance.
     *
     * @param imageManager the image manager parameter
     * @param typesTreatedAsSame the types treated as same parameter
     * @param tileModel the tile model parameter
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    public RiverStyleTileRenderer(ImageManager imageManager, List<Integer> typesTreatedAsSame, Terrain tileModel) throws IOException {
        super(tileModel, typesTreatedAsSame, 16);

        for (int i = 0; i < getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    /**
     * TODO optimize cache
     */
    @Override
    public int selectTileIconIndex(IntPoint2D mapLocation, UnmodifiableWorld world) {
        int iconNumber = 0;

        for (int i = 0; i < 4; i++) {
            iconNumber = iconNumber << 1;
            iconNumber = iconNumber | checkTile(IntPoint2D.add(mapLocation, new IntPoint2D(X_LOOK_AT[i], Y_LOOK_AT[i])), world);
        }

        return iconNumber;
    }

    /**
     * Performs the generate file name number operation.
     *
     * @param i the index of the item
     * @return the file name number produced by this method
     */
    @Override
    protected String generateFileNameNumber(int i) {
        return BinaryNumberFormatter.formatWithLowBitOnLeft(i, 4);
    }
}
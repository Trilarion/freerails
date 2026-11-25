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
import java.util.Set;

/**
 * Selects a tile icon to use based on the type of the tiles to the North, East, South and West.
 */
public class RiverStyleTileRenderer extends AbstractTileRenderer {

    /**
     * Offsets of the neighbours that are checked, in the order left, below,
     * right, above. The first neighbour becomes the highest bit of the icon
     * index. Because file names list the low bit first, the digits of a file
     * name such as {@code Ocean_abcd} stand for above, right, below and left.
     * Do not reorder without renaming the images.
     */
    private static final IntPoint2D[] NEIGHBOURS = {
            new IntPoint2D(-1, 0), new IntPoint2D(0, 1), new IntPoint2D(1, 0), new IntPoint2D(0, -1)};

    /**
     * Creates a river style tile renderer instance.
     *
     * @param imageManager the image manager parameter
     * @param typesTreatedAsSame the types treated as same parameter
     * @param tileModel the tile model parameter
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    public RiverStyleTileRenderer(ImageManager imageManager, Set<Integer> typesTreatedAsSame, Terrain tileModel) throws IOException {
        super(tileModel, typesTreatedAsSame, 16);

        for (int i = 0; i < getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    /**
     * Selects one of the 16 icons from which of the four neighbours connect to
     * this tile, one bit per neighbour (see {@link #NEIGHBOURS}).
     *
     * @param mapLocation the map coordinate
     * @param world the game world being queried
     * @return the icon index from 0 to 15
     */
    @Override
    protected int selectTileIconIndex(IntPoint2D mapLocation, UnmodifiableWorld world) {
        int iconNumber = 0;

        for (IntPoint2D offset : NEIGHBOURS) {
            iconNumber = (iconNumber << 1) | checkTile(IntPoint2D.add(mapLocation, offset), world);
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
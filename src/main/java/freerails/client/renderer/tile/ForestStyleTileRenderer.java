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
 * ForestStyleTileView.java
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
 * Chooses the icon from whether the tiles to the left and right connect to
 * this tile.
 * <p>
 * The left neighbour is the high bit of the icon index and the right
 * neighbour the low bit. File names list the high bit first, so the digits of
 * a file name such as {@code Hills_ab} stand for left and right.
 */
public class ForestStyleTileRenderer extends AbstractTileRenderer {

    private static final IntPoint2D SHIFT_LEFT = new IntPoint2D(-1, 0);
    private static final IntPoint2D SHIFT_RIGHT = new IntPoint2D(1, 0);

    /**
     * Creates a forest style tile renderer instance.
     *
     * @param imageManager the image manager parameter
     * @param typesTreatedAsSame the types treated as same parameter
     * @param tileModel the tile model parameter
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    public ForestStyleTileRenderer(ImageManager imageManager, Set<Integer> typesTreatedAsSame, Terrain tileModel) throws IOException {
        super(tileModel, typesTreatedAsSame, 4);

        for (int i = 0; i < getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    /**
     * Selects one of the 4 icons from whether the left and right neighbours
     * connect to this tile.
     *
     * @param mapLocation the map coordinate
     * @param world the game world being queried
     * @return the icon index from 0 to 3
     */
    @Override
    protected int selectTileIconIndex(IntPoint2D mapLocation, UnmodifiableWorld world) {
        return (checkTile(IntPoint2D.add(mapLocation, SHIFT_LEFT), world) << 1)
                | checkTile(IntPoint2D.add(mapLocation, SHIFT_RIGHT), world);
    }

    /**
     * Returns the icon index as two binary digits, high bit first, for use in
     * the image file name.
     *
     * @param i the icon index
     * @return the file name suffix, for example {@code 10}
     */
    @Override
    protected String generateFileNameNumber(int i) {
        return BinaryNumberFormatter.format(i, 2);
    }
}
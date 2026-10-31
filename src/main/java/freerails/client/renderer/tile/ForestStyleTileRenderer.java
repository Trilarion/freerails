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
import freerails.util.Vector2D;
import freerails.util.ui.ImageManager;
import freerails.model.world.UnmodifiableWorld;

import java.io.IOException;
import java.util.List;

/**
 * Looks to see whether the tiles to the left and right of the same type when deciding which tile icon to use.
 */
public class ForestStyleTileRenderer extends AbstractTileRenderer {

    private static final Vector2D SHIFT_LEFT = new Vector2D(-1, 0);
    private static final Vector2D SHIFT_RIGHT = new Vector2D(1, 0);

    /**
     * Creates a forest style tile renderer instance.
     *
     * @param imageManager the image manager parameter
     * @param typesTreatedAsSame the types treated as same parameter
     * @param tileModel the tile model parameter
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    public ForestStyleTileRenderer(ImageManager imageManager, List<Integer> typesTreatedAsSame, Terrain tileModel) throws IOException {
        super(tileModel, typesTreatedAsSame, 4);

        for (int i = 0; i < getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    /**
     * Selects the tile icon index.
     *
     * @param mapLocation the map coordinate
     * @param world the game world being queried
     * @return the tile icon index produced by this method
     */
    @Override
    public int selectTileIconIndex(Vector2D mapLocation, UnmodifiableWorld world) {
        int iconNumber = 0;

        // shift left
        iconNumber = iconNumber | checkTile(Vector2D.add(mapLocation, SHIFT_LEFT), world);
        iconNumber = iconNumber << 1;

        // shift right
        iconNumber = iconNumber | checkTile(Vector2D.add(mapLocation, SHIFT_RIGHT), world);

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
        return BinaryNumberFormatter.format(i, 2);
    }
}
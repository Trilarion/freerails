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
import freerails.util.Vector2D;
import freerails.util.ui.ImageManager;
import freerails.model.world.UnmodifiableWorld;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Paints a tile for which there only one tile icon.
 */
public class StandardTileRenderer extends AbstractTileRenderer {

    /**
     * Creates a standard tile renderer instance.
     *
     * @param imageManager the image manager parameter
     * @param typesTreatedAsSame the types treated as same parameter
     * @param terrainType the terrain type parameter
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    public StandardTileRenderer(ImageManager imageManager, List<Integer> typesTreatedAsSame, Terrain terrainType) throws IOException {
        super(terrainType, typesTreatedAsSame, 1);
        getTileIcons()[0] = imageManager.getImage(generateFilename());
    }

    /**
     * Generates the filename of this terrain type's tile image.
     * @return the terrain tile image filename
     */
    private String generateFilename() {
        return "terrain" + File.separator + getTerrainTypeName() + ".png";
    }

    /**
     * Selects the tile icon index.
     * @param mapLocation the map location
     * @param world the world
     * @return the index of the selected tile icon
     */
    @Override
    public int selectTileIconIndex(Vector2D mapLocation, UnmodifiableWorld world) {
        return 0;
    }

    /**
     * Performs the generate file name number operation.
     *
     * @param i the index of the item
     * @return the file name number produced by this method
     */
    @Override
    protected String generateFileNameNumber(int i) {
        throw new UnsupportedOperationException();
    }
}
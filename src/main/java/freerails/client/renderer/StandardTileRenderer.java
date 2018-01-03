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
package freerails.client.renderer;

import freerails.client.common.ImageManager;
import freerails.world.ReadOnlyWorld;
import freerails.world.terrain.TerrainType;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Paints a tile for which there only one tile icon.
 */
final public class StandardTileRenderer extends
        freerails.client.renderer.AbstractTileRenderer {

    /**
     * @param imageManager
     * @param rgbValues
     * @param tileModel
     * @param w
     * @throws IOException
     */
    public StandardTileRenderer(ImageManager imageManager, int[] rgbValues,
                                TerrainType tileModel, ReadOnlyWorld w) throws IOException {
        super(tileModel, rgbValues, w);
        this.setTileIcons(new Image[1]);
        this.getTileIcons()[0] = imageManager.getImage(generateFilename());
    }

    /**
     * @param typeName
     * @return
     */
    public static String generateFilename(String typeName) {
        return "terrain" + File.separator + typeName + ".png";
    }

    private String generateFilename() {
        return generateFilename(this.getTerrainType());
    }

    /**
     * @param i
     * @return
     */
    @Override
    protected String generateFileNameNumber(int i) {
        throw new UnsupportedOperationException();
    }
}
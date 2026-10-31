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

package freerails.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Rendering dimensions declared by the active tile set.
 *
 * @param tilePixelSize number of pixels along each tile edge
 */
public record TileSetMetadata(int tilePixelSize) {

    private static final String DEFAULT_RESOURCE = ClientConstants.GRAPHICS_PATH + "tile-set.properties";

    /**
     * Creates a tile set metadata instance.
     * @param tilePixelSize the tile pixel size
     */
    public TileSetMetadata {
        if (tilePixelSize <= 0) {
            throw new IllegalArgumentException("tilePixelSize must be positive: " + tilePixelSize);
        }
    }

    /** Loads the bundled tile-set metadata. */
    public static TileSetMetadata loadDefault() {
        Properties properties = new Properties();
        try (InputStream input = TileSetMetadata.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing tile-set metadata resource: " + DEFAULT_RESOURCE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load tile-set metadata: " + DEFAULT_RESOURCE, e);
        }

        String pixelSize = properties.getProperty("tile.pixel-size");
        if (pixelSize == null) {
            throw new IllegalStateException("Tile-set metadata has no tile.pixel-size: " + DEFAULT_RESOURCE);
        }
        try {
            return new TileSetMetadata(Integer.parseInt(pixelSize));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid tile.pixel-size in " + DEFAULT_RESOURCE + ": " + pixelSize, e);
        }
    }
}

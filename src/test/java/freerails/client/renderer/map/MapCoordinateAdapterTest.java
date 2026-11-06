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

package freerails.client.renderer.map;

import freerails.client.ClientConstants;
import freerails.client.TileSetMetadata;
import freerails.util.Vector2D;
import junit.framework.TestCase;

/**
 * Tests map coordinate adapter behavior.
 */
public class MapCoordinateAdapterTest extends TestCase {

    /**
     * Tests bundled tile set metadata behavior.
     */
    public void testBundledTileSetMetadata() {
        assertEquals(48, TileSetMetadata.loadDefault().tilePixelSize());
        assertEquals(TileSetMetadata.loadDefault().tilePixelSize(), ClientConstants.TILE_PIXEL_SIZE);
    }

    /**
     * Tests converts tile and fixed point model coordinates to pixels behavior.
     */
    public void testConvertsTileAndFixedPointModelCoordinatesToPixels() {
        assertEquals(new Vector2D(48, 96), MapCoordinateAdapter.tileOrigin(new Vector2D(1, 2)));
        assertEquals(new Vector2D(24, 48), MapCoordinateAdapter.modelToPixel(new Vector2D(50, 100)));
        assertEquals(38, MapCoordinateAdapter.modelDistanceToPixel(80));
    }
}

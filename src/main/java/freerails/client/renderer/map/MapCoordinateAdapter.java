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
import freerails.model.ModelConstants;
import freerails.util.Vec2D;

/** Converts integer map/model coordinates to the active tile set's pixel space. */
public final class MapCoordinateAdapter {

    private MapCoordinateAdapter() {
    }

    /** Returns the pixel origin of the specified map tile. */
    public static Vec2D tileOrigin(Vec2D tileLocation) {
        return Vec2D.multiply(tileLocation, ClientConstants.TILE_PIXEL_SIZE);
    }

    /** Converts a fixed-point model position to pixels relative to the map origin. */
    public static Vec2D modelToPixel(Vec2D modelPosition) {
        return new Vec2D(
                modelToPixel(modelPosition.x()),
                modelToPixel(modelPosition.y()));
    }

    /** Converts a fixed-point model distance to pixels. */
    public static int modelDistanceToPixel(double modelDistance) {
        return (int) Math.round(modelDistance * ClientConstants.TILE_PIXEL_SIZE / ModelConstants.MODEL_UNITS_PER_TILE);
    }

    private static int modelToPixel(int modelCoordinate) {
        return modelDistanceToPixel(modelCoordinate);
    }
}

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

package freerails.client.renderer.tile;

import freerails.model.world.UnmodifiableWorld;

import java.util.Map;

/**
 * Tile renderers keyed by their stable terrain IDs.
 */
public final class TileRendererList {

    private final Map<Integer, TileRenderer> tileRenderer;

    /**
     * Creates a tile renderer list instance.
     *
     * @param tileRenderer the tile renderers keyed by terrain ID
     */
    public TileRendererList(Map<Integer, TileRenderer> tileRenderer) {
        this.tileRenderer = tileRenderer;
    }

    /**
     * Returns the tile renderer for a terrain type.
     *
     * @param terrainId stable ID of the terrain type
     * @return the tile renderer for the terrain type
     * @throws IllegalArgumentException if no renderer exists for the terrain ID
     */
    public TileRenderer getTileRendererByIndex(int terrainId) {
        TileRenderer renderer = tileRenderer.get(terrainId);
        if (renderer == null) {
            throw new IllegalArgumentException("Unknown terrain ID: " + terrainId);
        }
        return renderer;
    }

    /**
     * Checks whether this list has exactly one renderer for each terrain type
     * of the specified world. The client uses this to decide whether already
     * loaded renderers can be reused for a newly received world.
     *
     * <p>Only terrain IDs are compared; a different terrain set that reuses the
     * same IDs would pass.
     *
     * @param world the world
     * @return whether these renderers cover the world's terrain types
     */
    public boolean validate(UnmodifiableWorld world) {
        return world.getTerrains().size() == tileRenderer.size()
                && world.getTerrains().stream().allMatch(terrain -> tileRenderer.containsKey(terrain.getId()));
    }
}

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
 *  TileViewList.java
 *
 *  Created on 08 August 2001, 17:11
 */
package freerails.client.renderer.tile;

import freerails.model.world.UnmodifiableWorld;

import java.util.Map;

/**
 * Tile renderers keyed by their stable terrain IDs.
 */
public class StandardTileRendererList implements TileRendererList {

    private final Map<Integer, TileRenderer> tileRenderer;

    /**
     * @param tileRenderer
     */
    public StandardTileRendererList(Map<Integer, TileRenderer> tileRenderer) {
        this.tileRenderer = tileRenderer;
    }

    /**
     * @param terrainId stable ID of the terrain type
     * @return
     */
    @Override
    public TileRenderer getTileRendererByIndex(int terrainId) {
        TileRenderer renderer = tileRenderer.get(terrainId);
        if (renderer == null) {
            throw new IllegalArgumentException("Unknown terrain ID: " + terrainId);
        }
        return renderer;
    }

    @Override
    public boolean validate(UnmodifiableWorld world) {
        return world.getTerrains().size() == tileRenderer.size()
                && world.getTerrains().stream().allMatch(terrain -> tileRenderer.containsKey(terrain.getId()));
    }
}
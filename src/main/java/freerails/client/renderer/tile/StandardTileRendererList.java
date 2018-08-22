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
 *  TileViewList.java
 *
 *  Created on 08 August 2001, 17:11
 */
package freerails.client.renderer.tile;

import freerails.model.world.UnmodifiableWorld;

import java.util.List;

/**
 * A list of TileRenderer stored in an array and created from an ArrayList.
 */
public class StandardTileRendererList implements TileRendererList {

    private final List<TileRenderer> tileRenderer;

    /**
     * @param tileRenderer
     */
    public StandardTileRendererList(List<TileRenderer> tileRenderer) {
        this.tileRenderer = tileRenderer;
    }

    /**
     * @param index
     * @return
     */
    @Override
    public TileRenderer getTileRendererByIndex(int index) {
        return tileRenderer.get(index);
    }

    @Override
    public boolean validate(UnmodifiableWorld world) {
        // There should a TileRenderer for each terrain type.
        return world.getTerrains().size() == tileRenderer.size();
    }
}
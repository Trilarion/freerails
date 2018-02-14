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

import freerails.client.renderer.tile.TileRenderer;
import freerails.client.renderer.tile.TileRendererList;
import freerails.world.world.ReadOnlyWorld;
import freerails.world.SKEY;

import java.util.List;

/**
 * A list of TileRenderer stored in an array and created from an ArrayList.
 */
public final class TileRendererListImpl implements TileRendererList {
    private final TileRenderer[] tiles;

    /**
     * @param t
     */
    public TileRendererListImpl(List<TileRenderer> t) {
        tiles = new TileRenderer[t.size()];

        for (int i = 0; i < t.size(); i++) {
            tiles[i] = t.get(i);
        }
    }

    /**
     * @param i
     * @return
     */
    public TileRenderer getTileViewWithNumber(int i) {
        return tiles[i];
    }

    public boolean validate(ReadOnlyWorld world) {
        // There should a TileRenderer for each terrain type.
        return world.size(SKEY.TERRAIN_TYPES) == tiles.length;
    }
}
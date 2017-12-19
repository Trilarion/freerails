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
package freerails.client.renderer;

import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;

import java.util.ArrayList;

/**
 * A list of TileRenderers stored in an array and created from an ArrayList.
 *
 */
final public class TileRendererListImpl implements TileRendererList {
    private final TileRenderer[] tiles;

    /**
     *
     * @param t
     */
    public TileRendererListImpl(ArrayList<TileRenderer> t) {
        tiles = new TileRenderer[t.size()];

        for (int i = 0; i < t.size(); i++) {
            tiles[i] = t.get(i);
        }
    }

    /**
     *
     * @param i
     * @return
     */
    public TileRenderer getTileViewWithNumber(int i) {
        return tiles[i];
    }

    public boolean validate(ReadOnlyWorld w) {
        // There should a TileRenderer for each terrain type.
        return w.size(SKEY.TERRAIN_TYPES) == tiles.length;
    }
}
/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
*  TileViewList.java
*
*  Created on 08 August 2001, 17:11
*/
package jfreerails.client.renderer;

import java.util.ArrayList;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;


/**
*@author           Luke Lindsay
*           09 October 2001
* public    class TileViewListImplrsion
*/
final public class TileRendererListImpl implements TileRendererList {
    private TileRenderer[] tiles;

    public TileRenderer getTileViewWithNumber(int i) {
        return tiles[i];
    }

    public TileRendererListImpl(ArrayList t) {
        tiles = new TileRenderer[t.size()];

        for (int i = 0; i < t.size(); i++) {
            tiles[i] = (TileRenderer)t.get(i);
        }
    }

    public boolean validate(ReadOnlyWorld w) {
        boolean okSoFar = true;

        for (int i = 0; i < w.size(KEY.TERRAIN_TYPES); i++) {
            TerrainType terrainType = (TerrainType)w.get(KEY.TERRAIN_TYPES, i);
        }

        return okSoFar;
    }
}
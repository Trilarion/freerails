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

/*
*  TileViewList.java
*
*  Created on 08 August 2001, 17:11
*/
package jfreerails.client.renderer;
import java.util.HashMap;

import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
*@author           Luke Lindsay
*           09 October 2001
* public    class TileViewListImplrsion
*/

final public class TileRendererListImpl implements TileRendererList {

	private HashMap tiles;

	public TileRenderer getTileViewWithRGBValue(int rgb) {
		return (TileRenderer) tiles.get(new Integer(rgb));
	}

	public TileRenderer getTileViewWithNumber() {
		return null;
	}

	public int getLength() {
		return tiles.size();
	}

	public boolean TestRGBValue(int rgb) {
		return tiles.containsKey(new Integer(rgb));
	}

	public boolean TestTileViewNumber() {
		return false;
	}

	public TileRendererListImpl(HashMap t) {
		tiles = t;
	}

	public java.util.Iterator getIterator() {
		return tiles.values().iterator();
	}

	public boolean validate(World w) {
		
		boolean okSoFar = true;
		for(int i = 0; i < w.size(KEY.TERRAIN_TYPES); i++){
			TerrainType terrainType = (TerrainType) w.get(KEY.TERRAIN_TYPES, i);
			if (!tiles.containsKey(new Integer(terrainType.getRGB()))) {
				okSoFar= false;
				System.out.println("No tile view for the following tile type: "+terrainType.getTerrainTypeName());
			}
		}
		return okSoFar;

	}
}

/*
*  TileViewList.java
*
*  Created on 08 August 2001, 17:11
*/
package jfreerails.client.renderer;
import java.util.HashMap;
import java.util.Iterator;

import jfreerails.world.terrain.TerrainTileTypesList;
import jfreerails.world.terrain.TerrainType;

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

	public boolean validate(TerrainTileTypesList terrainTypes) {
		Iterator iterator = terrainTypes.getIterator();
		boolean okSoFar = true;
		while (iterator.hasNext()) {
			TerrainType terrainType = (TerrainType) iterator.next();
			if (!tiles.containsKey(new Integer(terrainType.getRGB()))) {
				okSoFar= false;
				System.out.println("No tile view for the following tile type: "+terrainType.getTerrainTypeName());
			}
		}
		return okSoFar;

	}
}
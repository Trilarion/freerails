/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package jfreerails.world.terrain;

import jfreerails.world.common.Tile;

/**
 * @version 	1.0
 */
public interface TerrainTile extends Tile {
	String getTypeName();
	TerrainType getTerrainType();
	int terrainRgb();

}

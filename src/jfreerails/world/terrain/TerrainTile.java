/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package jfreerails.world.terrain;

import jfreerails.world.tilemap.*;

/**
 * @version 	1.0
 * @author
 */
public interface TerrainTile extends Tile {
	public String getTypeName();
	public TerrainType getTerrainType();

}

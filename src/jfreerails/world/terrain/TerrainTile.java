package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;

/**
 * Defines the interface of a terrain tile.
 * 
 * @author Luke
 */
public interface TerrainTile extends FreerailsSerializable {
	int getTerrainTypeID();
}
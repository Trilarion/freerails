package freerails.world.terrain;

import freerails.world.common.FreerailsSerializable;

/**
 * Defines the interface of a terrain tile.
 *
 * @author Luke
 */
public interface TerrainTile extends FreerailsSerializable {
    int getTerrainTypeID();
}
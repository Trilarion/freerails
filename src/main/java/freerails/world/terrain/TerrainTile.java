package freerails.world.terrain;

import freerails.world.FreerailsSerializable;

/**
 * Defines the interface of a terrain tile.
 *
 */
public interface TerrainTile extends FreerailsSerializable {

    /**
     *
     * @return
     */
    int getTerrainTypeID();
}
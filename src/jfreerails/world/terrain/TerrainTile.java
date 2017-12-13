package jfreerails.world.terrain;

import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.FreerailsTile;

/**
 * Defines the interface of a terrain tile.
 */
public interface TerrainTile {
    /**
     * @return an index into the TERRAIN_TYPES table
     */
    int getTerrainTypeNumber();

    /**
     * @return the land value of a tile, excluding any buildings or track.
     */
    public long getTerrainValue(ReadOnlyWorld w, int x, int y);
}

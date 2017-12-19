package freerails.server;

import freerails.world.top.World;

/**
 * This interface defines a method to add the terrain types to the world.
 *
 */
public interface TileSetFactory {

    /**
     *
     * @param w
     */
    void addTerrainTileTypesList(World w);
}
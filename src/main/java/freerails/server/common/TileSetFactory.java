package freerails.server.common;

import freerails.world.top.World;

/**
 * This interface defines a method to add the terrain types to the world.
 *
 * @author Luke Lindsay 09 October 2001
 */
public interface TileSetFactory {
    void addTerrainTileTypesList(World w);
}
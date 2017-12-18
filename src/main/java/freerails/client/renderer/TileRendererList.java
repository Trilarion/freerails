package freerails.client.renderer;

import freerails.world.top.ReadOnlyWorld;

/**
 * A list of TileRenderers.
 *
 * @author Luke Lindsay 09 October 2001
 */
public interface TileRendererList {

    /**
     *
     * @param i
     * @return
     */
    TileRenderer getTileViewWithNumber(int i);

    /**
     * Checks whether this tile view list has tile views for all the terrain
     * types in the specifed list.
     * @param world
     * @return 
     */
    boolean validate(ReadOnlyWorld world);
}
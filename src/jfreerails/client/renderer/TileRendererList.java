package jfreerails.client.renderer;

import jfreerails.world.top.ReadOnlyWorld;


/**
*  Description of the Interface
*
*@author     Luke Lindsay
*     09 October 2001
*/
public interface TileRendererList {
    TileRenderer getTileViewWithNumber(int i);

    /** Checks whether this tile view list has tile views for all
     * the terrain types in the specifed list.
     */
    boolean validate(ReadOnlyWorld world);
}
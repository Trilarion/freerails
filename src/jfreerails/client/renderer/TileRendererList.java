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

    //boolean TestRGBValue(int rgb);
    boolean TestTileViewNumber();

    int getLength();

    //Iterator getIterator();
    //TileRenderer getTileViewWithRGBValue(int rgb);

    /** Checks whether this tile view list has tile views for all
     * the terrain types in the specifed list.
     */
    boolean validate(ReadOnlyWorld world);
}
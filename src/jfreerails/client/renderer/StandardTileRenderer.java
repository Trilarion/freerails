
/*
* StandardTileIconSelecter.java
*
* Created on 07 July 2001, 12:11
*/
package jfreerails.client.renderer;

import jfreerails.world.terrain.TerrainType;

/**
*
* @author  Luke Lindsay
*/


final public class StandardTileRenderer extends jfreerails.client.renderer.AbstractTileRenderer {

    /** Creates new StandardTileIconSelecter */

    public StandardTileRenderer( jfreerails.client.common.ImageSplitter imageSplitter, int[] rgbValues, TerrainType tileModel )  {
        imageSplitter.setTransparencyToOPAQUE();
        tileIcons = new java.awt.Image[ 1 ];
        tileIcons[ 0 ] = imageSplitter.getTileFromSubGrid( 0, 0 );
        super.rgbValues = rgbValues;
        super.tileModel = tileModel;
    }
}

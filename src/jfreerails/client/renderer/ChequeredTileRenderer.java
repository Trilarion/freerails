
/*
* ChequeredTileView.java
*
* Created on 07 July 2001, 14:25
*/
package jfreerails.client.renderer;

import jfreerails.world.terrain.TerrainType;

/**
*
* @author  Luke Lindsay
*/


final public class ChequeredTileRenderer extends jfreerails.client.renderer.AbstractTileRenderer {

    public int selectTileIcon( int x, int y, jfreerails.world.terrain.TerrainMap map ) {
        return ( x + y ) % 2;
    }

    /** Creates new ChequeredTileView */

    public ChequeredTileRenderer( jfreerails.client.common.ImageSplitter imageSplitter, int[] rgbValues, TerrainType tileModel )  {
        imageSplitter.setTransparencyToOPAQUE();
        tileIcons = new java.awt.Image[ 2 ];
        for( int  i = 0;i < tileIcons.length;i++ ) {
            tileIcons[ i ] = imageSplitter.getTileFromSubGrid( 0 + i, 0 );
        }
        super.rgbValues = rgbValues;
        super.tileModel = tileModel;
    }
}


/*
* RiverStyleTileIconSelecter.java
*
* Created on 07 July 2001, 12:36
*/
package jfreerails.client.renderer;

import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.World;

/**
*
* @author  Luke Lindsay
*/


final public class RiverStyleTileRenderer extends jfreerails.client.renderer.AbstractTileRenderer {

    private static final int[] Y_LOOK_AT =  {
        0, 1, 0, -1
    };

    private static final int[] X_LOOK_AT =  {
        -1, 0, 1, 0
    };

    /** Creates new RiverStyleTileView */

    public RiverStyleTileRenderer( jfreerails.client.common.ImageSplitter imageSplitter, int[] rgbValues, TerrainType tileModel )  {
        imageSplitter.setTransparencyToOPAQUE();
        tileIcons = new java.awt.Image[ 16 ];
        for( int  i = 0;i < tileIcons.length;i++ ) {
            tileIcons[ i ] = imageSplitter.getTileFromSubGrid( 0 + i, 0 );
        }
        super.rgbValues = rgbValues;
        super.tileModel = tileModel;
    }

    public int selectTileIcon( int x, int y, World w ) {
        int  iconNumber = 0;
        for( int  i = 0;i < 4;i++ ) {
            iconNumber = iconNumber << 1;
            iconNumber = iconNumber | checkTile( x + X_LOOK_AT[ i ], y + Y_LOOK_AT[ i ], w );
        }
        return iconNumber;
    }
}

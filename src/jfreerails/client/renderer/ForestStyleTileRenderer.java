
/*
* ForestStyleTileView.java
*
* Created on 07 July 2001, 14:36
*/
package jfreerails.client.renderer;

import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.World;

/**
*
* @author  Luke Lindsay
*/


final public class ForestStyleTileRenderer extends jfreerails.client.renderer.AbstractTileRenderer {

    private static final int[] X_LOOK_AT =  {
        -1, 1
    };

    private static final int[] Y_LOOK_AT =  {
        0, 0
    };

    /** Creates new ForestStyleTileView */

    public ForestStyleTileRenderer( jfreerails.client.common.ImageSplitter imageSplitter, int[] rgbValues, TerrainType tileModel )  {
        imageSplitter.setTransparencyToOPAQUE();
        tileIcons = new java.awt.Image[ 4 ];

        //Grap them in this order so that they display correctly :)
        tileIcons[ 0 ] = imageSplitter.getTileFromSubGrid( 0 + 2, 0 );
        tileIcons[ 1 ] = imageSplitter.getTileFromSubGrid( 0 + 3, 0 );
        tileIcons[ 2 ] = imageSplitter.getTileFromSubGrid( 0 + 1, 0 );
        tileIcons[ 3 ] = imageSplitter.getTileFromSubGrid( 0, 0 );
        super.rgbValues = rgbValues;
        super.tileModel = tileModel;
    }

    public int selectTileIcon( int x, int y, World w ) {
        int  iconNumber = 0;
        for( int  i = 0;i < 2;i++ ) {
            iconNumber = iconNumber | checkTile( x + X_LOOK_AT[ i ], y + Y_LOOK_AT[ i ], w );
            iconNumber = iconNumber << 1;
        }
        iconNumber = iconNumber >> 1;
        return iconNumber;
    }
}


/*
* TileView.java
*
* Created on 04 July 2001, 07:01
*/
package jfreerails.client.renderer;
import java.awt.Image;

import jfreerails.world.terrain.TerrainTile;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.World;


/**
*  This class encapsulates the visible properties of a tile.
* @author  Luke Lindsay
*/


public abstract class AbstractTileRenderer  implements TileRenderer {

    protected int[] rgbValues;

    protected TileIconSelector tileIconSelector;

    protected Image[] tileIcons;

    protected TerrainType tileModel;

    protected int rgb;

    protected  int tileWidth;

    protected  int tileHeight;

    public void renderTile( java.awt.Graphics g, int screenX, int screenY, int mapX, int mapY, World w ) {
        Image  icon = this.getIcon( mapX, mapY, w );
            if( null != icon ) {
                g.drawImage( icon, screenX, screenY, null );
            }

    }

    public int getRGB() {
        return tileModel.getRGB();
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public Image getIcon() {
        return tileIcons[ 0 ];
    }

    public String getTerrainType() {
        return tileModel.getTerrainTypeName();
    }

    public Image getIcon( int x, int y, World w )  {
        int  tile = selectTileIcon( x, y, w );
        if( tileIcons[ tile ] != null ) {
            return tileIcons[ tile ];
        }
        else {
            throw new NullPointerException( "Error in TileView.getIcon: icon no. " + tile + "==null" );
        }
    }

    /*The terrain types that are treated as the same.  E.g. for terrain type
    river; ocean, ports, and other rivers are treated as the same terrain type.
    */

    public int selectTileIcon( int x, int y, World w ) {
        return 0;
    }

    public  void setTileSize( int height, int width ) {
        tileHeight = height;
        tileWidth = width;
    }

    protected int checkTile( int x, int y, World w ) {
        int  match = 1;

        /*0==match!  (0 is assigned to match because of the way the tiles are set up
        *in the image from which they are grabbed.)
        */
        if( ( ( x < w.getMapWidth() ) && ( x >= 0 ) ) && ( y < w.getMapHeight() ) && ( y >= 0 ) ) {
            for( int  i = 0;i < rgbValues.length;i++ ) {
            	TerrainTile tt = (TerrainTile)w.getTile(x, y);
                if( tt.getRGB() == rgbValues[ i ] ) {
                    match = 0;

                //A match
                }
            }
        }
        else {
            match = 0; //A match

        /*If the tile we are checking is off the map, let it be a match.
        This stops coast appearing where the ocean meets the map edge.
        */
        }
        return match;
    }
}

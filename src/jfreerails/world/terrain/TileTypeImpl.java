
/*
*  Tile.java
*
*  Created on 04 July 2001, 06:42
*/
package jfreerails.world.terrain;


/**
*  This class encapsulates the non-visual properites of a terrain tile. E.g.
*  its type-name, its right-of-way cost.
*
*@author     Luke Lindsay
*     16 August 2001
*@version    1.0
*/


final public class TileTypeImpl extends java.lang.Object implements TerrainType {

    private final int rgb;

    private final String terrainType;

    /**
    *@return    The name of this terrain type.
    */

    public String getTerrainTypeName() {
        return terrainType;
    }

    /**
    *  Creates new Tile
    *
    *@param  rgb          The RGB value to be mapped to this terrain type.
    *@param  terrainType  The name of this terrain type. E.g. forest.
    */

    public TileTypeImpl( int rgb, java.lang.String terrainType ) {
        this.terrainType = terrainType;
        this.rgb = rgb;
    }

    /**
    *@return    The RGB value mapped to this terrain type.
    */

    public int getRGB() {
        return rgb;
    }
}

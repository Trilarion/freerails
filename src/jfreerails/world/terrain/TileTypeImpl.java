/*
*  Tile.java
*
*  Created on 04 July 2001, 06:42
*/
package jfreerails.world.terrain;

import jfreerails.world.common.Money;


/**
* Represents a type of terrain.
* Note, this class has been annotated for use with ConstJava.
*
*  @author     Luke Lindsay
*     16 August 2001
*/
final public class TileTypeImpl implements TerrainType {
    private final int rgb;
    private final String terrainCategory;

    public int hashCode() {
        int result;
        result = rgb;
        result = 29 * result + terrainCategory.hashCode();
        result = 29 * result + terrainType.hashCode();
        result = 29 * result + rightOfWay;

        return result;
    }

    private final String terrainType;
    private final int rightOfWay;
    private final Production[] production;
    private final Consumption[] consumption;
    private final Conversion[] conversion;
    private final Money tileBuildCost;

    public String getTerrainTypeName() {
        return terrainType;
    }

    public String getTerrainCategory() {
        return terrainCategory;
    }

    public TileTypeImpl(int rgb, String terrainCategory, String terrainType,
        int rightOfWay, Production[] production, Consumption[] consumption,
        Conversion[] conversion, int tileBuildCost) {
        this.terrainType = terrainType;
        this.terrainCategory = terrainCategory;
        this.rgb = rgb;
        this.rightOfWay = rightOfWay;
        this.production = production;
        this.consumption = consumption;
        this.conversion = conversion;

        if (tileBuildCost > 0) {
            this.tileBuildCost = new Money(tileBuildCost);
        } else {
            this.tileBuildCost = null;
        }
    }

    /** Lets unit tests create terrain types without bothering with all the details.*/
    public TileTypeImpl(String terrainCategory, String terrainType) {
        this.terrainType = terrainType;
        this.terrainCategory = terrainCategory;
        this.rgb = 0;
        this.rightOfWay = 0;
        this.production = new Production[0];
        this.consumption = new Consumption[0];
        this.conversion = new Conversion[0];
        this.tileBuildCost = null;
    }

    /**
    *@return    The RGB value mapped to this terrain type.
    */
    public int getRGB() {
        return rgb;
    }

    public boolean equals(Object o) {
        if (o instanceof TileTypeImpl) {
            TileTypeImpl test = (TileTypeImpl)o;

            if (rgb == test.getRGB() &&
                    terrainType.equals(test.getTerrainTypeName()) &&
                    terrainCategory.equals(test.getTerrainCategory())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int getRightOfWay() {
        return rightOfWay;
    }

    public /*=const*/ Consumption[] getConsumption() {
        return consumption;
    }

    public /*=const*/ Conversion[] getConversion() {
        return conversion;
    }

    public /*=const*/ Production[] getProduction() {
        return production;
    }

    /** Returns the name, replacing any underscores with spaces. */
    public String getDisplayName() {
        return terrainType.replace('_', ' ');
    }

    public Money getBuildCost() {
        return tileBuildCost;
    }
}
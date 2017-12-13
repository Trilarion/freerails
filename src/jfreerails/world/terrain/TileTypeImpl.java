/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
*  Tile.java
*
*  Created on 04 July 2001, 06:42
*/

package jfreerails.world.terrain;

/**
 * This class represents a type of terrain
 *
 * @author     Luke Lindsay
 *     16 August 2001
 * @version    1.0
 */
final public class TileTypeImpl implements TerrainType {
    private final int rgb;
    private final String terrainCategory;
    private final String terrainType;
    private final Production[] production;
    private final Consumption[] consumption;
    private final Conversion[] conversion;
    private final long baseValue;

    public String getTerrainTypeName() {
        return terrainType;
    }

    public String getTerrainCategory() {
        return terrainCategory;
    }

    public TileTypeImpl(int rgb, String terrainCategory, String terrainType,
        Production[] production, Consumption[] consumption,
        Conversion[] conversion, long baseValue) {
        this.terrainType = terrainType;
        this.terrainCategory = terrainCategory;
        this.rgb = rgb;
        this.production = production;
        this.consumption = consumption;
        this.conversion = conversion;
	this.baseValue = baseValue;
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

    public Consumption[] getConsumption() {
        return consumption;
    }

    public Conversion[] getConversion() {
        return conversion;
    }

    public Production[] getProduction() {
        return production;
    }

    /** Returns the name, replacing any underscores with spaces. */
    public String getDisplayName() {
        return this.terrainType.replace('_', ' ');
    }

    public long getBaseValue() {
	return baseValue;
    }
}

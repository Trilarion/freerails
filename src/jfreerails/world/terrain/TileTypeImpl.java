
/*
*  Tile.java
*
*  Created on 04 July 2001, 06:42
*/
package jfreerails.world.terrain;

/**
*	This class represents a type of terrain
*
*@author     Luke Lindsay
*     16 August 2001
*@version    1.0
*/

final public class TileTypeImpl implements TerrainType { 

	private final int rgb;
	private final String terrainCategory;
	private final String terrainType;
	private final int rightOfWay;
	private final Production[] production;
	private final Consumption[] consumption;
	private final Conversion[] conversion;

	public String getTerrainTypeName() {
		return terrainType;
	}

	public String getTerrainCategory() {
		return terrainCategory;
	}

	/*public TileTypeImpl(int rgb, String terrainCategory, String terrainType, int rightOfWay) {
		this.terrainType = terrainType;
		this.terrainCategory = terrainCategory;
		this.rgb = rgb;
		this.rightOfWay = rightOfWay;
		production = new Production[0];
		consumption = new Consumption[0];
		conversion = new Conversion[0];
	}*/
	public TileTypeImpl(
		int rgb,
		String terrainCategory,
		String terrainType,
		int rightOfWay,
		Production[] production,
		Consumption[] consumption,
		Conversion[] conversion) {
		this.terrainType = terrainType;
		this.terrainCategory = terrainCategory;
		this.rgb = rgb;
		this.rightOfWay = rightOfWay;
		this.production = production;
		this.consumption = consumption;
		this.conversion = conversion;
	}

	/**
	*@return    The RGB value mapped to this terrain type.
	*/

	public int getRGB() {
		return rgb;
	}
	public boolean equals(Object o) {
		if (o instanceof TileTypeImpl) {
			TileTypeImpl test = (TileTypeImpl) o;
			if (rgb == test.getRGB()
				&& terrainType.equals(test.getTerrainTypeName())
				&& terrainCategory.equals(test.getTerrainCategory())) {
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
        public String getDisplayName(){
            return this.terrainType.replace('_', ' ');
        }
}

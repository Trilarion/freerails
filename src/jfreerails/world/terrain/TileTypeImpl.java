/*
 *  Tile.java
 *
 *  Created on 04 July 2001, 06:42
 */
package jfreerails.world.terrain;

import java.util.Arrays;

import jfreerails.world.common.Money;

/**
 * Represents a type of terrain.
 * 
 * @author Luke Lindsay 16 August 2001
 */
final public class TileTypeImpl implements TerrainType {
	private static final long serialVersionUID = 4049919380945253945L;

	private final Consumption[] consumption;

	private final Conversion[] conversion;

	private final Production[] production;

	private final int rgb;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TileTypeImpl))
			return false;

		final TileTypeImpl tileType = (TileTypeImpl) o;

		if (rgb != tileType.rgb)
			return false;
		if (rightOfWay != tileType.rightOfWay)
			return false;
		if (!Arrays.equals(consumption, tileType.consumption))
			return false;
		if (!Arrays.equals(conversion, tileType.conversion))
			return false;
		if (!Arrays.equals(production, tileType.production))
			return false;
		if (!terrainCategory.equals(tileType.terrainCategory))
			return false;
		if (!terrainType.equals(tileType.terrainType))
			return false;
		if (tileBuildCost != null ? !tileBuildCost
				.equals(tileType.tileBuildCost)
				: tileType.tileBuildCost != null)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = rgb;
		result = 29 * result + rightOfWay;
		result = 29 * result + terrainCategory.hashCode();
		result = 29 * result + terrainType.hashCode();
		result = 29 * result
				+ (tileBuildCost != null ? tileBuildCost.hashCode() : 0);
		return result;
	}

	private final int rightOfWay;

	private final TerrainType.Category terrainCategory;

	private final String terrainType;

	/**
	 * Cost to build a tile of this terrain type or null if this type is not
	 * buildable.
	 */
	private final Money tileBuildCost;

	public TileTypeImpl(int rgb, TerrainType.Category terrainCategory,
			String terrainType, int rightOfWay, Production[] production,
			Consumption[] consumption, Conversion[] conversion,
			int tileBuildCost) {
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

	/**
	 * Lets unit tests create terrain types without bothering with all the
	 * details.
	 */
	public TileTypeImpl(TerrainType.Category terrainCategory, String terrainType) {
		this.terrainType = terrainType;
		this.terrainCategory = terrainCategory;
		this.rgb = 0;
		this.rightOfWay = 0;
		this.production = new Production[0];
		this.consumption = new Consumption[0];
		this.conversion = new Conversion[0];
		this.tileBuildCost = null;
	}

	public Money getBuildCost() {
		return tileBuildCost;
	}

	public Category getCategory() {
		return terrainCategory;
	}

	public Consumption[] getConsumption() {
		return consumption;
	}

	public Conversion[] getConversion() {
		return conversion;
	}

	/** Returns the name, replacing any underscores with spaces. */
	public String getDisplayName() {
		return terrainType.replace('_', ' ');
	}

	public Production[] getProduction() {
		return production;
	}

	/**
	 * @return The RGB value mapped to this terrain type.
	 */
	public int getRGB() {
		return rgb;
	}

	public int getRightOfWay() {
		return rightOfWay;
	}

	public String getTerrainTypeName() {
		return terrainType;
	}

}
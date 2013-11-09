/*
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

package org.railz.world.terrain;

import org.railz.world.common.FreerailsSerializable;

/**
 * This class represents a type of terrain
 * 
 * @author Luke Lindsay 16 August 2001
 * @version 1.0
 */
public class TerrainType implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8675450928634216268L;
	public static final int CATEGORY_RIVER = 0;
	public static final int CATEGORY_OCEAN = 1;
	public static final int CATEGORY_HILL = 2;
	public static final int CATEGORY_COUNTRY = 3;
	public static final int MAX_CATEGORIES = 3;

	private final int rgb;
	private final int terrainCategory;
	private final String terrainType;
	private final long baseValue;

	/**
	 * A measure of the resistance encountered when entering or leaving a tile
	 * of a different type
	 */
	private final int elevation;

	/**
	 * A measure of the resistance encountered when entering or leaving a tile
	 * of the same type
	 */
	private final int roughness;

	/** @return roughness of terrain measured in gradient of 100ths of a % */
	public int getRoughness() {
		return roughness;
	}

	/**
	 * @return elevation of terrain from sea-level, measured in required gradient
	 *         in 100ths of a %
	 */
	public int getElevation() {
		return elevation;
	}

	public String getTerrainTypeName() {
		return terrainType;
	}

	public int getTerrainCategory() {
		return terrainCategory;
	}

	/**
	 * @param terrainType
	 *            Resource key to the name of the terrain type
	 */
	public TerrainType(int rgb, int terrainCategory, String terrainType,
			long baseValue, int elevation, int roughness) {
		this.terrainType = terrainType;
		this.terrainCategory = terrainCategory;
		this.rgb = rgb;
		this.baseValue = baseValue;
		this.elevation = elevation;
		this.roughness = roughness;
	}

	/**
	 * @return The RGB value mapped to this terrain type.
	 */
	public int getRGB() {
		return rgb;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TerrainType) {
			TerrainType test = (TerrainType) o;

			if (rgb == test.getRGB()
					&& terrainType.equals(test.getTerrainTypeName())
					&& terrainCategory == test.getTerrainCategory()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Returns the name, replacing any underscores with spaces.
	 */
	public String getDisplayName() {
		return this.terrainType.replace('_', ' ');
	}

	public long getBaseValue() {
		return baseValue;
	}
}

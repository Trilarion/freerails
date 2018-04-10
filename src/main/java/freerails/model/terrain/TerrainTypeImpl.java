/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.model.terrain;

import freerails.model.finances.Money;
import freerails.util.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Represents a type of terrain.
 */
public class TerrainTypeImpl implements TerrainType {

    // TODO why is TerrainType and TerrainTypeImplementation separate?
    private static final long serialVersionUID = 4049919380945253945L;

    /**
     * Consumption, conversion and production will not be modified
     */
    private final List<TileConsumption> consumption;
    private final List<TileConversion> conversion;
    private final List<TileProduction> production;
    private final int rgb;
    private final int rightOfWay;
    private final TerrainCategory terrainCategory;
    private final String terrainType;
    /**
     * Cost to build a tile of this terrain type or null if this type is not
     * buildable.
     */
    private final Money tileBuildCost;

    /**
     * @param rgb
     * @param terrainCategory
     * @param terrainType
     * @param rightOfWay
     * @param tileProduction
     * @param tileConsumption
     * @param tileConversion
     * @param tileBuildCost
     */
    public TerrainTypeImpl(int rgb, TerrainCategory terrainCategory, String terrainType, int rightOfWay, List<TileProduction> tileProduction, List<TileConsumption> tileConsumption, List<TileConversion> tileConversion, int tileBuildCost) {
        this.terrainType = terrainType;
        this.terrainCategory = terrainCategory;
        this.rgb = rgb;
        this.rightOfWay = rightOfWay;
        production = Utils.verifyUnmodifiable(tileProduction);
        consumption = Utils.verifyUnmodifiable(tileConsumption);
        conversion = Utils.verifyUnmodifiable(tileConversion);

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
    public TerrainTypeImpl(TerrainCategory terrainCategory, String terrainType) {
        this.terrainType = terrainType;
        this.terrainCategory = terrainCategory;
        rgb = 0;
        rightOfWay = 0;
        production = Collections.emptyList();
        consumption = Collections.emptyList();
        conversion = Collections.emptyList();
        tileBuildCost = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TerrainTypeImpl)) return false;

        final TerrainTypeImpl tileType = (TerrainTypeImpl) obj;

        if (rgb != tileType.rgb) return false;
        if (rightOfWay != tileType.rightOfWay) return false;
        if (!consumption.equals(tileType.consumption)) return false;
        if (!conversion.equals(tileType.conversion)) return false;
        if (!production.equals(tileType.production)) return false;
        if (terrainCategory != tileType.terrainCategory) return false;
        if (!terrainType.equals(tileType.terrainType)) return false;
        return tileBuildCost != null ? tileBuildCost.equals(tileType.tileBuildCost) : tileType.tileBuildCost == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = consumption.hashCode();
        result = 29 * result + conversion.hashCode();
        result = 29 * result + production.hashCode();
        result = 29 * result + rgb;
        result = 29 * result + rightOfWay;
        result = 29 * result + terrainCategory.hashCode();
        result = 29 * result + terrainType.hashCode();
        result = 29 * result + (tileBuildCost != null ? tileBuildCost.hashCode() : 0);
        return result;
    }

    /**
     * @return
     */
    public Money getBuildCost() {
        return tileBuildCost;
    }

    /**
     * @return
     */
    public TerrainCategory getCategory() {
        return terrainCategory;
    }

    /**
     * @return
     */
    public List<TileConsumption> getConsumption() {
        return consumption;
    }

    /**
     * @return
     */
    public List<TileConversion> getConversion() {
        return conversion;
    }

    /**
     * Returns the name, replacing any underscores with spaces.
     */
    public String getDisplayName() {
        return terrainType.replace('_', ' ');
    }

    /**
     * @return
     */
    public List<TileProduction> getProduction() {
        return production;
    }

    /**
     * @return The RGB value mapped to this terrain type.
     */
    public int getRGB() {
        return rgb;
    }

    /**
     * @return
     */
    public int getRightOfWay() {
        return rightOfWay;
    }

    /**
     * @return
     */
    public String getTerrainTypeName() {
        return terrainType;
    }

}
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

package freerails.world.terrain;

import freerails.util.ImList;
import freerails.world.finances.Money;

/**
 * Represents a type of terrain.
 */
public final class TileTypeImpl implements TerrainType {

    // TODO why is TerrainType and TerrainTypeImplementation separate?
    private static final long serialVersionUID = 4049919380945253945L;

    private final ImList<Consumption> consumption;
    private final ImList<Conversion> conversion;
    private final ImList<Production> production;
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
     * @param production
     * @param consumption
     * @param conversion
     * @param tileBuildCost
     */
    public TileTypeImpl(int rgb, TerrainCategory terrainCategory,
                        String terrainType, int rightOfWay, Production[] production,
                        Consumption[] consumption, Conversion[] conversion,
                        int tileBuildCost) {
        this.terrainType = terrainType;
        this.terrainCategory = terrainCategory;
        this.rgb = rgb;
        this.rightOfWay = rightOfWay;
        this.production = new ImList<>(production);
        this.consumption = new ImList<>(consumption);
        this.conversion = new ImList<>(conversion);

        if (tileBuildCost > 0) {
            this.tileBuildCost = new Money(tileBuildCost);
        } else {
            this.tileBuildCost = null;
        }
    }

    /**
     * Lets unit tests create terrain types without bothering with all the
     * details.
     *
     * @param terrainCategory
     * @param terrainType
     */
    public TileTypeImpl(TerrainCategory terrainCategory, String terrainType) {
        this.terrainType = terrainType;
        this.terrainCategory = terrainCategory;
        this.rgb = 0;
        this.rightOfWay = 0;
        this.production = new ImList<>();
        this.consumption = new ImList<>();
        this.conversion = new ImList<>();
        this.tileBuildCost = null;
    }

    @Override
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
        if (!consumption.equals(tileType.consumption))
            return false;
        if (!conversion.equals(tileType.conversion))
            return false;
        if (!production.equals(tileType.production))
            return false;
        if (!terrainCategory.equals(tileType.terrainCategory))
            return false;
        if (!terrainType.equals(tileType.terrainType))
            return false;
        return tileBuildCost != null ? tileBuildCost
                .equals(tileType.tileBuildCost) : tileType.tileBuildCost == null;
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
        result = 29 * result
                + (tileBuildCost != null ? tileBuildCost.hashCode() : 0);
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
    public ImList<Consumption> getConsumption() {
        return consumption;
    }

    /**
     * @return
     */
    public ImList<Conversion> getConversion() {
        return conversion;
    }

    /**
     * Returns the name, replacing any underscores with spaces.
     *
     * @return
     */
    public String getDisplayName() {
        return terrainType.replace('_', ' ');
    }

    /**
     * @return
     */
    public ImList<Production> getProduction() {
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
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

import java.io.Serializable;

// TODO are these things all
/**
 * Defines the methods to access the properties of a type of terrains.
 */
public interface TerrainType extends Serializable {

    /**
     * @return
     */
    String getTerrainTypeName();

    /**
     * @return
     */
    TerrainCategory getCategory();

    /**
     * @return
     */
    Money getBuildCost();

    /**
     * @return
     */
    int getRightOfWay();

    /**
     * @return
     */
    int getRGB();

    // TODO replace ImList  with List
    /**
     * @return
     */
    ImList<TileProduction> getProduction();

    /**
     * @return
     */
    ImList<TileConsumption> getConsumption();

    /**
     * @return
     */
    ImList<TileConversion> getConversion();

    /**
     * @return
     */
    String getDisplayName();

}
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
    Category getCategory();

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

    /**
     * @return
     */
    ImList<Production> getProduction();

    /**
     * @return
     */
    ImList<Consumption> getConsumption();

    /**
     * @return
     */
    ImList<Conversion> getConversion();

    /**
     * @return
     */
    String getDisplayName();

    /**
     *
     */
    enum Category implements Serializable {

        /**
         *
         */
        Urban,

        /**
         *
         */
        River,

        /**
         *
         */
        Ocean,

        /**
         *
         */
        Hill,

        /**
         *
         */
        Country,

        /**
         *
         */
        Special,

        /**
         *
         */
        Industry,

        /**
         *
         */
        Resource
    }
}
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

/*
 *
 */
package freerails.world.terrain;

import freerails.util.TestUtils;
import junit.framework.TestCase;

/**
 *
 */
public class TileTypeImplTest extends TestCase {

    /**
     *
     */
    public void testTileTypeImpl() {
        TileProduction[] productions = {new TileProduction(69, 10)};
        TileConsumption[] consumptions = {new TileConsumption(4, 4), new TileConsumption(4, 5)};
        TileConversion[] conversions1 = {new TileConversion(50, 30)};

        TestUtils.assertCloneBySerializationBehavesWell(productions[0]);
        TestUtils.assertCloneBySerializationBehavesWell(consumptions[0]);
        TestUtils.assertCloneBySerializationBehavesWell(conversions1[0]);

        TileTypeImpl tileTypeImpl1 = new TileTypeImpl(0, TerrainCategory.Country, "Grassland", 100, productions, consumptions, conversions1, 10);

        TestUtils.assertCloneBySerializationBehavesWell(tileTypeImpl1);

        TileConversion[] conversions2 = {new TileConversion(5, 30)};
        TileTypeImpl tileTypeImpl2 = new TileTypeImpl(0, TerrainCategory.Country, "Grassland", 100, productions, consumptions, conversions2, 10);

        assertFalse(tileTypeImpl1.equals(tileTypeImpl2));
    }


}

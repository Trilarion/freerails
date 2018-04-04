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
package freerails.model.terrain;

import freerails.util.TestUtils;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class TerrainTypeImplTest extends TestCase {

    /**
     *
     */
    public void testTileTypeImpl() {
        List<TileProduction> productions = List.of(new TileProduction(69, 10));
        List<TileConsumption> consumptions = List.of(new TileConsumption(4, 4), new TileConsumption(4, 5));
        List<TileConversion> conversions1 = List.of(new TileConversion(50, 30));

        TestUtils.assertCloneBySerializationBehavesWell(productions.get(0));
        TestUtils.assertCloneBySerializationBehavesWell(consumptions.get(0));
        TestUtils.assertCloneBySerializationBehavesWell(conversions1.get(0));

        TerrainTypeImpl terrainTypeImpl1 = new TerrainTypeImpl(0, TerrainCategory.Country, "Grassland", 100, productions, consumptions, conversions1, 10);

        TestUtils.assertCloneBySerializationBehavesWell(terrainTypeImpl1);

        List<TileConversion> conversions2 = List.of(new TileConversion(5, 30));
        TerrainTypeImpl terrainTypeImpl2 = new TerrainTypeImpl(0, TerrainCategory.Country, "Grassland", 100, productions, consumptions, conversions2, 10);

        assertFalse(terrainTypeImpl1.equals(terrainTypeImpl2));
    }


}

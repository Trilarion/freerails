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

import freerails.model.cargo.CargoConversion;
import freerails.model.cargo.CargoProductionOrConsumption;
import freerails.model.finances.Money;
import freerails.util.TestUtils;
import freerails.util.Utils;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class TerrainTypeTest extends TestCase {

    /**
     *
     */
    public void testTileTypeImpl() {
        List<CargoProductionOrConsumption> productions = asList(new CargoProductionOrConsumption(69, 10));
        List<CargoProductionOrConsumption> consumptions = asList(new CargoProductionOrConsumption(4, 4), new CargoProductionOrConsumption(4, 5));
        List<CargoConversion> conversions1 = asList(new CargoConversion(50, 30, 1));

        TestUtils.assertCloneBySerializationBehavesWell(productions.get(0));
        TestUtils.assertCloneBySerializationBehavesWell(consumptions.get(0));
        TestUtils.assertCloneBySerializationBehavesWell(conversions1.get(0));

        Terrain terrainType1 = new Terrain(1, "Grassland", TerrainCategory.COUNTRY, new Money(100), new Money(10), productions, conversions1, consumptions);

        TestUtils.assertCloneBySerializationBehavesWell(terrainType1);
    }

    @SafeVarargs
    public static <E> List<E> asList(E... e) {
        Utils.verifyNotNull(e);
        return Collections.unmodifiableList(Arrays.asList(e));
    }


}

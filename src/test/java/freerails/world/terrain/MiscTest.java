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
 * Created on 04-Jul-2005
 *
 */
package freerails.world.terrain;

import freerails.util.Utils;
import junit.framework.TestCase;

import java.io.Serializable;

/**
 *
 */
public class MiscTest extends TestCase {

    /**
     *
     */
    public void testCityModel() {
        City cm1 = new City("London", 20, 70);
        City cm2 = new City("Cardiff", 20, 70);
        testHashCodeAndEquals(cm1);
        testHashCodeAndEquals(cm2);
        assertDifferent(cm1, cm2);
    }

    /**
     *
     */
    public void testTileTypeImpl() {
        Production[] prod = {new Production(69, 10)};
        Consumption[] cons = {new Consumption(4, 4), new Consumption(4, 5)};
        Conversion[] conv = {new Conversion(50, 30)};
        testHashCodeAndEquals(prod[0]);
        testHashCodeAndEquals(cons[0]);
        testHashCodeAndEquals(conv[0]);
        TileTypeImpl tt = new TileTypeImpl(0, TerrainCategory.Country, "Grassland",
                100, prod, cons, conv, 10);
        testHashCodeAndEquals(tt);
        Conversion[] conv2 = {new Conversion(5, 30)};
        TileTypeImpl tt2 = new TileTypeImpl(0, TerrainCategory.Country, "Grassland",
                100, prod, cons, conv2, 10);
        assertFalse(tt.equals(tt2));
    }

    private void testHashCodeAndEquals(Serializable a) {
        Serializable copy = Utils.cloneBySerialisation(a);
        assertEquals(a, a);
        assertEquals(a, copy);
        assertEquals(a.hashCode(), copy.hashCode());
    }

    private void assertDifferent(Object a, Object b) {
        assertFalse(a.equals(b));
        assertFalse(a.hashCode() == b.hashCode());
    }

}

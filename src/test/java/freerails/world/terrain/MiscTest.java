/*
 * Created on 04-Jul-2005
 *
 */
package freerails.world.terrain;

import freerails.util.Utils;
import freerails.world.terrain.TerrainType.Category;
import junit.framework.TestCase;

import java.io.Serializable;

public class MiscTest extends TestCase {

    public void testCityModel() {
        CityModel cm1 = new CityModel("London", 20, 70);
        CityModel cm2 = new CityModel("Cardiff", 20, 70);
        testHashCodeAndEquals(cm1);
        testHashCodeAndEquals(cm2);
        assertDifferent(cm1, cm2);
    }

    public void testTileTypeImpl() {
        Production[] prod = {new Production(69, 10)};
        Consumption[] cons = {new Consumption(4, 4), new Consumption(4, 5)};
        Conversion[] conv = {new Conversion(50, 30)};
        testHashCodeAndEquals(prod[0]);
        testHashCodeAndEquals(cons[0]);
        testHashCodeAndEquals(conv[0]);
        TileTypeImpl tt = new TileTypeImpl(0, Category.Country, "Grassland",
                100, prod, cons, conv, 10);
        testHashCodeAndEquals(tt);
        Conversion[] conv2 = {new Conversion(5, 30)};
        TileTypeImpl tt2 = new TileTypeImpl(0, Category.Country, "Grassland",
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

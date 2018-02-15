package freerails.model.terrain;

import freerails.util.TestUtils;
import freerails.util.Vector2D;
import junit.framework.TestCase;

/**
 *
 */
public class CityTest extends TestCase {

    /**
     *
     */
    public void testHashCodeAndEquals() {
        City city1 = new City("London", new Vector2D(20, 70));
        City city2 = new City("Cardiff", new Vector2D(20, 70));
        TestUtils.assertCloneBySerializationBehavesWell(city1);
        TestUtils.assertCloneBySerializationBehavesWell(city2);
        TestUtils.assertUnequalAndNoHashcodeCollision(city1, city2);
    }
}

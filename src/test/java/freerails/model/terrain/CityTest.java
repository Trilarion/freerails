package freerails.model.terrain;

import freerails.util.TestUtils;
import freerails.util.Vec2D;
import junit.framework.TestCase;

/**
 *
 */
public class CityTest extends TestCase {

    /**
     *
     */
    public void testHashCodeAndEquals() {
        City2 city1 = new City2(1,"London", new Vec2D(20, 70));
        City2 city2 = new City2(2, "Cardiff", new Vec2D(20, 70));
        TestUtils.assertCloneBySerializationBehavesWell(city1);
        TestUtils.assertCloneBySerializationBehavesWell(city2);
        TestUtils.assertUnequalAndNoHashcodeCollision(city1, city2);
    }
}

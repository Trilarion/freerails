package freerails.world.track;

import freerails.world.common.ImPoint;
import freerails.world.common.Step;
import junit.framework.TestCase;

/**
 *
 * @author jkeller1
 */
public class TrackSectionTest extends TestCase {

    /**
     *
     */
    public void testEqualsObject() {
        TrackSection a = new TrackSection(Step.EAST, new ImPoint(10, 5));
        TrackSection b = new TrackSection(Step.WEST, new ImPoint(11, 5));
        assertEquals(a, a);
        assertEquals(b, b);
        assertEquals(a, b);
        assertEquals(b, a);

    }

}

package freerails.world.common;

import junit.framework.TestCase;

/**
 * Junit test for PositionOnTrack.
 * 
 * @author Luke Lindsay
 * 
 */
public class PositionOnTrackTest extends TestCase {
    /**
     * Constructor for PositionOnTrackTest.
     * 
     * @param arg0
     */
    public PositionOnTrackTest(String arg0) {
        super(arg0);
    }

    public void testValidation() {
        assertTrue(PositionOnTrack.MAX_COORDINATE < 70000);
        assertTrue(PositionOnTrack.MAX_COORDINATE > 10000);
        assertEquals(PositionOnTrack.MAX_DIRECTION, 7);

        assertNoException(0, 0, Step.EAST);
        assertNoException(PositionOnTrack.MAX_COORDINATE,
                PositionOnTrack.MAX_COORDINATE, Step.NORTH_WEST);

        assertException(-1, 0, Step.EAST);
        assertException(0, -1, Step.EAST);

        assertException(PositionOnTrack.MAX_COORDINATE + 1,
                PositionOnTrack.MAX_COORDINATE, Step.NORTH_WEST);

        assertException(PositionOnTrack.MAX_COORDINATE,
                PositionOnTrack.MAX_COORDINATE + 1, Step.NORTH_WEST);
    }

    public void testToInt() {
        PositionOnTrack p1 = PositionOnTrack.createComingFrom(10, 20,
                Step.NORTH);
        PositionOnTrack p2 = PositionOnTrack.createComingFrom(10, 30,
                Step.NORTH);

        assertTrue(p1.toInt() != p2.toInt());
    }

    public void testSetValuesFromInt() {
        PositionOnTrack p1 = PositionOnTrack.createComingFrom(10, 20,
                Step.NORTH);

        int i = p1.toInt();
        PositionOnTrack p2 = PositionOnTrack
                .createComingFrom(60, 70, Step.EAST);
        assertTrue(!p1.equals(p2));
        p2.setValuesFromInt(i);

        assertEquals(p1, p2);

        Step v = Step.getInstance(7); // 7 is the
        // maximum
        // vector
        // number.

        p1 = PositionOnTrack.createComingFrom(PositionOnTrack.MAX_COORDINATE,
                PositionOnTrack.MAX_COORDINATE, v);

        i = p1.toInt();
    }

    /*
     * Test for boolean equals(Object)
     */
    public void testEqualsObject() {
        PositionOnTrack p1 = PositionOnTrack.createComingFrom(10, 20,
                Step.NORTH);
        PositionOnTrack p2 = PositionOnTrack.createComingFrom(10, 20,
                Step.NORTH);
        assertEquals(p1, p2);

        p1 = PositionOnTrack.createComingFrom(10, 50, Step.NORTH);
        p2 = PositionOnTrack.createComingFrom(10, 20, Step.NORTH);

        assertTrue(!p1.equals(p2));
    }

    private void assertNoException(int x, int y, Step v) {
        try {
            PositionOnTrack.createComingFrom(x, y, v);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    private void assertException(int x, int y, Step v) {
        try {
            PositionOnTrack.createComingFrom(x, y, v);
            assertTrue(false);
        } catch (Exception e) {
        }
    }

    public void testGetOpposite() {
        PositionOnTrack p1 = PositionOnTrack.createComingFrom(10, 10,
                Step.NORTH);
        PositionOnTrack p2 = p1.getOpposite();
        assertNotNull(p2);

        PositionOnTrack p3 = PositionOnTrack.createComingFrom(10, 11,
                Step.SOUTH);
        assertEquals(p3, p2);
    }
}
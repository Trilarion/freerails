package jfreerails.world.common;

import junit.framework.TestCase;


/**
 * Junit test for PositionOnTrack.
 * @author Luke Lindsay
 *
 */
public class PositionOnTrackTest extends TestCase {
    /**
     * Constructor for PositionOnTrackTest.
     * @param arg0
     */
    public PositionOnTrackTest(String arg0) {
        super(arg0);
    }

    public void testValidation() {
        assertTrue(PositionOnTrack.MAX_COORINATE < 70000);
        assertTrue(PositionOnTrack.MAX_COORINATE > 10000);
        assertEquals(PositionOnTrack.MAX_DIRECTION, 7);

        assertNoException(0, 0, OneTileMoveVector.EAST);
        assertNoException(PositionOnTrack.MAX_COORINATE,
            PositionOnTrack.MAX_COORINATE, OneTileMoveVector.NORTH_WEST);

        assertException(-1, 0, OneTileMoveVector.EAST);
        assertException(0, -1, OneTileMoveVector.EAST);

        assertException(PositionOnTrack.MAX_COORINATE + 1,
            PositionOnTrack.MAX_COORINATE, OneTileMoveVector.NORTH_WEST);

        assertException(PositionOnTrack.MAX_COORINATE,
            PositionOnTrack.MAX_COORINATE + 1, OneTileMoveVector.NORTH_WEST);
    }

    public void testToInt() {
        PositionOnTrack p1 = new PositionOnTrack(10, 20, OneTileMoveVector.NORTH);
        PositionOnTrack p2 = new PositionOnTrack(10, 30, OneTileMoveVector.NORTH);

        assertTrue(p1.toInt() != p2.toInt());
    }

    public void testSetValuesFromInt() {
        PositionOnTrack p1 = new PositionOnTrack(10, 20, OneTileMoveVector.NORTH);

        int i = p1.toInt();
        PositionOnTrack p2 = new PositionOnTrack(60, 70, OneTileMoveVector.EAST);
        assertTrue(!p1.equals(p2));
        p2.setValuesFromInt(i);

        assertEquals(p1, p2);

        OneTileMoveVector v = OneTileMoveVector.getInstance(7); //7 is the maximum vector number.

        p1 = new PositionOnTrack(PositionOnTrack.MAX_COORINATE,
                PositionOnTrack.MAX_COORINATE, v);

        i = p1.toInt();
    }

    /*
     * Test for boolean equals(Object)
     */
    public void testEqualsObject() {
        PositionOnTrack p1 = new PositionOnTrack(10, 20, OneTileMoveVector.NORTH);
        PositionOnTrack p2 = new PositionOnTrack(10, 20, OneTileMoveVector.NORTH);
        assertEquals(p1, p2);

        p1 = new PositionOnTrack(10, 50, OneTileMoveVector.NORTH);
        p2 = new PositionOnTrack(10, 20, OneTileMoveVector.NORTH);

        assertTrue(!p1.equals(p2));
    }

    private void assertNoException(int x, int y, OneTileMoveVector v) {
        try {
            new PositionOnTrack(x, y, v);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    private void assertException(int x, int y, OneTileMoveVector v) {
        try {
            new PositionOnTrack(x, y, v);
            assertTrue(false);
        } catch (Exception e) {
        }
    }

    public void testGetOpposite() {
        PositionOnTrack p1 = new PositionOnTrack(10, 10, OneTileMoveVector.NORTH);
        PositionOnTrack p2 = p1.getOpposite();
        assertNotNull(p2);

        PositionOnTrack p3 = new PositionOnTrack(10, 11, OneTileMoveVector.SOUTH);
        assertEquals(p3, p2);
    }
}
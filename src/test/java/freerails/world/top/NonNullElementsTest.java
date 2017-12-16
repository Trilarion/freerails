/*
 * Created on 15-Apr-2003
 *
 */
package freerails.world.top;

import freerails.world.station.StationModel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.NoSuchElementException;

/**
 * This junit TestCase tests NonNullElements.
 *
 * @author Luke
 */
public class NonNullElementsTest extends TestCase {
    World w;

    StationModel station1;

    StationModel station2;

    StationModel station3;

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite testSuite = new TestSuite(NonNullElementsTest.class);

        return testSuite;
    }

    @Override
    protected void setUp() {
        w = new WorldImpl();
        station1 = new StationModel(10, 20, "Station1", 4, 0);
        station2 = new StationModel(15, 16, "Station2", 4, 1);
        station3 = new StationModel(30, 50, "Station3", 4, 2);
        w.addPlayer(MapFixtureFactory.TEST_PLAYER);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, station1);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, null);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, station2);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, null);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, null);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, station3);
    }

    public void testNext() {
        WorldIterator wi = new NonNullElements(KEY.STATIONS, w,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getRowID());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getIndex());

        // Look at first station
        boolean b = wi.next();
        assertTrue(b);

        int index = wi.getIndex();
        assertEquals(0, index);
        assertEquals(0, wi.getRowID());
        assertEquals(station1, wi.getElement());

        // Look at seond station
        assertTrue(wi.next());
        assertEquals(2, wi.getIndex());
        assertEquals(1, wi.getRowID());
        assertEquals(station2, wi.getElement());

        WorldIterator wi2 = new NonNullElements(SKEY.TRACK_RULES, w);
        assertTrue(!wi2.next());
    }

    public void testGotoIndex() {
        WorldIterator wi = new NonNullElements(KEY.STATIONS, w,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getRowID());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getIndex());

        wi.gotoIndex(2);
        assertEquals(2, wi.getIndex());
        assertEquals(1, wi.getRowID());

        try {
            wi.gotoIndex(100);
            assertTrue(false);
        } catch (NoSuchElementException e) {
        }
    }
}
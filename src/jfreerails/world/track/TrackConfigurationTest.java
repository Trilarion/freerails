/*
 * TrackConfigurationTest.java
 * JUnit based test
 *
 *Tests that adding and removing track sections from a configuration.
 * Created on 26 January 2002, 02:25
 */
package jfreerails.world.track;

import jfreerails.world.common.OneTileMoveVector;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * JUnit test.
 * @author lindsal
 */
public class TrackConfigurationTest extends TestCase {
    public TrackConfigurationTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TrackConfigurationTest.class);

        return suite;
    }

    public void testAdd() {
        TrackConfiguration a = TrackConfiguration.getFlatInstance("000010000");
        TrackConfiguration b = TrackConfiguration.add(a,
                OneTileMoveVector.NORTH_WEST);
        assertEquals(TrackConfiguration.getFlatInstance("100010000"), b);
        assertEquals(false, a == b);
    }

    public void testGetLength() {
        TrackConfiguration a = TrackConfiguration.getFlatInstance("010010000");
        TrackConfiguration b = TrackConfiguration.getFlatInstance("010010010");
        assertEquals(100, a.getLength());
        assertEquals(200, b.getLength());
    }

    public void testSubtract() {
        TrackConfiguration a = TrackConfiguration.getFlatInstance("100010000");
        TrackConfiguration b = TrackConfiguration.subtract(a,
                OneTileMoveVector.NORTH_WEST);
        assertEquals(TrackConfiguration.getFlatInstance("000010000"), b);
    }
}
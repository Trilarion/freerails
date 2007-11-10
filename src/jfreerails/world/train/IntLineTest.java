package jfreerails.world.train;

import jfreerails.world.common.IntLine;
import junit.framework.TestCase;

/**
 * Junit test.
 * 
 * @author Luke
 */
public class IntLineTest extends TestCase {
    public IntLineTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(IntLineTest.class);
    }

    public void testGetLength() {
        IntLine line = new IntLine(0, 0, 100, 0);
        assertEquals(100, line.getLength(), 0.1);
    }
}
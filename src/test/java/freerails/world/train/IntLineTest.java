package freerails.world.train;

import freerails.world.common.IntLine;
import junit.framework.TestCase;

/**
 * Junit test.
 *
 */
public class IntLineTest extends TestCase {

    /**
     *
     * @param arg0
     */
    public IntLineTest(String arg0) {
        super(arg0);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IntLineTest.class);
    }

    /**
     *
     */
    public void testGetLength() {
        IntLine line = new IntLine(0, 0, 100, 0);
        assertEquals(100, line.getLength(), 0.1);
    }
}
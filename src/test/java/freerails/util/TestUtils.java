package freerails.util;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.Serializable;
import java.util.function.Supplier;

public class TestUtils {

    private TestUtils() {}

    /**
     *
     * @param serializable
     */
    public static void assertCloneBySerializationBehavesWell(Serializable serializable) {
        Assert.assertEquals(serializable, serializable);
        Serializable copy = Utils.cloneBySerialisation(serializable);
        Assert.assertEquals(copy, copy);
        Assert.assertEquals(serializable, copy);

        Assert.assertEquals(serializable.hashCode(), copy.hashCode());
    }

    /**
     *
     * @param a
     * @param b
     */
    public static void assertUnequalAndNoHashcodeCollision(Object a, Object b) {
        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    /**
     *
     * @param action
     */
    public static void assertThrows(FailingRunnable action) {
        try {
            action.run();
        } catch (Exception e) {
            return;
        }
        // nothing thrown, we fail
        Assert.fail();
    }

    public static void assertLineSegmentEquals(int x1, int y1, int x2, int y2, LineSegment segment) {
        Assert.assertEquals(segment, new LineSegment(x1, y1, x2, y2));
    }

    public @FunctionalInterface interface FailingRunnable {
        void run() throws Exception;
    }
}

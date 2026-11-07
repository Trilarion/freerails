package freerails.model.track;

import freerails.util.TestUtils;
import junit.framework.TestCase;

/**
 * Tests track overlap result behavior.
 */
public class TrackOverlapResultTest extends TestCase {

    /**
     * Tests success has no message behavior.
     */
    public void testSuccessHasNoMessage() {
        TrackOverlapResult result = TrackOverlapResult.success();

        assertTrue(result.isSuccess());
        assertNull(result.message());
    }

    /**
     * Tests failure retains explanation behavior.
     */
    public void testFailureRetainsExplanation() {
        TrackOverlapResult result = TrackOverlapResult.failure("Too close to Station A");

        assertFalse(result.isSuccess());
        assertEquals("Too close to Station A", result.message());
    }

    /**
     * Tests rejects inconsistent results behavior.
     */
    public void testRejectsInconsistentResults() {
        TestUtils.assertThrows(IllegalArgumentException.class,
                () -> new TrackOverlapResult(true, "unexpected message"));
        TestUtils.assertThrows(NullPointerException.class, () -> TrackOverlapResult.failure(null));
    }
}

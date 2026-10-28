package freerails.model.track;

import junit.framework.TestCase;

public class TrackOverlapResultTest extends TestCase {

    public void testSuccessHasNoMessage() {
        TrackOverlapResult result = TrackOverlapResult.success();

        assertTrue(result.isSuccess());
        assertNull(result.message());
    }

    public void testFailureRetainsExplanation() {
        TrackOverlapResult result = TrackOverlapResult.failure("Too close to Station A");

        assertFalse(result.isSuccess());
        assertEquals("Too close to Station A", result.message());
    }

    public void testRejectsInconsistentResults() {
        try {
            new TrackOverlapResult(true, "unexpected message");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            TrackOverlapResult.failure(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }
}

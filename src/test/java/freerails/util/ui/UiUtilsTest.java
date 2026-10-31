package freerails.util.ui;

import junit.framework.TestCase;

/**
 * Tests ui utils behavior.
 */
public class UiUtilsTest extends TestCase {

    /**
     * Tests string to rgb value adds opaque alpha channel behavior.
     */
    public void testStringToRGBValueAddsOpaqueAlphaChannel() {
        assertEquals(0xFF123456, UiUtils.stringToRGBValue("123456"));
    }

    /**
     * Tests string to rgb value accepts short hex colors behavior.
     */
    public void testStringToRGBValueAcceptsShortHexColors() {
        assertEquals(0xFF00000A, UiUtils.stringToRGBValue("a"));
    }

    /**
     * Tests string to rgb value rejects invalid hex behavior.
     */
    public void testStringToRGBValueRejectsInvalidHex() {
        try {
            UiUtils.stringToRGBValue("not-a-color");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }
    }
}

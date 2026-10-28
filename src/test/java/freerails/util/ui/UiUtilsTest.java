package freerails.util.ui;

import junit.framework.TestCase;

public class UiUtilsTest extends TestCase {

    public void testStringToRGBValueAddsOpaqueAlphaChannel() {
        assertEquals(0xFF123456, UiUtils.stringToRGBValue("123456"));
    }

    public void testStringToRGBValueAcceptsShortHexColors() {
        assertEquals(0xFF00000A, UiUtils.stringToRGBValue("a"));
    }

    public void testStringToRGBValueRejectsInvalidHex() {
        try {
            UiUtils.stringToRGBValue("not-a-color");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }
    }
}

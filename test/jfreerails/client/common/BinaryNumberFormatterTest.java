package jfreerails.client.common;


import junit.framework.TestCase;

/**
 * This class formats and integer as a binary number with a specified number of
 * digits.
 * 
 * @author Luke Lindsay 03-Nov-2002
 * 
 */
public class BinaryNumberFormatterTest extends TestCase {
    public BinaryNumberFormatterTest(String arg0) {
        super(arg0);
    }

    public void testBinaryFormat() {
        assertEquals("0", BinaryNumberFormatter.format(0, 1));
        assertEquals("1", BinaryNumberFormatter.format(1, 1));
        assertEquals("00", BinaryNumberFormatter.format(0, 2));
        assertEquals("01", BinaryNumberFormatter.format(1, 2));
        assertEquals("10", BinaryNumberFormatter.format(2, 2));
        assertEquals("11", BinaryNumberFormatter.format(3, 2));

        assertEquals("1111", BinaryNumberFormatter.format(15, 4));

        try {
            BinaryNumberFormatter.format(-1, 2);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
        }

        try {
            BinaryNumberFormatter.format(4, 2);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
        }
    }
}
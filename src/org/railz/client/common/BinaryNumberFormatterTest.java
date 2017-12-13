/*
 * Copyright (C) 2002 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.client.common;

import junit.framework.*;


/**This class formats and integer as a binary number with a specified number of digits.
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

    public static Test suite() {
        TestSuite testSuite = new TestSuite(BinaryNumberFormatterTest.class);

        return testSuite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

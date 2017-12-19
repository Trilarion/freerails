/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.world.train;

import freerails.util.IntLine;
import junit.framework.TestCase;

/**
 * Junit test.
 */
public class IntLineTest extends TestCase {

    /**
     * @param arg0
     */
    public IntLineTest(String arg0) {
        super(arg0);
    }

    /**
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
/*
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

package jfreerails.world.train;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import junit.framework.TestCase;


public class SimplePathIteratorImplTest extends TestCase {
    /** @param arg0
     */
    public SimplePathIteratorImplTest(String arg0) {
        super(arg0);
    }

    /** @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SimplePathIteratorImplTest.class);
    }

    public void testHasNext() {
        int[] xpoints = {0, 100};
        int[] ypoints = {0, 0};

        FreerailsPathIterator it = new SimplePathIteratorImpl(xpoints, ypoints);
        assertTrue(it.hasNext());
        it.nextSegment(new IntLine(0, 0, 0, 0));
        assertTrue(!it.hasNext());
    }

    public void testNextSegment() {
        int[] xpoints = {1, 2, 3};
        int[] ypoints = {4, 5, 6};

        FreerailsPathIterator it = new SimplePathIteratorImpl(xpoints, ypoints);
        assertTrue(it.hasNext());

        IntLine line = new IntLine(0, 0, 0, 0);
        it.nextSegment(line);
        assertLineEquals(1, 4, 2, 5, line);
        assertTrue(it.hasNext());
        it.nextSegment(line);
        assertLineEquals(2, 5, 3, 6, line);
        assertTrue(!it.hasNext());
    }

    /** @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param line
     */
    private void assertLineEquals(int x1, int y1, int x2, int y2, IntLine line) {
        assertEquals(x1, line.x1);
        assertEquals(x2, line.x2);
        assertEquals(y1, line.y1);
        assertEquals(y2, line.y2);
    }
}
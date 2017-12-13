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

import java.util.NoSuchElementException;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;


public class SimplePathIteratorImpl implements FreerailsPathIterator {
    boolean hasNext = true;
    int[] x;
    int[] y;
    int position = 0;

    public SimplePathIteratorImpl(int[] xpoints, int[] ypoints) {
        x = (int[])xpoints.clone();
        y = (int[])ypoints.clone(); //defensive copy.

        if (x.length != y.length) {
            throw new IllegalArgumentException(
                "The array length of the array must be even");
        }
    }

    public void nextSegment(IntLine line) {
        if (hasNext()) {
            line.x1 = x[position];
            line.y1 = y[position];
            line.x2 = x[position + 1];
            line.y2 = y[position + 1];
            position++;
        } else {
            throw new NoSuchElementException();
        }
    }

    public boolean hasNext() {
        return (position + 1) < x.length;
    }
}
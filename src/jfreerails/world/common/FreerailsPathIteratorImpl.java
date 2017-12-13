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

/*
 * FreerailsPathIteratorImpl.java
 *
 * Created on 23 September 2002, 20:41
 */
package jfreerails.world.common;

import java.awt.Point;
import java.util.List;
import java.util.NoSuchElementException;


/**
 *
 * @author  Luke Lindsay
 */
public class FreerailsPathIteratorImpl implements FreerailsPathIterator {
    public static FreerailsPathIterator forwardsIterator(List l) {
        return new FreerailsPathIteratorImpl(l, true);
    }

    public static FreerailsPathIterator backwardsIterator(List l) {
        return new FreerailsPathIteratorImpl(l, false);
    }

    /** Creates new FreerailsPathIteratorImpl */
    public FreerailsPathIteratorImpl(List l, boolean f) {
        points = l;
        forwards = f;

        if (forwards) {
            this.position = 0;
        } else {
            this.position = l.size() - 1; //The last element of a list of size 7 is at position 6.
        }
    }

    private final boolean forwards;
    int position;
    List points;

    public boolean hasNext() {
        if (forwards) {
            return (position + 1) < points.size();
        } else {
            return (position - 1) >= 0;
        }
    }

    public void nextSegment(IntLine line) {
        if (hasNext()) {
            Point a;
            Point b;

            if (forwards) {
                position++;
                a = (Point)points.get(position - 1);
                b = (Point)points.get(position);
            } else {
                position--;
                a = (Point)points.get(position + 1);
                b = (Point)points.get(position);
            }

            line.x1 = a.x;
            line.y1 = a.y;
            line.x2 = b.x;
            line.y2 = b.y;
        } else {
            throw new NoSuchElementException();
        }
    }
}
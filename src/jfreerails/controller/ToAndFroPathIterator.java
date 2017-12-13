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

package jfreerails.controller;

import java.util.List;
import java.util.NoSuchElementException;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.FreerailsPathIteratorImpl;
import jfreerails.world.common.IntLine;


/**
 * @author Luke Lindsay 30-Oct-2002
 *
 */
public class ToAndFroPathIterator implements FreerailsPathIterator {
    FreerailsPathIterator path;
    boolean forwards = true;
    List list;

    public ToAndFroPathIterator(List l) {
        list = l;
        nextIterator();
    }

    public void nextIterator() {
        path = new FreerailsPathIteratorImpl(list, forwards);
    }

    public boolean hasNext() {
        if (list.size() < 2) {
            return false;
        } else {
            return true;
        }
    }

    public void nextSegment(IntLine line) {
        if (this.hasNext()) {
            if (!path.hasNext()) {
                forwards = !forwards;
                path = new FreerailsPathIteratorImpl(list, forwards);
            }

            path.nextSegment(line);
        } else {
            throw new NoSuchElementException();
        }
    }
}
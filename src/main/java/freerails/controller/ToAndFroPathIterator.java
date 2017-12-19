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

package freerails.controller;

import freerails.world.common.FreerailsPathIterator;
import freerails.world.common.FreerailsPathIteratorImpl;
import freerails.world.common.IntLine;

import java.awt.*;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Returns a path that goes forwards and backwards along the path passed to its
 * constructor.
 *
 */
public class ToAndFroPathIterator implements FreerailsPathIterator {
    private static final long serialVersionUID = 3256442525337202993L;
    private final List<Point> list;
    private FreerailsPathIterator path;
    private boolean forwards = true;

    /**
     *
     * @param l
     */
    public ToAndFroPathIterator(List<Point> l) {
        list = l;
        nextIterator();
    }

    private void nextIterator() {
        path = new FreerailsPathIteratorImpl(list, forwards);
    }

    public boolean hasNext() {
        return list.size() >= 2;
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
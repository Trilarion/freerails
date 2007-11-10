package jfreerails.controller;

import java.awt.Point;
import java.util.List;
import java.util.NoSuchElementException;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.FreerailsPathIteratorImpl;
import jfreerails.world.common.IntLine;

/**
 * Returns a path that goes forwards and backwards along the path passed to its
 * constructor.
 * 
 * @author Luke Lindsay 30-Oct-2002
 * 
 */
public class ToAndFroPathIterator implements FreerailsPathIterator {
    private static final long serialVersionUID = 3256442525337202993L;

    private FreerailsPathIterator path;

    private boolean forwards = true;

    private final List<Point> list;

    public ToAndFroPathIterator(List<Point> l) {
        list = l;
        nextIterator();
    }

    private void nextIterator() {
        path = new FreerailsPathIteratorImpl(list, forwards);
    }

    public boolean hasNext() {
        if (list.size() < 2) {
            return false;
        }
        return true;
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
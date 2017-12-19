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
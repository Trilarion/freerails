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
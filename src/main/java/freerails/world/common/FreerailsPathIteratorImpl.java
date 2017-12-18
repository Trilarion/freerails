/*
 * FreerailsPathIteratorImpl.java
 *
 * Created on 23 September 2002, 20:41
 */
package freerails.world.common;

import java.awt.*;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Lets the caller access a series of Points as a series of IntLines.
 *
 * @author Luke Lindsay
 */
public class FreerailsPathIteratorImpl implements FreerailsPathIterator {
    private static final long serialVersionUID = 3258411750679720758L;
    private final boolean forwards;
    private final List<Point> points;
    private int position;

    /**
     * Creates new FreerailsPathIteratorImpl
     * @param l
     * @param f
     */
    public FreerailsPathIteratorImpl(List<Point> l, boolean f) {
        points = l;
        forwards = f;

        if (forwards) {
            this.position = 0;
        } else {
            this.position = l.size() - 1; // The last element of a list of
            // size 7 is at position 6.
        }
    }

    /**
     *
     * @param l
     * @return
     */
    public static FreerailsPathIterator forwardsIterator(List<Point> l) {
        return new FreerailsPathIteratorImpl(l, true);
    }

    /**
     *
     * @param l
     * @return
     */
    public static FreerailsPathIterator backwardsIterator(List<Point> l) {
        return new FreerailsPathIteratorImpl(l, false);
    }

    public boolean hasNext() {
        if (forwards) {
            return (position + 1) < points.size();
        }
        return (position - 1) >= 0;
    }

    public void nextSegment(IntLine line) {
        if (hasNext()) {
            Point a;
            Point b;

            if (forwards) {
                position++;
                a = points.get(position - 1);
                b = points.get(position);
            } else {
                position--;
                a = points.get(position + 1);
                b = points.get(position);
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
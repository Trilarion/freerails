package jfreerails.world.train;

import java.util.NoSuchElementException;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;


/**
 * Exposes a path stored as an array of x points and an array of y points.
 * @author Luke
 */
public class SimplePathIteratorImpl implements FreerailsPathIterator {
    private final /*=const */ int[] x;
    private final /*=const */ int[] y;
    private int position = 0;

    public SimplePathIteratorImpl( /*=const */
        int[] xpoints, /*=const */
        int[] ypoints) {
        x = xpoints;
        y = ypoints; //defensive copy.

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
package freerails.world.train;

import freerails.world.common.FreerailsPathIterator;
import freerails.world.common.ImInts;
import freerails.world.common.IntLine;

import java.util.NoSuchElementException;

/**
 * Exposes a path stored as an array of x points and an array of y points.
 *
 */
public class SimplePathIteratorImpl implements FreerailsPathIterator {
    private static final long serialVersionUID = 3618420406261003576L;

    private final ImInts x;

    private final ImInts y;

    private int position = 0;

    /**
     *
     * @param xpoints
     * @param ypoints
     */
    public SimplePathIteratorImpl(ImInts xpoints, ImInts ypoints) {
        x = xpoints;
        y = ypoints;

        if (x.size() != y.size()) {
            throw new IllegalArgumentException(
                    "The array length of the array must be even");
        }
    }

    /**
     *
     * @param xpoints
     * @param ypoints
     */
    public SimplePathIteratorImpl( /* =const */
            int[] xpoints, /* =const */
            int[] ypoints) {
        x = new ImInts(xpoints);
        y = new ImInts(ypoints); // defensive copy.

        if (x.size() != y.size()) {
            throw new IllegalArgumentException(
                    "The array length of the array must be even");
        }
    }

    public void nextSegment(IntLine line) {
        if (hasNext()) {
            line.x1 = x.get(position);
            line.y1 = y.get(position);
            line.x2 = x.get(position + 1);
            line.y2 = y.get(position + 1);
            position++;
        } else {
            throw new NoSuchElementException();
        }
    }

    public boolean hasNext() {
        return (position + 1) < x.size();
    }
}
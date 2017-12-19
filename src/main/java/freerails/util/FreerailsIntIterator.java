package freerails.util;

/**
 * Returns a series of ints.
 *
 */
public interface FreerailsIntIterator {

    /**
     *
     * @return
     */
    boolean hasNextInt();

    /**
     *
     * @return
     */
    int nextInt();
}
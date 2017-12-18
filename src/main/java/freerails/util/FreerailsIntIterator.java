package freerails.util;

/**
 * Returns a series of ints.
 *
 * @author Luke Lindsay
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
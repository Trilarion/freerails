package freerails.util;

/**
 * Returns a series of ints.
 *
 * @author Luke Lindsay
 */
public interface FreerailsIntIterator {
    boolean hasNextInt();

    int nextInt();
}
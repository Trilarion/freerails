/*
 * Created on Sep 7, 2004
 *
 */
package freerails.controller;

/**
 * Defines part of the contract for a pathfinder whose search can be completed
 * in several steps.
 *
 * @author Luke
 */
public interface IncrementalPathFinder {

    // TODO replace with enum.

    /**
     *
     */
    int PATH_NOT_FOUND = Integer.MIN_VALUE;

    /**
     *
     */
    int PATH_FOUND = Integer.MIN_VALUE + 1;

    /**
     *
     */
    int SEARCH_PAUSED = Integer.MIN_VALUE + 2;

    /**
     *
     */
    int SEARCH_NOT_STARTED = Integer.MIN_VALUE + 3;

    /**
     *
     * @return
     */
    int getStatus();

    /**
     *
     * @param maxDuration
     * @throws PathNotFoundException
     */
    void search(long maxDuration) throws PathNotFoundException;

    /**
     *
     */
    void abandonSearch();
}
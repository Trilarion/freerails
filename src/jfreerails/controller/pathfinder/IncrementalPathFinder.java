/*
 * Created on Sep 7, 2004
 *
 */
package jfreerails.controller.pathfinder;


/**
 * Defines part of the contract for a pathfinder whose search can be completed in several steps.
 * @author Luke
 *
 */
public interface IncrementalPathFinder {
    public static final int PATH_NOT_FOUND = Integer.MIN_VALUE;
    public static final int PATH_FOUND = Integer.MIN_VALUE + 1;
    public static final int SEARCH_PAUSED = Integer.MIN_VALUE + 2;
    public static final int SEARCH_NOT_STARTED = Integer.MIN_VALUE + 3;

    public abstract int getStatus();

    public abstract void search(long maxDuration) throws PathNotFoundException;

    public abstract void abandonSearch();
}
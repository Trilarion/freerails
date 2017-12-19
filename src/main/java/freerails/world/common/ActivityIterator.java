/*
 * Created on 02-Jul-2005
 *
 */
package freerails.world.common;

import freerails.world.FreerailsSerializable;

import java.util.NoSuchElementException;

/**
 *
 */
public interface ActivityIterator {

    /**
     *
     * @return
     */
    boolean hasNext();

    /**
     *
     * @throws NoSuchElementException
     */
    void nextActivity() throws NoSuchElementException;

    /**
     * Returns the time the current activity starts.
     *
     * @return
     */
    double getStartTime();

    /**
     * Returns the time the current activity ends.
     *
     * @return
     */
    double getFinishTime();

    /**
     *
     * @return
     */
    double getDuration();

    /**
     * Converts an absolute time value to a time value relative to the start of
     * the current activity. If absoluteTime > getFinishTime(), getDuration() is
     * returned.
     *
     * @param absoluteTime
     * @return
     */
    double absolute2relativeTime(double absoluteTime);

    /**
     *
     * @param absoluteTime
     * @return
     */
    FreerailsSerializable getState(double absoluteTime);

    /**
     *
     * @return
     */
    Activity getActivity();

    /**
     *
     */
    void gotoLastActivity();

    /**
     *
     * @throws NoSuchElementException
     */
    void previousActivity() throws NoSuchElementException;

    /**
     *
     * @return
     */
    boolean hasPrevious();
}

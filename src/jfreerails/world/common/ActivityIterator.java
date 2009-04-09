/*
 * Created on 02-Jul-2005
 *
 */
package jfreerails.world.common;

import java.util.NoSuchElementException;


public interface ActivityIterator {

	boolean hasNext();

	void nextActivity() throws NoSuchElementException;

	/** Returns the time the current activity starts. */
	double getStartTime();

	/** Returns the time the current activity ends. */
	double getFinishTime();

	double getDuration();
	
    /**
     * Converts an absolute time value to a time value relative to the start of
     * the current activity. If absoluteTime > getFinishTime(), getDuration() is
     * returned.
	 */
	double absolute2relativeTime(double absoluteTime);

	FreerailsSerializable getState(double absoluteTime);

	Activity getActivity();

    void gotoLastActivity();

    void previousActivity() throws NoSuchElementException;

    boolean hasPrevious();
}

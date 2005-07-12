/*
 * Created on 02-Jul-2005
 *
 */
package jfreerails.world.top;

import java.util.NoSuchElementException;

import jfreerails.world.common.FreerailsSerializable;

public interface ActivityIterator {

	boolean hasNext();

	void nextActivity() throws NoSuchElementException;

	/** Returns the time the current activity starts. */
	double getStartTime();

	/** Returns the time the current activity ends. */
	double getFinishTime();

	double getDuration();

	FreerailsSerializable getState(double t);

	Activity getActivity();

}

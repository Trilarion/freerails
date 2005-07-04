/*
 * Created on 02-Jul-2005
 *
 */
package jfreerails.world.top;

import java.util.NoSuchElementException;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;

public interface ActivityIterator {

	boolean hasNext();

	void nextActivity() throws NoSuchElementException;

	GameTime getStartTime();

	GameTime getFinishTime();

	int getDuration();

	FreerailsSerializable getState(GameTime t);

	Activity getActivity();

}

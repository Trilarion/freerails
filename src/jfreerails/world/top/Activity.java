/*
 * Created on 02-Jul-2005
 *
 */
package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;

public interface Activity extends FreerailsSerializable {

	int duration();
	
	FreerailsSerializable getState(int dt);
	
}

/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;
import jfreerails.world.top.World;

/**
 * @author Luke Lindsay
 *
 */
public interface Transaction extends FreerailsSerializable {		
	
	Money getValue();
	
	String getDescription(World w);
}

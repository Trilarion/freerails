package jfreerails.world.cargo;

import java.util.Iterator;

import jfreerails.world.common.FreerailsSerializable;

/** This interface defines a bundle of cargo made up of 
 * quantities of cargo from different {@link CargoBatch}s.  
 * 
 * @author Luke
 *
 */
public interface CargoBundle extends FreerailsSerializable {	
	int getAmount(int cargoType);
	int getAmount(CargoBatch cb);
	void setAmount(CargoBatch cb, int cargoType);
	boolean contains(CargoBatch cb);
	Iterator cargoBatchIterator();
	CargoBundle getCopy();
}
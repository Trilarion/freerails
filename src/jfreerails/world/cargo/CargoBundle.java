package jfreerails.world.cargo;

import java.util.Iterator;
import java.util.Vector;


public interface CargoBundle {
	Vector getCargoBatch();

	Iterator getCargoBatchIterator();

	CargoType getCargoType();

	int getTotalAmount();

	int getTotalWeight();
}
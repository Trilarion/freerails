package jfreerails.world.cargo;

import java.util.Iterator;
import java.util.Vector;

import jfreerails.world.type.CargoType;

public interface CargoBundle {
	Vector getCargoBatch();

	Iterator getCargoBatchIterator();

	CargoType getCargoType();

	int getTotalAmount();

	int getTotalWeight();
}
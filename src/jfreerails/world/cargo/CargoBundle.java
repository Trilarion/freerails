package jfreerails.world.cargo;

import java.util.Iterator;
import java.util.Vector;

import jfreerails.type.CargoType;

public interface CargoBundle {
	Vector getCargoBatch();

	Iterator getCargoBatchIterator();

	CargoType getCargoType();

	int getTotalAmount();

	int getTotalWeight();
}
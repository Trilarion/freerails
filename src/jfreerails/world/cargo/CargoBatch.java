package jfreerails.world.cargo;

import java.awt.Point;

import jfreerails.world.type.CargoType;

public interface CargoBatch {
	
	Point getPointOfOrigin();

	String getPlaceOfOrigin();

	CargoType getCargoType();	

	boolean hasTravelled();
}
package jfreerails.world.cargo;

import java.awt.Point;

import jfreerails.type.CargoType;

public interface CargoBatch {
	
	Point getPointOfOrigin();

	String getPlaceOfOrigin();

	CargoType getCargoType();	

	boolean hasTravelled();
}
package jfreerails.world.cargo;

import java.awt.Point;


public interface CargoBatch {
	
	Point getPointOfOrigin();

	String getPlaceOfOrigin();

	CargoType getCargoType();	

	boolean hasTravelled();
}
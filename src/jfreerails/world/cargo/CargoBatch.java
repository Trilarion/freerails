package jfreerails.world.cargo;

import java.awt.Point;

import jfreerails.type.CargoType;
import jfreerails.world.std_cargo.CompositeCargoBundle;

public interface CargoBatch {
	
	public Point getPointOfOrigin();

	public String getPlaceOfOrigin();

	public CargoType getCargoType();	

	boolean hasTravelled();
}
package jfreerails.world.cargo;

import java.awt.Point;

import jfreerails.type.CargoType;
import jfreerails.world.station.StationModel;
import jfreerails.world.std_cargo.*;

public interface CargoBatch {
	
	public Point getPointOfOrigin();

	public String getPlaceOfOrigin();

	public CargoType getCargoType();

	public CompositeCargoBundle getCargoBundle();

	boolean hasTravelled();
}
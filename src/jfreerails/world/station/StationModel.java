package jfreerails.world.station;

import jfreerails.world.type.StationType;
import jfreerails.world.GameTime;

public interface StationModel {
	GameTime getBuiltDate();

	String getStationName();

	StationType getStationType();

}
package jfreerails.world.station;

import jfreerails.misc.GameTime;
import jfreerails.type.StationType;

public interface StationModel {
	GameTime getBuiltDate();

	String getStationName();

	StationType getStationType();

}
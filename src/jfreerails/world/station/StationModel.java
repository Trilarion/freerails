package jfreerails.world.station;

import jfreerails.world.misc.GameTime;

public interface StationModel {
	GameTime getBuiltDate();

	String getStationName();

	StationType getStationType();

}
package jfreerails.world.station;

import jfreerails.world.common.GameTime;

public interface StationModel {
	GameTime getBuiltDate();

	String getStationName();

	StationType getStationType();

}
package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.station.StationList;
import jfreerails.world.terrain.TerrainMap;
import jfreerails.world.track.TrackAndTerrainTileMap;
import jfreerails.world.track.TrackMap;
import jfreerails.world.train.EngineTypesList;
import jfreerails.world.train.TrainList;
import jfreerails.world.train.WagonTypesList;


public interface World extends Types, FreerailsSerializable  {

	TrackAndTerrainTileMap getMap();
	
	TrainList getTrainList();	

	StationList getStationList();
	
	EngineTypesList getEngineTypes();
	
	WagonTypesList getWagonTypesList();
	
	TrackMap getTrackMap();
	
	TerrainMap getTerrainMap();

}

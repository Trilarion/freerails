/*
 * Created on 26-May-2003
 * 
 */
package jfreerails.move;

import java.awt.Point;

import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
 * This {@link CompositeMove} adds a station to the station list and adds a cargo bundle 
 * (to store the cargo waiting at the station) to the cargo bundle list.
 *  
 * @author Luke
 * 
 */
public class AddStationMove extends CompositeMove {
		

	protected AddStationMove(Move[] moves){
		super(moves);					
	}

	public static AddStationMove generateMove(World w, String stationName, Point p){	
					
		int cargoBundleNumber = w.size(KEY.CARGO_BUNDLES);
		Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleNumber, new CargoBundleImpl());
		int stationNumber = w.size(KEY.STATIONS);
		StationModel station = new StationModel(p.x, p.y, stationName, w.size(KEY.CARGO_TYPES), cargoBundleNumber);
		Move addStation = new AddItemToListMove(KEY.STATIONS,  stationNumber, station);
		return new AddStationMove(new Move[]{addCargoBundleMove, addStation});
	}
}
/*
 * Created on 26-May-2003
 * 
 */
package jfreerails.move;

import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.common.Money;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.TrackRule;

/**
 * This {@link CompositeMove} adds a station to the station list and adds a cargo bundle 
 * (to store the cargo waiting at the station) to the cargo bundle list.
 *  
 * @author Luke
 * 
 */
public class AddStationMove extends CompositeMove implements TrackMove {
		

	protected AddStationMove(Move[] moves){
		super(moves);					
	}

	public static AddStationMove generateMove(ReadOnlyWorld w, String stationName, Point p, ChangeTrackPieceMove upgradeTrackMove){	
					
		int cargoBundleNumber = w.size(KEY.CARGO_BUNDLES);
		Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleNumber, new CargoBundleImpl());
		int stationNumber = w.size(KEY.STATIONS);
		StationModel station = new StationModel(p.x, p.y, stationName, w.size(KEY.CARGO_TYPES), cargoBundleNumber);
		
		Move addStation = new AddItemToListMove(KEY.STATIONS,  stationNumber, station);
		TrackRule typeAfter = upgradeTrackMove.getNewTrackPiece().getTrackRule();
		Money cost = typeAfter.getPrice();	
		return new AddStationMove(new Move[]{upgradeTrackMove, addCargoBundleMove, addStation});
	}
	
	public Rectangle getUpdatedTiles() {
		//We know we there is a TrackMove in position 0 in the moves array since we put it there;
		TrackMove tm = (TrackMove)this.getMove(0);
		return tm.getUpdatedTiles();
	}
}

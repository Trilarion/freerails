/**
 * @author Luke Lindsay 08-Nov-2002
 *  
 * Updated 12th April 2003 by Scott Bennett to include nearest city names.
 * 
 * Class to build a station at a given point, names station after nearest
 * city. If that name is taken then a "Junction" or "Siding" is added to 
 * the name. 
 */

package jfreerails.controller;

import java.awt.Point;

import jfreerails.move.AddStationMove;
import jfreerails.move.ChangeTrackPieceMove;
import jfreerails.move.Move;
import jfreerails.world.station.naming.CalcNearestCity;
import jfreerails.world.station.naming.VerifyStationName;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;

public class StationBuilder {
	
	private MoveReceiver moveReceiver;	
	private World w;
	private int ruleNumber;
	private TrackMoveTransactionsGenerator transactionsGenerator;

	public StationBuilder(MoveReceiver moveReceiver, World world) {
		this.moveReceiver = moveReceiver;
		this.w = world;
		TrackRule trackRule;

		int i = -1;
		do {
			i++;
			trackRule = (TrackRule) w.get(KEY.TRACK_RULES, i);
		} while (!trackRule.isStation());

		ruleNumber = i;
		transactionsGenerator = new TrackMoveTransactionsGenerator(world);
	}

	public boolean canBuiltStationHere(Point p) {
		FreerailsTile oldTile = w.getTile(p.x, p.y);
		return !oldTile.getTrackRule().equals(NullTrackType.getInstance());
	}

	public void buildStation(Point p) {
		FreerailsTile oldTile = w.getTile(p.x, p.y);

		//Only build a station if there is track at the specified point.
		if (canBuiltStationHere(p)) {
			String cityName;
			String stationName;
			
			TrackPiece before = (TrackPiece) w.getTile(p.x, p.y);
			TrackRule trackRule =
				(TrackRule) w.get(KEY.TRACK_RULES, this.ruleNumber);
			TrackPiece after =
				trackRule.getTrackPiece(before.getTrackConfiguration());
			ChangeTrackPieceMove upgradeTrackMove =
				new ChangeTrackPieceMove(before, after, p);
				
			//Check whether we can upgrade the track to a station here.
			if(!upgradeTrackMove.tryDoMove(w).ok){
				System.out.println("Cannot upgrade this track to a station!");
				return;
			}
									
			if (!oldTile.getTrackRule().isStation()) {
				//There isn't already a station here, we need to pick a name and add an entry
				//to the station list.
			
				CalcNearestCity cNC = new CalcNearestCity(w, p.x, p.y);
				cityName = cNC.findNearestCity();

				VerifyStationName vSN = new VerifyStationName(w, cityName);
				stationName = vSN.getName();

				if (stationName == null) {
					//there are no cities, this should never happen
					stationName = "Central Station";
				}

				//check the terrain to see if we can build a station on it...
				Move m = AddStationMove.generateMove(w, stationName, p, upgradeTrackMove);
				
				this.moveReceiver.processMove(transactionsGenerator.addTransactions(m));			
			}else{
				//Upgrade an existing station.
				this.moveReceiver.processMove(upgradeTrackMove);
			}

		} else {
			System.out.println(
				"Can't build station since there is no track here!");
		}

	}

	public World getWorld() {
		return w;
	}

	public void setStationType(int ruleNumber) {
		this.ruleNumber = ruleNumber;
	}

}

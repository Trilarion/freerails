/*
 * Created on 28-Mar-2003
 * 
 */
package jfreerails.move;

import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
 * This Move changes what is being built 
 * at an engine shop - when a client wants to build a train, it
 * should send an instance of this class to the server.
 *  
 * @author Luke
 * 
 */
public class ChangeProductionAtEngineShopMove implements Move {

	final ProductionAtEngineShop before;
	final ProductionAtEngineShop after;

	final int stationNumber;

	public ChangeProductionAtEngineShopMove(
		ProductionAtEngineShop b,
		ProductionAtEngineShop a,
		int station) {
		this.before = b;
		this.after = a;
		this.stationNumber = station;
	}

	public MoveStatus tryDoMove(World w) {
		return tryMove(w, before);
	}

	private MoveStatus tryMove(World w, ProductionAtEngineShop stateA) {
		//Check that the specified station exists.
		if (!w.boundsContain(KEY.STATIONS, this.stationNumber)) {
			return MoveStatus.MOVE_FAILED;
		}
		StationModel station = (StationModel) w.get(KEY.STATIONS, stationNumber);
		if (null == station) {
			return MoveStatus.MOVE_FAILED;
		}

		//Check that the station is building what we expect.					
		if (null == station.getProduction()) {
			if (null == stateA) {
				return MoveStatus.MOVE_OK;
			} else {
				return MoveStatus.MOVE_FAILED;
			}
		} else {
			if (station.getProduction().equals(stateA)) {
				return MoveStatus.MOVE_OK;
			} else {
				return MoveStatus.MOVE_FAILED;
			}
		}
	}

	public MoveStatus tryUndoMove(World w) {
		return tryMove(w, after);
	}

	public MoveStatus doMove(World w) {
		MoveStatus status = tryDoMove(w);
		if (status.isOk()) {
			StationModel station = (StationModel) w.get(KEY.STATIONS, stationNumber);
			station.setProduction(this.after);
		}
		return status;
	}

	public MoveStatus undoMove(World w) {
		MoveStatus status = tryUndoMove(w);
		if (status.isOk()) {
			StationModel station = (StationModel) w.get(KEY.STATIONS, stationNumber);
			station.setProduction(this.before);
		}
		return status;
	}

	public boolean equals(Object o) {
		if (o instanceof ChangeProductionAtEngineShopMove) {
			ChangeProductionAtEngineShopMove arg = (ChangeProductionAtEngineShopMove) o;			
			boolean stationNumbersEqual = (this.stationNumber == arg.stationNumber);
			boolean beforeFieldsEqual = (before == null ? arg.before == null : before.equals(arg.before));
			boolean afterFieldsEqual = (after == null ? arg.after == null : after.equals(arg.after));
			if(stationNumbersEqual && beforeFieldsEqual && afterFieldsEqual){
				return true;
			}else{
				return false;
			}			
		} else {
			return false;
		}

	}

}

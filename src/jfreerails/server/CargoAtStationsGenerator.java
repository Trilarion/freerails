/*
 * Created on 31-May-2003
 * 
 */
package jfreerails.server;

import jfreerails.controller.FreerailsServerSerializable;
import jfreerails.move.ChangeCargoBundleMove;
import jfreerails.move.Move;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.station.StationModel;
import jfreerails.world.station.SupplyAtStation;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.World;

/**
 * This class loops over the list of stations and adds cargo
 * depending on what the surrounding tiles supply.
 * 
 * @author Luke
 * 
 */
public class CargoAtStationsGenerator implements FreerailsServerSerializable{
	
	

	public CargoAtStationsGenerator(){
		
	}

	public void update(World w) {
		NonNullElements nonNullStations = new NonNullElements(KEY.STATIONS, w);
		while(nonNullStations.next()){
			StationModel station = (StationModel)nonNullStations.getElement();
			SupplyAtStation supply = station.getSupply();
			CargoBundle cargoBundle = (CargoBundle)w.get(KEY.CARGO_BUNDLES, station.getCargoBundleNumber());
			CargoBundle before = cargoBundle.getCopy();
			CargoBundle after = cargoBundle.getCopy();
			int stationNumber = nonNullStations.getIndex();
			for(int i = 0 ; i < w.size(KEY.CARGO_TYPES); i ++){
				int amountSupplied = supply.getSupply(i);
				if(amountSupplied>0){
					CargoBatch cb = new CargoBatch(i, station.x, station.y, 0, stationNumber);
					int amountAlready = before.getAmount(cb);
					after.setAmount(cb, amountSupplied + amountAlready);
				}
			}
			Move m = new ChangeCargoBundleMove(before, after, station.getCargoBundleNumber());
			m.doMove(w);
		}
	}
}

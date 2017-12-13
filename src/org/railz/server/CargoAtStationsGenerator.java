/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 31-May-2003
 *
 */
package org.railz.server;

import java.util.Iterator;
import java.util.Map.Entry;

import org.railz.controller.FreerailsServerSerializable;
import org.railz.controller.MoveReceiver;
import org.railz.move.ChangeCargoBundleMove;
import org.railz.move.Move;
import org.railz.world.cargo.CargoBatch;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoType;
import org.railz.world.common.*;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.station.StationModel;
import org.railz.world.station.SupplyAtStation;
import org.railz.world.top.*;


/**
 * This class loops over the list of stations and adds cargo
 * depending on what the surrounding tiles supply.
 *
 * @author Luke
 *
 */
public class CargoAtStationsGenerator implements FreerailsServerSerializable {
    private final MoveReceiver moveReceiver;

    public CargoAtStationsGenerator(MoveReceiver moveExecuter) {
        moveReceiver = moveExecuter;
    }

    /**
     * Remove all cargo from the bundle older than the expiry time
     * @return a new CargoBundle without the expired cargo
     */
    private CargoBundle expireOldCargo(CargoBundle cb, ReadOnlyWorld w) {
	CargoBundle newCb = cb.getCopy();
	Iterator i = newCb.cargoBatchIterator();
	int now = ((GameTime) w.get(ITEM.TIME,
		    Player.AUTHORITATIVE)).getTime();
	while (i.hasNext()) {
	    CargoBatch batch = (CargoBatch) ((Entry) i.next()).getKey();
	    CargoType ct = (CargoType) w.get(KEY.CARGO_TYPES,
		    batch.getCargoType(), Player.AUTHORITATIVE);
	    if (now - batch.getTimeCreated() > ct.getExpiryTime())
		i.remove();
	}
	return newCb;
    }

    /**
     * Called once per month
     */
    public void update(World w) {
	NonNullElements players = new NonNullElements(KEY.PLAYERS, w,
		Player.AUTHORITATIVE);
	while (players.next()) {
	    FreerailsPrincipal p = (FreerailsPrincipal) ((Player)
		    players.getElement()).getPrincipal();
	    NonNullElements nonNullStations = new
		NonNullElements(KEY.STATIONS, w, p);

	    while (nonNullStations.next()) {
		StationModel station =
		    (StationModel)nonNullStations.getElement();
		SupplyAtStation supply = station.getSupply();
		CargoBundle cargoBundle = (CargoBundle)w.get(KEY.CARGO_BUNDLES,
			station.getCargoBundleNumber());
		CargoBundle before = cargoBundle.getCopy();
		CargoBundle after = expireOldCargo(cargoBundle, w);
		int stationNumber = nonNullStations.getIndex();

		GameTime gt = (GameTime) w.get(ITEM.TIME,
			Player.AUTHORITATIVE);
		int now = gt.getTime();
		for (int i = 0; i < w.size(KEY.CARGO_TYPES); i++) {
		    int amountSupplied = supply.getSupply(i);
		    CargoType cargoType = (CargoType) w.get(KEY.CARGO_TYPES, i);

		    if (amountSupplied > 0) {
			CargoBatch cb = new CargoBatch(i, station.x,
				station.y, now, stationNumber);
			int amountAlready = after.getAmount(cb);
			after.setAmount(cb, (amountSupplied / 12) + amountAlready);
		    }
		}

		Move m = new ChangeCargoBundleMove(before, after,
			station.getCargoBundleNumber());
		moveReceiver.processMove(m);
	    }
        }
    }
}

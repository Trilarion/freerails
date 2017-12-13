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
package jfreerails.server;

import java.util.Iterator;
import java.util.Map.Entry;

import jfreerails.controller.FreerailsServerSerializable;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.ChangeCargoBundleMove;
import jfreerails.move.Move;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
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
public class CargoAtStationsGenerator implements FreerailsServerSerializable {
    private final MoveReceiver moveReceiver;

    public CargoAtStationsGenerator(MoveReceiver moveExecuter) {
        moveReceiver = moveExecuter;
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
		CargoBundle after = cargoBundle.getCopy();
		int stationNumber = nonNullStations.getIndex();

		/* Let the cargo have a half life of one year, so half the
		 * existing cargo wastes away.*/
		Iterator it = after.cargoBatchIterator();

		while (it.hasNext()) {
		    Entry entry = (Entry) it.next();
		    CargoBatch cb = (CargoBatch)entry.getKey();
		    int amount = after.getAmount(cb);

		    if (amount > 0) {
			entry.setValue(new Integer((int) ((1.0 - Math.pow(0.5,
						1.0 / 12)) * amount)));
		    }
		}

		for (int i = 0; i < w.size(KEY.CARGO_TYPES); i++) {
		    int amountSupplied = supply.getSupply(i);
		    CargoType cargoType = (CargoType) w.get(KEY.CARGO_TYPES, i);

		    if (amountSupplied > 0) {
			CargoBatch cb = new CargoBatch(i, station.x,
				station.y, 0, stationNumber);
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

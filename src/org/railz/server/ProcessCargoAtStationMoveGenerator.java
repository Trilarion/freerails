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
 * Created on 30-Jul-2003
 *
 */
package org.railz.server;

import java.util.Iterator;
import java.util.Map.Entry;

import org.railz.move.AddTransactionMove;
import org.railz.world.accounts.DeliverCargoReceipt;
import org.railz.world.cargo.*;
import org.railz.world.common.GameTime;
import org.railz.world.station.StationModel;
import org.railz.world.top.KEY;
import org.railz.world.top.ITEM;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.train.*;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;

/** This class generates Moves that pay the player for delivering the cargo.
 *
 * @author Luke Lindsay
 *
 */
public class ProcessCargoAtStationMoveGenerator {
    /**
     * @param tp owner of the train
     * @param sp owner of the station
     */
    public static AddTransactionMove[] processCargo(ReadOnlyWorld w,
	CargoBundle cargoBundle, FreerailsPrincipal tp, int stationID,
	FreerailsPrincipal sp) {
	StationModel thisStation = (StationModel)w.get(KEY.STATIONS, stationID,
		sp);
        Iterator batches = cargoBundle.cargoBatchIterator();
        int amountOfCargo = 0;
        double passengerAmount = 0;
	double freightAmount = 0;

	CargoBundle passengerBundle = new CargoBundleImpl();
	CargoBundle freightBundle = new CargoBundleImpl();

	GameTime now = (GameTime) w.get(ITEM.TIME, tp);
        while (batches.hasNext()) {
            CargoBatch batch = (CargoBatch)((Entry) batches.next()).getKey();
	    int dx = (batch.getSourceX() - thisStation.x);
	    int dy = (batch.getSourceY() - thisStation.y);
            double dist = Math.sqrt(dx*dx + dy*dy);
	    int elapsedTime = now.getTime() - (int) batch.getTimeCreated();
	    CargoType ct = (CargoType) w.get(KEY.CARGO_TYPES,
		    batch.getCargoType(), Player.AUTHORITATIVE);
            double amount = ((double) cargoBundle.getAmount(batch)) *
	       	Math.log(1 + dist) *
	       	(double) ct.getAgeAdjustedValue(elapsedTime);
	    if (ct.getCategory() == TransportCategory.PASSENGER) {
		passengerAmount += amount;
		passengerBundle.addCargo(batch, cargoBundle.getAmount(batch));
	    } else {
		freightAmount += amount;
		freightBundle.addCargo(batch, cargoBundle.getAmount(batch));
	    }
        }

	AddTransactionMove[] moves = new AddTransactionMove[2];
        DeliverCargoReceipt receipt = new DeliverCargoReceipt(now, (long)
		passengerAmount, passengerBundle,
		DeliverCargoReceipt.SUBCATEGORY_PASSENGERS);
        moves[0] = new AddTransactionMove(0, receipt, tp);
	receipt = new DeliverCargoReceipt(now, (long)
		freightAmount, freightBundle,
		DeliverCargoReceipt.SUBCATEGORY_FREIGHT);
        moves[1] = new AddTransactionMove(0, receipt, tp);
	return moves;
    }
}

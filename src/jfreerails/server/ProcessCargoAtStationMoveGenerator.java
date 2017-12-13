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
package jfreerails.server;

import java.util.Iterator;
import java.util.Map.Entry;

import jfreerails.move.AddTransactionMove;
import jfreerails.world.accounts.DeliverCargoReceipt;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.common.GameTime;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;

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
    public static AddTransactionMove processCargo(ReadOnlyWorld w,
	CargoBundle cargoBundle, FreerailsPrincipal tp, int stationID,
	FreerailsPrincipal sp) {
	StationModel thisStation = (StationModel)w.get(KEY.STATIONS, stationID,
		sp);
        Iterator batches = cargoBundle.cargoBatchIterator();
        int amountOfCargo = 0;
        double amount = 0;

        while (batches.hasNext()) {
            CargoBatch batch = (CargoBatch)((Entry) batches.next()).getKey();
	    int dx = (batch.getSourceX() - thisStation.x);
	    int dy = (batch.getSourceY() - thisStation.y);
            double dist = Math.sqrt(dx*dx + dy*dy);
            amount += cargoBundle.getAmount(batch) * Math.log(dist) * 100;
        }
	System.out.println("amount for cargo is " + amount);
	GameTime now = (GameTime) w.get(ITEM.TIME, tp);

        DeliverCargoReceipt receipt = new DeliverCargoReceipt(now, (long)
		amount, cargoBundle);

	/* credit owner of the train for cargo delivery */
        return new AddTransactionMove(0, receipt, tp);
    }
}

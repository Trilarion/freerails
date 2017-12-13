/*
 * Copyright (C) 2003 Scott Bennett
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

/**
 * @author Scott Bennett
 * Created: 19th May 2003
 *
 * This class loops through all of the known stations and recalculates
 * the cargoes that they supply.
 */
package jfreerails.server;

import java.util.Vector;
import jfreerails.controller.CargoElementObject;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.ChangeStationMove;
import jfreerails.move.Move;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.station.StationModel;
import jfreerails.world.station.SupplyAtStation;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldListListener;


public class CalcSupplyAtStations implements WorldListListener {
    private World w;
    private MoveReceiver moveReceiver;

    /**
     *
     * Constructor, currently called from GUIComponentFactory
     *
     * @param world The World object that contains all about the game world
     *
     */
    public CalcSupplyAtStations(World world, MoveReceiver mr) {
        this.w = world;
        this.moveReceiver = mr;
    }

    /**
     *
     * Loop through each known station, call calculations method
     *
     */
    public void doProcessing() {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, w,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = (FreerailsPrincipal) ((Player)
		    i.getElement()).getPrincipal();
	    NonNullElements iterator = new NonNullElements(KEY.STATIONS, w, p);
	    while (iterator.next()) {
		StationModel stationBefore =
		    (StationModel)iterator.getElement();

		StationModel stationAfter = calculations(stationBefore);

		if (!stationAfter.equals(stationBefore)) {
		    Move move = new ChangeStationMove(iterator.getIndex(),
			    stationBefore, stationAfter, p);
		    this.moveReceiver.processMove(move);
		}
	    }
	}
    }

    /**
     *
     * Process each existing station, updating what is supplied to it
     *
     * @param station A StationModel ojbect to be processed
     *
     */
    private StationModel calculations(StationModel station) {
        int x = station.getStationX();
        int y = station.getStationY();

        //init vars
        CalcCargoSupplyRateAtStation supplyRate;
        Vector supply = new Vector();
        int[] cargoSupplied = new int[w.size(KEY.CARGO_TYPES)];

        //calculate the supply rates and put information into a vector
        supplyRate = new CalcCargoSupplyRateAtStation(w, x, y);
        supply = supplyRate.scanAdjacentTiles();

        //grab the supply rates from the vector
        for (int i = 0; i < supply.size(); i++) {
            cargoSupplied[i] = ((CargoElementObject)supply.elementAt(i)).getRate();
        }

        //set the supply rates for the current station	
        SupplyAtStation supplyAtStation = new SupplyAtStation(cargoSupplied);
        station = new StationModel(station, supplyAtStation);
        station = new StationModel(station, supplyRate.getDemand());
        station = new StationModel(station, supplyRate.getConversion());

        return station;
    }

    public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
        if (key == KEY.STATIONS) {
            this.doProcessing();
        }
    }

    public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
        if (key == KEY.STATIONS) {
            this.doProcessing();
        }
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal p) {
        if (key == KEY.STATIONS) {
            this.doProcessing();
        }
    }
}

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.railz.config.LogManager;
import org.railz.move.AddTransactionMove;
import org.railz.world.accounts.DeliverCargoReceipt;
import org.railz.world.cargo.CargoBatch;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoBundleImpl;
import org.railz.world.cargo.CargoType;
import org.railz.world.common.GameTime;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.station.StationModel;
import org.railz.world.top.ITEM;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.train.TransportCategory;

/**
 * This class generates Moves that pay the player for delivering the cargo.
 * 
 * @author Luke Lindsay
 * 
 */
public class ProcessCargoAtStationMoveGenerator {
    private static final String CLASS_NAME = ProcessCargoAtStationMoveGenerator.class.getName();
    private static final Logger LOGGER = LogManager.getLogger(CLASS_NAME);
    
    /**
     * @param trainPrinciple
     *            owner of the train
     * @param stationPrinciple
     *            owner of the station
     */
    public static AddTransactionMove[] processCargo(ReadOnlyWorld w, CargoBundle cargoBundle,
	    FreerailsPrincipal trainPrinciple, int stationID, FreerailsPrincipal stationPrinciple) {
	final String methodName = "processCargo";
	LOGGER.entering(CLASS_NAME, methodName);
	
	StationModel thisStation = (StationModel) w.get(KEY.STATIONS, stationID, stationPrinciple);
	Iterator batches = cargoBundle.cargoBatchIterator();
	
	int amountOfCargo = 0;
	double passengerAmount = 0;
	double freightAmount = 0;
	
	CargoBundle passengerBundle = new CargoBundleImpl();
	CargoBundle freightBundle = new CargoBundleImpl();
	
	GameTime now = (GameTime) w.get(ITEM.TIME, trainPrinciple);
	
	while (batches.hasNext()) {
	    CargoBatch currentBatch = (CargoBatch) ((Entry) batches.next()).getKey();
	    
	    // Distance
	    int dx = (currentBatch.getSourceX() - thisStation.x);
	    int dy = (currentBatch.getSourceY() - thisStation.y);
	    double dist = Math.sqrt(dx * dx + dy * dy);
	    
	    // Time
	    int elapsedTime = now.getTime() - (int) currentBatch.getTimeCreated();
	    
	    CargoType ct = (CargoType) w.get(KEY.CARGO_TYPES, currentBatch.getCargoType(),
		    Player.AUTHORITATIVE);
	    
	    // Weird calculation
	    double amount = ((double) cargoBundle.getAmount(currentBatch)) * Math.log(1 + dist)
		    * (double) ct.getAgeAdjustedValue(elapsedTime);
	    
	    TransportCategory goodsCategory = ct.getCategory();
	    // LOGGER.logp(Level.INFO, CLASS_NAME, methodName,
	    // "ct.getCategory() = " + goodsCategory);
	    if (TransportCategory.PASSENGER.equals(ct.getCategory())) {
		passengerAmount += amount;
		passengerBundle.addCargo(currentBatch, cargoBundle.getAmount(currentBatch));
		
		LOGGER.logp(Level.INFO, CLASS_NAME, methodName, "ct.getCategory() = "
			+ goodsCategory + ". Passenger amount = " + passengerAmount);
	    } else {
		freightAmount += amount;
		freightBundle.addCargo(currentBatch, cargoBundle.getAmount(currentBatch));
		LOGGER.logp(Level.INFO, CLASS_NAME, methodName, "ct.getCategory() = "
			+ goodsCategory + ". Freight amount = " + freightAmount);
	    }
	}
	
	AddTransactionMove[] moves = new AddTransactionMove[2];
	
	DeliverCargoReceipt receipt = new DeliverCargoReceipt(now, (long) passengerAmount,
		passengerBundle, DeliverCargoReceipt.SUBCATEGORY_PASSENGERS);
	moves[0] = new AddTransactionMove(0, receipt, trainPrinciple);
	
	receipt = new DeliverCargoReceipt(now, (long) freightAmount, freightBundle,
		DeliverCargoReceipt.SUBCATEGORY_FREIGHT);
	moves[1] = new AddTransactionMove(0, receipt, trainPrinciple);
	
	return moves;
    }
}

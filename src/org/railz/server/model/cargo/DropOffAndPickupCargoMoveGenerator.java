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

package org.railz.server.model.cargo;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.railz.config.LogManager;
import org.railz.controller.MoveReceiver;
import org.railz.move.AddTransactionMove;
import org.railz.move.ChangeCargoBundleMove;
import org.railz.move.TransferCargoAtStationMove;
import org.railz.world.cargo.CargoBatch;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoBundleImpl;
import org.railz.world.common.GameTime;
import org.railz.world.player.Player;
import org.railz.world.station.ConvertedAtStation;
import org.railz.world.station.DemandAtStation;
import org.railz.world.station.StationModel;
import org.railz.world.top.ITEM;
import org.railz.world.top.KEY;
import org.railz.world.top.ObjectKey;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.train.TrainModel;
import org.railz.world.train.TrainOrdersModel;
import org.railz.world.train.WagonType;

/**
 * This class generates moves that transfer cargo between train and the stations
 * it stops at - it also handles cargo converions that occur when cargo is
 * dropped off.
 * 
 * @author Scott Bennett Date Created: 4 June 2003
 * 
 */
public class DropOffAndPickupCargoMoveGenerator {
    private ReadOnlyWorld readOnlyWorld;
    private MoveReceiver moveReceiver;
    private static final String CLASS_NAME = DropOffAndPickupCargoMoveGenerator.class.getName();
    private static final Logger logger = LogManager.getLogger(CLASS_NAME);
    private static final Logger logger2 = LogManager.getDefault();
    
    /**
     * Contructor
     * 
     * @param world
     *            The world object
     */
    public DropOffAndPickupCargoMoveGenerator(ReadOnlyWorld world, MoveReceiver mr) {
	readOnlyWorld = world;
	moveReceiver = mr;
    }
    
    /**
     * Cargo on board the train is unloaded and sold.
     */
    public void unloadTrain(ObjectKey trainKey, ObjectKey stationKey) {
	TrainModel train = (TrainModel) readOnlyWorld.get(trainKey.key, trainKey.index, trainKey.principal);
	
	int trainBundleId = train.getCargoBundleNumber();
	CargoBundle trainBefore = (CargoBundle) readOnlyWorld.get(KEY.CARGO_BUNDLES, trainBundleId,
		Player.AUTHORITATIVE);
	
	// Prepare loads before/after
	
	// TODO get copy if already have for before?
	
	trainBefore = trainBefore.getCopy();
	CargoBundle trainAfter = trainBefore.getCopy();
	
	StationModel station = (StationModel) readOnlyWorld.get(stationKey.key, stationKey.index,
		stationKey.principal);
	int stationBundleId = station.getCargoBundleNumber();
	
	// Station cargo bundle stored under world??
	// Should be under station?
	// Station should map to production unit?
	CargoBundle stationBefore = (CargoBundle) readOnlyWorld.get(KEY.CARGO_BUNDLES, stationBundleId,
		Player.AUTHORITATIVE);
	stationBefore = stationBefore.getCopy();
	CargoBundle stationAfter = stationBefore.getCopy();
	
	// Get iterator and iterate through batches
	Iterator trainBatches = trainAfter.cargoBatchIterator();
	CargoBundle cargoDroppedOff = new CargoBundleImpl();
	
	DemandAtStation demand = station.getDemand();
	
	// Old- Unload the cargo that the station demands
	
	// Iterate through train batches
	while (trainBatches.hasNext()) {
	    CargoBatch trainCb = (CargoBatch) ((Entry) trainBatches.next()).getKey();
	    int cargoType = trainCb.getCargoType();
	    logger.log(Level.INFO, "Unloading cargo type: " + cargoType);
	    // logger2.log(Level.INFO, "log2Unloading cargo type: " +
	    // cargoType);
	    // logger2.log(Level.FINE, "blahshitUnloading cargo type: " +
	    // cargoType);
	    
	    // if the cargo is demanded.
	    // DemandAtStation demand = station.getDemand();
	    
	    if (demand.isCargoDemanded(cargoType)) {
		logger.log(Level.INFO, "Cargo is demanded.");
		int amountOnTrain = trainAfter.getAmount(trainCb);
		
		// drop off cargo whether needed or not...
		cargoDroppedOff.addCargo(trainCb, amountOnTrain);
		
		// Now perform any conversions..
		ConvertedAtStation converted = station.getConverted();
		
		if (converted.isCargoConverted(cargoType)) {
		    int newCargoType = converted.getConversion(cargoType);
		    GameTime now = (GameTime) readOnlyWorld.get(ITEM.TIME, Player.AUTHORITATIVE);
		    CargoBatch newCargoBatch = new CargoBatch(newCargoType, station.x, station.y,
			    now.getTime(), stationKey.index);
		    // Add cargo to station, amount is unchanged from total...
		    stationAfter.addCargo(newCargoBatch, amountOnTrain);
		}
		
		trainBatches.remove();
	    } else {
		logger.log(Level.INFO, "Cargo is not demanded. Do nothing.");
	    }
	    
	}
	
	AddTransactionMove[] payment = ProcessCargoAtStationMoveGenerator.processCargo(readOnlyWorld,
		cargoDroppedOff, trainKey.principal, stationKey.index, stationKey.principal);
	
	ChangeCargoBundleMove changeAtStation = new ChangeCargoBundleMove(stationBefore,
		stationAfter, stationBundleId);
	
	ChangeCargoBundleMove changeOnTrain = new ChangeCargoBundleMove(trainBefore, trainAfter,
		trainBundleId);
	
	logger.log(Level.FINE, "train " + trainKey.index + ": stationAfter = " + stationAfter
		+ ", stationBefore = " + stationBefore + ", trainAfter = " + trainAfter
		+ ", trainBefore = " + trainBefore + ", dropped off = " + cargoDroppedOff);
	
	logger.log(Level.FINE, "payment0 = " + payment[0]);
	logger.log(Level.FINE, "payment1 = " + payment[1]);
	moveReceiver.processMove(TransferCargoAtStationMove.generateMove(changeAtStation,
		changeOnTrain, payment));
    }
    
    /**
     * Sell or dump all cargo which can't fit on the train
     */
    public void dumpSurplusCargo(ObjectKey trainKey, ObjectKey stationKey) {
	TrainModel train = (TrainModel) readOnlyWorld.get(trainKey.key, trainKey.index, trainKey.principal);
	
	int trainBundleId = train.getCargoBundleNumber();
	CargoBundle trainBefore = (CargoBundle) readOnlyWorld.get(KEY.CARGO_BUNDLES, trainBundleId,
		Player.AUTHORITATIVE);
	trainBefore = trainBefore.getCopy();
	CargoBundle trainAfter = trainBefore.getCopy();
	
	StationModel station = (StationModel) readOnlyWorld.get(stationKey.key, stationKey.index,
		stationKey.principal);
	int stationBundleId = station.getCargoBundleNumber();
	CargoBundle stationBefore = (CargoBundle) readOnlyWorld.get(KEY.CARGO_BUNDLES, stationBundleId,
		Player.AUTHORITATIVE);
	stationBefore = stationBefore.getCopy();
	CargoBundle stationAfter = stationBefore.getCopy();
	
	CargoBundle cargoDroppedOff = new CargoBundleImpl();
	
	// Unload the cargo that there isn't space for on the train regardless
	// of whether the station demands it.
	int[] spaceAvailable = this.getSpaceAvailableOnTrain(train);
	
	boolean needToDump = false;
	for (int cargoType = 0; cargoType < spaceAvailable.length; cargoType++) {
	    if (spaceAvailable[cargoType] < 0) {
		int amount2transfer = -spaceAvailable[cargoType];
		transferCargoToStation(cargoType, amount2transfer, trainAfter, stationAfter,
			cargoDroppedOff, stationKey);
		needToDump = true;
	    }
	}
	
	if (!needToDump)
	    return;
	
	AddTransactionMove payment[] = ProcessCargoAtStationMoveGenerator.processCargo(readOnlyWorld,
		cargoDroppedOff, trainKey.principal, stationKey.index, stationKey.principal);
	
	ChangeCargoBundleMove changeAtStation = new ChangeCargoBundleMove(stationBefore,
		stationAfter, stationBundleId);
	
	ChangeCargoBundleMove changeOnTrain = new ChangeCargoBundleMove(trainBefore, trainAfter,
		trainBundleId);
	
	moveReceiver.processMove(TransferCargoAtStationMove.generateMove(changeAtStation,
		changeOnTrain, payment));
    }
    
    /**
     * @return true if there is enough cargo at this station to load the train,
     *         false otherwise.
     */
    public boolean checkCargoAtStation(ObjectKey trainKey, ObjectKey stationKey) {
	TrainModel tm = (TrainModel) readOnlyWorld.get(trainKey.key, trainKey.index, trainKey.principal);
	
	TrainOrdersModel tom = tm.getScheduleIterator().getCurrentOrder(readOnlyWorld);
	if (tom == null)
	    return false;
	
	if (!tom.getWaitUntilFull())
	    return true;
	
	StationModel sm = (StationModel) readOnlyWorld.get(stationKey.key, stationKey.index,
		stationKey.principal);
	
	CargoBundle stationBundle = (CargoBundle) readOnlyWorld.get(KEY.CARGO_BUNDLES,
		sm.getCargoBundleNumber(), Player.AUTHORITATIVE);
	
	int[] spaceLeft = getSpaceAvailableOnTrain(tm);
	for (int cargoType = 0; cargoType < readOnlyWorld.size(KEY.CARGO_TYPES); cargoType++) {
	    if (stationBundle.getAmount(cargoType) < spaceLeft[cargoType])
		return false;
	}
	
	return true;
    }
    
    /**
     * Load the train with as much cargo as is available and will fit on the
     * train.
     */
    public void loadTrain(ObjectKey trainKey, ObjectKey stationKey) {
	TrainModel train = (TrainModel) readOnlyWorld.get(trainKey.key, trainKey.index, trainKey.principal);
	
	int trainBundleId = train.getCargoBundleNumber();
	CargoBundle trainBefore = (CargoBundle) readOnlyWorld.get(KEY.CARGO_BUNDLES, trainBundleId,
		Player.AUTHORITATIVE);
	trainBefore = trainBefore.getCopy();
	CargoBundle trainAfter = trainBefore.getCopy();
	
	StationModel station = (StationModel) readOnlyWorld.get(stationKey.key, stationKey.index,
		stationKey.principal);
	int stationBundleId = station.getCargoBundleNumber();
	CargoBundle stationBefore = (CargoBundle) readOnlyWorld.get(KEY.CARGO_BUNDLES, stationBundleId,
		Player.AUTHORITATIVE);
	stationBefore = stationBefore.getCopy();
	CargoBundle stationAfter = stationBefore.getCopy();
	
	int[] spaceAvailable = getSpaceAvailableOnTrain(train);
	
	// Transfer cargo from the station to the train subject to the space
	// available on the train.
	for (int cargoType = 0; cargoType < readOnlyWorld.size(KEY.CARGO_TYPES); cargoType++) {
	    int amount2transfer = Math.min(spaceAvailable[cargoType],
		    stationAfter.getAmount(cargoType));
	    transferCargo(cargoType, amount2transfer, stationAfter, trainAfter);
	}
	
	ChangeCargoBundleMove changeAtStation = new ChangeCargoBundleMove(stationBefore,
		stationAfter, stationBundleId);
	
	ChangeCargoBundleMove changeOnTrain = new ChangeCargoBundleMove(trainBefore, trainAfter,
		trainBundleId);
	
	moveReceiver.processMove(TransferCargoAtStationMove.generateMove(changeAtStation,
		changeOnTrain));
    }
    
    /**
     * @return array indexed by CARGO_TYPE indicating available space
     */
    private int[] getSpaceAvailableOnTrain(TrainModel train) {
	// This array will store the amount of space available on the train for
	// each cargo type.
	int[] spaceAvailable = new int[readOnlyWorld.size(KEY.CARGO_TYPES)];
	
	// First calculate the train's total capacity.
	for (int j = 0; j < train.getNumberOfWagons(); j++) {
	    WagonType wagonType = (WagonType) readOnlyWorld.get(KEY.WAGON_TYPES, train.getWagon(j),
		    Player.AUTHORITATIVE);
	    int cargoType = wagonType.getCargoType();
	    
	    spaceAvailable[cargoType] += wagonType.getCapacity();
	}
	
	CargoBundle cb = (CargoBundle) readOnlyWorld.get(KEY.CARGO_BUNDLES, train.getCargoBundleNumber(),
		Player.AUTHORITATIVE);
	// Second, subtract the space taken up by cargo that the train is
	// already carrying.
	for (int cargoType = 0; cargoType < readOnlyWorld.size(KEY.CARGO_TYPES); cargoType++) {
	    spaceAvailable[cargoType] -= cb.getAmount(cargoType);
	}
	
	return spaceAvailable;
    }
    
    private void transferCargoToStation(int cargoType, int amountToTransfer, CargoBundle from,
	    CargoBundle to, CargoBundle droppedOff, ObjectKey stationKey) {
	if (0 == amountToTransfer)
	    return;
	
	StationModel station = (StationModel) readOnlyWorld.get(KEY.STATIONS, stationKey.index,
		stationKey.principal);
	Iterator batches = from.cargoBatchIterator();
	int amountTransferedSoFar = 0;
	DemandAtStation demand = station.getDemand();
	ConvertedAtStation converted = station.getConverted();
	
	while (batches.hasNext() && amountTransferedSoFar < amountToTransfer) {
	    CargoBatch cb = (CargoBatch) ((Entry) batches.next()).getKey();
	    
	    if (cb.getCargoType() == cargoType) {
		int amount = from.getAmount(cb);
		int amountOfThisBatchToTransfer;
		
		if (amount < amountToTransfer - amountTransferedSoFar) {
		    amountOfThisBatchToTransfer = amount;
		    batches.remove();
		} else {
		    amountOfThisBatchToTransfer = amountToTransfer - amountTransferedSoFar;
		    from.addCargo(cb, -amountOfThisBatchToTransfer);
		}
		
		if (droppedOff != null && demand.isCargoDemanded(cargoType))
		    droppedOff.addCargo(cb, amountOfThisBatchToTransfer);
		
		if (converted.isCargoConverted(cargoType)) {
		    int newCargoType = converted.getConversion(cargoType);
		    GameTime now = (GameTime) readOnlyWorld.get(ITEM.TIME, Player.AUTHORITATIVE);
		    CargoBatch newCargoBatch = new CargoBatch(newCargoType, station.x, station.y,
			    now.getTime(), stationKey.index);
		    to.addCargo(newCargoBatch, amountOfThisBatchToTransfer);
		}
		amountTransferedSoFar += amountOfThisBatchToTransfer;
	    }
	}
    }
    
    /**
     * Move the specified quantity of the specifed cargotype from one bundle to
     * another.
     */
    private static void transferCargo(int cargoTypeToTransfer, int amountToTransfer,
	    CargoBundle from, CargoBundle to) {
	if (0 == amountToTransfer) {
	    return;
	} else {
	    Iterator batches = from.cargoBatchIterator();
	    int amountTransferedSoFar = 0;
	    
	    while (batches.hasNext() && amountTransferedSoFar < amountToTransfer) {
		CargoBatch cb = (CargoBatch) ((Entry) batches.next()).getKey();
		
		if (cb.getCargoType() == cargoTypeToTransfer) {
		    int amount = from.getAmount(cb);
		    int amountOfThisBatchToTransfer;
		    
		    if (amount < amountToTransfer - amountTransferedSoFar) {
			amountOfThisBatchToTransfer = amount;
			batches.remove();
		    } else {
			amountOfThisBatchToTransfer = amountToTransfer - amountTransferedSoFar;
			from.addCargo(cb, -amountOfThisBatchToTransfer);
		    }
		    
		    to.addCargo(cb, amountOfThisBatchToTransfer);
		    amountTransferedSoFar += amountOfThisBatchToTransfer;
		}
	    }
	}
    }
}

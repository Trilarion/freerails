/**
 * Class to transfer cargo between train and the stations it stops at.
 * 
 * @author Scott Bennett
 * Date Created: 4 June 2003
 * 
 */

package jfreerails.server;

import java.util.Iterator;

import jfreerails.move.ChangeCargoBundleMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.TransferCargoAtStationMove;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.train.TrainModel;
//import jfreerails.world.train.WagonType;

public class DropOffAndPickupCargoMoveGenerator {
	
	private World w;
	
	private TrainModel train;
	private int trainId;
	private int trainBundleId;
	//private CargoBundle trainBundle;
	
	private int stationId;
	private int stationBundleId;
	//private CargoBundle stationBundle;
	
	private CargoBundle stationBefore;
	private	CargoBundle stationAfter;
	private	CargoBundle trainBefore;
	private	CargoBundle trainAfter;
	
	public DropOffAndPickupCargoMoveGenerator(int trainNo ,int stationNo, World world) {
		trainId = trainNo;
		stationId = stationNo;
		w = world;
		
		train = (TrainModel)w.get(KEY.TRAINS,trainId);
		
		getBundles();
		
		/*showWagonTypes();
		showCargoTypes();*/
		
		processTrainBundle(); //ie. unload train / dropoff cargo
		processStationBundle(); //ie. load train / pickup cargo
		
	}
	
	public Move generateMove(){
		
		//The methods that calculate the before and after bundles could be called from here.
		
		ChangeCargoBundleMove changeAtStation = new ChangeCargoBundleMove(stationBefore, stationAfter, stationBundleId);
		ChangeCargoBundleMove changeOnTrain = new ChangeCargoBundleMove(trainBefore, trainAfter, trainBundleId);
		return TransferCargoAtStationMove.generateMove(changeAtStation, changeOnTrain);
	}	
	/*public void showWagonTypes() {
		for (int i=0; i<w.size(KEY.WAGON_TYPES); i++) {
			WagonType wagonType = (WagonType) w.get(KEY.WAGON_TYPES,i);
			System.out.println(wagonType.getName());
		}
	}*/
	
	
	/*public void showCargoTypes() {
		for (int i=0; i<w.size(KEY.CARGO_TYPES); i++) {
			CargoType cargo = (CargoType)w.get(KEY.CARGO_TYPES,i);
			System.out.println(cargo.getName());
		}
	}*/
	
	
	public void getBundles(){
		trainBundleId = ((TrainModel)w.get(KEY.TRAINS,trainId)).getCargoBundleNumber(); 
		trainBefore = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, trainBundleId)).getCopy();		
		trainAfter = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, trainBundleId)).getCopy();
		stationBundleId = ((StationModel)w.get(KEY.STATIONS,stationId)).getCargoBundleNumber();
		stationAfter = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, stationBundleId)).getCopy();
		stationBefore = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, stationBundleId)).getCopy();	
	}
	
	
	public void processTrainBundle() {				
		
		Iterator batches = trainAfter.cargoBatchIterator();

		StationModel station = (StationModel)w.get(KEY.STATIONS, stationId);
		
		while (batches.hasNext()) {

			CargoBatch cb = (CargoBatch)batches.next();

			if ( station.getDemand().isCargoDemanded(cb.getCargoType()) ) {
				//cargo is demanded, so:
				//	pay train owner...
				System.out.println(w.get(KEY.CARGO_TYPES,cb.getCargoType()) + " was delivered by train #" + trainId);	
			}
					
			batches.remove();	
		}		
		
		//for each cargo batch in the train bundle:
		//	if station demands that cargo type
		//		pay owner of train, remove batch from train bundle
		//	else
		//		remove batch from train bundle
	}
	
	/*
	public void refreshBeforeAfterBundles() {
		getBundles();
		stationBefore = stationBundle.getCopy();
		stationAfter = stationBundle.getCopy();
		trainBefore = trainBundle.getCopy();
		trainAfter = trainBundle.getCopy();
	}
	*/
	
	
	public void processStationBundle() {
			
		//refreshBeforeAfterBundles();
										
		//test output
		System.out.println("train #" + trainId + " has " + train.getNumberOfWagons() + " wagons");
		
		//see what can be put in a train's wagons
		for (int j=0; j<train.getNumberOfWagons(); j++) {
			CargoType wagonCargoType = (CargoType)w.get(KEY.CARGO_TYPES,train.getWagon(j));
			
			//test output
			System.out.println("train #" + trainId + " has a " + wagonCargoType.getCategory() + " wagon for wagon #" + j);
				
			//for each cargo type, compare wagon category with cargo waiting at station
			for (int k=0; k<w.size(KEY.CARGO_TYPES); k++) {
				CargoType cargoType = (CargoType)w.get(KEY.CARGO_TYPES,k);
								
				//compare cargo types wagon can carry, to cargo waiting type
				if (wagonCargoType.getCategory().equals(cargoType.getCategory())) {
					
					//check if there is any cargo of this type waiting	
					if (stationBefore.getAmount(k) > 0) {
						
						//test output 
						System.out.println(stationBefore.getAmount(k) + " wagons of " + wagonCargoType.getCategory() + " available for pickup");
						
						//transfer cargo to the current wagon
						transferCargo(k);
					}
				}
			}
			//LL, is it ok to comment out this line?
			//doCargoTransferMove();	
			//refreshBeforeAfterBundles();
		}
	}
	
	public void transferCargo(int cargoTypeToTransfer) {
		Iterator batches = stationAfter.cargoBatchIterator();
		int amount = 0;
		CargoBatch replacementBatch = null;
		boolean TRANSFER_NOT_DONE = true;
		
		while (batches.hasNext() && TRANSFER_NOT_DONE) {
			CargoBatch cb = (CargoBatch)batches.next();

			amount = stationAfter.getAmount(cb);
			
			if ( transferIfTheSameType(cb, cb.getCargoType(),cargoTypeToTransfer) ) {
				//cargo was transferred into wagon
				
				//test output
				System.out.println("transferring a wagon of cargo " + cargoTypeToTransfer +
				  	 " to train #" + trainId + " from station #" + stationId);			
				
				//we need to decrement the value of amount by 1
				if (amount > 1) {
					//create a replacement batch
					//Since CargoBatch is immutable, there is no need to create a copy, LL
					replacementBatch = cb; 
					//remove the current batch	
					batches.remove();														
				}
				else {
					//there was only one wagon load in the batch, 
					//	which has now been transferred, so delete batch
					batches.remove();
				}			 
				
				TRANSFER_NOT_DONE = false; 	 
			}
		}
		
		if (!TRANSFER_NOT_DONE) {
			//transfer was done, the original batch was removed,
			//	now put the replacement batch in stationBundle
			stationAfter.setAmount(replacementBatch,amount-1);
		}
		
	}
	
	
	public boolean transferIfTheSameType(CargoBatch cb,int stationBatch, int cargoTransferType) {

		if (stationBatch == cargoTransferType) {
			//transfer a wagon load of this batch to train

		
			//put new batch on the train
			trainAfter.setAmount(cb,1);

			return true;
		}
		
		return false;
	}
	
	
	public void doCargoTransferMove() {
		//move cargo from station
	  	ChangeCargoBundleMove fromStation = new ChangeCargoBundleMove(stationBefore,
	  																  stationAfter,
	  																  stationBundleId
	  																  );
	  	MoveStatus stationMS = fromStation.doMove(w);

	  	//move cargo to train
	  	ChangeCargoBundleMove toTrain = new ChangeCargoBundleMove(trainBefore,
	  															  trainAfter,
	  															  trainBundleId
	  															  );
	  	MoveStatus trainMS = toTrain.doMove(w);	
	}
}
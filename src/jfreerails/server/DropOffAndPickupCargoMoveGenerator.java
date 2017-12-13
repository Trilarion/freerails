package jfreerails.server;

import java.util.Iterator;
import java.util.Map.Entry;

import jfreerails.move.AddTransactionMove;
import jfreerails.move.ChangeCargoBundleMove;
import jfreerails.move.Move;
import jfreerails.move.TransferCargoAtStationMove;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.station.ConvertedAtStation;
import jfreerails.world.station.DemandAtStation;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.WagonType;
import jfreerails.world.player.FreerailsPrincipal;

/**
 * This class generates moves that transfer cargo between train and the
 * stations it stops at - it also handles cargo converions that occur when
 * cargo is dropped off.
 * 
 * @author Scott Bennett
 * Date Created: 4 June 2003
 *
 */
class DropOffAndPickupCargoMoveGenerator {
    private ReadOnlyWorld w;
    private TrainModel train;
    private int trainId;
    private int trainBundleId;
    private FreerailsPrincipal trainPrincipal;
    private FreerailsPrincipal stationPrincipal;
    private int stationId;
    private int stationBundleId;
    private CargoBundle stationAfter;
    private CargoBundle stationBefore;
    private CargoBundle trainAfter;
    private CargoBundle trainBefore;
    private AddTransactionMove payment;

    /**
     * Contructor
     * @param trainNo ID of the train
     * @param stationNo ID of the station
     * @param world The world object
     * @param tp Player which owns the train
     * @param sp Player which owns the station
     */
    public DropOffAndPickupCargoMoveGenerator(FreerailsPrincipal tp,
	    int trainNo, FreerailsPrincipal sp, int stationNo, ReadOnlyWorld
	    world) {
        trainId = trainNo;
        stationId = stationNo;
	trainPrincipal = tp;
	stationPrincipal = sp;
        w = world;

        train = (TrainModel)w.get(KEY.TRAINS, trainId, trainPrincipal);

        getBundles();

        processTrainBundle(); //ie. unload train / dropoff cargo
        processStationBundle(); //ie. load train / pickup cargo
    }

    Move generateMove() {
	// The methods that calculate the before and after bundles could be
	// called from here.
	ChangeCargoBundleMove changeAtStation = new
	    ChangeCargoBundleMove(stationBefore, stationAfter,
		    stationBundleId);

	ChangeCargoBundleMove changeOnTrain = new
	    ChangeCargoBundleMove(trainBefore,
                trainAfter, trainBundleId);

        return TransferCargoAtStationMove.generateMove(changeAtStation,
            changeOnTrain, payment);
    }

    private void getBundles() {
        trainBundleId = ((TrainModel)w.get(KEY.TRAINS, trainId, trainPrincipal))
	    .getCargoBundleNumber();
        trainBefore = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, trainBundleId)).getCopy();
        trainAfter = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, trainBundleId)).getCopy();
	stationBundleId = ((StationModel)w.get(KEY.STATIONS, stationId,
		    stationPrincipal)).getCargoBundleNumber();
        stationAfter = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, stationBundleId)).getCopy();
        stationBefore = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, stationBundleId)).getCopy();
    }

    private void processTrainBundle() {
        Iterator batches = trainAfter.cargoBatchIterator();

	StationModel station = (StationModel)w.get(KEY.STATIONS, stationId,
		stationPrincipal);

        CargoBundle cargoDroppedOff = new CargoBundleImpl();

        //Unload the cargo that the station demands
        while (batches.hasNext()) {
            CargoBatch cb = (CargoBatch)((Entry) batches.next()).getKey();

            //if the cargo is demanded and its not from this station originally...
            DemandAtStation demand = station.getDemand();
            int cargoType = cb.getCargoType();

            if ((demand.isCargoDemanded(cargoType)) &&
                    (stationId != cb.getStationOfOrigin())) {
                int amount = trainAfter.getAmount(cb);
                cargoDroppedOff.addCargo(cb, amount);

                //Now perform any conversions..
                ConvertedAtStation converted = station.getConverted();

                if (converted.isCargoConverted(cargoType)) {
                    int newCargoType = converted.getConversion(cargoType);
                    CargoBatch newCargoBatch = new CargoBatch(newCargoType,
                            station.x, station.y, 0, stationId);
                    stationAfter.addCargo(newCargoBatch, amount);
                }

                batches.remove();
            }
        }

        payment = ProcessCargoAtStationMoveGenerator.processCargo(w,
		cargoDroppedOff, trainPrincipal, this.stationId,
		stationPrincipal);

        //Unload the cargo that there isn't space for on the train regardless of whether the station
        // demands it.
        int[] spaceAvailable = this.getSpaceAvailableOnTrain();

        for (int cargoType = 0; cargoType < spaceAvailable.length;
                cargoType++) {
            if (spaceAvailable[cargoType] < 0) {
                int amount2transfer = -spaceAvailable[cargoType];
                transferCargo(cargoType, amount2transfer, trainAfter,
                    stationAfter);
            }
        }
    }

    private void processStationBundle() {
        int[] spaceAvailable = getSpaceAvailableOnTrain();

        //Third, transfer cargo from the station to the train subject to the space available on the train.
        for (int cargoType = 0; cargoType < w.size(KEY.CARGO_TYPES);
                cargoType++) {
            int amount2transfer = Math.min(spaceAvailable[cargoType],
                    stationAfter.getAmount(cargoType));
            transferCargo(cargoType, amount2transfer, stationAfter, trainAfter);
        }
    }

    /**
     * @return array indexed by CARGO_TYPE indicating available space
     */
    private int[] getSpaceAvailableOnTrain() {
        //This array will store the amount of space available on the train for each cargo type. 
        int[] spaceAvailable = new int[w.size(KEY.CARGO_TYPES)];

        //First calculate the train's total capacity.
        for (int j = 0; j < train.getNumberOfWagons(); j++) {
	    WagonType wagonType = (WagonType) w.get(KEY.WAGON_TYPES,
		    train.getWagon(j));
	    int cargoType = wagonType.getCargoType();

            spaceAvailable[cargoType] += wagonType.getCapacity();
        }

        //Second, subtract the space taken up by cargo that the train is already carrying.
        for (int cargoType = 0; cargoType < w.size(KEY.CARGO_TYPES);
                cargoType++) {
            spaceAvailable[cargoType] -= trainAfter.getAmount(cargoType);
        }

        return spaceAvailable;
    }

    /**
     * Move the specified quantity of the specifed cargotype from one bundle to another.
     */
    private static void transferCargo(int cargoTypeToTransfer,
        int amountToTransfer, CargoBundle from, CargoBundle to) {
        if (0 == amountToTransfer) {
            return;
        } else {
            Iterator batches = from.cargoBatchIterator();
            int amountTransferedSoFar = 0;

            while (batches.hasNext() &&
                    amountTransferedSoFar < amountToTransfer) {
                CargoBatch cb = (CargoBatch)((Entry) batches.next()).getKey();

                if (cb.getCargoType() == cargoTypeToTransfer) {
                    int amount = from.getAmount(cb);
                    int amountOfThisBatchToTransfer;

                    if (amount < amountToTransfer - amountTransferedSoFar) {
                        amountOfThisBatchToTransfer = amount;
                        batches.remove();
                    } else {
                        amountOfThisBatchToTransfer = amountToTransfer -
                            amountTransferedSoFar;
                        from.addCargo(cb, -amountOfThisBatchToTransfer);
                    }

                    to.addCargo(cb, amountOfThisBatchToTransfer);
                    amountTransferedSoFar += amountOfThisBatchToTransfer;
                }
            }
        }
    }
}

package jfreerails.server;

import java.util.Iterator;
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


/**
 * This class generates moves that transfer cargo between train and the stations it stops at - it also
 * handles cargo converions that occur when cargo is dropped off.
 *
 * @author Scott Bennett
 * Date Created: 4 June 2003
 *
 */
public class DropOffAndPickupCargoMoveGenerator {
    private ReadOnlyWorld w;
    private TrainModel train;
    private int trainId;
    private int trainBundleId;
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
     */
    public DropOffAndPickupCargoMoveGenerator(int trainNo, int stationNo,
        ReadOnlyWorld world) {
        trainId = trainNo;
        stationId = stationNo;
        w = world;

        train = (TrainModel)w.get(KEY.TRAINS, trainId);

        getBundles();

        processTrainBundle(); //ie. unload train / dropoff cargo
        processStationBundle(); //ie. load train / pickup cargo
    }

    public Move generateMove() {
        //The methods that calculate the before and after bundles could be called from here.
        ChangeCargoBundleMove changeAtStation = new ChangeCargoBundleMove(stationBefore,
                stationAfter, stationBundleId);
        ChangeCargoBundleMove changeOnTrain = new ChangeCargoBundleMove(trainBefore,
                trainAfter, trainBundleId);

        return TransferCargoAtStationMove.generateMove(changeAtStation,
            changeOnTrain, payment);
    }

    public void getBundles() {
        trainBundleId = ((TrainModel)w.get(KEY.TRAINS, trainId)).getCargoBundleNumber();
        trainBefore = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, trainBundleId)).getCopy();
        trainAfter = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, trainBundleId)).getCopy();
        stationBundleId = ((StationModel)w.get(KEY.STATIONS, stationId)).getCargoBundleNumber();
        stationAfter = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, stationBundleId)).getCopy();
        stationBefore = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, stationBundleId)).getCopy();
    }

    public void processTrainBundle() {
        Iterator batches = trainAfter.cargoBatchIterator();

        StationModel station = (StationModel)w.get(KEY.STATIONS, stationId);

        CargoBundle cargoDroppedOff = new CargoBundleImpl();

        //Unload the cargo that the station demands
        while (batches.hasNext()) {
            CargoBatch cb = (CargoBatch)batches.next();

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
                cargoDroppedOff, this.stationId);

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

    public void processStationBundle() {
        int[] spaceAvailable = getSpaceAvailableOnTrain();

        //Third, transfer cargo from the station to the train subject to the space available on the train.
        for (int cargoType = 0; cargoType < w.size(KEY.CARGO_TYPES);
                cargoType++) {
            int amount2transfer = Math.min(spaceAvailable[cargoType],
                    stationAfter.getAmount(cargoType));
            transferCargo(cargoType, amount2transfer, stationAfter, trainAfter);
        }

        /*
        //loop through each wagon, see what can be put in them
        for (int j=0; j<train.getNumberOfWagons(); j++) {
                CargoType wagonCargoType = (CargoType)w.get(KEY.CARGO_TYPES,train.getWagon(j));


                //for each cargo type, compare wagon category with cargo waiting at station
                for (int k=0; k<w.size(KEY.CARGO_TYPES); k++) {
                        CargoType cargoType = (CargoType)w.get(KEY.CARGO_TYPES,k);

                        //compare cargo types wagon can carry, to cargo waiting type
                        if (wagonCargoType.getCategory().equals(cargoType.getCategory())) {

                                //check if there is any cargo of this type waiting
                                if (stationBefore.getAmount(k) > 0) {

                                        //transfer cargo to the current wagon
                                        transferCargo(k);
                                }
                        }
                }

        }
        */
    }

    private int[] getSpaceAvailableOnTrain() {
        //This array will store the amount of space available on the train for each cargo type. 
        int[] spaceAvailable = new int[w.size(KEY.CARGO_TYPES)];

        //First calculate the train's total capacity.
        for (int j = 0; j < train.getNumberOfWagons(); j++) {
            int cargoType = train.getWagon(j);

            spaceAvailable[cargoType] += WagonType.UNITS_OF_CARGO_PER_WAGON;
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
    public static void transferCargo(int cargoTypeToTransfer,
        int amountToTransfer, CargoBundle from, CargoBundle to) {
        if (0 == amountToTransfer) {
            return;
        } else {
            Iterator batches = from.cargoBatchIterator();
            int amountTransferedSoFar = 0;

            while (batches.hasNext() &&
                    amountTransferedSoFar < amountToTransfer) {
                CargoBatch cb = (CargoBatch)batches.next();

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

    /**
     * Do the transfer
     * @param cb The cargo batch being transferred
     * @param stationBatch The ID for the station's batch
     * @param cargoTransferType The ID for the cargo type
     * @return boolean
     */
    public boolean transferIfTheSameType(CargoBatch cb, int stationBatch,
        int cargoTransferType) {
        if (stationBatch == cargoTransferType) {
            //transfer a wagon load of this batch to train
            int currentAmount = trainAfter.getAmount(cb);

            //put new batch on the train
            trainAfter.setAmount(cb, currentAmount + 40);

            return true;
        }

        return false;
    }
}
package jfreerails.server;

import java.util.ArrayList;
import java.util.Iterator;
import jfreerails.move.ChangeCargoBundleMove;
import jfreerails.move.Move;
import jfreerails.move.TransferCargoAtStationMove;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.ConvertedAtStation;
import jfreerails.world.station.DemandAtStation;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.WagonType;


/**
 * This class generates moves that transfer cargo between train and the stations it stops at - it also
 * handles cargo conversions that occur when cargo is dropped off.
 *
 * @author Scott Bennett
 * @author Luke Lindsay
 * Date Created: 4 June 2003
 *
 */
public class DropOffAndPickupCargoMoveGenerator {
    private final ReadOnlyWorld w;
    private final TrainModel train;
    private final int trainId;
    private int trainBundleId;
    private final int stationId;
    private int stationBundleId;
    private CargoBundle stationAfter;
    private CargoBundle stationBefore;
    private CargoBundle trainAfter;
    private CargoBundle trainBefore;
    private ArrayList moves;
    private final FreerailsPrincipal principal;
    private boolean waitingForFullLoad;

    /**
     * Contructor.
     * @param trainNo ID of the train
     * @param stationNo ID of the station
     * @param world The world object
     */
    public DropOffAndPickupCargoMoveGenerator(int trainNo, int stationNo,
        ReadOnlyWorld world, FreerailsPrincipal p, boolean waiting) {
        principal = p;
        trainId = trainNo;
        stationId = stationNo;
        w = world;
        this.waitingForFullLoad = waiting;

        train = (TrainModel)w.get(KEY.TRAINS, trainId, principal);

        getBundles();

        processTrainBundle(); //ie. unload train / dropoff cargo
        processStationBundle(); //ie. load train / pickup cargo
    }

    public Move generateMove() {
        //The methods that calculate the before and after bundles could be called from here.
        ChangeCargoBundleMove changeAtStation = new ChangeCargoBundleMove(stationBefore,
                stationAfter, stationBundleId, principal);
        ChangeCargoBundleMove changeOnTrain = new ChangeCargoBundleMove(trainBefore,
                trainAfter, trainBundleId, principal);

        moves.add(TransferCargoAtStationMove.CHANGE_AT_STATION_INDEX,
            changeAtStation);
        moves.add(TransferCargoAtStationMove.CHANGE_ON_TRAIN_INDEX,
            changeOnTrain);

        TransferCargoAtStationMove move = new TransferCargoAtStationMove(moves,
                waitingForFullLoad);
        assert move.getChangeAtStation() == changeAtStation;
        assert move.getChangeOnTrain() == changeOnTrain;

        return move;
    }

    private void getBundles() {
        trainBundleId = ((TrainModel)w.get(KEY.TRAINS, trainId, principal)).getCargoBundleNumber();
        trainBefore = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, trainBundleId,
                principal)).getCopy();
        trainAfter = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, trainBundleId,
                principal)).getCopy();
        stationBundleId = ((StationModel)w.get(KEY.STATIONS, stationId,
                principal)).getCargoBundleNumber();
        stationAfter = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, stationBundleId,
                principal)).getCopy();
        stationBefore = ((CargoBundle)w.get(KEY.CARGO_BUNDLES, stationBundleId,
                principal)).getCopy();
    }

    private void processTrainBundle() {
        Iterator batches = trainAfter.getCopy().cargoBatchIterator();

        StationModel station = (StationModel)w.get(KEY.STATIONS, stationId,
                principal);

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

                trainAfter.setAmount(cb, 0);
            }
        }

        moves = ProcessCargoAtStationMoveGenerator.processCargo(w,
                cargoDroppedOff, this.stationId, principal);

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
        for (int cargoType = 0; cargoType < w.size(SKEY.CARGO_TYPES);
                cargoType++) {
            int amount2transfer = Math.min(spaceAvailable[cargoType],
                    stationAfter.getAmount(cargoType));
            transferCargo(cargoType, amount2transfer, stationAfter, trainAfter);
        }

        /*
        //loop through each wagon, see what can be put in them
        for (int j=0; j<train.getNumberOfWagons(); j++) {
                CargoType wagonCargoType = (CargoType)w.get(SKEY.CARGO_TYPES,train.getWagon(j));


                //for each cargo type, compare wagon category with cargo waiting at station
                for (int k=0; k<w.size(SKEY.CARGO_TYPES); k++) {
                        CargoType cargoType = (CargoType)w.get(SKEY.CARGO_TYPES,k);

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
        int[] spaceAvailable = new int[w.size(SKEY.CARGO_TYPES)];

        //First calculate the train's total capacity.
        for (int j = 0; j < train.getNumberOfWagons(); j++) {
            int cargoType = train.getWagon(j);

            spaceAvailable[cargoType] += WagonType.UNITS_OF_CARGO_PER_WAGON;
        }

        //Second, subtract the space taken up by cargo that the train is already carrying.
        for (int cargoType = 0; cargoType < w.size(SKEY.CARGO_TYPES);
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
            Iterator batches = from.getCopy().cargoBatchIterator();
            int amountTransferedSoFar = 0;

            while (batches.hasNext() &&
                    amountTransferedSoFar < amountToTransfer) {
                CargoBatch cb = (CargoBatch)batches.next();

                if (cb.getCargoType() == cargoTypeToTransfer) {
                    int amount = from.getAmount(cb);
                    int amountOfThisBatchToTransfer;

                    if (amount < amountToTransfer - amountTransferedSoFar) {
                        amountOfThisBatchToTransfer = amount;
                        from.setAmount(cb, 0);
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
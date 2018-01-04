/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.server;

import freerails.controller.*;
import freerails.move.ChangeProductionAtEngineShopMove;
import freerails.move.Move;
import freerails.network.MoveReceiver;
import freerails.util.ImInts;
import freerails.util.ImList;
import freerails.util.ImPoint;
import freerails.world.KEY;
import freerails.world.NonNullElementWorldIterator;
import freerails.world.ReadOnlyWorld;
import freerails.world.WorldIterator;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.PlannedTrain;
import freerails.world.station.StationModel;
import freerails.world.train.ImmutableSchedule;
import freerails.world.train.MutableSchedule;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainOrdersModel;

import java.util.ArrayList;

/**
 * This class is used by the server to generate moves that add trains, move
 * trains, and handle stops at stations. Note, the client should not use this
 * class to build trains, instead it should request that a train gets built by
 * setting production at an engine shop.
 */
public class TrainUpdater implements ServerAutomaton {

    private static final long serialVersionUID = 3258410646839243577L;
    private transient MoveReceiver moveReceiver;

    /**
     * @param mr
     */
    public TrainUpdater(MoveReceiver mr) {
        moveReceiver = mr;

        if (null == mr) {
            throw new NullPointerException();
        }

    }

    /**
     * @param engineTypeId
     * @param wagons
     * @param p
     * @param principal
     * @param world
     */
    public void buildTrain(int engineTypeId, ImInts wagons, ImPoint p,
                           FreerailsPrincipal principal, ReadOnlyWorld world) {

        // If there are no wagons, setup an automatic schedule.
        boolean autoSchedule = 0 == wagons.size();

        ImmutableSchedule is = generateInitialSchedule(principal, world,
                autoSchedule);

        PreMove addTrain = new AddTrainPreMove(engineTypeId, wagons, p,
                principal, is);

        Move m = addTrain.generateMove(world);
        moveReceiver.process(m);

    }

    // /**
    // * Generates a composite move that adds a train to the train list, adds a
    // * cargo bundle for the train to the cargo bundles list, and sets the
    // * train's initial position. The move is sent to the moveProcessor and a
    // * TrainMover object to update the trains position is returned.
    // *
    // * @param engineTypeId
    // * type of the engine
    // * @param wagons
    // * array of wagon types
    // * @param p
    // * point at which to add train on map.
    // *
    // *
    // */
    // public TrainMover buildTrain(int engineTypeId, ImInts wagons, ImPoint p,
    // FreerailsPrincipal principal, ReadOnlyWorld world) {
    // /* Check that the specified position is on the track. */
    // FreerailsTile tile = (FreerailsTile) world.getTile(p.x, p.y);
    // if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != tile.getTrackTypeID()) {
    // /* Create the move that sets up the train's cargo bundle. */
    // int cargoBundleId = world.size(principal, KEY.CARGO_BUNDLES);
    // Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleId,
    // ImmutableCargoBundle.EMPTY_BUNDLE, principal);
    //
    // /* Create the train model object. */
    // int scheduleId = world.size(principal, KEY.TRAIN_SCHEDULES);
    //
    // TrainModel train = new TrainModel(engineTypeId, wagons, scheduleId,
    // cargoBundleId);
    //
    // /* Create the move that sets up the train's schedule. */
    //
    // // If there are no wagons, setup an automatic schedule.
    // boolean autoSchedule = 0 == wagons.size();
    //
    // ImmutableSchedule is = generateInitialSchedule(principal, world,
    // autoSchedule);
    // int trainId = world.size(principal, KEY.TRAINS);
    // Move setupScheduleMove = TrainUpdater.initTarget(train, trainId,
    // is, principal);
    //
    // /* Create the move that sets the train's initial position. */
    // FreerailsPathIterator from = getRandomPathToFollow(p, world);
    // TrainPositionOnMap initialPosition = TrainUpdater
    // .setInitialTrainPosition(train, from);
    // Move positionMove = new InitialiseTrainPositionMove(trainId,
    // initialPosition, principal);
    //
    // /* Determine the price of the train. */
    // EngineType engineType = (EngineType) world.get(SKEY.ENGINE_TYPES,
    // engineTypeId);
    // Money price = engineType.getPrice();
    //
    // /* Create the move that adds the train to the train list. */
    // AddTrainMove addTrainMove = AddTrainMove.generateMove(trainId,
    // train, price, is, principal);
    //
    // /* Create a composite move made up of the moves created above. */
    // Move compositeMove = new CompositeMove(new Move[] {
    // addCargoBundleMove, addTrainMove, setupScheduleMove });
    //
    // /* Execute the move. */
    // moveReceiver.process(compositeMove);
    // moveReceiver.process(positionMove);
    //
    // /* Create a TrainMover to update the train's position. */
    // TrainPathFinder tpf = getPathToFollow(p, world, trainId, principal);
    // TrainMover trainMover = new TrainMover(tpf, world, trainId,
    // principal);
    //
    // return trainMover;
    // }
    // throw new IllegalArgumentException("No track here (" + p.x + ", " + p.y
    // + ") so cannot build train");
    // }

    /**
     * Iterator over the stations and build trains at any that have their
     * production field set.
     */
    void buildTrains(ReadOnlyWorld world) {
        for (int k = 0; k < world.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = world.getPlayer(k).getPrincipal();

            for (int i = 0; i < world.size(principal, KEY.STATIONS); i++) {
                StationModel station = (StationModel) world.get(principal,
                        KEY.STATIONS, i);
                if (null != station) {

                    ImList<PlannedTrain> production = station.getProduction();

                    if (production.size() > 0) {

                        ImPoint p = new ImPoint(station.x, station.y);

                        for (int j = 0; j < production.size(); j++) {
                            int engineType = production.get(j).getEngineType();
                            ImInts wagonTypes = production.get(j)
                                    .getWagonTypes();
                            this.buildTrain(engineType, wagonTypes, p,
                                    principal, world);
                            // TrainMover trainMover =
                            // this.buildTrain(engineType, wagonTypes, p,
                            // principal, world);

                            // this.addTrainMover(trainMover);
                        }

                        ChangeProductionAtEngineShopMove move = new ChangeProductionAtEngineShopMove(
                                production, new ImList<>(), i,
                                principal);
                        moveReceiver.process(move);
                    }
                }
            }
        }
    }

    private ImmutableSchedule generateInitialSchedule(
            FreerailsPrincipal principal, ReadOnlyWorld world,
            boolean autoSchedule) {
        WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, world, principal);

        MutableSchedule s = new MutableSchedule();

        // Add up to 4 stations to the schedule.
        while (wi.next() && s.getNumOrders() < 5) {
            TrainOrdersModel orders = new TrainOrdersModel(wi.getIndex(), null,
                    false, autoSchedule);
            s.addOrder(orders);
        }

        s.setOrderToGoto(0);

        return s.toImmutableSchedule();
    }

    public void initAutomaton(MoveReceiver mr) {
        moveReceiver = mr;

    }

    void moveTrains(ReadOnlyWorld world) {
        int time = world.currentTime().getTicks();

        for (int k = 0; k < world.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = world.getPlayer(k).getPrincipal();
            OccupiedTracks occupiedTracks = new OccupiedTracks(principal, world);
            // If a train is moving, we want it to keep moving rather than stop
            // to allow an already stationary train to start moving. To achieve
            // this
            // we process moving trains first.
            ArrayList<MoveTrainPreMove> movingTrains = new ArrayList<>();
            ArrayList<MoveTrainPreMove> stoppedTrains = new ArrayList<>();
            for (int i = 0; i < world.size(principal, KEY.TRAINS); i++) {

                TrainModel train = (TrainModel) world.get(principal,
                        KEY.TRAINS, i);
                if (null == train)
                    continue;

                MoveTrainPreMove moveTrain = new MoveTrainPreMove(i, principal,
                        occupiedTracks);
                if (moveTrain.isUpdateDue(world)) {
                    TrainAccessor ta = new TrainAccessor(world, principal, i);
                    if (ta.isMoving(time)) {
                        movingTrains.add(moveTrain);
                    } else {
                        stoppedTrains.add(moveTrain);
                    }

                }
            }
            for (MoveTrainPreMove preMove : movingTrains) {
                Move m;
                try {
                    m = preMove.generateMove(world);
                } catch (NoTrackException e) {
                    continue; // user deleted track, continue and ignore
                    // train!
                }
                moveReceiver.process(m);
            }
            for (MoveTrainPreMove preMove : stoppedTrains) {
                Move m = preMove.generateMove(world);
                moveReceiver.process(m);
            }
        }

    }
}
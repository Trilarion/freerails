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
import freerails.util.Point2D;
import freerails.world.KEY;
import freerails.world.NonNullElementWorldIterator;
import freerails.world.ReadOnlyWorld;
import freerails.world.WorldIterator;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.TrainBlueprint;
import freerails.world.station.Station;
import freerails.world.train.ImmutableSchedule;
import freerails.world.train.MutableSchedule;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainOrdersModel;

import java.util.ArrayList;

/**
 * Is used by the server to generate moves that add trains, move
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
    public void buildTrain(int engineTypeId, ImInts wagons, Point2D p,
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

    /**
     * Iterator over the stations and build trains at any that have their
     * production field set.
     */
    void buildTrains(ReadOnlyWorld world) {
        for (int k = 0; k < world.getNumberOfPlayers(); k++) {
            FreerailsPrincipal principal = world.getPlayer(k).getPrincipal();

            for (int i = 0; i < world.size(principal, KEY.STATIONS); i++) {
                Station station = (Station) world.get(principal,
                        KEY.STATIONS, i);
                if (null != station) {

                    ImList<TrainBlueprint> production = station.getProduction();

                    if (production.size() > 0) {

                        Point2D p = new Point2D(station.x, station.y);

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
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

import freerails.model.track.OccupiedTracks;
import freerails.model.train.schedule.TrainOrder;
import freerails.move.*;
import freerails.move.generator.AddTrainMoveGenerator;
import freerails.move.generator.MoveTrainMoveGenerator;
import freerails.move.generator.MoveGenerator;
import freerails.move.receiver.MoveReceiver;

import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.track.NoTrackException;
import freerails.model.train.*;
import freerails.model.train.schedule.Schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

// TODO why should the client not use this? include this in the full server game model
/**
 * Is used by the server to generate moves that add trains, move trains, and handle stops at stations.
 *
 * Note: The client should not use this class to build trains, instead it should request that a train gets built by
 * setting production at an engine shop.
 */
public class TrainUpdater implements Serializable {

    private static final long serialVersionUID = 3258410646839243577L;
    private transient MoveReceiver moveReceiver;

    /**
     * @param moveReceiver
     */
    public TrainUpdater(MoveReceiver moveReceiver) {
        this.moveReceiver = Utils.verifyNotNull(moveReceiver);
    }

    /**
     * @param engineId
     * @param wagons
     * @param location
     * @param player
     * @param world
     */
    private void buildTrain(int engineId, List<Integer> wagons, Vec2D location, Player player, UnmodifiableWorld world) {

        // If there are no wagons, setup an automatic schedule.
        boolean autoSchedule = 0 == wagons.size();

        // generate initial schedule
        Schedule schedule = new Schedule();

        // Add up to 4 stations to the schedule.
        Iterator<Station> wi = world.getStations(player).iterator();
        while (wi.hasNext() && schedule.getNumberOfOrders() < 5) {
            TrainOrder orders = new TrainOrder(wi.next().getId(), null, false, autoSchedule);
            schedule.addOrder(orders);
        }

        schedule.setOrderToGoto(0);

        MoveGenerator addTrain = new AddTrainMoveGenerator(engineId, wagons, location, player, schedule);

        Move move = addTrain.generate(world);
        moveReceiver.process(move);
    }

    /**
     * Iterator over the stations and build trains at any that have their
     * production field set.
     */
    public void buildTrains(UnmodifiableWorld world) {
        // for all player
        for (Player player: world.getPlayers()) {
            // for all stations of that player
            for (Station station: world.getStations(player)) {
                List<TrainTemplate> production = station.getProduction();
                if (production.size() > 0) {

                    for (TrainTemplate aProduction : production) {
                        int engineId = aProduction.getEngineId();
                        List<Integer> wagonTypes = aProduction.getWagonTypes();
                        buildTrain(engineId, wagonTypes, station.getLocation(), player, world);
                    }

                    Move move = new ChangeProductionAtEngineShopMove(production, new ArrayList<>(), station.getId(), player);
                    moveReceiver.process(move);
                }
            }
        }
    }

    public void moveTrains(UnmodifiableWorld world) {
        int time = world.getClock().getCurrentTime().getTicks();

        for (Player player: world.getPlayers()) {
            OccupiedTracks occupiedTracks = new OccupiedTracks(player, world);
            // If a train is moving, we want it to keep moving rather than stop
            // to allow an already stationary train to start moving. To achieve
            // this
            // we process moving trains first.
            Collection<MoveTrainMoveGenerator> movingTrains = new ArrayList<>();
            Collection<MoveTrainMoveGenerator> stoppedTrains = new ArrayList<>();
            for (int i = 0; i < world.getTrains(player).size(); i++) {

                Train train = world.getTrain(player, i);
                // TODO this should never happen, we should not check here
                if (null == train) continue;

                MoveTrainMoveGenerator moveTrain = new MoveTrainMoveGenerator(i, player, occupiedTracks);
                if (moveTrain.isUpdateDue(world)) {
                    TrainAccessor ta = new TrainAccessor(world, player, i);
                    if (ta.isMoving(time)) {
                        movingTrains.add(moveTrain);
                    } else {
                        stoppedTrains.add(moveTrain);
                    }
                }
            }
            for (MoveTrainMoveGenerator preMove : movingTrains) {
                Move move;
                try {
                    move = preMove.generate(world);
                } catch (NoTrackException e) {
                    continue; // user deleted track, continue and ignore
                    // train!
                }
                moveReceiver.process(move);
            }
            for (MoveTrainMoveGenerator preMove : stoppedTrains) {
                Move move = preMove.generate(world);
                moveReceiver.process(move);
            }
        }
    }
}
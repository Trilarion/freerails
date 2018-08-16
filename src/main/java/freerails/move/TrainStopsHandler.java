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

/*
 *
 */
package freerails.move;

import freerails.model.station.StationUtils;
import freerails.model.train.motion.TrainMotion;
import freerails.model.train.schedule.TrainOrder;
import freerails.model.world.World;
import freerails.move.generator.DropOffAndPickupCargoMoveGenerator;

import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.train.*;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.UnmodifiableSchedule;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO Remove use of World from here (remnant from WorldDiffs from here), use readonly world instead if possible
/**
 * A lot of handling (generating moves) for trains.
 */
public class TrainStopsHandler implements Serializable {

    private static final Logger logger = Logger.getLogger(TrainStopsHandler.class.getName());
    private static final long serialVersionUID = 3257567287094882872L;
    private final Player player;
    private final int trainId;
    private World world;
    private final List<Move> moves = new ArrayList<>();
    private final UnmodifiableWorld unmodifiableWorld;

    /**
     * @param id
     * @param player
     * @param unmodifiableWorld
     */
    public TrainStopsHandler(int id, Player player, UnmodifiableWorld unmodifiableWorld) {
        trainId = id;
        this.player = player;
        this.world =  (World) Utils.cloneBySerialisation(unmodifiableWorld);
        this.unmodifiableWorld = unmodifiableWorld;
    }

    /**
     * @return
     */
    public void arrivesAtPoint(Vec2D location) {
        Vec2D targetPoint = TrainUtils.getTargetLocation(world, player, trainId);

        if (location.equals(targetPoint)) {
            /*
             * Issues a ChangeTrainScheduleMove to set the train to move to the next
             * station.
             */
            scheduledStop();
        } else {
            // not a scheduled stop but still a city
            int stationNumber = StationUtils.getStationId(world, player, location);
            if (-1 != stationNumber) {
                loadAndUnloadCargo(stationNumber, false, false);
            }
        }
    }

    /**
     * Returns all accumulated moves.
     *
     * @return
     */
    public List<Move> getMoves() {
        List<Move> currentMoves = new ArrayList<>(moves);
        moves.clear();
        world = (World) Utils.cloneBySerialisation(unmodifiableWorld);
        return currentMoves;
    }

    /**
     * @return
     */
    public int getTrainLength() {
        return world.getTrain(player, trainId).getLength();
    }

    private void loadAndUnloadCargo(int stationId, boolean waiting, boolean autoConsist) {

        // train is at a station so do the cargo processing
        DropOffAndPickupCargoMoveGenerator transfer = new DropOffAndPickupCargoMoveGenerator(trainId, stationId, world, player, waiting, autoConsist);
        Move move = transfer.generate();
        if (null != move) {
            moves.add(move);
            Status status = move.doMove(world, player);
            if (!status.succeeds()) throw new IllegalStateException(status.getMessage());
        }
    }

    // TODO not yet implemented
    public void makeTrainWait(int ticks) {}

    /**
     * @return
     */
    public boolean refreshWaitingForFullLoad() {

        TrainAccessor trainAccessor = new TrainAccessor(world, player, trainId);
        Train train = world.getTrain(player, trainId);
        UnmodifiableSchedule schedule = train.getSchedule();

        int stationId = trainAccessor.getStationId(Double.MAX_VALUE);
        if (stationId < 0) throw new IllegalStateException();

        // The train's orders may have changed...
        TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());

        // Should we go to another station?
        if (stationId != order.getStationId()) {
            return false;
        }

        // Should we change the consist?
        List<Integer> consist = train.getConsist();
        if (!consist.equals(order.getConsist())) {
            // ..if so, we should change the consist.
            int oldLength = train.getLength();
            int engineId = train.getEngineId();

            // TODO does this change the consist?
            // TODO newTrain is computed in the ChangeTrainMove also
            // TODO need a way to get a new id for trains, this is not the best way so far
            int id = world.getTrains(player).size();
            Train newTrain = new Train(id, engineId, order.getConsist(), train.getCargoBatchBundle(), train.getSchedule());
            // worldDiffs.set(player, PlayerKey.Trains, trainId, newTrain);
            Train after = new Train(id, engineId, order.getConsist(), train.getCargoBatchBundle(), train.getSchedule());
            // TODO need dedicated change train move instead
            // Move move = new ChangeItemInListMove(PlayerKey.Trains, trainId, before, after, player);
            Move move = new ChangeTrainMove(player, after);
            move.doMove(world, player);
            moves.add(move);

            int newLength = newTrain.getLength();
            // has the trains length increased?
            if (newLength > oldLength) {
                TrainMotion trainMotion = trainAccessor.findCurrentMotion(Double.MAX_VALUE);
                PathOnTiles path = trainMotion.getPath();
                path = TrainUtils.lengthenPath(world, path, oldLength);
                TrainState status = TrainUtils.isWaitingForFullLoad(world, player, trainId) ? TrainState.WAITING_FOR_FULL_LOAD : TrainState.STOPPED_AT_STATION;
                TrainMotion nextMotion = new TrainMotion(path, newLength, 0, status);

                // Create a new Move object.
                Move trainMove = new NextActivityMove(nextMotion, trainId, player);
                moves.add(trainMove);
                Status moveStatus = trainMove.doMove(world, Player.AUTHORITATIVE);
                if (!moveStatus.succeeds()) throw new IllegalStateException(moveStatus.getMessage());
            }
        }

        // Add any cargo that is waiting.
        loadAndUnloadCargo(schedule.getNextStationId(), order.isWaitUntilFull(), order.isAutoConsist());

        // Should we stop waiting?
        if (!order.isWaitUntilFull() || TrainUtils.isTrainFull(world, player, trainId)) {
            updateSchedule();
            return false;
        }

        return true;
    }

    private void scheduledStop() {

        Train train = world.getTrain(player, trainId);
        UnmodifiableSchedule schedule = train.getSchedule();

        List<Integer> wagonsToAdd = schedule.getWagonsToAdd();

        // Loading and unloading cargo takes time, so we make the train wait for
        // a few ticks.
        makeTrainWait(50);

        boolean autoConsist = schedule.autoConsist();

        if (null != wagonsToAdd) {
            int engineType = train.getEngineId();
            // TODO need a way to get a new id for trains, this is not the best way so far
            int id = world.getTrains(player).size();
            Train after = new Train(trainId, engineType, wagonsToAdd, train.getCargoBatchBundle(), train.getSchedule());
            // TODO need dedicated change train move
            // Move move = new ChangeItemInListMove(PlayerKey.Trains, trainId, train, after, player);
            Move move = new ChangeTrainMove(player, after);
            // TODO instead of doing the move, add them to a list
            moves.add(move);
            move.doMove(world, player);
        }
        updateSchedule();
        int stationToGoto = schedule.getNextStationId();
        loadAndUnloadCargo(stationToGoto, true, autoConsist);
    }

    private void updateSchedule() {
        Train train =  world.getTrain(player, trainId);
        Schedule schedule = new Schedule(train.getSchedule());

        TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());
        boolean waitingForFullLoad = order.isWaitUntilFull() && !TrainUtils.isTrainFull(world, player, trainId);

        if (!waitingForFullLoad) {
            // TODO needs a mutable schedule
            schedule.gotoNextStation();
            train.setSchedule(schedule);
            Move move = new ChangeTrainMove(player, train);
            move.doMove(world, player);
            moves.add(move);

            int stationNumber = schedule.getNextStationId();
            Station station = world.getStation(player, stationNumber);

            if (null == station) {
                logger.warn("null == station, train " + trainId + " doesn't know where to go next!");
            }
        }
    }

}

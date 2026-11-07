/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.simulation.Time;
import freerails.model.operations.station.StationUtils;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.world.World;
import freerails.move.generator.DropOffAndPickupCargoMoveGenerator;

import freerails.util.IntPoint2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.*;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO Remove use of World from here (remnant from WorldDiffs from here), use readonly world instead if possible
/**
 * A lot of handling (generating moves) for trains.
 */
public class TrainStopsHandler implements Serializable {

    private static final TaggedLogger logger = Logger.tag("TrainStopsHandler");
    @Serial
    private static final long serialVersionUID = 3257567287094882872L;
    private final Player player;
    private final int trainId;
    private World world;
    private final List<Move> moves = new ArrayList<>();
    private final UnmodifiableWorld unmodifiableWorld;

    /**
     * @param id the identifier of the train to inspect
     * @param player the player who owns or performs the operation
     * @param unmodifiableWorld the read-only game world used to inspect the train and its stops
     */
    public TrainStopsHandler(int id, Player player, UnmodifiableWorld unmodifiableWorld) {
        trainId = id;
        this.player = player;
        world = (World) Utils.cloneBySerialisation(unmodifiableWorld);
        this.unmodifiableWorld = unmodifiableWorld;
    }

    /**
     * @param location the location at which the train has arrived
     */
    public void arrivesAtPoint(IntPoint2D location) {
        IntPoint2D targetPoint = TrainUtils.getTargetLocation(world, player, trainId);

        if (location.equals(targetPoint)) {
            /*
             * Issues a ChangeTrainScheduleMove to set the train to move to the next
             * station.
             */

            // scheduled stop
            Train train = world.getTrain(player, trainId);
            UnmodifiableSchedule schedule = train.getSchedule();

            TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());

            // Loading and unloading cargo takes time, so we make the train wait for a few ticks.
            // makeTrainWait not implemented
            // makeTrainWait(50);

            boolean autoConsist = order.isAutoConsist();

            if (order.hasSpecifiedConsist()) {
                Move move = new UpdateTrainMove(player, trainId, null, order.getConsist(), null);
                moves.add(move);
                move.apply(world);
            }
            updateSchedule();
            int stationToGoto = schedule.getNextStationId();
            loadAndUnloadCargo(stationToGoto, true, autoConsist);
        } else {
            // not a scheduled stop but still a city
            Integer stationId = StationUtils.getStationId(world, player, location);
            if (stationId != null) {
                loadAndUnloadCargo(stationId, false, false);
            }
        }
    }

    /**
     * Returns all accumulated moves.
     *
     * @return the moves generated for the train at its current stop
     */
    public List<Move> getMoves() {
        List<Move> currentMoves = new ArrayList<>(moves);
        moves.clear();
        // TODO this effectively undoes all the moves on the local world copy
        world = (World) Utils.cloneBySerialisation(unmodifiableWorld);
        return currentMoves;
    }

    // TODO this has to remain here until the code is simplified, because it works on the updated train
    /**
     * @return the train length in wagon units
     */
    public int getTrainLength() {
        return world.getTrain(player, trainId).getLength();
    }

    /**
     * Loads load and unload cargo from the configured source.
     *
     * @param stationId the identifier to use
     * @param waiting the waiting
     * @param autoConsist the auto consist
     */
    private void loadAndUnloadCargo(int stationId, boolean waiting, boolean autoConsist) {

        // train is at a station so do the cargo processing
        DropOffAndPickupCargoMoveGenerator transfer = new DropOffAndPickupCargoMoveGenerator(trainId, stationId, world, player, waiting, autoConsist);
        Move move = transfer.generate();
        if (null != move) {
            moves.add(move);
            Status status = move.applicable(world);
            if (!status.isSuccess()) throw new IllegalStateException(status.getMessage());
            move.apply(world);
        }
    }

    /**
     * @return true if the train is still waiting for a full load, otherwise false
     */
    public boolean refreshWaitingForFullLoad() {

        Train train = world.getTrain(player, trainId);
        UnmodifiableSchedule schedule = train.getSchedule();
        Integer stationId = StationUtils.getStationId(world, player, train.getLocation(Time.INFINITY));
        if (stationId == null) throw new IllegalStateException();

        // The train's orders may have changed...
        TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());

        // Should we go to another station?
        if (stationId != order.getStationId()) {
            return false;
        }

        // Should we change the consist?
        List<Integer> consist = train.getConsist();
        if (order.hasSpecifiedConsist()
                && !consist.equals(order.getConsist())) {
            // ..if so, we should change the consist.
            int oldLength = train.getLength();
            Move move = new UpdateTrainMove(player, trainId, null, order.getConsist(), null);
            move.apply(world);
            moves.add(move);
            int newLength = train.getLength();
            // has the trains length increased?
            if (newLength > oldLength) {
                TrainMotion trainMotion = train.findCurrentMotion(Double.MAX_VALUE);
                PathOnTiles path = trainMotion.getPath();
                path = TrainUtils.lengthenPath(world, path, oldLength);
                TrainState status = TrainUtils.isWaitingForFullLoad(world, player, trainId) ? TrainState.WAITING_FOR_FULL_LOAD : TrainState.STOPPED_AT_STATION;
                TrainMotion nextMotion = new TrainMotion(path, newLength, 0, status);

                // Create a new Move object.
                Move trainMove = new AddActivityMove(player, trainId, nextMotion);
                moves.add(trainMove);
                Status moveStatus = trainMove.applicable(world);
                if (!moveStatus.isSuccess()) throw new IllegalStateException(moveStatus.getMessage());
                trainMove.apply(world);
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

    /**
     * Updates the update schedule using the supplied values.
     */
    private void updateSchedule() {
        Train train =  world.getTrain(player, trainId);
        ScheduleState schedule = new ScheduleState(train.getSchedule());

        TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());
        boolean waitingForFullLoad = order.isWaitUntilFull() && !TrainUtils.isTrainFull(world, player, trainId);

        if (!waitingForFullLoad) {
            schedule = schedule.gotoNextStation();
            Move move = new UpdateTrainMove(player, trainId, null, null, schedule);
            move.apply(world);
            moves.add(move);

            int stationNumber = schedule.getNextStationId();
            Station station = world.getStation(player, stationNumber);

            if (null == station) {
                logger.warn("null == station, train " + trainId + " doesn't know where to go next!");
            }
        }
    }

}

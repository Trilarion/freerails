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

import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.station.Station;
import freerails.model.operations.station.StationUtils;
import freerails.model.operations.train.PathOnTiles;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.TrainConfiguration;
import freerails.model.operations.train.TrainState;
import freerails.model.operations.train.TrainUtils;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.player.Player;
import freerails.model.simulation.Time;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.generator.DropOffAndPickupCargoMoveGenerator;
import freerails.util.IntPoint2D;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Plans train stop changes against a read-only world without applying moves.
 */
public class TrainStopsHandler implements Serializable {

    private static final TaggedLogger logger = Logger.tag("TrainStopsHandler");
    @Serial
    private static final long serialVersionUID = 3257567287094882872L;
    private final Player player;
    private final int trainId;
    private final UnmodifiableWorld world;
    private final List<Move> moves = new ArrayList<>();
    private TrainConfiguration resultingConfiguration;
    private ScheduleState resultingSchedule;
    private CargoBatchBundle resultingCargo;
    private boolean waitingForFullLoad;

    /**
     * @param id the identifier of the train to inspect
     * @param player the player who owns or performs the operation
     * @param unmodifiableWorld the read-only game world used to inspect the train and its stops
     */
    public TrainStopsHandler(int id, Player player, UnmodifiableWorld unmodifiableWorld) {
        trainId = id;
        this.player = player;
        world = unmodifiableWorld;
        Train train = world.getTrain(player, trainId);
        resultingConfiguration = train.getConfiguration();
        resultingSchedule = new ScheduleState(train.getSchedule());
        resultingCargo = new CargoBatchBundle(train.getCargoBatchBundle());
    }

    /**
     * @param location the location at which the train has arrived
     */
    public void arrivesAtPoint(IntPoint2D location) {
        IntPoint2D targetPoint = TrainUtils.getTargetLocation(world, player, trainId);

        if (location.equals(targetPoint)) {
            TrainOrder order = resultingSchedule.getOrder(resultingSchedule.getCurrentOrderIndex());
            int stationId = resultingSchedule.getNextStationId();

            // Loading and unloading cargo takes time, so we make the train wait for a few ticks.
            // makeTrainWait not implemented
            // makeTrainWait(50);

            boolean autoConsist = order.isAutoConsist();

            if (order.hasSpecifiedConsist()) {
                resultingConfiguration = resultingConfiguration.withWagonTypes(order.getConsist());
                addMove(new UpdateTrainMove(player, trainId, null, order.getConsist(), null));
            }
            updateSchedule();
            loadAndUnloadCargo(stationId, true, autoConsist);
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
        return currentMoves;
    }

    /**
     * Returns the train configuration resulting from the planned stop.
     *
     * @return the resulting train configuration
     */
    public TrainConfiguration getResultingTrainConfiguration() {
        return resultingConfiguration;
    }

    /**
     * Returns whether the planned train state remains waiting for a full load.
     *
     * @return {@code true} if the train remains waiting for a full load
     */
    public boolean isWaitingForFullLoad() {
        return waitingForFullLoad;
    }

    /**
     * Loads load and unload cargo from the configured source.
     *
     * @param stationId the identifier to use
     * @param waiting the waiting
     * @param autoConsist the auto consist
     */
    private void loadAndUnloadCargo(int stationId, boolean waiting, boolean autoConsist) {
        DropOffAndPickupCargoMoveGenerator transfer = new DropOffAndPickupCargoMoveGenerator(
                trainId, stationId, world, player, waiting, autoConsist, resultingSchedule,
                resultingCargo, resultingConfiguration);
        Move move = transfer.generate();
        if (null != move) {
            addMove(move);
        }
        resultingCargo = new CargoBatchBundle(transfer.getResultingTrainCargo());
        resultingConfiguration = resultingConfiguration.withWagonTypes(transfer.getResultingConsist());
    }

    /**
     * @return true if the train is still waiting for a full load, otherwise false
     */
    public boolean refreshWaitingForFullLoad() {

        Train train = world.getTrain(player, trainId);
        Integer stationId = StationUtils.getStationId(world, player, train.getLocation(Time.INFINITY));
        if (stationId == null) throw new IllegalStateException();

        // The train's orders may have changed...
        TrainOrder order = resultingSchedule.getOrder(resultingSchedule.getCurrentOrderIndex());

        // Should we go to another station?
        if (stationId != order.getStationId()) {
            return false;
        }

        // Should we change the consist?
        int oldLength = resultingConfiguration.getLength();
        List<Integer> consist = resultingConfiguration.wagonTypes();
        if (order.hasSpecifiedConsist()
                && !consist.equals(order.getConsist())) {
            resultingConfiguration = resultingConfiguration.withWagonTypes(order.getConsist());
            addMove(new UpdateTrainMove(player, trainId, null, order.getConsist(), null));
            int newLength = resultingConfiguration.getLength();
            // has the trains length increased?
            if (newLength > oldLength) {
                TrainMotion trainMotion = train.findCurrentMotion(Double.MAX_VALUE);
                PathOnTiles path = trainMotion.getPath();
                path = TrainUtils.lengthenPath(world, path, oldLength);
                waitingForFullLoad = TrainUtils.isWaitingForFullLoad(
                        world, resultingSchedule, resultingCargo, resultingConfiguration.wagonTypes());
                TrainState status = waitingForFullLoad ? TrainState.WAITING_FOR_FULL_LOAD : TrainState.STOPPED_AT_STATION;
                TrainMotion nextMotion = new TrainMotion(path, newLength, 0, status);

                addMove(new AddActivityMove(player, trainId, nextMotion));
            }
        }

        // Add any cargo that is waiting.
        loadAndUnloadCargo(resultingSchedule.getNextStationId(), order.isWaitUntilFull(), order.isAutoConsist());

        // Should we stop waiting?
        if (!order.isWaitUntilFull()
                || TrainUtils.isTrainFull(world, resultingCargo, resultingConfiguration.wagonTypes())) {
            updateSchedule();
            waitingForFullLoad = false;
            return false;
        }

        waitingForFullLoad = true;
        return true;
    }

    /**
     * Advances the planned schedule unless its current order is still waiting for cargo.
     */
    private void updateSchedule() {
        waitingForFullLoad = TrainUtils.isWaitingForFullLoad(
                world, resultingSchedule, resultingCargo, resultingConfiguration.wagonTypes());

        if (!waitingForFullLoad) {
            resultingSchedule = resultingSchedule.gotoNextStation();
            addMove(new UpdateTrainMove(player, trainId, null, null, resultingSchedule));

            int stationNumber = resultingSchedule.getNextStationId();
            Station station = world.getStation(player, stationNumber);

            if (null == station) {
                logger.warn("null == station, train " + trainId + " doesn't know where to go next!");
            }
        }
    }

    /**
     * Adds a planned move after checking it against the original read-only world.
     *
     * @param move move to add
     */
    private void addMove(Move move) {
        Status status = move.applicable(world);
        if (!status.isSuccess()) {
            throw new IllegalStateException(status.getMessage());
        }
        moves.add(move);
    }

}

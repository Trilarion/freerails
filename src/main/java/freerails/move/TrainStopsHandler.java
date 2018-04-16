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

import freerails.model.track.explorer.FlatTrackExplorer;
import freerails.move.listmove.ChangeTrainMove;
import freerails.move.listmove.ChangeTrainScheduleMove;
import freerails.move.mapupdatemove.WorldDiffMove;
import freerails.model.track.NoTrackException;
import freerails.util.ImmutableList;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.PlayerKey;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.FullWorldDiffs;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.TileTransition;
import freerails.model.train.*;
import freerails.model.train.schedule.ImmutableSchedule;
import freerails.model.train.schedule.MutableSchedule;
import freerails.model.train.schedule.Schedule;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A lot of handling (generating moves) for trains.
 */
public class TrainStopsHandler implements Serializable {

    private static final Logger logger = Logger.getLogger(TrainStopsHandler.class.getName());
    private static final int NOT_AT_STATION = -1;
    private static final long serialVersionUID = 3257567287094882872L;
    private final FreerailsPrincipal principal;
    private final int trainId;
    private final FullWorldDiffs worldDiffs;
    private final List<Move> moves = new ArrayList<>();
    private final ReadOnlyWorld world;

    /**
     * @param id
     * @param principal
     * @param world
     */
    public TrainStopsHandler(int id, FreerailsPrincipal principal, ReadOnlyWorld world) {
        trainId = id;
        this.principal = principal;
        this.worldDiffs = new FullWorldDiffs(world);
        this.world = world;
    }

    /**
     * If wagons are added to a train, we need to increase its length.
     */
    public static PathOnTiles lengthenPath(ReadOnlyWorld world, PathOnTiles path, int currentTrainLength) {
        double pathDistance = path.getTotalDistance();
        double extraDistanceNeeded = currentTrainLength - pathDistance;

        List<TileTransition> tileTransitions = new ArrayList<>();
        Vec2D start = path.getStart();
        TileTransition firstTileTransition = path.getStep(0);
        PositionOnTrack nextPositionOnTrack = PositionOnTrack.createComingFrom(start, firstTileTransition);

        while (extraDistanceNeeded > 0) {

            FlatTrackExplorer flatTrackExplorer;
            try {
                flatTrackExplorer = new FlatTrackExplorer(world, nextPositionOnTrack);
            } catch (NoTrackException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            flatTrackExplorer.nextEdge();
            nextPositionOnTrack.setValuesFromInt(flatTrackExplorer.getVertexConnectedByEdge());
            TileTransition cameFrom = nextPositionOnTrack.facing();
            tileTransitions.add(0, cameFrom);
            extraDistanceNeeded -= cameFrom.getLength();
        }

        // Add existing tileTransitions
        for (int i = 0; i < path.steps(); i++) {
            TileTransition tileTransition = path.getStep(i);
            tileTransitions.add(tileTransition);
        }

        path = new PathOnTiles(nextPositionOnTrack.getLocation(), tileTransitions);
        return path;
    }

    /**
     * @return
     */
    public void arrivesAtPoint(Vec2D location) {
        TrainAccessor trainAccessor = new TrainAccessor(worldDiffs, principal, trainId);
        Vec2D targetPoint = trainAccessor.getTargetLocation();

        if (location.equals(targetPoint)) {
            /*
             * Issues a ChangeTrainScheduleMove to set the train to move to the next
             * station.
             */
            scheduledStop();
        } else {
            // not a scheduled stop but still a city
            int stationNumber = getStationId(location);
            if (NOT_AT_STATION != stationNumber) {
                loadAndUnloadCargo(stationNumber, false, false);
            }
        }
    }

    // TODO return list of moves and remove worldDiffs from here
    /**
     * Returns all accumulated moves.
     *
     * @return
     */
    public ImmutableList<Move> getMoves() {
        ImmutableList<Move> currentMoves = new ImmutableList<Move>(moves);
        moves.clear();
        return currentMoves;
    }

    /**
     * @return the number of the station the train is currently at, or NOT_AT_STATION if no
     * current station.
     */
    public int getStationId(Vec2D location) {
        // loop through the station list to check if train is at the same Point2D as a station
        for (int i = 0; i < worldDiffs.size(principal, PlayerKey.Stations); i++) {
            Station station = (Station) worldDiffs.get(principal, PlayerKey.Stations, i);

            if (null != station && location.equals(station.location)) {
                return i; // train is at the station at location tempPoint
            }
        }
        return NOT_AT_STATION;
        // there are no stations that exist where the train is currently
    }

    /**
     * @return
     */
    public int getTrainLength() {
        TrainAccessor trainAccessor = new TrainAccessor(worldDiffs, principal, trainId);
        return trainAccessor.getTrain().getLength();
    }

    /**
     * @return
     */
    private boolean isTrainFull() {
        TrainAccessor trainAccessor = new TrainAccessor(worldDiffs, principal, trainId);
        ImmutableList<Integer> spaceAvailable = trainAccessor.spaceAvailable();
        return Utils.sumOfIntegerImmutableList(spaceAvailable) == 0;
    }

    /**
     * @return
     */
    public boolean isWaitingForFullLoad() {
        Train train = (Train) worldDiffs.get(principal, PlayerKey.Trains, trainId);
        int scheduleID = train.getScheduleID();
        Schedule schedule = (ImmutableSchedule) worldDiffs.get(principal, PlayerKey.TrainSchedules, scheduleID);
        int orderToGoto = schedule.getOrderToGoto();
        if (orderToGoto < 0) {
            return false;
        }
        TrainOrders order = schedule.getOrder(orderToGoto);
        return !isTrainFull() && order.waitUntilFull;
    }

    private void loadAndUnloadCargo(int stationId, boolean waiting, boolean autoConsist) {

        // train is at a station so do the cargo processing
        DropOffAndPickupCargoMoveGenerator transfer = new DropOffAndPickupCargoMoveGenerator(trainId, stationId, worldDiffs, principal, waiting, autoConsist);
        Move move = transfer.generateMove();
        // TODO instead of doing the move, keep them in a list
        if (null != move) {
            moves.add(move);
            MoveStatus moveStatus = move.doMove(worldDiffs, principal);
            if (!moveStatus.succeeds()) throw new IllegalStateException(moveStatus.getMessage());
        }
    }

    // TODO not yet implemented
    public void makeTrainWait(int ticks) {}

    /**
     * @return
     */
    public boolean refreshWaitingForFullLoad() {

        TrainAccessor trainAccessor = new TrainAccessor(worldDiffs, principal, trainId);
        ImmutableSchedule schedule = trainAccessor.getSchedule();

        int stationId = trainAccessor.getStationId(Double.MAX_VALUE);
        if (stationId < 0) throw new IllegalStateException();

        // The train's orders may have changed...
        TrainOrders order = schedule.getOrder(schedule.getOrderToGoto());

        // Should we go to another station?
        if (stationId != order.stationId) {
            return false;
        }

        // Should we change the consist?
        ImmutableList<Integer> consist = trainAccessor.getTrain().getConsist();
        if (!consist.equals(order.consist)) {
            // ..if so, we should change the consist.
            int oldLength = trainAccessor.getTrain().getLength();
            int engineType = trainAccessor.getTrain().getEngineType();

            // TODO newTrain is computed in the ChangeTrainMove also
            Train newTrain = trainAccessor.getTrain().getNewInstance(engineType, order.consist);
            // worldDiffs.set(principal, PlayerKey.Trains, trainId, newTrain);
            Move move = ChangeTrainMove.generateMove(trainId, trainAccessor.getTrain(), engineType, order.consist, principal);
            move.doMove(worldDiffs, principal);
            moves.add(move);

            int newLength = newTrain.getLength();
            // has the trains length increased?
            if (newLength > oldLength) {
                TrainMotion trainMotion = trainAccessor.findCurrentMotion(Double.MAX_VALUE);
                PathOnTiles path = trainMotion.getPath();
                path = lengthenPath(worldDiffs, path, oldLength);
                TrainState status = isWaitingForFullLoad() ? TrainState.WAITING_FOR_FULL_LOAD : TrainState.STOPPED_AT_STATION;
                TrainMotion nextMotion = new TrainMotion(path, newLength, 0, status);

                // Create a new Move object.
                Move trainMove = new NextActivityMove(nextMotion, trainId, principal);
                // TODO instead of doing the move, add them to a list
                moves.add(trainMove);
                MoveStatus moveStatus = trainMove.doMove(worldDiffs, Player.AUTHORITATIVE);
                if (!moveStatus.succeeds()) throw new IllegalStateException(moveStatus.getMessage());
            }
        }

        // Add any cargo that is waiting.
        loadAndUnloadCargo(schedule.getStationToGoto(), order.waitUntilFull, order.autoConsist);

        // Should we stop waiting?
        if (!order.waitUntilFull || isTrainFull()) {
            updateSchedule();
            return false;
        }

        return true;
    }

    private void scheduledStop() {

        Train train = (Train) worldDiffs.get(principal, PlayerKey.Trains, trainId);
        Schedule schedule = (ImmutableSchedule) worldDiffs.get(principal, PlayerKey.TrainSchedules, train.getScheduleID());

        ImmutableList<Integer> wagonsToAdd = schedule.getWagonsToAdd();

        // Loading and unloading cargo takes time, so we make the train wait for
        // a few ticks.
        makeTrainWait(50);

        boolean autoConsist = schedule.autoConsist();

        if (null != wagonsToAdd) {
            int engineType = train.getEngineType();
            Move move = ChangeTrainMove.generateMove(trainId, train, engineType, wagonsToAdd, principal);
            // TODO instead of doing the move, add them to a list
            moves.add(move);
            move.doMove(worldDiffs, principal);
        }
        updateSchedule();
        int stationToGoto = schedule.getStationToGoto();
        loadAndUnloadCargo(stationToGoto, true, autoConsist);
    }

    private void updateSchedule() {
        Train train = (Train) worldDiffs.get(principal, PlayerKey.Trains, trainId);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule currentSchedule = (ImmutableSchedule) worldDiffs.get(principal, PlayerKey.TrainSchedules, scheduleID);
        MutableSchedule schedule = new MutableSchedule(currentSchedule);
        Station station;

        TrainOrders order = schedule.getOrder(schedule.getOrderToGoto());
        boolean waitingForFullLoad = order.waitUntilFull && !isTrainFull();

        if (!waitingForFullLoad) {
            schedule.gotoNextStation();

            ImmutableSchedule newSchedule = schedule.toImmutableSchedule();
            // worldDiffs.set(principal, PlayerKey.TrainSchedules, scheduleID, newSchedule);
            Move move = new ChangeTrainScheduleMove(scheduleID, currentSchedule, newSchedule, principal);
            move.doMove(worldDiffs, principal);
            moves.add(move);

            int stationNumber = schedule.getStationToGoto();
            station = (Station) worldDiffs.get(principal, PlayerKey.Stations, stationNumber);

            if (null == station) {
                logger.warn("null == station, train " + trainId + " doesn't know where to go next!");
            }
        }
    }

}

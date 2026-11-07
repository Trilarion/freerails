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

package freerails.controller;

import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.model.simulation.Time;
import freerails.model.operations.station.Station;
import freerails.model.operations.station.StationUtils;
import freerails.model.terrain.Terrain;
import freerails.model.track.*;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.move.*;
import freerails.move.generator.TrackMoveTransactionsGenerator;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.RemoveStationCompositeMove;
import freerails.move.mapupdatemove.TrackMove;
import freerails.util.IntPoint2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

/**
 * Provides methods that generate moves that build, upgrade, and remove track.
 */
public class TrackMoveProducer {

    private final ModelRoot modelRoot;
    private final MoveExecutor executor;
    private final Collection<Move> moveStack = new Stack<>();
    /**
     * This generates the transaction - the charge - for the track being built.
     */
    private final TrackMoveTransactionsGenerator transactionsGenerator;
    private Time lastMoveTime;

    /**
     * Creates a track move producer instance.
     *
     * @param executor the executor parameter
     * @param world the game world being queried
     * @param modelRoot the client model that provides current game state
     */
    public TrackMoveProducer(MoveExecutor executor, UnmodifiableWorld world, ModelRoot modelRoot) {
        this.executor = executor;
        this.modelRoot = Utils.verifyNotNull(modelRoot);
        Player player = executor.getPlayer();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world, player);
        setBuildTrackStrategy(BuildTrackStrategy.getDefault(world));
    }

    /**
     * Creates a track move producer instance.
     *
     * @param modelRoot the client model that provides current game state
     */
    public TrackMoveProducer(ModelRoot modelRoot) {
        Utils.verifyNotNull(modelRoot);
        executor = modelRoot;
        this.modelRoot = modelRoot;

        UnmodifiableWorld world = executor.getWorld();

        Player player = executor.getPlayer();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world, player);
        setBuildTrackStrategy(BuildTrackStrategy.getDefault(world));
    }

    /**
     * Performs the generate build track move operation.
     *
     * @param from the map coordinate
     * @param direction the direction parameter
     * @param world the game world being queried
     * @param player the player whose game state is used
     * @return the build track move produced by this method
     */
    public static ChangeTrackPieceCompositeMove generateBuildTrackMove(IntPoint2D from, TileTransition direction, TrackType typeA, TrackType typeB, UnmodifiableWorld world, Player player) {
        ChangeTrackPieceMove a = getBuildTrackChangeTrackPieceMove(from, direction, typeA, world, player);
        ChangeTrackPieceMove b = getBuildTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), typeB, world, player);

        return new ChangeTrackPieceCompositeMove(a, b, player);
    }

    /**
     * Performs the generate remove track move operation.
     *
     * @param from the map coordinate
     * @param direction the direction parameter
     * @param world the game world being queried
     * @param player the player whose game state is used
     * @return the remove track move produced by this method
     * @throws Exception if the operation cannot be completed.
     */
    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(IntPoint2D from, TileTransition direction, UnmodifiableWorld world, Player player) throws Exception {
        TrackMove a = getRemoveTrackChangeTrackPieceMove(from, direction, world, player);
        TrackMove b = getRemoveTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), world, player);

        return new ChangeTrackPieceCompositeMove(a, b, player);
    }

    // TODO put part of it in model
    // utility method.
    /**
     * Returns the build track change track piece move.
     * @param p the p
     * @param direction the direction
     * @param trackType the track type
     * @param world the world
     * @param player the player
     * @return the build track change track piece move
     */
    private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(IntPoint2D p, TrackConfigurations direction, TrackType trackType, UnmodifiableWorld world, Player player) {
        TrackPiece oldTrackPiece = null;
        TrackPiece newTrackPiece;

        int playerId = player.getId();

        if (world.boundsContain(p)) {
            oldTrackPiece = world.getTile(p).trackPiece();
        }

        if (oldTrackPiece != null) {
            TrackConfiguration trackConfiguration = TrackConfiguration.add(oldTrackPiece.trackConfiguration(), direction);
            newTrackPiece = new TrackPiece(trackConfiguration, oldTrackPiece.trackType(), playerId);
        } else {
            newTrackPiece = TrackUtils.getTrackPieceWhenOldTrackPieceIsNull(direction, trackType, playerId);
        }

        return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
    }

    // TODO put part of it in model
    // utility method.
    /**
     * Returns the remove track change track piece move.
     * @param p the p
     * @param direction the direction
     * @param world the world
     * @param player the player
     * @return the remove track change track piece move
     * @throws Exception if the operation cannot be completed
     */
    private static TrackMove getRemoveTrackChangeTrackPieceMove(IntPoint2D p, TrackConfigurations direction, UnmodifiableWorld world, Player player) throws Exception {
        TrackPiece oldTrackPiece = null;
        TrackPiece newTrackPiece;

        if (world.boundsContain(p)) {
            oldTrackPiece = world.getTile(p).trackPiece();
        }

        if (oldTrackPiece != null) {
            TrackConfiguration trackConfiguration = TrackConfiguration.subtract(oldTrackPiece.trackConfiguration(), direction);

            if (trackConfiguration.get8bitTemplate() != 0) {
                int playerId = player.getId();
                newTrackPiece = new TrackPiece(trackConfiguration, oldTrackPiece.trackType(), playerId);
            } else {
                newTrackPiece = null;
            }
        } else {
            // There is no track to remove.
            // Fix for bug [ 948670 ] Removing non-existent track
            throw new Exception();
        }


        ChangeTrackPieceMove changeTrackPieceMove = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);

        // TODO maybe the removal of a station should be checked and induced somewhere else
        // If we are removing a station, we also need to remove the station from the station list.
        if (oldTrackPiece.trackType().isStation() && (newTrackPiece == null || !newTrackPiece.trackType().isStation())) {
            int stationIndex = -1;

            for (Station station: world.getStations(player)) {
                if (station.getLocation().equals(changeTrackPieceMove.location())) {
                    // We have found the station!
                    stationIndex = station.getId();
                    break;
                }
            }

            if (-1 == stationIndex) {
                throw new IllegalArgumentException("Could find a station at " + changeTrackPieceMove.location());
            }

            ArrayList<Move> moves = new ArrayList<>();
            moves.add(changeTrackPieceMove);
            moves.add(new RemoveStationMove(player, stationIndex));

            // Now update any train schedules that include this station by iterating over all trains
            for (Player player1: world.getPlayers()) {
                for (Train train: world.getTrains(player1)) {
                    UnmodifiableSchedule schedule = train.getSchedule();
                    if (schedule.stopsAtStation(stationIndex)) {
                        ScheduleState schedule1 = new ScheduleState(schedule);
                        schedule1 = schedule1.removeAllStopsAtStation(stationIndex);
                        Move changeScheduleMove = new UpdateTrainMove(player, train.getId(), null, null, schedule1);
                        moves.add(changeScheduleMove);
                    }
                }
            }

            return new RemoveStationCompositeMove(moves);
        } else {
            return changeTrackPieceMove;
        }
    }

    /**
     * Builds the track.
     *
     * @param from the map coordinate
     * @param path the path parameter
     * @return the track produced by this method
     */
    public Status buildTrack(IntPoint2D from, TileTransition[] path) {
        Status returnValue = Status.OK;
        int x = from.x();
        int y = from.y();
        for (TileTransition aPath : path) {

            returnValue = buildTrack(new IntPoint2D(x, y), aPath);
            x += aPath.deltaX;
            y += aPath.deltaY;
            if (!returnValue.isSuccess()) {
                return returnValue;
            }
        }
        return returnValue;
    }

    /**
     * Builds the track.
     *
     * @param from the map coordinate
     * @param trackVector the track vector parameter
     * @return the track produced by this method
     */
    public Status buildTrack(IntPoint2D from, TileTransition trackVector) {

        UnmodifiableWorld world = executor.getWorld();
        Player player = executor.getPlayer();
        switch (getBuildMode()) {
            case IGNORE_TRACK: {
                return Status.OK;
            }
            case REMOVE_TRACK: {
                try {
                    ChangeTrackPieceCompositeMove move = generateRemoveTrackMove(from, trackVector, world, player);

                    Move moveAndTransaction = transactionsGenerator.addTransactions(move);

                    return sendMove(moveAndTransaction);
                } catch (Exception e) {
                    // thrown when there is no track to remove.
                    // Fix for bug [ 948670 ] Removing non-existent track
                    return Status.fail("No track to remove.");
                }
            }
            case BUILD_TRACK:
            case UPGRADE_TRACK:
                /*
                 * Do nothing yet since we need to work out what type of track to
                 * build.
                 */
                break;
        }
        assert getBuildMode() == BuildMode.BUILD_TRACK || getBuildMode() == BuildMode.UPGRADE_TRACK;

        int[] ruleIDs = new int[2];
        TrackType[] types = new TrackType[2];
        int[] xs = {from.x(), from.x() + trackVector.deltaX};
        int[] ys = {from.y(), from.y() + trackVector.deltaY};
        for (int i = 0; i < ruleIDs.length; i++) {
            int x = xs[i];
            int y = ys[i];
            TerrainTile tile = world.getTile(new IntPoint2D(x, y));
            int terrainTypeId = tile.terrainTypeId();
            ruleIDs[i] = getBuildTrackStrategy().getRule(terrainTypeId);

            if (ruleIDs[i] == -1) {
                Terrain terrainType = world.getTerrain(terrainTypeId);
                String message = "Non of the selected track types can be built on " + terrainType.getName();
                return Status.fail(message);
            }
            types[i] = world.getTrackType(ruleIDs[i]);
        }

        switch (getBuildMode()) {
            case UPGRADE_TRACK: {
                // upgrade the from tile if necessary.
                TerrainTile tileA = world.getTile(from);
                if (tileA.trackPiece().trackType().getId() != ruleIDs[0] && !StationUtils.isStationHere(executor.getWorld(), from)) {
                    Status status = upgradeTrack(from, ruleIDs[0]);
                    if (!status.isSuccess()) {
                        return status;
                    }
                }
                IntPoint2D point = IntPoint2D.add(from, trackVector.getD());
                TerrainTile tileB = world.getTile(point);
                if (tileB.trackPiece().trackType().getId() != ruleIDs[1] && !StationUtils.isStationHere(executor.getWorld(), point)) {
                    Status status = upgradeTrack(point, ruleIDs[1]);
                    if (!status.isSuccess()) {
                        return status;
                    }
                }
                return Status.OK;
            }
            case BUILD_TRACK: {
                ChangeTrackPieceCompositeMove move = generateBuildTrackMove(from, trackVector, types[0], types[1], world, player);

                Move moveAndTransaction = transactionsGenerator.addTransactions(move);

                return sendMove(moveAndTransaction);
            }
            default:
                throw new IllegalArgumentException(String.valueOf(getBuildMode()));
        }
    }

    /**
     * Creates and submits the move that upgrades track at the specified point.
     * @param point the point
     * @param trackRuleID the track rule id
     * @return the status of the submitted upgrade move
     */
    private Status upgradeTrack(IntPoint2D point, int trackRuleID) {
        UnmodifiableWorld world = executor.getWorld();
        TrackPiece before = world.getTile(point).trackPiece();
        // Check whether there is track here.
        if (before == null) {
            return Status.fail("No track to upgrade.");
        }

        Player player = executor.getPlayer();
        int owner = player.getId();
        TrackType trackType = world.getTrackType(trackRuleID);
        TrackPiece after = new TrackPiece(before.trackConfiguration(), trackType, owner);

        // We don't want to 'upgrade' a station to track. See bug 874416.
        if (before.trackType().isStation()) {
            return Status.fail("No need to upgrade track at station.");
        }

        Move move = new ChangeTrackPieceMove(before, after, point);
        // Move move = SpecialTrackCompositeMove.generateUpgradeTrackCompositeMove(before, after, point);
        Move move2 = transactionsGenerator.addTransactions(move);

        return sendMove(move2);
    }

    /**
     * Moves are only un-doable if no game time has passed since they they were
     * executed. This method clears the move stack if the moves were added to
     * the stack at a time other than the current time.
     */
    private void clearStackIfStale() {
        UnmodifiableWorld world = executor.getWorld();
        Time currentTime = world.getClock().getCurrentTime();

        if (!currentTime.equals(lastMoveTime)) {
            moveStack.clear();
            lastMoveTime = currentTime;
        }
    }

    /**
     * Returns the track builder mode.
     *
     * @return the track builder mode produced by this method
     */
    public BuildMode getTrackBuilderMode() {
        return getBuildMode();
    }

    /**
     * Sets the track builder mode.
     *
     * @param buildMode the build mode parameter
     */
    public void setTrackBuilderMode(BuildMode buildMode) {
        setBuildMode(buildMode);
    }

    /**
     * Submits a move and returns its execution status.
     * @param move the move
     * @return the move execution status
     */
    private Status sendMove(Move move) {
        Status status = executor.applyMove(move);

        if (status.isSuccess()) {
            clearStackIfStale();
            moveStack.add(move);
        }

        return status;
    }

    /**
     * Returns the build track strategy.
     * @return the build track strategy
     */
    private BuildTrackStrategy getBuildTrackStrategy() {
        return (BuildTrackStrategy) modelRoot.getProperty(ModelRootProperty.BUILD_TRACK_STRATEGY);
    }

    /**
     * Sets the build track strategy.
     *
     * @param buildTrackStrategy the build track strategy parameter
     */
    public void setBuildTrackStrategy(BuildTrackStrategy buildTrackStrategy) {

        modelRoot.setProperty(ModelRootProperty.BUILD_TRACK_STRATEGY, buildTrackStrategy);
    }

    /**
     * Returns the build mode.
     *
     * @return the build mode produced by this method
     */
    private BuildMode getBuildMode() {
        return (BuildMode) modelRoot.getProperty(ModelRootProperty.TRACK_BUILDER_MODE);
    }

    /**
     * Sets the build mode.
     *
     * @param buildMode the build mode parameter
     */
    private void setBuildMode(BuildMode buildMode) {
        modelRoot.setProperty(ModelRootProperty.TRACK_BUILDER_MODE, buildMode);
    }

}

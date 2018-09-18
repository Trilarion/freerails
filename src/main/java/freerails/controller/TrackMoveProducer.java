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

package freerails.controller;

import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.model.game.Time;
import freerails.model.station.Station;
import freerails.model.station.StationUtils;
import freerails.model.terrain.Terrain;
import freerails.model.track.*;
import freerails.model.train.Train;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.move.*;
import freerails.move.generator.TrackMoveTransactionsGenerator;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.RemoveStationCompositeMove;
import freerails.move.mapupdatemove.TrackMove;
import freerails.util.Vec2D;
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
     * @param executor
     * @param world
     * @param modelRoot
     */
    public TrackMoveProducer(MoveExecutor executor, UnmodifiableWorld world, ModelRoot modelRoot) {
        this.executor = executor;
        this.modelRoot = Utils.verifyNotNull(modelRoot);
        Player player = executor.getPlayer();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world, player);
        setBuildTrackStrategy(BuildTrackStrategy.getDefault(world));
    }

    /**
     * @param modelRoot
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
     * @param from
     * @param direction
     * @param world
     * @param player
     * @return
     */
    public static ChangeTrackPieceCompositeMove generateBuildTrackMove(Vec2D from, TileTransition direction, TrackType typeA, TrackType typeB, UnmodifiableWorld world, Player player) {
        ChangeTrackPieceMove a = getBuildTrackChangeTrackPieceMove(from, direction, typeA, world, player);
        ChangeTrackPieceMove b = getBuildTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), typeB, world, player);

        return new ChangeTrackPieceCompositeMove(a, b, player);
    }

    /**
     * @param from
     * @param direction
     * @param world
     * @param player
     * @return
     * @throws Exception
     */
    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(Vec2D from, TileTransition direction, UnmodifiableWorld world, Player player) throws Exception {
        TrackMove a = getRemoveTrackChangeTrackPieceMove(from, direction, world, player);
        TrackMove b = getRemoveTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), world, player);

        return new ChangeTrackPieceCompositeMove(a, b, player);
    }

    // TODO put part of it in model
    // utility method.
    private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(Vec2D p, TrackConfigurations direction, TrackType trackType, UnmodifiableWorld world, Player player) {
        if (!world.boundsContain(p)) {
            throw new RuntimeException("Out of bounds");
        }

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        int playerId = player.getId();

        oldTrackPiece = world.getTile(p).getTrackPiece();

        if (oldTrackPiece != null) {
            TrackConfiguration trackConfiguration = TrackConfiguration.add(oldTrackPiece.getTrackConfiguration(), direction);
            newTrackPiece = new TrackPiece(trackConfiguration, oldTrackPiece.getTrackType(), playerId);
        } else {
            newTrackPiece = TrackUtils.getTrackPieceWhenOldTrackPieceIsNull(direction, trackType, playerId);
        }

        return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
    }

    // TODO put part of it in model
    // utility method.
    private static TrackMove getRemoveTrackChangeTrackPieceMove(Vec2D p, TrackConfigurations direction, UnmodifiableWorld world, Player player) throws Exception {
        if (!world.boundsContain(p)) {
            throw new RuntimeException("Out of bounds");
        }

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        oldTrackPiece = world.getTile(p).getTrackPiece();

        if (oldTrackPiece != null) {
            TrackConfiguration trackConfiguration = TrackConfiguration.subtract(oldTrackPiece.getTrackConfiguration(), direction);

            if (trackConfiguration != TrackConfiguration.getFlatInstance("000010000")) {
                int playerId = player.getId();
                newTrackPiece = new TrackPiece(trackConfiguration, oldTrackPiece.getTrackType(), playerId);
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
        if (oldTrackPiece.getTrackType().isStation() && (newTrackPiece == null || !newTrackPiece.getTrackType().isStation())) {
            int stationIndex = -1;

            for (Station station: world.getStations(player)) {
                if (station.getLocation().equals(changeTrackPieceMove.getLocation())) {
                    // We have found the station!
                    stationIndex = station.getId();
                    break;
                }
            }

            if (-1 == stationIndex) {
                throw new IllegalArgumentException("Could find a station at " + changeTrackPieceMove.getLocation());
            }

            ArrayList<Move> moves = new ArrayList<>();
            moves.add(changeTrackPieceMove);
            moves.add(new RemoveStationMove(player, stationIndex));

            // Now update any train schedules that include this station by iterating over all trains
            for (Player player1: world.getPlayers()) {
                for (Train train: world.getTrains(player1)) {
                    UnmodifiableSchedule schedule = train.getSchedule();
                    if (schedule.stopsAtStation(stationIndex)) {
                        Schedule schedule1 = new Schedule(schedule);
                        schedule1.removeAllStopsAtStation(stationIndex);
                        train.setSchedule(schedule1);
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
     * @param from
     * @param path
     * @return
     */
    public Status buildTrack(Vec2D from, TileTransition[] path) {
        Status returnValue = Status.OK;
        int x = from.x;
        int y = from.y;
        for (TileTransition aPath : path) {

            returnValue = buildTrack(new Vec2D(x, y), aPath);
            x += aPath.deltaX;
            y += aPath.deltaY;
            if (!returnValue.isSuccess()) {
                return returnValue;
            }
        }
        return returnValue;
    }

    /**
     * @param from
     * @param trackVector
     * @return
     */
    public Status buildTrack(Vec2D from, TileTransition trackVector) {

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
        int[] xs = {from.x, from.x + trackVector.deltaX};
        int[] ys = {from.y, from.y + trackVector.deltaY};
        for (int i = 0; i < ruleIDs.length; i++) {
            int x = xs[i];
            int y = ys[i];
            TerrainTile tile = world.getTile(new Vec2D(x, y));
            int terrainTypeId = tile.getTerrainTypeId();
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
                if (tileA.getTrackPiece().getTrackType().getId() != ruleIDs[0] && !StationUtils.isStationHere(executor.getWorld(), from)) {
                    Status status = upgradeTrack(from, ruleIDs[0]);
                    if (!status.isSuccess()) {
                        return status;
                    }
                }
                Vec2D point = Vec2D.add(from, trackVector.getD());
                TerrainTile tileB = world.getTile(point);
                if (tileB.getTrackPiece().getTrackType().getId() != ruleIDs[1] && !StationUtils.isStationHere(executor.getWorld(), point)) {
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

    private Status upgradeTrack(Vec2D point, int trackRuleID) {
        UnmodifiableWorld world = executor.getWorld();
        TrackPiece before = world.getTile(point).getTrackPiece();
        // Check whether there is track here.
        if (before == null) {
            return Status.fail("No track to upgrade.");
        }

        Player player = executor.getPlayer();
        int owner = player.getId();
        TrackType trackType = world.getTrackType(trackRuleID);
        TrackPiece after = new TrackPiece(before.getTrackConfiguration(), trackType, owner);

        // We don't want to 'upgrade' a station to track. See bug 874416.
        if (before.getTrackType().isStation()) {
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
     * @return
     */
    public BuildMode getTrackBuilderMode() {
        return getBuildMode();
    }

    /**
     * @param buildMode
     */
    public void setTrackBuilderMode(BuildMode buildMode) {
        setBuildMode(buildMode);
    }

    private Status sendMove(Move move) {
        Status status = executor.applyMove(move);

        if (status.isSuccess()) {
            clearStackIfStale();
            moveStack.add(move);
        }

        return status;
    }

    private BuildTrackStrategy getBuildTrackStrategy() {
        return (BuildTrackStrategy) modelRoot.getProperty(ModelRootProperty.BUILD_TRACK_STRATEGY);
    }

    /**
     * @param buildTrackStrategy
     */
    public void setBuildTrackStrategy(BuildTrackStrategy buildTrackStrategy) {

        modelRoot.setProperty(ModelRootProperty.BUILD_TRACK_STRATEGY, buildTrackStrategy);
    }

    /**
     * @return
     */
    private BuildMode getBuildMode() {
        return (BuildMode) modelRoot.getProperty(ModelRootProperty.TRACK_BUILDER_MODE);
    }

    /**
     * @param buildMode
     */
    private void setBuildMode(BuildMode buildMode) {
        modelRoot.setProperty(ModelRootProperty.TRACK_BUILDER_MODE, buildMode);
    }

}

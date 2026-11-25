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
import freerails.model.operations.station.StationUtils;
import freerails.model.terrain.Terrain;
import freerails.model.track.*;
import freerails.move.*;
import freerails.move.generator.BuildTrackMoveGenerator;
import freerails.move.generator.MoveGenerator;
import freerails.move.generator.RemoveTrackMoveGenerator;
import freerails.move.generator.UpgradeTrackMoveGenerator;
import freerails.util.IntPoint2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;

import java.util.Collection;
import java.util.Stack;

/**
 * Provides methods that generate moves that build, upgrade, and remove track.
 */
public class TrackMoveProducer {

    private final ModelRoot modelRoot;
    private final MoveExecutor executor;
    private final Collection<MoveGenerator> moveStack = new Stack<>();
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

        setBuildTrackStrategy(BuildTrackStrategy.getDefault(executor.getWorld()));
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
            case INSPECT: {
                return Status.OK;
            }
            case REMOVE_TRACK: {
                return send(new RemoveTrackMoveGenerator(from, trackVector, player));
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
        }

        switch (getBuildMode()) {
            case UPGRADE_TRACK: {
                // Upgrade the tiles if necessary; stations and other players' track are never upgraded.
                TerrainTile tileA = world.getTile(from);
                if (tileA.trackPiece().trackType().getId() != ruleIDs[0] && !StationUtils.isStationHere(executor.getWorld(), from)
                        && tileA.trackPiece().playerId() == player.getId()) {
                    Status status = upgradeTrack(from, ruleIDs[0]);
                    if (!status.isSuccess()) {
                        return status;
                    }
                }
                IntPoint2D point = IntPoint2D.add(from, trackVector.getD());
                TerrainTile tileB = world.getTile(point);
                if (tileB.trackPiece().trackType().getId() != ruleIDs[1] && !StationUtils.isStationHere(executor.getWorld(), point)
                        && tileB.trackPiece().playerId() == player.getId()) {
                    Status status = upgradeTrack(point, ruleIDs[1]);
                    if (!status.isSuccess()) {
                        return status;
                    }
                }
                return Status.OK;
            }
            case BUILD_TRACK: {
                return send(new BuildTrackMoveGenerator(from, trackVector, ruleIDs[0], ruleIDs[1], player));
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
        return send(new UpgradeTrackMoveGenerator(point, trackRuleID, executor.getPlayer()));
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
     * Submits a track change; the server generates it, including its cost.
     * @param generator the generator of the track change
     * @return the status of the change in the local world
     */
    private Status send(MoveGenerator generator) {
        Status status = executor.doPreMove(generator);

        if (status.isSuccess()) {
            clearStackIfStale();
            moveStack.add(generator);
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

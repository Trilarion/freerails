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
import freerails.model.terrain.Terrain;
import freerails.model.track.*;
import freerails.move.*;
import freerails.move.generator.TrackMoveTransactionsGenerator;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.game.GameTime;
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
    private final Collection<Move> moveStack = new Stack<>();
    /**
     * This generates the transactions - the charge - for the track being built.
     */
    private final TrackMoveTransactionsGenerator transactionsGenerator;
    private GameTime lastMoveTime;

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
            if (!returnValue.succeeds()) {
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
                    ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(from, trackVector, world, player);

                    Move moveAndTransaction = transactionsGenerator.addTransactions(move);

                    return sendMove(moveAndTransaction);
                } catch (Exception e) {
                    // thrown when there is no track to remove.
                    // Fix for bug [ 948670 ] Removing non-existent track
                    return Status.moveFailed("No track to remove.");
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
        assert (getBuildMode() == BuildMode.BUILD_TRACK || getBuildMode() == BuildMode.UPGRADE_TRACK);

        int[] ruleIDs = new int[2];
        TrackType[] types = new TrackType[2];
        int[] xs = {from.x, from.x + trackVector.deltaX};
        int[] ys = {from.y, from.y + trackVector.deltaY};
        for (int i = 0; i < ruleIDs.length; i++) {
            int x = xs[i];
            int y = ys[i];
            TerrainTile tile = (TerrainTile) world.getTile(new Vec2D(x, y));
            int terrainTypeId = tile.getTerrainTypeId();
            ruleIDs[i] = getBuildTrackStrategy().getRule(terrainTypeId);

            if (ruleIDs[i] == -1) {
                Terrain terrainType = world.getTerrain(terrainTypeId);
                String message = "Non of the selected track types can be built on " + terrainType.getName();
                return Status.moveFailed(message);
            }
            types[i] = world.getTrackType(ruleIDs[i]);
        }

        switch (getBuildMode()) {
            case UPGRADE_TRACK: {
                // upgrade the from tile if necessary.
                TerrainTile tileA = (TerrainTile) world.getTile(from);
                if (tileA.getTrackPiece().getTrackType().getId() != ruleIDs[0] && !isStationHere(from)) {
                    Status status = upgradeTrack(from, ruleIDs[0]);
                    if (!status.succeeds()) {
                        return status;
                    }
                }
                Vec2D point = Vec2D.add(from, trackVector.getD());
                TerrainTile tileB = (TerrainTile) world.getTile(point);
                if (tileB.getTrackPiece().getTrackType().getId() != ruleIDs[1] && !isStationHere(point)) {
                    Status status = upgradeTrack(point, ruleIDs[1]);
                    if (!status.succeeds()) {
                        return status;
                    }
                }
                return Status.OK;
            }
            case BUILD_TRACK: {
                ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(from, trackVector, types[0], types[1], world, player);

                Move moveAndTransaction = transactionsGenerator.addTransactions(move);

                return sendMove(moveAndTransaction);
            }
            default:
                throw new IllegalArgumentException(String.valueOf(getBuildMode()));
        }
    }

    private Status upgradeTrack(Vec2D point, int trackRuleID) {
        UnmodifiableWorld world = executor.getWorld();
        TrackPiece before = ((TerrainTile) world.getTile(point)).getTrackPiece();
        // Check whether there is track here.
        if (before == null) {
            return Status.moveFailed("No track to upgrade.");
        }

        Player player = executor.getPlayer();
        int owner = player.getId();
        TrackType trackType = world.getTrackType(trackRuleID);
        TrackPiece after = new TrackPiece(before.getTrackConfiguration(), trackType, owner);

        // We don't want to 'upgrade' a station to track. See bug 874416.
        if (before.getTrackType().isStation()) {
            return Status.moveFailed("No need to upgrade track at station.");
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
        GameTime currentTime = world.currentTime();

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
        Status status = executor.doMove(move);

        if (status.succeeds()) {
            clearStackIfStale();
            moveStack.add(move);
        }

        return status;
    }

    private boolean isStationHere(Vec2D p) {
        UnmodifiableWorld world = executor.getWorld();
        TerrainTile tile = (TerrainTile) world.getTile(p);
        return tile.getTrackPiece().getTrackType().isStation();
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

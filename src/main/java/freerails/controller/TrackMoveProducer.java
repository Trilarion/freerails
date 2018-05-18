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
import freerails.model.world.WorldUtils;
import freerails.move.*;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.UpgradeTrackMove;
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
        Player principal = executor.getPrincipal();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world, principal);
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

        Player principal = executor.getPrincipal();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world, principal);
        setBuildTrackStrategy(BuildTrackStrategy.getDefault(world));
    }

    /**
     * @param from
     * @param path
     * @return
     */
    public MoveStatus buildTrack(Vec2D from, TileTransition[] path) {
        MoveStatus returnValue = MoveStatus.MOVE_OK;
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
    public MoveStatus buildTrack(Vec2D from, TileTransition trackVector) {

        UnmodifiableWorld world = executor.getWorld();
        Player principal = executor.getPrincipal();
        switch (getBuildMode()) {
            case IGNORE_TRACK: {
                return MoveStatus.MOVE_OK;
            }
            case REMOVE_TRACK: {
                try {
                    ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(from, trackVector, world, principal);

                    Move moveAndTransaction = transactionsGenerator.addTransactions(move);

                    return sendMove(moveAndTransaction);
                } catch (Exception e) {
                    // thrown when there is no track to remove.
                    // Fix for bug [ 948670 ] Removing non-existent track
                    return MoveStatus.moveFailed("No track to remove.");
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
                return MoveStatus.moveFailed(message);
            }
            types[i] = world.getTrackType(ruleIDs[i]);
        }

        switch (getBuildMode()) {
            case UPGRADE_TRACK: {
                // upgrade the from tile if necessary.
                TerrainTile tileA = (TerrainTile) world.getTile(from);
                if (tileA.getTrackPiece().getTrackType().getId() != ruleIDs[0] && !isStationHere(from)) {
                    MoveStatus moveStatus = upgradeTrack(from, ruleIDs[0]);
                    if (!moveStatus.succeeds()) {
                        return moveStatus;
                    }
                }
                Vec2D point = Vec2D.add(from, trackVector.getD());
                TerrainTile tileB = (TerrainTile) world.getTile(point);
                if (tileB.getTrackPiece().getTrackType().getId() != ruleIDs[1] && !isStationHere(point)) {
                    MoveStatus moveStatus = upgradeTrack(point, ruleIDs[1]);
                    if (!moveStatus.succeeds()) {
                        return moveStatus;
                    }
                }
                return MoveStatus.MOVE_OK;
            }
            case BUILD_TRACK: {
                ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(from, trackVector, types[0], types[1], world, principal);

                Move moveAndTransaction = transactionsGenerator.addTransactions(move);

                return sendMove(moveAndTransaction);
            }
            default:
                throw new IllegalArgumentException(String.valueOf(getBuildMode()));
        }
    }

    private MoveStatus upgradeTrack(Vec2D point, int trackRuleID) {
        UnmodifiableWorld world = executor.getWorld();
        TrackPiece before = ((TerrainTile) world.getTile(point)).getTrackPiece();
        // Check whether there is track here.
        if (before == null) {
            return MoveStatus.moveFailed("No track to upgrade.");
        }

        Player principal = executor.getPrincipal();
        int owner = WorldUtils.getPlayerIndex(world, principal);
        TrackType trackType = world.getTrackType(trackRuleID);
        TrackPiece after = new TrackPiece(before.getTrackConfiguration(), trackType, owner);

        // We don't want to 'upgrade' a station to track. See bug 874416.
        if (before.getTrackType().isStation()) {
            return MoveStatus.moveFailed("No need to upgrade track at station.");
        }

        Move move = UpgradeTrackMove.generateMove(before, after, point);
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
     * @param i
     */
    public void setTrackBuilderMode(BuildMode i) {
        setBuildMode(i);
    }

    private MoveStatus sendMove(Move move) {
        MoveStatus moveStatus = executor.doMove(move);

        if (moveStatus.succeeds()) {
            clearStackIfStale();
            moveStack.add(move);
        }

        return moveStatus;
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

    // TODO remove modelRoot, instead use BuildMode as an construction time argument
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

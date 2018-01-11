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

import freerails.controller.ModelRoot.Property;
import freerails.move.*;
import freerails.util.Point2D;
import freerails.world.game.GameTime;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.terrain.TerrainTile;
import freerails.world.terrain.TileTransition;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TerrainType;
import freerails.world.track.NullTrackType;
import freerails.world.track.TrackPiece;
import freerails.world.track.TrackPieceImpl;
import freerails.world.track.TrackRule;

import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 * Provides methods that generate moves that build, upgrade, and remove track.
 */
public final class TrackMoveProducer {

    private final ModelRoot mr;

    private final MoveExecutor executor;
    private final Collection<Move> moveStack = new Stack<>();
    /**
     * This generates the transactions - the charge - for the track being built.
     */
    private final TrackMoveTransactionsGenerator transactionsGenerator;
    private GameTime lastMoveTime = GameTime.BIG_BANG;

    /**
     * @param executor
     * @param world
     * @param mr
     */
    public TrackMoveProducer(MoveExecutor executor, ReadOnlyWorld world,
                             ModelRoot mr) {
        if (null == mr)
            throw new NullPointerException();
        this.executor = executor;
        this.mr = mr;
        FreerailsPrincipal principal = executor.getPrincipal();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world,
                principal);
        setBuildTrackStrategy(BuildTrackStrategy.getDefault(world));

    }

    /**
     * @param mr
     */
    public TrackMoveProducer(ModelRoot mr) {
        executor = mr;
        if (null == mr)
            throw new NullPointerException();
        this.mr = mr;

        ReadOnlyWorld world = executor.getWorld();

        FreerailsPrincipal principal = executor.getPrincipal();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world,
                principal);
        setBuildTrackStrategy(BuildTrackStrategy.getDefault(world));

    }

    /**
     * @param from
     * @param path
     * @return
     */
    public MoveStatus buildTrack(Point2D from, TileTransition[] path) {
        MoveStatus returnValue = MoveStatus.MOVE_OK;
        int x = from.x;
        int y = from.y;
        for (TileTransition aPath : path) {

            returnValue = buildTrack(new Point2D(x, y), aPath);
            x += aPath.deltaX;
            y += aPath.deltaY;
            if (!returnValue.ok) {
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
    public MoveStatus buildTrack(Point2D from, TileTransition trackVector) {

        ReadOnlyWorld w = executor.getWorld();
        FreerailsPrincipal principal = executor.getPrincipal();
        switch (getBuildMode()) {
            case IGNORE_TRACK: {
                return MoveStatus.MOVE_OK;
            }
            case REMOVE_TRACK: {
                try {
                    ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                            .generateRemoveTrackMove(from, trackVector, w,
                                    principal);

                    Move moveAndTransaction = transactionsGenerator
                            .addTransactions(move);

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
        TrackRule[] rules = new TrackRule[2];
        int[] xs = {from.x, from.x + trackVector.deltaX};
        int[] ys = {from.y, from.y + trackVector.deltaY};
        for (int i = 0; i < ruleIDs.length; i++) {
            int x = xs[i];
            int y = ys[i];
            TerrainTile tile = (FullTerrainTile) w.getTile(x, y);
            int tt = tile.getTerrainTypeID();
            ruleIDs[i] = getBuildTrackStrategy().getRule(tt);

            if (ruleIDs[i] == -1) {
                TerrainType terrainType = (TerrainType) w.get(
                        SKEY.TERRAIN_TYPES, tt);
                String message = "Non of the selected track types can be built on "
                        + terrainType.getDisplayName();
                return MoveStatus.moveFailed(message);
            }
            rules[i] = (TrackRule) w.get(SKEY.TRACK_RULES, ruleIDs[i]);
        }

        switch (getBuildMode()) {
            case UPGRADE_TRACK: {
                // upgrade the from tile if necessary.
                FullTerrainTile tileA = (FullTerrainTile) w.getTile(from.x, from.y);
                if (tileA.getTrackPiece().getTrackTypeID() != ruleIDs[0]
                        && !isStationHere(from)) {
                    MoveStatus ms = upgradeTrack(from, ruleIDs[0]);
                    if (!ms.ok) {
                        return ms;
                    }
                }
                Point2D point = new Point2D(from.x + trackVector.getDx(), from.y
                        + trackVector.getDy());
                FullTerrainTile tileB = (FullTerrainTile) w.getTile(point.x, point.y);
                if (tileB.getTrackPiece().getTrackTypeID() != ruleIDs[1]
                        && !isStationHere(point)) {
                    MoveStatus ms = upgradeTrack(point, ruleIDs[1]);
                    if (!ms.ok) {
                        return ms;
                    }
                }
                return MoveStatus.MOVE_OK;
            }
            case BUILD_TRACK: {
                ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                        .generateBuildTrackMove(from, trackVector, rules[0],
                                rules[1], w, principal);

                Move moveAndTransaction = transactionsGenerator
                        .addTransactions(move);

                return sendMove(moveAndTransaction);
            }
            default:
                throw new IllegalArgumentException(String.valueOf(getBuildMode()));
        }

    }

    private MoveStatus upgradeTrack(Point2D point, int trackRuleID) {
        ReadOnlyWorld w = executor.getWorld();
        TrackPiece before = ((FullTerrainTile) w.getTile(point.x, point.y))
                .getTrackPiece();
        /* Check whether there is track here. */
        if (before.getTrackTypeID() == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
            return MoveStatus.moveFailed("No track to upgrade.");
        }

        FreerailsPrincipal principal = executor.getPrincipal();
        int owner = ChangeTrackPieceCompositeMove.getOwner(principal, w);
        TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, trackRuleID);
        TrackPiece after = new TrackPieceImpl(before.getTrackConfiguration(),
                trackRule, owner, trackRuleID);

        /* We don't want to 'upgrade' a station to track. See bug 874416. */
        if (before.getTrackRule().isStation()) {
            return MoveStatus
                    .moveFailed("No need to upgrade track at station.");
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
        ReadOnlyWorld w = executor.getWorld();
        GameTime currentTime = w.currentTime();

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

    private MoveStatus sendMove(Move m) {
        MoveStatus ms = executor.doMove(m);

        if (ms.isOk()) {
            clearStackIfStale();
            moveStack.add(m);
        }

        return ms;
    }

    private boolean isStationHere(Point2D p) {
        ReadOnlyWorld w = executor.getWorld();
        FullTerrainTile tile = (FullTerrainTile) w.getTile(p.x, p.y);
        return tile.getTrackPiece().getTrackRule().isStation();
    }

    public BuildTrackStrategy getBuildTrackStrategy() {
        return (BuildTrackStrategy) mr
                .getProperty(Property.BUILD_TRACK_STRATEGY);
    }

    /**
     * @param buildTrackStrategy
     */
    public void setBuildTrackStrategy(BuildTrackStrategy buildTrackStrategy) {

        mr.setProperty(Property.BUILD_TRACK_STRATEGY, buildTrackStrategy);
    }

    /**
     * @return
     */
    public BuildMode getBuildMode() {
        return (BuildMode) mr.getProperty(Property.TRACK_BUILDER_MODE);
    }

    /**
     * @param buildMode
     */
    public void setBuildMode(BuildMode buildMode) {
        mr.setProperty(Property.TRACK_BUILDER_MODE, buildMode);
    }

    /**
     *
     */
    public enum BuildMode {

        /**
         *
         */
        BUILD_TRACK,

        /**
         *
         */
        REMOVE_TRACK,

        /**
         *
         */
        UPGRADE_TRACK,

        /**
         *
         */
        IGNORE_TRACK,

        /**
         *
         */
        BUILD_STATION
    }

}

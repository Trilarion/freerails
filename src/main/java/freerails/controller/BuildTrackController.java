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

import freerails.client.ClientConfig;
import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.model.world.FullWorld;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.ui.SoundManager;
import freerails.model.track.pathfinding.*;
import freerails.model.track.BuildTrackStrategy;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.move.mapupdatemove.UpgradeTrackMove;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.server.GameModel;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackPieceImpl;
import freerails.model.track.TrackRule;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO Remove use of FullWorld from here (remnant from FullWorldDiffs from here), use readonly world instead if possible
/**
 * Provides methods to change the proposed track and save it to the real world.
 */
public class BuildTrackController implements GameModel {

    private static final Logger logger = Logger.getLogger(BuildTrackController.class.getName());
    private final ModelRoot modelRoot;
    private final TrackPathFinder trackPathFinder;
    private final PathOnTrackFinder pathOnTrackFinder;
    private final FreerailsPrincipal principal;
    private final ReadOnlyWorld world;
    private FullWorld fullWorld;
    private final Map<Vec2D, TrackPiece> proposedTrack = new HashMap<>();
    private boolean buildNewTrack = true;
    private List<Vec2D> builtTrack = new ArrayList<>();
    private boolean isBuildTrackSuccessful = false;
    private TileTransition[] path;
    private Vec2D startPoint;
    private Vec2D targetPoint;
    private boolean visible = false;

    /**
     * BuildTrackRenderer
     *
     * @param readOnlyWorld ReadOnlyWorld
     */
    public BuildTrackController(ReadOnlyWorld readOnlyWorld, ModelRoot modelRoot) {
        fullWorld = (FullWorld) Utils.cloneBySerialisation(readOnlyWorld);
        world = readOnlyWorld;
        trackPathFinder = new TrackPathFinder(readOnlyWorld, modelRoot.getPrincipal());
        pathOnTrackFinder = new PathOnTrackFinder(readOnlyWorld);
        this.modelRoot = modelRoot;
        principal = modelRoot.getPrincipal();
        this.modelRoot.setProperty(ModelRootProperty.PROPOSED_TRACK, proposedTrack);
    }

    /**
     * Utility method that gets the BuildTrackStrategy from the model root.
     */
    private BuildTrackStrategy getBuildTrackStrategy() {
        BuildTrackStrategy buildTrackStrategy = (BuildTrackStrategy) modelRoot.getProperty(ModelRootProperty.BUILD_TRACK_STRATEGY);
        return Utils.verifyNotNull(buildTrackStrategy);
    }

    /**
     * Utility method that gets the cursor position from the model root.
     */
    private Vec2D getCursorPosition() {
        Vec2D point = (Vec2D) modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION);

        // Check for null & make a defensive copy
        point = null == point ? new Vec2D() : point;

        if (!modelRoot.getWorld().boundsContain(point)) {
            throw new IllegalStateException(String.valueOf(point));
        }

        return point;
    }

    /**
     * Hides and cancels any proposed track.
     */
    public void hide() {
        setVisible(false);
        setTargetPoint(null);
        reset();
    }

    /**
     * returns {@code true} if the track is being build - it is iff the
     * build track is shown
     *
     * @return boolean
     */
    public boolean isBuilding() {
        return visible;
    }

    /**
     * Returns true if all the track pieces can be successfully built.
     */
    public boolean isBuildTrackSuccessful() {
        return isBuildTrackSuccessful;
    }

    /**
     * Moves cursor which causes track to be built on the worldDiff object.
     */
    private void moveCursorMoreTiles(List<Vec2D> track) {
        moveCursorMoreTiles(track, null);
    }

    /**
     * uses {@code trackBuilder} if not null -- otherwise uses own
     * {@code buildTrack} method - that is applied on
     * {@code worldDifferences}
     *
     * @param track        List
     * @param trackBuilder TrackMoveProducer
     */
    private MoveStatus moveCursorMoreTiles(List<Vec2D> track, TrackMoveProducer trackBuilder) {
        Vec2D oldPosition = getCursorPosition();

        if (!TileTransition.checkValidity(oldPosition, track.get(0))) {
            throw new IllegalStateException(oldPosition.toString() + " and " + track.get(0).toString());
        }

        MoveStatus moveStatus = null;
        int piecesOfNewTrack = 0;

        if (null != trackBuilder) {
            trackBuilder.setBuildTrackStrategy(getBuildTrackStrategy());
        }

        for (Vec2D point : track) {
            logger.debug("point" + point);
            logger.debug("oldPosition" + oldPosition);

            if (oldPosition.equals(point)) {
                logger.debug("(oldPosition.equals(point))" + point);

                continue;
            }

            TileTransition vector = TileTransition.getInstance(Vec2D.subtract(point, oldPosition));

            // If there is already track between the two tiles, do nothing
            FullTerrainTile tile = (FullTerrainTile) world.getTile(oldPosition);

            if (tile.getTrackPiece().getTrackConfiguration().contains(vector)) {
                oldPosition = point;

                continue;
            }
            piecesOfNewTrack++;

            if (trackBuilder != null) {
                moveStatus = trackBuilder.buildTrack(oldPosition, vector);
            } else {
                moveStatus = planBuildingTrack(oldPosition, vector);
            }

            if (moveStatus.succeeds()) {
                setCursorMessage("");
            } else {
                setCursorMessage(moveStatus.getMessage());
                reset();

                return moveStatus;
            }

            oldPosition = point;
        }

        // Check whether there is already track at every point.
        if (piecesOfNewTrack == 0) {
            MoveStatus moveFailed = MoveStatus.moveFailed("Track already here");
            setCursorMessage(moveFailed.getMessage());

            return moveFailed;
        }

        isBuildTrackSuccessful = true;

        // If track has actually been built, play the build track sound.
        if (trackBuilder != null && moveStatus.succeeds()) {
            if (trackBuilder.getTrackBuilderMode() == BuildMode.BUILD_TRACK) {
                SoundManager.getInstance().playSound(ClientConfig.SOUND_BUILD_TRACK, 0);
            }
        }

        return moveStatus;
    }

    /**
     * Attempts to building track from the specified point in the specified
     * direction on the worldDiff object.
     */
    private MoveStatus planBuildingTrack(Vec2D point, TileTransition tileTransition) {
        TerrainTile tileA = (FullTerrainTile) fullWorld.getTile(point);
        BuildTrackStrategy buildTrackStrategy = getBuildTrackStrategy();
        int trackTypeAID = buildTrackStrategy.getRule(tileA.getTerrainTypeID());
        TrackRule trackRuleA = (TrackRule) world.get(SharedKey.TrackRules, trackTypeAID);

        TerrainTile tileB = (FullTerrainTile) fullWorld.getTile(Vec2D.add(point, tileTransition.getD()));
        int trackTypeBID = buildTrackStrategy.getRule(tileB.getTerrainTypeID());
        TrackRule trackRuleB = (TrackRule) world.get(SharedKey.TrackRules, trackTypeBID);

        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(point, tileTransition, trackRuleA, trackRuleB, fullWorld, principal);

        // add to proposed track
        ChangeTrackPieceMove m = (ChangeTrackPieceMove) move.getMove(0);
        proposedTrack.put(m.location, m.trackPieceAfter);
        m = (ChangeTrackPieceMove) move.getMove(1);
        proposedTrack.put(m.location, m.trackPieceAfter);

        return move.doMove(fullWorld, principal);
    }

    /**
     * Cancels any proposed track and resets the path finder.
     */
    private void reset() {
        proposedTrack.clear();
        fullWorld = (FullWorld) Utils.cloneBySerialisation(world);
        trackPathFinder.abandonSearch();
        builtTrack.clear();
        isBuildTrackSuccessful = false;
    }

    private PathFinderStatus searchStatus() {
        if (buildNewTrack) {
            return trackPathFinder.getStatus();
        }
        return pathOnTrackFinder.getStatus();
    }

    /**
     * Utility method that sets the CURSOR_MESSAGE property on the model root.
     */
    private void setCursorMessage(String s) {
        modelRoot.setProperty(ModelRootProperty.CURSOR_MESSAGE, s);
    }

    /**
     * Sets the proposed track: from the current cursor position to the
     * specified point.
     */
    public void setProposedTrack(Vec2D to, TrackMoveProducer trackBuilder) {

        Vec2D from = getCursorPosition();

        assert (trackBuilder.getTrackBuilderMode() != BuildMode.IGNORE_TRACK);
        assert (trackBuilder.getTrackBuilderMode() != BuildMode.BUILD_STATION);
        buildNewTrack = trackBuilder.getTrackBuilderMode() == BuildMode.BUILD_TRACK;

        /*
         * If we have just found the route between the two points, don't waste
         * time doing it again.
         */
        if (null != targetPoint && null != startPoint && targetPoint.equals(to) && startPoint.equals(from) && searchStatus() != PathFinderStatus.SEARCH_NOT_STARTED) {
            return;
        }

        proposedTrack.clear();
        fullWorld = (FullWorld) Utils.cloneBySerialisation(world);
        builtTrack.clear();
        isBuildTrackSuccessful = false;

        if (from.equals(to)) {
            hide();

            return;
        }

        // Check both points are on the map.
        if (!world.boundsContain(from) || !world.boundsContain(to)) {
            hide();

            return;
        }

        setTargetPoint(to);
        startPoint = from;

        try {

            BuildTrackStrategy bts = getBuildTrackStrategy();
            if (buildNewTrack) {
                trackPathFinder.setupSearch(from, to, bts);
            } else {
                pathOnTrackFinder.setupSearch(from, to);
            }
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());

            return;
        }

        updateSearch();
    }

    /**
     * @param newTargetPoint The m_targetPoint to set.
     */
    private void setTargetPoint(Vec2D newTargetPoint) {
        targetPoint = newTargetPoint;
        modelRoot.setProperty(ModelRootProperty.THINKING_POINT, newTargetPoint);
    }

    private void setVisible(boolean show) {
        if (show == visible) {
            return;
        }
        if (show) {
            modelRoot.setProperty(ModelRootProperty.PROPOSED_TRACK, proposedTrack);
        } else {
            modelRoot.setProperty(ModelRootProperty.PROPOSED_TRACK, null);
        }
        visible = show;
    }

    /**
     *
     */
    public void show() {
        setVisible(true);
    }

    /**
     *
     */
    public void update() {
        // update search for path if necessary.
        if (searchStatus() == PathFinderStatus.SEARCH_PAUSED) {
            updateSearch();
        }
    }

    /**
     *
     */
    public void updateUntilComplete() {
        while (searchStatus() != PathFinderStatus.PATH_FOUND) {
            updateSearch();
        }
    }

    /**
     * Updates the search, if the search is completed, the proposed track is
     * shown.
     */
    private void updateSearch() {
        try {
            if (buildNewTrack) {
                trackPathFinder.search(100);
            } else {
                pathOnTrackFinder.search(100);
            }
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());
            return;
        }

        if (searchStatus() == PathFinderStatus.PATH_FOUND) {
            if (buildNewTrack) {
                builtTrack = trackPathFinder.pathAsPoints();
                moveCursorMoreTiles(builtTrack);
            } else {
                boolean okSoFar = true;
                path = pathOnTrackFinder.pathAsVectors();
                BuildMode mode = getBuildMode();

                Vec2D location = new Vec2D(startPoint);
                FreerailsPrincipal fp = modelRoot.getPrincipal();
                for (TileTransition v : path) {
                    Move move;
                    attemptMove:
                    {

                        switch (mode) {
                            case REMOVE_TRACK:

                                try {
                                    move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(location, v, fullWorld, fp);

                                    // add to proposed track
                                    ChangeTrackPieceMove m = (ChangeTrackPieceMove) ((ChangeTrackPieceCompositeMove) move).getMove(0);
                                    proposedTrack.put(m.location, m.trackPieceAfter);

                                    break;
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    break attemptMove;
                                }

                            case UPGRADE_TRACK:

                                FullTerrainTile tile = (FullTerrainTile) fullWorld.getTile(location);
                                int tt = tile.getTerrainTypeID();
                                int trackRuleID = getBuildTrackStrategy().getRule(tt);

                                /*
                                 * Skip tiles that already have the right track
                                 * type.
                                 */
                                if (trackRuleID == tile.getTrackPiece().getTrackTypeID()) {
                                    break attemptMove;
                                }

                                int owner = ReadOnlyWorld.getPlayerIndex(world, fp);
                                TrackRule trackRule = (TrackRule) world.get(SharedKey.TrackRules, trackRuleID);
                                TrackPiece after = new TrackPieceImpl(tile.getTrackPiece().getTrackConfiguration(), trackRule, owner, trackRuleID);

                                /*
                                 * We don't want to 'upgrade' a station to track.
                                 * See bug 874416.
                                 */
                                if (tile.getTrackPiece().getTrackRule().isStation()) {
                                    break attemptMove;
                                }

                                move = UpgradeTrackMove.generateMove(tile.getTrackPiece(), after, location);

                                // add to proposed track
                                ChangeTrackPieceMove m = (ChangeTrackPieceMove) ((UpgradeTrackMove) move).getMove(0);
                                proposedTrack.put(m.location, m.trackPieceAfter);

                                break;

                            default:
                                throw new IllegalStateException(mode.toString());
                        }

                        MoveStatus moveStatus = move.doMove(fullWorld, fp);
                        okSoFar = moveStatus.succeeds() && okSoFar;
                    }// end of attemptMove
                    location = Vec2D.add(location, v.getD());
                }
                startPoint = new Vec2D(location);
                isBuildTrackSuccessful = okSoFar;
                if (okSoFar) {
                    setCursorMessage("");
                }
            }
            show();
        }
    }

    private BuildMode getBuildMode() {
        BuildMode mode;
        mode = (BuildMode) modelRoot.getProperty(ModelRootProperty.TRACK_BUILDER_MODE);
        return mode;
    }

    /**
     * Saves track into real world
     */
    public Vec2D updateWorld(TrackMoveProducer trackBuilder) {
        Vec2D actPoint = getCursorPosition();

        if (buildNewTrack) {
            if (!builtTrack.isEmpty()) {
                MoveStatus moveStatus = moveCursorMoreTiles(builtTrack, trackBuilder);

                // Note, reset() will have been called if moveStatus.success == false
                if (moveStatus.succeeds()) {
                    actPoint = builtTrack.get(builtTrack.size() - 1);
                    builtTrack = new ArrayList<>();
                }
            }
        } else {
            trackBuilder.setBuildTrackStrategy(getBuildTrackStrategy());
            MoveStatus moveStatus = trackBuilder.buildTrack(actPoint, path);

            if (moveStatus.succeeds()) {
                actPoint = targetPoint;
                setCursorMessage("");
                if (BuildMode.REMOVE_TRACK == getBuildMode()) {
                    SoundManager.getInstance().playSound(ClientConfig.SOUND_REMOVE_TRACK, 0);
                } else {
                    SoundManager.getInstance().playSound(ClientConfig.SOUND_BUILD_TRACK, 0);
                }
            } else {
                setCursorMessage(moveStatus.getMessage());
                reset();
            }
        }
        hide();

        return actPoint;
    }

}
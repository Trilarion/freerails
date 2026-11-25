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

import freerails.client.ClientConstants;
import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.model.track.TrackType;
import freerails.model.world.ProposedTrackWorld;
import freerails.model.track.NoTrackException;
import freerails.move.generator.TrackMoveGenerator;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.RemoveStationCompositeMove;
import freerails.util.ui.SoundManager;
import freerails.model.track.pathfinding.*;
import freerails.model.track.BuildTrackStrategy;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.Move;
import freerails.move.Status;
import freerails.util.IntPoint2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackPiece;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods to change the proposed track and save it to the real world.
 *
 * <p>The preview is checked against a {@link ProposedTrackWorld}: the live world
 * with the track pieces proposed so far on top. Each preview step is generated and
 * checked against that view, so later steps build on earlier ones, and only the
 * changed track pieces are recorded; the world is never copied or changed.
 */
public class BuildTrackController {

    private static final TaggedLogger logger = Logger.tag("BuildTrackController");
    private final ModelRoot modelRoot;
    private final TrackPathFinder trackPathFinder;
    private final PathOnTrackFinder pathOnTrackFinder;
    private final Player player;
    private final UnmodifiableWorld unmodifiableWorld;
    /** The live world with the proposed track on top; its proposed pieces are drawn. */
    private final ProposedTrackWorld preview;
    private boolean buildNewTrack = true;
    private List<IntPoint2D> builtTrack = new ArrayList<>();
    private boolean isBuildTrackSuccessful = false;
    private TileTransition[] path;
    private IntPoint2D startPoint;
    private IntPoint2D targetPoint;
    private boolean visible = false;

    /**
     * BuildTrackRenderer
     *
     * @param unmodifiableWorld ReadOnlyWorld
     */
    public BuildTrackController(UnmodifiableWorld unmodifiableWorld, ModelRoot modelRoot) {
        preview = new ProposedTrackWorld(unmodifiableWorld);
        this.unmodifiableWorld = unmodifiableWorld;
        trackPathFinder = new TrackPathFinder(unmodifiableWorld, modelRoot.getPlayer());
        pathOnTrackFinder = new PathOnTrackFinder(unmodifiableWorld);
        this.modelRoot = modelRoot;
        player = modelRoot.getPlayer();
        this.modelRoot.setProperty(ModelRootProperty.PROPOSED_TRACK, preview.getProposedTrack());
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
    private IntPoint2D getCursorPosition() {
        IntPoint2D point = (IntPoint2D) modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION);

        // Check for null & make a defensive copy
        point = null == point ? new IntPoint2D() : point;

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
     * Moves the cursor and plans the track along the path in the preview.
     */
    private void moveCursorMoreTiles(List<IntPoint2D> track) {
        moveCursorMoreTiles(track, null);
    }

    /**
     * Builds the track along the path with {@code trackBuilder} if it is not null;
     * otherwise plans it in the preview (see {@link #planBuildingTrack}).
     *
     * @param track        List
     * @param trackBuilder TrackMoveProducer
     */
    private Status moveCursorMoreTiles(List<IntPoint2D> track, TrackMoveProducer trackBuilder) {
        IntPoint2D oldPosition = getCursorPosition();

        if (!TileTransition.checkValidity(oldPosition, track.getFirst())) {
            throw new IllegalStateException(oldPosition + " and " + track.getFirst().toString());
        }

        Status status = null;
        int piecesOfNewTrack = 0;

        if (null != trackBuilder) {
            trackBuilder.setBuildTrackStrategy(getBuildTrackStrategy());
        }

        for (IntPoint2D point : track) {
            logger.debug("point" + point);
            logger.debug("oldPosition" + oldPosition);

            if (oldPosition.equals(point)) {
                logger.debug("(oldPosition.equals(point))" + point);

                continue;
            }

            TileTransition vector = TileTransition.getInstance(IntPoint2D.subtract(point, oldPosition));

            // If there is already track between the two tiles, do nothing
            TerrainTile tile = unmodifiableWorld.getTile(oldPosition);
            TrackPiece trackPiece = tile.trackPiece();
            if (trackPiece != null && trackPiece.trackConfiguration().contains(vector)) {
                oldPosition = point;
                continue;
            }
            piecesOfNewTrack++;

            if (trackBuilder != null) {
                status = trackBuilder.buildTrack(oldPosition, vector);
            } else {
                status = planBuildingTrack(oldPosition, vector);
            }

            if (status.isSuccess()) {
                setCursorMessage("");
            } else {
                setCursorMessage(status.getMessage());
                reset();

                return status;
            }

            oldPosition = point;
        }

        // Check whether there is already track at every point.
        if (piecesOfNewTrack == 0) {
            Status moveFailed = Status.fail("Track already here");
            setCursorMessage(moveFailed.getMessage());

            return moveFailed;
        }

        isBuildTrackSuccessful = true;

        // If track has actually been built, play the build track sound.
        if (trackBuilder != null && status.isSuccess()) {
            if (trackBuilder.getTrackBuilderMode() == BuildMode.BUILD_TRACK) {
                SoundManager.getInstance().playSound(ClientConstants.SOUND_BUILD_TRACK, 0);
            }
        }

        return status;
    }

    /**
     * Plans building one track section from the specified point in the specified
     * direction: the section is checked against the preview and, if allowed,
     * proposed there.
     */
    private Status planBuildingTrack(IntPoint2D point, TileTransition tileTransition) {
        TerrainTile tileA = preview.getTile(point);
        BuildTrackStrategy buildTrackStrategy = getBuildTrackStrategy();
        int trackTypeAID = buildTrackStrategy.getRule(tileA.terrainTypeId());
        TrackType trackTypeA = unmodifiableWorld.getTrackType(trackTypeAID);

        TerrainTile tileB = preview.getTile(IntPoint2D.add(point, tileTransition.getD()));
        int trackTypeBID = buildTrackStrategy.getRule(tileB.terrainTypeId());
        TrackType trackTypeB = unmodifiableWorld.getTrackType(trackTypeBID);

        ChangeTrackPieceCompositeMove move = TrackMoveGenerator.generateBuildTrackMove(point, tileTransition, trackTypeA, trackTypeB, preview, player);

        Status status = move.applicable(preview);
        if (status.isSuccess()) {
            propose(move);
        }
        return status;
    }

    /**
     * Proposes the track pieces a track move leaves on its tiles: both tiles of a
     * track section, or the single tile of an upgrade.
     *
     * @param move a track move
     */
    private void propose(Move move) {
        if (move instanceof ChangeTrackPieceCompositeMove section) {
            for (Move component : section.getMoves()) {
                propose(component);
            }
            return;
        }
        ChangeTrackPieceMove change = trackChangeOf(move);
        preview.propose(change.location(), change.trackPieceAfter());
    }

    /**
     * Cancels any proposed track and resets the pathfinder.
     */
    private void reset() {
        preview.clear();
        trackPathFinder.abandonSearch();
        builtTrack.clear();
        isBuildTrackSuccessful = false;
    }

    /**
     * Returns the current status of the track path search.
     * @return the path finder status for the active search
     */
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
    public void setProposedTrack(IntPoint2D to, TrackMoveProducer trackBuilder) {

        IntPoint2D from = getCursorPosition();

        assert trackBuilder.getTrackBuilderMode() != BuildMode.INSPECT;
        assert trackBuilder.getTrackBuilderMode() != BuildMode.BUILD_STATION;
        buildNewTrack = trackBuilder.getTrackBuilderMode() == BuildMode.BUILD_TRACK;

        /*
         * If we have just found the route between the two points, don't waste
         * time doing it again.
         */
        if (null != targetPoint && null != startPoint && targetPoint.equals(to) && startPoint.equals(from) && searchStatus() != PathFinderStatus.SEARCH_NOT_STARTED) {
            return;
        }

        preview.clear();
        builtTrack.clear();
        isBuildTrackSuccessful = false;

        if (from.equals(to)) {
            hide();

            return;
        }

        // Check both points are on the map.
        if (!unmodifiableWorld.boundsContain(from) || !unmodifiableWorld.boundsContain(to)) {
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
     * Sets the target point.
     *
     * @param newTargetPoint The m_targetPoint to set.
     */
    private void setTargetPoint(IntPoint2D newTargetPoint) {
        targetPoint = newTargetPoint;
        modelRoot.setProperty(ModelRootProperty.THINKING_POINT, newTargetPoint);
    }

    /**
     * Sets the visible.
     * @param show the show
     */
    private void setVisible(boolean show) {
        if (show == visible) {
            return;
        }
        if (show) {
            modelRoot.setProperty(ModelRootProperty.PROPOSED_TRACK, preview.getProposedTrack());
        } else {
            modelRoot.setProperty(ModelRootProperty.PROPOSED_TRACK, null);
        }
        visible = show;
    }

    /**
     * Performs the show operation.
     */
    public void show() {
        setVisible(true);
    }

    /**
     * Performs the update operation.
     */
    public void update() {
        // update search for path if necessary.
        if (searchStatus() == PathFinderStatus.SEARCH_PAUSED) {
            updateSearch();
        }
    }

    /**
     * Returns whether the path search is still in progress, so that further
     * {@link #update()} calls continue it. It is over once a path was found, no
     * path exists, or the search was abandoned because a preview step was refused
     * (see {@link #isBuildTrackSuccessful()}).
     *
     * @return true while the search is paused and not yet finished
     */
    public boolean isSearching() {
        return searchStatus() == PathFinderStatus.SEARCH_PAUSED;
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

                IntPoint2D location = new IntPoint2D(startPoint);
                Player fp = modelRoot.getPlayer();
                for (TileTransition v : path) {
                    Move move;
                    attemptMove:
                    {

                        switch (mode) {
                            case REMOVE_TRACK:

                                try {
                                    move = TrackMoveGenerator.generateRemoveTrackMove(location, v, preview, fp);
                                } catch (NoTrackException | IllegalStateException e1) {
                                    logger.error(e1, "Could not generate track removal move.");
                                    break attemptMove;
                                }
                                break;

                            case UPGRADE_TRACK:

                                TerrainTile tile = preview.getTile(location);
                                int tt = tile.terrainTypeId();
                                int trackRuleID = getBuildTrackStrategy().getRule(tt);

                                /*
                                 * Skip tiles that already have the right track
                                 * type, and other players' track, which is never
                                 * upgraded.
                                 */
                                if (trackRuleID == tile.trackPiece().trackType().getId()
                                        || tile.trackPiece().playerId() != fp.getId()) {
                                    break attemptMove;
                                }

                                ChangeTrackPieceMove m;
                                try {
                                    m = TrackMoveGenerator.generateUpgradeTrackMove(
                                            location, unmodifiableWorld.getTrackType(trackRuleID), preview, fp);
                                } catch (NoTrackException | IllegalStateException e1) {
                                    // Stations are never upgraded. See bug 874416.
                                    break attemptMove;
                                }
                                move = m;
                                break;

                            default:
                                throw new IllegalStateException(mode.toString());
                        }

                        // Only show and plan changes that the track rules allow.
                        Status status = move.applicable(preview);
                        if (status.isSuccess()) {
                            propose(move);
                        } else {
                            okSoFar = false;
                            setCursorMessage(status.getMessage());
                        }
                    }// end of attemptMove
                    location = IntPoint2D.add(location, v.getD());
                }
                startPoint = new IntPoint2D(location);
                isBuildTrackSuccessful = okSoFar;
                if (okSoFar) {
                    setCursorMessage("");
                }
            }
            show();
        }
    }

    /**
     * Returns the single-tile track change of a track move component, including
     * the station track removal inside a station removal.
     *
     * @param move a component of a track move
     * @return the track change
     */
    private static ChangeTrackPieceMove trackChangeOf(Move move) {
        if (move instanceof RemoveStationCompositeMove stationRemoval) {
            return stationRemoval.trackChange();
        }
        return (ChangeTrackPieceMove) move;
    }

    /**
     * Returns the build mode.
     * @return the build mode
     */
    private BuildMode getBuildMode() {
        BuildMode mode;
        mode = (BuildMode) modelRoot.getProperty(ModelRootProperty.TRACK_BUILDER_MODE);
        return mode;
    }

    /**
     * Saves track into real world
     */
    public IntPoint2D updateWorld(TrackMoveProducer trackBuilder) {
        IntPoint2D actPoint = getCursorPosition();

        if (buildNewTrack) {
            if (!builtTrack.isEmpty()) {
                Status status = moveCursorMoreTiles(builtTrack, trackBuilder);

                // Note, reset() will have been called if moveStatus.success == false
                if (status.isSuccess()) {
                    actPoint = builtTrack.getLast();
                    builtTrack = new ArrayList<>();
                }
            }
        } else {
            trackBuilder.setBuildTrackStrategy(getBuildTrackStrategy());
            Status status = trackBuilder.buildTrack(actPoint, path);

            if (status.isSuccess()) {
                actPoint = targetPoint;
                setCursorMessage("");
                if (BuildMode.REMOVE_TRACK == getBuildMode()) {
                    SoundManager.getInstance().playSound(ClientConstants.SOUND_REMOVE_TRACK, 0);
                } else {
                    SoundManager.getInstance().playSound(ClientConstants.SOUND_BUILD_TRACK, 0);
                }
            } else {
                setCursorMessage(status.getMessage());
                reset();
            }
        }
        hide();

        return actPoint;
    }

}
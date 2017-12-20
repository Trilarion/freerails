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

package freerails.client.renderer;

import freerails.client.ClientConfig;
import freerails.client.common.SoundManager;
import freerails.controller.*;
import freerails.move.ChangeTrackPieceCompositeMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.move.UpgradeTrackMove;
import freerails.util.ImPoint;
import freerails.world.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FreerailsTile;
import freerails.world.track.TrackPiece;
import freerails.world.track.TrackPieceImpl;
import freerails.world.track.TrackRule;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static freerails.controller.TrackMoveProducer.BuildMode.*;

/**
 * This class provides methods to change the proposed track and save it to the
 * real world.
 *
 * TODO freerails.client.renderer is not the most logical place for this class.
 */
public class BuildTrackController implements GameModel {

    private static final Logger LOGGER = Logger
            .getLogger(BuildTrackController.class.getName());
    private final ModelRoot modelRoot;
    private final TrackPathFinder path4newTrackFinder;
    private final PathOnTrackFinder pathOnExistingTrackFinder;
    private final FreerailsPrincipal principal;
    private final ReadOnlyWorld realWorld;
    private final SoundManager soundManager = SoundManager.getSoundManager();
    private final WorldDiffs worldDiffs;
    private boolean buildNewTrack = true;
    private List<ImPoint> builtTrack = new ArrayList<>();
    private boolean isBuildTrackSuccessful = false;
    private TileTransition[] path;
    private ImPoint startPoint;
    private ImPoint targetPoint;
    private boolean visible = false;

    /**
     * BuildTrackRenderer
     *
     * @param readOnlyWorld ReadOnlyWorld
     * @param modelRoot
     */
    public BuildTrackController(ReadOnlyWorld readOnlyWorld, ModelRoot modelRoot) {
        worldDiffs = new WorldDiffs(readOnlyWorld);
        realWorld = readOnlyWorld;
        path4newTrackFinder = new TrackPathFinder(readOnlyWorld, modelRoot
                .getPrincipal());
        pathOnExistingTrackFinder = new PathOnTrackFinder(readOnlyWorld);
        this.modelRoot = modelRoot;
        principal = modelRoot.getPrincipal();
        setWorldDiffs(worldDiffs);
    }

    /**
     * Utility method that gets the BuildTrackStrategy from the model root.
     */
    private BuildTrackStrategy getBts() {
        BuildTrackStrategy btss = (BuildTrackStrategy) modelRoot
                .getProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY);
        if (null == btss)
            throw new NullPointerException();
        return btss;
    }

    /**
     * Utility method that gets the cursor position from the model root.
     */
    private ImPoint getCursorPosition() {
        ImPoint point = (ImPoint) modelRoot
                .getProperty(ModelRoot.Property.CURSOR_POSITION);

        // Check for null & make a defensive copy
        point = null == point ? new ImPoint() : point;

        if (!modelRoot.getWorld().boundsContain(point.x, point.y)) {
            throw new IllegalStateException(String.valueOf(point));
        }

        return point;
    }

    /**
     * Hides and cancels any proposed track.
     */
    public void hide() {
        this.setVisible(false);
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
     *
     * @return
     */
    public boolean isBuildTrackSuccessful() {
        return isBuildTrackSuccessful;
    }

    /**
     * Moves cursor which causes track to be built on the worldDiff object.
     */
    private void moveCursorMoreTiles(List<ImPoint> track) {
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
    private MoveStatus moveCursorMoreTiles(List<ImPoint> track,
                                           TrackMoveProducer trackBuilder) {
        ImPoint oldPosition = getCursorPosition();

        if (!TileTransition.checkValidity(oldPosition, track.get(0))) {
            throw new IllegalStateException(oldPosition.toString() + " and "
                    + track.get(0).toString());
        }

        MoveStatus ms = null;
        int piecesOfNewTrack = 0;

        if (null != trackBuilder) {
            trackBuilder.setBuildTrackStrategy(getBts());
        }

        for (ImPoint point : track) {
            LOGGER.debug("point" + point);
            LOGGER.debug("oldPosition" + oldPosition);

            if (oldPosition.equals(point)) {
                LOGGER.debug("(oldPosition.equals(point))" + point);

                continue;
            }

            TileTransition vector = TileTransition.getInstance(point.x - oldPosition.x, point.y
                    - oldPosition.y);

            // If there is already track between the two tiles, do nothing
            FreerailsTile tile = (FreerailsTile) realWorld.getTile(
                    oldPosition.x, oldPosition.y);

            if (tile.getTrackPiece().getTrackConfiguration().contains(vector)) {
                oldPosition = point;

                continue;
            }
            piecesOfNewTrack++;

            if (trackBuilder != null) {
                ms = trackBuilder.buildTrack(oldPosition, vector);
            } else {
                ms = planBuildingTrack(oldPosition, vector);
            }

            if (ms.ok) {
                setCursorMessage("");
            } else {
                setCursorMessage(ms.message);
                reset();

                return ms;
            }

            oldPosition = point;
        }

        /* Check whether there is already track at every point. */
        if (piecesOfNewTrack == 0) {
            MoveStatus moveFailed = MoveStatus.moveFailed("Track already here");
            setCursorMessage(moveFailed.message);

            return moveFailed;
        }

        isBuildTrackSuccessful = true;

        // If track has actually been built, play the build track sound.
        if (trackBuilder != null && ms.isOk()) {
            if (trackBuilder.getTrackBuilderMode() == BUILD_TRACK) {
                this.soundManager.playSound(
                        ClientConfig.SOUND_BUILD_TRACK, 0);
            }
        }

        return ms;
    }

    /**
     * Attempts to building track from the specified point in the specified
     * direction on the worldDiff object.
     */
    private MoveStatus planBuildingTrack(ImPoint point, TileTransition vector) {
        FreerailsTile tileA = (FreerailsTile) worldDiffs.getTile(point.x,
                point.y);
        BuildTrackStrategy bts = getBts();
        int trackTypeAID = bts.getRule(tileA.getTerrainTypeID());
        TrackRule trackRuleA = (TrackRule) worldDiffs.get(SKEY.TRACK_RULES,
                trackTypeAID);

        FreerailsTile tileB = (FreerailsTile) worldDiffs.getTile(point.x
                + vector.deltaX, point.y + vector.deltaY);
        int trackTypeBID = bts.getRule(tileB.getTerrainTypeID());
        TrackRule trackRuleB = (TrackRule) worldDiffs.get(SKEY.TRACK_RULES,
                trackTypeBID);

        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(point, vector, trackRuleA, trackRuleB,
                        worldDiffs, principal);

        return move.doMove(worldDiffs, principal);
    }

    /**
     * Cancels any proposed track and resets the path finder.
     */
    private void reset() {
        worldDiffs.reset();
        path4newTrackFinder.abandonSearch();
        this.builtTrack.clear();
        this.isBuildTrackSuccessful = false;
    }

    int searchStatus() {
        if (buildNewTrack) {
            return path4newTrackFinder.getStatus();
        }
        return pathOnExistingTrackFinder.getStatus();
    }

    /**
     * Utility method that sets the CURSOR_MESSAGE property on the model root.
     */
    private void setCursorMessage(String s) {
        modelRoot.setProperty(ModelRoot.Property.CURSOR_MESSAGE, s);
    }

    /**
     * Sets the proposed track: from the current cursor position to the
     * specified point.
     *
     * @param to
     * @param trackBuilder
     */
    public void setProposedTrack(ImPoint to, TrackMoveProducer trackBuilder) {

        ImPoint from = getCursorPosition();

        assert (trackBuilder.getTrackBuilderMode() != IGNORE_TRACK);
        assert (trackBuilder.getTrackBuilderMode() != BUILD_STATION);
        buildNewTrack = trackBuilder.getTrackBuilderMode() == BUILD_TRACK;

        /*
         * If we have just found the route between the two points, don't waste
         * time doing it again.
         */
        if (null != targetPoint && null != startPoint && targetPoint.equals(to)
                && startPoint.equals(from)
                && searchStatus() != IncrementalPathFinder.SEARCH_NOT_STARTED) {
            return;
        }

        worldDiffs.reset();
        builtTrack.clear();
        isBuildTrackSuccessful = false;

        if (from.equals(to)) {
            hide();

            return;
        }

        /* Check both points are on the map. */
        if (!realWorld.boundsContain(from.x, from.y)
                || !realWorld.boundsContain(to.x, to.y)) {
            hide();

            return;
        }

        setTargetPoint(to);
        startPoint = from;

        try {

            BuildTrackStrategy bts = getBts();
            if (buildNewTrack) {
                path4newTrackFinder.setupSearch(from, to, bts);
            } else {
                pathOnExistingTrackFinder.setupSearch(from, to);
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
    private void setTargetPoint(ImPoint newTargetPoint) {
        this.targetPoint = newTargetPoint;
        ImPoint p = null == newTargetPoint ? null : newTargetPoint;
        modelRoot.setProperty(ModelRoot.Property.THINKING_POINT, p);
    }

    private void setVisible(boolean show) {
        if (show == visible) {
            return;
        }
        if (show) {
            setWorldDiffs(worldDiffs);
        } else {
            setWorldDiffs(null);
        }
        this.visible = show;
    }

    private void setWorldDiffs(WorldDiffs worldDiffs) {
        modelRoot.setProperty(ModelRoot.Property.PROPOSED_TRACK, worldDiffs);
    }

    /**
     *
     */
    public void show() {
        this.setVisible(true);
    }

    /**
     *
     */
    public void update() {
        // update search for path if necessary.
        if (searchStatus() == IncrementalPathFinder.SEARCH_PAUSED) {
            updateSearch();
        }
    }

    /**
     *
     */
    public void updateUntilComplete() {
        while (searchStatus() != IncrementalPathFinder.PATH_FOUND) {
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
                path4newTrackFinder.search(100);
            } else {
                pathOnExistingTrackFinder.search(100);
            }
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());

            return;
        }

        if (searchStatus() == IncrementalPathFinder.PATH_FOUND) {
            if (buildNewTrack) {
                builtTrack = path4newTrackFinder.pathAsPoints();
                moveCursorMoreTiles(builtTrack);
            } else {
                boolean okSoFar = true;
                path = pathOnExistingTrackFinder.pathAsVectors();
                TrackMoveProducer.BuildMode mode = getBuildMode();

                int locationX = startPoint.x;
                int locationY = startPoint.y;
                FreerailsPrincipal fp = modelRoot.getPrincipal();
                for (TileTransition v : path) {
                    Move move;
                    attemptMove:
                    {

                        switch (mode) {
                            case REMOVE_TRACK:

                                try {
                                    move = ChangeTrackPieceCompositeMove
                                            .generateRemoveTrackMove(new ImPoint(
                                                            locationX, locationY), v,
                                                    worldDiffs, fp);
                                    break;

                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    break attemptMove;
                                }

                            case UPGRADE_TRACK:

                                int owner = ChangeTrackPieceCompositeMove.getOwner(
                                        fp, worldDiffs);
                                FreerailsTile tile = (FreerailsTile) worldDiffs
                                        .getTile(locationX, locationY);
                                int tt = tile.getTerrainTypeID();
                                int trackRuleID = getBts().getRule(tt);

                                /*
                                 * Skip tiles that already have the right track
                                 * type.
                                 */
                                if (trackRuleID == tile.getTrackPiece()
                                        .getTrackTypeID()) {
                                    break attemptMove;
                                }

                                TrackRule trackRule = (TrackRule) worldDiffs.get(
                                        SKEY.TRACK_RULES, trackRuleID);
                                TrackPiece after = new TrackPieceImpl(tile
                                        .getTrackPiece().getTrackConfiguration(),
                                        trackRule, owner, trackRuleID);

                                /*
                                 * We don't want to 'upgrade' a station to track.
                                 * See bug 874416.
                                 */
                                if (tile.getTrackPiece().getTrackRule().isStation()) {
                                    break attemptMove;
                                }

                                move = UpgradeTrackMove.generateMove(tile
                                        .getTrackPiece(), after, new ImPoint(
                                        locationX, locationY));
                                break;

                            default:
                                throw new IllegalStateException(mode.toString());

                        }// end of switch statement
                        MoveStatus ms = move.doMove(worldDiffs, fp);
                        okSoFar = ms.ok && okSoFar;
                    }// end of attemptMove
                    locationX += v.deltaX;
                    locationY += v.deltaY;
                }// end for loop
                startPoint = new ImPoint(locationX, locationY);
                isBuildTrackSuccessful = okSoFar;
                if (okSoFar) {
                    setCursorMessage("");
                }
            }
            show();
        }
    }

    private TrackMoveProducer.BuildMode getBuildMode() {
        TrackMoveProducer.BuildMode mode;
        mode = (TrackMoveProducer.BuildMode) modelRoot
                .getProperty(ModelRoot.Property.TRACK_BUILDER_MODE);
        return mode;
    }

    /**
     * Saves track into real world
     *
     * @param trackBuilder
     * @return
     */
    public ImPoint updateWorld(TrackMoveProducer trackBuilder) {
        ImPoint actPoint = getCursorPosition();

        if (buildNewTrack) {
            if (builtTrack.size() > 0) {
                MoveStatus ms = moveCursorMoreTiles(builtTrack, trackBuilder);

                /* Note, reset() will have been called if ms.ok == false */
                if (ms.ok) {
                    actPoint = builtTrack.get(builtTrack.size() - 1);
                    builtTrack = new ArrayList<>();
                }
            }
        } else {
            trackBuilder.setBuildTrackStrategy(getBts());
            MoveStatus ms = trackBuilder.buildTrack(actPoint, path);
            // MoveStatus ms = trackBuilder.buildTrack(startPoint, path);
            if (ms.ok) {
                actPoint = targetPoint;
                setCursorMessage("");
                if (REMOVE_TRACK == getBuildMode()) {
                    soundManager.playSound(
                            ClientConfig.SOUND_REMOVE_TRACK, 0);
                } else {
                    soundManager.playSound(
                            ClientConfig.SOUND_BUILD_TRACK, 0);
                }

            } else {
                setCursorMessage(ms.message);
                reset();
            }
        }
        hide();

        return actPoint;
    }

}
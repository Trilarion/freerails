package jfreerails.client.renderer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.SoundManager;
import jfreerails.controller.BuildTrackStrategy;
import jfreerails.controller.IncrementalPathFinder;
import jfreerails.controller.PathNotFoundException;
import jfreerails.controller.PathOnTrackFinder;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.TrackPathFinder;
import static jfreerails.controller.TrackMoveProducer.BuildMode.BUILD_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.IGNORE_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.REMOVE_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.UPGRADE_TRACK;
import static jfreerails.controller.TrackMoveProducer.BuildMode.BUILD_STATION;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.UpgradeTrackMove;
import jfreerails.util.GameModel;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldDifferences;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackPieceImpl;
import jfreerails.world.track.TrackRule;

/**
 * This class provides methods to change the proposed track and save it to the
 * real world.
 *
 * TODO jfreerails.client.renderer is not the most logical place for this class.
 *
 * @author MystiqueAgent
 * @author Luke
 *
 */
public class BuildTrackController implements GameModel {
    
    private static final Logger LOGGER = Logger
            .getLogger(BuildTrackController.class.getName());
    
    private boolean m_buildNewTrack = true;
    
    private List<Point> m_builtTrack = new ArrayList<Point>();
    
    private boolean m_isBuildTrackSuccessful = false;
    
    private final ModelRoot m_modelRoot;
    
    private OneTileMoveVector[] m_path;
    
    private TrackPathFinder m_path4newTrackFinder;
    
    private PathOnTrackFinder m_pathOnExistingTrackFinder;
    
    private FreerailsPrincipal m_principal;
    
    private ReadOnlyWorld m_realWorld;
    
    private SoundManager m_soundManager = SoundManager.getSoundManager();
    
    private Point m_startPoint;
    
    private Point m_targetPoint;
    
    private boolean m_visible = false;
    
    private WorldDifferences m_worldDiffs;
    
    /**
     * BuildTrackRenderer
     *
     * @param readOnlyWorld
     *            ReadOnlyWorld
     */
    public BuildTrackController(ReadOnlyWorld readOnlyWorld, ModelRoot modelRoot) {
        m_worldDiffs = new WorldDifferences(readOnlyWorld);
        m_realWorld = readOnlyWorld;
        m_path4newTrackFinder = new TrackPathFinder(readOnlyWorld, modelRoot.getPrincipal());
        m_pathOnExistingTrackFinder = new PathOnTrackFinder(readOnlyWorld);
        this.m_modelRoot = modelRoot;
        m_principal = modelRoot.getPrincipal();
        setWorldDiffs(m_worldDiffs);
    }
    
    /** Called when we want to upgrade or remove track between two points. */
    public MoveStatus changeTrack(Point a, Point b,
            TrackMoveProducer trackBuilder) {
        TrackMoveProducer.BuildMode mode = trackBuilder.getTrackBuilderMode();
        assert (mode == REMOVE_TRACK || mode == UPGRADE_TRACK);
        return MoveStatus.MOVE_OK;
    }
    
    /** Utility method that gets the BuildTrackStrategy from the model root. */
    private BuildTrackStrategy getBts() {
        BuildTrackStrategy btss = (BuildTrackStrategy) m_modelRoot
                .getProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY);
        if (null == btss)
            throw new NullPointerException();
        return btss;
    }
    
    /** Utility method that gets the cursor position from the model root. */
    private Point getCursorPosition() {
        Point point = (Point) m_modelRoot
                .getProperty(ModelRoot.Property.CURSOR_POSITION);
        
        // Check for null & make a defensive copy
        point = null == point ? new Point() : new Point(point);
        
        if (!m_modelRoot.getWorld().boundsContain(point.x, point.y)) {
            throw new IllegalStateException(String.valueOf(point));
        }
        
        return point;
    }
    
    /** Hides and cancels any proposed track. */
    public void hide() {
        this.setVisible(false);
        setTargetPoint(null);
        reset();
    }
    
    /**
     * returns <code>true</code> if the track is being build - it is iff the
     * build track is shown
     *
     * @return boolean
     */
    public boolean isBuilding() {
        return m_visible;
    }
    
    /** Returns true if all the track pieces can be successfully built. */
    public boolean isBuildTrackSuccessful() {
        return m_isBuildTrackSuccessful;
    }
    
    /** Moves cursor which causes track to be built on the worldDiff object. */
    private void moveCursorMoreTiles(List track) {
        moveCursorMoreTiles(track, null);
    }
    
    /**
     * uses <code>trackBuilder</code> if not null -- otherwise uses own
     * <code>buildTrack</code> method - that is applied on
     * <code>worldDifferences</code>
     *
     * @param track
     *            List
     * @param trackBuilder
     *            TrackMoveProducer
     */
    private MoveStatus moveCursorMoreTiles(List track,
            TrackMoveProducer trackBuilder) {
        Point oldPosition = getCursorPosition();
        
        MoveStatus ms = null;
        int piecesOfNewTrack = 0;
        
        if (null != trackBuilder) {
            trackBuilder.setBuildTrackStrategy(getBts());
        }
        
        for (Iterator iter = track.iterator(); iter.hasNext();) {
            Point point = (Point) iter.next();
            LOGGER.fine("point" + point);
            LOGGER.fine("oldPosition" + oldPosition);
            
            if (oldPosition.equals(point)) {
                LOGGER.fine("(oldPosition.equals(point))" + point);
                
                continue;
            }
            
            OneTileMoveVector vector = OneTileMoveVector.getInstance(point.x
                    - oldPosition.x, point.y - oldPosition.y);
            
            // If there is already track between the two tiles, do nothing
            FreerailsTile tile = (FreerailsTile) m_realWorld.getTile(
                    oldPosition.x, oldPosition.y);
            
            if (tile.getTrackConfiguration().contains(vector)) {
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
        
        m_isBuildTrackSuccessful = true;
        
        // If track has actually been built, play the build track sound.
        if (trackBuilder != null && ms.isOk()) {
            if (trackBuilder.getTrackBuilderMode() == BUILD_TRACK) {
                this.m_soundManager.playSound(
                        "/jfreerails/client/sounds/buildtrack.wav", 0);
            }
        }
        
        return ms;
    }
    
    /**
     * Attempts to building track from the specified point in the specified
     * direction on the worldDiff object.
     */
    private MoveStatus planBuildingTrack(Point point, OneTileMoveVector vector) {
        WorldDifferences worldDiffs = m_worldDiffs;
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
                worldDiffs, m_principal);
        
        return move.doMove(worldDiffs, m_principal);
    }
    
    /** Cancels any proposed track and resets the path finder. */
    private void reset() {
        m_worldDiffs.reset();
        m_path4newTrackFinder.abandonSearch();
        this.m_builtTrack.clear();
        this.m_isBuildTrackSuccessful = false;
    }
    
    private int searchStatus() {
        if (m_buildNewTrack) {
            return m_path4newTrackFinder.getStatus();
        }
        return m_pathOnExistingTrackFinder.getStatus();
    }
    
    /** Utility method that sets the CURSOR_MESSAGE property on the model root. */
    private void setCursorMessage(String s) {
        m_modelRoot.setProperty(ModelRoot.Property.CURSOR_MESSAGE, s);
    }
    
    /** Sets the proposed track. */
    public void setProposedTrack(Point startPoint, Point endPoint,
            TrackMoveProducer trackBuilder) {
        assert (trackBuilder.getTrackBuilderMode() != IGNORE_TRACK);
        assert (trackBuilder.getTrackBuilderMode() != BUILD_STATION);
        m_buildNewTrack = trackBuilder.getTrackBuilderMode() == BUILD_TRACK;
        
                /*
                 * If we have just found the route between the two points, don't waste
                 * time doing it again.
                 */
        if (null != m_targetPoint && null != m_startPoint
                && m_targetPoint.equals(endPoint)
                && m_startPoint.equals(startPoint)
                && searchStatus() != IncrementalPathFinder.SEARCH_NOT_STARTED) {
            return;
        }
        
        m_worldDiffs.reset();
        m_builtTrack.clear();
        m_isBuildTrackSuccessful = false;
        
        if (startPoint.equals(endPoint)) {
            hide();
            
            return;
        }
        
        /* Check both points are on the map. */
        if (!m_realWorld.boundsContain(startPoint.x, startPoint.y)
        || !m_realWorld.boundsContain(endPoint.x, endPoint.y)) {
            hide();
            
            return;
        }
        
        setTargetPoint(new Point(endPoint));
        m_startPoint = new Point(startPoint);
        
        try {
            
            BuildTrackStrategy bts = getBts();
            if (m_buildNewTrack) {
                m_path4newTrackFinder.setupSearch(startPoint, endPoint, bts);
            } else {
                m_pathOnExistingTrackFinder.setupSearch(startPoint, endPoint,
                        bts);
            }
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());
            
            return;
        }
        
        updateSearch();
    }
    
    /**
     * @param newTargetPoint
     *            The m_targetPoint to set.
     */
    private void setTargetPoint(Point newTargetPoint) {
        this.m_targetPoint = newTargetPoint;
        Point p = null == newTargetPoint ? null : new Point(newTargetPoint);
        m_modelRoot.setProperty(ModelRoot.Property.THINKING_POINT, p);
    }
    
    private void setVisible(boolean show) {
        if (show == m_visible) {
            return;
        }
        if (show) {
            setWorldDiffs(m_worldDiffs);
        } else {
            setWorldDiffs(null);
        }
        this.m_visible = show;
    }
    
    private void setWorldDiffs(WorldDifferences worldDiffs) {
        m_modelRoot.setProperty(ModelRoot.Property.PROPOSED_TRACK, worldDiffs);
    }
    
    public void show() {
        this.setVisible(true);
    }
    
    public void update() {
        // update search for path if necessary.
        if (searchStatus() == IncrementalPathFinder.SEARCH_PAUSED) {
            updateSearch();
        }
    }
    
    /**
     * Updates the search, if the search is completed, the proposed track is
     * shown.
     */
    private void updateSearch() {
        try {
            if (m_buildNewTrack) {
                m_path4newTrackFinder.search(100);
            } else {
                m_pathOnExistingTrackFinder.search(100);
            }
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());
            
            return;
        }
        
        if (searchStatus() == IncrementalPathFinder.PATH_FOUND) {
            if (m_buildNewTrack) {
                m_builtTrack = m_path4newTrackFinder.pathAsPoints();
                moveCursorMoreTiles(m_builtTrack);
            } else {
                boolean okSoFar = true;
                m_path = m_pathOnExistingTrackFinder.pathAsVectors();
                TrackMoveProducer.BuildMode mode = getBuildMode();
                
                Point location = new Point(m_startPoint);
                FreerailsPrincipal principal = m_modelRoot.getPrincipal();
                for (OneTileMoveVector v : m_path) {
                    Move move;
                    attemptMove: {
                        
                        switch (mode){
                            case REMOVE_TRACK:
                                
                                try {
                                    move = ChangeTrackPieceCompositeMove
                                            .generateRemoveTrackMove(location, v,
                                            m_worldDiffs, principal);
                                    break;
                                    
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    break attemptMove;
                                }
                                
                            case UPGRADE_TRACK:
                                
                                int owner = ChangeTrackPieceCompositeMove.getOwner(
                                        principal, m_worldDiffs);
                                FreerailsTile tile = (FreerailsTile) m_worldDiffs
                                        .getTile(location.x, location.y);
                                int tt = tile.getTerrainTypeID();
                                int trackRuleID = getBts().getRule(tt);
                                
                                                        /*
                                                         * Skip tiles that already have the right track
                                                         * type.
                                                         */
                                if (trackRuleID == tile.getTrackTypeID()) {
                                    break attemptMove;
                                }
                                
                                TrackRule trackRule = (TrackRule) m_worldDiffs.get(
                                        SKEY.TRACK_RULES, trackRuleID);
                                TrackPiece after = new TrackPieceImpl(tile
                                        .getTrackConfiguration(), trackRule, owner,
                                        trackRuleID);
                                
                                                        /*
                                                         * We don't want to 'upgrade' a station to track.
                                                         * See bug 874416.
                                                         */
                                if (tile.getTrackRule().isStation()) {
                                    break attemptMove;
                                }
                                
                                move = UpgradeTrackMove.generateMove(tile
                                        .getTrackPiece(), after, location);
                                break;
                                
                            default:
                                throw new IllegalStateException(mode.toString());
                                
                                
                        }//end of switch statement
                        MoveStatus ms = move.doMove(m_worldDiffs, principal);
                        okSoFar = ms.ok ? okSoFar : false;
                    }//end of attemptMove
                    location.x += v.deltaX;
                    location.y += v.deltaY;
                }//end for loop
                m_isBuildTrackSuccessful = okSoFar;
                if (okSoFar) {
                    setCursorMessage("");
                }
            }
            show();
        }
    }
    
    private TrackMoveProducer.BuildMode getBuildMode() {
        TrackMoveProducer.BuildMode mode;
        mode = (TrackMoveProducer.BuildMode) m_modelRoot
                .getProperty(ModelRoot.Property.TRACK_BUILDER_MODE);
        return mode;
    }
    
    /**
     * Saves track into real world
     */
    public Point updateWorld(TrackMoveProducer trackBuilder) {
        Point actPoint = getCursorPosition();
        
        if (m_buildNewTrack) {
            if (m_builtTrack.size() > 0) {
                MoveStatus ms = moveCursorMoreTiles(m_builtTrack, trackBuilder);
                
                /* Note, reset() will have been called if ms.ok == false */
                if (ms.ok) {
                    actPoint = m_builtTrack.get(m_builtTrack.size() - 1);
                    m_builtTrack = new ArrayList<Point>();
                }
            }
        } else {
            trackBuilder.setBuildTrackStrategy(getBts());
            MoveStatus ms = trackBuilder.buildTrack(m_startPoint, m_path);
            if (ms.ok) {
                actPoint = new Point(m_targetPoint);
                setCursorMessage("");               
                if (REMOVE_TRACK == getBuildMode()) {
                    m_soundManager.playSound(
                            "/jfreerails/client/sounds/removetrack.wav", 0);
                } else {
                    m_soundManager.playSound(
                            "/jfreerails/client/sounds/buildtrack.wav", 0);
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
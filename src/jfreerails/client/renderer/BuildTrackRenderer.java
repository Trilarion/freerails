package jfreerails.client.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.Painter;
import jfreerails.client.common.SoundManager;
import jfreerails.controller.BuildTrackStrategy;
import jfreerails.controller.IncrementalPathFinder;
import jfreerails.controller.PathNotFoundException;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.TrackPathFinder;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.MoveStatus;
import jfreerails.util.GameModel;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldDifferences;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


/** This class draws the track being build.
 * 
 * @author MystiqueAgent
 * @author Luke
 * 
 * TODO Split into two classes: one which displays any proposed track and one that provides methods to change 
 * the proposed track and save it to the real world.
 * */
public class BuildTrackRenderer implements Painter, GameModel {
    public static final int BIG_DOT_WIDTH = 12;
    private static final Logger LOGGER = Logger.getLogger(BuildTrackRenderer.class.getName());
    public static final int SMALL_DOT_WIDTH = 6;
    private List<Point> m_builtTrack = new ArrayList<Point>();
    private boolean m_isBuildTrackSuccessful = false;
    private ModelRoot m_modelRoot;
    private FreerailsPrincipal m_principal;
    private ReadOnlyWorld m_realWorld;
    private WorldDifferences m_worldDiffs;
    private boolean m_setUp = false;
    private boolean m_show = false;
    private SoundManager m_soundManager = SoundManager.getSoundManager();
    private Point m_startPoint;
    private Point m_targetPoint;
    private final Dimension m_tileSize = new Dimension(30, 30);
    private TrackPathFinder m_trackPathFinder;
    private TrackPieceRendererList m_trackPieceViewList;    

    /**
     * BuildTrackRenderer
     *
     * @param readOnlyWorld ReadOnlyWorld
     */
    public BuildTrackRenderer(ReadOnlyWorld readOnlyWorld,
        TrackPieceRendererList trackPieceViewList) {
    	m_worldDiffs = new WorldDifferences(readOnlyWorld);
      
        this.m_trackPieceViewList = trackPieceViewList;
        m_realWorld = readOnlyWorld;
        m_trackPathFinder = new TrackPathFinder(readOnlyWorld);       
    }

   
    /** Utility method that gets the BuildTrackStrategy from the model root.*/
	private BuildTrackStrategy getBts() {
		BuildTrackStrategy btss = (BuildTrackStrategy)m_modelRoot.getProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY);
		if(null == btss) throw new NullPointerException();
		return btss;
	}

	/** Utility method taht gets the cursor position from the model root.*/
    private Point getCursorPosition() {
        Point point = (Point)m_modelRoot.getProperty(ModelRoot.Property.CURSOR_POSITION);

        //Check for null & make a defensive copy
        point = null == point ? new Point() : new Point(point);

        if (!m_modelRoot.getWorld().boundsContain(point.x, point.y)) {
            throw new IllegalStateException(String.valueOf(point));
        }

        return point;
    }


    /** Hides and cancels any proposed track.*/ 
    public void hide() {
        this.setM_show(false);
        setTargetPoint(null);
        reset();
    }

    /**
     * returns <code>true</code> if the track is being build - it is iff the build track is shown
     *
     * @return boolean
     */
    public boolean isBuilding() {
        return isM_show();
    }

    /** Returns true if all the track pieces can be successfully built.*/
    public boolean isBuildTrackSuccessful() {
        return m_isBuildTrackSuccessful;
    }

    /** Moves cursor which causes track to be built on the worldDiff object.*/
    private void moveCursorMoreTiles(List track) {
        moveCursorMoreTiles(track, null);
    }

    /**
     * uses <code>trackBuilder</code> if not null -- otherwise uses own <code>buildTrack</code> method - that is applied on <code>worldDifferences</code>
     * @param track List
     * @param trackBuilder TrackMoveProducer
     */
    private MoveStatus moveCursorMoreTiles(List track,
        TrackMoveProducer trackBuilder) {
        Point oldPosition = getCursorPosition();

        MoveStatus ms = null;
        int piecesOfNewTrack = 0;
        
        if(null != trackBuilder){
        	trackBuilder.setBuildTrackStrategy(getBts());
        }

        for (Iterator iter = track.iterator(); iter.hasNext();) {
            Point point = (Point)iter.next();
            LOGGER.fine("point" + point);
            LOGGER.fine("oldPosition" + oldPosition);

            if (oldPosition.equals(point)) {
                LOGGER.fine("(oldPosition.equals(point))" + point);

                continue;
            }

            OneTileMoveVector vector = OneTileMoveVector.getInstance(point.x -
                    oldPosition.x, point.y - oldPosition.y);

            //If there is already track between the two tiles, do nothing
            FreerailsTile tile = (FreerailsTile)m_realWorld.getTile(oldPosition.x,
                    oldPosition.y);

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

        /* Check whether there is already track at every point.*/
        if (piecesOfNewTrack == 0) {
            MoveStatus moveFailed = MoveStatus.moveFailed("Track already here");
            setCursorMessage(moveFailed.message);

            return moveFailed;
        }

        m_isBuildTrackSuccessful = true;

        //If track has actually been built, play the build track sound.
        if (trackBuilder != null && ms.isOk()) {
            if (trackBuilder.getTrackBuilderMode() == TrackMoveProducer.BUILD_TRACK) {
                this.m_soundManager.playSound("/jfreerails/client/sounds/buildtrack.wav",
                    0);
            }
        }

        return ms;
    }

    /** Paints the proposed track and dots to distinguish the proposed track from any existing track.*/
    public void paint(Graphics2D g) {
        

        
        WorldDifferences worldDiffs = getWorldDiffs();
        if(null != worldDiffs){
			for (Iterator iter = worldDiffs.getMapDifferences();
                    iter.hasNext();) {
                Point point = (Point)iter.next();               
                TrackPiece tp = (TrackPiece)worldDiffs.getTile(point.x,
                        point.y);

                int graphicsNumber = tp.getTrackGraphicID();

                int ruleNumber = tp.getTrackTypeID();
                jfreerails.client.renderer.TrackPieceRenderer trackPieceView = m_trackPieceViewList.getTrackPieceView(ruleNumber);
                trackPieceView.drawTrackPieceIcon(graphicsNumber, g, point.x,
                    point.y, m_tileSize);
            }

            //Draw small dots for each tile on the path.           
			for (Iterator<Point> iter = worldDiffs.getMapDifferences();
            iter.hasNext();){
                Point p = iter.next();
                int x = p.x * m_tileSize.width +
                    (m_tileSize.width - SMALL_DOT_WIDTH) / 2;
                int y = p.y * m_tileSize.width +
                    (m_tileSize.height - SMALL_DOT_WIDTH) / 2;
                g.setColor(Color.WHITE);
                g.fillOval(x, y, SMALL_DOT_WIDTH, SMALL_DOT_WIDTH);
            }
        }

    }

    /** Attempts to building track from the specifed point in the specified direction
     * on the worldDiff object.    
     */
    private MoveStatus planBuildingTrack(Point point, OneTileMoveVector vector) {
    	WorldDifferences worldDiffs = m_worldDiffs;
		FreerailsTile tileA = (FreerailsTile)worldDiffs.getTile(point.x, point.y);
    	BuildTrackStrategy bts = getBts();
		int trackTypeAID = bts.getRule(tileA.getTerrainTypeID());
        TrackRule trackRuleA = (TrackRule)worldDiffs.get(SKEY.TRACK_RULES,
        		trackTypeAID);
        
        FreerailsTile tileB = (FreerailsTile)worldDiffs.getTile(point.x + vector.deltaX, point.y+ vector.deltaY);
    	int trackTypeBID = bts.getRule(tileB.getTerrainTypeID());
        TrackRule trackRuleB = (TrackRule)worldDiffs.get(SKEY.TRACK_RULES,
        		trackTypeBID);
        
        
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(point,
                vector, trackRuleA, trackRuleB,worldDiffs, m_principal);

        return move.doMove(worldDiffs, m_principal);
    }

    /** Cancels any proposed track and resets the pathfinder.*/
    private void reset() {
    	m_worldDiffs.reset();
        m_trackPathFinder.abandonSearch();
        this.m_builtTrack.clear();
        this.m_isBuildTrackSuccessful = false;
    }
    
    /** Utility method the sets the CURSOR_MESSAGE property on the model root.*/
	private void setCursorMessage(String s) {
        m_modelRoot.setProperty(ModelRoot.Property.CURSOR_MESSAGE, s);
    }

	/** Sets the proposed track.*/
    public void setProposedTrack(Point startPoint, Point endPoint) {
        assert m_setUp;

        /*If we have just found the route between the two points, don't
         * waste time doing it again.
         */
        if (null != m_targetPoint && null != m_startPoint &&
        		m_targetPoint.equals(endPoint) &&
                m_startPoint.equals(startPoint) &&
                m_trackPathFinder.getStatus() != IncrementalPathFinder.SEARCH_NOT_STARTED) {
            return;
        }

        m_worldDiffs.reset();
        m_builtTrack.clear();

        if (startPoint.equals(endPoint)) {
            hide();

            return;
        }

        /* Check both points are on the map.*/
        if (!m_realWorld.boundsContain(startPoint.x, startPoint.y) ||
                !m_realWorld.boundsContain(endPoint.x, endPoint.y)) {
            hide();

            return;
        }

        setTargetPoint(new Point(endPoint));
        m_startPoint = new Point(startPoint);

        try {
          
			BuildTrackStrategy bts = getBts();
			m_trackPathFinder.setupSearch(startPoint, endPoint, bts);
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());

            return;
        }

        updateSearch();
    }

    /** Called when the model root has changed.*/
    public void setup(ModelRoot modelRoot) {
        if (m_setUp) {
            return;
        }

        m_setUp = true;
        this.m_modelRoot = modelRoot;
        m_principal = modelRoot.getPrincipal();
        setworldDiffs(m_worldDiffs);
    }

    /** Sets the m_show field to true.*/
    public void show() {
        this.setM_show(true);
    }

    /** Updates the search, if the search is completed, the proposed track is shown.*/
    private void updateSearch() {
        try {
            m_trackPathFinder.search(100);
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());

            return;
        }

        if (m_trackPathFinder.getStatus() == IncrementalPathFinder.PATH_FOUND) {
            m_builtTrack = m_trackPathFinder.pathAsPoints();
            moveCursorMoreTiles(m_builtTrack);
            show();
        }
    }

    /**
     * Saves track into real world
     */
    public Point updateWorld(TrackMoveProducer trackBuilder) {
        Point actPoint = getCursorPosition();

        if (m_builtTrack.size() > 0) {
            MoveStatus ms = moveCursorMoreTiles(m_builtTrack, trackBuilder);

            /* Note, reset() will have been called if ms.ok == false */
            if (ms.ok) {
                actPoint = m_builtTrack.get(m_builtTrack.size() - 1);
                m_builtTrack = new ArrayList<Point>();
            }
        }

        hide();

        return actPoint;
    }


	
	private void setworldDiffs(WorldDifferences worldDiffs) {
		m_modelRoot.setProperty(ModelRoot.Property.PROPOSED_TRACK, worldDiffs);		
	}

	
	private WorldDifferences getWorldDiffs() {
		if(m_modelRoot == null){
			return null;
		}
		return (WorldDifferences)m_modelRoot.getProperty(ModelRoot.Property.PROPOSED_TRACK);
	}


	
	private void setM_show(boolean show) {
		if(show == m_show){
			return;
		}
		if(show){
			setworldDiffs(m_worldDiffs);
		}else{
			setworldDiffs(null);
		}
		this.m_show = show;
	}


	/**
	 * @return Returns the m_show.
	 */
	private boolean isM_show() {
		return m_show;
	}


	/**
	 * @param newTargetPoint The m_targetPoint to set.
	 */
	private void setTargetPoint(Point newTargetPoint) {
		this.m_targetPoint = newTargetPoint;
		Point p = null == newTargetPoint ? null : new Point(newTargetPoint);
		m_modelRoot.setProperty(ModelRoot.Property.THINKING_POINT, p);		
	}


	
	public void update() {
		//update search for path if necessay.
        if (m_trackPathFinder.getStatus() == IncrementalPathFinder.SEARCH_PAUSED) {
            updateSearch();
        }		
	}
	
}
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
import jfreerails.controller.SimpleAStarPathFinder;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.TrackPathFinder;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldDifferences;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


/** This class draws the track being build.
 * @author MystiqueAgent
 * @author Luke
 * */
public class BuildTrackRenderer implements Painter {
    private static final int BIG_DOT_WIDTH = 12;
    private static final Logger LOGGER = Logger.getLogger(BuildTrackRenderer.class.getName());
    private static final int SMALL_DOT_WIDTH = 6;
    private List<Point> m_builtTrack = new ArrayList<Point>();
    private boolean m_isBuildTrackSuccessful = false;
    private ModelRoot m_modelRoot;
    private FreerailsPrincipal m_principal;
    private ReadOnlyWorld m_realWorld;
    private boolean m_setUp = false;
    private boolean m_show = false;
    private SoundManager m_soundManager = SoundManager.getSoundManager();
    private Point m_startPoint;
    private Point m_targetPoint;
    private final Dimension m_tileSize = new Dimension(30, 30);
    private TrackPathFinder m_trackPathFinder;
    private TrackPieceRendererList m_trackPieceViewList;
    private WorldDifferences m_worldDiffs;	

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

    private MoveStatus buildTrack(Point point, OneTileMoveVector vector) {
    	FreerailsTile tileA = (FreerailsTile)m_worldDiffs.getTile(point.x, point.y);
    	BuildTrackStrategy bts = getBts();
		int trackTypeAID = bts.getRule(tileA.getTerrainTypeID());
        TrackRule trackRuleA = (TrackRule)m_worldDiffs.get(SKEY.TRACK_RULES,
        		trackTypeAID);
        
        FreerailsTile tileB = (FreerailsTile)m_worldDiffs.getTile(point.x + vector.deltaX, point.y+ vector.deltaY);
    	int trackTypeBID = bts.getRule(tileB.getTerrainTypeID());
        TrackRule trackRuleB = (TrackRule)m_worldDiffs.get(SKEY.TRACK_RULES,
        		trackTypeBID);
        
        
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(point,
                vector, trackRuleA, trackRuleB,m_worldDiffs, m_principal);

        return move.doMove(m_worldDiffs, m_principal);
    }

    /**
     * return List of Point where the track should be built
     * @param startPoint Point
     * @param targetPoint Point
     * @return List
     */
    private List createProposedTrack(Point startPoint, Point targetPoint) {
        int x = startPoint.x;
        int y = startPoint.y;

        int deltaX = targetPoint.x - x;
        int deltaY = targetPoint.y - y;
        int aDeltaX = Math.abs(deltaX);
        int aDeltaY = Math.abs(deltaY);

        /*Build track! */

        /** @todo Replace this 'if' with longer track creation */
        int diagLen = Math.min(aDeltaX, aDeltaY);

        List proposedTrack = new ArrayList(Math.max(aDeltaX, aDeltaY) + 1);

        int dirX = (deltaX > 0 ? 1 : -1);
        int dirY = (deltaY > 0 ? 1 : -1);

        int actX = x;
        int actY = y;

        for (int diag = 0; diag < diagLen; diag++) {
            actX += dirX;
            actY += dirY;
            proposedTrack.add(new Point(actX, actY));
        }

        int diff = aDeltaX - aDeltaY;

        // if diff > 0 then we need to build some track in X direction
        for (int rest = 0; rest < diff; rest++) {
            actX += dirX;
            proposedTrack.add(new Point(actX, actY));
        }

        // if diff < 0 then we need to build some track in Y direction
        for (int rest = 0; rest > diff; rest--) {
            actY += dirY;
            proposedTrack.add(new Point(actX, actY));
        }

        return proposedTrack;
    }

	private BuildTrackStrategy getBts() {
		BuildTrackStrategy btss = (BuildTrackStrategy)m_modelRoot.getProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY);
		if(null == btss) throw new NullPointerException();
		return btss;
	}

    private Point getCursorPosition() {
        Point point = (Point)m_modelRoot.getProperty(ModelRoot.Property.CURSOR_POSITION);

        //Check for null & make a defensive copy
        point = null == point ? new Point() : new Point(point);

        if (!m_modelRoot.getWorld().boundsContain(point.x, point.y)) {
            throw new IllegalStateException(String.valueOf(point));
        }

        return point;
    }

//    private int getTrackRule() {
//        Integer trackType = (Integer)modelRoot.getProperty(ModelRoot.SELECTED_TRACK_TYPE);
//        int intValue = trackType.intValue();
//
//        return intValue;
//    }

    /** Hides and cancels any proposed track.*/ 
    public void hide() {
        this.m_show = false;
        m_targetPoint = null;
        reset();
    }

    /**
     * returns <code>true</code> if the track is being build - it is iff the build track is shown
     *
     * @return boolean
     */
    public boolean isBuilding() {
        return m_show;
    }

    /** Returns true if all the track pieces can be successfully built.*/
    public boolean isBuildTrackSuccessful() {
        return m_isBuildTrackSuccessful;
    }

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
            } else {
                piecesOfNewTrack++;
            }

            if (trackBuilder != null) {
                ms = trackBuilder.buildTrack(oldPosition, vector);
            } else {
                ms = buildTrack(oldPosition, vector);
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

    public void paint(Graphics2D g) {
        //update search for path if necessay.
        if (m_trackPathFinder.getStatus() == SimpleAStarPathFinder.SEARCH_PAUSED) {
            updateSearch();
        }

        if (m_show) {
            for (Iterator iter = m_worldDiffs.getMapDifferences();
                    iter.hasNext();) {
                Point point = (Point)iter.next();

                //                FreerailsTile tile = worldDifferences.getTile(point.x, point.y);
                //                paintRectangleOfTiles(g, new Rectangle(tileX, tileY, 1, 1));
                TrackPiece tp = (TrackPiece)m_worldDiffs.getTile(point.x,
                        point.y);

                int graphicsNumber = tp.getTrackGraphicID();

                int ruleNumber = tp.getTrackTypeID();
                jfreerails.client.renderer.TrackPieceRenderer trackPieceView = m_trackPieceViewList.getTrackPieceView(ruleNumber);
                trackPieceView.drawTrackPieceIcon(graphicsNumber, g, point.x,
                    point.y, m_tileSize);
            }

            //Draw while small dots for each tile on the path.
            for (Iterator<Point> iter = m_builtTrack.iterator(); iter.hasNext();) {
                Point p = iter.next();
                int x = p.x * m_tileSize.width +
                    (m_tileSize.width - SMALL_DOT_WIDTH) / 2;
                int y = p.y * m_tileSize.width +
                    (m_tileSize.height - SMALL_DOT_WIDTH) / 2;
                g.setColor(Color.WHITE);
                g.fillOval(x, y, SMALL_DOT_WIDTH, SMALL_DOT_WIDTH);
            }
        }

        //Draw a big white dot at the target point.
        if (null != m_targetPoint) {
            long time = System.currentTimeMillis();
            int dotSize;

            if ((time % 500) > 250) {
                dotSize = BIG_DOT_WIDTH;
            } else {
                dotSize = SMALL_DOT_WIDTH;
            }

            g.setColor(Color.WHITE);

            int x = m_targetPoint.x * m_tileSize.width +
                (m_tileSize.width - dotSize) / 2;
            int y = m_targetPoint.y * m_tileSize.width +
                (m_tileSize.height - dotSize) / 2;
            g.fillOval(x, y, dotSize, dotSize);
        }
    }

    private void reset() {
        m_worldDiffs.reset();
        m_trackPathFinder.abandonSearch();
        this.m_builtTrack.clear();
        this.m_isBuildTrackSuccessful = false;
    }

	private void setBts(BuildTrackStrategy bts) {
		m_modelRoot.setProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY, bts);		
	}

    private void setCursorMessage(String s) {
        m_modelRoot.setProperty(ModelRoot.Property.CURSOR_MESSAGE, s);
    }

    private void setCursorPosition(Point p) {
        //Make a defensive copy.
        Point point = new Point(p);
        m_modelRoot.setProperty(ModelRoot.Property.CURSOR_POSITION, point);
    }

    public void setTrack(Point startPoint, Point endPoint) {
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

        m_targetPoint = new Point(endPoint);
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

    public void setup(ModelRoot modelRoot) {
        if (m_setUp) {
            return;
        }

        m_setUp = true;
        this.m_modelRoot = modelRoot;
        m_principal = modelRoot.getPrincipal();
    }

    public void show() {
        this.m_show = true;
    }

    private void updateSearch() {
        try {
            m_trackPathFinder.search(100);
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());

            return;
        }

        if (m_trackPathFinder.getStatus() == SimpleAStarPathFinder.PATH_FOUND) {
            m_builtTrack = m_trackPathFinder.pathAsPoints();
            moveCursorMoreTiles(m_builtTrack);
            show();
        }
    }

    /**
     * saves track into real world
     */
    public Point updateWorld(TrackMoveProducer trackBuilder) {
        Point actPoint = getCursorPosition();

        if (m_builtTrack.size() > 0) {
            MoveStatus ms = moveCursorMoreTiles(m_builtTrack, trackBuilder);

            /* Note, reset() will have been called if ms.ok == false */
            if (ms.ok) {
                actPoint = (Point)m_builtTrack.get(m_builtTrack.size() - 1);
                m_builtTrack = new ArrayList();
            }
        }

        hide();

        return actPoint;
    }
}
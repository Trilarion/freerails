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
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.pathfinder.IncrementalPathFinder;
import jfreerails.controller.pathfinder.PathNotFoundException;
import jfreerails.controller.pathfinder.SimpleAStarPathFinder;
import jfreerails.controller.pathfinder.TrackPathFinder;
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
 * */
public class BuildTrackRenderer implements Painter {
    private static final int SMALL_DOT_WIDTH = 6;
    private static final int BIG_DOT_WIDTH = 12;
    private static final Logger logger = Logger.getLogger(BuildTrackRenderer.class.getName());
    private final Dimension tileSize = new Dimension(30, 30);
    private boolean show = false;
    private WorldDifferences worldDifferences;
    private TrackPieceRendererList trackPieceViewList;
    private ModelRoot modelRoot;
    private boolean settedUp = false;
    private List builtTrack = new ArrayList();
    private FreerailsPrincipal principal;
    private SoundManager soundManager = SoundManager.getSoundManager();
    private ReadOnlyWorld realWorld;
    private TrackPathFinder trackPathFinder;
    private boolean isBuildTrackSuccessful = false;
    private Point m_targetPoint;
    private Point m_startPoint;

    /** Returns true if all the track pieces can be successfully built.*/
    public boolean isBuildTrackSuccessful() {
        return isBuildTrackSuccessful;
    }

    /**
     * BuildTrackRenderer
     *
     * @param readOnlyWorld ReadOnlyWorld
     */
    public BuildTrackRenderer(ReadOnlyWorld readOnlyWorld,
        TrackPieceRendererList trackPieceViewList) {
        worldDifferences = new WorldDifferences(readOnlyWorld);
        this.trackPieceViewList = trackPieceViewList;
        realWorld = readOnlyWorld;
        trackPathFinder = new TrackPathFinder(readOnlyWorld);
    }

    public void setup(ModelRoot modelRoot) {
        if (settedUp) {
            return;
        }

        settedUp = true;
        this.modelRoot = modelRoot;
        principal = modelRoot.getPrincipal();
    }

    public void show() {
        this.show = true;
    }

    public void hide() {
        this.show = false;
        m_targetPoint = null;
        reset();
    }

    private void reset() {
        worldDifferences.reset();
        trackPathFinder.abandonSearch();
        this.builtTrack.clear();
        this.isBuildTrackSuccessful = false;
    }

    public void setTrack(Point startPoint, Point endPoint) {
        assert settedUp;

        /*If we have just found the route between the two points, don't
         * waste time doing it again.
         */
        if (null != m_targetPoint && null != m_startPoint &&
                m_targetPoint.equals(endPoint) &&
                m_startPoint.equals(startPoint) &&
                trackPathFinder.getStatus() != IncrementalPathFinder.SEARCH_NOT_STARTED) {
            return;
        }

        worldDifferences.reset();
        builtTrack.clear();

        if (startPoint.equals(endPoint)) {
            hide();

            return;
        }

        /* Check both points are on the map.*/
        if (!realWorld.boundsContain(startPoint.x, startPoint.y) ||
                !realWorld.boundsContain(endPoint.x, endPoint.y)) {
            hide();

            return;
        }

        m_targetPoint = new Point(endPoint);
        m_startPoint = new Point(startPoint);

        //builtTrack = createProposedTrack(startPoint, endPoint);
        try {
            trackPathFinder.setupSearch(startPoint, endPoint);
            //builtTrack = trackPathFinder.generatePath(startPoint, endPoint);
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());

            return;
        }

        updateSearch();
    }

    private void updateSearch() {
        try {
            trackPathFinder.search(100);
        } catch (PathNotFoundException e) {
            setCursorMessage(e.getMessage());

            return;
        }

        if (trackPathFinder.getStatus() == SimpleAStarPathFinder.PATH_FOUND) {
            builtTrack = trackPathFinder.retrievePath();
            moveCursorMoreTiles(builtTrack);
            show();
        }
    }

    public void paint(Graphics2D g) {
        //update search for path if necessay.
        if (trackPathFinder.getStatus() == SimpleAStarPathFinder.SEARCH_PAUSED) {
            updateSearch();
        }

        if (show) {
            for (Iterator iter = worldDifferences.getMapDifferences();
                    iter.hasNext();) {
                Point point = (Point)iter.next();

                //                FreerailsTile tile = worldDifferences.getTile(point.x, point.y);
                //                paintRectangleOfTiles(g, new Rectangle(tileX, tileY, 1, 1));
                TrackPiece tp = (TrackPiece)worldDifferences.getTile(point.x,
                        point.y);

                int graphicsNumber = tp.getTrackGraphicNumber();

                int ruleNumber = tp.getTrackRule().getRuleNumber();
                jfreerails.client.renderer.TrackPieceRenderer trackPieceView = trackPieceViewList.getTrackPieceView(ruleNumber);
                trackPieceView.drawTrackPieceIcon(graphicsNumber, g, point.x,
                    point.y, tileSize);
            }

            //Draw while small dots for each tile on the path.
            for (Iterator iter = builtTrack.iterator(); iter.hasNext();) {
                Point p = (Point)iter.next();
                int x = p.x * tileSize.width +
                    (tileSize.width - SMALL_DOT_WIDTH) / 2;
                int y = p.y * tileSize.width +
                    (tileSize.height - SMALL_DOT_WIDTH) / 2;
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

            int x = m_targetPoint.x * tileSize.width +
                (tileSize.width - dotSize) / 2;
            int y = m_targetPoint.y * tileSize.width +
                (tileSize.height - dotSize) / 2;
            g.fillOval(x, y, dotSize, dotSize);
        }
    }

    /**
     * saves track into real world
     */
    public Point updateWorld(TrackMoveProducer trackBuilder) {
        Point actPoint = getCursorPosition();

        if (builtTrack.size() > 0) {
            MoveStatus ms = moveCursorMoreTiles(builtTrack, trackBuilder);

            /* Note, reset() will have been called if ms.ok == false */
            if (ms.ok) {
                actPoint = (Point)builtTrack.get(builtTrack.size() - 1);
                builtTrack = new ArrayList();
            }
        }

        hide();

        return actPoint;
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

        for (Iterator iter = track.iterator(); iter.hasNext();) {
            Point point = (Point)iter.next();
            logger.fine("point" + point);
            logger.fine("oldPosition" + oldPosition);

            if (oldPosition.equals(point)) {
                logger.fine("(oldPosition.equals(point))" + point);

                continue;
            }

            OneTileMoveVector vector = OneTileMoveVector.getInstance(point.x -
                    oldPosition.x, point.y - oldPosition.y);

            //If there is already track between the two tiles, do nothing
            FreerailsTile tile = (FreerailsTile)realWorld.getTile(oldPosition.x,
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

        /* Check whether the is already track every point.*/
        if (piecesOfNewTrack == 0) {
            MoveStatus moveFailed = MoveStatus.moveFailed("Track already here");
            setCursorMessage(moveFailed.message);

            return moveFailed;
        }

        isBuildTrackSuccessful = true;

        //If track has actually been built, play the build track sound.
        if (trackBuilder != null && ms.isOk()) {
            if (trackBuilder.getTrackBuilderMode() == TrackMoveProducer.BUILD_TRACK) {
                this.soundManager.playSound("/jfreerails/client/sounds/buildtrack.wav",
                    0);
            }
        }

        return ms;
    }

    private MoveStatus buildTrack(Point point, OneTileMoveVector vector) {
        TrackRule trackRule = (TrackRule)worldDifferences.get(SKEY.TRACK_RULES,
                0);
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(point,
                vector, trackRule, worldDifferences, principal);

        return move.doMove(worldDifferences, principal);
    }

    /**
     * returns <code>true</code> if the track is being build - it is iff the build track is shown
     *
     * @return boolean
     */
    public boolean isBuilding() {
        return show;
    }

    private Point getCursorPosition() {
        Point point = (Point)modelRoot.getProperty(ModelRoot.CURSOR_POSITION);

        //Check for null & make a defensive copy
        point = null == point ? new Point() : new Point(point);

        if (!modelRoot.getWorld().boundsContain(point.x, point.y)) {
            throw new IllegalStateException(String.valueOf(point));
        }

        return point;
    }

    private void setCursorPosition(Point p) {
        //Make a defensive copy.
        Point point = new Point(p);
        modelRoot.setProperty(ModelRoot.CURSOR_POSITION, point);
    }

    private void setCursorMessage(String s) {
        modelRoot.setProperty(ModelRoot.CURSOR_MESSAGE, s);
    }
}
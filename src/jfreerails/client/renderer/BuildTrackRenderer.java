package jfreerails.client.renderer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.Painter;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldDifferences;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


/** This class draws the track being build.
 * @author MystiqueAgent
 * */
public class BuildTrackRenderer implements Painter {
    private final Dimension tileSize = new Dimension(30, 30);

    //    private int x;
    //    private int y;
    private boolean show = false;
    private WorldDifferences worldDifferences;
    private TrackPieceRendererList trackPieceViewList;

    //  private UserInputOnMapController owner;
    private ModelRoot modelRoot;
    private boolean settedUp = false;
    private List builtTrack;
    private FreerailsPrincipal principal;

    /**
     * BuildTrackRenderer
     *
     * @param readOnlyWorld ReadOnlyWorld
     */
    public BuildTrackRenderer(ReadOnlyWorld readOnlyWorld,
        TrackPieceRendererList trackPieceViewList) {
        worldDifferences = new WorldDifferences(readOnlyWorld);
        this.trackPieceViewList = trackPieceViewList;
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
        worldDifferences.reset();
    }

    public void setTrack(Point startPoint, Point endPoint) {
        assert settedUp;

        worldDifferences.reset();

        if (startPoint.equals(endPoint)) {
            hide();

            return;
        }

        builtTrack = createProposedTrack(startPoint, endPoint);
        moveCursorMoreTiles(builtTrack);
        show();
    }

    public void paint(Graphics2D g) {
        if (show) {
            for (Iterator iter = worldDifferences.getMapDifferences();
                    iter.hasNext();) {
                Point point = (Point)iter.next();

                //                FreerailsTile tile = worldDifferences.getTile(point.x, point.y);
                //                paintRectangleOfTiles(g, new Rectangle(tileX, tileY, 1, 1));
                TrackPiece tp = worldDifferences.getTile(point.x, point.y);

                int graphicsNumber = tp.getTrackGraphicNumber();

                int ruleNumber = tp.getTrackRule().getRuleNumber();
                jfreerails.client.renderer.TrackPieceRenderer trackPieceView = trackPieceViewList.getTrackPieceView(ruleNumber);
                trackPieceView.drawTrackPieceIcon(graphicsNumber, g, point.x,
                    point.y, tileSize);
            }
        }
    }

    /**
     * saves track into real world
     */
    public Point updateWorld(TrackMoveProducer trackBuilder) {
        Point actPoint = getCursorPosition();

        if (builtTrack != null) {
            moveCursorMoreTiles(builtTrack, trackBuilder);
            actPoint = (Point)builtTrack.get(builtTrack.size() - 1);
            builtTrack = null;
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
    private void moveCursorMoreTiles(List track, TrackMoveProducer trackBuilder) {
        Point oldPosition = getCursorPosition();

        for (Iterator iter = track.iterator(); iter.hasNext();) {
            Point point = (Point)iter.next();
            OneTileMoveVector vector = OneTileMoveVector.getInstance(point.x -
                    oldPosition.x, point.y - oldPosition.y);
            MoveStatus ms;

            if (trackBuilder != null) {
                ms = trackBuilder.buildTrack(oldPosition, vector);
            } else {
                ms = buildTrack(oldPosition, vector);
            }

            if (ms.ok) {
                setCursorMessage("");
            } else {
                setCursorMessage(ms.message);

                return;
            }

            oldPosition = point;
        }
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
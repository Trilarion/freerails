package jfreerails.client.top;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.FreerailsCursor;
import jfreerails.client.view.MapViewJComponent;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.OneTileMoveVector;
import java.util.List;
import java.util.*;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.BuildTrackRenderer;


/** Handles key presses and mouse movements on the map - responsible for moving the cursor etc.
 * @author Luke
 */
public class UserInputOnMapController extends KeyAdapter {
    private StationTypesPopup stationTypesPopup;
    private MapViewJComponent mapView;
    private TrackMoveProducer trackBuilder;
    private DialogueBoxController dialogueBoxController;
    private final ModelRoot modelRoot;
    private final MouseInputAdapter mouseInputAdapter = new CursorMouseAdapter();
    private BuildTrackRenderer buildTrack;

    public UserInputOnMapController(ModelRoot mr) {
        modelRoot = mr;
    }

    private class CursorMouseAdapter extends MouseInputAdapter {
        private int x;
        private int y;
        private boolean pressedInside = false;
        private List proposedTrack;

        public void mousePressed(MouseEvent evt) {
            if (SwingUtilities.isLeftMouseButton(evt)) {
                x = evt.getX();
                y = evt.getY();

                float scale = mapView.getScale();
                Dimension tileSize = new Dimension((int)scale, (int)scale);

                // only jump - no track building
                moveCursorJump(new Point(x / tileSize.width, y / tileSize.height));

                mapView.requestFocus();
                pressedInside = true;
                buildTrack.show();
            }
        }

        public void mouseDragged(MouseEvent evt) {
            //            System.err.println("mouseDragged()");
            if (SwingUtilities.isLeftMouseButton(evt) && pressedInside) {
                int x = evt.getX();
                int y = evt.getY();
                float scale = mapView.getScale();
                Dimension tileSize = new Dimension((int)scale, (int)scale);
                int tileX = x / tileSize.width;
                int tileY = y / tileSize.height;
                proposedTrack = createProposedTrack(new Point(tileX, tileY));
                buildTrack.setTrack(getCursorPosition(), proposedTrack);
                mapView.requestFocus();

                /** @todo  show created/show track but not send it to other players
                 * ??? How ???
                 */
            }
        }

        public void mouseReleased(MouseEvent evt) {
            //            System.err.println("mouseReleased()");
            if (SwingUtilities.isLeftMouseButton(evt)) {
                // build a railroad from x,y to current cursor position

                /** @todo build a track
                 *  1st version -> create oneTileMove
                 *  final version -> create multiTileMove to build longer track
                 */
                if (pressedInside && (null != proposedTrack)) {
                    moveCursorMoreTiles(proposedTrack);
                }

                pressedInside = false;
                proposedTrack = null;
                buildTrack.hide();
            }
        }
    }

    private void cursorOneTileMove(Point oldPosition, OneTileMoveVector vector) {
        if (null != trackBuilder) {
            MoveStatus ms = trackBuilder.buildTrack(oldPosition, vector);

            if (ms.ok) {
                setCursorMessage("");
            } else {
                setCursorMessage(ms.message);
            }

            Point tile = new Point();

            //            for (tile.x = oldPosition.x - 1; tile.x < oldPosition.x + 2;
            //                    tile.x++) {
            //                for (tile.y = oldPosition.y - 1; tile.y < oldPosition.y + 2;
            //                        tile.y++) {
            //                    mapView.refreshTile(tile.x, tile.y);
            //                }
            //            }
        } else {
            System.err.println("No track builder available!");
        }
    }

    public void setup(MapViewJComponent mv, TrackMoveProducer trackBuilder,
        StationTypesPopup stPopup, ModelRoot mr, DialogueBoxController dbc,
        FreerailsCursor cursor, BuildTrackRenderer buildTrack) {
        this.dialogueBoxController = dbc;
        this.mapView = mv;
        this.stationTypesPopup = stPopup;
        this.trackBuilder = trackBuilder;
        this.buildTrack = buildTrack;

        /* We attempt to remove listeners before adding them to
         * prevent them being added several times.
         */
        mapView.removeMouseListener(mouseInputAdapter);
        mapView.addMouseListener(mouseInputAdapter);
        mapView.removeMouseMotionListener(mouseInputAdapter);
        mapView.addMouseMotionListener(mouseInputAdapter);
        mapView.removeKeyListener(this);
        mapView.addKeyListener(this);
    }

    private void cursorJumped(Point to) {
        if (trackBuilder.getTrackBuilderMode() == TrackMoveProducer.UPGRADE_TRACK) {
            MoveStatus ms = trackBuilder.upgradeTrack(to);

            if (ms.ok) {
                setCursorMessage("");
            } else {
                setCursorMessage(ms.message);
            }
        }
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

    public void keyPressed(KeyEvent e) {
        Point cursorPosition = getCursorPosition();

        switch (e.getKeyCode()) {
        case KeyEvent.VK_NUMPAD1:
            moveCursorOneTile(OneTileMoveVector.SOUTH_WEST);

            break;

        case KeyEvent.VK_NUMPAD2:
            moveCursorOneTile(OneTileMoveVector.SOUTH);

            break;

        case KeyEvent.VK_NUMPAD3:
            moveCursorOneTile(OneTileMoveVector.SOUTH_EAST);

            break;

        case KeyEvent.VK_NUMPAD4:
            moveCursorOneTile(OneTileMoveVector.WEST);

            break;

        case KeyEvent.VK_NUMPAD6:
            moveCursorOneTile(OneTileMoveVector.EAST);

            break;

        case KeyEvent.VK_NUMPAD7:
            moveCursorOneTile(OneTileMoveVector.NORTH_WEST);

            break;

        case KeyEvent.VK_NUMPAD8:
            moveCursorOneTile(OneTileMoveVector.NORTH);

            break;

        case KeyEvent.VK_NUMPAD9:
            moveCursorOneTile(OneTileMoveVector.NORTH_EAST);

            break;

        case KeyEvent.VK_F8: {
            //Check whether we can built a station here before proceeding.
            if (stationTypesPopup.canBuiltStationHere(cursorPosition)) {
                float scale = mapView.getScale();
                Dimension tileSize = new Dimension((int)scale, (int)scale);
                int x = cursorPosition.x * tileSize.width;
                int y = cursorPosition.y * tileSize.height;
                stationTypesPopup.showMenu(mapView, x, y, cursorPosition);
            } else {
                modelRoot.setProperty(ModelRoot.QUICK_MESSAGE,
                    "Can't" + " build station here!");
            }

            break;
        }

        case KeyEvent.VK_BACK_SPACE:

            MoveStatus ms = trackBuilder.undoLastTrackMove();

            if (!ms.isOk()) {
                setCursorMessage(ms.message);
            }

            break;

        case KeyEvent.VK_I: {
            dialogueBoxController.showStationOrTerrainInfo(cursorPosition.x,
                cursorPosition.y);

            break;
        }

        case KeyEvent.VK_C: {
            mapView.centerOnTile(cursorPosition);

            break;
        }
        }
    }

    private List createProposedTrack(Point tryThisPoint) {
        Point oldCursorMapPosition = getCursorPosition();

        int deltaX = tryThisPoint.x - oldCursorMapPosition.x;
        int deltaY = tryThisPoint.y - oldCursorMapPosition.y;
        int aDeltaX = Math.abs(deltaX);
        int aDeltaY = Math.abs(deltaY);

        /*Build track! */

        /** @todo Replace this 'if' with longer track creation */
        int diagLen = Math.min(aDeltaX, aDeltaY);

        List proposedTrack = new ArrayList(Math.max(aDeltaX, aDeltaY));

        int dirX = (deltaX > 0 ? 1 : -1);
        int dirY = (deltaY > 0 ? 1 : -1);

        for (int diag = 0; diag < diagLen; diag++) {
            OneTileMoveVector vector = OneTileMoveVector.getInstance(dirX, dirY);
            proposedTrack.add(vector);
        }

        int diff = aDeltaX - aDeltaY;

        // if diff > 0 then we need to build some track in X direction
        for (int rest = 0; rest < diff; rest++) {
            OneTileMoveVector vector = OneTileMoveVector.getInstance(dirX, 0);
            proposedTrack.add(vector);
        }

        // if diff < 0 then we need to build some track in Y direction
        for (int rest = 0; rest > diff; rest--) {
            OneTileMoveVector vector = OneTileMoveVector.getInstance(0, dirY);
            proposedTrack.add(vector);
        }

        return proposedTrack;
    }

    private void moveCursorMoreTiles(List track) {
        for (Iterator iter = track.iterator(); iter.hasNext();) {
            OneTileMoveVector vector = (OneTileMoveVector)iter.next();
            moveCursorOneTile(vector);
        }
    }

    private void moveCursorJump(Point tryThisPoint) {
        setCursorMessage("");

        if (legalRectangleContains(tryThisPoint)) {
            setCursorPosition(tryThisPoint);
            cursorJumped(tryThisPoint);
        } else {
            this.setCursorMessage("Illegal cursor position!");
        }
    }

    /**
     * Checks whether specified point is in legal rectangle.
     * @param tryThisPoint Point
     * @return boolean
     */
    private boolean legalRectangleContains(Point tryThisPoint) {
        float tileSize = mapView.getScale();
        Dimension mapSizeInPixels = mapView.getMapSizeInPixels();
        int maxX = (int)(mapSizeInPixels.width / tileSize) - 2;
        int maxY = (int)(mapSizeInPixels.height / tileSize) - 2;
        Rectangle legalRectangle; //The set of legal cursor positions.
        legalRectangle = new Rectangle(1, 1, maxX, maxY);

        return legalRectangle.contains(tryThisPoint);
    }

    private void moveCursorOneTile(OneTileMoveVector v) {
        setCursorMessage(null);

        Point cursorMapPosition = this.getCursorPosition();
        Point tryThisPoint = new Point(cursorMapPosition.x + v.getDx(),
                cursorMapPosition.y + v.getDy());

        /*Move the cursor. */
        if (legalRectangleContains(tryThisPoint)) {
            setCursorPosition(tryThisPoint);
            cursorOneTileMove(cursorMapPosition, v);
        } else {
            this.setCursorMessage("Illegal cursor position!");
        }
    }
}
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
import jfreerails.client.view.ModelRoot;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.OneTileMoveVector;


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

    public UserInputOnMapController(ModelRoot mr) {
        modelRoot = mr;
    }

    private class CursorMouseAdapter extends MouseInputAdapter {
        public void mousePressed(MouseEvent evt) {
            if (SwingUtilities.isLeftMouseButton(evt)) {
                int x = evt.getX();
                int y = evt.getY();
                float scale = mapView.getScale();
                Dimension tileSize = new Dimension((int)scale, (int)scale);
                tryMoveCursor(new Point(x / tileSize.width, y / tileSize.height));
                mapView.requestFocus();
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

            for (tile.x = oldPosition.x - 1; tile.x < oldPosition.x + 2;
                    tile.x++) {
                for (tile.y = oldPosition.y - 1; tile.y < oldPosition.y + 2;
                        tile.y++) {
                    mapView.refreshTile(tile.x, tile.y);
                }
            }
        } else {
            System.err.println("No track builder available!");
        }
    }

    public void setup(MapViewJComponent mv, TrackMoveProducer trackBuilder,
        StationTypesPopup stPopup, ModelRoot mr, DialogueBoxController dbc,
        FreerailsCursor cursor) {
        this.dialogueBoxController = dbc;
        this.mapView = mv;
        this.stationTypesPopup = stPopup;
        this.trackBuilder = trackBuilder;

        /* We attempt to remove listeners before adding them to
         * prevent them being added several times.
         */
        mapView.removeMouseListener(mouseInputAdapter);
        mapView.addMouseListener(mouseInputAdapter);
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
            moveCursor(OneTileMoveVector.SOUTH_WEST);

            break;

        case KeyEvent.VK_NUMPAD2:
            moveCursor(OneTileMoveVector.SOUTH);

            break;

        case KeyEvent.VK_NUMPAD3:
            moveCursor(OneTileMoveVector.SOUTH_EAST);

            break;

        case KeyEvent.VK_NUMPAD4:
            moveCursor(OneTileMoveVector.WEST);

            break;

        case KeyEvent.VK_NUMPAD6:
            moveCursor(OneTileMoveVector.EAST);

            break;

        case KeyEvent.VK_NUMPAD7:
            moveCursor(OneTileMoveVector.NORTH_WEST);

            break;

        case KeyEvent.VK_NUMPAD8:
            moveCursor(OneTileMoveVector.NORTH);

            break;

        case KeyEvent.VK_NUMPAD9:
            moveCursor(OneTileMoveVector.NORTH_EAST);

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

    private void tryMoveCursor(Point tryThisPoint) {
        setCursorMessage(null);

        Point oldCursorMapPosition = getCursorPosition();
        float tileSize = mapView.getScale();
        Dimension mapSizeInPixels = mapView.getMapSizeInPixels();
        int maxX = (int)(mapSizeInPixels.width / tileSize) - 2;
        int maxY = (int)(mapSizeInPixels.height / tileSize) - 2;
        Rectangle legalRectangle; //The set of legal cursor positions.
        legalRectangle = new Rectangle(1, 1, maxX, maxY);

        if (legalRectangle.contains(tryThisPoint)) {
            /*Move the cursor. */
            setCursorPosition(tryThisPoint);

            int deltaX = tryThisPoint.x - oldCursorMapPosition.x;
            int deltaY = tryThisPoint.y - oldCursorMapPosition.y;

            /*Build track! */
            if (OneTileMoveVector.checkValidity(deltaX, deltaY)) {
                OneTileMoveVector vector = OneTileMoveVector.getInstance(deltaX,
                        deltaY);
                this.cursorOneTileMove(oldCursorMapPosition, vector);
            } else {
                cursorJumped(tryThisPoint);
            }
        } else {
            this.setCursorMessage("Illegal cursor position!");
        }
    }

    private void moveCursor(OneTileMoveVector v) {
        Point cursorMapPosition = this.getCursorPosition();
        tryMoveCursor(new Point(cursorMapPosition.x + v.getDx(),
                cursorMapPosition.y + v.getDy()));
    }
}
package jfreerails.client.top;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import jfreerails.client.model.CursorEvent;
import jfreerails.client.model.CursorEventListener;
import jfreerails.client.model.MapCursor;
import jfreerails.client.model.ModelRoot;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.MapViewJComponent;
import jfreerails.client.view.GUIRoot;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.UncommittedMoveReceiver;
import jfreerails.move.MoveStatus;


public class UserInputOnMapController implements CursorEventListener {
    private StationTypesPopup stationTypesPopup;
    private MapViewJComponent mapView;
    private TrackMoveProducer trackBuilder;
    private MapCursor cursor;
    private DialogueBoxController dialogueBoxController;
    private UncommittedMoveReceiver trackMoveExecutor;
    private ModelRoot modelRoot;

    public UserInputOnMapController(ModelRoot mr) {
        modelRoot = mr;
    }

    public void cursorOneTileMove(CursorEvent ce) {
        if (null != trackBuilder) {
            MoveStatus ms = trackBuilder.buildTrack(ce.oldPosition, ce.vector);

            if (ms.ok) {
                cursor.setMessage("");
            } else {
                cursor.setMessage(ms.message);
            }

            Point tile = new Point();

            for (tile.x = ce.oldPosition.x - 1; tile.x < ce.oldPosition.x + 2;
                    tile.x++) {
                for (tile.y = ce.oldPosition.y - 1;
                        tile.y < ce.oldPosition.y + 2; tile.y++) {
                    mapView.refreshTile(tile.x, tile.y);
                }
            }
        } else {
            System.err.println("No track builder available!");
        }
    }

    public void setup(GUIRoot gr,
	    MapViewJComponent mv, StationTypesPopup stp) {
        trackMoveExecutor = modelRoot.getReceiver();
        this.dialogueBoxController = gr.getDialogueBoxController();
        this.mapView = mv;

        this.trackBuilder = modelRoot.getTrackMoveProducer();

        this.cursor = modelRoot.getCursor();

        cursor.addCursorEventListener(this);
	stationTypesPopup = stp;
    }

    public void cursorJumped(CursorEvent ce) {
        if (trackBuilder.getTrackBuilderMode() == TrackMoveProducer.UPGRADE_TRACK) {
            MoveStatus ms = trackBuilder.upgradeTrack(ce.newPosition);

            if (ms.ok) {
                cursor.setMessage("");
            } else {
                cursor.setMessage(ms.message);
            }
        }
    }

    public void cursorKeyPressed(CursorEvent ce) {
        switch (ce.keyEvent.getKeyCode()) {
        case KeyEvent.VK_F8: {
            //defensive copy.
            Point tile = new Point(ce.newPosition);

            //Check whether we can built a station here before proceeding.
            if (stationTypesPopup.canBuiltStationHere(tile)) {
                float scale = mapView.getScale();
                Dimension tileSize = new Dimension((int)scale, (int)scale);
                int x = tile.x * tileSize.width;
                int y = tile.y * tileSize.height;
                stationTypesPopup.showMenu(mapView, x, y, tile);
            } else {
                modelRoot.getUserMessageLogger().println("Can't" +
                    " build station here!");
            }

            break;
        }

        case KeyEvent.VK_BACK_SPACE:
            trackMoveExecutor.undoLastMove();

            break;

        case KeyEvent.VK_I: {
            dialogueBoxController.showStationOrTerrainInfo(ce.newPosition.x,
                ce.newPosition.y);

            break;
        }

        case KeyEvent.VK_C: {
            mapView.centerOnTile(new Point(ce.newPosition.x, ce.newPosition.y));

            break;
        }
        }
    }
}

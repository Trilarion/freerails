package jfreerails.client.top;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;

import jfreerails.client.view.CursorEvent;
import jfreerails.client.view.CursorEventListener;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.FreerailsCursor;
import jfreerails.client.view.MapViewJComponent;
import jfreerails.controller.TrackMoveProducer;

public class UserInputOnMapController implements CursorEventListener {

	private StationTypesPopup stationTypesPopup;

	private MapViewJComponent mapView;

	private TrackMoveProducer trackBuilder;

	private FreerailsCursor cursor;

	private DialogueBoxController dialogueBoxController;

	
	public void cursorOneTileMove(CursorEvent ce) {
		if (null != trackBuilder) {

			trackBuilder.buildTrack(ce.oldPosition, ce.vector);
			Point tile = new Point();
			for (tile.x = ce.oldPosition.x - 1; tile.x < ce.oldPosition.x + 2; tile.x++) {
				for (tile.y = ce.oldPosition.y - 1; tile.y < ce.oldPosition.y + 2; tile.y++) {
					mapView.refreshTile(tile.x, tile.y);
				}
			}

		} else {
			System.out.println("No track builder available!");
		}
	}

	public void setup(
		MapViewJComponent mv,
		TrackMoveProducer trackBuilder,
		StationTypesPopup stPopup,
		FreerailsCursor fc,
		DialogueBoxController dbc) {
		this.dialogueBoxController = dbc;
		this.mapView = mv;
		this.stationTypesPopup = stPopup;

		this.trackBuilder = trackBuilder;

		this.cursor = fc;

		cursor.addCursorEventListener(this);

	}

	
	public void cursorJumped(CursorEvent ce) {
		trackBuilder.upgradeTrack(ce.newPosition);
	}

	
	public void cursorKeyPressed(CursorEvent ce) {

		switch (ce.keyEvent.getKeyCode()) {
			case KeyEvent.VK_F7 :
				{
					buildTrain(ce);
					break;
				}
			case KeyEvent.VK_F8 :
				{
					
					float scale = mapView.getScale();
					Point tile = new Point(ce.newPosition); //defensive copy.
					Dimension tileSize = new Dimension((int) scale, (int) scale);
					int x = tile.x * tileSize.width;
					int y = tile.y * tileSize.height;
					stationTypesPopup.show(mapView, x, y, tile);
					break;

				}
                    case KeyEvent.VK_I :
                    {
                        System.out.println("Show terrain info");                        
                        dialogueBoxController.showTerrainInfo(ce.newPosition.x, ce.newPosition.y);
                        break;
                    }			
		}
	}

	private void buildTrain(CursorEvent ce) {
		
		dialogueBoxController.showSelectEngine();
		//trainBuilder.buildTrain(ce.newPosition);
	}

}

package jfreerails.client.top;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;

import jfreerails.client.event.CursorEvent;
import jfreerails.client.event.CursorEventListener;
import jfreerails.client.menu.StationTypesPopup;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.FreerailsCursor;
import jfreerails.client.view.MapViewJComponent;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.pathfinder.TrainPathFinder;

public class UserInputOnMapController implements CursorEventListener {

	
	private StationTypesPopup stationTypesPopup;

	private MapViewJComponent mapView;

	private TrackMoveProducer trackBuilder;
	
	private FreerailsCursor cursor;
	
	private DialogueBoxController dialogueBoxController;


	/**
	 * @see jfreerails.client.event.CursorEventListener#cursorOneTileMove(jfreerails.client.event.CursorEvent)
	 */
	public void cursorOneTileMove(CursorEvent ce) {
		if (null != trackBuilder) {

			trackBuilder.buildTrack(ce.oldPosition, ce.vector);
			Point tile = new Point();
			for (tile.x = ce.oldPosition.x - 1;
				tile.x < ce.oldPosition.x + 2;
				tile.x++) {
				for (tile.y = ce.oldPosition.y - 1;
					tile.y < ce.oldPosition.y + 2;
					tile.y++) {
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
			
			StationTypesPopup stPopup, FreerailsCursor fc,
			DialogueBoxController dbc){
			this.dialogueBoxController=dbc;
			this.mapView = mv;			
			this.stationTypesPopup = stPopup;
			
			this.trackBuilder = trackBuilder;
		

			this.cursor = fc;
			
			cursor.addCursorEventListener(this);
			
		}

	/**
	 * @see jfreerails.client.event.CursorEventListener#cursorJumped(jfreerails.client.event.CursorEvent)
	 */
	public void cursorJumped(CursorEvent ce) {
		trackBuilder.upgradeTrack(ce.newPosition);
	}

	/**
	 * @see jfreerails.client.event.CursorEventListener#cursorKeyPressed(jfreerails.client.event.CursorEvent)
	 */
	public void cursorKeyPressed(CursorEvent ce) {
		if (ce.keyEvent.getKeyCode() == KeyEvent.VK_F7) {
			buildTrain(ce);
		} else if (ce.keyEvent.getKeyCode() == KeyEvent.VK_F8) {
			System.out.println("Build station");
			float scale = mapView.getScale();
			Point tile = new Point(ce.newPosition); //defensive copy.
			Dimension tileSize = new Dimension((int) scale, (int) scale);
			int x = tile.x * tileSize.width;
			int y = tile.y * tileSize.height;
			stationTypesPopup.show(mapView, x, y, tile);
			//stationBuilder.buildStation(ce.newPosition);
			//repaintMap(ce);
		} else if (ce.keyEvent.getKeyCode() == KeyEvent.VK_T) {

			TrainPathFinder.setTarget(ce.newPosition.x, ce.newPosition.y);
			System.out.println(
				"The target for the train pathfinder is now: "
					+ ce.newPosition.x
					+ ", "
					+ ce.newPosition.y);

		}
	}

	private void buildTrain(CursorEvent ce) {
		System.out.println("Build train");
		dialogueBoxController.showSelectEngine();
		//trainBuilder.buildTrain(ce.newPosition);
	}


}

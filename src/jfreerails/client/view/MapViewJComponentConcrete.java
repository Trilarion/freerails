
/*
 * MapViewJComponent.java
 *
 * Created on 31 July 2001, 13:56
 */
package jfreerails.client.view;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;
import jfreerails.client.event.CursorEvent;
import jfreerails.client.event.CursorEventListener;
import jfreerails.client.menu.StationTypesPopup;
import jfreerails.controller.TrackMoveProducer;
import jfreerails.controller.TrainBuilder;
import jfreerails.misc.TrainPathFinder;

/**
 *
 * @author  Luke Lindsay
 * @version
 */

final public class MapViewJComponentConcrete
	extends MapViewJComponent
	implements CursorEventListener {

	private TrackMoveProducer trackBuilder;

	private TrainBuilder trainBuilder;

	private StationTypesPopup stationTypesPopup;

	private FreerailsCursor cursor;

	final private class MapViewJComponentMouseAdapter
		extends java.awt.event.MouseAdapter {

		public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
				int x = mouseEvent.getX();
				int y = mouseEvent.getY();
				float scale = mapView.getScale();
				Dimension tileSize = new Dimension((int) scale, (int) scale);
				cursor.TryMoveCursor(
					new java.awt.Point(
						x / tileSize.width,
						y / tileSize.height));
				MapViewJComponentConcrete.this.requestFocus();
			}
		}
	}

	protected void paintComponent(java.awt.Graphics g) {
		super.paintComponent(g);
		java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;

		java.awt.Rectangle r = this.getVisibleRect();

		mapView.paintRect(g2, r);
		if (null != cursor) {
			cursor.cursorRenderer.paintCursor(
				g2,
				new java.awt.Dimension(30, 30));
		}
	}

	public MapViewJComponentConcrete() {
		super();
		this.addMouseListener(new MapViewJComponentMouseAdapter());
	}

	public void setup(
		MapView mv,
		TrackMoveProducer trackBuilder,
		TrainBuilder tb,
		StationTypesPopup stPopup) {
		super.mapView = mv;
		this.trainBuilder = tb;
		this.stationTypesPopup = stPopup;
		this.setBorder(null);
		this.trackBuilder = trackBuilder;

		this.removeKeyListener(this.cursor);

		this.cursor = new FreerailsCursor(mv);
		cursor.addCursorEventListener(this);

		this.addKeyListener(cursor);
	}

	public void setup(MapView mv) {
		super.mapView = mv;
	}

	public void cursorJumped(CursorEvent ce) {

		trackBuilder.upgradeTrack(ce.newPosition);
		//repaintMap(ce);

		reactToCursorMovement(ce);

	}
	/* The map is repainted in reponse to moves being received
	 using the class MapViewMoveReceiver.
	
	public void repaintMap(CursorEvent ce) {

		Point tile = new Point();
		for (tile.x = ce.newPosition.x - 1;
			tile.x < ce.newPosition.x + 2;
			tile.x++) {
			for (tile.y = ce.newPosition.y - 1;
				tile.y < ce.newPosition.y + 2;
				tile.y++) {
				mapView.refreshTile(tile.x, tile.y);
			}
		}
	}
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
		reactToCursorMovement(ce);
	}

	public void cursorKeyPressed(CursorEvent ce) {
		if (ce.keyEvent.getKeyCode() == KeyEvent.VK_F7) {
			System.out.println("Build train");
			trainBuilder.buildTrain(ce.newPosition);
		} else if (ce.keyEvent.getKeyCode() == KeyEvent.VK_F8) {
			System.out.println("Build station");
			float scale = mapView.getScale();
			Point tile = new Point(ce.newPosition);	//defensive copy.
			Dimension tileSize = new Dimension((int) scale, (int) scale);
			int x =tile.x*tileSize.width;
			int y =tile.y*tileSize.height;
			stationTypesPopup.show(this, x,y, tile);
			//stationBuilder.buildStation(ce.newPosition);
			//repaintMap(ce);
		} else if (ce.keyEvent.getKeyCode() == KeyEvent.VK_T){
		
			TrainPathFinder.setTarget(ce.newPosition.x, ce.newPosition.y);
			System.out.println("The target for the train pathfinder is now: "+ce.newPosition.x+", "+ce.newPosition.y);
				
			
		}
		reactToCursorMovement(ce);
	}

	private void reactToCursorMovement(CursorEvent ce) {
		float scale = mapView.getScale();
		Dimension tileSize = new Dimension((int) scale, (int) scale);
		Rectangle vr = this.getVisibleRect();
		Rectangle rectangleSurroundingCursor = new Rectangle(0, 0, 1, 1);
		rectangleSurroundingCursor.setLocation(
			(ce.newPosition.x - 1) * tileSize.width,
			(ce.newPosition.y - 1) * tileSize.height);
		rectangleSurroundingCursor.setSize(
			tileSize.width * 3,
			tileSize.height * 3);
		if (!(vr.contains(rectangleSurroundingCursor))) {
			int x = ce.newPosition.x * tileSize.width - vr.width / 2;
			int y = ce.newPosition.y * tileSize.height - vr.height / 2;
			this.scrollRectToVisible(new Rectangle(x, y, vr.width, vr.height));
		}
		this.repaint(
			(ce.newPosition.x - 1) * tileSize.width,
			(ce.newPosition.y - 1) * tileSize.height,
			tileSize.width * 3,
			tileSize.height * 3);
		this.repaint(
			(ce.oldPosition.x - 1) * tileSize.width,
			(ce.oldPosition.y - 1) * tileSize.height,
			tileSize.width * 3,
			tileSize.height * 3);
	}

	public float getScale() {
		return mapView.getScale();

	}

	public void paintTile(Graphics g, int tileX, int tileY) {
	}

	public void paintRectangleOfTiles(
		Graphics g,
		int x,
		int y,
		int width,
		int height) {
	}

	public void refreshTile(int x, int y) {
	}

	public void refreshRectangleOfTiles(int x, int y, int width, int height) {
	}

	public void paintRect(Graphics g, Rectangle visibleRect) {
	}

}

/*
 * MapViewJComponent.java
 *
 * Created on 31 July 2001, 13:56
 */
package jfreerails.client.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.SwingUtilities;

import jfreerails.client.event.CursorEvent;
import jfreerails.client.event.CursorEventListener;
import jfreerails.client.renderer.MapRenderer;

/**
 *
 * @author  Luke Lindsay
 * 
 */

final public class MapViewJComponentConcrete
	extends MapViewJComponent
	implements CursorEventListener {

	//private TrackMoveProducer trackBuilder;

	//private TrainBuilder trainBuilder;

	//private StationTypesPopup stationTypesPopup;

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
		MapRenderer mv,
		FreerailsCursor fc
		) {
		super.mapView = mv;
		
		this.setBorder(null);
		
		this.removeKeyListener(this.cursor);

		this.cursor = fc;
		//this.cursor = new FreerailsCursor(mv);
		cursor.addCursorEventListener(this);

		this.addKeyListener(cursor);
	}

	public void setup(MapRenderer mv) {
		super.mapView = mv;
	}

	public void cursorJumped(CursorEvent ce) {

		
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
		
		reactToCursorMovement(ce);
	}

	public void cursorKeyPressed(CursorEvent ce) {
		
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
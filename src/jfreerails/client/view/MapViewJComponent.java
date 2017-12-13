
/*
 *  MapViewJComponent.java
 *
 *  Created on 06 August 2001, 14:12
 */
package jfreerails.client.view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import jfreerails.client.common.UserMessageLogger;
import jfreerails.client.renderer.BlankMapRenderer;
import jfreerails.client.renderer.MapRenderer;

/**
 *@author     Luke Lindsay
 *     01 November 2001
 */

public abstract class MapViewJComponent
	extends JPanel
	implements Scrollable, MapRenderer, UserMessageLogger {
	
	private MapRenderer mapView=new BlankMapRenderer(10);

	public MapViewJComponent() {
	}

	public float getScale() {
		return getMapView().getScale();

	}

	protected void paintComponent(java.awt.Graphics g) {
		java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
		java.awt.Rectangle r = this.getVisibleRect();
		getMapView().paintRect(g2, r);
	}

	public int getScrollableUnitIncrement(
		java.awt.Rectangle rectangle,
		int orientation,
		int direction) {

		return (int) getMapView().getScale();

	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableBlockIncrement(
		java.awt.Rectangle rectangle,
		int orientation,
		int direction) {
		if (javax.swing.SwingConstants.VERTICAL == orientation) {
			int best =
				(int) (((rectangle.height / getMapView().getScale()) - 2)
					* getMapView().getScale());
			if (best > 0) {
				return best;
			} else {
				return rectangle.height;
			}
		} else {
			int best =
				(int) (((rectangle.width / getMapView().getScale()) - 2)
					* getMapView().getScale());
			if (best > 0) {
				return best;
			} else {
				return rectangle.width;
			}
		}
	}

	/**
	 *  Gets the scrollableTracksViewportHeight attribute of the
	 *  MapViewJComponent object
	 *
	 *@return    The scrollableTracksViewportHeight value
	 */
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/**
	 *  Gets the preferredScrollableViewportSize attribute of the
	 *  MapViewJComponent object
	 *
	 *@return    The preferredScrollableViewportSize value
	 */
	public java.awt.Dimension getPreferredScrollableViewportSize() {
		return this.getPreferredSize();
	}
	public void centerOnTile(Point tile) {

		float scale = getMapView().getScale();
		Rectangle visRect = new Rectangle(this.getVisibleRect());
		visRect.x = (int) (tile.x * scale - (visRect.width / 2));
		visRect.y = (int) (tile.y * scale - (visRect.height / 2));
		this.scrollRectToVisible(visRect);

	}
	
	public Dimension getMapSizeInPixels() {
		return getMapView().getMapSizeInPixels();
	}
	public Dimension getPreferredSize(){
		return getMapSizeInPixels();
	}

	protected void setMapView(MapRenderer mapView) {
		this.mapView = mapView;
	}

	protected MapRenderer getMapView() {
		return mapView;
	}

	/**
	 * Override the default for JPanel.
	 */
	public boolean isOpaque() {
	    return false;
	}

	public boolean isOptimizedDrawingEnabled() {
	    return true;
	}
}

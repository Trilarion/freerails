/*
 * MainMapAndOverviewMapMediator.java
 *
 * Created on 24 June 2002, 21:04
 */

package jfreerails.client.view;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputAdapter;

/** This class mediates between the main map view and the overview
 * map view.  It does the following:<br>
 * (1) Updates the rectangle on the overview map when the visible
 * rectangle of the main map changes.<br>
 * (2) Updates the main map visible rectangle when the user clicks
 * on the overview map.<br>
 * (3) Updates the main map visible rectangle when the user drags
 * the rectangle on the overview map.<br>
 * (4) Changes the mouse cursor to indicate that the rectangle on
 * the overview map is draggable when the mouse moves into the
 * rectangle.
 *
 * @author Luke Lindsay
 * @version 1.0
 */

public class MainMapAndOverviewMapMediator extends MouseInputAdapter {

	private  JComponent overviewMapJPanel;
	private  JViewport viewport;
	private  JComponent mainMap;
	private  Rectangle currentVisRect;

	private Point lastMouseLocation = new Point();

	boolean inside = false;
	boolean draggingAndStartedInside = false;

    public MainMapAndOverviewMapMediator(){

    }

	public MainMapAndOverviewMapMediator(
		JComponent omv,
		JViewport v,
		JComponent mm,
		Rectangle rect) {
        setup( omv,
		 v,
		 mm,
		 rect);

    }

    public void setup(
		JComponent omv,
		JViewport v,
		JComponent mm,
		Rectangle rect) {
        currentVisRect = rect;

        overviewMapJPanel = omv;
        viewport = v;
        mainMap = mm;

        overviewMapJPanel.addMouseMotionListener(this);
        overviewMapJPanel.addMouseListener(this);
        viewport.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateObservedRect();
            }
        });

        overviewMapJPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
    public void componentResized(java.awt.event.ComponentEvent evt) {
    updateObservedRect();
    }
    public void componentShown(java.awt.event.ComponentEvent evt) {
    updateObservedRect();
    }
    });
    }

    public void mouseMoved(MouseEvent evt) {

		lastMouseLocation.x = evt.getX();
		lastMouseLocation.y = evt.getY();
		updateInside(evt);
	}

	public void mousePressed(MouseEvent evt) {
		if (inside) {
			draggingAndStartedInside = true;
		}
	}

	public void mouseReleased(MouseEvent evt) {
		draggingAndStartedInside = false;
	}

	public void mouseDragged(MouseEvent evt) {
		if (draggingAndStartedInside) {
			/*
			Rectangle r= overviewMapJPanel.mainMapVisibleRect;
			r.x+=evt.getX()-lastMouseLocation.x;
			r.y+=evt.getY()-lastMouseLocation.y;
			lastMouseLocation.x=evt.getX();
			lastMouseLocation.y=evt.getY();
			 
			updateInside(evt);
			overviewMapJPanel.repaint();
			 */

			int deltaX = evt.getX() - lastMouseLocation.x;
			int deltaY = evt.getY() - lastMouseLocation.y;
			lastMouseLocation.x = evt.getX();
			lastMouseLocation.y = evt.getY();

			//float overviewScale=overviewMapJPanel.getScale();
			//float mainMapScale=mainMap.getScale();
			int overviewScale = overviewMapJPanel.getPreferredSize().width;
			int mainMapScale = mainMap.getWidth();

			int scaledDeltaX = (int) (deltaX * mainMapScale / overviewScale);
			int scaledDeltaY = (int) (deltaY * mainMapScale / overviewScale);

			Rectangle r = mainMap.getVisibleRect();
			r.x += scaledDeltaX;
			r.y += scaledDeltaY;

			mainMap.scrollRectToVisible(r);
			updateInside(evt);
		}

	}

	public void mouseClicked(MouseEvent evt) {
		/*
		Rectangle r= overviewMapJPanel.mainMapVisibleRect;
		r.x=evt.getX()-r.width/2;
		r.y=evt.getY()-r.width/2;
		 */
		//float overviewScale=overviewMapJPanel.getScale();
		//float mainMapScale=mainMap.getScale();

		int overviewScale = overviewMapJPanel.getPreferredSize().width;
		int mainMapScale = mainMap.getWidth();

		int x = (int) (evt.getX() * mainMapScale / overviewScale);
		int y = (int) (evt.getY() * mainMapScale / overviewScale);

		Rectangle r = mainMap.getVisibleRect();
		r.x = x - r.width / 2;
		r.y = y - r.height / 2;
		mainMap.scrollRectToVisible(r);
		updateInside(evt);
	}

	private void updateInside(MouseEvent evt) {
		//Rectangle r= overviewMapJPanel.mainMapVisibleRect;
		boolean b = currentVisRect.contains(evt.getX(), evt.getY());

		if (b != inside) {
			inside = b;
			if (inside) {
				overviewMapJPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			} else {
				overviewMapJPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}

	}
	private void updateObservedRect() {
		Rectangle r = mainMap.getVisibleRect();
		//if (!r.equals(this.currentVisRect)) {
			//float overviewScale=overviewMapJPanel.getScale();
			//float mainMapScale=mainMap.getScale();

			int overviewScale = overviewMapJPanel.getPreferredSize().width;
			int mainMapScale = mainMap.getWidth();
			if (0 != (overviewScale * mainMapScale)) {
				//avoid division by zero.
				currentVisRect.x = (int) (r.x * overviewScale / mainMapScale);
				currentVisRect.y = (int) (r.y * overviewScale / mainMapScale);
				currentVisRect.width = (int) (r.width * overviewScale / mainMapScale);
				currentVisRect.height = (int) (r.height * overviewScale / mainMapScale);
				overviewMapJPanel.repaint();
			}
		//}
	}
}

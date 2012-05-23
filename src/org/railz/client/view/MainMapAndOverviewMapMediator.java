/*
 * Copyright (C) 2002 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * MainMapAndOverviewMapMediator.java
 *
 * Created on 24 June 2002, 21:04
 */

package org.railz.client.view;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;

/**
 * This class mediates between the main map view and the overview map view. It
 * does the following:<br>
 * (1) Updates the rectangle on the overview map when the visible rectangle of
 * the main map changes.<br>
 * (2) Updates the main map visible rectangle when the user clicks on the
 * overview map.<br>
 * (3) Updates the main map visible rectangle when the user drags the rectangle
 * on the overview map.<br>
 * (4) Changes the mouse cursor to indicate that the rectangle on the overview
 * map is draggable when the mouse moves into the rectangle.
 * 
 * @author Luke Lindsay
 * @version 1.0
 */
public class MainMapAndOverviewMapMediator {
    private JComponent overviewMapJPanel;
    private JViewport viewport;
    private JComponent mainMap;
    
    /**
     * Position of the overview map rectangle relative to the overview map
     * origin
     */
    private Rectangle currentVisRect;
    
    /**
     * The previous position of the overview map rectangle
     */
    private Point oldLocation;
    
    public MainMapAndOverviewMapMediator() {
	currentVisRect = new Rectangle(10, 10, 10, 10);
    }
    
    public void setOverviewMap(JComponent omv) {
	overviewMapJPanel = omv;
    }
    
    /**
     * @param v
     *            the main map viewport
     * @param mm
     *            The Main map component
     */
    public void setMainMap(JViewport v, JComponent mm) {
	mainMap = mm;
	viewport = v;
	
	viewport.addChangeListener(new javax.swing.event.ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		updateObservedRect();
	    }
	});
    }
    
    /**
     * Scroll the main map to the position indicated by the coordinates.
     * 
     * @param overviewMapPos
     *            position indicated by the overview map coords.
     */
    public void setMainMapPosition(Point overviewMapPos) {
	if (oldLocation == null) {
	    oldLocation = new Point();
	} else {
	    oldLocation.x = currentVisRect.x;
	    oldLocation.y = currentVisRect.y;
	}
	
	if (mainMap == null)
	    return;
	
	int deltaX = overviewMapPos.x - oldLocation.x;
	int deltaY = overviewMapPos.y - oldLocation.y;
	Rectangle r = mainMap.getVisibleRect();
	
	int overviewScale = overviewMapJPanel.getPreferredSize().width;
	int mainMapScale = mainMap.getWidth();
	
	/* calculate the delta to scroll */
	int scaledDeltaX = (int) (deltaX * mainMapScale / overviewScale);
	int scaledDeltaY = (int) (deltaY * mainMapScale / overviewScale);
	
	r.x += scaledDeltaX;
	r.y += scaledDeltaY;
	
	mainMap.scrollRectToVisible(r);
    }
    
    /**
     * Update the overview map with the coordinates from the main map, and
     * redraw the overview map.
     */
    public void updateObservedRect() {
	if (mainMap == null || overviewMapJPanel == null)
	    return;
	
	Rectangle r = mainMap.getVisibleRect();
	int overviewScale = overviewMapJPanel.getPreferredSize().width;
	int mainMapScale = mainMap.getWidth();
	if (0 != (overviewScale * mainMapScale)) {
	    // avoid division by zero.
	    currentVisRect.x = (int) (r.x * overviewScale / mainMapScale);
	    currentVisRect.y = (int) (r.y * overviewScale / mainMapScale);
	    currentVisRect.width = (int) (r.width * overviewScale / mainMapScale);
	    currentVisRect.height = (int) (r.height * overviewScale / mainMapScale);
	    overviewMapJPanel.repaint();
	}
    }
    
    /**
     * @return the rectangle describing the area of the overview map visible on
     *         the main map.
     */
    public Rectangle getMapVisibleRectangle() {
	return currentVisRect;
    }
}

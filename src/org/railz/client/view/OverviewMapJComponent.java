/*
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

package org.railz.client.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.BlankMapRenderer;
import org.railz.client.renderer.MapRenderer;
import org.railz.client.renderer.ZoomedOutMapRenderer;

public class OverviewMapJComponent extends JPanel {
    private GUIRoot guiRoot;
    private MapViewMoveReceiver moveReceiver;
    MainMapAndOverviewMapMediator mediator;

	protected MapRenderer mapView=new BlankMapRenderer(0.4F);

	public OverviewMapJComponent(GUIRoot gr) {
	    this.setPreferredSize(mapView.getMapSizeInPixels());
	    guiRoot = gr;
	    mediator = guiRoot.getMapMediator();
	    addComponentListener(componentListener);
	    addMouseMotionListener(mouseAdapter);
	    addMouseListener(mouseAdapter);
	    guiRoot.getMapMediator().setOverviewMap(this);
	}	

	public void setup(ModelRoot mr){
	    mapView = new ZoomedOutMapRenderer(mr.getWorld(), 240);
	    this.setPreferredSize(mapView.getMapSizeInPixels());
	    this.setMinimumSize(this.getPreferredSize());
	    this.setSize(this.getPreferredSize());

	    if (moveReceiver != null) {
		mr.getMoveChainFork().removeSplitMoveReceiver
		    (moveReceiver);
	    }
	    moveReceiver = new MapViewMoveReceiver(mapView);
	    mr.getMoveChainFork().addSplitMoveReceiver(moveReceiver);

	    if(null!=this.getParent()){									
		this.getParent().validate();
	    }			
	    guiRoot.getMapMediator().setOverviewMap(this);
	}

	protected void paintComponent(java.awt.Graphics g) {
		java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
		java.awt.Rectangle r = this.getVisibleRect();
		// draw the overview map
		mapView.paintRect(g2, r);
		// draw the rectangle
		Rectangle mainMapVisRect = guiRoot.
		    getMapMediator().getMapVisibleRectangle();
		g2.setColor(Color.WHITE);
		g2.drawRect(mainMapVisRect.x, mainMapVisRect.y,
			mainMapVisRect.width, mainMapVisRect.height);
	}
	
	public Dimension getPreferredSize() {
		return mapView.getMapSizeInPixels();
	}
    
	private ComponentListener componentListener = new ComponentAdapter() {
	    public void componentResized(java.awt.event.ComponentEvent evt) {
		guiRoot.getMapMediator().updateObservedRect();
	    }
	    public void componentShown(java.awt.event.ComponentEvent evt) {
		guiRoot.getMapMediator().updateObservedRect();
	    }
	};
	
	private MouseInputAdapter mouseAdapter = new MouseInputAdapter() {
	    boolean inside = false;
	    boolean draggingAndStartedInside = false;

	    private void updateInside(MouseEvent evt) {
		Rectangle currentVisRect = mediator.getMapVisibleRectangle();
		boolean b = currentVisRect.contains(evt.getX(), evt.getY());
		if (b != inside) {
		    inside = b;
		    if (inside) {
			OverviewMapJComponent.this.setCursor
			    (new Cursor(Cursor.MOVE_CURSOR));
		    } else {
			OverviewMapJComponent.this.setCursor
			    (new Cursor(Cursor.DEFAULT_CURSOR));
		    }
		}
	    }

	    public void mouseMoved(MouseEvent evt) {
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

		    mediator.setMainMapPosition(evt.getPoint());
		    updateInside(evt);
		}
	    }

	    public void mouseClicked(MouseEvent evt) {
		Rectangle r = mediator.getMapVisibleRectangle();
		Point p = new Point(evt.getX() - r.width / 2,
			evt.getY() - r.height / 2);
		mediator.setMainMapPosition(p);
		updateInside(evt);
	    }
	};
}

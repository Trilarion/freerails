package jfreerails.client.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JComponent;

import jfreerails.client.renderer.BlankMapRenderer;
import jfreerails.client.renderer.MapRenderer;

public class OverviewMapJComponent extends JComponent {

	protected MapRenderer mapView=new BlankMapRenderer(0.4F);

	protected Rectangle mainMapVisRect;
	
	public OverviewMapJComponent(Rectangle r){		
		this.setPreferredSize(mapView.getMapSizeInPixels());
		mainMapVisRect=r;	
	}	
	public void setup(MapRenderer mv){
		mapView=mv;					
		this.setPreferredSize(mapView.getMapSizeInPixels());
		this.setMinimumSize(this.getPreferredSize());
		this.setSize(this.getPreferredSize());
		if(null!=this.getParent()){									
			this.getParent().validate();
		}			
	}

	protected void paintComponent(java.awt.Graphics g) {
		java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
		java.awt.Rectangle r = this.getVisibleRect();
		mapView.paintRect(g2, r);
		g2.setColor(Color.WHITE);
		g2.drawRect(mainMapVisRect.x,mainMapVisRect.y,mainMapVisRect.width,mainMapVisRect.height);
	}
	
	public Dimension getPreferredSize(){
		return mapView.getMapSizeInPixels();
	}
	
}

package jfreerails.client.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import jfreerails.client.common.Painter;

/** This class draws the radius of a station on the map */
public class StationRadiusRenderer implements Painter {

	static final int tileSize = 30;

	int radius = 2;
	int x, y;
	boolean show = false;

	public void show() {
		this.show = true;
	}

	public void hide() {
		this.show = false;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public void paint(Graphics2D g) {
		if (show) {
			g.setStroke(new BasicStroke(2f)) ;
			g.setColor(Color.WHITE);
			
			g.drawRect(
				tileSize * (x - radius),
				tileSize * (y - radius),
				tileSize * (2 * radius + 1),
				tileSize * (2 * radius + 1));
		}
	}

}

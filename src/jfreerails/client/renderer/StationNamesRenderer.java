/**@author Scott Bennett
 * Date: 14th April 2003
 *
 * Class to render the station names on the game map. Names are retrieved
 * from the KEY.STATIONS object.
 */

package jfreerails.client.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import jfreerails.client.common.Painter;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldIterator;

public class StationNamesRenderer implements Painter {

	private ReadOnlyWorld w;
	
	private int fontSize;
	private Color bgColor;
	private Color textColor;
	
	public StationNamesRenderer(ReadOnlyWorld world) {
		this.w = world;
		
		this.fontSize = 10;
		this.bgColor = Color.BLACK;
		this.textColor = Color.WHITE;
	}
	
	public void paint(Graphics2D g) {
		
		int rectWidth;
		int rectHeight;
		int rectX;
		int rectY;
		float visibleAdvance;
		float textX;
		float textY;
		
		StationModel tempStation;
		String stationName;		
		int positionX;
		int positionY;
		
		Font font = new Font("Arial",0,fontSize);
		FontRenderContext frc = g.getFontRenderContext();
		TextLayout layout;
		
		//draw station names onto map
		WorldIterator wi = new NonNullElements(KEY.STATIONS, w);
		while(wi.next()){	//loop over non null stations
			
			tempStation = (StationModel)wi.getElement();
	
		  	stationName = tempStation.getStationName();
		  	positionX = (tempStation.getStationX() * 30) + 15;
		  	positionY = (tempStation.getStationY() * 30) + 30;
	
			layout = new TextLayout(stationName, font, frc);
			visibleAdvance = layout.getVisibleAdvance();
			
			rectWidth = (int)(visibleAdvance*1.2);		
			rectHeight = (int)(fontSize*1.5);
			rectX = (int)(positionX - (rectWidth/2));
			rectY = positionY;
			
			g.setColor(bgColor);
			g.fillRect(rectX,rectY,rectWidth,rectHeight);
			
			textX = (float)(positionX - (visibleAdvance/2));
			textY = positionY + fontSize + 1;
			
			g.setColor(textColor);
			layout.draw(g,textX,textY);
			
			g.setStroke(new BasicStroke(1.0f));
				//draw a border 1 pixel inside the edges of the rectangle
			g.draw(new Rectangle(rectX+1,rectY+1,rectWidth-3,rectHeight-3));
			
	  	} //end FOR loop
	  	
	} //paint method
	
}



		


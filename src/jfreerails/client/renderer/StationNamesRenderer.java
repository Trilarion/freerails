package jfreerails.client.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.RoundRectangle2D;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.Painter;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.track.FreerailsTile;


/**
 *
 * Class to render the station names and spheres of influence on the game map. Names are retrieved
 * from the KEY.STATIONS object.
 * Date: 14th April 2003
 * 28 May 2004 updated to also show station sphere of influence.
 *
 * @author Scott Bennett
 * @author Luke Lindsay
 *
 */
public class StationNamesRenderer implements Painter {
    private final ReadOnlyWorld w;
    private final ModelRoot modelRoot;
    private final int fontSize;
    private final Color bgColor;
    private final Color textColor;
    final static float[] dash1 = {5.0f};
    final static BasicStroke dashed = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
    private final Font font;

    public StationNamesRenderer(ReadOnlyWorld world, ModelRoot modelRoot) {
        this.w = world;
        this.modelRoot = modelRoot;
        this.fontSize = 10;
        this.bgColor = Color.BLACK;
        this.textColor = Color.WHITE;
        font = new Font("Arial", 0, fontSize);
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

        Boolean showStationNames = (Boolean)modelRoot.getProperty(ModelRoot.SHOW_STATION_NAMES);
        Boolean showStationBorders = (Boolean)modelRoot.getProperty(ModelRoot.SHOW_STATION_BORDERS);

        FontRenderContext frc = g.getFontRenderContext();
        TextLayout layout;

        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();

            //draw station names onto map
            WorldIterator wi = new NonNullElements(KEY.STATIONS, w, principal);

            while (wi.next()) { //loop over non null stations
                tempStation = (StationModel)wi.getElement();

                int x = tempStation.getStationX();
                int y = tempStation.getStationY();

                //First draw station sphere of influence  
                if (showStationBorders.booleanValue()) {
                    FreerailsTile tile = w.getTile(x, y);
                    int radius = tile.getTrackRule().getStationRadius();
                    int diameterInPixels = (radius * 2 + 1) * 30;
                    int radiusX = (x - radius) * 30;
                    int radiusY = (y - radius) * 30;
                    g.setColor(Color.WHITE);
                    g.setStroke(dashed);
                    g.draw(new RoundRectangle2D.Double(radiusX, radiusY,
                            diameterInPixels, diameterInPixels, 10, 10));
                }

                //Then draw the station name.
                if (showStationNames.booleanValue()) {
                    stationName = tempStation.getStationName();

                    positionX = (x * 30) + 15;
                    positionY = (y * 30) + 30;

                    layout = new TextLayout(stationName, font, frc);
                    visibleAdvance = layout.getVisibleAdvance();

                    rectWidth = (int)(visibleAdvance * 1.2);
                    rectHeight = (int)(fontSize * 1.5);
                    rectX = (positionX - (rectWidth / 2));
                    rectY = positionY;

                    g.setColor(bgColor);
                    g.fillRect(rectX, rectY, rectWidth, rectHeight);

                    textX = (positionX - (visibleAdvance / 2));
                    textY = positionY + fontSize + 1;

                    g.setColor(textColor);
                    layout.draw(g, textX, textY);

                    g.setStroke(new BasicStroke(1.0f));
                    //draw a border 1 pixel inside the edges of the rectangle
                    g.draw(new Rectangle(rectX + 1, rectY + 1, rectWidth - 3,
                            rectHeight - 3));
                }
            }
        }

        //end FOR loop
    }
    //paint method
}
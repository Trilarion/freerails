package jfreerails.client.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.Painter;


/** This class draws the radius of a station on the map.
 * @author Luke
 * */
public class StationRadiusRenderer implements Painter {
    /**
     * Border colour to use when placement is OK.
     */
    public static final Color COLOR_OK = Color.WHITE;

    /**
     * Border colour to use when placement is not allowed.
     */
    public static final Color COLOR_CANNOT_BUILD = Color.RED;

    /**
     * Colour of the highlighted border.
     */
    private Color borderColor = COLOR_OK;
    private static final int tileSize = 30;
    private int radius = 2;
    private int x;
    private int y;
    private final ModelRoot modelRoot;

    public StationRadiusRenderer(ModelRoot mr) {
        this.modelRoot = mr;
    }

    public void setBorderColor(Color c) {
        borderColor = c;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void show() {
        String lastCursorMode = (String)modelRoot.getProperty(ModelRoot.CURSOR_MODE);

        if (!lastCursorMode.equals(ModelRoot.PLACE_STATION_CURSOR_MODE)) {
            modelRoot.setProperty(ModelRoot.PREVIOUS_CURSOR_MODE, lastCursorMode);
            modelRoot.setProperty(ModelRoot.CURSOR_MODE,
                ModelRoot.PLACE_STATION_CURSOR_MODE);
        }
    }

    public void hide() {
        String lastCursorMode = (String)modelRoot.getProperty(ModelRoot.PREVIOUS_CURSOR_MODE);

        assert !lastCursorMode.equals(ModelRoot.PLACE_STATION_CURSOR_MODE);

        modelRoot.setProperty(ModelRoot.CURSOR_MODE, lastCursorMode);
    }

    public void paint(Graphics2D g) {
        if (modelRoot.getProperty(ModelRoot.CURSOR_MODE).equals(ModelRoot.PLACE_STATION_CURSOR_MODE)) {
            g.setStroke(new BasicStroke(2f));
            g.setColor(borderColor);

            g.drawRect(tileSize * (x - radius), tileSize * (y - radius),
                tileSize * (2 * radius + 1), tileSize * (2 * radius + 1));
        }
    }
}
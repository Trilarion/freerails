package jfreerails.client.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import jfreerails.client.common.Painter;


/** This class draws the radius of a station on the map */
public class StationRadiusRenderer implements Painter {
    /**
     * Border colour to use when placement is OK
     */
    public static final Color COLOR_OK = Color.WHITE;

    /**
     * Border colour to use when placement is not allowed
     */
    public static final Color COLOR_CANNOT_BUILD = Color.RED;

    /**
     * Colour of the highlighted border
     */
    private Color borderColor = COLOR_OK;
    static final int tileSize = 30;
    int radius = 2;
    int x;
    int y;
    boolean show = false;

    public void setBorderColor(Color c) {
        borderColor = c;
    }

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
            g.setStroke(new BasicStroke(2f));
            g.setColor(borderColor);

            g.drawRect(tileSize * (x - radius), tileSize * (y - radius),
                tileSize * (2 * radius + 1), tileSize * (2 * radius + 1));
        }
    }
}
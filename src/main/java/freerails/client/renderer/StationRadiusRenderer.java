package freerails.client.renderer;

import freerails.client.common.Painter;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.controller.ModelRoot.Value;
import freerails.client.Constants;

import java.awt.*;

/**
 * This class draws the radius of a station on the map.
 *
 */
public class StationRadiusRenderer implements Painter {
    /**
     * Border colour to use when placement is OK.
     */
    public static final Color COLOR_OK = Color.WHITE;

    /**
     * Border colour to use when placement is not allowed.
     */
    public static final Color COLOR_CANNOT_BUILD = Color.RED;
    private static final int tileSize = Constants.TILE_SIZE;
    private final ModelRoot modelRoot;
    /**
     * Colour of the highlighted border.
     */
    private Color borderColor = COLOR_OK;
    private int radius = 2;
    private int x;
    private int y;

    /**
     *
     * @param mr
     */
    public StationRadiusRenderer(ModelRoot mr) {
        this.modelRoot = mr;
    }

    /**
     *
     * @param c
     */
    public void setBorderColor(Color c) {
        borderColor = c;
    }

    /**
     *
     * @param x
     * @param y
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @param radius
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     *
     */
    public void show() {
        if (!modelRoot
                .is(Property.CURSOR_MODE, Value.PLACE_STATION_CURSOR_MODE)) {
            modelRoot.setProperty(Property.PREVIOUS_CURSOR_MODE, modelRoot
                    .getProperty(Property.CURSOR_MODE));
            modelRoot.setProperty(Property.CURSOR_MODE,
                    Value.PLACE_STATION_CURSOR_MODE);
            modelRoot.setProperty(Property.IGNORE_KEY_EVENTS, Boolean.TRUE);
        }
    }

    /**
     *
     */
    public void hide() {
        ModelRoot.Value lastCursorMode = (ModelRoot.Value) modelRoot
                .getProperty(ModelRoot.Property.PREVIOUS_CURSOR_MODE);

        assert !lastCursorMode
                .equals(ModelRoot.Value.PLACE_STATION_CURSOR_MODE);

        modelRoot.setProperty(ModelRoot.Property.CURSOR_MODE, lastCursorMode);
        modelRoot.setProperty(Property.IGNORE_KEY_EVENTS, Boolean.FALSE);
    }

    /**
     *
     * @param g
     * @param newVisibleRectectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectectangle) {
        if (modelRoot.getProperty(ModelRoot.Property.CURSOR_MODE).equals(
                Value.PLACE_STATION_CURSOR_MODE)) {
            g.setStroke(new BasicStroke(2f));
            g.setColor(borderColor);

            g.drawRect(tileSize * (x - radius), tileSize * (y - radius),
                    tileSize * (2 * radius + 1), tileSize * (2 * radius + 1));
        }
    }
}
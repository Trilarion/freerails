package jfreerails.client.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import jfreerails.client.common.Painter;
import java.util.List;
import jfreerails.world.common.OneTileMoveVector;
import java.awt.Point;
import java.util.*;


/** This class draws the track being build.
 * @author MystiqueAgent
 * */
public class BuildTrackRenderer implements Painter {
    /**
     * Track colour.
     */
    public static final Color COLOR = Color.GREEN;

    /**
     * Colour of the track.
     */
    private Color trackColor = COLOR;

    private static final int tileSize = 30;
    private List track;
    private int x;
    private int y;
    private boolean show = false;

    public void setTrackColor(Color c) {
        trackColor = c;
    }

    public void show() {
        this.show = true;
    }

    public void hide() {
        this.show = false;
        track = null;
    }

    public void setTrack(Point startPoint, List track) {
        this.track = track;
        this.x = (int) startPoint.getX();
        this.y = (int) startPoint.getY();
    }

    public void paint(Graphics2D g) {
        if (show && track != null) {
            g.setStroke(new BasicStroke(2f));
            g.setColor(trackColor);
            int lastX = x;
            int lastY = y;
            int diffXY = (int) (0.5 * tileSize);
            for (Iterator iter = track.iterator(); iter.hasNext(); ) {
                OneTileMoveVector vector = (OneTileMoveVector)iter.next();
                int newX = lastX + vector.getDx();
                int newY = lastY + vector.getDy();
                g.drawLine(tileSize * lastX + diffXY, tileSize * lastY + diffXY,
                           tileSize * newX + diffXY, tileSize * newY + diffXY);
                lastX = newX;
                lastY = newY;
            }
        }
    }
}

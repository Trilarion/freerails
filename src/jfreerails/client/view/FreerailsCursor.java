/**
*
* Created on 01 August 2001, 06:02
*/
package jfreerails.client.view;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import jfreerails.client.common.ModelRoot;


/** Paints the cursor on the map, note the cursor's position is stored on the ModelRoot
 * under the key CURSOR_POSITION.
 * @author Luke
 */
final public class FreerailsCursor implements KeyListener {
    private int blinkValue = 1;
    private BasicStroke stroke = new BasicStroke(3);
    private final ModelRoot modelRoot;

    /** Paints the cursor.  The method calculates position to paint it based on the
    * tile size and the cursor's map position.
    * @param g The graphics object to paint the cursor on.
    * @param tileSize The dimensions of a tile.
    */
    public void paintCursor(Graphics g, Dimension tileSize) {
        Graphics2D g2 = (Graphics2D)g;

        //First draw the cursor.
        g2.setStroke(stroke);

        if (1 == blinkValue) {
            g2.setColor(java.awt.Color.white); //The colour of the cursor
        } else {
            g2.setColor(java.awt.Color.black);
        }

        Point cursorMapPosition = (Point)modelRoot.getProperty(ModelRoot.CURSOR_POSITION);
        int x = cursorMapPosition.x * tileSize.width;
        int y = cursorMapPosition.y * tileSize.height;
        g2.drawRect(x, y, tileSize.width, tileSize.height);

        //Second, draw a message below the cursor if appropriate.
        String message = (String)modelRoot.getProperty(ModelRoot.CURSOR_MESSAGE);

        if (null != message && !message.equals("")) {
            int fontSize = 12;
            Font font = new Font("Arial", 0, fontSize);
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout layout = new TextLayout(message, font, frc);

            //We want the message to be centered below the cursor.
            float visibleAdvance = layout.getVisibleAdvance();
            float textX = (x + (tileSize.width / 2) - (visibleAdvance / 2));
            float textY = y + tileSize.height + fontSize + 5;
            g.setColor(java.awt.Color.white);
            layout.draw(g2, textX, textY);
        }
    }

    /** Use this method rather than KeyTyped to process keyboard input.
    * @param keyEvent The key pressed.
    */
    public void keyPressed(KeyEvent keyEvent) {
    }

    /** Empty method, needed to implement the KeyListener interface.
    * @param keyEvent The key typed.
    */
    public void keyTyped(KeyEvent keyEvent) {
    }

    /** Use keyPressed instead of this method.
    * @param keyEvent the key pressed
    */
    public void keyReleased(KeyEvent keyEvent) {
    }

    /** Creates a new FreerailsCursor.
    */
    public FreerailsCursor(ModelRoot mr) {
        this.modelRoot = mr;
        modelRoot.setProperty(ModelRoot.CURSOR_MESSAGE, null);
    }
}
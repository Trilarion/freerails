/**
 *
 * Created on 01 August 2001, 06:02
 */
package jfreerails.client.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.IOException;

import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.BuildTrackRenderer;
import jfreerails.controller.TrackMoveProducer;

/**
 * Paints the cursor on the map, note the cursor's position is stored on the
 * ModelRoot under the key CURSOR_POSITION.
 * 
 * @author Luke
 */
final public class FreerailsCursor {
	private final Image buildTrack, upgradeTrack, removeTrack, infoMode;

	private final ModelRoot modelRoot;
	
	/** The location of the cursor last time paintCursor(.) was called.*/
	private Point lastCursorPosition = new Point();
	
	/** The time in ms the cursor arrived at its current position.*/
	private long timeArrived = 0;
	
	/**
	 * Creates a new FreerailsCursor.
	 * 
	 * @throws IOException
	 */
	public FreerailsCursor(ModelRoot mr, ImageManager im) throws IOException {
		this.modelRoot = mr;
		modelRoot.setProperty(ModelRoot.Property.CURSOR_MESSAGE, null);
		buildTrack = im.getImage("/cursor/buildtrack.png");
		upgradeTrack = im.getImage("/cursor/upgradetrack.png");
		removeTrack = im.getImage("/cursor/removetrack.png");
		infoMode = im.getImage("/cursor/infomode.png");
	}

	/**
	 * Paints the cursor. The method calculates position to paint it based on
	 * the tile size and the cursor's map position.
	 * 
	 * @param g
	 *            The graphics object to paint the cursor on.
	 * @param tileSize
	 *            The dimensions of a tile.
	 */
	public void paintCursor(Graphics g, Dimension tileSize) {
		Graphics2D g2 = (Graphics2D) g;
		

		Integer trackBuilderMode = (Integer) modelRoot
				.getProperty(ModelRoot.Property.TRACK_BUILDER_MODE);

		Point cursorMapPosition = (Point) modelRoot
				.getProperty(ModelRoot.Property.CURSOR_POSITION);
		
		/* Has the cursor moved since we last painted it?*/
		if(!cursorMapPosition.equals(lastCursorPosition)){
			lastCursorPosition = new Point(cursorMapPosition);
			timeArrived = System.currentTimeMillis();
		}
		
		int x = cursorMapPosition.x * tileSize.width;
		int y = cursorMapPosition.y * tileSize.height;
		
		Image cursor = null;
		switch (trackBuilderMode.intValue()) {
		case TrackMoveProducer.BUILD_TRACK:
			cursor = buildTrack;
			break;
		case TrackMoveProducer.REMOVE_TRACK:
			cursor = removeTrack;
			break;
		case TrackMoveProducer.UPGRADE_TRACK:
			cursor = upgradeTrack;
			break;
		case TrackMoveProducer.IGNORE_TRACK:
			cursor = infoMode;
			break;
		}
		Boolean b = (Boolean)modelRoot.getProperty(ModelRoot.Property.IGNORE_KEY_EVENTS);
		long time = System.currentTimeMillis() - timeArrived;
		boolean show = ((time / 500) % 2) == 0;
		if (show && !b.booleanValue()) {
			g.drawImage(cursor, x, y, null);
		}

		// Second, draw a message below the cursor if appropriate.
		String message = (String) modelRoot
				.getProperty(ModelRoot.Property.CURSOR_MESSAGE);

		if (null != message && !message.equals("")) {
			int fontSize = 12;
			Font font = new Font("Arial", 0, fontSize);
			FontRenderContext frc = g2.getFontRenderContext();
			TextLayout layout = new TextLayout(message, font, frc);

			// We want the message to be centered below the cursor.
			float visibleAdvance = layout.getVisibleAdvance();
			float textX = (x + (tileSize.width / 2) - (visibleAdvance / 2));
			float textY = y + tileSize.height + fontSize + 5;
			g.setColor(java.awt.Color.white);
			layout.draw(g2, textX, textY);
		}
		
		//Draw a big white dot at the target point.
        Point targetPoint = (Point)modelRoot.getProperty(ModelRoot.Property.THINKING_POINT);
		if (null != targetPoint) {
            time = System.currentTimeMillis();
            int dotSize;

            if ((time % 500) > 250) {
                dotSize = BuildTrackRenderer.BIG_DOT_WIDTH;
            } else {
                dotSize = BuildTrackRenderer.SMALL_DOT_WIDTH;
            }

            g.setColor(Color.WHITE);

            x = targetPoint.x * tileSize.width +
                (tileSize.width - dotSize) / 2;
            y = targetPoint.y * tileSize.width +
                (tileSize.height - dotSize) / 2;
            g.fillOval(x, y, dotSize, dotSize);
        }
	}		

	
}
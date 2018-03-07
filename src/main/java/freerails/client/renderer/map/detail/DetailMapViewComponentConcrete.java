/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * MapViewComponent.java
 *
 */
package freerails.client.renderer.map.detail;

import freerails.client.ClientConfig;
import freerails.client.ModelRootImpl;
import freerails.client.ModelRootListener;
import freerails.client.ModelRootProperty;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.map.MapRenderer;
import freerails.client.view.FreerailsCursor;
import freerails.util.Vector2D;
import freerails.model.WorldConstants;

import java.awt.*;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Displays the map, the cursor, and user messages (which are stored on the
 * ModelRoot under the keys QUICK_MESSAGE and PERMANENT_MESSAGE).
 */
public class DetailMapViewComponentConcrete extends DetailMapViewComponent implements ModelRootListener {

    private static final long serialVersionUID = 3834868087706236208L;
    /**
     * The length of the array is the number of lines. This is necessary since
     * Graphics.drawString(..) doesn't know about newline characters
     */
    private String[] quickMessage = new String[0];
    /**
     * Message that will appear in the middle of the screen in
     * {@code LARGE_MESSAGE_FONT}.
     */
    private String permanentMessage = null;
    /**
     * Time at which to stop displaying the current user message.
     */
    private long displayMessageUntil = 0;
    private FreerailsCursor mapCursor;

    /**
     *
     */
    public DetailMapViewComponentConcrete() {
        super();

        DetailMapViewComponentMouseAdapter mouseAdapter = new DetailMapViewComponentMouseAdapter(this);
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // TODO without this the map is blank the first time (why?)
        if (EventQueue.isDispatchThread()) {
            return;
        }
        super.paintComponent(g);

        if (null != mapCursor && isFocusOwner()) {
            mapCursor.paintCursor(g, new Dimension(WorldConstants.TILE_SIZE, WorldConstants.TILE_SIZE));
        }

        if (System.currentTimeMillis() < displayMessageUntil) {
            Rectangle visRect = getVisibleRect();
            g.setColor(Color.WHITE);
            g.setFont(ClientConfig.USER_MESSAGE_FONT);

            for (int i = 0; i < quickMessage.length; i++) {
                g.drawString(quickMessage[i], 50 + visRect.x, 50 + visRect.y + i * 20);
            }
        }

        if (permanentMessage != null) {
            Rectangle visRect = getVisibleRect();
            g.setColor(Color.lightGray);
            g.setFont(ClientConfig.LARGE_MESSAGE_FONT);

            int msgWidth = g.getFontMetrics(ClientConfig.LARGE_MESSAGE_FONT).stringWidth(permanentMessage);
            int msgHeight = g.getFontMetrics(ClientConfig.LARGE_MESSAGE_FONT).getHeight();
            g.drawString(permanentMessage, (int) (visRect.x + (visRect.getWidth() - msgWidth) / 2), (int) (visRect.y + (visRect.getHeight() - msgHeight) / 2));
        }
    }

    /**
     * @param mapRenderer
     * @param modelRoot
     * @param rendererRoot
     * @throws IOException
     */
    public void setup(MapRenderer mapRenderer, ModelRootImpl modelRoot, RendererRoot rendererRoot) throws IOException {
        super.setMapRenderer(mapRenderer);

        setBorder(null);

        mapCursor = new FreerailsCursor(modelRoot, rendererRoot);

        modelRoot.addPropertyChangeListener(this);
    }

    /**
     * @param g
     */
    public void paintTile(Graphics g, Vector2D tileLocation) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void refreshTile(Vector2D tileLocation) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void refreshAll() {
        getMapRenderer().refreshAll();
    }

    /**
     * @param g
     * @param visibleRect
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks what triggered the specified PropertyChangeEvent and reacts as
     * follows.
     *
     * (1) If it was ModelRoot.CURSOR_POSITION, scrolls the map if necessary.
     *
     *
     * (2) If it was ModelRoot.QUICK_MESSAGE, display or hide the message as
     * appropriate.
     *
     *
     * (3) If it was ModelRoot.PERMANENT_MESSAGE, display or hide the message as
     * appropriate.
     */
    @Override
    public void propertyChange(ModelRootProperty modelRootProperty, Object oldValue, Object newValue) {

        switch (modelRootProperty) {
            case CURSOR_POSITION:
                Vector2D newPoint = (Vector2D) newValue;
                Vector2D oldPoint = (Vector2D) oldValue;

                if (null == oldPoint) {
                    oldPoint = new Vector2D();
                }

                // react to cursor move
                float scale = getMapRenderer().getScale();
                Dimension tileSize = new Dimension((int) scale, (int) scale);
                Rectangle visibleRect = getVisibleRect();
                Rectangle rectangleSurroundingCursor = new Rectangle(0, 0, 1, 1);

                rectangleSurroundingCursor.setLocation((newPoint.x - 1) * tileSize.width, (newPoint.y - 1) * tileSize.height);
                rectangleSurroundingCursor.setSize(tileSize.width * 3, tileSize.height * 3);

                if (!(visibleRect.contains(rectangleSurroundingCursor))) {
                    int x = newPoint.x * tileSize.width - visibleRect.width / 2;
                    int y = newPoint.y * tileSize.height - visibleRect.height / 2;
                    scrollRectToVisible(new Rectangle(x, y, visibleRect.width, visibleRect.height));
                }

                // why 3x3 tiles are newly painted
                repaint((newPoint.x - 1) * tileSize.width, (newPoint.y - 1) * tileSize.height, tileSize.width * 3, tileSize.height * 3);
                repaint((oldPoint.x - 1) * tileSize.width, (oldPoint.y - 1) * tileSize.height, tileSize.width * 3, tileSize.height * 3);
                break;
            case QUICK_MESSAGE:
                String newMessage = (String) newValue;

                if (null != newMessage) {
                    StringTokenizer st = new StringTokenizer(newMessage, "\n");
                    quickMessage = new String[st.countTokens()];

                    int i = 0;

                    while (st.hasMoreTokens()) {
                        quickMessage[i] = st.nextToken();
                        i++;
                    }

                    // Display the message for 5 seconds.
                    displayMessageUntil = System.currentTimeMillis() + 1000 * 5;
                } else {
                    // Its null, so stop displaying whatever we where displaying.
                    displayMessageUntil = Long.MIN_VALUE;
                }
                break;
            case PERMANENT_MESSAGE:
                permanentMessage = (String) newValue;
                break;
        }
    }

}
package jfreerails.client.common;

import java.awt.Graphics;

/**
 * Implemented by components that require regular screen updates
 */
public interface UpdatedComponent {
    /**
     * Called when the component should schedule a screen update
     */
    public void doFrameUpdate(Graphics g);
}

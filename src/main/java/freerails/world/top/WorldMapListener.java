/*
 * Created on Apr 11, 2004
 */
package freerails.world.top;

import java.awt.*;

/**
 * Classes that need to be notified of changes to the map on the world object
 * should implement this interface.
 *
 * @author Luke Lindsay
 */
public interface WorldMapListener {
    /**
     * Called when tiles have changed.
     *
     * @param tilesChanged rectangle containing the tiles that have change; all the
     *                     points contained by the rectangle must be within the map's
     *                     bounds.
     */
    void tilesChanged(Rectangle tilesChanged);
}
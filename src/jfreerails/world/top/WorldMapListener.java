/*
 * Created on Apr 11, 2004
 */
package jfreerails.world.top;

import java.awt.Rectangle;


/**
 * Classes that need to be notified of changes to the map on the world object should implement this interface.
 *
 * @author Luke Lindsay
 *
 *
 */
public interface WorldMapListener {
    void tilesChanged(Rectangle tilesChanged);
}
package freerails.move;

import java.awt.*;

/**
 * This interface tags Moves that change items on the map and tells the caller
 * which tiles have been updated. It is used by the map-view classes to
 * determine which tiles need repainting.
 *
 * @author Luke
 */
public interface MapUpdateMove extends Move {
    Rectangle getUpdatedTiles();
}
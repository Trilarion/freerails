package jfreerails.world.track;

import jfreerails.world.common.FreerailsSerializable;


/** Defines methods to access the properties of the track on a tile.
 * @author Luke
 */
public interface TrackPiece extends FreerailsSerializable {
    int getTrackGraphicNumber();

    TrackRule getTrackRule();

    TrackConfiguration getTrackConfiguration();

    int getOwnerID();
}
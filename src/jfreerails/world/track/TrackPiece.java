package jfreerails.world.track;

import jfreerails.world.common.FreerailsSerializable;


/** Defines the interface of a the track on a tile.
 * @author Luke
 */
public interface TrackPiece extends FreerailsSerializable {
    int getTrackGraphicNumber();

    TrackRule getTrackRule();

    TrackConfiguration getTrackConfiguration();

    int getOwnerID();
}
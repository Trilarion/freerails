package freerails.world.track;

import freerails.world.common.FreerailsSerializable;

/**
 * Defines methods to access the properties of the track on a tile.
 * 
 * @author Luke
 */
public interface TrackPiece extends FreerailsSerializable {
    int getTrackGraphicID();

    int getTrackTypeID();

    TrackRule getTrackRule();

    TrackConfiguration getTrackConfiguration();

    int getOwnerID();
}
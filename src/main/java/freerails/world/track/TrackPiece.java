package freerails.world.track;

import freerails.world.FreerailsSerializable;

/**
 * Defines methods to access the properties of the track on a tile.
 *
 */
public interface TrackPiece extends FreerailsSerializable {

    /**
     *
     * @return
     */
    int getTrackGraphicID();

    /**
     *
     * @return
     */
    int getTrackTypeID();

    /**
     *
     * @return
     */
    TrackRule getTrackRule();

    /**
     *
     * @return
     */
    TrackConfiguration getTrackConfiguration();

    /**
     *
     * @return
     */
    int getOwnerID();
}
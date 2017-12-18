package freerails.world.track;

import freerails.world.common.FreerailsSerializable;

/**
 * Defines methods to access the properties of the track on a tile.
 *
 * @author Luke
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
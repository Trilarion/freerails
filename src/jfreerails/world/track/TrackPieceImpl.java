package jfreerails.world.track;


/**
 * Represents the track on a tile.
 * @author Luke
 */
final public class TrackPieceImpl implements TrackPiece {
    private final TrackConfiguration configuration;
    private final TrackRule trackType;
    private final int ownerID;

    public int hashCode() {
        int result;
        result = configuration.hashCode();
        result = 29 * result + trackType.hashCode();
        result = 29 * result + ownerID;

        return result;
    }

    public TrackPieceImpl(jfreerails.world.track.TrackConfiguration c,
        TrackRule type, int owner) {
        configuration = c;
        trackType = type;
        ownerID = owner;
    }

    public int getTrackGraphicNumber() {
        return configuration.getTrackGraphicsNumber();
    }

    public TrackRule getTrackRule() {
        return trackType;
    }

    public TrackConfiguration getTrackConfiguration() {
        return configuration;
    }

    public boolean equals(Object o) {
        if (o instanceof TrackPieceImpl) {
            TrackPieceImpl trackPieceImpl = (TrackPieceImpl)o;

            if (configuration.equals(trackPieceImpl.getTrackConfiguration()) &&
                    trackType.equals(trackPieceImpl.getTrackRule())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int getOwnerID() {
        return ownerID;
    }
}
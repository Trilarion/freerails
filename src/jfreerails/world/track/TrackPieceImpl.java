package jfreerails.world.track;

final public class TrackPieceImpl implements TrackPiece {
    private final TrackConfiguration configuration;
    private final TrackRule trackType;
    private final int ownerID;

    public TrackPieceImpl(jfreerails.world.track.TrackConfiguration c,
        TrackRule type, int owner) {
        configuration = c;
        trackType = type;
        this.ownerID = owner;
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
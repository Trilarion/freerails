package jfreerails.world.track;

final public class TrackPieceImpl implements TrackPiece {
    private final TrackConfiguration configuration;
    private final TrackRule trackType;

    public TrackPieceImpl(jfreerails.world.track.TrackConfiguration c,
        TrackRule type) {
        configuration = c;
        trackType = type;
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
}
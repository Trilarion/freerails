package jfreerails.world.track;


/**
 * Represents the track on a tile.
 * @author Luke
 */
final public class TrackPieceImpl implements TrackPiece {
    private final TrackConfiguration configuration;
    private final TrackRule trackType;
    private final int ownerID;
    private final int ruleNumber;

    public int hashCode() {
        int result;
        result = configuration.hashCode();
        result = 29 * result + trackType.hashCode();
        result = 29 * result + ownerID;

        return result;
    }

    public TrackPieceImpl(jfreerails.world.track.TrackConfiguration c,
        TrackRule type, int owner, int rule) {
        configuration = c;
        trackType = type;
        ownerID = owner;
        ruleNumber = rule;        
    }

    public int getTrackGraphicID() {
        return configuration.getTrackGraphicsID();
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
            }
			return false;
        }
		return false;
    }

    public int getOwnerID() {
        return ownerID;
    }

	public int getTrackTypeID() {		
		return ruleNumber;
	}
}
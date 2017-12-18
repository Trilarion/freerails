package freerails.world.track;

/**
 * Represents the track on a tile.
 *
 * @author Luke
 */
final public class TrackPieceImpl implements TrackPiece {
    private static final long serialVersionUID = 4049080423458027569L;

    private final TrackConfiguration configuration;

    private final TrackRule trackType;

    private final int ownerID;

    private final int ruleNumber;

    /**
     *
     * @param c
     * @param type
     * @param owner
     * @param rule
     */
    public TrackPieceImpl(TrackConfiguration c, TrackRule type, int owner,
                          int rule) {
        configuration = c;
        trackType = type;
        ownerID = owner;
        ruleNumber = rule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final TrackPieceImpl that = (TrackPieceImpl) o;

        if (ownerID != that.ownerID)
            return false;
        if (ruleNumber != that.ruleNumber)
            return false;
        if (!configuration.equals(that.configuration))
            return false;
        return trackType.equals(that.trackType);
    }

    @Override
    public int hashCode() {
        int result;
        result = configuration.hashCode();
        result = 29 * result + trackType.hashCode();
        result = 29 * result + ownerID;
        result = 29 * result + ruleNumber;
        return result;
    }

    /**
     *
     * @return
     */
    public int getTrackGraphicID() {
        return configuration.getTrackGraphicsID();
    }

    /**
     *
     * @return
     */
    public TrackRule getTrackRule() {
        return trackType;
    }

    /**
     *
     * @return
     */
    public TrackConfiguration getTrackConfiguration() {
        return configuration;
    }

    /**
     *
     * @return
     */
    public int getOwnerID() {
        return ownerID;
    }

    /**
     *
     * @return
     */
    public int getTrackTypeID() {
        return ruleNumber;
    }
}
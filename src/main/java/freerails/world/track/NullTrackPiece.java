/*
 * NullTrackPiece.java
 *
 * Created on 23 January 2002, 21:31
 */
package freerails.world.track;

import java.io.ObjectStreamException;

/**
 * A track piece that doesn't exist - using this avoids needing to check against
 * null before calling the methods on a track piece.
 *
 * @author lindsal
 */
final public class NullTrackPiece implements TrackPiece {
    private static final long serialVersionUID = 3258413915376268599L;

    private static final TrackPiece nullTrackPiece = new NullTrackPiece();

    private static final int NO_OWNER = Integer.MIN_VALUE;

    private NullTrackPiece() {
    }

    /**
     *
     * @return
     */
    public static TrackPiece getInstance() {
        return nullTrackPiece;
    }

    /**
     *
     * @return
     */
    public int getTrackGraphicID() {
        return 0;
    }

    /**
     *
     * @return
     */
    public TrackRule getTrackRule() {
        return NullTrackType.getInstance();
    }

    /**
     *
     * @return
     */
    public TrackConfiguration getTrackConfiguration() {
        return TrackConfiguration.from9bitTemplate(0);
    }

    private Object readResolve() throws ObjectStreamException {
        return nullTrackPiece;
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return 777;
    }

    /**
     *
     * @return
     */
    public int getOwnerID() {
        return NO_OWNER;
    }

    /**
     *
     * @return
     */
    public int getTrackTypeID() {
        return NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER;
    }
}
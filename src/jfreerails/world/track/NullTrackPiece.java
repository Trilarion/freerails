/*
 * NullTrackPiece.java
 *
 * Created on 23 January 2002, 21:31
 */

package jfreerails.world.track;

import java.io.ObjectStreamException;


/**
 *
 * @author  lindsal
 */
final public class NullTrackPiece implements TrackPiece {

    private static final TrackPiece nullTrackPiece=new NullTrackPiece();

    /** Creates new NullTrackPiece */
    private NullTrackPiece() {
    }

    public static TrackPiece getInstance(){
        return nullTrackPiece;
    }


    public int getRGB() {
        return 0;
    }


    public int getTrackGraphicNumber() {
        return 0;
    }

    public TrackRule getTrackRule() {
        return NullTrackType.getInstance();
    }

    public TrackConfiguration getTrackConfiguration() {
        return TrackConfiguration.getFlatInstance(0);
    }

    private Object readResolve() throws ObjectStreamException {
    	return nullTrackPiece;
    }

}

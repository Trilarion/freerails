package jfreerails.world.track;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import jfreerails.world.common.FlatTrackTemplate;
import jfreerails.world.common.OneTileMoveVector;


/**
 * An instance of this class represents one of the possible track configurations
 * in a map square - the combinations of directions in which track can be
 * laid. Instances of this class cannot be created and must be obtained via the
 * static methods herein.
 *
 * @author Luke
 */
final public class TrackConfiguration implements FlatTrackTemplate {
    private static final ArrayList<TrackConfiguration> flatTrackConfigurations = setupConfigurations();
    public static final int LENGTH_OF_STRAIGHT_TRACK_PIECE = 200;

    /**
     * @return the superposition of two track templates
     */
    public static TrackConfiguration add(FlatTrackTemplate c,
        FlatTrackTemplate v) {
        /*
        int x=v.getX()+1;
        int y=v.getY()+1;
        int oldTemplate =c.getTrackGraphicsNumber();
        int newTemplate = oldTemplate | (1 << (3 * y + x));
        */
        int newTemplate = c.get9bitTemplate() | v.get9bitTemplate();

        return from9bitTemplate(newTemplate);
    }

    public static TrackConfiguration from9bitTemplate(int i) {
        return flatTrackConfigurations.get(i);
    }

    public static TrackConfiguration getFlatInstance(OneTileMoveVector v) {
        return from9bitTemplate(v.get9bitTemplate());
    }

    public static TrackConfiguration getFlatInstance(String trackTemplate) {
        int i = TrackConfiguration.stringTemplate2Int(trackTemplate);

        return (flatTrackConfigurations.get(i));
    }

    private static ArrayList<TrackConfiguration> setupConfigurations() {
        ArrayList<TrackConfiguration> configurations = new ArrayList<TrackConfiguration>(512);

        for (int i = 0; i < 512; i++) {
            configurations.add(i, new TrackConfiguration(i));
        }

        return configurations;
    }

    public static int stringTemplate2Int(String templateString) {
        //Hack - so that result is as expected by earlier written code.
        StringBuffer strb = new StringBuffer(templateString);
        strb = strb.reverse();
        templateString = strb.toString();

        //End of hack
        return Integer.parseInt(templateString, 2);
    }

    /**
     * @return the TrackConfiguration representing the track section c minus
     * the track sections represented by v.
     */
    public static TrackConfiguration subtract(FlatTrackTemplate c,
        FlatTrackTemplate v) {
        /*
        int x=v.getX()+1;
        int y=v.getY()+1;
        int oldTemplate =c.getTrackGraphicsNumber();
         int newTemplate = oldTemplate ^ (1 << (3 * y + x));
         */
        int newTemplate = c.get9bitTemplate() & (~v.get9bitTemplate());

        return from9bitTemplate(newTemplate);
    }
    private final int m_length;

    private final int m_configuration;

    private TrackConfiguration(int configuration) {
        m_configuration = configuration;

        //Calculate length.
        int tempLength = 0;
        OneTileMoveVector[] vectors = OneTileMoveVector.getList();

        for (int i = 0; i < vectors.length; i++) {
            if (this.contains(vectors[i].get9bitTemplate())) {
                tempLength += vectors[i].getLength();
            }
        }

        m_length = tempLength;
    }

    public boolean contains(FlatTrackTemplate ftt) {
        int trackTemplate = ftt.get9bitTemplate();

        return contains(trackTemplate);
    }

    public boolean contains(int trackTemplate) {
        if ((trackTemplate | this.m_configuration) == this.m_configuration) {
            return true;
        }
		return false;
    }

    public boolean equals(Object o) {
        return o == this;
    }

    public int get8bitTemplate() {
        int newTemplate = 0;
        OneTileMoveVector[] vectors = OneTileMoveVector.getList();

        for (int i = 0; i < vectors.length; i++) {
            if (this.contains(vectors[i])) {
                newTemplate = newTemplate | vectors[i].get8bitTemplate();
            }
        }

        return newTemplate;
    }

    /**
     * @return an int representing this track configuration.
     */
    public int get9bitTemplate() {
        return m_configuration;
    }

    /** Returns the length of track used in this configuration.  Used to
     * calculate the cost of building track.
     */
    public int getLength() {
        return m_length;
    }

    public Iterator getPossibleConfigurationsIterator() {
        return flatTrackConfigurations.iterator();
    }

    public int getTrackGraphicsID() {
        return m_configuration;
    }

    public int hashCode() {
        return m_configuration;
    }

    private Object readResolve() throws ObjectStreamException {
        return TrackConfiguration.from9bitTemplate(this.m_configuration);
    }

    /** Returns a String representing this configuration, for example "north, south".
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        int matches = 0;

        if (contains(TrackConfiguration.getFlatInstance("000010000"))) {
            sb.append("tile center");
        } else {
            sb.append("no tile center");
        }

        for (int i = 0; i < 8; i++) {
            OneTileMoveVector v = OneTileMoveVector.getInstance(i);

            if (contains(v)) {
                sb.append(",");
                sb.append(v);
                matches++;
            }
        }

        return sb.toString().trim();
    }
}
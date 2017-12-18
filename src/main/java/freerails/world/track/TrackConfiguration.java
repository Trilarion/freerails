package freerails.world.track;

import freerails.world.common.FlatTrackTemplate;
import freerails.world.common.Step;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * An instance of this class represents one of the possible track configurations
 * in a map square - the combinations of directions in which track can be laid.
 * Instances of this class cannot be created and must be obtained via the static
 * methods herein.
 *
 * @author Luke
 */
final public class TrackConfiguration implements FlatTrackTemplate {
    public static final int LENGTH_OF_STRAIGHT_TRACK_PIECE = 200;
    private static final long serialVersionUID = 3618695301330974512L;
    private static final ArrayList<TrackConfiguration> flatTrackConfigurations = setupConfigurations();
    private final int length;
    private final int configuration;

    private TrackConfiguration(int configuration) {
        this.configuration = configuration;

        // Calculate length.
        int tempLength = 0;
        Step[] vectors = Step.getList();

        for (Step vector : vectors) {
            if (this.contains(vector.get9bitTemplate())) {
                tempLength += vector.getLength();
            }
        }

        length = tempLength;
    }

    /**
     * @return the superposition of two track templates
     */
    public static TrackConfiguration add(FlatTrackTemplate c,
                                         FlatTrackTemplate v) {
        /*
         * int x=v.getX()+1; int y=v.getY()+1; int oldTemplate
         * =c.getTrackGraphicsNumber(); int newTemplate = oldTemplate | (1 <<
         * (3 * y + x));
         */
        int newTemplate = c.get9bitTemplate() | v.get9bitTemplate();

        return from9bitTemplate(newTemplate);
    }

    public static TrackConfiguration from9bitTemplate(int i) {
        return flatTrackConfigurations.get(i);
    }

    public static TrackConfiguration getFlatInstance(Step v) {
        return from9bitTemplate(v.get9bitTemplate());
    }

    public static TrackConfiguration getFlatInstance(String trackTemplate) {
        int i = TrackConfiguration.stringTemplate2Int(trackTemplate);

        return (flatTrackConfigurations.get(i));
    }

    private static ArrayList<TrackConfiguration> setupConfigurations() {
        ArrayList<TrackConfiguration> configurations = new ArrayList<>(
                512);

        for (int i = 0; i < 512; i++) {
            configurations.add(i, new TrackConfiguration(i));
        }

        return configurations;
    }

    public static int stringTemplate2Int(String templateString) {
        // Hack - so that result is as expected by earlier written code.
        StringBuffer strb = new StringBuffer(templateString);
        strb = strb.reverse();
        templateString = strb.toString();

        // End of hack
        return Integer.parseInt(templateString, 2);
    }

    /**
     * @return the TrackConfiguration representing the track section c minus the
     * track sections represented by v.
     */
    public static TrackConfiguration subtract(FlatTrackTemplate c,
                                              FlatTrackTemplate v) {
        /*
         * int x=v.getX()+1; int y=v.getY()+1; int oldTemplate
         * =c.getTrackGraphicsNumber(); int newTemplate = oldTemplate ^ (1 <<
         * (3 * y + x));
         */
        int newTemplate = c.get9bitTemplate() & (~v.get9bitTemplate());

        return from9bitTemplate(newTemplate);
    }

    public boolean contains(FlatTrackTemplate ftt) {
        int trackTemplate = ftt.get9bitTemplate();

        return contains(trackTemplate);
    }

    public boolean contains(int trackTemplate) {
        return (trackTemplate | this.configuration) == this.configuration;
    }

    public int get8bitTemplate() {
        int newTemplate = 0;
        Step[] vectors = Step.getList();

        for (Step vector : vectors) {
            if (this.contains(vector)) {
                newTemplate = newTemplate | vector.get8bitTemplate();
            }
        }

        return newTemplate;
    }

    /**
     * @return an int representing this track configuration.
     */
    public int get9bitTemplate() {
        return configuration;
    }

    /**
     * Returns the length of track used in this configuration. Used to calculate
     * the cost of building track.
     */
    public int getLength() {
        return length;
    }

    public Iterator getPossibleConfigurationsIterator() {
        return flatTrackConfigurations.iterator();
    }

    public int getTrackGraphicsID() {
        return configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final TrackConfiguration that = (TrackConfiguration) o;

        return configuration == that.configuration;
    }

    @Override
    public int hashCode() {
        return configuration;
    }

    private Object readResolve() throws ObjectStreamException {
        return TrackConfiguration.from9bitTemplate(this.configuration);
    }

    /**
     * Returns a String representing this configuration, for example "north,
     * south".
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int matches = 0;

        if (contains(TrackConfiguration.getFlatInstance("000010000"))) {
            sb.append("tile center");
        } else {
            sb.append("no tile center");
        }

        for (int i = 0; i < 8; i++) {
            Step v = Step.getInstance(i);

            if (contains(v)) {
                sb.append(",");
                sb.append(v);
                matches++;
            }
        }

        return sb.toString().trim();
    }
}
package freerails.world.track;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImHashSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Stores the legal track configurations for a type of track.
 *
 * @author Luke.
 */
final public class LegalTrackConfigurations implements FreerailsSerializable {

    private static final long serialVersionUID = 3617295631735928119L;

    private final ImHashSet<TrackConfiguration> legalConfigs;// = new
    // HashSet<TrackConfiguration>();

    private final int maximumConsecutivePieces;

    /**
     *
     * @param max
     * @param legalTrackTemplatesArrayList
     */
    public LegalTrackConfigurations(int max,
                                    ArrayList<String> legalTrackTemplatesArrayList) {
        maximumConsecutivePieces = max;

        HashSet<TrackConfiguration> temp = new HashSet<>();
        // Iterate over the track templates.
        for (String trackTemplateString : legalTrackTemplatesArrayList) {
            processTemplate(trackTemplateString, temp);
        }
        legalConfigs = new ImHashSet<>(temp);
    }

    /**
     *
     * @param max
     * @param legalTrackTemplatesArray
     */
    public LegalTrackConfigurations(int max, String[] legalTrackTemplatesArray) {
        maximumConsecutivePieces = max;
        HashSet<TrackConfiguration> temp = new HashSet<>();
        for (String aLegalTrackTemplatesArray : legalTrackTemplatesArray) {
            processTemplate(aLegalTrackTemplatesArray, temp);
        }
        legalConfigs = new ImHashSet<>(temp);
    }

    static private void processTemplate(String trackTemplateString,
                                        HashSet<TrackConfiguration> temp) {
        int trackTemplate = Integer.parseInt(trackTemplateString, 2);

        // Check for invalid parameters.
        if ((trackTemplate > 511) || (trackTemplate < 0)) {
            throw new IllegalArgumentException("trackTemplate = "
                    + trackTemplate + ", it should be in the range 0-511");
        }

        int[] rotationsOfTrackTemplate = EightRotationsOfTrackPieceProducer
                .getRotations(trackTemplate);

        for (int i : rotationsOfTrackTemplate) {
            TrackConfiguration trackConfiguration = TrackConfiguration
                    .from9bitTemplate(i);

            if (!temp.contains(trackConfiguration)) {
                temp.add(trackConfiguration);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LegalTrackConfigurations) {
            LegalTrackConfigurations test = (LegalTrackConfigurations) o;

            return this.maximumConsecutivePieces == test
                    .getMaximumConsecutivePieces()
                    && this.legalConfigs.equals(test.legalConfigs);
        }
        return false;
    }

    /**
     *
     * @return
     */
    public Iterator<TrackConfiguration> getLegalConfigurationsIterator() {
        return legalConfigs.iterator();
    }

    /**
     *
     * @return
     */
    public int getMaximumConsecutivePieces() {
        return maximumConsecutivePieces;
    }

    @Override
    public int hashCode() {
        int result;
        result = maximumConsecutivePieces;
        result = 29 * result
                + (legalConfigs != null ? legalConfigs.hashCode() : 0);

        return result;
    }

    /**
     *
     * @param trackConfiguration
     * @return
     */
    public boolean trackConfigurationIsLegal(
            TrackConfiguration trackConfiguration) {
        return legalConfigs.contains(trackConfiguration);
    }
}
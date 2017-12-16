package freerails.world.track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImHashSet;

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

    public LegalTrackConfigurations(int max,
            ArrayList<String> legalTrackTemplatesArrayList) {
        maximumConsecutivePieces = max;

        HashSet<TrackConfiguration> temp = new HashSet<TrackConfiguration>();
        // Iterate over the track templates.
        for (int i = 0; i < legalTrackTemplatesArrayList.size(); i++) {
            String trackTemplateString = legalTrackTemplatesArrayList.get(i);
            processTemplate(trackTemplateString, temp);
        }
        legalConfigs = new ImHashSet<TrackConfiguration>(temp);
    }

    public LegalTrackConfigurations(int max, String[] legalTrackTemplatesArray) {
        maximumConsecutivePieces = max;
        HashSet<TrackConfiguration> temp = new HashSet<TrackConfiguration>();
        for (int i = 0; i < legalTrackTemplatesArray.length; i++) {
            processTemplate(legalTrackTemplatesArray[i], temp);
        }
        legalConfigs = new ImHashSet<TrackConfiguration>(temp);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LegalTrackConfigurations) {
            LegalTrackConfigurations test = (LegalTrackConfigurations) o;

            if (this.maximumConsecutivePieces == test
                    .getMaximumConsecutivePieces()
                    && this.legalConfigs.equals(test.legalConfigs)) {
                return true;
            }
            return false;
        }
        return false;
    }

    public Iterator<TrackConfiguration> getLegalConfigurationsIterator() {
        return legalConfigs.iterator();
    }

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

        for (int k = 0; k < rotationsOfTrackTemplate.length; k++) {
            int i = rotationsOfTrackTemplate[k];
            TrackConfiguration trackConfiguration = TrackConfiguration
                    .from9bitTemplate(i);

            if (!temp.contains(trackConfiguration)) {
                temp.add(trackConfiguration);
            }
        }
    }

    public boolean trackConfigurationIsLegal(
            TrackConfiguration trackConfiguration) {
        return legalConfigs.contains(trackConfiguration);
    }
}
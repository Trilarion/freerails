package jfreerails.world.track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import jfreerails.world.common.FreerailsSerializable;


/** Stores the legal track configurations for a type of track.
 * @author Luke.
 */
final public class LegalTrackConfigurations implements FreerailsSerializable {

    /** We tell ConstJava that this field is mutable because HashSet is not annotated.*/
    private final /*=mutable*/ HashSet<TrackConfiguration> legalConfigs = new HashSet<TrackConfiguration>();

    private final int maximumConsecutivePieces;

    public LegalTrackConfigurations(int max,
        ArrayList legalTrackTemplatesArrayList) {
        maximumConsecutivePieces = max;

        //Iterate over the track templates.
        for (int i = 0; i < legalTrackTemplatesArrayList.size(); i++) {
            String trackTemplateString = (String)(legalTrackTemplatesArrayList.get(i));
            processTemplate(trackTemplateString);
        }
    }

    public LegalTrackConfigurations(int max, String[] legalTrackTemplatesArray) {
        maximumConsecutivePieces = max;

        for (int i = 0; i < legalTrackTemplatesArray.length; i++) {
            processTemplate(legalTrackTemplatesArray[i]);
        }
    }

    public boolean equals(Object o) {
        if (o instanceof LegalTrackConfigurations) {
            LegalTrackConfigurations test = (LegalTrackConfigurations)o;

            if (this.maximumConsecutivePieces == test.getMaximumConsecutivePieces() &&
                    this.legalConfigs.equals(
                        test.legalConfigs)) {
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

    public int hashCode() {
        int result;
        result = maximumConsecutivePieces;
        result = 29 * result +
            (legalConfigs != null
            ? legalConfigs.hashCode() : 0);

        return result;
    }

    private void processTemplate(String trackTemplateString) {
        int trackTemplate = Integer.parseInt(trackTemplateString, 2);

        //  Check for invalid parameters.
        if ((trackTemplate > 511) || (trackTemplate < 0)) {
            throw new IllegalArgumentException("trackTemplate = " +
                trackTemplate + ", it should be in the range 0-511");
        }

        int[] rotationsOfTrackTemplate = EightRotationsOfTrackPieceProducer.getRotations(trackTemplate);

        for (int k = 0; k < rotationsOfTrackTemplate.length; k++) {
            int i = rotationsOfTrackTemplate[k];
            TrackConfiguration trackConfiguration = TrackConfiguration.from9bitTemplate(i);

            if (!legalConfigs.contains(trackConfiguration)) {
                legalConfigs.add(trackConfiguration);
            }
        }
    }

    public boolean trackConfigurationIsLegal(
        TrackConfiguration trackConfiguration) {
        return legalConfigs.contains(trackConfiguration);
    }
}
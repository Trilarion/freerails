package jfreerails.world.track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import jfreerails.world.common.FreerailsSerializable;

final public class LegalTrackConfigurations implements FreerailsSerializable {

	public int getMaximumConsecutivePieces() {
		return maximumConsecutivePieces;
	}

	private final int maximumConsecutivePieces;

	//final private int[][] legalRoutesAcrossNodeTemplates;

	//private final boolean[] legalTrackTemplates = new boolean[512];

	/**
	 *  TrackConfiguration
	 */
	private final HashSet legalTrackConfigurationsHashSet = new HashSet();

	public LegalTrackConfigurations(int max, ArrayList legalTrackTemplatesArrayList) {
		maximumConsecutivePieces = max;

		//Iterate over the track templates.
		for (int i = 0; i < legalTrackTemplatesArrayList.size(); i++) {
			String trackTemplateString = (String) (legalTrackTemplatesArrayList.get(i));
			processTemplate(trackTemplateString);
		}
	}

	public LegalTrackConfigurations(int max, String[] legalTrackTemplatesArray) {
		maximumConsecutivePieces = max;
		for (int i = 0; i < legalTrackTemplatesArray.length; i++) {
			processTemplate(legalTrackTemplatesArray[i]);
		}
	}

	private void processTemplate(String trackTemplateString) {
		int trackTemplate = (int) Integer.parseInt(trackTemplateString, 2);

		//  Check for invalid parameters.
		if ((trackTemplate > 511) || (trackTemplate < 0)) {
			throw new IllegalArgumentException(
				"trackTemplate = " + trackTemplate + ", it should be in the range 0-511");
		}

		int[] rotationsOfTrackTemplate =
			EightRotationsOfTrackPieceProducer.getRotations(trackTemplate);
		for (int k = 0; k < rotationsOfTrackTemplate.length; k++) {
			TrackConfiguration trackConfiguration =
				TrackConfiguration.getFlatInstance(rotationsOfTrackTemplate[k]);
			if (!legalTrackConfigurationsHashSet.contains(trackConfiguration)) {
				legalTrackConfigurationsHashSet.add(trackConfiguration);
			}
		}
	}

	public boolean trackConfigurationIsLegal(TrackConfiguration trackConfiguration) {
		return legalTrackConfigurationsHashSet.contains(trackConfiguration);
	}

	public static int stringTemplate2Int(String templateString) {
		//Hack - so that result is as expected by earlier written code.
		StringBuffer strb = new StringBuffer(templateString);
		strb = strb.reverse();
		templateString = strb.toString();
		//End of hack

		return (int) Integer.parseInt(templateString, 2);

	}
	public Iterator getLegalConfigurationsIterator() {
		return legalTrackConfigurationsHashSet.iterator();
	}
	public boolean equals(Object o) {
		if (o instanceof LegalTrackConfigurations) {
			LegalTrackConfigurations test = (LegalTrackConfigurations) o;
			if (this.maximumConsecutivePieces == test.getMaximumConsecutivePieces()
				&& this.legalTrackConfigurationsHashSet.equals(
					test.getLegalTrackConfigurationsHashSet())) {
				return true;
			} else {
				return false;
			}
		}else{
			return false;
		}
	}

	public HashSet getLegalTrackConfigurationsHashSet() {
		return legalTrackConfigurationsHashSet;
	}

}
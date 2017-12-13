/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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

    private void processTemplate(String trackTemplateString) {
        int trackTemplate = (int)Integer.parseInt(trackTemplateString, 2);

        //  Check for invalid parameters.
        if ((trackTemplate > 511) || (trackTemplate < 0)) {
            throw new IllegalArgumentException("trackTemplate = " +
                trackTemplate + ", it should be in the range 0-511");
        }

        int[] rotationsOfTrackTemplate = EightRotationsOfTrackPieceProducer.getRotations(trackTemplate);

        for (int k = 0; k < rotationsOfTrackTemplate.length; k++) {
            TrackConfiguration trackConfiguration = TrackConfiguration.getFlatInstance(rotationsOfTrackTemplate[k]);

            if (!legalTrackConfigurationsHashSet.contains(trackConfiguration)) {
                legalTrackConfigurationsHashSet.add(trackConfiguration);
            }
        }
    }

    public boolean trackConfigurationIsLegal(
        TrackConfiguration trackConfiguration) {
        return legalTrackConfigurationsHashSet.contains(trackConfiguration);
    }

    public Iterator getLegalConfigurationsIterator() {
        return legalTrackConfigurationsHashSet.iterator();
    }

    public boolean equals(Object o) {
        if (o instanceof LegalTrackConfigurations) {
            LegalTrackConfigurations test = (LegalTrackConfigurations)o;

            if (this.maximumConsecutivePieces == test.getMaximumConsecutivePieces() &&
                    this.legalTrackConfigurationsHashSet.equals(
                        test.getLegalTrackConfigurationsHashSet())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public HashSet getLegalTrackConfigurationsHashSet() {
        return legalTrackConfigurationsHashSet;
    }
}
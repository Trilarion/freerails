/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.world.track;

import java.io.Serializable;
import java.util.*;

/**
 * Stores the legal track configurations for a type of track.
 */
public final class ValidTrackConfigurations implements Serializable {

    private static final long serialVersionUID = 3617295631735928119L;
    private final Set<TrackConfiguration> legalTrackConfigurations;
    private final int maximumConsecutivePieces;

    /**
     * @param max
     * @param legalTrackTemplateStrings
     */
    public ValidTrackConfigurations(int max, Iterable<String> legalTrackTemplateStrings) {
        maximumConsecutivePieces = max;

        Set<TrackConfiguration> trackConfigurations = new HashSet<>();
        // Iterate over the track templates.
        for (String trackTemplateString : legalTrackTemplateStrings) {
            processTemplate(trackTemplateString, trackConfigurations);
        }
        legalTrackConfigurations = Collections.unmodifiableSet(trackConfigurations);
    }

    /**
     * @param max
     * @param legalTrackTemplatesArray
     */
    public ValidTrackConfigurations(int max, String[] legalTrackTemplatesArray) {
        maximumConsecutivePieces = max;
        Set<TrackConfiguration> temp = new HashSet<>();
        for (String aLegalTrackTemplatesArray : legalTrackTemplatesArray) {
            processTemplate(aLegalTrackTemplatesArray, temp);
        }
        legalTrackConfigurations = Collections.unmodifiableSet(temp);
    }

    private static void processTemplate(String trackTemplateString, Collection<TrackConfiguration> temp) {
        int trackTemplate = Integer.parseInt(trackTemplateString, 2);

        // Check for invalid parameters.
        if ((trackTemplate > 511) || (trackTemplate < 0)) {
            throw new IllegalArgumentException("trackTemplate = " + trackTemplate + ", it should be in the range 0-511");
        }

        int[] rotationsOfTrackTemplate = EightRotationsOfTrackPieceProducer.getRotations(trackTemplate);

        for (int i : rotationsOfTrackTemplate) {
            TrackConfiguration trackConfiguration = TrackConfiguration.from9bitTemplate(i);

            if (!temp.contains(trackConfiguration)) {
                temp.add(trackConfiguration);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ValidTrackConfigurations) {
            ValidTrackConfigurations test = (ValidTrackConfigurations) obj;

            return maximumConsecutivePieces == test.maximumConsecutivePieces && legalTrackConfigurations.equals(test.legalTrackConfigurations);
        }
        return false;
    }

    /**
     * @return
     */
    public Iterator<TrackConfiguration> getLegalConfigurationsIterator() {
        return legalTrackConfigurations.iterator();
    }

    @Override
    public int hashCode() {
        int result;
        result = maximumConsecutivePieces;
        result = 29 * result + (legalTrackConfigurations != null ? legalTrackConfigurations.hashCode() : 0);

        return result;
    }

    /**
     * @param trackConfiguration
     * @return
     */
    public boolean trackConfigurationIsLegal(TrackConfiguration trackConfiguration) {
        return legalTrackConfigurations.contains(trackConfiguration);
    }
}
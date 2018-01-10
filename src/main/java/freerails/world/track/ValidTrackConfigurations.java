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

import freerails.util.ImHashSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Stores the legal track configurations for a type of track.
 */
public final class ValidTrackConfigurations implements Serializable {

    private static final long serialVersionUID = 3617295631735928119L;
    private final ImHashSet<TrackConfiguration> legalConfigs;
    private final int maximumConsecutivePieces;

    /**
     * @param max
     * @param legalTrackTemplatesArrayList
     */
    public ValidTrackConfigurations(int max,
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
     * @param max
     * @param legalTrackTemplatesArray
     */
    public ValidTrackConfigurations(int max, String[] legalTrackTemplatesArray) {
        maximumConsecutivePieces = max;
        HashSet<TrackConfiguration> temp = new HashSet<>();
        for (String aLegalTrackTemplatesArray : legalTrackTemplatesArray) {
            processTemplate(aLegalTrackTemplatesArray, temp);
        }
        legalConfigs = new ImHashSet<>(temp);
    }

    private static void processTemplate(String trackTemplateString,
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
        if (o instanceof ValidTrackConfigurations) {
            ValidTrackConfigurations test = (ValidTrackConfigurations) o;

            return maximumConsecutivePieces == test.maximumConsecutivePieces
                    && legalConfigs.equals(test.legalConfigs);
        }
        return false;
    }

    /**
     * @return
     */
    public Iterator<TrackConfiguration> getLegalConfigurationsIterator() {
        return legalConfigs.iterator();
    }

    /**
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
     * @param trackConfiguration
     * @return
     */
    public boolean trackConfigurationIsLegal(
            TrackConfiguration trackConfiguration) {
        return legalConfigs.contains(trackConfiguration);
    }
}
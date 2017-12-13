/*
 * Copyright (C) 2001 Luke Lindsay
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

package org.railz.world.track;

import java.util.Arrays;
import java.util.Iterator;
import org.railz.world.common.*;

/**
 * Describes a particular type of track (eg stone bridge, standard track,
 * tunnel.
 *  This class encapsulates the rules that apply to a type of track node.
 *  They concern: the legal routes trains can travel across the node, whether
 *  the node's track can be doubled, on which terrain types it can be built,
 *  and the maximum number of consecutive nodes of this type (used for bridges
 *  and tunnels).
 *
 * @author     Luke Lindsay
 *     09 October 2001
 */
public class TrackRule implements FreerailsSerializable {
    private final boolean isDoubleTrack;
    private final boolean buildPermissions[];
    private final String name;
    private final long price;
    private final long maintenanceCost;

    /**
     * Array describing the allowed track configurations. true indicates the
     * configuration is allowed.
     */
    private final boolean legalConfigurations[] = new boolean[256];

    private final int maxConsecutivePieces;
    /**
     * @param the name of this track type.
     * @param isDoubleTrack whether this track is twin-track (permits travel
     * in both directions simultaneously)
     * @param maxConsecutivePieces this is the maximum number of consecutive
     * pieces of track that may exist in a stretch. This may be limited, e.g.
     * in the case of bridges. 0 indicates that the number of pieces is
     * unlimited.
     * @param lc an array containing the non-isomorphic allowed track layouts.
     * @param maintenanceCost the annual maintenance charge.
     * @param price the price of building a unit of this track.
     * @param buildPermissions array of booleans indicating the terrain
     * categories that may be built on.
     */
    public TrackRule(long price, String name, boolean isDoubleTrack, long
	    maintenanceCost, byte[] lc, int maxConsecutivePieces, boolean[]
	    buildPermissions) {
        if (null == lc || null == buildPermissions) {
            throw new java.lang.IllegalArgumentException();
        }

	for (int i = 0; i < lc.length; i++) {
	    int b = ((int) lc[i]) & 0xFF;
	    for (int j = 0; j < 8; j++) {
		legalConfigurations[b] = true;
		b = (int) (CompassPoints.rotateClockwise((byte) b) & 0xFF);
	    }
	}
	this.maxConsecutivePieces = maxConsecutivePieces;
	this.buildPermissions = buildPermissions;
	this.maintenanceCost = maintenanceCost;
	this.isDoubleTrack = isDoubleTrack;
	this.name = name;
	this.price = price;
    }

    public boolean testTrackPieceLegality(byte trackTemplateToTest) {
	return legalConfigurations[((int) trackTemplateToTest) & 0xFF];
    }

    public boolean canBuildOnThisTerrainType(int terrainCategory) {
        return buildPermissions[terrainCategory];
    }

    public String toString() {
        return name;
    }

    public int getMaximumConsecutivePieces() {
        return maxConsecutivePieces;
    }

    public boolean equals(Object o) {
        if (o instanceof TrackRule) {
            TrackRule trackRule = (TrackRule)o;
            boolean legalConfigurationsEqual =
		Arrays.equals(legalConfigurations,
			trackRule.legalConfigurations) &&
		maxConsecutivePieces == trackRule.maxConsecutivePieces;
	    boolean legalTrackPlacementEqual = Arrays.equals(buildPermissions,
		    trackRule.buildPermissions);

            return legalConfigurationsEqual &&
		legalTrackPlacementEqual;
        } else {
            return false;
        }
    }

    public long getPrice() {
        return price;
    }

    public long getMaintenanceCost() {
        return maintenanceCost;
    }

    public boolean isDoubleTrack() {
	return isDoubleTrack;
    }
}

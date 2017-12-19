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

/*
 * LegalTrackPlacement.java
 *
 * Created on 22 January 2002, 10:20
 */
package freerails.world.track;

import freerails.util.ImHashSet;
import freerails.world.terrain.TerrainCategory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This class encapsulates the rules governing where, that is, on what terrain,
 * track of a given type can be built.
 */
public final class LegalTrackPlacement implements Serializable {
    private static final long serialVersionUID = 3616445687756437049L;

    private final ImHashSet<TerrainCategory> terrainTypes;// = new

    // HashSet<TerrainType.Category>();
    private final PlacementRule placementRule;

    /**
     * @param types
     * @param placementRule
     */
    public LegalTrackPlacement(HashSet<TerrainCategory> types,
                               PlacementRule placementRule) {
        this.placementRule = placementRule;

        Iterator<TerrainCategory> iterator = types.iterator();

        HashSet<TerrainCategory> temp = new HashSet<>();
        while (iterator.hasNext()) {
            temp.add(iterator.next());
        }
        terrainTypes = new ImHashSet<>(temp);
    }

    @Override
    public int hashCode() {
        return (placementRule != null ? placementRule.hashCode() : 0);
    }

    /**
     * @param terrainType
     * @return
     */
    public boolean canBuildOnThisTerrain(TerrainCategory terrainType) {
        if (PlacementRule.ONLY_ON_THESE == placementRule) {
            return terrainTypes.contains(terrainType);
        }
        return !terrainTypes.contains(terrainType);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LegalTrackPlacement) {
            LegalTrackPlacement test = (LegalTrackPlacement) o;

            return this.placementRule.equals(test.getPlacementRule())
                    && this.terrainTypes.equals(test.terrainTypes);
        }
        return false;
    }

    /**
     * @return
     */
    public PlacementRule getPlacementRule() {
        return placementRule;
    }

    /**
     *
     */
    public enum PlacementRule {

        /**
         *
         */
        ONLY_ON_THESE,

        /**
         *
         */
        ANYWHERE_EXCEPT_ON_THESE
    }
}
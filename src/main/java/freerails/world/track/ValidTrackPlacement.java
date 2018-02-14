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

import freerails.world.terrain.TerrainCategory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Encapsulates the rules governing where, that is, on what terrain,
 * track of a given type can be built.
 */
public class ValidTrackPlacement implements Serializable {

    private static final long serialVersionUID = 3616445687756437049L;
    private final Set<TerrainCategory> terrainTypes;
    private final PlacementRule placementRule;

    /**
     * @param types
     * @param placementRule
     */
    public ValidTrackPlacement(Iterable<TerrainCategory> types, PlacementRule placementRule) {

        this.placementRule = placementRule;
        Iterator<TerrainCategory> iterator = types.iterator();
        Set<TerrainCategory> temp = new HashSet<>();
        while (iterator.hasNext()) {
            temp.add(iterator.next());
        }
        terrainTypes = Collections.unmodifiableSet(temp);
    }

    @Override
    public int hashCode() {
        return placementRule != null ? placementRule.hashCode() : 0;
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
    public boolean equals(Object obj) {
        if (obj instanceof ValidTrackPlacement) {
            ValidTrackPlacement test = (ValidTrackPlacement) obj;

            return placementRule == test.placementRule && terrainTypes.equals(test.terrainTypes);
        }
        return false;
    }

}
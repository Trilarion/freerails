/*
 * LegalTrackPlacement.java
 *
 * Created on 22 January 2002, 10:20
 */
package freerails.world.track;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImHashSet;
import freerails.world.terrain.TerrainType;

import java.util.HashSet;
import java.util.Iterator;

/**
 * This class encapsulates the rules governing where, that is, on what terrain,
 * track of a given type can be built.
 *
 * @author lindsal
 */
public final class LegalTrackPlacement implements FreerailsSerializable {
    private static final long serialVersionUID = 3616445687756437049L;

    private final ImHashSet<TerrainType.Category> terrainTypes;// = new

    // HashSet<TerrainType.Category>();
    private final PlacementRule placementRule;

    /**
     *
     * @param types
     * @param placementRule
     */
    public LegalTrackPlacement(HashSet<TerrainType.Category> types,
                               PlacementRule placementRule) {
        this.placementRule = placementRule;

        Iterator<TerrainType.Category> iterator = types.iterator();

        HashSet<TerrainType.Category> temp = new HashSet<>();
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
     *
     * @param terrainType
     * @return
     */
    public boolean canBuildOnThisTerrain(TerrainType.Category terrainType) {
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
     *
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
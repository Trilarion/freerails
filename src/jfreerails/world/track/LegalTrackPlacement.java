/*
 * LegalTrackPlacement.java
 *
 * Created on 22 January 2002, 10:20
 */
package jfreerails.world.track;

import java.io.ObjectStreamException;
import java.util.HashSet;
import java.util.Iterator;
import jfreerails.world.common.FreerailsSerializable;


/**
 * This class encapsulates the rules governing where, that is, on what terrain, track
 * of a given type can be built.
 *
 * @author  lindsal
 */
public final class LegalTrackPlacement implements FreerailsSerializable {
    private final HashSet terrainTypes = new HashSet();
    private final PlacementRule placementRule;

    public int hashCode() {
        return (placementRule != null ? placementRule.hashCode() : 0);
    }

    public LegalTrackPlacement(HashSet types, PlacementRule placementRule) {
        this.placementRule = placementRule;

        Iterator iterator = types.iterator();

        while (iterator.hasNext()) {
            String typeName = (String)(iterator.next());
            terrainTypes.add(typeName);
        }
    }

    public boolean canBuildOnThisTerrain(String terrainType) {
        if (PlacementRule.ONLY_ON_THESE == placementRule) {
            return terrainTypes.contains(terrainType);
        } else {
            return !terrainTypes.contains(terrainType);
        }
    }

    final public static class PlacementRule implements FreerailsSerializable {
        private final int i;

        private PlacementRule(int i) {
            this.i = i;
        }

        private Object readResolve() throws ObjectStreamException {
            if (i == 1) {
                return ONLY_ON_THESE;
            } else {
                return ANYWHERE_EXCEPT_ON_THESE;
            }
        }

        public static final PlacementRule ONLY_ON_THESE = new PlacementRule(1);
        public static final PlacementRule ANYWHERE_EXCEPT_ON_THESE = new PlacementRule(2);
    }

    public boolean equals(Object o) {
        if (o instanceof LegalTrackPlacement) {
            LegalTrackPlacement test = (LegalTrackPlacement)o;

            if (this.placementRule.equals(test.getPlacementRule()) &&
                    this.getTerrainTypes().equals(test.getTerrainTypes())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public PlacementRule getPlacementRule() {
        return placementRule;
    }

    public HashSet getTerrainTypes() {
        return terrainTypes;
    }
}
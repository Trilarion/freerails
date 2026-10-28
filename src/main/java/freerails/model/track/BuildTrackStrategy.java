/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
 * BuildTrackStrategy.java
 *
 */

package freerails.model.track;

import freerails.model.terrain.Terrain;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps terrain IDs to the track type to use when constructing or upgrading track.
 */
public class BuildTrackStrategy {

    /**
     * Map TerrainType.Id -> Rule
     */
    private final Map<Integer, Integer> rules;

    /**
     * Creates a strategy backed by the supplied terrain-to-track rules.
     *
     * @param rules mapping from terrain ID to track type ID
     */
    private BuildTrackStrategy(Map<Integer, Integer> rules) {
        this.rules = Utils.verifyNotNull(rules);
    }

    /**
     * Creates a strategy that selects the same track type on every terrain.
     *
     * @param trackTypeID track type ID to select
     * @param world world supplying the available terrains
     * @return a strategy with one rule for each terrain in {@code world}
     */
    public static BuildTrackStrategy getSingleRuleInstance(int trackTypeID, UnmodifiableWorld world) {
        Map<Integer, Integer> rules = new HashMap<>();
        for (Terrain terrain: world.getTerrains()) {
            rules.put(terrain.getId(), trackTypeID);
        }

        return new BuildTrackStrategy(rules);
    }

    /**
     * Creates a strategy by applying the supplied ordered track-type rules to
     * each terrain where a type is permitted.
     *
     * @param ruleIDs track type IDs to consider, in preference order
     * @param world world supplying terrains and track types
     * @return the generated terrain-to-track strategy
     */
    public static BuildTrackStrategy getMultipleRuleInstance(Iterable<Integer> ruleIDs, UnmodifiableWorld world) {
        Map<Integer, Integer> rules = TrackUtils.generateRules(ruleIDs, world);
        return new BuildTrackStrategy(rules);
    }

    /**
     * Creates the default strategy using the cheapest track, bridge, and tunnel
     * types available in the world.
     *
     * @param world world supplying terrains and track types
     * @return a strategy using the default least-cost choices for each terrain
     */
    public static BuildTrackStrategy getDefault(UnmodifiableWorld world) {
        Collection<Integer> allowable = new ArrayList<>();
        allowable.add(TrackUtils.getCheapest(TrackCategory.TRACK, world));
        allowable.add(TrackUtils.getCheapest(TrackCategory.BRIDGE, world));
        allowable.add(TrackUtils.getCheapest(TrackCategory.TUNNEL, world));
        return new BuildTrackStrategy(TrackUtils.generateRules(allowable, world));
    }

    /**
     * Returns the selected track type for a terrain.
     *
     * @param terrainId terrain ID whose rule is requested
     * @return the selected track type ID, or {@code -1} when no rule exists
     */
    public int getRule(int terrainId) {
        return rules.getOrDefault(terrainId, -1);
    }

}

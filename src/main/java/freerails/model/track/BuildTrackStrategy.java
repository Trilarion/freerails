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
 * A BuildTrackStrategy determines which track types to build (or upgrade to) on different terrains.
 */
public class BuildTrackStrategy {

    /**
     * Map TerrainType.Id -> Rule
     */
    private final Map<Integer, Integer> rules;

    /**
     * Creates a new instance of BuildTrackStrategy
     */
    private BuildTrackStrategy(Map<Integer, Integer> rules) {
        this.rules = Utils.verifyNotNull(rules);
    }

    /**
     * @param trackTypeID
     * @param world
     * @return
     */
    public static BuildTrackStrategy getSingleRuleInstance(int trackTypeID, UnmodifiableWorld world) {
        Map<Integer, Integer> rules = new HashMap<>();
        for (Terrain terrain: world.getTerrains()) {
            rules.put(terrain.getId(), trackTypeID);
        }

        return new BuildTrackStrategy(rules);
    }

    /**
     * @param ruleIDs
     * @param world
     * @return
     */
    public static BuildTrackStrategy getMultipleRuleInstance(Iterable<Integer> ruleIDs, UnmodifiableWorld world) {
        Map<Integer, Integer> rules = TrackUtils.generateRules(ruleIDs, world);
        return new BuildTrackStrategy(rules);
    }

    /**
     * @param world
     * @return
     */
    public static BuildTrackStrategy getDefault(UnmodifiableWorld world) {
        Collection<Integer> allowable = new ArrayList<>();
        allowable.add(TrackUtils.getCheapest(TrackCategory.TRACK, world));
        allowable.add(TrackUtils.getCheapest(TrackCategory.BRIDGE, world));
        allowable.add(TrackUtils.getCheapest(TrackCategory.TUNNEL, world));
        return new BuildTrackStrategy(TrackUtils.generateRules(allowable, world));
    }

    /**
     * @param terrainId
     * @return
     */
    public int getRule(int terrainId) {
        return rules.getOrDefault(terrainId, -1);
    }

}

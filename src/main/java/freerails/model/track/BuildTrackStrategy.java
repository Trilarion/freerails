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

import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.model.terrain.TerrainType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A BuildTrackStrategy determines which track types to build (or upgrade to) on different terrains.
 */
public class BuildTrackStrategy {

    private final int[] rules;

    /**
     * Creates a new instance of BuildTrackStrategy
     */
    private BuildTrackStrategy(int[] rules) {
        this.rules = rules;
    }

    /**
     * @param trackTypeID
     * @param world
     * @return
     */
    public static BuildTrackStrategy getSingleRuleInstance(int trackTypeID, ReadOnlyWorld world) {
        int numberTerrainTypes = world.size(SharedKey.TerrainTypes);
        int[] rules = new int[numberTerrainTypes];
        for (int i = 0; i < numberTerrainTypes; i++) {
            rules[i] = trackTypeID;
        }

        return new BuildTrackStrategy(rules);
    }

    /**
     * @param ruleIDs
     * @param world
     * @return
     */
    public static BuildTrackStrategy getMultipleRuleInstance(Iterable<Integer> ruleIDs, ReadOnlyWorld world) {
        int[] rules = generateRules(ruleIDs, world);
        return new BuildTrackStrategy(rules);
    }

    /**
     * @param world
     * @return
     */
    public static BuildTrackStrategy getDefault(ReadOnlyWorld world) {
        Collection<Integer> allowable = new ArrayList<>();
        allowable.add(getCheapest(TrackCategory.track, world));
        allowable.add(getCheapest(TrackCategory.bridge, world));
        allowable.add(getCheapest(TrackCategory.tunnel, world));
        return new BuildTrackStrategy(generateRules(allowable, world));
    }

    private static Integer getCheapest(TrackCategory category, ReadOnlyWorld world) {
        TrackRule cheapest = null;
        Integer cheapestID = null;
        for (int i = 0; i < world.size(SharedKey.TrackRules); i++) {
            TrackRule rule = (TrackRule) world.get(SharedKey.TrackRules, i);
            if (rule.getCategory() == category) {
                if (null == cheapest || cheapest.getPrice().compareTo(rule.getPrice()) > 0) {
                    cheapest = rule;
                    cheapestID = i;
                }
            }
        }
        return cheapestID;
    }

    private static int[] generateRules(Iterable<Integer> allowable, ReadOnlyWorld world) {
        int noTerrainTypes = world.size(SharedKey.TerrainTypes);
        int[] newRules = new int[noTerrainTypes];
        for (int i = 0; i < noTerrainTypes; i++) {
            TerrainType terrainType = (TerrainType) world.get(SharedKey.TerrainTypes, i);
            newRules[i] = -1; // the default value.
            for (Integer rule : allowable) {
                if (null != rule) {
                    TrackRule trackRule = (TrackRule) world.get(SharedKey.TrackRules, rule);
                    if (trackRule.canBuildOnThisTerrainType(terrainType.getCategory())) {
                        newRules[i] = rule;
                        break;
                    }
                }
            }
        }
        return newRules;
    }

    /**
     * @param terrainType
     * @return
     */
    public int getRule(int terrainType) {
        return rules[terrainType];
    }

}

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

package freerails.controller;

import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.terrain.TerrainType;
import freerails.world.track.TrackCategories;
import freerails.world.track.TrackRule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A BuildTrackStrategy determines which track types to build (or upgrade to) on
 * different terrains.
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
        int numberTerrainTypes = world.size(SKEY.TERRAIN_TYPES);
        int[] newRules = new int[numberTerrainTypes];
        for (int i = 0; i < numberTerrainTypes; i++) {
            newRules[i] = trackTypeID;
        }

        return new BuildTrackStrategy(newRules);

    }

    /**
     * @param ruleIDs
     * @param world
     * @return
     */
    public static BuildTrackStrategy getMultipleRuleInstance(Iterable<Integer> ruleIDs, ReadOnlyWorld world) {
        int[] rulesArray = generateRules(ruleIDs, world);
        return new BuildTrackStrategy(rulesArray);
    }

    /**
     * @param world
     * @return
     */
    public static BuildTrackStrategy getDefault(ReadOnlyWorld world) {
        Collection<Integer> allowable = new ArrayList<>();
        allowable.add(getCheapest(TrackCategories.track, world));
        allowable.add(getCheapest(TrackCategories.bridge, world));
        allowable.add(getCheapest(TrackCategories.tunnel, world));
        return new BuildTrackStrategy(generateRules(allowable, world));
    }

    private static Integer getCheapest(TrackCategories category, ReadOnlyWorld world) {
        TrackRule cheapest = null;
        Integer cheapestID = null;
        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            TrackRule rule = (TrackRule) world.get(SKEY.TRACK_RULES, i);
            if (rule.getCategory() == category) {
                if (null == cheapest || cheapest.getPrice().getAmount() > rule.getPrice().getAmount()) {
                    cheapest = rule;
                    cheapestID = i;
                }
            }
        }
        return cheapestID;
    }

    private static int[] generateRules(Iterable<Integer> allowable, ReadOnlyWorld world) {
        int noTerrainTypes = world.size(SKEY.TERRAIN_TYPES);
        int[] newRules = new int[noTerrainTypes];
        for (int i = 0; i < noTerrainTypes; i++) {
            TerrainType terrainType = (TerrainType) world.get(SKEY.TERRAIN_TYPES, i);
            newRules[i] = -1; // the default value.
            for (Integer rule : allowable) {
                if (null != rule) {
                    TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, rule);
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

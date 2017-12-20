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
import freerails.world.track.TrackRule;

import java.util.ArrayList;

/**
 * A BuildTrackStrategy determines which track types to build (or upgrade to) on
 * different terrains.
 */
public class BuildTrackStrategy {

    private final int[] rules;

    /**
     * Creates a new instance of BuildTrackStrategy
     */
    private BuildTrackStrategy(int[] r) {
        rules = r;
    }

    /**
     * @param trackTypeID
     * @param w
     * @return
     */
    public static BuildTrackStrategy getSingleRuleInstance(int trackTypeID,
                                                           ReadOnlyWorld w) {
        int noTerrainTypes = w.size(SKEY.TERRAIN_TYPES);
        int[] newRules = new int[noTerrainTypes];
        for (int i = 0; i < noTerrainTypes; i++) {
            newRules[i] = trackTypeID;
        }

        return new BuildTrackStrategy(newRules);

    }

    /**
     * @param ruleIDs
     * @param w
     * @return
     */
    public static BuildTrackStrategy getMultipleRuleInstance(
            ArrayList<Integer> ruleIDs, ReadOnlyWorld w) {
        int[] rulesArray = generateRules(ruleIDs, w);
        return new BuildTrackStrategy(rulesArray);
    }

    /**
     * @param w
     * @return
     */
    public static BuildTrackStrategy getDefault(ReadOnlyWorld w) {
        ArrayList<Integer> allowable = new ArrayList<>();
        allowable.add(getCheapest(TrackRule.TrackCategories.track, w));
        allowable.add(getCheapest(TrackRule.TrackCategories.bridge, w));
        allowable.add(getCheapest(TrackRule.TrackCategories.tunnel, w));
        return new BuildTrackStrategy(generateRules(allowable, w));
    }

    private static Integer getCheapest(TrackRule.TrackCategories category,
                                       ReadOnlyWorld w) {
        TrackRule cheapest = null;
        Integer cheapestID = null;
        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
            TrackRule rule = (TrackRule) w.get(SKEY.TRACK_RULES, i);
            if (rule.getCategory().equals(category)) {
                if (null == cheapest
                        || cheapest.getPrice().getAmount() > rule.getPrice()
                        .getAmount()) {
                    cheapest = rule;
                    cheapestID = i;
                }
            }
        }
        return cheapestID;
    }

    private static int[] generateRules(ArrayList<Integer> allowable,
                                       ReadOnlyWorld w) {
        int noTerrainTypes = w.size(SKEY.TERRAIN_TYPES);
        int[] newRules = new int[noTerrainTypes];
        for (int i = 0; i < noTerrainTypes; i++) {
            TerrainType terrainType = (TerrainType) w
                    .get(SKEY.TERRAIN_TYPES, i);
            newRules[i] = -1; // the default value.
            for (Integer rule : allowable) {
                if (null != rule) {
                    TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES,
                            rule);
                    if (trackRule.canBuildOnThisTerrainType(terrainType
                            .getCategory())) {
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

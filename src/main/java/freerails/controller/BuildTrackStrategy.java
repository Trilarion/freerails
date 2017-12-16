/*
 * BuildTrackStrategy.java
 *
 * Created on 13 December 2004, 22:24
 */

package freerails.controller;

import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.track.TrackRule;

import java.util.ArrayList;

/**
 * A BuildTrackStrategy determines which track types to build (or upgrade to) on
 * different terrains.
 *
 * @author Luke
 */
public class BuildTrackStrategy {

    private final int[] rules;

    public static BuildTrackStrategy getSingleRuleInstance(int trackTypeID,
                                                           ReadOnlyWorld w) {
        int noTerrainTypes = w.size(SKEY.TERRAIN_TYPES);
        int[] newRules = new int[noTerrainTypes];
        for (int i = 0; i < noTerrainTypes; i++) {
            newRules[i] = trackTypeID;
        }

        return new BuildTrackStrategy(newRules);

    }

    public static BuildTrackStrategy getMultipleRuleInstance(
            ArrayList<Integer> ruleIDs, ReadOnlyWorld w) {
        int[] rulesArray = generateRules(ruleIDs, w);
        return new BuildTrackStrategy(rulesArray);
    }

    public static BuildTrackStrategy getDefault(ReadOnlyWorld w) {
        ArrayList<Integer> allowable = new ArrayList<Integer>();
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
                    cheapestID = new Integer(i);
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
                            rule.intValue());
                    if (trackRule.canBuildOnThisTerrainType(terrainType
                            .getCategory())) {
                        newRules[i] = rule.intValue();
                        break;
                    }
                }
            }

        }
        return newRules;
    }

    /**
     * Creates a new instance of BuildTrackStrategy
     */
    private BuildTrackStrategy(int[] r) {
        rules = r;
    }

    public int getRule(int terrainType) {
        return rules[terrainType];
    }

}

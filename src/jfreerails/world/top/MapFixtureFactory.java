/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.world.top;

import java.util.HashSet;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.track.LegalTrackConfigurations;
import jfreerails.world.track.LegalTrackPlacement;
import jfreerails.world.track.TrackRule;
import jfreerails.world.track.TrackRuleImpl;
import jfreerails.world.track.TrackRuleProperties;


/** This class is used to generate fixures for Junit tests.
 *
 * @author Luke
 *
 */
public class MapFixtureFactory {
    public int w = 10;
    public int h = 10;
    public World world = new WorldImpl(w, h);

    public MapFixtureFactory() {
        generateTrackRuleList(world);
    }

    public static void generateTrackRuleList(World world) {
        TrackRule[] trackRulesArray = new TrackRule[3];
        TrackRuleProperties[] trackRuleProperties = new TrackRuleProperties[3];
        LegalTrackConfigurations[] legalTrackConfigurations = new LegalTrackConfigurations[3];
        LegalTrackPlacement[] legalTrackPlacement = new LegalTrackPlacement[3];

        //1st track type..
        String[] trackTemplates0 = {
            "000010000", "010010000", "010010010", "100111000", "001111000",
            "010110000", "100110000"
        };

        legalTrackConfigurations[0] = new LegalTrackConfigurations(-1,
                trackTemplates0);
        trackRuleProperties[0] = new TrackRuleProperties(1, false, "type0", 0,
                false, 0, 0, 10);
        legalTrackPlacement[0] = new LegalTrackPlacement(new HashSet(),
                LegalTrackPlacement.PlacementRule.ANYWHERE_EXCEPT_ON_THESE);
        trackRulesArray[0] = new TrackRuleImpl(trackRuleProperties[0],
                legalTrackConfigurations[0], legalTrackPlacement[0]);

        //2nd track type..
        String[] trackTemplates1 = {"000010000", "010010000", "010010010"};
        legalTrackConfigurations[1] = new LegalTrackConfigurations(-1,
                trackTemplates1);
        trackRuleProperties[1] = new TrackRuleProperties(2, false, "type1", 1,
                false, 0, 0, 20);

        HashSet cannotBuildOnTheseTerrainTypes = new HashSet();
        cannotBuildOnTheseTerrainTypes.add("mountain");
        legalTrackPlacement[1] = new LegalTrackPlacement(cannotBuildOnTheseTerrainTypes,
                LegalTrackPlacement.PlacementRule.ANYWHERE_EXCEPT_ON_THESE);
        trackRulesArray[1] = new TrackRuleImpl(trackRuleProperties[1],
                legalTrackConfigurations[1], legalTrackPlacement[1]);

        //3rd track type..
        trackRuleProperties[2] = new TrackRuleProperties(3, false, "type2", 2,
                false, 0, 0, 30);

        String[] trackTemplates2 = {"000010000"};
        legalTrackConfigurations[2] = new LegalTrackConfigurations(-1,
                trackTemplates2);
        legalTrackPlacement[2] = new LegalTrackPlacement(new HashSet(),
                LegalTrackPlacement.PlacementRule.ANYWHERE_EXCEPT_ON_THESE);
        trackRulesArray[2] = new TrackRuleImpl(trackRuleProperties[2],
                legalTrackConfigurations[2], legalTrackPlacement[2]);

        //Add track rules to world
        for (int i = 0; i < trackRulesArray.length; i++) {
            world.add(KEY.TRACK_RULES, trackRulesArray[i]);
        }

        //Add a single terrain type..		
        //We need this since when we built track, the terrain type gets check to see if we can
        //built track on it and an exception is thrown if terrain type 0 does not exist.
        world.add(KEY.TERRAIN_TYPES, TerrainType.NULL);
    }
}
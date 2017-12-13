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

package org.railz.world.top;

import java.util.HashSet;

import org.railz.world.common.*;
import org.railz.world.terrain.TerrainType;
import org.railz.world.track.*;

/**
 * This class is used to generate fixures for Junit tests.
 *
 * @author Luke
 *
 */
public class MapFixtureFactory {
    public int w = 10;
    public int h = 10;
    public World world;
    protected byte legalTrackConfigurations[][] = new byte[3][];
    protected boolean legalTrackPlacement[][] = new boolean[3][];

    public MapFixtureFactory(int w, int h) {
	this.w = w;
	this.h = h;
	world = new WorldImpl(w, h);
	generateTrackRuleList(world);
	for (int x = 0; x < w; x++) {
	    for (int y = 0; y < h; y++) {
		world.setTile(x, y, new FreerailsTile(0, null, null));
	    }
	}
    }

    public MapFixtureFactory() {
	world = new WorldImpl(w, h);
        generateTrackRuleList(world);
	for (int x = 0; x < w; x++) {
	    for (int y = 0; y < h; y++) {
		world.setTile(x, y, new FreerailsTile(0, null, null));
	    }
	}
    }

    public void generateTrackRuleList(World world) {
        TrackRule[] trackRulesArray = new TrackRule[3];
        //1st track type..
        byte[] trackTemplates0 = {
            CompassPoints.NORTH,
	    CompassPoints.NORTH | CompassPoints.SOUTH,
	    CompassPoints.NORTHWEST | CompassPoints.WEST | CompassPoints.EAST,
	    CompassPoints.NORTHEAST | CompassPoints.WEST | CompassPoints.EAST,
            CompassPoints.NORTH | CompassPoints.WEST,
	    CompassPoints.NORTHWEST | CompassPoints.WEST
        };

        legalTrackConfigurations[0] = trackTemplates0;
        legalTrackPlacement[0] = new boolean[] { true, true, true, true };
        trackRulesArray[0] = new TrackRule(0, "type0", false, 10,
		legalTrackConfigurations[0], 0, legalTrackPlacement[0]);

        //2nd track type..
        legalTrackConfigurations[1] = new byte[] {
	    CompassPoints.NORTH,
	    CompassPoints.NORTH | CompassPoints.SOUTH
	};

        legalTrackPlacement[1] = new boolean[] { true, true, true, true };
        trackRulesArray[1] = new TrackRule(0, "type1", false, 20,
		legalTrackConfigurations[1], 0, legalTrackPlacement[1]);

        //3rd track type..
        legalTrackConfigurations[2] = new byte[0];
        legalTrackPlacement[2] = new boolean[] { true, true, true, true };
        trackRulesArray[2] = new TrackRule(0, "type2", false, 30,
		legalTrackConfigurations[2], 0, legalTrackPlacement[2]);

        //Add track rules to world
        for (int i = 0; i < trackRulesArray.length; i++) {
            world.add(KEY.TRACK_RULES, trackRulesArray[i]);
        }

        //Add a single terrain type..		
        //We need this since when we built track, the terrain type gets check to see if we can
        //built track on it and an exception is thrown if terrain type 0 does not exist.
	world.add(KEY.TERRAIN_TYPES, new TerrainType(0,
		    TerrainType.CATEGORY_COUNTRY, "Dummy Terrain", 0L));
    }
}

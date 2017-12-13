/*
 * Copyright (C) 2004 Robert Tuck
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

package org.railz.server.parser;

import java.util.*;
import org.xml.sax.*;

import org.railz.world.player.*;
import org.railz.world.station.*;
import org.railz.world.top.*;

class StationImprovementsHandler {
    private World world;
    private StationImprovement stationImprovement;
    private ArrayList prerequisites;
    private ArrayList replaced;
    private String id;
    private String description;
    private long basePrice;

    public StationImprovementsHandler(World w) {
	world = w;
    }

    public void startElement(String ns, String name, String qname, Attributes
	    attrs) throws SAXException {
	if ("StationImprovement".equals(name)) {
	    prerequisites = new ArrayList();
	    replaced = new ArrayList();
	    id = attrs.getValue("id");
	    description = attrs.getValue("description");
	    basePrice = Long.parseLong(attrs.getValue("basePrice"));
	} else  if ("PrerequisiteImprovement".equals(name)) {
	    String prereqName = attrs.getValue("id");
	    int prereqIndex = -1;
	    for (int i = 0; i < world.size(KEY.STATION_IMPROVEMENTS,
			Player.AUTHORITATIVE); i++) {
		StationImprovement si = (StationImprovement)
		    world.get(KEY.STATION_IMPROVEMENTS, i,
			    Player.AUTHORITATIVE);
		if (si.getName().equals(prereqName))
		    prereqIndex = i;
	    }
	    if (prereqIndex < 0)
		throw new IllegalStateException("Unrecognized improvement" +
			prereqName);
	    prerequisites.add(new Integer(prereqIndex));
	} else if ("ReplacedImprovement".equals(name)) {
	    String replacedName = attrs.getValue("id");
	    int replacedIndex = -1;
	    for (int i = 0; i < world.size(KEY.STATION_IMPROVEMENTS,
			Player.AUTHORITATIVE); i++) {
		StationImprovement si = (StationImprovement)
		    world.get(KEY.STATION_IMPROVEMENTS, i,
			    Player.AUTHORITATIVE);
		if (si.getName().equals(replacedName))
		    replacedIndex = i;
	    }
	    if (replacedIndex < 0)
		throw new IllegalStateException("Unrecognized improvement" +
			replacedName);
	    replaced.add(new Integer(replacedIndex));
	}
    }

    public void endElement(String ns, String name, String qname) throws
	SAXException {
	    if ("StationImprovement".equals(name)) {
		int[] prereqs = new int[prerequisites.size()];
		for (int i = 0; i < prereqs.length; i++)
		    prereqs[i] = ((Integer) prerequisites.get(i)).intValue();
		
		int[] reps = new int[replaced.size()];
		for (int i = 0; i < reps.length; i++)
		    reps[i] = ((Integer) replaced.get(i)).intValue();

		stationImprovement = new StationImprovement(id, description,
			basePrice, prereqs, reps);
		world.add(KEY.STATION_IMPROVEMENTS, stationImprovement,
			Player.AUTHORITATIVE);
	    }
	}
}

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

package org.railz.server;

import java.net.URL;
import java.util.logging.*;

import org.railz.server.parser.*;
import org.railz.util.*;
import org.railz.world.common.*;
import org.railz.world.player.Player;
import org.railz.world.top.ITEM;
import org.railz.world.top.KEY;
import org.railz.world.top.World;
import org.railz.world.top.WorldImpl;
import org.xml.sax.SAXException;

/**
 * This class sets up a World object. It cannot be instantiated.
 */
class WorldFactory {
    private static final Logger logger = Logger.getLogger("global");
    
    private WorldFactory() {
    }

    /**
     * TODO This would be better implemented in a config file, or better
     * still dynamically determined by scanning the directory.
     */
    public static String[] getMapNames() {
        return new String[] {"south_america", "small_south_america"};
    }

    public static WorldImpl createWorldFromMapFile(String mapName,
        FreerailsProgressMonitor pm) {
	ModdableResourceFinder mrf = new ModdableResourceFinder
	    ("org/railz/server/data");

        pm.setMessage("Setting up world.");
        pm.setValue(0);
        pm.setMax(5);

        int progess = 0;

        //Load the xml file specifying terrain types.

        WorldImpl w = new WorldImpl();
        pm.setValue(++progess);

        //Set the time..
        w.set(ITEM.CALENDAR, new GameCalendar(30, 1840, 0));
        w.set(ITEM.TIME, new GameTime(0));

        try {
            java.net.URL url = mrf.getURLForReading
		("cargo_and_terrain.xml");

            CargoAndTerrainParser.parse(url, w);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
        pm.setValue(++progess);

        URL track_xml_url = mrf.getURLForReading
	    ("track_tiles.xml");

	Track_TilesHandlerImpl trackSetFactory = new
	    Track_TilesHandlerImpl(track_xml_url, w);
        pm.setValue(++progess);

        //Load the terrain map
        URL map_url = mrf.getURLForReading
	    ("maps/" + mapName + "/map.png");
        MapFactory.setupMap(map_url, w, pm);

        //Load the city names
	URL cities_xml_url = mrf.getURLForReading
	    ("maps/" + mapName + "/map.xml");

        try {
            InputCityNames r = new InputCityNames(w, cities_xml_url);
        } catch (SAXException e) {
	    logger.log(Level.WARNING,"Caught exception " + e.getMessage(), e);
        }

        //Randomly position the city tiles - no need to assign this object
        new BuildingTilePositioner(w);

        return w;
    }
}

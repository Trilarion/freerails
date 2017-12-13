/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 28-Apr-2003
 *
 */
package jfreerails.server;

import jfreerails.server.common.*;
import jfreerails.server.parser.CargoAndTerrainHandlerImpl;
import jfreerails.server.parser.CargoAndTerrainParser;
import jfreerails.server.parser.RunTypesParser;
import jfreerails.world.top.World;


/**
 * This class adds cargo and terrain types defined in an XML file to a World object.
 *
 * @author Luke
 *
 */
public class NewTileSetFactoryImpl implements TileSetFactory {
    public void addTerrainTileTypesList(World w) {
        try {
            java.net.URL url = RunTypesParser.class.getResource(
                    "/jfreerails/data/cargo_and_terrain.xml");

            CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl(w));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }
}
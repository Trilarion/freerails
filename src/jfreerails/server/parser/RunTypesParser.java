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
 * RunTypesParser.java
 *
 * Created on 27 April 2003, 18:40
 */
package jfreerails.server.parser;

import jfreerails.world.top.WorldImpl;


/**
 *
 * @author  Luke
 */
public class RunTypesParser {
    public static void main(String[] args) {
        try {
            java.net.URL url = RunTypesParser.class.getResource(
                    "/jfreerails/data/cargo_and_terrain.xml");

            CargoAndTerrainParser.parse(url,
                new CargoAndTerrainHandlerImpl(new WorldImpl()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
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
 *
 */
package freerails.server;

import freerails.server.parser.CargoAndTerrainHandlerImpl;
import freerails.server.parser.CargoAndTerrainParser;
import freerails.server.parser.RunTypesParser;
import freerails.world.World;

/**
 * Adds cargo and terrain types defined in an XML file to a World
 * object.
 */
// TODO difference interface and implementation
public class TileSetFactoryImpl implements TileSetFactory {

    /**
     * @param world
     */
    public void addTerrainTileTypesList(World world) {
        try {
            java.net.URL url = RunTypesParser.class
                    .getResource("/freerails/data/cargo_and_terrain.xml");

            CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl(world));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
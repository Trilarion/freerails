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

package freerails.server;

import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.terrain.City;
import freerails.world.MapFixtureFactory;
import junit.framework.TestCase;

/**
 * Test for CityModel
 */
public class CityModelTest extends TestCase {

    /**
     * Tests generating populated CityModel from cities on the map.
     */
    public void testLoadFromMap() {
        World world = MapFixtureFactory.getWorld(100, 100);
        City city = new City("New York", 10, 20);
        world.add(SKEY.CITIES, city);

        CityModel cityModel = new CityModel();
        cityModel.loadFromMap(world, 0);
        assertEquals(0, cityModel.industryCityTiles.size());
        assertEquals(0, cityModel.urbanCityTiles.size());
        assertEquals("A city is a 7*7 area", 49, cityModel.clearTiles.size());
    }

}
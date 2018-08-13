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

import freerails.model.terrain.City;
import freerails.model.terrain.CityModel;
import freerails.util.Vec2D;
import freerails.model.world.World;
import freerails.model.MapFixtureFactory;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * Test for CityModel
 */
public class CityModelTest extends TestCase {

    /**
     * Tests generating populated CityModel from cities on the map.
     */
    public void testLoadFromMap() throws IOException {
        World world = MapFixtureFactory.getWorld(new Vec2D(100, 100));
        // TODO this is not supposed to work, but still works
        City city = new City(1,"New York", new Vec2D(10, 20));
        world.getCities().add(city);

        CityModel cityModel = new CityModel();
        cityModel.loadFromMap(world, 1);
        assertEquals(0, cityModel.getIndustryCityTiles().size());
        assertEquals(0, cityModel.getUrbanCityTiles().size());
        assertEquals("A city is a 7*7 area", 49, cityModel.getClearTiles().size());
    }

}
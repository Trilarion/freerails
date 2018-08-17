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

import freerails.model.cargo.Cargo;
import freerails.model.cargo.CargoCategory;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.city.City;
import freerails.model.terrain.city.CityModel;
import freerails.util.Vec2D;
import freerails.model.world.World;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Test for CityModel
 */
public class CityModelTest extends TestCase {

    /**
     * Tests generating populated CityModel from cities on the map.
     */
    public void testLoadFromMap() throws IOException {
        World world = WorldGenerator.testWorld(true);
        Vec2D mapSize = world.getMapSize();

        TerrainTile tile = new TerrainTile(0);
        for (int x = 0; x < mapSize.x; x++) {
            for (int y = 0; y < mapSize.y; y++) {
                world.setTile(new Vec2D(x, y), tile);
            }
        }

        // TODO this is not supposed to work, but still works
        City city = new City(1,"New York", new Vec2D(5, 5));
        world.getCities().add(city);

        CityModel cityModel = new CityModel();
        cityModel.loadFromMap(world, 1);
        assertEquals(0, cityModel.getIndustryCityTiles().size());
        assertEquals(0, cityModel.getUrbanCityTiles().size());
        assertEquals("A city is a 7*7 area", 49, cityModel.getClearTiles().size());
    }

}
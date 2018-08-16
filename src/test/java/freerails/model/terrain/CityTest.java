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

package freerails.model.terrain;

import freerails.model.terrain.city.City;
import freerails.util.TestUtils;
import freerails.util.Vec2D;
import junit.framework.TestCase;

/**
 *
 */
public class CityTest extends TestCase {

    /**
     *
     */
    public void testHashCodeAndEquals() {
        City city1 = new City(1,"London", new Vec2D(20, 70));
        City city2 = new City(2, "Cardiff", new Vec2D(20, 70));
        TestUtils.assertCloneBySerializationBehavesWell(city1);
        TestUtils.assertCloneBySerializationBehavesWell(city2);
        TestUtils.assertUnequalAndNoHashcodeCollision(city1, city2);
    }
}

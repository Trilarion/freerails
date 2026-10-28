/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.model.cargo;

import junit.framework.TestCase;

public class WagonLoadTest extends TestCase {

    public void testGettersAndComparisonByQuantity() {
        WagonLoad smaller = new WagonLoad(5, 2);
        WagonLoad larger = new WagonLoad(8, 1);
        WagonLoad sameQuantityDifferentCargo = new WagonLoad(5, 9);

        assertEquals(5, smaller.quantity());
        assertEquals(2, smaller.cargoType());
        assertTrue(smaller.compareTo(larger) < 0);
        assertTrue(larger.compareTo(smaller) > 0);
        assertEquals(0, smaller.compareTo(sameQuantityDifferentCargo));
    }
}

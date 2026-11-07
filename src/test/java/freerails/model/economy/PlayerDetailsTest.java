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

package freerails.model.economy;

import junit.framework.TestCase;

/**
 * Tests player details behavior.
 */
public class PlayerDetailsTest extends TestCase {

    /**
     * Tests constructor and setters behavior.
     */
    public void testConstructorAndSetters() {
        PlayerDetails details = new PlayerDetails("Ada", new Money(1234), 3);

        assertEquals("Ada", details.getName());
        assertEquals(new Money(1234), details.getNetworth());
        assertEquals(3, details.getStations());
        assertEquals("Ada, 1234 net worth, 3  stations.", details.toString());

        details.setName("Grace");
        details.setNetworth(new Money(4321));
        details.setStations(7);

        assertEquals("Grace", details.getName());
        assertEquals(new Money(4321), details.getNetworth());
        assertEquals(7, details.getStations());
        assertEquals("Grace, 4321 net worth, 7  stations.", details.toString());
    }

    /**
     * Tests comparison uses net worth behavior.
     */
    public void testComparisonUsesNetWorth() {
        PlayerDetails poorer = new PlayerDetails("Poor", new Money(-5), 0);
        PlayerDetails wealthier = new PlayerDetails("Rich", new Money(5), 1);

        assertTrue(poorer.compareTo(wealthier) < 0);
        assertTrue(wealthier.compareTo(poorer) > 0);
    }
}

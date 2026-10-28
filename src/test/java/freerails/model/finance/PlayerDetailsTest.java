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

package freerails.model.finance;

import junit.framework.TestCase;

public class PlayerDetailsTest extends TestCase {

    public void testDefaultValuesAndSetters() {
        PlayerDetails details = new PlayerDetails();

        assertEquals("player", details.getName());
        assertEquals(Money.ZERO, details.getNetworth());
        assertEquals(0, details.getStations());

        details.setName("Ada");
        details.setNetworth(new Money(1234));
        details.setStations(3);

        assertEquals("Ada", details.getName());
        assertEquals(new Money(1234), details.getNetworth());
        assertEquals(3, details.getStations());
        assertEquals("Ada, 1,234 net worth, 3  stations.", details.toString());
    }

    public void testComparisonUsesNetWorth() {
        PlayerDetails poorer = new PlayerDetails();
        PlayerDetails wealthier = new PlayerDetails();
        poorer.setNetworth(new Money(-5));
        wealthier.setNetworth(new Money(5));

        assertTrue(poorer.compareTo(wealthier) < 0);
        assertTrue(wealthier.compareTo(poorer) > 0);
    }
}

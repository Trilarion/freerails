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

import junit.framework.TestCase;

/**
 * Test for CargoAtStationsUpdater.
 */
public class CargoAtStationsUpdaterTest extends TestCase {

    /**
     *
     */
    public void testCalculateAmountToAdd() {
        CargoAtStationsUpdater cargoGenerator = new CargoAtStationsUpdater();

        int amount = cargoGenerator.calculateAmountToAdd(12, 0);
        assertEquals(1, amount);
        assertCorrectTotalAddedOverYear(0);
        assertCorrectTotalAddedOverYear(12);
        assertCorrectTotalAddedOverYear(14);
        assertCorrectTotalAddedOverYear(140);
        assertCorrectTotalAddedOverYear(3);
    }

    /**
     * If, say, 14 units get added each year, some month we should add 1 and
     * others we should add 2 such that over the year exactly 14 units get
     * added.
     */
    private void assertCorrectTotalAddedOverYear(final int unitPerYear) {
        CargoAtStationsUpdater cargoGenerator = new CargoAtStationsUpdater();
        int amountAddedThisSoFar = 0;

        for (int i = 0; i < 12; i++) {
            amountAddedThisSoFar += cargoGenerator.calculateAmountToAdd(
                    unitPerYear, i);
        }

        assertEquals(unitPerYear, amountAddedThisSoFar);
    }
}
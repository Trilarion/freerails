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
package freerails.controller;

import junit.framework.TestCase;

/**
 */
public class SharePriceCalculatorTest extends TestCase {

    /**
     *
     */
    public void test1() {
        SharePriceCalculator cal = new SharePriceCalculator();
        cal.networth = 100000;
        cal.profitsLastYear = 100000;
        cal.stockholderEquity = 500000;
        cal.totalShares = 100000;

        long expected = (100000 + 500000 + 100000 * 5) / 100000;
        assertEquals(expected, cal.calulatePrice());
    }
}
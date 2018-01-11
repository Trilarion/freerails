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

/**
 */
public class SharePriceCalculator {

    public int totalShares;
    public int treasuryStock;
    public int otherRRStakes;
    public long profitsLastYear;
    public long networth;
    public long stockholderEquity;

    /**
     * @return
     */
    public long calulatePrice() {
        assert totalShares > 0;
        assert totalShares >= treasuryStock + otherRRStakes;
        assert stockholderEquity > 0;

        long price;
        long currentValue = networth + stockholderEquity;
        long expectedIncrease = profitsLastYear * 5;

        int publicOwnedShares = totalShares - treasuryStock - otherRRStakes;
        price = 2 * (currentValue + expectedIncrease) / (2 * publicOwnedShares + otherRRStakes);

        return price;
    }
}
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

package freerails.model.finances;

import freerails.model.ModelConstants;

/**
 *
 */
public class StockPrice {

    public final Money currentPrice;
    public final Money sellPrice;
    public final Money buyPrice;
    public final Money treasuryBuyPrice;
    public final Money treasurySellPrice;

    /**
     * @param netWorth
     * @param profitLastYear
     * @param publicShares
     * @param otherRRShares
     */
    StockPrice(long netWorth, long profitLastYear, int publicShares, int otherRRShares) {
        currentPrice = calculateStockPrice(netWorth, profitLastYear, publicShares, otherRRShares);
        sellPrice = calculateStockPrice(netWorth, profitLastYear, publicShares + ModelConstants.STOCK_BUNDLE_SIZE, otherRRShares - ModelConstants.STOCK_BUNDLE_SIZE);
        buyPrice = calculateStockPrice(netWorth, profitLastYear, publicShares - ModelConstants.STOCK_BUNDLE_SIZE, otherRRShares + ModelConstants.STOCK_BUNDLE_SIZE);
        treasurySellPrice = calculateStockPrice(netWorth, profitLastYear, publicShares + ModelConstants.STOCK_BUNDLE_SIZE, otherRRShares);
        treasuryBuyPrice = calculateStockPrice(netWorth, profitLastYear, publicShares - ModelConstants.STOCK_BUNDLE_SIZE, otherRRShares);
    }

    /**
     *
     * @param netWorth
     * @param profitLastyear
     * @param publicShares
     * @param otherRRShares
     * @return
     */
    public static Money calculateStockPrice(long netWorth, long profitLastyear, int publicShares, int otherRRShares) {
        if ((publicShares + otherRRShares) == 0) return new Money(Long.MAX_VALUE);
        long price = 2 * (5 * profitLastyear + netWorth) / (2 * publicShares + otherRRShares);
        return new Money(price);
    }
}

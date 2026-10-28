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

import freerails.model.ModelConstants;

/** Holds the calculated market and treasury prices for a company's shares. */
public class StockPrice {

    public final Money currentPrice;
    public final Money sellPrice;
    public final Money buyPrice;
    public final Money treasuryBuyPrice;
    public final Money treasurySellPrice;

    /**
     * Calculates current, public-trade, and treasury-trade prices for a share
     * bundle using the same valuation inputs.
     *
     * @param netWorth company net worth
     * @param profitLastYear profit during the previous year
     * @param publicShares shares held by the public
     * @param otherRRShares shares held by other railroads
     */
    StockPrice(Money netWorth, Money profitLastYear, int publicShares, int otherRRShares) {
        currentPrice = calculateStockPrice(netWorth, profitLastYear, publicShares, otherRRShares);
        sellPrice = calculateStockPrice(netWorth, profitLastYear, publicShares + ModelConstants.STOCK_BUNDLE_SIZE, otherRRShares - ModelConstants.STOCK_BUNDLE_SIZE);
        buyPrice = calculateStockPrice(netWorth, profitLastYear, publicShares - ModelConstants.STOCK_BUNDLE_SIZE, otherRRShares + ModelConstants.STOCK_BUNDLE_SIZE);
        treasurySellPrice = calculateStockPrice(netWorth, profitLastYear, publicShares + ModelConstants.STOCK_BUNDLE_SIZE, otherRRShares);
        treasuryBuyPrice = calculateStockPrice(netWorth, profitLastYear, publicShares - ModelConstants.STOCK_BUNDLE_SIZE, otherRRShares);
    }

    // TODO document this (in the design document)
    /**
     * Calculates a share price from company net worth, prior-year profit, and
     * the supplied share counts. A zero denominator yields the maximum
     * representable amount.
     *
     * @param netWorth company net worth
     * @param profitLastyear company profit in the previous year
     * @param publicShares shares held by the public
     * @param otherRRShares shares held by other railroads
     * @return calculated price per share unit
     */
    public static Money calculateStockPrice(Money netWorth, Money profitLastyear, int publicShares, int otherRRShares) {
        if (publicShares + otherRRShares == 0) return new Money(Long.MAX_VALUE);
        long price = 2 * (5 * profitLastyear.amount() + netWorth.amount()) / (2 * publicShares + otherRRShares);
        return new Money(price);
    }
}

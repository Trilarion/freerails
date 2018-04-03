package freerails.model.finances;

import freerails.model.WorldConstants;

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
        sellPrice = calculateStockPrice(netWorth, profitLastYear, publicShares + WorldConstants.STOCK_BUNDLE_SIZE, otherRRShares - WorldConstants.STOCK_BUNDLE_SIZE);
        buyPrice = calculateStockPrice(netWorth, profitLastYear, publicShares - WorldConstants.STOCK_BUNDLE_SIZE, otherRRShares + WorldConstants.STOCK_BUNDLE_SIZE);
        treasurySellPrice = calculateStockPrice(netWorth, profitLastYear, publicShares + WorldConstants.STOCK_BUNDLE_SIZE, otherRRShares);
        treasuryBuyPrice = calculateStockPrice(netWorth, profitLastYear, publicShares - WorldConstants.STOCK_BUNDLE_SIZE, otherRRShares);
    }

    public static Money calculateStockPrice(long netWorth, long profitLastyear, int publicShares, int otherRRShares) {
        if ((publicShares + otherRRShares) == 0) return new Money(Long.MAX_VALUE);
        long price = 2 * (5 * profitLastyear + netWorth) / (2 * publicShares + otherRRShares);
        return new Money(price);
    }
}

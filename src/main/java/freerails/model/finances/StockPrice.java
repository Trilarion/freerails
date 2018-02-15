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
        currentPrice = StockPriceCalculator.calculateStockPrice(netWorth, profitLastYear, publicShares, otherRRShares);
        sellPrice = StockPriceCalculator.calculateStockPrice(netWorth, profitLastYear, publicShares + WorldConstants.STOCK_BUNDLE_SIZE, otherRRShares - WorldConstants.STOCK_BUNDLE_SIZE);
        buyPrice = StockPriceCalculator.calculateStockPrice(netWorth, profitLastYear, publicShares - WorldConstants.STOCK_BUNDLE_SIZE, otherRRShares + WorldConstants.STOCK_BUNDLE_SIZE);
        treasurySellPrice = StockPriceCalculator.calculateStockPrice(netWorth, profitLastYear, publicShares + WorldConstants.STOCK_BUNDLE_SIZE, otherRRShares);
        treasuryBuyPrice = StockPriceCalculator.calculateStockPrice(netWorth, profitLastYear, publicShares - WorldConstants.STOCK_BUNDLE_SIZE, otherRRShares);
    }
}

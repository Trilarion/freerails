/*
 * Created on 04-Oct-2004
 *
 */
package freerails.controller;

/**
 * @author Luke
 */
public class SharePriceCalculator {
    public int totalShares;

    public int treasuryStock;

    public int otherRRStakes;

    public long profitsLastYear;

    public long networth;

    public long stockholderEquity;

    public long calulatePrice() {
        assert totalShares > 0;
        assert totalShares >= treasuryStock + otherRRStakes;
        assert stockholderEquity > 0;

        long price;
        long currentValue = networth + stockholderEquity;
        long expectedIncrease = profitsLastYear * 5;

        int publicOwnedShares = totalShares - treasuryStock - otherRRStakes;
        price = 2 * (currentValue + expectedIncrease)
                / (2 * publicOwnedShares + otherRRStakes);

        return price;
    }
}
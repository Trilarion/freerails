/*
 * Created on 04-Oct-2004
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
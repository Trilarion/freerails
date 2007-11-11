/*
 * Created on Mar 14, 2004
 */
package jfreerails.server;

import junit.framework.TestCase;

/**
 * JUnit test for CargoAtStationsGenerator.
 * 
 * @author Luke
 * 
 */
public class CargoAtStationsGeneratorTest extends TestCase {
    public void testCalculateAmountToAdd() {
        CargoAtStationsGenerator cargoGenerator = new CargoAtStationsGenerator();

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
        CargoAtStationsGenerator cargoGenerator = new CargoAtStationsGenerator();
        int amountAddedThisSoFar = 0;

        for (int i = 0; i < 12; i++) {
            amountAddedThisSoFar += cargoGenerator.calculateAmountToAdd(
                    unitPerYear, i);
        }

        assertEquals(unitPerYear, amountAddedThisSoFar);
    }
}
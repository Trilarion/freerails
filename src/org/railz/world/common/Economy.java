/*
 * Copyright (C) Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.world.common;

/**
 *A repository for attributes common to the Railz economy.
 *
 * @author rtuck99@users.berlios.de
 */
public class Economy implements FreerailsSerializable {
    private long[] fuelUnitPrice = new long[3];

    /**
     * Interest rates for bonds, loans, overdrafts and accounts in credit will
     * be derived from this. Measured as annual %age rate.
     */
    private final float baseInterestRate;

    /**
     * Rate at which income tax is applied in %.
     */
    private final int incomeTaxRate;

    public Economy(int incomeTaxRate, float baseInterestRate) {
	this(incomeTaxRate, baseInterestRate, new long[3]);
    }

    private Economy(int incomeTaxRate, float baseInterestRate, long
	    fuelUnitPrice[]) {
	this.incomeTaxRate = incomeTaxRate;
	this.baseInterestRate = baseInterestRate;
	this.fuelUnitPrice = fuelUnitPrice;
    }

    /**
     * @return income tax rate in percent
     */
    public int getIncomeTaxRate() {
	return incomeTaxRate;
    }

    public float getBaseInterestRate() {
	return baseInterestRate;
    }

    public static double aerToMonthly(double rate) {
	return (float) ((Math.pow((1 + rate / 100), (1.0 / 12)) - 1.0) *
		100.0);
    }

    public Economy setFuelUnitPrice(int fuelType, long unitPrice) {
	long[] unitPrices = (long[]) fuelUnitPrice.clone();
	unitPrices[fuelType - 1] = unitPrice;
	return new Economy(incomeTaxRate, baseInterestRate, unitPrices);
    }

    /**
     * @param fuelType defined in EngineType.java
     * @return the unit price of the specified fuel
     */
    public long getFuelUnitPrice(int fuelType) {
	if (fuelType < 1 || fuelType - 1 > fuelUnitPrice.length)
	    throw new IllegalArgumentException();

	return fuelUnitPrice[fuelType - 1];
    }
}

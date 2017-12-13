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

package jfreerails.world.common;

/**
 *A repository for attributes common to the Railz economy.
 *
 * @author rtuck99@users.berlios.de
 */
public class Economy implements FreerailsSerializable {
    /**
     * Interest rates for bonds, loans, overdrafts and accounts in credit will
     * be derived from this. Measured as annual %age rate.
     */
    private float baseInterestRate;

    /**
     * Rate at which income tax is applied in %.
     */
    private int incomeTaxRate;

    /**
     * @return income tax rate in percent
     */
    public int getIncomeTaxRate() {
	return incomeTaxRate;
    }

    public void setIncomeTaxRate(int rate) {
	incomeTaxRate = rate;
    }

    public void setBaseInterestRate(float rate) {
	baseInterestRate = rate;
    }

    public float getBaseInterestRate() {
	return baseInterestRate;
    }

    public static double aerToMonthly(double rate) {
	return (float) ((Math.pow((1 + rate / 100), (1.0 / 12)) - 1.0) *
		100.0);
    }
}

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

    public int getIncomeTaxRate() {
	return incomeTaxRate;
    }

    public void setIncomeTaxRate(int rate) {
	incomeTaxRate = rate;
    }

    public void setBaseInterestRate(float rate) {
	baseInterestRate = rate;
    }

    private double aerToMonthly(double rate) {
	return (float) ((Math.pow((1 + rate / 100), (1.0 / 12)) - 1.0) *
		100.0);
    }
    /**
     * @return the monthly interest rate applied to an account in credit
     */
    public float getCreditAccountInterestRate() {
	float creditRate = (float) (baseInterestRate - 2.0);
	if (creditRate <= 0.0) 
	    return (float) 0.0;
	
	return (float) aerToMonthly(creditRate);
    }
}

/*
 * Created on 04-Oct-2004
 *
 */
package freerails.world.accounts;

import freerails.world.common.FreerailsSerializable;

/**
 * Represents the state of the economy.
 *
 * @author Luke
 */
public class EconomicClimate implements FreerailsSerializable {
    private static final long serialVersionUID = 3834025840475321136L;

    private static int i = 2;

    private final String name;

    public static final EconomicClimate BOOM = new EconomicClimate(i++, "BOOM");

    public static final EconomicClimate PROSPERITY = new EconomicClimate(i++,
            "PROSPERITY");

    public static final EconomicClimate MODERATION = new EconomicClimate(i++,
            "MODERATION");

    public static final EconomicClimate RECESSION = new EconomicClimate(i++,
            "RECESSION");

    public static final EconomicClimate PANIC = new EconomicClimate(i++,
            "PANIC");

    public int getBaseInterestRate() {
        return baseInterestRate;
    }

    private final int baseInterestRate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EconomicClimate)) {
            return false;
        }

        final EconomicClimate economicClimate = (EconomicClimate) o;

        if (baseInterestRate != economicClimate.baseInterestRate) {
            return false;
        }

        return name != null ? name.equals(economicClimate.name) : economicClimate.name == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 29 * result + baseInterestRate;

        return result;
    }

    private EconomicClimate(int r, String s) {
        baseInterestRate = r;
        name = s;
    }
}
/*
 * Created on 04-Oct-2004
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.FreerailsSerializable;


/**
 * @author Luke
 *
 */
public class EconomicClimate implements FreerailsSerializable {
    private static int i = 2;
    private final String name;
    public static final EconomicClimate BOOM = new EconomicClimate(i++, "BOOM");
    public static final EconomicClimate PROSPERITY = new EconomicClimate(i++,
            "PROSPERITY");
    public static final EconomicClimate MODERATION = new EconomicClimate(i++,
            "MODERATION");
    public static final EconomicClimate RECESSION = new EconomicClimate(i++,
            "RECESSION");
    public static final EconomicClimate PANIC = new EconomicClimate(i++, "PANIC");

    public int getBaseInterestRate() {
        return baseInterestRate;
    }

    private final int baseInterestRate;

    private EconomicClimate(int r, String s) {
        baseInterestRate = r;
        name = s;
    }
}
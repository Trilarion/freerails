/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.world.finances;

import java.io.Serializable;

/**
 * Represents the state of the economy.
 */
public class EconomicClimate implements Serializable {
    private static final long serialVersionUID = 3834025840475321136L;

    private static int i = 2;

    /**
     *
     */
    public static final EconomicClimate BOOM = new EconomicClimate(i++, "BOOM");

    /**
     *
     */
    public static final EconomicClimate PROSPERITY = new EconomicClimate(i++,
            "PROSPERITY");

    /**
     *
     */
    public static final EconomicClimate MODERATION = new EconomicClimate(i++,
            "MODERATION");

    /**
     *
     */
    public static final EconomicClimate RECESSION = new EconomicClimate(i++,
            "RECESSION");

    /**
     *
     */
    public static final EconomicClimate PANIC = new EconomicClimate(i++,
            "PANIC");
    private final String name;
    private final int baseInterestRate;

    private EconomicClimate(int r, String s) {
        baseInterestRate = r;
        name = s;
    }

    /**
     * @return
     */
    public int getBaseInterestRate() {
        return baseInterestRate;
    }

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
}
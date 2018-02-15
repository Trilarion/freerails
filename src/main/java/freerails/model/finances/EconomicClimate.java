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
package freerails.model.finances;

import java.io.Serializable;

/**
 * Represents the state of the economy.
 */

public class EconomicClimate implements Serializable {

    // TODO why start with 2
    public static final EconomicClimate BOOM = new EconomicClimate("BOOM", 2);
    public static final EconomicClimate PROSPERITY = new EconomicClimate("PROSPERITY", 3);
    public static final Serializable MODERATION = new EconomicClimate("MODERATION", 4);
    public static final EconomicClimate RECESSION = new EconomicClimate("RECESSION", 5);
    public static final EconomicClimate PANIC = new EconomicClimate("PANIC", 6);
    private static final long serialVersionUID = 3834025840475321136L;
    private final String name;
    private final double baseInterestRate;

    private EconomicClimate(String description, double rate) {
        baseInterestRate = rate;
        name = description;
    }

    /**
     * @return
     */
    public double getBaseInterestRate() {
        return baseInterestRate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EconomicClimate)) {
            return false;
        }
        final EconomicClimate economicClimate = (EconomicClimate) obj;
        if (baseInterestRate != economicClimate.baseInterestRate) {
            return false;
        }
        return name != null ? name.equals(economicClimate.name) : economicClimate.name == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 29 * result + (int) baseInterestRate;
        return result;
    }
}
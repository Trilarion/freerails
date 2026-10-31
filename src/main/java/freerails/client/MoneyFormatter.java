/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.client;

import freerails.model.economy.Money;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Formats game money for display using locale-specific conventions and USD.
 */
public final class MoneyFormatter {

    private static final Currency GAME_CURRENCY = Currency.getInstance("USD");

    /**
     * Creates a money formatter instance.
     */
    private MoneyFormatter() {
    }

    /**
     * Formats an amount using the process default locale.
     *
     * @param money amount to format
     * @return localized USD amount with no fractional digits
     */
    public static String format(Money money) {
        return format(money, Locale.getDefault());
    }

    /**
     * Formats an amount using the given locale.
     *
     * @param money amount to format
     * @param locale locale to use for grouping and currency placement
     * @return localized USD amount with no fractional digits
     */
    public static String format(Money money, Locale locale) {
        Objects.requireNonNull(money, "money");
        Objects.requireNonNull(locale, "locale");

        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setCurrency(GAME_CURRENCY);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(money.amount());
    }
}

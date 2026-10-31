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
import junit.framework.TestCase;

import java.util.Locale;

public class MoneyFormatterTest extends TestCase {

    public void testFormatsUsdWithLocaleSpecificGrouping() {
        assertEquals("$1,234", MoneyFormatter.format(new Money(1_234), Locale.US));

        String german = MoneyFormatter.format(new Money(1_234), Locale.GERMANY);
        assertTrue(german, german.contains("1.234"));
        assertTrue(german, german.contains("$"));
        assertFalse(german, german.contains(","));
    }

    public void testFormatsNegativeAndLongAmounts() {
        assertEquals("-$1,234", MoneyFormatter.format(new Money(-1_234), Locale.US));
        assertEquals("$9,223,372,036,854,775,807",
                MoneyFormatter.format(new Money(Long.MAX_VALUE), Locale.US));
    }
}

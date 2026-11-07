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

package freerails.model.economy;

import freerails.util.TestUtils;
import junit.framework.TestCase;

import java.util.Locale;

/**
 * Tests money behavior.
 */
public class MoneyTest extends TestCase {

    /**
     * Tests to string is locale independent behavior.
     */
    public void testToStringIsLocaleIndependent() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            Money amount = new Money(1_234_567);
            assertEquals("1234567", amount.toString());
            assertEquals(amount, new Money(1_234_567));
            assertEquals(new Money(1_234_568), Money.add(amount, new Money(1)));

            Locale.setDefault(Locale.GERMANY);
            assertEquals("1234567", amount.toString());
            assertEquals(amount, new Money(1_234_567));
            assertEquals(new Money(1_234_568), Money.add(amount, new Money(1)));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    /**
     * Tests divide behavior.
     */
    public void testDivide() {
        assertEquals(new Money(3), Money.divide(new Money(12), 4));
        assertEquals(new Money(-3), Money.divide(new Money(-12), 4));
        assertEquals(new Money(2), Money.divide(new Money(7), 3));
        assertEquals(new Money(-2), Money.divide(new Money(-7), 3));
        assertEquals(new Money(-2), Money.divide(new Money(7), -3));
        assertArithmeticException(() -> Money.divide(new Money(1), 0));
    }

    /**
     * Tests arithmetic is exact at long limits behavior.
     */
    public void testArithmeticIsExactAtLongLimits() {
        assertEquals(new Money(Long.MAX_VALUE), Money.add(new Money(Long.MAX_VALUE), Money.ZERO));
        assertEquals(new Money(Long.MIN_VALUE), Money.add(new Money(Long.MIN_VALUE), Money.ZERO));
        assertEquals(new Money(Long.MAX_VALUE), Money.subtract(new Money(Long.MAX_VALUE), Money.ZERO));
        assertEquals(new Money(Long.MIN_VALUE), Money.subtract(new Money(Long.MIN_VALUE), Money.ZERO));
        assertEquals(new Money(Long.MAX_VALUE), Money.multiply(new Money(Long.MAX_VALUE), 1));
        assertEquals(new Money(Long.MIN_VALUE), Money.multiply(new Money(Long.MIN_VALUE), 1));
    }

    /**
     * Tests arithmetic overflow is rejected behavior.
     */
    public void testArithmeticOverflowIsRejected() {
        assertArithmeticException(() -> Money.add(new Money(Long.MAX_VALUE), new Money(1)));
        assertArithmeticException(() -> Money.add(new Money(Long.MIN_VALUE), new Money(-1)));
        assertArithmeticException(() -> Money.subtract(new Money(Long.MIN_VALUE), new Money(1)));
        assertArithmeticException(() -> Money.subtract(new Money(Long.MAX_VALUE), new Money(-1)));
        assertArithmeticException(() -> Money.multiply(new Money(Long.MAX_VALUE), 2));
        assertArithmeticException(() -> Money.multiply(new Money(Long.MIN_VALUE), -1));
    }

    /**
     * Asserts arithmetic exception.
     * @param operation the operation to use in the test.
     */
    private static void assertArithmeticException(Runnable operation) {
        TestUtils.assertThrows(ArithmeticException.class, operation::run);
    }
}

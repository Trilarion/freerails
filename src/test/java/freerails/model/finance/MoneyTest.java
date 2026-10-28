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

package freerails.model.finance;

import junit.framework.TestCase;

public class MoneyTest extends TestCase {

    public void testDivide() {
        assertEquals(new Money(3), Money.divide(new Money(12), 4));
        assertEquals(new Money(-3), Money.divide(new Money(-12), 4));
    }

    public void testArithmeticOverflowIsRejected() {
        assertArithmeticException(() -> Money.add(new Money(Long.MAX_VALUE), new Money(1)));
        assertArithmeticException(() -> Money.subtract(new Money(Long.MIN_VALUE), new Money(1)));
        assertArithmeticException(() -> Money.multiply(new Money(Long.MAX_VALUE), 2));
    }

    private static void assertArithmeticException(Runnable operation) {
        try {
            operation.run();
            fail("Expected arithmetic overflow.");
        } catch (ArithmeticException expected) {
            // Expected.
        }
    }
}

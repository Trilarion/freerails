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

package freerails.model.finance.transaction;

import freerails.model.finance.Money;
import freerails.model.game.Time;
import junit.framework.TestCase;

public class TransactionEqualityTest extends TestCase {

    public void testBaseTransactionIncludesTimeInEqualityAndHashCode() {
        Transaction first = new Transaction(TransactionCategory.MISC_INCOME, new Money(12), Time.ZERO);
        Transaction same = new Transaction(TransactionCategory.MISC_INCOME, new Money(12), Time.ZERO);
        Transaction differentTime = new Transaction(
                TransactionCategory.MISC_INCOME, new Money(12), Time.ZERO.advance());

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(differentTime));
    }

    public void testItemTransactionIncludesItemFieldsAndConcreteType() {
        ItemTransaction item = new ItemTransaction(
                TransactionCategory.TRACK, new Money(-10), Time.ZERO, 2, 8);
        ItemTransaction same = new ItemTransaction(
                TransactionCategory.TRACK, new Money(-10), Time.ZERO, 2, 8);
        ItemTransaction differentId = new ItemTransaction(
                TransactionCategory.TRACK, new Money(-10), Time.ZERO, 2, 9);
        Transaction base = new Transaction(TransactionCategory.TRACK, new Money(-10), Time.ZERO);

        assertEquals(item, same);
        assertEquals(item.hashCode(), same.hashCode());
        assertFalse(item.equals(differentId));
        assertFalse(item.equals(base));
        assertFalse(base.equals(item));
    }

    public void testBondTransactionIncludesRate() {
        BondItemTransaction first = new BondItemTransaction(new Money(10), Time.ZERO, 1, 4.5);
        BondItemTransaction same = new BondItemTransaction(new Money(10), Time.ZERO, 1, 4.5);
        BondItemTransaction differentRate = new BondItemTransaction(new Money(10), Time.ZERO, 1, 5.0);

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(differentRate));
    }
}

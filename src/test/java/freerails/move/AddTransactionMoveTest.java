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

/*
 *
 */
package freerails.move;

import freerails.model.economy.*;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.util.WorldGenerator;

/**
 * Tests add transaction move behavior.
 */
public class AddTransactionMoveTest extends AbstractMoveTestCase {

    /**
     * Tests move behavior.
     */
    public void testMove() {
        Money currentBalance = getWorld().getCurrentBalance(WorldGenerator.TEST_PLAYER);
        assertEquals(new Money(0), currentBalance);

        Transaction transaction = new Transaction(TransactionCategory.MISC_INCOME, new Money(100), world.getClock().getCurrentTime());
        Move move1 = new AddTransactionMove(WorldGenerator.TEST_PLAYER, transaction);
        assertMoveApplicable(move1);
        assertMoveApplyIsOk(move1);
        currentBalance = getWorld().getCurrentBalance(WorldGenerator.TEST_PLAYER);
        assertEquals(new Money(100), currentBalance);
        assertSurvivesSerialisation(move1);
    }

    /**
     * Tests constrained move behavior.
     */
    public void testConstrainedMove() {
        Money currentBalance = getWorld().getCurrentBalance(WorldGenerator.TEST_PLAYER);
        assertEquals(new Money(0), currentBalance);

        Transaction transaction = new Transaction(TransactionCategory.MISC_INCOME, new Money(-100), world.getClock().getCurrentTime());
        Move move = new AddTransactionMove(WorldGenerator.TEST_PLAYER, transaction, true);

        // This move should fail since there is no money in the account and it is constrained is set to true.
        assertMoveNotApplicable(move);
    }
}
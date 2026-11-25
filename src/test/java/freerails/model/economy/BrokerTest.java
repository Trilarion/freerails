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

import freerails.model.MapFixtureFactory2;
import freerails.model.ModelConstants;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.simulation.Time;
import freerails.model.world.World;
import freerails.move.Move;
import freerails.move.Status;
import freerails.move.generator.IssueBondMoveGenerator;
import freerails.move.generator.RepayBondMoveGenerator;
import freerails.move.generator.TradeSharesMoveGenerator;
import junit.framework.TestCase;

import java.util.List;

/**
 * Tests the share and bond trading rules and that the generators apply them.
 */
public class BrokerTest extends TestCase {

    private static final int BUNDLE = ModelConstants.STOCK_BUNDLE_SIZE;

    private World world;
    private Player player;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        player = world.getPlayer(0);
    }

    /**
     * Tests that only whole bundles of known railroads are traded.
     */
    public void testTradesNeedBundlesOfKnownRailroads() {
        setBalance(new Money(1_000_000_000L));

        assertTrue(Broker.checkShareTrade(world, player, player.getId(), BUNDLE + 1).isPresent());
        assertTrue(Broker.checkShareTrade(world, player, 999, BUNDLE).isPresent());
        assertTrue(Broker.checkShareTrade(world, player, player.getId(), BUNDLE).isEmpty());
    }

    /**
     * Tests that buying needs cash and selling needs shares, and that the
     * generated trade uses the broker's price.
     */
    public void testBuyingAndSellingShares() {
        int railroad = player.getId();
        assertEquals("You don't hold enough of these shares.",
                Broker.checkShareTrade(world, player, railroad, -BUNDLE).orElseThrow());

        setBalance(new Money(-1_000_000L));
        assertEquals("You can't afford that!", Broker.checkShareTrade(world, player, railroad, BUNDLE).orElseThrow());

        setBalance(new Money(1_000_000_000L));
        Money price = Broker.sharePrice(world, player, railroad, BUNDLE);
        Money before = world.getCurrentBalance(player);
        apply(new TradeSharesMoveGenerator(railroad, BUNDLE, player).generate(world));
        assertEquals(Money.subtract(before, Money.multiply(price, BUNDLE)), world.getCurrentBalance(player));

        assertTrue(Broker.checkShareTrade(world, player, railroad, -BUNDLE).isEmpty());
    }

    /**
     * Tests the bond rules: issue while allowed, repay only outstanding bonds
     * the player can pay for.
     */
    public void testBonds() {
        assertTrue(Broker.checkIssueBond(world, player).isEmpty());
        Money before = world.getCurrentBalance(player);
        apply(new IssueBondMoveGenerator(player).generate(world));
        assertEquals(Money.add(before, ModelConstants.BOND_VALUE_ISSUE), world.getCurrentBalance(player));

        setBalance(Money.ZERO);
        assertEquals("You can't afford that!", Broker.checkRepayBond(world, player).orElseThrow());
        assertFalse(new RepayBondMoveGenerator(player).generate(world).applicable(world).isSuccess());

        // Repay every outstanding bond, including any the fixture starts with.
        setBalance(new Money(1_000_000_000L));
        for (int i = 0; i < 100 && Broker.checkRepayBond(world, player).isEmpty(); i++) {
            apply(new RepayBondMoveGenerator(player).generate(world));
        }
        assertEquals("There are no bonds to repay.", Broker.checkRepayBond(world, player).orElseThrow());
        assertFalse(new RepayBondMoveGenerator(player).generate(world).applicable(world).isSuccess());
    }

    /**
     * Tests that outstanding bonds keep their own rates, that repaying a bond
     * repays the most expensive one, and that interest follows the remaining rates.
     */
    public void testRepayingRepaysTheMostExpensiveBond() {
        Time now = world.getClock().getCurrentTime();
        for (double rate : Broker.outstandingBondRates(world, player)) {
            world.addTransaction(player, TransactionUtils.repayBond(rate, now));
        }
        assertEquals(Money.ZERO, Broker.yearlyBondInterest(world, player));

        world.addTransaction(player, TransactionUtils.issueBond(3, now));
        world.addTransaction(player, TransactionUtils.issueBond(6, now));
        assertEquals(List.of(3.0, 6.0), Broker.outstandingBondRates(world, player));

        setBalance(new Money(1_000_000_000L));
        apply(new RepayBondMoveGenerator(player).generate(world));

        assertEquals(List.of(3.0), Broker.outstandingBondRates(world, player));
        assertEquals(Money.divide(Money.multiply(ModelConstants.BOND_VALUE_ISSUE, 3), 100),
                Broker.yearlyBondInterest(world, player));
    }

    private void apply(Move move) {
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
        move.apply(world);
    }

    private void setBalance(Money balance) {
        Money change = Money.subtract(balance, world.getCurrentBalance(player));
        world.addTransaction(player, new Transaction(TransactionCategory.MISC_INCOME, change,
                world.getClock().getCurrentTime()));
    }
}

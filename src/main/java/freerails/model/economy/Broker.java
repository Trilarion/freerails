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

import freerails.model.ModelConstants;
import freerails.model.economy.transaction.BondItemTransaction;
import freerails.model.economy.transaction.ItemTransaction;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.aggregator.FinancialDataAggregator;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The rules and prices for trading shares and bonds. Both the broker dialog and
 * the server use them, so what a player is offered is what the server accepts.
 */
public final class Broker {

    private Broker() {
    }

    /**
     * Checks whether a player may buy (positive quantity) or sell (negative
     * quantity) shares of a railroad. Shares are traded in bundles of
     * {@link ModelConstants#STOCK_BUNDLE_SIZE}.
     *
     * @param world the world
     * @param player the trading player
     * @param railroadId the player ID of the railroad whose shares are traded
     * @param quantity the number of shares, positive to buy and negative to sell
     * @return the reason the trade is not allowed, or empty if it is allowed
     */
    public static Optional<String> checkShareTrade(UnmodifiableWorld world, Player player, int railroadId, int quantity) {
        Optional<Player> railroad = world.getPlayers().stream()
                .filter(candidate -> candidate.getId() == railroadId)
                .findFirst();
        if (railroad.isEmpty()) {
            return Optional.of("Unknown railroad.");
        }
        if (Math.abs(quantity) != ModelConstants.STOCK_BUNDLE_SIZE) {
            return Optional.of("Shares are traded in bundles of " + ModelConstants.STOCK_BUNDLE_SIZE + ".");
        }
        int railroadIndex = world.getPlayerIndex(railroadId);
        FinancialDataAggregator own = aggregate(world, player);

        if (quantity < 0) {
            if (own.getStockInRRs()[railroadIndex] < ModelConstants.STOCK_BUNDLE_SIZE) {
                return Optional.of("You don't hold enough of these shares.");
            }
            return Optional.empty();
        }

        FinancialDataAggregator railroadData = aggregate(world, railroad.get());
        if (railroadData.sharesHeldByPublic() < ModelConstants.STOCK_BUNDLE_SIZE) {
            return Optional.of("Not enough shares are available.");
        }
        if (railroadId == player.getId()
                && ModelConstants.STOCK_BUNDLE_SIZE + railroadData.treasuryStock() >= railroadData.totalShares()) {
            return Optional.of("Not enough shares are available.");
        }
        if (sharePrice(world, player, railroadId, quantity).amount()
                > world.getCurrentBalance(player).amount() / ModelConstants.STOCK_BUNDLE_SIZE) {
            return Optional.of("You can't afford that!");
        }
        return Optional.empty();
    }

    /**
     * Returns the price per share of a trade. Trading a railroad's own shares
     * uses its treasury prices.
     *
     * @param world the world
     * @param player the trading player
     * @param railroadId the player ID of the railroad whose shares are traded
     * @param quantity the number of shares, positive to buy and negative to sell
     * @return the price per share
     */
    public static Money sharePrice(UnmodifiableWorld world, Player player, int railroadId, int quantity) {
        StockPrice stockPrice = new StockPriceCalculator(world).calculate()[world.getPlayerIndex(railroadId)];
        if (railroadId == player.getId()) {
            return quantity > 0 ? stockPrice.treasuryBuyPrice : stockPrice.treasurySellPrice;
        }
        return quantity > 0 ? stockPrice.buyPrice : stockPrice.sellPrice;
    }

    /**
     * Returns the transaction of a share trade at the current price. Check the
     * trade with {@link #checkShareTrade} first.
     *
     * @param world the world
     * @param player the trading player
     * @param railroadId the player ID of the railroad whose shares are traded
     * @param quantity the number of shares, positive to buy and negative to sell
     * @return the share transfer transaction
     */
    public static ItemTransaction shareTrade(UnmodifiableWorld world, Player player, int railroadId, int quantity) {
        return TransactionUtils.buyOrSellStock(railroadId, quantity,
                sharePrice(world, player, railroadId, quantity), world.getClock().getCurrentTime());
    }

    /**
     * Checks whether a player may issue another bond.
     *
     * @param world the world
     * @param player the player
     * @return the reason no bond may be issued, or empty if one may
     */
    public static Optional<String> checkIssueBond(UnmodifiableWorld world, Player player) {
        if (!aggregate(world, player).canIssueBond()) {
            return Optional.of("The maximum number of bonds is already outstanding.");
        }
        return Optional.empty();
    }

    /**
     * Returns the transaction that issues one bond at the current interest rate.
     *
     * @param world the world
     * @param player the player
     * @return the bond transaction
     */
    public static BondItemTransaction issueBond(UnmodifiableWorld world, Player player) {
        return TransactionUtils.issueBond(aggregate(world, player).nextBondInterestRate(),
                world.getClock().getCurrentTime());
    }

    /**
     * Checks whether a player may repay a bond: one must be outstanding and the
     * player must be able to pay for it.
     *
     * @param world the world
     * @param player the player
     * @return the reason no bond may be repaid, or empty if one may
     */
    public static Optional<String> checkRepayBond(UnmodifiableWorld world, Player player) {
        if (aggregate(world, player).getBonds() <= 0) {
            return Optional.of("There are no bonds to repay.");
        }
        if (!Money.add(world.getCurrentBalance(player), ModelConstants.BOND_VALUE_REPAY).isNonNegative()) {
            return Optional.of("You can't afford that!");
        }
        return Optional.empty();
    }

    /**
     * Returns the transaction that repays one bond: the outstanding bond with the
     * highest interest rate. Check with {@link #checkRepayBond} first.
     *
     * @param world the world
     * @param player the player
     * @return the bond transaction
     */
    public static BondItemTransaction repayBond(UnmodifiableWorld world, Player player) {
        List<Double> rates = outstandingBondRates(world, player);
        return TransactionUtils.repayBond(rates.getLast(), world.getClock().getCurrentTime());
    }

    /**
     * Returns the interest rates of the player's outstanding bonds, lowest first.
     * Each issued bond adds its rate; each repayment removes the bond with the
     * repaid rate.
     *
     * @param world the world
     * @param player the player
     * @return the interest rates, in percent, sorted ascending
     */
    public static List<Double> outstandingBondRates(UnmodifiableWorld world, Player player) {
        List<Double> rates = new ArrayList<>();
        for (Transaction transaction : world.getTransactions(player)) {
            if (transaction instanceof BondItemTransaction bond) {
                for (int i = 0; i < bond.getQuantity(); i++) {
                    rates.add(bond.getInterestRate());
                }
                for (int i = 0; i < -bond.getQuantity(); i++) {
                    if (!rates.remove(bond.getInterestRate()) && !rates.isEmpty()) {
                        // A repayment without a matching bond repays the most expensive one.
                        rates.remove(Collections.max(rates));
                    }
                }
            }
        }
        Collections.sort(rates);
        return rates;
    }

    /**
     * Returns the yearly interest due on the player's outstanding bonds: each
     * bond pays its interest rate on {@link ModelConstants#BOND_VALUE_ISSUE}.
     *
     * @param world the world
     * @param player the player
     * @return the interest due, zero or positive
     */
    public static Money yearlyBondInterest(UnmodifiableWorld world, Player player) {
        long interest = 0;
        for (double rate : outstandingBondRates(world, player)) {
            interest = Math.addExact(interest, Math.round(ModelConstants.BOND_VALUE_ISSUE.amount() * rate / 100.0));
        }
        return new Money(interest);
    }

    private static FinancialDataAggregator aggregate(UnmodifiableWorld world, Player player) {
        FinancialDataAggregator aggregator = new FinancialDataAggregator(world, player);
        aggregator.aggregate();
        return aggregator;
    }
}

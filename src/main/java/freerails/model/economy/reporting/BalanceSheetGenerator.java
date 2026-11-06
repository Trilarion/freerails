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

package freerails.model.economy.reporting;

import freerails.model.economy.Statistics;
import freerails.model.simulation.Clock;
import freerails.model.simulation.Time;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import java.util.Map;

/**
 * Calculates lifetime and year-to-date balance-sheet statistics for a player.
 */
public class BalanceSheetGenerator {

    /** Lifetime statistics for the player's balance sheet. */
    public final Statistics total;
    /** Year-to-date statistics for the player's balance sheet. */
    public final Statistics ytd;
    private final int year;

    /**
     * Builds statistics through the current game time for the specified player.
     *
     * @param world world state supplying the clock and transactions
     * @param player player whose balance sheet is calculated
     */
    public BalanceSheetGenerator(UnmodifiableWorld world, Player player) {
        // Calculate totals
        Clock clock = world.getClock();
        year = clock.getCurrentYear();
        Time[] totalTimeInterval = new Time[]{Time.ZERO, world.getClock().getCurrentTime().advance()};

        total = new Statistics(world, player, totalTimeInterval);

        Time[] ytdTimeInterval = new Time[]{clock.getTimeAtStartOfCurrentYear(), world.getClock().getCurrentTime().advance()};
        ytd = new Statistics(world, player, ytdTimeInterval);
    }

    /**
     * Returns the string values consumed by the balance-sheet HTML template.
     *
     * @return all balance-sheet template values, indexed by token
     */
    public Map<String, String> getTemplateValues() {
        return Map.ofEntries(
                Map.entry("year", Integer.toString(year)),
                Map.entry("total.operatingFunds", total.getOperatingFunds().toString()),
                Map.entry("ytd.operatingFunds", ytd.getOperatingFunds().toString()),
                Map.entry("total.track", total.getTrack().toString()),
                Map.entry("ytd.track", ytd.getTrack().toString()),
                Map.entry("total.stations", total.getStations().toString()),
                Map.entry("ytd.stations", ytd.getStations().toString()),
                Map.entry("total.rollingStock", total.getRollingStock().toString()),
                Map.entry("ytd.rollingStock", ytd.getRollingStock().toString()),
                Map.entry("total.industries", total.getIndustries().toString()),
                Map.entry("ytd.industries", ytd.getIndustries().toString()),
                Map.entry("total.treasuryStock", total.getTreasuryStock().toString()),
                Map.entry("ytd.treasuryStock", ytd.getTreasuryStock().toString()),
                Map.entry("total.otherRrStock", total.getOtherRrStock().toString()),
                Map.entry("ytd.otherRrStock", ytd.getOtherRrStock().toString()),
                Map.entry("total.loans", total.getLoans().toString()),
                Map.entry("ytd.loans", ytd.getLoans().toString()),
                Map.entry("total.equity", total.getEquity().toString()),
                Map.entry("ytd.equity", ytd.getEquity().toString()),
                Map.entry("total.profit", total.getProfit().toString()),
                Map.entry("ytd.profit", ytd.getProfit().toString()));
    }

}

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

import freerails.model.operations.cargo.Cargo;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.CargoDeliveryTransaction;
import freerails.model.economy.transaction.Transaction;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

import java.util.Map;

/**
 * Calculates income-statement totals for a player.
 */
public class IncomeStatementGenerator {

    private final UnmodifiableWorld world;
    private final Player player;
    private final int startYear;

    private Money mailYtd = Money.ZERO;
    /** Total income from mail carried by the player's trains. */
    public Money mailTotal = Money.ZERO;
    private Money passengersYtd = Money.ZERO;
    private Money passengersTotal = Money.ZERO;
    private Money fastFreightYtd = Money.ZERO;
    private Money fastFreightTotal = Money.ZERO;
    private Money slowFreightYtd = Money.ZERO;
    private Money slowFreightTotal = Money.ZERO;
    private Money bulkFreightYtd = Money.ZERO;
    private Money bulkFreightTotal = Money.ZERO;
    private Money interestYtd = Money.ZERO;
    private Money interestTotal = Money.ZERO;
    private Money trainMaintenanceYtd = Money.ZERO;
    private Money trainMaintenanceTotal = Money.ZERO;
    private Money trackMaintenanceYtd = Money.ZERO;
    private Money trackMaintenanceTotal = Money.ZERO;
    private Money stationMaintenanceYtd = Money.ZERO;
    private Money stationMaintenanceTotal = Money.ZERO;
    private Money profitYtd = Money.ZERO;
    private Money profitTotal = Money.ZERO;

    /**
     * Creates a generator using the current game year as the year-to-date
     * boundary.
     *
     * @param world world state containing the clock, trains, and transactions
     * @param player player whose income is summarized
     */
    public IncomeStatementGenerator(UnmodifiableWorld world, Player player) {
        this.world = world;
        this.player = player;
        startYear = world.getClock().getCurrentYear();
    }

    /** Calculates the income and maintenance totals from the player's transactions. */
    public void calculateAll() {
        long mailTotal = 0;
        long passengersTotal = 0;
        long fastFreightTotal = 0;
        long slowFreightTotal = 0;
        long bulkFreightTotal = 0;
        long interestTotal = 0;
        long trainMaintenanceTotal = 0;
        long trackMaintenanceTotal = 0;
        long stationMaintenanceTotal = 0;
        long mailYtd = 0;
        long passengersYtd = 0;
        long fastFreightYtd = 0;
        long slowFreightYtd = 0;
        long bulkFreightYtd = 0;
        long interestYtd = 0;
        long trainMaintenanceYtd = 0;
        long trackMaintenanceYtd = 0;
        long stationMaintenanceYtd = 0;

        for (Transaction transaction: world.getTransactions(player)) {
            int transactionYear = world.getClock().getYear(transaction.getTime());
            // TODO shortcut out if transactionYear < startYear?
            if (transaction instanceof CargoDeliveryTransaction cargoDeliveryTransaction) {
                int cargoTypeId = cargoDeliveryTransaction.getCargoBatch().getCargoTypeId();
                Cargo cargo = world.getCargo(cargoTypeId);
                switch (cargo.getCategory()) {
                    case BULK_FREIGHT:
                        bulkFreightTotal += cargoDeliveryTransaction.getAmount().amount();
                        if (transactionYear >= startYear) {
                            bulkFreightYtd += cargoDeliveryTransaction.getAmount().amount();
                        }
                        break;
                    case FAST_FREIGHT:
                        fastFreightTotal += cargoDeliveryTransaction.getAmount().amount();
                        if (transactionYear >= startYear) {
                            fastFreightYtd += cargoDeliveryTransaction.getAmount().amount();
                        }
                        break;
                    case MAIL:
                        mailTotal += cargoDeliveryTransaction.getAmount().amount();
                        if (transactionYear >= startYear) {
                            mailYtd += cargoDeliveryTransaction.getAmount().amount();
                        }
                        break;
                    case PASSENGER:
                        passengersTotal += cargoDeliveryTransaction.getAmount().amount();
                        if (transactionYear >= startYear) {
                            passengersYtd += cargoDeliveryTransaction.getAmount().amount();
                        }
                        break;
                    case SLOW_FREIGHT:
                        slowFreightTotal += cargoDeliveryTransaction.getAmount().amount();
                        if (transactionYear >= startYear) {
                            slowFreightYtd += cargoDeliveryTransaction.getAmount().amount();
                        }
                        break;
                }
            }
            switch (transaction.getCategory()) {
                case INTEREST_CHARGE:
                    interestTotal += transaction.getAmount().amount();
                    if (transactionYear >= startYear) {
                        interestYtd += transaction.getAmount().amount();
                    }
                    break;
                case TRAIN_MAINTENANCE:
                    trainMaintenanceTotal += transaction.getAmount().amount();
                    if (transactionYear >= startYear) {
                        trainMaintenanceYtd += transaction.getAmount().amount();
                    }
                    break;
                case TRACK_MAINTENANCE:
                    trackMaintenanceTotal += transaction.getAmount().amount();
                    if (transactionYear >= startYear) {
                        trackMaintenanceYtd += transaction.getAmount().amount();
                    }
                    break;
                case STATION_MAINTENANCE:
                    stationMaintenanceTotal += transaction.getAmount().amount();
                    if (transactionYear >= startYear) {
                        stationMaintenanceYtd += transaction.getAmount().amount();
                    }
                    break;
            }
        }
        this.mailTotal = new Money(mailTotal);
        this.passengersTotal = new Money(passengersTotal);
        this.fastFreightTotal = new Money(fastFreightTotal);
        this.slowFreightTotal = new Money(slowFreightTotal);
        this.bulkFreightTotal = new Money(bulkFreightTotal);
        this.mailYtd = new Money(mailYtd);
        this.passengersYtd = new Money(passengersYtd);
        this.fastFreightYtd = new Money(fastFreightYtd);
        this.slowFreightYtd = new Money(slowFreightYtd);
        this.bulkFreightYtd = new Money(bulkFreightYtd);
        this.interestTotal = new Money(interestTotal);
        this.interestYtd = new Money(interestYtd);
        this.trainMaintenanceTotal = new Money(trainMaintenanceTotal);
        this.trainMaintenanceYtd = new Money(trainMaintenanceYtd);
        this.trackMaintenanceTotal = new Money(trackMaintenanceTotal);
        this.trackMaintenanceYtd = new Money(trackMaintenanceYtd);
        this.stationMaintenanceTotal = new Money(stationMaintenanceTotal);
        this.stationMaintenanceYtd = new Money(stationMaintenanceYtd);
        profitTotal = sum(mailTotal, passengersTotal, fastFreightTotal, slowFreightTotal, bulkFreightTotal,
                interestTotal, trainMaintenanceTotal, trackMaintenanceTotal, stationMaintenanceTotal);
        profitYtd = sum(mailYtd, passengersYtd, fastFreightYtd, slowFreightYtd, bulkFreightYtd,
                interestYtd, trainMaintenanceYtd, trackMaintenanceYtd, stationMaintenanceYtd);
    }

    /**
     * Adds the statement components with overflow checking.
     *
     * @param amounts component amounts
     * @return the sum of all amounts
     */
    private static Money sum(long... amounts) {
        long total = 0;
        for (long amount : amounts) {
            total = Math.addExact(total, amount);
        }
        return new Money(total);
    }

    /**
     * Returns the string values consumed by the income-statement template.
     *
     * @return all income-statement template values, indexed by token
     */
    public Map<String, String> getTemplateValues() {
        return Map.ofEntries(
                Map.entry("year", Integer.toString(startYear)),
                Map.entry("mailYtd", mailYtd.toString()),
                Map.entry("mailTotal", mailTotal.toString()),
                Map.entry("passengersYtd", passengersYtd.toString()),
                Map.entry("passengersTotal", passengersTotal.toString()),
                Map.entry("fastFreightYtd", fastFreightYtd.toString()),
                Map.entry("fastFreightTotal", fastFreightTotal.toString()),
                Map.entry("slowFreightYtd", slowFreightYtd.toString()),
                Map.entry("slowFreightTotal", slowFreightTotal.toString()),
                Map.entry("bulkFreightYtd", bulkFreightYtd.toString()),
                Map.entry("bulkFreightTotal", bulkFreightTotal.toString()),
                Map.entry("interestYtd", interestYtd.toString()),
                Map.entry("interestTotal", interestTotal.toString()),
                Map.entry("trainMaintenanceYtd", trainMaintenanceYtd.toString()),
                Map.entry("trainMaintenanceTotal", trainMaintenanceTotal.toString()),
                Map.entry("trackMaintenanceYtd", trackMaintenanceYtd.toString()),
                Map.entry("trackMaintenanceTotal", trackMaintenanceTotal.toString()),
                Map.entry("stationMaintenanceYtd", stationMaintenanceYtd.toString()),
                Map.entry("stationMaintenanceTotal", stationMaintenanceTotal.toString()),
                Map.entry("profitYtd", profitYtd.toString()),
                Map.entry("profitTotal", profitTotal.toString()));
    }

    /**
     * Fills an array with each train's cargo-delivery revenue for the current
     * year; extra array slots are set to {@link Money#ZERO}.
     *
     * @param money destination array, with at least one slot per train
     * @throws IllegalArgumentException if the array has fewer slots than trains
     */
    public void calTrainRevenue(Money[] money) {
        int trainCount = world.getTrains(player).size();
        if (money.length < trainCount) {
            throw new IllegalArgumentException("Revenue array must have one slot per train.");
        }
        long[] amount = new long[trainCount];

        for (Transaction transaction: world.getTransactions(player)) {
            int transactionYear = world.getClock().getYear(transaction.getTime());
            if (transaction instanceof CargoDeliveryTransaction cargoDeliveryTransaction && transactionYear >= startYear) {
                int trainId = cargoDeliveryTransaction.getTrainId();
                int trainIndex = world.getTrainIndex(player, trainId);
                amount[trainIndex] += cargoDeliveryTransaction.getAmount().amount();
            }
        }
        for (int i = 0; i < money.length; i++) {
            money[i] = i < trainCount ? new Money(amount[i]) : Money.ZERO;
        }
    }
}
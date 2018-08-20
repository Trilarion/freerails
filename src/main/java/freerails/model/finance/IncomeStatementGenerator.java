/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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

import freerails.model.cargo.Cargo;
import freerails.model.finance.transaction.CargoDeliveryTransaction;
import freerails.model.finance.transaction.Transaction;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

/**
 * Generates the income statement- note, its fields are read using reflection so
 * don't change their names.
 */
public class IncomeStatementGenerator {

    private final UnmodifiableWorld world;
    private final Player player;
    private final int startyear;

    public Money mailTotal;

    public IncomeStatementGenerator(UnmodifiableWorld world, Player player) {
        this.world = world;
        this.player = player;
        startyear = world.getClock().getCurrentYear();
    }

    /**
     * calculates all public values
     */
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

        for (Transaction transaction: world.getTransactions(this.player)) {
            int transactionYear = world.getClock().getYear(transaction.getTime());
            // TODO shortcut out if transactionYear < startYear?
            if (transaction instanceof CargoDeliveryTransaction) {
                CargoDeliveryTransaction cargoDeliveryTransaction = (CargoDeliveryTransaction) transaction;
                int cargoTypeId = cargoDeliveryTransaction.getCargoBatch().getCargoTypeId();
                Cargo cargo = world.getCargo(cargoTypeId);
                switch (cargo.getCategory()) {
                    case BULK_FREIGHT:
                        bulkFreightTotal += cargoDeliveryTransaction.getAmount().amount;
                        if (transactionYear >= this.startyear) {
                            bulkFreightYtd += cargoDeliveryTransaction.getAmount().amount;
                        }
                        break;
                    case FAST_FREIGHT:
                        fastFreightTotal += cargoDeliveryTransaction.getAmount().amount;
                        if (transactionYear >= this.startyear) {
                            fastFreightYtd += cargoDeliveryTransaction.getAmount().amount;
                        }
                        break;
                    case MAIL:
                        mailTotal += cargoDeliveryTransaction.getAmount().amount;
                        if (transactionYear >= this.startyear) {
                            mailYtd += cargoDeliveryTransaction.getAmount().amount;
                        }
                        break;
                    case PASSENGER:
                        passengersTotal += cargoDeliveryTransaction.getAmount().amount;
                        if (transactionYear >= this.startyear) {
                            passengersYtd += cargoDeliveryTransaction.getAmount().amount;
                        }
                        break;
                    case SLOW_FREIGHT:
                        slowFreightTotal += cargoDeliveryTransaction.getAmount().amount;
                        if (transactionYear >= this.startyear) {
                            slowFreightYtd += cargoDeliveryTransaction.getAmount().amount;
                        }
                        break;
                }
            }
            switch (transaction.getCategory()) {
                case INTEREST_CHARGE:
                    interestTotal += transaction.getAmount().amount;
                    if (transactionYear >= this.startyear) {
                        interestYtd += transaction.getAmount().amount;
                    }
                    break;
                case TRAIN_MAINTENANCE:
                    trainMaintenanceTotal += transaction.getAmount().amount;
                    if (transactionYear >= this.startyear) {
                        trainMaintenanceYtd += transaction.getAmount().amount;
                    }
                    break;
                case TRACK_MAINTENANCE:
                    trackMaintenanceTotal += transaction.getAmount().amount;
                    if (transactionYear >= this.startyear) {
                        trackMaintenanceYtd += transaction.getAmount().amount;
                    }
                    break;
                case STATION_MAINTENANCE:
                    stationMaintenanceTotal += transaction.getAmount().amount;
                    if (transactionYear >= this.startyear) {
                        stationMaintenanceYtd += transaction.getAmount().amount;
                    }
                    break;
            }
        }
        this.mailTotal = new Money(mailTotal);
        Money passengersTotal1 = new Money(passengersTotal);
        Money fastFreightTotal1 = new Money(fastFreightTotal);
        Money slowFreightTotal1 = new Money(slowFreightTotal);
        Money bulkFreightTotal1 = new Money(bulkFreightTotal);
        Money mailYtd1 = new Money(mailYtd);
        Money passengersYtd1 = new Money(passengersYtd);
        Money fastFreightYtd1 = new Money(fastFreightYtd);
        Money slowFreightYtd1 = new Money(slowFreightYtd);
        Money bulkFreightYtd1 = new Money(bulkFreightYtd);

        Money interestTotal1 = new Money(interestTotal);
        Money interestYtd1 = new Money(interestYtd);
        Money trainMaintenanceTotal1 = new Money(trainMaintenanceTotal);
        Money trainMaintenanceYtd1 = new Money(trainMaintenanceYtd);
        Money trackMaintenanceTotal1 = new Money(trackMaintenanceTotal);
        Money trackMaintenanceYtd1 = new Money(trackMaintenanceYtd);
        Money stationMaintenanceTotal1 = new Money(stationMaintenanceTotal);
        Money stationMaintenanceYtd1 = new Money(stationMaintenanceYtd);
    }

    /**
     * returns the revenue for all trains with id from 1 to money.length-1
     */
    public void calTrainRevenue(Money[] money) {
        // TODO use Money arithmetic
        long[] amount = new long[money.length];

        for (Transaction transaction: world.getTransactions(this.player)) {
            int transactionYear = world.getClock().getYear(transaction.getTime());
            if (transaction instanceof CargoDeliveryTransaction && transactionYear >= this.startyear) {
                CargoDeliveryTransaction cargoDeliveryTransaction = (CargoDeliveryTransaction) transaction;
                int trainId = cargoDeliveryTransaction.getTrainId();
                if (trainId < money.length) {
                    amount[trainId] += cargoDeliveryTransaction.getAmount().amount;
                }
            }
        }
        int i = 0;
        for (long a : amount) {
            money[i++] = new Money(a);
        }
    }
}
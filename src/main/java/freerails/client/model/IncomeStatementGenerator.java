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

package freerails.client.model;

import freerails.util.Pair;
import freerails.world.ITEM;
import freerails.world.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.cargo.CargoType;
import freerails.world.finances.CargoDeliveryMoneyTransaction;
import freerails.world.finances.Money;
import freerails.world.finances.Transaction;
import freerails.world.game.GameCalendar;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;

/**
 * Generates the income statement- note, its fields are read using reflection so
 * don't change their names.
 */
public class IncomeStatementGenerator {

    private final ReadOnlyWorld world;
    private final FreerailsPrincipal principal;
    private final GameCalendar cal;
    private final int startyear;

    public Money mailTotal;

    public IncomeStatementGenerator(ReadOnlyWorld world, FreerailsPrincipal principal) {
        this.world = world;
        this.principal = principal;
        cal = (GameCalendar) world.get(ITEM.CALENDAR);
        GameTime time = world.currentTime();
        startyear = cal.getYear(time.getTicks());
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

        int numberOfTransactions = world.getNumberOfTransactions(this.principal);
        for (int i = 0; i < numberOfTransactions; i++) {
            Pair<Transaction, GameTime> transactionAndTimeStamp = world.getTransactionAndTimeStamp(principal, i);
            Transaction transaction = transactionAndTimeStamp.getA();
            GameTime time = transactionAndTimeStamp.getB();
            if (transaction instanceof CargoDeliveryMoneyTransaction) {
                CargoDeliveryMoneyTransaction cargoDeliveryMoneyTransaction = (CargoDeliveryMoneyTransaction) transaction;
                int cargoType = cargoDeliveryMoneyTransaction.getCargoBatch().getCargoType();
                CargoType ct = (CargoType) world.get(SKEY.CARGO_TYPES, cargoType);
                switch (ct.getCategory()) {
                    case Bulk_Freight:
                        bulkFreightTotal += cargoDeliveryMoneyTransaction.price().amount;
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            bulkFreightYtd += cargoDeliveryMoneyTransaction.price().amount;
                        }
                        break;
                    case Fast_Freight:
                        fastFreightTotal += cargoDeliveryMoneyTransaction.price().amount;
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            fastFreightYtd += cargoDeliveryMoneyTransaction.price().amount;
                        }
                        break;
                    case Mail:
                        mailTotal += cargoDeliveryMoneyTransaction.price().amount;
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            mailYtd += cargoDeliveryMoneyTransaction.price().amount;
                        }
                        break;
                    case Passengers:
                        passengersTotal += cargoDeliveryMoneyTransaction.price().amount;
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            passengersYtd += cargoDeliveryMoneyTransaction.price().amount;
                        }
                        break;
                    case Slow_Freight:
                        slowFreightTotal += cargoDeliveryMoneyTransaction.price().amount;
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            slowFreightYtd += cargoDeliveryMoneyTransaction.price().amount;
                        }
                        break;
                }
            }
            switch (transaction.getCategory()) {
                case INTEREST_CHARGE:
                    interestTotal += transaction.price().amount;
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        interestYtd += transaction.price().amount;
                    }
                    break;
                case TRAIN_MAINTENANCE:
                    trainMaintenanceTotal += transaction.price().amount;
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        trainMaintenanceYtd += transaction.price().amount;
                    }
                    break;
                case TRACK_MAINTENANCE:
                    trackMaintenanceTotal += transaction.price().amount;
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        trackMaintenanceYtd += transaction.price().amount;
                    }
                    break;
                case STATION_MAINTENANCE:
                    stationMaintenanceTotal += transaction.price().amount;
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        stationMaintenanceYtd += transaction.price().amount;
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

        int numberOfTransactions = world.getNumberOfTransactions(this.principal);
        for (int i = 0; i < numberOfTransactions; i++) {
            Pair<Transaction, GameTime> transactionAndTimeStamp = world.getTransactionAndTimeStamp(principal, i);
            Transaction transaction = transactionAndTimeStamp.getA();
            GameTime time = transactionAndTimeStamp.getB();
            if (transaction instanceof CargoDeliveryMoneyTransaction && cal.getYear(time.getTicks()) >= this.startyear) {
                CargoDeliveryMoneyTransaction cargoDeliveryMoneyTransaction = (CargoDeliveryMoneyTransaction) transaction;
                int trainId = cargoDeliveryMoneyTransaction.getTrainId();
                if (trainId < money.length) {
                    amount[trainId] += cargoDeliveryMoneyTransaction.price().amount;
                }
            }
        }
        int i = 0;
        for (long a : amount) {
            money[i++] = new Money(a);
        }
    }
}
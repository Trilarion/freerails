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

package freerails.client.view;

import freerails.util.Pair;
import freerails.world.ITEM;
import freerails.world.ReadOnlyWorld;
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
class IncomeStatementGenerator {

    private final String year;
    private final ReadOnlyWorld world;
    private final FreerailsPrincipal principal;
    private final GameCalendar cal;
    private final int startyear;

    public Money mailTotal;
    private Money passengersTotal;
    private Money fastFreightTotal;
    private Money slowFreightTotal;
    private Money bulkFreightTotal;
    private Money interestTotal;
    private Money trainMaintenanceTotal;
    private Money trackMaintenanceTotal;
    private Money stationMaintenanceTotal;
    private Money profitTotal;
    private Money mailYtd;
    private Money passengersYtd;
    private Money fastFreightYtd;
    private Money slowFreightYtd;
    private Money bulkFreightYtd;
    private Money interestYtd;
    private Money trainMaintenanceYtd;
    private Money trackMaintenanceYtd;
    private Money stationMaintenanceYtd;
    private Money profitYtd;
    GameTime from;
    GameTime to;

    IncomeStatementGenerator(ReadOnlyWorld world, FreerailsPrincipal principal) {
        this.world = world;
        this.principal = principal;
        cal = (GameCalendar) world.get(ITEM.CALENDAR);
        GameTime time = world.currentTime();
        startyear = cal.getYear(time.getTicks());
        year = String.valueOf(startyear);
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
            Transaction t = transactionAndTimeStamp.getA();
            GameTime time = transactionAndTimeStamp.getB();
            if (t instanceof CargoDeliveryMoneyTransaction) {
                CargoDeliveryMoneyTransaction dcr = (CargoDeliveryMoneyTransaction) t;
                int cargoType = dcr.getCargoBatch().getCargoType();
                CargoType ct = (CargoType) world.get(SKEY.CARGO_TYPES, cargoType);
                switch (ct.getCategory()) {
                    case Bulk_Freight:
                        bulkFreightTotal += dcr.value().getAmount();
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            bulkFreightYtd += dcr.value().getAmount();
                        }
                        break;
                    case Fast_Freight:
                        fastFreightTotal += dcr.value().getAmount();
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            fastFreightYtd += dcr.value().getAmount();
                        }
                        break;
                    case Mail:
                        mailTotal += dcr.value().getAmount();
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            mailYtd += dcr.value().getAmount();
                        }
                        break;
                    case Passengers:
                        passengersTotal += dcr.value().getAmount();
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            passengersYtd += dcr.value().getAmount();
                        }
                        break;
                    case Slow_Freight:
                        slowFreightTotal += dcr.value().getAmount();
                        if (cal.getYear(time.getTicks()) >= this.startyear) {
                            slowFreightYtd += dcr.value().getAmount();
                        }
                        break;
                }

            }
            switch (t.getCategory()) {
                case INTEREST_CHARGE:
                    interestTotal += t.value().getAmount();
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        interestYtd += t.value().getAmount();
                    }
                    break;
                case TRAIN_MAINTENANCE:
                    trainMaintenanceTotal += t.value().getAmount();
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        trainMaintenanceYtd += t.value().getAmount();
                    }
                    break;
                case TRACK_MAINTENANCE:
                    trackMaintenanceTotal += t.value().getAmount();
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        trackMaintenanceYtd += t.value().getAmount();
                    }
                    break;
                case STATION_MAINTENANCE:
                    stationMaintenanceTotal += t.value().getAmount();
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        stationMaintenanceYtd += t.value().getAmount();
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

        long profit = this.mailTotal.getAmount() + this.passengersTotal.getAmount() + this.fastFreightTotal.getAmount() + this.slowFreightTotal.getAmount() + this.bulkFreightTotal.getAmount() + this.interestTotal.getAmount() + this.trainMaintenanceTotal.getAmount() + this.trackMaintenanceTotal.getAmount() + this.stationMaintenanceTotal.getAmount();

        profitTotal = new Money(profit);

        profit = this.mailYtd.getAmount() + this.passengersYtd.getAmount() + this.fastFreightYtd.getAmount() + this.slowFreightYtd.getAmount() + this.bulkFreightYtd.getAmount() + this.interestYtd.getAmount() + this.trainMaintenanceYtd.getAmount() + this.trackMaintenanceYtd.getAmount() + this.stationMaintenanceYtd.getAmount();

        profitYtd = new Money(profit);
    }

    /**
     * returns the revenue for all trains with id from 1 to money.length-1
     */
    public void calTrainRevenue(Money[] money) {
        long[] amount = new long[money.length];

        int numberOfTransactions = world.getNumberOfTransactions(this.principal);
        for (int i = 0; i < numberOfTransactions; i++) {
            Pair<Transaction, GameTime> transactionAndTimeStamp = world.getTransactionAndTimeStamp(principal, i);
            Transaction t = transactionAndTimeStamp.getA();
            GameTime time = transactionAndTimeStamp.getB();
            if (t instanceof CargoDeliveryMoneyTransaction && cal.getYear(time.getTicks()) >= this.startyear) {
                CargoDeliveryMoneyTransaction dcr = (CargoDeliveryMoneyTransaction) t;
                int trainId = dcr.getTrainId();
                if (trainId < money.length) {
                    amount[trainId] += dcr.value().getAmount();
                }
            }
        }
        int i = 0;
        for (long a : amount) {
            money[i++] = new Money(a);
        }
    }
}
/*
 * Created on Mar 28, 2004
 */
package jfreerails.client.view;

import jfreerails.util.Pair;
import jfreerails.world.accounts.DeliverCargoReceipt;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;

/**
 * Generates the income statement- note, its fields are read using reflection so
 * don't change their names.
 * 
 * @author Luke
 * 
 */
public class IncomeStatementGenerator {
    GameTime from;

    GameTime to;

    final ReadOnlyWorld w;

    final FreerailsPrincipal principal;

    private GameCalendar cal;

    public Money mailTotal;

    public Money passengersTotal;

    public Money fastFreightTotal;

    public Money slowFreightTotal;

    public Money bulkFreightTotal;

    public Money interestTotal;

    public Money trainMaintenanceTotal;

    public Money trackMaintenanceTotal;

    public Money stationMaintenanceTotal;

    public Money profitTotal;

    public Money mailYtd;

    public Money passengersYtd;

    public Money fastFreightYtd;

    public Money slowFreightYtd;

    public Money bulkFreightYtd;

    public Money interestYtd;

    public Money trainMaintenanceYtd;

    public Money trackMaintenanceYtd;

    public Money stationMaintenanceYtd;

    public Money profitYtd;

    public String year;

    private int startyear;

    IncomeStatementGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        this.w = w;
        this.principal = principal;
        cal = (GameCalendar) w.get(ITEM.CALENDAR);
        GameTime time = w.currentTime();
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

        int numberOfTransactions = w.getNumberOfTransactions(this.principal);
        for (int i = 0; i < numberOfTransactions; i++) {
            Pair<Transaction, GameTime> transactionAndTimeStamp = w
                    .getTransactionAndTimeStamp(principal, i);
            Transaction t = transactionAndTimeStamp.getA();
            GameTime time = transactionAndTimeStamp.getB();
            if (t instanceof DeliverCargoReceipt) {
                DeliverCargoReceipt dcr = (DeliverCargoReceipt) t;
                int cargoType = dcr.getCb().getCargoType();
                CargoType ct = (CargoType) w.get(SKEY.CARGO_TYPES, cargoType);
                switch (ct.getCategory()) {
                case Bulk_Freight:
                    bulkFreightTotal += dcr.deltaCash().getAmount();
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        bulkFreightYtd += dcr.deltaCash().getAmount();
                    }
                    break;
                case Fast_Freight:
                    fastFreightTotal += dcr.deltaCash().getAmount();
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        fastFreightYtd += dcr.deltaCash().getAmount();
                    }
                    break;
                case Mail:
                    mailTotal += dcr.deltaCash().getAmount();
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        mailYtd += dcr.deltaCash().getAmount();
                    }
                    break;
                case Passengers:
                    passengersTotal += dcr.deltaCash().getAmount();
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        passengersYtd += dcr.deltaCash().getAmount();
                    }
                    break;
                case Slow_Freight:
                    slowFreightTotal += dcr.deltaCash().getAmount();
                    if (cal.getYear(time.getTicks()) >= this.startyear) {
                        slowFreightYtd += dcr.deltaCash().getAmount();
                    }
                    break;
                }

            }
            switch (t.getCategory()) {
            case INTEREST_CHARGE:
                interestTotal += t.deltaCash().getAmount();
                if (cal.getYear(time.getTicks()) >= this.startyear) {
                    interestYtd += t.deltaCash().getAmount();
                }
                break;
            case TRAIN_MAINTENANCE:
                trainMaintenanceTotal += t.deltaCash().getAmount();
                if (cal.getYear(time.getTicks()) >= this.startyear) {
                    trainMaintenanceYtd += t.deltaCash().getAmount();
                }
                break;
            case TRACK_MAINTENANCE:
                trackMaintenanceTotal += t.deltaCash().getAmount();
                if (cal.getYear(time.getTicks()) >= this.startyear) {
                    trackMaintenanceYtd += t.deltaCash().getAmount();
                }
                break;
            case STATION_MAINTENANCE:
                stationMaintenanceTotal += t.deltaCash().getAmount();
                if (cal.getYear(time.getTicks()) >= this.startyear) {
                    stationMaintenanceYtd += t.deltaCash().getAmount();
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

        long profit = this.mailTotal.getAmount()
                + this.passengersTotal.getAmount()
                + this.fastFreightTotal.getAmount()
                + this.slowFreightTotal.getAmount()
                + this.bulkFreightTotal.getAmount()
                + this.interestTotal.getAmount()
                + this.trainMaintenanceTotal.getAmount()
                + this.trackMaintenanceTotal.getAmount()
                + this.stationMaintenanceTotal.getAmount();

        profitTotal = new Money(profit);

        profit = this.mailYtd.getAmount() + this.passengersYtd.getAmount()
                + this.fastFreightYtd.getAmount()
                + this.slowFreightYtd.getAmount()
                + this.bulkFreightYtd.getAmount()
                + this.interestYtd.getAmount()
                + this.trainMaintenanceYtd.getAmount()
                + this.trackMaintenanceYtd.getAmount()
                + this.stationMaintenanceYtd.getAmount();

        profitYtd = new Money(profit);
    }

    // 666 save old values -> if numberOfTransactions not changed -> do nothing
    public Money calTrainRevenue(int trainId) {
        long amount = 0;

        int numberOfTransactions = w.getNumberOfTransactions(this.principal);
        for (int i = 0; i < numberOfTransactions; i++) {
            Pair<Transaction, GameTime> transactionAndTimeStamp = w
                    .getTransactionAndTimeStamp(principal, i);
            Transaction t = transactionAndTimeStamp.getA();
            GameTime time = transactionAndTimeStamp.getB();
            if (t instanceof DeliverCargoReceipt
                    && cal.getYear(time.getTicks()) >= this.startyear) {
                DeliverCargoReceipt dcr = (DeliverCargoReceipt) t;
                if (dcr.getTrainId() == trainId) {
                    amount += dcr.deltaCash().getAmount();
                }
            }
        }
        return new Money(amount);
    }

    /**
     * returns the revenue for all trains with id from 1 to money.length-1
     * 
     * @param money
     */
    public void calTrainRevenue(Money[] money) {
        long[] amount = new long[money.length];

        int numberOfTransactions = w.getNumberOfTransactions(this.principal);
        for (int i = 0; i < numberOfTransactions; i++) {
            Pair<Transaction, GameTime> transactionAndTimeStamp = w
                    .getTransactionAndTimeStamp(principal, i);
            Transaction t = transactionAndTimeStamp.getA();
            GameTime time = transactionAndTimeStamp.getB();
            if (t instanceof DeliverCargoReceipt
                    && cal.getYear(time.getTicks()) >= this.startyear) {
                DeliverCargoReceipt dcr = (DeliverCargoReceipt) t;
                int trainId = dcr.getTrainId();
                if (trainId < money.length) {
                    amount[trainId] += dcr.deltaCash().getAmount();
                }
            }
        }
        int i = 0;
        for (long a : amount) {
            money[i++] = new Money(a);
        }
    }
}
/*
 * Created on Mar 28, 2004
 */
package jfreerails.client.view;

import static jfreerails.world.accounts.Transaction.Category.INTEREST_CHARGE;
import static jfreerails.world.accounts.Transaction.Category.STATION_MAINTENANCE;
import static jfreerails.world.accounts.Transaction.Category.TRACK_MAINTENANCE;
import static jfreerails.world.accounts.Transaction.Category.TRAIN_MAINTENANCE;
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

	private int startyear = 0;

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

	IncomeStatementGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
		this.w = w;
		this.principal = principal;
		cal = (GameCalendar) w.get(ITEM.CALENDAR);

		// Income from cargo delivery
		mailTotal = calRevenue("Mail");
		passengersTotal = calRevenue("Passengers");
		fastFreightTotal = calRevenue("Fast_Freight");
		slowFreightTotal = calRevenue("Slow_Freight");
		bulkFreightTotal = calRevenue("Bulk_Freight");

		// Expenses.
		interestTotal = calTotal(INTEREST_CHARGE);
		trainMaintenanceTotal = calTotal(TRAIN_MAINTENANCE);
		trackMaintenanceTotal = calTotal(TRACK_MAINTENANCE);
		stationMaintenanceTotal = calTotal(STATION_MAINTENANCE);

		/*
		 * Note, expenses are stored as negative values so we just add
		 * everything up.
		 */
		long profit = mailTotal.getAmount() + passengersTotal.getAmount()
				+ fastFreightTotal.getAmount() + slowFreightTotal.getAmount()
				+ bulkFreightTotal.getAmount() + interestTotal.getAmount()
				+ trainMaintenanceTotal.getAmount()
				+ trackMaintenanceTotal.getAmount()
				+ stationMaintenanceTotal.getAmount();

		profitTotal = new Money(profit);

		GameTime time = w.currentTime();
		startyear = cal.getYear(time.getTicks());

		year = String.valueOf(startyear);

		// Income from cargo delivery
		mailYtd = calRevenue("Mail");
		passengersYtd = calRevenue("Passengers");
		fastFreightYtd = calRevenue("Fast_Freight");
		slowFreightYtd = calRevenue("Slow_Freight");
		bulkFreightYtd = calRevenue("Bulk_Freight");

		// Expenses.
		interestYtd = calTotal(INTEREST_CHARGE);
		trainMaintenanceYtd = calTotal(TRAIN_MAINTENANCE);
		trackMaintenanceYtd = calTotal(TRACK_MAINTENANCE);
		stationMaintenanceYtd = calTotal(STATION_MAINTENANCE);

		/*
		 * Note, expenses are stored as negative values so we just add
		 * everything up.
		 */
		profit = mailYtd.getAmount() + passengersYtd.getAmount()
				+ fastFreightYtd.getAmount() + slowFreightYtd.getAmount()
				+ bulkFreightYtd.getAmount() + interestYtd.getAmount()
				+ trainMaintenanceYtd.getAmount()
				+ trackMaintenanceYtd.getAmount()
				+ stationMaintenanceYtd.getAmount();

		profitYtd = new Money(profit);
	}

	/* Calulates the total revenue from the specified cargo type. */
	Money calRevenue(String cargoCategory) {
		long amount = 0;

		for (int i = 0; i < w.getNumberOfTransactions(this.principal); i++) {
			Transaction t = w.getTransaction(principal, i);
			GameTime time = w.getTransactionTimeStamp(principal, i);

			if (t instanceof DeliverCargoReceipt
					&& cal.getYear(time.getTicks()) >= this.startyear) {
				DeliverCargoReceipt dcr = (DeliverCargoReceipt) t;
				int cargoType = dcr.getCb().getCargoType();
				CargoType ct = (CargoType) w.get(SKEY.CARGO_TYPES, cargoType);

				if (ct.getCategory().equals(cargoCategory)) {
					amount += dcr.deltaCash().getAmount();
				}
			}
		}

		return new Money(amount);
	}

	Money calTrainRevenue(int trainId) {
		long amount = 0;

		for (int i = 0; i < w.getNumberOfTransactions(this.principal); i++) {
			Transaction t = w.getTransaction(principal, i);
			GameTime time = w.getTransactionTimeStamp(principal, i);

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

	private Money calTotal(Transaction.Category transactionCategory) {
		long amount = 0;

		for (int i = 0; i < w.getNumberOfTransactions(this.principal); i++) {
			Transaction t = w.getTransaction(principal, i);
			GameTime time = w.getTransactionTimeStamp(principal, i);

			if (t.getCategory() == transactionCategory
					&& cal.getYear(time.getTicks()) >= this.startyear) {
				amount += t.deltaCash().getAmount();
			}
		}

		return new Money(amount);
	}
}
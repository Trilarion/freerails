/*
 * Copyright (C) Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/**
 * @author rtuck99@users.berlios.de
 */
package org.railz.world.accounts;

import java.util.GregorianCalendar;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.common.GameCalendar;
import org.railz.world.common.GameTime;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.station.StationModel;
import org.railz.world.station.StationModelViewer;
import org.railz.world.terrain.TerrainTileViewer;
import org.railz.world.top.ITEM;
import org.railz.world.top.KEY;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.FreerailsTile;
import org.railz.world.track.TrackPieceViewer;
import org.railz.world.track.TrackRule;
import org.railz.world.train.TrainModel;
import org.railz.world.train.TrainModelViewer;

/**
 * Models a balance sheet. A balance sheet is created at the end of every year,
 * and stored as part of the Game World
 */
public class BalanceSheet implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -596545345821291605L;

	/**
	 * Year for which the balance sheet was created
	 */
	public final int year;

	/* Current assets */
	public final long cash;
	public final long totalCurrentAssets;
	/* non-current assets */
	public final long rollingStock;
	public final long property;
	public final long totalAssets;
	/* Current liabilities */
	public final long interest;
	public final long trackMaintenance;
	public final long trainMaintenance;
	public final long tax;
	public final long overdraft;
	public final long totalCurrentLiabilities;
	/* non-current liabilities */
	public final long loans;
	public final long bonds;
	public final long totalLiabilities;
	public final long assetsLessLiabilities;
	/* equities */
	public final long investedCapital;
	public final long retainedEarnings;
	public final long stock;
	public final long totalEquity;

	private final boolean proForma;

	private BalanceSheet(int year, boolean proForma, long cash,
			long rollingStock, long property, long overdraft, long interest,
			long trackMaintenance, long trainMaintenance, long tax, long loans,
			long bonds, long stock, long investedCapital) {
		this.year = year;
		this.proForma = proForma;
		this.cash = cash;
		totalCurrentAssets = cash;
		this.rollingStock = rollingStock;
		this.property = property;
		this.totalAssets = totalCurrentAssets + rollingStock + property;
		this.overdraft = overdraft;
		this.interest = interest;
		this.trackMaintenance = trackMaintenance;
		this.trainMaintenance = trainMaintenance;
		this.tax = tax;
		this.totalCurrentLiabilities = overdraft + interest + trackMaintenance
				+ trainMaintenance + tax;
		this.loans = loans;
		this.bonds = bonds;
		totalLiabilities = totalCurrentLiabilities + loans + bonds;
		assetsLessLiabilities = totalAssets - totalLiabilities;
		this.stock = stock;
		this.investedCapital = investedCapital;
		retainedEarnings = totalAssets - totalLiabilities - stock
				- investedCapital;
		totalEquity = investedCapital + stock + retainedEarnings;
	}

	/**
	 * @param p
	 *            The player for which the balance sheet should be generated
	 *            TODO generate pro-forma balance sheet based on cash-flow
	 *            projections rather than current status.
	 */
	public static BalanceSheet generateBalanceSheet(ReadOnlyWorld w,
			FreerailsPrincipal p, boolean proForma) {
		int year;
		GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
		GameCalendar calendar = (GameCalendar) w.get(ITEM.CALENDAR,
				Player.AUTHORITATIVE);
		year = calendar.getCalendar(now).get(GregorianCalendar.YEAR);

		BankAccount account = (BankAccount) w.get(KEY.BANK_ACCOUNTS, 0, p);
		long cash = account.getCurrentBalance();
		long overdraft;
		if (cash < 0) {
			overdraft = -cash;
			cash = 0;
		} else {
			overdraft = 0;
		}
		long trainMaintenance = 0;
		long rollingStock = 0;
		TrainModelViewer tmViewer = new TrainModelViewer(w);
		NonNullElements i = new NonNullElements(KEY.TRAINS, w, p);
		while (i.next()) {
			tmViewer.setTrainModel((TrainModel) i.getElement());
			rollingStock += tmViewer.getBookValue();
			trainMaintenance += tmViewer.getMaintenance();
		}
		long property = 0;
		int width = w.getMapWidth();
		int height = w.getMapHeight();
		TerrainTileViewer ttv = new TerrainTileViewer(w);
		TrackPieceViewer tpv = new TrackPieceViewer(w);
		int trackRules[] = new int[w
				.size(KEY.TRACK_RULES, Player.AUTHORITATIVE)];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				FreerailsTile tile = w.getTile(x, y);
				if (tile.getOwner().equals(p)) {
					ttv.setFreerailsTile(x, y);
					tpv.setFreerailsTile(x, y);
					property += ttv.getBookValue();
					property += tpv.getBookValue();
					trackRules[tile.getTrackRule()]++;
				}
			}
		}
		StationModelViewer smv = new StationModelViewer(w);
		i = new NonNullElements(KEY.STATIONS, w, p);
		while (i.next()) {
			smv.setStationModel((StationModel) i.getElement());
			property += smv.getBookValue();
		}
		long trackMaintenance = 0;
		for (int j = 0; j < trackRules.length; j++) {
			TrackRule tr = (TrackRule) w.get(KEY.TRACK_RULES, j,
					Player.AUTHORITATIVE);
			trackMaintenance += trackRules[j] * tr.getMaintenanceCost();
		}
		BankAccountViewer bav = new BankAccountViewer(w);
		bav.setBankAccount(account);
		long tax = bav.getIncomeTaxLiability();
		long interest = account.getCurrentBalance();
		if (interest < 0) {
			interest = (long) (interest * -bav.getOverdraftInterestRate());
		} else {
			interest = 0;
		}
		long loans = 0;
		long bonds = 0;
		long stock = 0;
		long investedCapital = bav.getOutsideInvestment();

		return new BalanceSheet(year - 1, proForma, cash, rollingStock,
				property, overdraft, interest, trackMaintenance,
				trainMaintenance, tax, loans, bonds, stock, investedCapital);
	}

	/**
	 * @return whether this balance sheet is a forecast.
	 */
	public boolean isProForma() {
		return proForma;
	}
}

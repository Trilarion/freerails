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
package org.railz.client.model;

import java.util.GregorianCalendar;

import org.railz.world.accounts.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
public class BalanceSheetModel {
    private final BalanceSheet balanceSheet;

    public BalanceSheet getBalanceSheet() {
	return balanceSheet;
    }

    private ModelRoot modelRoot;

    public BalanceSheetModel(ModelRoot mr, int year) {
	modelRoot = mr;
	ReadOnlyWorld w = mr.getWorld();
	GameCalendar gc = (GameCalendar) w.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
	GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	int thisYear = gc.getCalendar(now).get(GregorianCalendar.YEAR);
	if (year == thisYear) {
	    /* create a pro-forma balance sheet */
	    balanceSheet = BalanceSheet.generateBalanceSheet(w,
		    modelRoot.getPlayerPrincipal(), true);
	} else {
	    /* retreive historical balance sheet */
	    balanceSheet = (BalanceSheet) w.get(KEY.BALANCE_SHEETS, year -
		    gc.getStartYear(), modelRoot.getPlayerPrincipal()); 
	}
    }
}

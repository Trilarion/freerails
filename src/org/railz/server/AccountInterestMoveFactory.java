/*
 * Copyright (C) 2004 Robert Tuck
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
package org.railz.server;

import org.railz.controller.MoveReceiver;
import org.railz.move.AddTransactionMove;
import org.railz.world.accounts.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

/**
 * Responsible for adjusting accounts for interest due to being in credit or
 * having an overdraft
 *
 * @author rtuck99@users.berlios.de
 */
class AccountInterestMoveFactory {
    private ReadOnlyWorld world;
    private MoveReceiver moveReceiver;

    AccountInterestMoveFactory(ReadOnlyWorld w, MoveReceiver mr) {
	world = w;
	moveReceiver = mr;
    }

    /**
     * Interest is calculated monthly
     */
    void generateMoves() {
	BankAccountViewer bav = new BankAccountViewer(world);
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = ((Player) i.getElement()).getPrincipal();
	    BankAccount ba = (BankAccount) world.get(KEY.BANK_ACCOUNTS, 0, p);
	    bav.setBankAccount(ba);
	    long balance = ba.getCurrentBalance();
	    float interestRate;
	    int subcat;
	    if (balance < 0) {
		interestRate = bav.getOverdraftInterestRate();
		subcat = InterestTransaction.SUBCATEGORY_OVERDRAFT;
	    } else {
		interestRate = bav.getCreditAccountInterestRate();
		subcat =
		    InterestTransaction.SUBCATEGORY_ACCOUNT_CREDIT_INTEREST;
	    }
	    long interestAdded = (long) (balance * interestRate / 100.0);
	    GameTime now = (GameTime) world.get(ITEM.TIME,
		    Player.AUTHORITATIVE);
	    InterestTransaction t = new InterestTransaction(now, interestAdded,
		    subcat);
	    AddTransactionMove m = new AddTransactionMove(0, t, p);
	    moveReceiver.processMove(m);
	}
    }
}

/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 11-Aug-2003
 *
 */
package jfreerails.server;

import jfreerails.controller.MoveReceiver;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Bill;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.GameTime;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.World;
import jfreerails.world.track.TrackRule;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.NonNullElements;

/**
 * This class iterates over the entries in the BankAccount
 * and counts the number of units of each track type, then
 * calculates the cost of maintenance.
 *
 * @author Luke Lindsay
 *
 */
public class TrackMaintenanceMoveGenerator {
    private final MoveReceiver moveReceiver;

    public TrackMaintenanceMoveGenerator(MoveReceiver mr) {
        this.moveReceiver = mr;
    }

    static AddTransactionMove[] generateMove(World w) {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, w,
		Player.AUTHORITATIVE);
	AddTransactionMove[] moves = new AddTransactionMove[i.size()];
	int j = 0;
	while (i.next()) {
	    moves[j++] = generateMoveImpl(w, ((Player)
	    i.getElement()).getPrincipal());
	}
	return moves;
    }
    
    private static AddTransactionMove generateMoveImpl(World w,
	    FreerailsPrincipal p) {
        int[] track = calulateNumberOfEachTrackType(w, p);
        long amount = 0;

        for (int i = 0; i < track.length; i++) {
            TrackRule trackRule = (TrackRule)w.get(KEY.TRACK_RULES, i);
            long maintenanceCost = trackRule.getMaintenanceCost();

            if (track[i] > 0) {
                amount += maintenanceCost * track[i];
            }
        }
	amount /= 12;

	GameTime now = (GameTime) w.get(ITEM.TIME, p);
        Transaction t = new Bill(now, amount, Bill.TRACK_MAINTENANCE);

        return new AddTransactionMove(0, t, p);
    }

    /**
     * determine the quantity of track this player owns by examining the
     * players account and adding up how many he has purchased.
     */
    static int[] calulateNumberOfEachTrackType(World w,
	    FreerailsPrincipal p) {
        int[] unitsOfTrack = new int[w.size(KEY.TRACK_RULES)];
        BankAccount account = (BankAccount)w.get(KEY.BANK_ACCOUNTS, 0, p);

        for (int i = 0; i < account.size(); i++) {
            Transaction t = account.getTransaction(i);

            if (t instanceof AddItemTransaction) {
                AddItemTransaction addItemTransaction = (AddItemTransaction)t;

                if (AddItemTransaction.TRACK ==
		       	addItemTransaction.getSubcategory()) {
		    unitsOfTrack[addItemTransaction.getType()] +=
			addItemTransaction.getQuantity();
                }
            }
        }
        return unitsOfTrack;
    }

    public void update(World w) {
        Move[] m = generateMove(w);
	for (int i = 0; i < m.length; i++) {
	    moveReceiver.processMove(m[i]);
	}
    }
}

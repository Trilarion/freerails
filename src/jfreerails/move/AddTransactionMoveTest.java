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
 * Created on 07-Jul-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.InitialDeposit;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ITEM;

/**
 * @author Luke Lindsay
 *
 */
public class AddTransactionMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        BankAccount account = (BankAccount)getWorld().get(KEY.BANK_ACCOUNTS, 0,
		testPlayer.getPrincipal());
        assertTrue(0 == account.getCurrentBalance());

        Transaction t = new InitialDeposit((GameTime)
		getWorld().get(ITEM.TIME, Player.AUTHORITATIVE), 100);
        Move m = new AddTransactionMove(0, t, testPlayer.getPrincipal());
        assertTryMoveIsOk(m);
        assertTryUndoMoveFails(m);
        assertDoMoveIsOk(m);
        assertTrue(100 == account.getCurrentBalance());

        Move m2 = new AddTransactionMove(5, t, testPlayer.getPrincipal());
        assertTryMoveFails(m2);
        assertEqualsSurvivesSerialisation(m);

        assertOkAndRepeatable(m);
    }

    public void testConstrainedMove() {
        BankAccount account = (BankAccount)getWorld().get(KEY.BANK_ACCOUNTS, 0,
		testPlayer.getPrincipal());
        assertTrue(0 == account.getCurrentBalance());

	GameTime now = (GameTime) getWorld().get(ITEM.TIME,
		testPlayer.getPrincipal());
	Transaction t = new AddItemTransaction(now, AddItemTransaction.TRACK,
		0, 1, -100);
        Move m = new AddTransactionMove(0, t, true, testPlayer.getPrincipal());

        //This move should fail since there is no money in the account and 
        //it is constrained is set to true.
        assertTryMoveFails(m);
    }
}

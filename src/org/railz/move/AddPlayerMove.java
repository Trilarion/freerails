/*
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

package org.railz.move;

import org.railz.world.player.Player;
import org.railz.world.player.PlayerPrincipal;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.accounts.BankAccount;
import org.railz.world.accounts.InitialDeposit;
import org.railz.world.common.GameTime;
import org.railz.world.top.KEY;
import org.railz.world.top.ITEM;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.World;


/**
 * Adds a player to the world
 */
public class AddPlayerMove extends CompositeMove implements ServerMove {
    private static class OpenPlayerBankAccountMove extends AddItemToListMove
	implements ServerMove {
	    OpenPlayerBankAccountMove (KEY key, int i, BankAccount item,
		    FreerailsPrincipal principal) {
		super(key, i, item, principal);
	    }
	}

    private static class AddPlayerToListMove extends AddItemToListMove
        implements ServerMove {
        AddPlayerToListMove(KEY key, int i, Player p) {
            super(key, i, p, Player.NOBODY);
        }

        public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
            MoveStatus ms;

            if ((ms = super.tryDoMove(w, p)) != MoveStatus.MOVE_OK) {
                assert false;

                return ms;
            }

            /* verify that name is free */
            NonNullElements i = new NonNullElements(KEY.PLAYERS, w,
                    Player.NOBODY);

            while (i.next()) {
                Player pl = (Player)i.getElement();

                if (pl.getName().equals(((Player)item).getName())) {
                    return MoveStatus.moveFailed("Name already in use!");
                }
            }

            return MoveStatus.MOVE_OK;
        }
    }

    private static Move[] generateMove(ReadOnlyWorld w, Player player) {
        /**
         * create a new player with a corresponding Principal
         */
        Player newPlayer = new Player(player.getName(), player.getPublicKey(),
                w.size(KEY.PLAYERS, Player.AUTHORITATIVE));
        PlayerPrincipal tmpPlayer = new PlayerPrincipal(w.size(KEY.PLAYERS,
                    Player.AUTHORITATIVE));

	BankAccount ba = new BankAccount();
	GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);

        return new Move[] {
            new AddPlayerToListMove(KEY.PLAYERS,
                w.size(KEY.PLAYERS, Player.AUTHORITATIVE), newPlayer),
		new OpenPlayerBankAccountMove(KEY.BANK_ACCOUNTS,
		    0, ba, tmpPlayer)

        };
    }

    public AddPlayerMove(ReadOnlyWorld w, Player player) {
        super(generateMove(w, player));
    }
}

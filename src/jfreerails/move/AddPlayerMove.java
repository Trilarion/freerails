package jfreerails.move;

import jfreerails.world.player.Player;
import jfreerails.world.player.PlayerPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;


/**
 * Adds a player to the world
 */
public class AddPlayerMove extends CompositeMove implements ServerMove {
    private static class AddPlayerToListMove extends AddItemToListMove
        implements ServerMove {
        AddPlayerToListMove(KEY key, int i, Player p) {
            super(key, i, p);
        }

        public MoveStatus tryDoMove(World w) {
            MoveStatus ms;

            if ((ms = super.tryDoMove(w)) != MoveStatus.MOVE_OK) {
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

        return new Move[] {
            new AddPlayerToListMove(KEY.PLAYERS,
                w.size(KEY.PLAYERS, Player.AUTHORITATIVE), newPlayer)
        };
    }

    public AddPlayerMove(ReadOnlyWorld w, Player player) {
        super(generateMove(w, player));
    }
}
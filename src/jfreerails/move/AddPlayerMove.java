package jfreerails.move;

import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;


/**
 * Adds a player to the world.
 * @author Luke
 */
public class AddPlayerMove implements Move, ServerMove {
    private final Player player2add;

    private AddPlayerMove(Player p) {
        this.player2add = p;
    }

    public static AddPlayerMove generateMove(ReadOnlyWorld w, Player player) {
        /**
         * create a new player with a corresponding Principal
         */
        Player player2add = new Player(player.getName(), player.getPublicKey(),
                w.getNumberOfPlayers());

        return new AddPlayerMove(player2add);
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        // TODO Auto-generated method stub
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        // TODO Auto-generated method stub
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        w.addPlayer(this.player2add);

        //Sell the player 2 $500,000 bonds at 5% interest.        
        w.addTransaction(BondTransaction.issueBond(5), player2add.getPrincipal());
        w.addTransaction(BondTransaction.issueBond(5), player2add.getPrincipal());

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        // TODO Auto-generated method stub
        return MoveStatus.MOVE_OK;
    }
}
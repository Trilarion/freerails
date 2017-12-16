/*
 * Created on Sep 11, 2004
 *
 */
package experimental;

import freerails.controller.PreMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.network.UntriedMoveReceiver;
import freerails.world.player.Player;
import freerails.world.top.World;

/**
 * An UntriedMoveReceiver that executes moves on the world object passed to its
 * constructor.
 * 
 * @author Luke
 * 
 */
public final class SimpleMoveReciever implements UntriedMoveReceiver {
    private final World w;

    public SimpleMoveReciever(World w) {
        this.w = w;
        if (null == w)
            throw new NullPointerException();
    }

    public MoveStatus tryDoMove(Move move) {
        return move.tryDoMove(w, Player.AUTHORITATIVE);
    }

    public void processMove(Move move) {
        move.doMove(w, Player.AUTHORITATIVE);
    }

    public void processPreMove(PreMove pm) {
        processMove(pm.generateMove(w));
    }
}
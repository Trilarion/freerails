/*
 * Created on Sep 11, 2004
 *
 */
package experimental;

import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.network.UntriedMoveReceiver;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;


public final class SimpleMoveReciever implements UntriedMoveReceiver {
    private final World w;

    public SimpleMoveReciever(World w) {
        this.w = w;
    }

    public MoveStatus tryDoMove(Move move) {
        return move.tryDoMove(w, Player.AUTHORITATIVE);
    }

    public void undoLastMove() {
    }

    public void processMove(Move move) {
        move.doMove(w, Player.AUTHORITATIVE);
    }
}
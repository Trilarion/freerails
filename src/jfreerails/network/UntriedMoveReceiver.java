package jfreerails.network;

import jfreerails.controller.PreMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;


/** Lets the caller test moves.
 * @author rob
 *
 */
public interface UntriedMoveReceiver extends MoveReceiver {
    MoveStatus tryDoMove(Move move);

    void processPreMove(PreMove pm);
}
package jfreerails.network;

import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.PreMove;


/** Lets the caller test moves.
 * @author rob
 *
 */
public interface UntriedMoveReceiver extends MoveReceiver {
    MoveStatus tryDoMove(Move move);

    void processPreMove(PreMove pm);
}
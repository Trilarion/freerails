package jfreerails.network;

import jfreerails.move.Move;
import jfreerails.move.MoveStatus;


/** Lets the caller test moves.
 * @author rob
 *
 */
public interface UntriedMoveReceiver extends MoveReceiver {
    public MoveStatus tryDoMove(Move move);
}
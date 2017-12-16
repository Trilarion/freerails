package freerails.network;

import freerails.controller.PreMove;
import freerails.move.Move;
import freerails.move.MoveStatus;

/**
 * Lets the caller test moves.
 * 
 * @author rob
 * 
 */
public interface UntriedMoveReceiver extends MoveReceiver {
    MoveStatus tryDoMove(Move move);

    void processPreMove(PreMove pm);
}
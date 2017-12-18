package freerails.network;

import freerails.controller.PreMove;
import freerails.move.Move;
import freerails.move.MoveStatus;

/**
 * Lets the caller test moves.
 *
 * @author rob
 */
public interface UntriedMoveReceiver extends MoveReceiver {

    /**
     *
     * @param move
     * @return
     */
    MoveStatus tryDoMove(Move move);

    /**
     *
     * @param pm
     */
    void processPreMove(PreMove pm);
}
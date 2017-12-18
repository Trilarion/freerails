package freerails.network;

import freerails.move.Move;

/**
 * Accepts a Move without the caller knowing where its going. TODO replace with
 * MoveExecutor where the moves are expected to be executed.
 *
 * @author Luke
 */
public interface MoveReceiver {

    /**
     *
     * @param move
     */
    void processMove(Move move);
}
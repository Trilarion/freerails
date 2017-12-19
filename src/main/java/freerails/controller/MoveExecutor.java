package freerails.controller;

import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;

/**
 * Lets the caller try and execute Moves.
 *
 */
public interface MoveExecutor {

    /**
     *
     * @param m
     * @return
     */
    MoveStatus doMove(Move m);

    /**
     *
     * @param pm
     * @return
     */
    MoveStatus doPreMove(PreMove pm);

    /**
     *
     * @param m
     * @return
     */
    MoveStatus tryDoMove(Move m);

    /**
     *
     * @return
     */
    ReadOnlyWorld getWorld();

    /**
     *
     * @return
     */
    FreerailsPrincipal getPrincipal();
}
/*
 * Created on Apr 10, 2004
 */
package freerails.controller;

import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;

/**
 * Lets the caller try and execute Moves.
 *
 * @author Luke
 */
public interface MoveExecutor {
    MoveStatus doMove(Move m);

    MoveStatus doPreMove(PreMove pm);

    MoveStatus tryDoMove(Move m);

    ReadOnlyWorld getWorld();

    FreerailsPrincipal getPrincipal();
}
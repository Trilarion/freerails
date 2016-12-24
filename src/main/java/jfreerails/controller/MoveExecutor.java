/*
 * Created on Apr 10, 2004
 */
package jfreerails.controller;

import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * Lets the caller try and execute Moves.
 * 
 * @author Luke
 * 
 */
public interface MoveExecutor {
    MoveStatus doMove(Move m);

    MoveStatus doPreMove(PreMove pm);

    MoveStatus tryDoMove(Move m);

    ReadOnlyWorld getWorld();

    FreerailsPrincipal getPrincipal();
}
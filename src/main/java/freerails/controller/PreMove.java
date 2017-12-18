/*
 * Created on 13-Oct-2004
 *
 */
package freerails.controller;

import freerails.move.Move;
import freerails.world.common.FreerailsSerializable;
import freerails.world.top.ReadOnlyWorld;

/**
 * Defines a method that generates a move based on the state of the world
 * object. The state of a move is often a function of the state of the world
 * object and some other input.
 *
 * @author Luke
 */
public interface PreMove extends FreerailsSerializable {

    /**
     *
     * @param w
     * @return
     */
    Move generateMove(ReadOnlyWorld w);
}
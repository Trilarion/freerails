/*
 * Created on 13-Oct-2004
 *
 */
package jfreerails.controller;

import jfreerails.move.Move;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.top.ReadOnlyWorld;


/**
 * Defines a method that generates a move based on the state of the
 * world object.  The state of a move
 * is often a function of the state of the world object and some
 * other input.
 *
 * @author Luke
 *
 */
public interface PreMove extends FreerailsSerializable {
    Move generateMove(ReadOnlyWorld w);
}
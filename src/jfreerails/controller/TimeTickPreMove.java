/*
 * Created on 18-Oct-2004
 *
 */
package jfreerails.controller;

import jfreerails.move.Move;
import jfreerails.move.TimeTickMove;
import jfreerails.world.top.ReadOnlyWorld;


/**
 * @author Luke
 *
 */
public class TimeTickPreMove implements PreMove {
    public Move generateMove(ReadOnlyWorld w) {
        return TimeTickMove.getMove(w);
    }
}
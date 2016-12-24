/*
 * Created on 18-Oct-2004
 *
 */
package jfreerails.controller;

import java.io.ObjectStreamException;

import jfreerails.move.Move;
import jfreerails.move.TimeTickMove;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * Generates a TimeTickMove.
 * 
 * @author Luke
 * 
 */
@jfreerails.util.InstanceControlled
public class TimeTickPreMove implements PreMove {
    private static final long serialVersionUID = 3690479125647208760L;

    public static final TimeTickPreMove INSTANCE = new TimeTickPreMove();

    private TimeTickPreMove() {

    }

    public Move generateMove(ReadOnlyWorld w) {
        return TimeTickMove.getMove(w);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}
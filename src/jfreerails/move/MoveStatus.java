package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;


/**
 * Records the success or failure of an attempt to execute a move.
 * @author lindsal
 */
final public class MoveStatus implements FreerailsSerializable {
    public static final MoveStatus MOVE_OK = new MoveStatus(true,
            "Move accepted");
    public final boolean ok;
    public final String message;

    /**
     * Avoid creating a duplicate when deserializing.
     */
    private Object readResolve() {
        if (ok) {
            return MOVE_OK;
        }
		return this;
    }

    private MoveStatus(boolean ok, String mess) {
        this.ok = ok;
        this.message = mess;
    }

    public static MoveStatus moveFailed(String reason) {
        //Next 2 lines are just for debuging.
        //It lets us see where moves are failing.
        //Exception e = new Exception();
        //e.printStackTrace();
        return new MoveStatus(false, reason);
    }

    public boolean isOk() {
        return ok;
    }

    public String toString() {
        return message;
    }
}
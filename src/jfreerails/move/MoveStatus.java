package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;


/**
 * XXX DO NOT TEST == AGAINST MOVE_FAILED XXX
 *
 * @author lindsal
 */
final public class MoveStatus implements FreerailsSerializable {
    public static final MoveStatus MOVE_OK = new MoveStatus(true,
            "Move accepted");

    /**
     * Not public - only instances of Move should need to access this.
     */
    static final MoveStatus MOVE_FAILED = new MoveStatus(false, "Move rejected");
    public final boolean ok;
    public final String message;

    /**
     * Avoid creating a duplicate when deserializing.
     */
    private Object readResolve() {
        if (ok) {
            return MOVE_OK;
        } else {
            return this;
        }
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
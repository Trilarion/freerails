package jfreerails.controller;

import jfreerails.move.MoveStatus;
import jfreerails.world.common.FreerailsSerializable;


/**
 * Records the success or failure of an attempt to execute a move.
 * @author lindsal
 */
final public class PreMoveStatus implements FreerailsSerializable {
    public static final PreMoveStatus PRE_MOVE_OK = new PreMoveStatus(MoveStatus.MOVE_OK);
    public final MoveStatus ms;

    /**
     * Avoid creating a duplicate when deserializing.
     */
    private Object readResolve() {
        if (ms.ok) {
            return PRE_MOVE_OK;
        }
		return this;
    }

    private PreMoveStatus(MoveStatus ms) {
        this.ms = ms;
    }

    public static PreMoveStatus failed(String reason) {
        return new PreMoveStatus(MoveStatus.moveFailed(reason));
    }

    public static PreMoveStatus fromMoveStatus(MoveStatus ms) {
        if (ms.ok) {
            return PRE_MOVE_OK;
        }
		return new PreMoveStatus(ms);
    }
}
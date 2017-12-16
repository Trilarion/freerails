package freerails.controller;

import freerails.move.MoveStatus;
import freerails.world.common.FreerailsSerializable;

/**
 * Records the success or failure of an attempt to execute a move.
 *
 * @author lindsal
 */
final public class PreMoveStatus implements FreerailsSerializable {
    private static final long serialVersionUID = 3978145456646009140L;

    public static final PreMoveStatus PRE_MOVE_OK = new PreMoveStatus(
            MoveStatus.MOVE_OK);

    public final MoveStatus ms;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PreMoveStatus))
            return false;

        final PreMoveStatus preMoveStatus = (PreMoveStatus) o;

        if (!ms.equals(preMoveStatus.ms))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ms.hashCode();
    }

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
package freerails.move;

import freerails.util.Immutable;
import freerails.world.common.FreerailsSerializable;

/**
 * Records the success or failure of an attempt to execute a move.
 *
 * @author lindsal
 */
@Immutable
final public class MoveStatus implements FreerailsSerializable {

    /**
     *
     */
    public static final MoveStatus MOVE_OK = new MoveStatus(true,
            "Move accepted");
    private static final long serialVersionUID = 3258129171879309624L;

    /**
     *
     */
    public final boolean ok;

    /**
     *
     */
    public final String message;

    private final Throwable t;

    private MoveStatus(boolean ok, String mess) {
        if (ok) {
            t = null;
        } else {
            t = new Throwable();
            t.fillInStackTrace();
        }
        this.ok = ok;
        this.message = mess;
    }

    /**
     *
     * @param reason
     * @return
     */
    public static MoveStatus moveFailed(String reason) {
        return new MoveStatus(false, reason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MoveStatus))
            return false;

        final MoveStatus moveStatus = (MoveStatus) o;

        if (ok != moveStatus.ok)
            return false;
        return message != null ? message.equals(moveStatus.message) : moveStatus.message == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (ok ? 1 : 0);
        result = 29 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    /**
     * Avoid creating a duplicate when deserializing.
     */
    private Object readResolve() {
        if (ok) {
            return MOVE_OK;
        }
        return this;
    }

    /**
     *
     * @return
     */
    public boolean isOk() {
        return ok;
    }

    /**
     *
     */
    public void printStackTrack() {
        if (null != t)
            t.printStackTrace();
    }

    @Override
    public String toString() {
        return message;
    }
}
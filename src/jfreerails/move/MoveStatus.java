/**
 *
 *
 *
 *
 *
 */

package jfreerails.move;

/**
 *
 *
 *
 * @author lindsal
 */

final public class MoveStatus {

	public static final MoveStatus MOVE_ACCEPTED = new MoveStatus(true, "Move accepted");

	public static final MoveStatus MOVE_REJECTED = new MoveStatus(false, "Move rejected");

	public static final MoveStatus MOVE_RECEIVED = new MoveStatus(false, "Move received");

	public final boolean ok;

	public final String message;

	public MoveStatus(boolean ok, String mess) {
		this.ok = ok;
		this.message = mess;
	}

	public boolean isOk() {
		return ok;
	}
	public String getMessage() {
		return message;
	}

}
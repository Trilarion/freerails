package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;

/**
 * Records the success or failure of an attempt to execute a move.
 * 
 * @author lindsal
 */
final public class MoveStatus implements FreerailsSerializable {
	private static final long serialVersionUID = 3258129171879309624L;

	public static final MoveStatus MOVE_OK = new MoveStatus(true,
			"Move accepted");

	public final boolean ok;

	public final String message;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MoveStatus))
			return false;

		final MoveStatus moveStatus = (MoveStatus) o;

		if (ok != moveStatus.ok)
			return false;
		if (message != null ? !message.equals(moveStatus.message)
				: moveStatus.message != null)
			return false;

		return true;
	}

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

	private MoveStatus(boolean ok, String mess) {
		this.ok = ok;
		this.message = mess;
	}

	public static MoveStatus moveFailed(String reason) {
		// Next 2 lines are just for debuging.
		// It lets us see where moves are failing.
		// Exception e = new Exception();
		// e.printStackTrace();
		return new MoveStatus(false, reason);
	}

	public boolean isOk() {
		return ok;
	}

	public String toString() {
		return message;
	}
}
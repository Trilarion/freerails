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

	public static final MoveStatus MOVE_OK = new MoveStatus(true, "Move accepted");

	public static final MoveStatus MOVE_FAILED = new MoveStatus(false, "Move rejected");

	public static final MoveStatus MOVE_RECEIVED = new MoveStatus(false, "Move received");

	public final boolean ok;

	public final String message;

	private MoveStatus(boolean ok, String mess) {
		this.ok = ok;
		this.message = mess;
	}
	
	public static MoveStatus moveFailed(String reason){
		
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
/**
 * Java class generated from Poseidon UML diagram. 
 * Poseidon is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 * Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 *
 * 
 */

package jfreerails.move;

/**
 * Represents ...
 * 
 * @see OtherClasses
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
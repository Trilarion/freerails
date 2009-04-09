package jfreerails.move;

import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;

/**
 * 
 * Changes the time item on the world object.
 * 
 * @author rob
 */
public class TimeTickMove implements Move {
	private static final long serialVersionUID = 3257290240212153393L;

	private final GameTime oldTime;

	private final GameTime newTime;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TimeTickMove))
			return false;

		final TimeTickMove timeTickMove = (TimeTickMove) o;

		if (!newTime.equals(timeTickMove.newTime))
			return false;
		if (!oldTime.equals(timeTickMove.oldTime))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = oldTime.hashCode();
		result = 29 * result + newTime.hashCode();
		return result;
	}

	public static TimeTickMove getMove(ReadOnlyWorld w) {
		GameTime oldTime = w.currentTime();
		GameTime newTime = new GameTime(oldTime.getTicks() + 1);

		return new TimeTickMove(oldTime, newTime);
	}

	public TimeTickMove(GameTime oldTime, GameTime newTime) {
		this.oldTime = oldTime;
		this.newTime = newTime;
	}

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		if (w.currentTime().equals(oldTime)) {
			return MoveStatus.MOVE_OK;
		}
		String string = "oldTime = " + oldTime.getTicks() + " <=> "
				+ "currentTime " + (w.currentTime()).getTicks();

		return MoveStatus.moveFailed(string);
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		GameTime time = w.currentTime();

		if (time.equals(newTime)) {
			return MoveStatus.MOVE_OK;
		}
        return MoveStatus.moveFailed("Expected " + newTime + ", found " + time);
	}

	public MoveStatus doMove(World w, FreerailsPrincipal p) {
		MoveStatus status = tryDoMove(w, p);

		if (status.ok) {
			w.setTime(newTime);
		}

		return status;
	}

	public MoveStatus undoMove(World w, FreerailsPrincipal p) {
		MoveStatus status = tryUndoMove(w, p);

		if (status.isOk()) {
			w.setTime(oldTime);
		}

		return status;
	}

	@Override
	public String toString() {
		return "TimeTickMove: " + oldTime + "=>" + newTime;
	}
}
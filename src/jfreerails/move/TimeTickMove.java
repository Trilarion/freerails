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

	private final GameTime m_oldTime;

	private final GameTime m_newTime;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TimeTickMove))
			return false;

		final TimeTickMove timeTickMove = (TimeTickMove) o;

		if (!m_newTime.equals(timeTickMove.m_newTime))
			return false;
		if (!m_oldTime.equals(timeTickMove.m_oldTime))
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = m_oldTime.hashCode();
		result = 29 * result + m_newTime.hashCode();
		return result;
	}

	public static TimeTickMove getMove(ReadOnlyWorld w) {
		GameTime oldTime = w.currentTime();
		GameTime newTime = new GameTime(oldTime.getTicks() + 1);

		return new TimeTickMove(oldTime, newTime);
	}

	public TimeTickMove(GameTime oldTime, GameTime newTime) {
		m_oldTime = oldTime;
		m_newTime = newTime;
	}

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		if (w.currentTime().equals(m_oldTime)) {
			return MoveStatus.MOVE_OK;
		}
		String string = "oldTime = " + m_oldTime.getTicks() + " <=> "
				+ "currentTime " + (w.currentTime()).getTicks();

		return MoveStatus.moveFailed(string);
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		GameTime time = w.currentTime();

		if (time.equals(m_newTime)) {
			return MoveStatus.MOVE_OK;
		}
		return MoveStatus.moveFailed("Expected " + m_newTime + ", found "
				+ time);
	}

	public MoveStatus doMove(World w, FreerailsPrincipal p) {
		MoveStatus status = tryDoMove(w, p);

		if (status.ok) {
			w.setTime(m_newTime);
		}

		return status;
	}

	public MoveStatus undoMove(World w, FreerailsPrincipal p) {
		MoveStatus status = tryUndoMove(w, p);

		if (status.isOk()) {
			w.setTime(m_oldTime);
		}

		return status;
	}

	public String toString() {
		return "TimeTickMove: " + m_oldTime + "=>" + m_newTime;
	}
}
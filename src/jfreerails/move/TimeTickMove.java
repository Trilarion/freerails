package jfreerails.move;

import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;


/**
 *
 *  Changes the time item on the world object.
 * @author rob
 */
public class TimeTickMove implements Move {
    private final GameTime m_oldTime;
    private final GameTime m_newTime;

    public static TimeTickMove getMove(ReadOnlyWorld w) {
        GameTime oldTime = (GameTime)w.get(ITEM.TIME);
        GameTime newTime = new GameTime(oldTime.getTime() + 1);

        return new TimeTickMove(oldTime, newTime);
    }

    public TimeTickMove(GameTime oldTime, GameTime newTime) {
        m_oldTime = oldTime;
        m_newTime = newTime;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.get(ITEM.TIME).equals(m_oldTime)) {
            return MoveStatus.MOVE_OK;
        } else {
            String string = "oldTime = " + m_oldTime.getTime() + " <=> " +
                "currentTime " + ((GameTime)w.get(ITEM.TIME)).getTime();

            return MoveStatus.moveFailed(string);
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        GameTime time = ((GameTime)w.get(ITEM.TIME));

        if (time.equals(m_newTime)) {
            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.moveFailed("Expected " + m_newTime + ", found " +
                time);
        }
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryDoMove(w, p);

        if (status.ok) {
            w.set(ITEM.TIME, m_newTime);
        }

        return status;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryUndoMove(w, p);

        if (status.isOk()) {
            w.set(ITEM.TIME, m_oldTime);
        }

        return status;
    }

    public String toString() {
        return "TimeTickMove: " + m_oldTime + "=>" + m_newTime;
    }
}
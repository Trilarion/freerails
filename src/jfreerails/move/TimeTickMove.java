package jfreerails.move;

import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;


/**
 *
 *  Changes the time item on the world object.
 *
 */
public class TimeTickMove implements Move {
    private GameTime oldTime = null;
    private GameTime newTime = null;

    public static TimeTickMove getMove(ReadOnlyWorld w) {
        TimeTickMove timeTickMove = new TimeTickMove();
        timeTickMove.oldTime = (GameTime)w.get(ITEM.TIME);
        timeTickMove.newTime = new GameTime(timeTickMove.oldTime.getTime() + 1);

        return timeTickMove;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (((GameTime)w.get(ITEM.TIME)).equals(oldTime)) {
            return MoveStatus.MOVE_OK;
        } else {
            String string = "oldTime = " + oldTime.getTime() + " <=> " +
                "currentTime " + ((GameTime)w.get(ITEM.TIME)).getTime();
            System.err.println(string);

            return MoveStatus.moveFailed(string);
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        GameTime time = ((GameTime)w.get(ITEM.TIME));

        if (time.equals(newTime)) {
            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.moveFailed("Expected " + newTime + ", found " +
                time);
        }
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryDoMove(w, p);

        if (status.ok) {
            w.set(ITEM.TIME, newTime);
        }

        return status;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryUndoMove(w, p);

        if (status.isOk()) {
            w.set(ITEM.TIME, oldTime);
        }

        return status;
    }

    public String toString() {
        return "TimeTickMove: " + oldTime + "=>" + newTime;
    }
}
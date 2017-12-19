package freerails.move;

import freerails.world.common.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.World;

/**
 * Changes the time item on the world object.
 *
 */
public class TimeTickMove implements Move {
    private static final long serialVersionUID = 3257290240212153393L;

    private final GameTime oldTime;

    private final GameTime newTime;

    /**
     *
     * @param oldTime
     * @param newTime
     */
    public TimeTickMove(GameTime oldTime, GameTime newTime) {
        this.oldTime = oldTime;
        this.newTime = newTime;
    }

    /**
     *
     * @param w
     * @return
     */
    public static TimeTickMove getMove(ReadOnlyWorld w) {
        GameTime oldTime = w.currentTime();
        GameTime newTime = new GameTime(oldTime.getTicks() + 1);

        return new TimeTickMove(oldTime, newTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TimeTickMove))
            return false;

        final TimeTickMove timeTickMove = (TimeTickMove) o;

        if (!newTime.equals(timeTickMove.newTime))
            return false;
        return oldTime.equals(timeTickMove.oldTime);
    }

    @Override
    public int hashCode() {
        int result;
        result = oldTime.hashCode();
        result = 29 * result + newTime.hashCode();
        return result;
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
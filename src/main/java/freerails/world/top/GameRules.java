/*
 * Created on Mar 2, 2004
 */
package freerails.world.top;

import freerails.world.common.FreerailsSerializable;

/**
 * Stores rules governing what players are allowed to do, for example whether
 * they can connect their track to the track of other players.
 *
 * @author Luke
 */
public class GameRules implements FreerailsSerializable {

    /**
     *
     */
    public static final GameRules DEFAULT_RULES = new GameRules(true, false);

    /**
     *
     */
    public static final GameRules NO_RESTRICTIONS = new GameRules(false, true);
    private static final long serialVersionUID = 3258125847557978416L;
    private final boolean canConnect2OtherRRTrack;
    private final boolean mustConnect2ExistingTrack;

    private GameRules(boolean mustConnect, boolean canConnect2others) {
        canConnect2OtherRRTrack = canConnect2others;
        mustConnect2ExistingTrack = mustConnect;
    }

    @Override
    public int hashCode() {
        int result;
        result = (canConnect2OtherRRTrack ? 1 : 0);
        result = 29 * result + (mustConnect2ExistingTrack ? 1 : 0);

        return result;
    }

    /**
     *
     * @return
     */
    public synchronized boolean isCanConnect2OtherRRTrack() {
        return canConnect2OtherRRTrack;
    }

    /**
     *
     * @return
     */
    public synchronized boolean isMustConnect2ExistingTrack() {
        return mustConnect2ExistingTrack;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GameRules)) {
            return false;
        }

        GameRules test = (GameRules) obj;

        return this.canConnect2OtherRRTrack == test.canConnect2OtherRRTrack
                && this.mustConnect2ExistingTrack == test.mustConnect2ExistingTrack;
    }
}
/*
 * Created on Mar 2, 2004
 */
package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;


/**
 * Stores rules governing what players are allowed to do, for example whether
 * they can connect their track to the track of other players.
 *  @author Luke
 *
 */
public class GameRules implements FreerailsSerializable {
    private final boolean canConnect2OtherRRTrack;
    private final boolean mustConnect2ExistingTrack;
    public static final GameRules DEFAULT_RULES = new GameRules(true, false);
    public static final GameRules NO_RESTRICTIONS = new GameRules(false, true);

    private GameRules(boolean mustConnect, boolean canConnect2others) {
        canConnect2OtherRRTrack = canConnect2others;
        mustConnect2ExistingTrack = mustConnect;
    }

    public synchronized boolean isCanConnect2OtherRRTrack() {
        return canConnect2OtherRRTrack;
    }

    public synchronized boolean isMustConnect2ExistingTrack() {
        return mustConnect2ExistingTrack;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GameRules)) {
            return false;
        }

        GameRules test = (GameRules)obj;

        return this.canConnect2OtherRRTrack == test.canConnect2OtherRRTrack &&
        this.mustConnect2ExistingTrack == test.mustConnect2ExistingTrack;
    }
}
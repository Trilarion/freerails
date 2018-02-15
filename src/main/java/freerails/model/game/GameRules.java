/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.model.game;

import java.io.Serializable;

/**
 * Stores rules governing what players are allowed to do, for example whether
 * they can connect their track to the track of other players.
 */
public class GameRules implements Serializable {

    public static final Serializable DEFAULT_RULES = new GameRules(true, false);
    public static final Serializable NO_RESTRICTIONS = new GameRules(false, true);
    private static final long serialVersionUID = 3258125847557978416L;
    private final boolean canConnectToOtherRRTrack;
    private final boolean mustConnectToExistingTrack;

    private GameRules(boolean mustConnect, boolean canConnect2others) {
        canConnectToOtherRRTrack = canConnect2others;
        mustConnectToExistingTrack = mustConnect;
    }

    @Override
    public int hashCode() {
        int result;
        result = (canConnectToOtherRRTrack ? 1 : 0);
        result = 29 * result + (mustConnectToExistingTrack ? 1 : 0);

        return result;
    }

    /**
     * @return
     */
    public synchronized boolean isCanConnectToOtherRRTrack() {
        return canConnectToOtherRRTrack;
    }

    /**
     * @return
     */
    public synchronized boolean isMustConnectToExistingTrack() {
        return mustConnectToExistingTrack;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GameRules)) {
            return false;
        }

        GameRules test = (GameRules) obj;

        return canConnectToOtherRRTrack == test.canConnectToOtherRRTrack && mustConnectToExistingTrack == test.mustConnectToExistingTrack;
    }
}
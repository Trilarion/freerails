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

package freerails.controller;

import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.move.premove.AddStationPreMove;
import freerails.util.Vector2D;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.SKEY;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.track.TrackRule;
import org.apache.log4j.Logger;

import java.util.NoSuchElementException;

/**
 * Class to build a station at a given point, names station after nearest city.
 * If that name is taken then a "Junction" or "Siding" is added to the name.
 *
 *
 * Updated 12th April 2003 by Scott Bennett to include nearest city names.
 */
public class StationBuilder {

    private static final Logger logger = Logger.getLogger(StationBuilder.class.getName());
    private final MoveExecutor executor;
    private int ruleNumber;

    /**
     * @param executor
     */
    public StationBuilder(MoveExecutor executor) {
        this.executor = executor;

        TrackRule trackRule;

        int i = -1;

        ReadOnlyWorld world = executor.getWorld();

        do {
            i++;
            trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, i);
        } while (!trackRule.isStation());

        ruleNumber = i;
    }

    /**
     * @param p
     * @return
     */
    public MoveStatus tryBuildingStation(Vector2D p) {
        ReadOnlyWorld world = executor.getWorld();

        FreerailsPrincipal principal = executor.getPrincipal();
        AddStationPreMove preMove = AddStationPreMove.newStation(p, ruleNumber, principal);
        Move move = preMove.generateMove(world);

        return executor.tryDoMove(move);
    }

    /**
     * @param p
     * @return
     */
    public MoveStatus buildStation(Vector2D p) {
        // Only build a station if there is track at the specified point.
        MoveStatus status = tryBuildingStation(p);
        if (status.succeeds()) {
            FreerailsPrincipal principal = executor.getPrincipal();
            AddStationPreMove preMove = AddStationPreMove.newStation(p, ruleNumber, principal);
            return executor.doPreMove(preMove);
        }

        logger.debug(status.getMessage());

        return status;
    }

    /**
     * @param ruleNumber
     */
    public void setStationType(int ruleNumber) {
        this.ruleNumber = ruleNumber;
    }

    int getTrackTypeID(String string) {
        ReadOnlyWorld world = executor.getWorld();
        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            TrackRule r = (TrackRule) world.get(SKEY.TRACK_RULES, i);

            if (string.equals(r.getTypeName())) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }
}
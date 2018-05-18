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

package freerails.move;

import freerails.model.track.TrackType;
import freerails.move.generator.AddStationMoveGenerator;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.FreerailsPrincipal;
import org.apache.log4j.Logger;

import java.util.NoSuchElementException;

/**
 * Class to build a station at a given point, names station after nearest city.
 * If that name is taken then a "Junction" or "Siding" is added to the name.
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
        UnmodifiableWorld world = executor.getWorld();

        for (TrackType trackType: world.getTrackTypes()) {
            if (trackType.isStation()) {
                ruleNumber = trackType.getId();
                break;
            }
        }
    }

    /**
     * @param location
     * @return
     */
    public MoveStatus tryBuildingStation(Vec2D location) {
        UnmodifiableWorld world = executor.getWorld();

        FreerailsPrincipal principal = executor.getPrincipal();
        AddStationMoveGenerator preMove = AddStationMoveGenerator.newStation(location, ruleNumber, principal);
        Move move = preMove.generate(world);

        return executor.tryDoMove(move);
    }

    /**
     * @param location
     * @return
     */
    public MoveStatus buildStation(Vec2D location) {
        // Only build a station if there is track at the specified point.
        MoveStatus status = tryBuildingStation(location);
        if (status.succeeds()) {
            FreerailsPrincipal principal = executor.getPrincipal();
            AddStationMoveGenerator preMove = AddStationMoveGenerator.newStation(location, ruleNumber, principal);
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

    public int getTrackTypeID(String string) {
        UnmodifiableWorld world = executor.getWorld();
        for (TrackType trackType: world.getTrackTypes()) {
            if (string.equals(trackType.getName())) {
                return trackType.getId();
            }
        }
        throw new NoSuchElementException();
    }
}
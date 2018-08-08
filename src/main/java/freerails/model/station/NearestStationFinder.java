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

package freerails.model.station;

import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.terrain.TileTransition;

/**
 * Provides methods that find the nearest station in a given direction, used by
 * the select station popup window.
 */
public class NearestStationFinder {

    public static final int NOT_FOUND = Integer.MIN_VALUE;
    public static final int MAX_DISTANCE_TO_SELECT_SQUARED = 20 * 20;
    private final UnmodifiableWorld world;
    private final Player player;

    /**
     * @param world
     * @param player
     */
    public NearestStationFinder(UnmodifiableWorld world, Player player) {
        this.world = world;
        this.player = player;
    }

    /**
     * Returns true if the angle between direction and the vector (deltaX, deltaY) is less than 45 degrees.
     */
    private static boolean isInRightDirection(TileTransition direction, Vec2D delta) {
        boolean isDiagonal = direction.deltaX * direction.deltaY != 0;
        boolean sameXDirection = (direction.deltaX * delta.x) > 0;
        boolean sameYDirection = (direction.deltaY * delta.y > 0);
        boolean deltaXisLongerThanDeltaY = delta.x * delta.x < delta.y * delta.y;

        if (isDiagonal) {
            return sameXDirection && sameYDirection;
        }
        if (0 == direction.deltaX) {
            return deltaXisLongerThanDeltaY && sameYDirection;
        }
        return !deltaXisLongerThanDeltaY && sameXDirection;
    }

    // TODO should this maybe be static (so that the world reference can be removed)
    /**
     * @param p
     * @return
     */
    public int findNearestStation(Vec2D p) {
        // Find nearest station.
        int distanceToClosestSquared = Integer.MAX_VALUE;

        int nearestStation = NOT_FOUND;

        for (Station station: world.getStations(player)) {
            Vec2D delta = Vec2D.subtract(p, station.location);
            int distanceSquared = delta.x * delta.x + delta.y * delta.y;

            if (distanceSquared < distanceToClosestSquared && MAX_DISTANCE_TO_SELECT_SQUARED > distanceSquared) {
                distanceToClosestSquared = distanceSquared;
                nearestStation = station.getId();
            }
        }

        return nearestStation;
    }

    /**
     * @param startStation
     * @param direction
     * @return
     */
    public int findNearestStationInDirection(int startStation, TileTransition direction) {
        int distanceToClosestSquared = Integer.MAX_VALUE;
        Station currentStation = world.getStation(player, startStation);

        int nearestStation = NOT_FOUND;

        for (Station station: world.getStations(player)) {
            Vec2D delta = Vec2D.subtract(station.location, currentStation.location);
            int distanceSquared = delta.x * delta.x + delta.y * delta.y;
            boolean closer = distanceSquared < distanceToClosestSquared;
            boolean notTheSameStation = startStation != station.getId();
            boolean inRightDirection = isInRightDirection(direction, delta);

            if (closer && inRightDirection && notTheSameStation) {
                distanceToClosestSquared = distanceSquared;
                nearestStation = station.getId();
            }
        }

        return nearestStation;
    }
}
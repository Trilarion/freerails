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

package freerails.client.view;

import freerails.world.KEY;
import freerails.world.NonNullElementWorldIterator;
import freerails.world.ReadOnlyWorld;
import freerails.world.TileTransition;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;

/**
 * Provides methods that find the nearest station in a given direction, used by
 * the select station popup window.
 */
public class NearestStationFinder {

    /**
     *
     */
    public static final int NOT_FOUND = Integer.MIN_VALUE;

    private final ReadOnlyWorld world;

    private final FreerailsPrincipal principal;

    /**
     * @param w
     * @param player
     */
    public NearestStationFinder(ReadOnlyWorld w, FreerailsPrincipal player) {
        world = w;
        this.principal = player;
    }

    /**
     * @param x
     * @param y
     * @return
     */
    public int findNearestStation(int x, int y) {
        // Find nearest station.
        int distanceToClosestSquared = Integer.MAX_VALUE;

        NonNullElementWorldIterator it = new NonNullElementWorldIterator(KEY.STATIONS, world, principal);
        int nearestStation = NOT_FOUND;

        while (it.next()) {
            StationModel station = (StationModel) it.getElement();

            int deltaX = x - station.x;

            int deltaY = y - station.y;
            int distanceSquared = deltaX * deltaX + deltaY * deltaY;

            int MAX_DISTANCE_TO_SELECT_SQUARED = 20 * 20;
            if (distanceSquared < distanceToClosestSquared
                    && MAX_DISTANCE_TO_SELECT_SQUARED > distanceSquared) {
                distanceToClosestSquared = distanceSquared;
                nearestStation = it.getIndex();
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
        NonNullElementWorldIterator it = new NonNullElementWorldIterator(KEY.STATIONS, world, principal);

        StationModel currentStation = (StationModel) world.get(principal,
                KEY.STATIONS, startStation);

        int nearestStation = NOT_FOUND;

        while (it.next()) {
            StationModel station = (StationModel) it.getElement();
            int deltaX = station.x - currentStation.x;
            int deltaY = station.y - currentStation.y;
            int distanceSquared = deltaX * deltaX + deltaY * deltaY;
            boolean closer = distanceSquared < distanceToClosestSquared;
            boolean notTheSameStation = startStation != it.getIndex();
            boolean inRightDirection = isInRightDirection(direction, deltaX,
                    deltaY);

            if (closer && inRightDirection && notTheSameStation) {
                distanceToClosestSquared = distanceSquared;
                nearestStation = it.getIndex();
            }
        }

        return nearestStation;
    }

    /**
     * Returns true if the angle between direction and the vector (deltaX,
     * deltaY) is less than 45 degrees.
     */
    private boolean isInRightDirection(TileTransition direction, int deltaX, int deltaY) {
        boolean isDiagonal = direction.deltaX * direction.deltaY != 0;
        boolean sameXDirection = (direction.deltaX * deltaX) > 0;
        boolean sameYDirection = (direction.deltaY * deltaY > 0);
        boolean deltaXisLongerThanDeltaY = deltaX * deltaX < deltaY * deltaY;

        if (isDiagonal) {
            return sameXDirection && sameYDirection;
        }
        if (0 == direction.deltaX) {
            return deltaXisLongerThanDeltaY && sameYDirection;
        }
        return !deltaXisLongerThanDeltaY && sameXDirection;
    }
}
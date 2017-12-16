/*
 * Created on Feb 6, 2004
 */
package freerails.client.view;

import freerails.world.common.Step;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.NonNullElements;
import freerails.world.top.ReadOnlyWorld;

/**
 * Provides methods that find the nearest station in a given direction, used by
 * the select station popup window.
 * 
 * @author Luke
 * 
 */
public class NearestStationFinder {
    public static final int NOT_FOUND = Integer.MIN_VALUE;

    private final ReadOnlyWorld world;

    private final FreerailsPrincipal principal;

    private final int MAX_DISTANCE_TO_SELECT_SQUARED = 20 * 20;

    public NearestStationFinder(ReadOnlyWorld w, FreerailsPrincipal player) {
        world = w;
        this.principal = player;
    }

    public int findNearestStation(int x, int y) {
        // Find nearest station.
        int distanceToClosestSquared = Integer.MAX_VALUE;

        NonNullElements it = new NonNullElements(KEY.STATIONS, world, principal);
        int nearestStation = NOT_FOUND;

        while (it.next()) {
            StationModel station = (StationModel) it.getElement();

            int deltaX = x - station.x;

            int deltaY = y - station.y;
            int distanceSquared = deltaX * deltaX + deltaY * deltaY;

            if (distanceSquared < distanceToClosestSquared
                    && MAX_DISTANCE_TO_SELECT_SQUARED > distanceSquared) {
                distanceToClosestSquared = distanceSquared;
                nearestStation = it.getIndex();
            }
        }

        return nearestStation;
    }

    public int findNearestStationInDirection(int startStation, Step direction) {
        int distanceToClosestSquared = Integer.MAX_VALUE;
        NonNullElements it = new NonNullElements(KEY.STATIONS, world, principal);

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
    private boolean isInRightDirection(Step direction, int deltaX, int deltaY) {
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
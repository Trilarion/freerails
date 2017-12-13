/*
 * Copyright (C) 2004 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on Feb 6, 2004
 */
package org.railz.client.view;

import org.railz.client.model.ModelRoot;
import org.railz.world.common.*;
import org.railz.world.station.StationModel;
import org.railz.world.top.KEY;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ReadOnlyWorld;

/**
 * @author Luke
 *  
 */
public class NearestStationFinder {
    public static final int NOT_FOUND = Integer.MIN_VALUE;

    private final ReadOnlyWorld world;
    private final ModelRoot modelRoot;

    final int MAX_DISTANCE_TO_SELECT_SQUARED = 20 * 20;

    public NearestStationFinder(ModelRoot mr) {
	modelRoot = mr;
	world = mr.getWorld();
    }

    public int findNearestStation(int x, int y) {
	//Find nearest station.
	int distanceToClosestSquared = Integer.MAX_VALUE;

	NonNullElements it = new NonNullElements(KEY.STATIONS, world,
		modelRoot.getPlayerPrincipal());
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

    public int findNearestStationInDirection(
	    int startStation,
	    byte direction) {
	int distanceToClosestSquared = Integer.MAX_VALUE;
	NonNullElements it = new NonNullElements(KEY.STATIONS, world,
		modelRoot.getPlayerPrincipal());

	StationModel currentStation =
	    (StationModel) world.get(KEY.STATIONS, startStation,
				     modelRoot.getPlayerPrincipal());

	int nearestStation = NOT_FOUND;

	while (it.next()) {
	    StationModel station = (StationModel) it.getElement();
	    int deltaX = station.x - currentStation.x;
	    int deltaY = station.y - currentStation.y;
	    int distanceSquared = deltaX * deltaX + deltaY * deltaY;			
	    byte directionOfStation;						
	    boolean closer = distanceSquared < distanceToClosestSquared;
	    boolean notTheSameStation = startStation != it.getIndex();
	    boolean inRightDirection = isInRightDirection(direction, deltaX, deltaY);
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
    private boolean isInRightDirection(byte direction, int deltaX,
	    int deltaY) {
	int dx = CompassPoints.getUnitDeltaX(direction);
	int dy = CompassPoints.getUnitDeltaY(direction);
	boolean isDiagonal = dx * dy != 0;
	boolean sameXDirection = (dx * deltaX) > 0;
	boolean sameYDirection = (dy * deltaY > 0);
	boolean deltaXisLongerThanDeltaY = deltaX * deltaX < deltaY * deltaY;

	if(isDiagonal){		
	    return sameXDirection && sameYDirection;
	}else{

	    if(0 == CompassPoints.getUnitDeltaX(direction)){				
		return deltaXisLongerThanDeltaY && sameYDirection;
	    }else{
		return !deltaXisLongerThanDeltaY && sameXDirection;
	    }
	}			
    }
}

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

/*
 *
 */
package freerails.model.train;

import freerails.model.train.activity.Activity;
import freerails.util.BidirectionalIterator;
import freerails.model.train.motion.TrainMotion;
import freerails.model.train.motion.TrainPositionOnMap;
import freerails.model.train.schedule.TrainOrder;

import freerails.util.Vec2D;
import freerails.model.*;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackSection;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.world.UnmodifiableWorld;

import java.awt.*;
import java.util.HashSet;

/**
 * Provides convenience methods to access the properties of a train from the world object.
 */
public class TrainAccessor {

    private final UnmodifiableWorld world;
    private final Player player;
    private final int trainId;

    /**
     * @param world
     * @param player
     * @param trainId
     */
    public TrainAccessor(final UnmodifiableWorld world, final Player player, final int trainId) {
        this.world = world;
        this.player = player;
        this.trainId = trainId;
    }

    // TODO the same as Station.getStationIdAtLocation...
    /**
     * @return the trainId of the station the train is currently at, or -1 if no
     * current station.
     */
    public int getStationId(double time) {
        TrainMotion trainMotion = findCurrentMotion(time);
        PositionOnTrack positionOnTrack = trainMotion.getFinalPosition();
        Vec2D location = positionOnTrack.getLocation();

        // loop through the station list to check if train is at the same Point2D as a station
        for (Station station: world.getStations(player)) {
            if (location.equals(station.getLocation())) {
                return station.getId(); // train is at the station at location tempPoint
            }
        }
        return -1;
    }

    /**
     * @param time
     * @param view
     * @return
     */
    public TrainPositionOnMap findPosition(double time, Rectangle view) {
        BidirectionalIterator<Activity> bidirectionalIterator = world.getTrain(player, trainId).getActivities();

        // TODO why starting at the end and going backwards?
        // goto last
        bidirectionalIterator.gotoLast();
        // search backwards
        while (bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration() >= time && bidirectionalIterator.hasPrevious()) {
            bidirectionalIterator.previous();
        }
        boolean afterFinish = bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration() < time;
        while (afterFinish && bidirectionalIterator.hasNext()) {
            bidirectionalIterator.next();
            afterFinish = bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration() < time;
        }
        double dt = time - bidirectionalIterator.get().getStartTime();
        dt = Math.min(dt, bidirectionalIterator.get().getDuration());
        TrainMotion trainMotion = (TrainMotion) bidirectionalIterator.get();

        Vec2D start = trainMotion.getPath().getStart();
        int trainLength = trainMotion.getTrainLength();
        Rectangle trainBox = new Rectangle(start.x * ModelConstants.TILE_SIZE - trainLength * 2, start.y * ModelConstants.TILE_SIZE - trainLength * 2, trainLength * 4, trainLength * 4);
        if (!view.intersects(trainBox)) {
            return null; // TODO doesn't work
        }
        return trainMotion.getStateAtTime(dt);
    }

    /**
     * @param time
     * @return
     */
    public TrainMotion findCurrentMotion(double time) {
        BidirectionalIterator<Activity> bidirectionalIterator = world.getTrain(player, trainId).getActivities();
        boolean afterFinish = bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration() < time;
        if (afterFinish) {
            bidirectionalIterator.gotoLast();
        }
        return (TrainMotion) bidirectionalIterator.get();
    }

    /**
     * Returns true if all the following hold.
     * <ol>
     * <li>The train is waiting for a full load at some station X.</li>
     * <li>The current train order tells the train to goto station X.</li>
     * <li>The current train order tells the train to wait for a full load.</li>
     * <li>The current train order specifies a consist that matches the train's current consist.</li>
     * </ol>
     */
    public boolean keepWaiting() {
        double time = world.getClock().getCurrentTime().getTicks();
        int stationId = getStationId(time);
        if (stationId == -1) return false;
        TrainMotion trainMotion = findCurrentMotion(time);
        TrainState trainState = trainMotion.getTrainState();
        if (trainState != TrainState.WAITING_FOR_FULL_LOAD) return false;
        Train train = world.getTrain(player, trainId);
        UnmodifiableSchedule schedule = train.getSchedule();
        TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());
        if (order.getStationId() != stationId) return false;
        if (!order.isWaitUntilFull()) return false;
        return order.getConsist().equals(train.getConsist());
    }

    // TODO this could be part of train utils

    /**
     * @param time
     * @return
     */
    public HashSet<TrackSection> occupiedTrackSection(double time) {
        TrainMotion trainMotion = findCurrentMotion(time);
        PathOnTiles path = trainMotion.getPath();
        HashSet<TrackSection> sections = new HashSet<>();
        Vec2D start = path.getStart();
        int x = start.x;
        int y = start.y;
        for (int i = 0; i < path.steps(); i++) {
            TileTransition s = path.getStep(i);
            Vec2D tile = new Vec2D(x, y);
            x += s.deltaX;
            y += s.deltaY;
            sections.add(new TrackSection(s, tile));
        }
        return sections;
    }

    /**
     * @param time
     * @return
     */
    public boolean isMoving(double time) {
        TrainMotion trainMotion = findCurrentMotion(time);
        double speed = trainMotion.getSpeedAtEnd();
        return speed != 0;
    }

}

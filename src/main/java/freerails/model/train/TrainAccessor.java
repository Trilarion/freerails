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

import freerails.model.activity.ActivityIterator;
import freerails.model.train.schedule.TrainOrder;

import freerails.util.Vec2D;
import freerails.model.*;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackSection;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.world.UnmodifiableWorld;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Provides convenience methods to access the properties of a train from the world object.
 */
public class TrainAccessor {

    private final UnmodifiableWorld world;
    private final Player player;
    private final int id;

    /**
     * @param world
     * @param player
     * @param id
     */
    public TrainAccessor(final UnmodifiableWorld world, final Player player, final int id) {
        this.world = world;
        this.player = player;
        this.id = id;
    }

    /**
     * @param world
     * @param onTrain
     * @param consist
     * @return
     */
    public static List<Integer> spaceAvailable2(UnmodifiableWorld world, UnmodifiableCargoBatchBundle onTrain, List<Integer> consist) {
        // This array will store the amount of space available on the train for
        // each cargo type.
        final int NUM_CARGO_TYPES = world.getCargos().size();
        Integer[] spaceAvailable = new Integer[NUM_CARGO_TYPES];
        Arrays.fill(spaceAvailable, 0);

        // First calculate the train's total capacity.
        for (Integer aConsist : consist) {
            int cargoType = aConsist;
            spaceAvailable[cargoType] += ModelConstants.UNITS_OF_CARGO_PER_WAGON;
        }

        for (int cargoType = 0; cargoType < NUM_CARGO_TYPES; cargoType++) {
            spaceAvailable[cargoType] = spaceAvailable[cargoType] - onTrain.getAmountOfType(cargoType);
        }
        return new ArrayList<>(Arrays.asList(spaceAvailable));
    }

    /**
     * @param time
     * @return
     */
    public TrainState getStatus(double time) {
        TrainMotion trainMotion = findCurrentMotion(time);
        return trainMotion.getTrainState();
    }

    // TODO the same as Station.getStationIdAtLocation...
    /**
     * @return the id of the station the train is currently at, or -1 if no
     * current station.
     */
    public int getStationId(double time) {

        TrainMotion trainMotion = findCurrentMotion(time);
        PositionOnTrack positionOnTrack = trainMotion.getFinalPosition();
        Vec2D location = positionOnTrack.getLocation();

        // loop through the station list to check if train is at the same Point2D as a station
        for (Station station: world.getStations(player)) {
            if (location.equals(station.location)) {
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
        ActivityIterator activityIterator = world.getActivities(player, id);

        // goto last
        activityIterator.gotoLastActivity();
        // search backwards
        while (activityIterator.getFinishTime() >= time && activityIterator.hasPrevious()) {
            activityIterator.previousActivity();
        }
        boolean afterFinish = activityIterator.getFinishTime() < time;
        while (afterFinish && activityIterator.hasNext()) {
            activityIterator.nextActivity();
            afterFinish = activityIterator.getFinishTime() < time;
        }
        double dt = time - activityIterator.getStartTime();
        dt = Math.min(dt, activityIterator.getDuration());
        TrainMotion trainMotion = (TrainMotion) activityIterator.getActivity();

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
        ActivityIterator activityIterator = world.getActivities(player, id);
        boolean afterFinish = activityIterator.getFinishTime() < time;
        if (afterFinish) {
            activityIterator.gotoLastActivity();
        }
        return (TrainMotion) activityIterator.getActivity();
    }

    /**
     * @return
     */
    public Train getTrain() {
        return world.getTrain(player, id);
    }

    /**
     * @return
     */
    public UnmodifiableSchedule getSchedule() {
        Train train = getTrain();
        return train.getSchedule();
    }

    /**
     * @return
     */
    public UnmodifiableCargoBatchBundle getCargoBundle() {
        Train train = getTrain();
        return train.getCargoBatchBundle();
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
        double time = world.currentTime().getTicks();
        int stationId = getStationId(time);
        if (stationId == -1) return false;
        TrainState act = getStatus(time);
        if (act != TrainState.WAITING_FOR_FULL_LOAD) return false;
        UnmodifiableSchedule schedule = getSchedule();
        TrainOrder order = schedule.getOrder(schedule.getOrderToGoto());
        if (order.getStationId() != stationId) return false;
        if (!order.isWaitUntilFull()) return false;
        Train train = getTrain();
        return order.getConsist().equals(train.getConsist());
    }

    /**
     * @return the location of the station the train is currently heading
     * towards.
     */
    public Vec2D getTargetLocation() {
        Train train = world.getTrain(player, id);
        UnmodifiableSchedule schedule = train.getSchedule();
        int stationNumber = schedule.getStationToGoto();

        if (-1 == stationNumber) {
            // There are no stations on the schedule.
            return Vec2D.ZERO;
        }

        Station station = world.getStation(player, stationNumber);
        return station.location;
    }

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

    /**
     * The space available on the train measured in cargo units.
     */
    public List<Integer> spaceAvailable() {

        Train train = world.getTrain(player, id);
        return spaceAvailable2(world, train.getCargoBatchBundle(), train.getConsist());
    }

}

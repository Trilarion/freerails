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

import freerails.model.world.SharedKey;
import freerails.model.world.PlayerKey;
import freerails.util.ImmutableList;
import freerails.util.Vec2D;
import freerails.model.*;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.ImmutableCargoBatchBundle;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackSection;
import freerails.model.train.schedule.ImmutableSchedule;
import freerails.model.train.schedule.Schedule;
import freerails.model.world.ReadOnlyWorld;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Provides convenience methods to access the properties of a train from the world object.
 */
public class TrainAccessor {

    private final ReadOnlyWorld world;
    private final FreerailsPrincipal principal;
    private final int id;

    /**
     * @param world
     * @param principal
     * @param id
     */
    public TrainAccessor(final ReadOnlyWorld world, final FreerailsPrincipal principal, final int id) {
        this.world = world;
        this.principal = principal;
        this.id = id;
    }

    /**
     * @param world
     * @param onTrain
     * @param consist
     * @return
     */
    public static ImmutableList<Integer> spaceAvailable2(ReadOnlyWorld world, CargoBatchBundle onTrain, ImmutableList<Integer> consist) {
        // This array will store the amount of space available on the train for
        // each cargo type.
        final int NUM_CARGO_TYPES = world.size(SharedKey.CargoTypes);
        Integer[] spaceAvailable = new Integer[NUM_CARGO_TYPES];
        Arrays.fill(spaceAvailable, 0);

        // First calculate the train's total capacity.
        for (Integer aConsist : consist) {
            int cargoType = aConsist;
            spaceAvailable[cargoType] += WagonType.UNITS_OF_CARGO_PER_WAGON;
        }

        for (int cargoType = 0; cargoType < NUM_CARGO_TYPES; cargoType++) {
            spaceAvailable[cargoType] = spaceAvailable[cargoType] - onTrain.getAmountOfType(cargoType);
        }
        return new ImmutableList<>(spaceAvailable);
    }

    /**
     * @param time
     * @return
     */
    public TrainState getStatus(double time) {
        TrainMotion trainMotion = findCurrentMotion(time);
        return trainMotion.getTrainState();
    }

    /**
     * @return the id of the station the train is currently at, or -1 if no
     * current station.
     */
    public int getStationId(double time) {

        TrainMotion trainMotion = findCurrentMotion(time);
        PositionOnTrack positionOnTrack = trainMotion.getFinalPosition();
        Vec2D location = positionOnTrack.getLocation();

        // loop through the station list to check if train is at the same Point2D as a station
        for (int i = 0; i < world.size(principal, PlayerKey.Stations); i++) {
            Station station = (Station) world.get(principal, PlayerKey.Stations, i);

            if (null != station && location.equals(station.location)) {
                return i; // train is at the station at location tempPoint
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
        ActivityIterator activityIterator = world.getActivities(principal, id);

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
        Rectangle trainBox = new Rectangle(start.x * WorldConstants.TILE_SIZE - trainLength * 2, start.y * WorldConstants.TILE_SIZE - trainLength * 2, trainLength * 4, trainLength * 4);
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
        ActivityIterator activityIterator = world.getActivities(principal, id);
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
        return (Train) world.get(principal, PlayerKey.Trains, id);
    }

    /**
     * @return
     */
    public ImmutableSchedule getSchedule() {
        Train train = getTrain();
        return (ImmutableSchedule) world.get(principal, PlayerKey.TrainSchedules, train.getScheduleID());
    }

    /**
     * @return
     */
    public CargoBatchBundle getCargoBundle() {
        Train train = getTrain();
        return (ImmutableCargoBatchBundle) world.get(principal, PlayerKey.CargoBundles, train.getCargoBundleID());
    }

    /**
     * Returns true iff all the following hold.
     * <ol>
     * <li>The train is waiting for a full load at some station X.</li>
     * <li>The current train order tells the train to goto station X.</li>
     * <li>The current train order tells the train to wait for a full load.</li>
     * <li>The current train order specifies a consist that matches the train's
     * current consist.</li>
     * </ol>
     */
    public boolean keepWaiting() {
        double time = world.currentTime().getTicks();
        int stationId = getStationId(time);
        if (stationId == -1) return false;
        TrainState act = getStatus(time);
        if (act != TrainState.WAITING_FOR_FULL_LOAD) return false;
        ImmutableSchedule schedule = getSchedule();
        TrainOrders order = schedule.getOrder(schedule.getOrderToGoto());
        if (order.stationId != stationId) return false;
        if (!order.waitUntilFull) return false;
        Train train = getTrain();
        return order.getConsist().equals(train.getConsist());
    }

    /**
     * @return the location of the station the train is currently heading
     * towards.
     */
    public Vec2D getTargetLocation() {
        Train train = (Train) world.get(principal, PlayerKey.Trains, id);
        int scheduleID = train.getScheduleID();
        Schedule schedule = (ImmutableSchedule) world.get(principal, PlayerKey.TrainSchedules, scheduleID);
        int stationNumber = schedule.getStationToGoto();

        if (-1 == stationNumber) {
            // There are no stations on the schedule.
            return Vec2D.ZERO;
        }

        Station station = (Station) world.get(principal, PlayerKey.Stations, stationNumber);
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
    public ImmutableList<Integer> spaceAvailable() {

        Train train = (Train) world.get(principal, PlayerKey.Trains, id);
        CargoBatchBundle bundleOnTrain = (ImmutableCargoBatchBundle) world.get(principal, PlayerKey.CargoBundles, train.getCargoBundleID());
        return spaceAvailable2(world, bundleOnTrain, train.getConsist());
    }

}

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
 * Created on 04-Mar-2005
 *
 */
package freerails.controller;

import freerails.util.ImInts;
import freerails.util.ImPoint;
import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.common.ActivityIterator;
import freerails.world.common.PositionOnTrack;
import freerails.world.common.Step;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.track.TrackSection;
import freerails.world.train.*;
import freerails.world.train.SpeedTimeAndStatus.TrainActivity;

import java.awt.*;
import java.util.HashSet;

import static freerails.world.common.Step.TILE_DIAMETER;

/**
 * Provides convenience methods to access the properties of a train from the
 * world object.
 */
public class TrainAccessor {

    private final ReadOnlyWorld w;

    private final FreerailsPrincipal p;

    private final int id;

    /**
     * @param w
     * @param p
     * @param id
     */
    public TrainAccessor(final ReadOnlyWorld w, final FreerailsPrincipal p,
                         final int id) {
        this.w = w;
        this.p = p;
        this.id = id;
    }

    /**
     * @param row
     * @param onTrain
     * @param consist
     * @return
     */
    public static ImInts spaceAvailable2(ReadOnlyWorld row,
                                         ImmutableCargoBundle onTrain, ImInts consist) {
        // This array will store the amount of space available on the train for
        // each cargo type.
        final int NUM_CARGO_TYPES = row.size(SKEY.CARGO_TYPES);
        int[] spaceAvailable = new int[NUM_CARGO_TYPES];

        // First calculate the train's total capacity.
        for (int j = 0; j < consist.size(); j++) {
            int cargoType = consist.get(j);
            spaceAvailable[cargoType] += WagonType.UNITS_OF_CARGO_PER_WAGON;
        }

        for (int cargoType = 0; cargoType < NUM_CARGO_TYPES; cargoType++) {
            spaceAvailable[cargoType] = spaceAvailable[cargoType]
                    - onTrain.getAmount(cargoType);
        }
        return new ImInts(spaceAvailable);

    }

    /**
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * @param time
     * @return
     */
    public SpeedTimeAndStatus.TrainActivity getStatus(double time) {
        TrainMotion tm = findCurrentMotion(time);
        return tm.getActivity();
    }

    /**
     * @param time
     * @return the id of the station the train is currently at, or -1 if no
     * current station.
     */
    public int getStationId(double time) {

        TrainMotion tm = findCurrentMotion(time);
        PositionOnTrack pot = tm.getFinalPosition();
        int x = pot.getX();
        int y = pot.getY();

        // loop thru the station list to check if train is at the same Point
        // as
        // a station
        for (int i = 0; i < w.size(p, KEY.STATIONS); i++) {
            StationModel tempPoint = (StationModel) w.get(p, KEY.STATIONS, i);

            if (null != tempPoint && (x == tempPoint.x) && (y == tempPoint.y)) {
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
        ActivityIterator ai = w.getActivities(p, id);

        // goto last
        ai.gotoLastActivity();
        // search backwards
        while (ai.getFinishTime() >= time && ai.hasPrevious()) {
            ai.previousActivity();
        }
        boolean afterFinish = ai.getFinishTime() < time;
        while (afterFinish && ai.hasNext()) {
            ai.nextActivity();
            afterFinish = ai.getFinishTime() < time;
        }
        double dt = time - ai.getStartTime();
        dt = Math.min(dt, ai.getDuration());
        TrainMotion tm = (TrainMotion) ai.getActivity();

        ImPoint start = tm.getPath().getStart();
        int trainLength = tm.getTrainLength();
        Rectangle trainBox = new Rectangle(start.x * TILE_DIAMETER
                - trainLength * 2, start.y * TILE_DIAMETER - trainLength * 2,
                trainLength * 4, trainLength * 4);
        if (!view.intersects(trainBox)) {
            return null; // 666 doesn't work
        }
        return tm.getState(dt);
    }

    /**
     * @param time
     * @return
     */
    public TrainMotion findCurrentMotion(double time) {
        ActivityIterator ai = w.getActivities(p, id);
        boolean afterFinish = ai.getFinishTime() < time;
        if (afterFinish) {
            ai.gotoLastActivity();
        }
        return (TrainMotion) ai.getActivity();
    }

    /**
     * @return
     */
    public TrainModel getTrain() {
        return (TrainModel) w.get(p, KEY.TRAINS, id);
    }

    /**
     * @return
     */
    public ImmutableSchedule getSchedule() {
        TrainModel train = getTrain();
        return (ImmutableSchedule) w.get(p, KEY.TRAIN_SCHEDULES, train
                .getScheduleID());
    }

    /**
     * @return
     */
    public ImmutableCargoBundle getCargoBundle() {
        TrainModel train = getTrain();
        return (ImmutableCargoBundle) w.get(p, KEY.CARGO_BUNDLES, train
                .getCargoBundleID());
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
     *
     * @return
     */
    public boolean keepWaiting() {
        double time = w.currentTime().getTicks();
        int stationId = getStationId(time);
        if (stationId == -1)
            return false;
        SpeedTimeAndStatus.TrainActivity act = getStatus(time);
        if (act != TrainActivity.WAITING_FOR_FULL_LOAD)
            return false;
        ImmutableSchedule shedule = getSchedule();
        TrainOrdersModel order = shedule.getOrder(shedule.getOrderToGoto());
        if (order.stationId != stationId)
            return false;
        if (!order.waitUntilFull)
            return false;
        TrainModel train = getTrain();
        return order.getConsist().equals(train.getConsist());
    }

    /**
     * @return the location of the station the train is currently heading
     * towards.
     */
    public ImPoint getTarget() {
        TrainModel train = (TrainModel) w.get(p, KEY.TRAINS, id);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule schedule = (ImmutableSchedule) w.get(p,
                KEY.TRAIN_SCHEDULES, scheduleID);
        int stationNumber = schedule.getStationToGoto();

        if (-1 == stationNumber) {
            // There are no stations on the schedule.
            return new ImPoint(0, 0);
        }

        StationModel station = (StationModel) w.get(p, KEY.STATIONS,
                stationNumber);

        return new ImPoint(station.x, station.y);
    }

    /**
     * @param time
     * @return
     */
    public HashSet<TrackSection> occupiedTrackSection(double time) {
        TrainMotion tm = findCurrentMotion(time);
        PathOnTiles path = tm.getPath();
        HashSet<TrackSection> sections = new HashSet<>();
        ImPoint start = path.getStart();
        int x = start.x;
        int y = start.y;
        for (int i = 0; i < path.steps(); i++) {
            Step s = path.getStep(i);
            ImPoint tile = new ImPoint(x, y);
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
        TrainMotion tm = findCurrentMotion(time);
        double speed = tm.getSpeedAtEnd();
        return speed != 0;
    }

    /**
     * The space available on the train measured in cargo units.
     *
     * @return
     */
    public ImInts spaceAvailable() {

        TrainModel train = (TrainModel) w.get(p, KEY.TRAINS, id);
        ImmutableCargoBundle bundleOnTrain = (ImmutableCargoBundle) w.get(p,
                KEY.CARGO_BUNDLES, train.getCargoBundleID());
        return spaceAvailable2(w, bundleOnTrain, train.getConsist());

    }

}

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

package freerails.model.train;

import freerails.model.Identifiable;
import freerails.model.ModelConstants;
import freerails.model.game.Time;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackSection;
import freerails.model.train.activity.Activity;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.train.motion.TrainMotion;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.util.BidirectionalIterator;
import freerails.util.Vec2D;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

// TODO add position on map to train
// TODO delete Activities when they are finished
// TODO naming of wagonTypes/consist clarify
// TODO if all activities are TrainMotions, make it a list of Motions instead
/**
 * Represents a train.
 */
public class Train extends Identifiable {

    private int engineId;
    private List<Integer> wagonTypes = new ArrayList<>();
    private UnmodifiableCargoBatchBundle cargoBatchBundle = CargoBatchBundle.EMPTY;
    private UnmodifiableSchedule schedule = Schedule.EMPTY;
    private List<Activity> activities = new ArrayList<>();

    // TODO we have to trust that the ids are valid, only the world can check this consistency
    public Train(int id, int engineId) {
        super(id);
        this.engineId = engineId;
    }

    public BidirectionalIterator<Activity> getActivities() {
        return new BidirectionalIterator<>(activities);
    }

    // TODO not sure if this is a good idea, would like to use addActivity probably
    public void setActivities(BidirectionalIterator<Activity> iterator) {
        activities.clear();
        while (iterator.hasNext()) {
            iterator.next();
            activities.add(iterator.get());
        }
    }

    public void removeLastActivity() {
        activities.remove(activities.size() - 1);
    }

    /**
     * Does not start before the last activity has ended.
     *
     * @param activity
     */
    public void addActivity(@NotNull Activity activity) {
        if (!activities.isEmpty()) {
            Activity last = activities.get(activities.size() - 1);
            double lastFinishTime = last.getStartTime() + last.getDuration();
            activity.setStartTime(Math.max(activity.getStartTime(), lastFinishTime));
        }
        activities.add(activity);
    }

    /**
     * @return
     */
    public int getLength() {
        return (1 + wagonTypes.size()) * ModelConstants.WAGON_LENGTH; // Engine + wagons.
    }

    /**
     * @return
     */
    public int getNumberOfWagons() {
        return wagonTypes.size();
    }

    /**
     * @param position
     * @return
     */
    public int getWagonType(int position) {
        return wagonTypes.get(position);
    }

    /**
     * @return
     */
    public int getEngine() {
        return engineId;
    }

    public void setEngine(int engineId) {
        this.engineId = engineId;
    }

    /**
     * @return
     */
    public UnmodifiableCargoBatchBundle getCargoBatchBundle() {
        return cargoBatchBundle;
    }

    /**
     * Makes a copy.
     *
     * @param cargoBatchBundle
     */
    public void setCargoBatchBundle(@NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        this.cargoBatchBundle = new CargoBatchBundle(cargoBatchBundle);
    }

    /**
     * @return
     */
    public UnmodifiableSchedule getSchedule() {
        return schedule;
    }

    /**
     * Makes a copy of the schedule.
     * @param schedule
     */
    public void setSchedule(@NotNull UnmodifiableSchedule schedule) {
        this.schedule = new Schedule(schedule);
    }

    /**
     * @return
     */
    public List<Integer> getConsist() {
        return wagonTypes;
    }

    // TODO defensive copy?
    public void setConsist(@NotNull List<Integer> wagonTypes) {
        this.wagonTypes = wagonTypes;
    }

    /**
     * @param time
     * @return
     */
    public TrainMotion findCurrentMotion(double time) {
        Activity activity = activities.get(0);
        boolean afterFinish = activity.getStartTime() + activity.getDuration() < time;
        if (afterFinish) {
            activity = activities.get(activities.size() - 1);
        }
        return (TrainMotion) activity;
    }

    /**
     * Convenience function.
     *
     * @param time
     * @return
     */
    public boolean isMoving(@NotNull Time time) {
        TrainMotion trainMotion = findCurrentMotion(time.getTicks());
        double speed = trainMotion.getSpeedAtEnd();
        return speed != 0;
    }

    /**
     * @param time
     * @return
     */
    public HashSet<TrackSection> occupiedTrackSection(@NotNull Time time) {
        TrainMotion trainMotion = findCurrentMotion(time.getTicks());
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

    public @NotNull Vec2D getLocation(@NotNull Time time) {
        TrainMotion trainMotion = findCurrentMotion(time.getTicks());
        PositionOnTrack positionOnTrack = trainMotion.getFinalPosition();
        return positionOnTrack.getLocation();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Train)) {
            return false;
        }
        // TODO there is a problem with the equality test on the sorted sets, only the id is tested (see https://docs.oracle.com/javase/7/docs/api/java/util/Set.html#equals(java.lang.Object))
        Train o = (Train) obj;
        boolean equal = getId() == o.getId() && engineId == o.getEngine() && Objects.equals(wagonTypes, o.wagonTypes);
        equal = equal && Objects.equals(cargoBatchBundle, o.cargoBatchBundle) && Objects.equals(schedule, o.schedule);
        equal = equal && Objects.equals(activities, o.activities);
        return equal;
    }
}

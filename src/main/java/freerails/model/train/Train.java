/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.util.Identifiable;
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
 * Represents a train's consist, cargo, schedule, and planned activities.
 */
public class Train extends Identifiable {

    private int engineId;
    private List<Integer> wagonTypes = new ArrayList<>();
    private UnmodifiableCargoBatchBundle cargoBatchBundle = CargoBatchBundle.EMPTY;
    private UnmodifiableSchedule schedule = Schedule.EMPTY;
    private List<Activity> activities = new ArrayList<>();

    // TODO we have to trust that the ids are valid, only the world can check this consistency
    /**
     * Creates a train with the specified identifier and engine type.
     *
     * @param id unique train identifier
     * @param engineId engine type identifier
     */
    public Train(int id, int engineId) {
        super(id);
        this.engineId = engineId;
    }

    /** Returns an iterator over this train's activities. */
    public BidirectionalIterator<Activity> getActivities() {
        return new BidirectionalIterator<>(activities);
    }

    // TODO not sure if this is a good idea, would like to use addActivity probably
    /**
     * Replaces the activities with the remaining elements of the supplied iterator.
     *
     * @param iterator iterator positioned before the activities to copy
     */
    public void setActivities(BidirectionalIterator<Activity> iterator) {
        activities.clear();
        while (iterator.hasNext()) {
            iterator.next();
            activities.add(iterator.get());
        }
    }

    /**
     * Does not start before the last activity has ended.
     *
     * @param activity activity to append; it is delayed until the previous activity ends
     */
    public void addActivity(@NotNull Activity activity) {
        if (!activities.isEmpty()) {
            Activity last = activities.getLast();
            double lastFinishTime = last.getStartTime() + last.getDuration();
            activity.setStartTime(Math.max(activity.getStartTime(), lastFinishTime));
        }
        activities.add(activity);
    }

    /** Returns the train length in map-distance units, including its engine. */
    public int getLength() {
        return (1 + wagonTypes.size()) * ModelConstants.WAGON_LENGTH; // Engine + wagons.
    }

    /** Returns the number of wagons in the consist. */
    public int getNumberOfWagons() {
        return wagonTypes.size();
    }

    /**
     * Returns the wagon type at the specified consist position.
     *
     * @param position zero-based wagon position
     * @return wagon type identifier
     * @throws IndexOutOfBoundsException if the position is outside the consist
     */
    public int getWagonType(int position) {
        return wagonTypes.get(position);
    }

    /** Returns the engine type identifier. */
    public int getEngine() {
        return engineId;
    }

    /**
     * Sets the train's engine type.
     *
     * @param engineId engine type identifier
     */
    public void setEngine(int engineId) {
        this.engineId = engineId;
    }

    /** Returns the train's cargo batches through the read-only bundle interface. */
    public UnmodifiableCargoBatchBundle getCargoBatchBundle() {
        return cargoBatchBundle;
    }

    /**
     * Makes a copy.
     *
     * @param cargoBatchBundle cargo contents to copy
     */
    public void setCargoBatchBundle(@NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        this.cargoBatchBundle = new CargoBatchBundle(cargoBatchBundle);
    }

    /** Returns the train's schedule through the read-only schedule interface. */
    public UnmodifiableSchedule getSchedule() {
        return schedule;
    }

    /**
     * Replaces the schedule with a defensive copy.
     *
     * @param schedule schedule to copy
     */
    public void setSchedule(@NotNull UnmodifiableSchedule schedule) {
        this.schedule = new Schedule(schedule);
    }

    /** Returns an immutable snapshot of the wagon type identifiers. */
    public List<Integer> getConsist() {
        return List.copyOf(wagonTypes);
    }

    /**
     * Replaces the wagon consist with a copy of the supplied wagon types.
     *
     * @param wagonTypes wagon type identifiers in consist order
     */
    public void setConsist(@NotNull List<Integer> wagonTypes) {
        this.wagonTypes = new ArrayList<>(wagonTypes);
    }

    /**
     * Finds the activity whose time interval contains or precedes the requested time.
     *
     * @param time game time in ticks
     * @return current or most recently started train motion
     * @throws IllegalStateException if the train has no activities
     */
    public TrainMotion findCurrentMotion(double time) {
        if (activities.isEmpty()) {
            throw new IllegalStateException("Train has no activities.");
        }

        Activity previous = activities.getFirst();
        for (Activity activity : activities) {
            if (time < activity.getStartTime()) {
                break;
            }
            previous = activity;
        }
        return (TrainMotion) previous;
    }

    /**
     * Convenience function.
     *
     * @param time
     * @return {@code true} if the train's current motion ends with nonzero speed
     */
    public boolean isMoving(@NotNull Time time) {
        TrainMotion trainMotion = findCurrentMotion(time.ticks());
        double speed = trainMotion.getSpeedAtEnd();
        return speed != 0;
    }

    /**
     * Returns the track sections traversed by the train's current motion path.
     *
     * @param time game time
     * @return track sections occupied along the current motion path
     */
    public HashSet<TrackSection> occupiedTrackSection(@NotNull Time time) {
        TrainMotion trainMotion = findCurrentMotion(time.ticks());
        PathOnTiles path = trainMotion.getPath();
        HashSet<TrackSection> sections = new HashSet<>();
        Vec2D start = path.getStart();
        int x = start.x();
        int y = start.y();
        for (int i = 0; i < path.steps(); i++) {
            TileTransition transition = path.getStep(i);
            Vec2D tile = new Vec2D(x, y);
            x += transition.deltaX;
            y += transition.deltaY;
            sections.add(new TrackSection(transition, tile));
        }
        return sections;
    }

    /**
     * Returns the tile location at the end of the current motion's path.
     *
     * @param time game time
     * @return train location in tile coordinates
     */
    public @NotNull Vec2D getLocation(@NotNull Time time) {
        TrainMotion trainMotion = findCurrentMotion(time.ticks());
        PositionOnTrack positionOnTrack = trainMotion.getFinalPosition();
        return positionOnTrack.getLocation();
    }

    /** Compares train identity and its engine, consist, cargo, schedule, and activities. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Train o)) {
            return false;
        }
        // TODO there is a problem with the equality test on the sorted sets, only the id is tested (see https://docs.oracle.com/javase/7/docs/api/java/util/Set.html#equals(java.lang.Object))
        boolean equal = getId() == o.getId() && engineId == o.getEngine() && Objects.equals(wagonTypes, o.wagonTypes);
        equal = equal && Objects.equals(cargoBatchBundle, o.cargoBatchBundle) && Objects.equals(schedule, o.schedule);
        equal = equal && Objects.equals(activities, o.activities);
        return equal;
    }
}

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

package freerails.model.operations.train;

import freerails.model.operations.cargo.ImmutableCargoBatchBundle;
import freerails.model.operations.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.util.BidirectionalIterator;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Mutable cargo, schedule, and activity state for a train. */
public final class TrainRuntimeState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ImmutableCargoBatchBundle cargoBatchBundle = ImmutableCargoBatchBundle.EMPTY;
    private UnmodifiableSchedule schedule = ScheduleState.EMPTY;
    /** Complete history is retained to support queries at earlier game times. */
    private List<TrainMotion> activities = new ArrayList<>();

    /** Returns an iterator over this train's activities. */
    public BidirectionalIterator<TrainMotion> getActivities() {
        return new BidirectionalIterator<>(activities);
    }

    /**
     * Replaces the activities with the remaining elements of the supplied iterator.
     *
     * @param iterator iterator positioned before the activities to copy
     */
    public void setActivities(BidirectionalIterator<TrainMotion> iterator) {
        activities.clear();
        while (iterator.hasNext()) {
            activities.add(iterator.next());
        }
    }

    /**
     * Appends an activity, delaying it until the previous activity ends.
     *
     * @param activity activity to append
     */
    public void addActivity(@NotNull TrainMotion activity) {
        addActivity(activity, activity.getStartTime());
    }

    /**
     * Appends an activity at or after the requested start time, delaying it
     * until the previous activity ends.
     *
     * @param activity activity to append
     * @param requestedStartTime requested start time in game ticks
     */
    public void addActivity(@NotNull TrainMotion activity, double requestedStartTime) {
        double startTime = requestedStartTime;
        if (!activities.isEmpty()) {
            TrainMotion last = activities.getLast();
            double lastFinishTime = last.getStartTime() + last.getDuration();
            startTime = Math.max(startTime, lastFinishTime);
        }
        activities.add(activity.withStartTime(startTime));
    }

    /** Returns the train's cargo batches through the read-only bundle interface. */
    public UnmodifiableCargoBatchBundle getCargoBatchBundle() {
        return cargoBatchBundle;
    }

    /** Replaces cargo with a defensive copy. */
    public void setCargoBatchBundle(@NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        this.cargoBatchBundle = new ImmutableCargoBatchBundle(cargoBatchBundle);
    }

    /** Returns the train's schedule through the read-only schedule interface. */
    public UnmodifiableSchedule getSchedule() {
        return schedule;
    }

    /** Replaces the schedule with a defensive copy. */
    public void setSchedule(@NotNull UnmodifiableSchedule schedule) {
        this.schedule = new ScheduleState(schedule);
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

        TrainMotion previous = activities.getFirst();
        for (TrainMotion activity : activities) {
            if (time < activity.getStartTime()) {
                break;
            }
            previous = activity;
        }
        return previous;
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TrainRuntimeState other)) {
            return false;
        }
        return Objects.equals(cargoBatchBundle, other.cargoBatchBundle)
                && Objects.equals(schedule, other.schedule)
                && Objects.equals(activities, other.activities);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(cargoBatchBundle, schedule, activities);
    }
}

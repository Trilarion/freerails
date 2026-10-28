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

/*
 *
 */
package freerails.model.train.motion;

import freerails.model.ModelConstants;
import freerails.model.train.PathOnTiles;
import freerails.model.train.PositionOnTrack;
import freerails.model.train.TrainState;
import freerails.util.Pair;
import freerails.util.Vec2D;
import freerails.model.train.activity.Activity;
import freerails.model.terrain.TileTransition;
import freerails.model.track.PathIterator;

import java.io.Serial;
import java.util.ArrayList;

/**
 * This immutable class provides methods that return a train's position and
 * speed at any time within an interval. An instance of this class will be
 * stored on the world object for each train rather the train's position. The
 * reasons for this are as follows.
 *
 * <ol type="i">
 * <li> It decouples the number of game updates per second and number of frames
 * per second shown by the client. If the train's position were stored on the
 * world object, it would get updated each game tick. But this would mean that
 * if the game was being updated 10 times per second, even if the client was
 * displaying 50 FPS, the train's motion would still appear jerky since its
 * position would only change 10 times per second. </li>
 * <li>
 *
 * It makes supporting low bandwidth networks easier since it allows the server
 * to send updates less frequently. </li>
 * </ol>
 *
 * @see freerails.model.train.PathOnTiles
 * @see CompositeMotion
 */
public class TrainMotion extends Activity<TrainPositionOnMap> {

    @Serial
    private static final long serialVersionUID = 3618423722025891641L;
    private final double duration, distanceEngineWillTravel;
    private final double initialPosition;
    private final PathOnTiles path;
    private final Motion speeds;
    private final int trainLength;
    private final TrainState trainState;

    /**
     * Creates a moving train activity along a tile path.
     *
     * @param path        the path the train will take.
     * @param path complete route available to the train
     * @param engineStep number of transitions from the path start to the engine
     * @param trainLength train length in map-distance units
     * @param speeds motion profile for the engine along the route
     * @throws IllegalArgumentException if the train length is outside the supported
     *         range, its initial route position is too short, or the motion profile
     *         does not cover the remaining route
     * @throws ArrayIndexOutOfBoundsException if {@code engineStep} exceeds the path's step count
     */

    public TrainMotion(PathOnTiles path, int engineStep, int trainLength, Motion speeds) {
        if (trainLength < ModelConstants.WAGON_LENGTH || trainLength > ModelConstants.MAX_TRAIN_LENGTH)
            throw new IllegalArgumentException();
        this.path = path;
        this.speeds = speeds;
        this.trainLength = trainLength;

        if (engineStep > path.steps()) throw new ArrayIndexOutOfBoundsException(String.valueOf(engineStep));

        initialPosition = path.getDistance(engineStep);
        if (initialPosition < trainLength)
            throw new IllegalArgumentException("The engine's initial position is not far enough along the path for " + "the train's initial position to be specified.");
        double totalPathDistance = path.getTotalDistance();
        distanceEngineWillTravel = totalPathDistance - initialPosition;
        if (distanceEngineWillTravel > speeds.getTotalDistance())
            throw new IllegalArgumentException("The train's speed is not defined for the whole of the journey.");

        if (distanceEngineWillTravel == 0) {
            duration = 0.0d;
        } else {
            double tempDuration = speeds.calculateTimeAtDistance(distanceEngineWillTravel);
            while (speeds.calculateDistanceAtTime(tempDuration) - distanceEngineWillTravel > 0) {
                tempDuration -= Math.ulp(tempDuration);
            }
            duration = tempDuration;
        }

        trainState = TrainState.READY;
        sanityCheck();
    }

    /**
     * Creates a stationary train activity with the supplied status and duration.
     *
     * @param path path containing the train
     * @param trainLength train length in map-distance units
     * @param duration activity duration
     * @param act train state during the activity
     */
    public TrainMotion(PathOnTiles path, int trainLength, double duration, TrainState act) {
        this.path = path;
        this.trainLength = trainLength;
        trainState = act;
        distanceEngineWillTravel = 0;
        initialPosition = path.getTotalDistance();
        speeds = ConstantAccelerationMotion.STOPPED;
        this.duration = duration;
    }

    /**
     * Checks we are not creating an object with an inconsistent state. That is,
     * at the time stored in the field duration, the engine must not have gone
     * off the end of the path.
     */
    /**
     * Verifies that the train remains on the path at the activity's end.
     *
     * @throws IllegalStateException if the train extends beyond the available path
     */
    private void sanityCheck() {
        double offset = calcOffSet(duration);
        double totalLength = path.getTotalDistance();
        double trainLengthDouble = trainLength;
        if (totalLength < offset + trainLengthDouble)
            throw new IllegalStateException(offset + " + " + trainLengthDouble + " > " + totalLength);
    }

    /**
     * Calculates the distance from the path start to the rear of the train at time {@code t}.
     *
     * @param t elapsed time in this activity
     * @return path distance at the train's rear
     */
    private double calcOffSet(double t) {
        return getDistance(t) + initialPosition - trainLength;
    }

    /**
     * Validates that a time lies within this activity.
     *
     * @param t elapsed time
     * @throws IllegalArgumentException if time is outside the activity interval
     */
    private void checkT(double t) {
        if (t < 0.0d || t > duration) throw new IllegalArgumentException("t=" + t + ", but duration=" + duration);
    }

    /** Returns the duration of this train activity. */
    @Override
    public double getDuration() {
        return duration;
    }

    /** Compares the train length, path, and motion profile. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TrainMotion trainMotion)) return false;

        if (trainLength != trainMotion.trainLength) return false;
        if (!path.equals(trainMotion.path)) return false;
        return speeds.equals(trainMotion.speeds);
    }

    /**
     * Returns the engine's distance along the track since this motion began.
     *
     * @param t elapsed time in this activity
     * @return engine distance traveled since the start of this motion
     * @throws IllegalArgumentException if {@code t} is outside the activity interval
     */
    public double getDistance(double t) {
        checkT(t);
        t = Math.min(t, speeds.getTotalTime());
        return speeds.calculateDistanceAtTime(t);
    }

    /** Returns the final track position encoded by the route. */
    public PositionOnTrack getFinalPosition() {
        return path.getFinalPosition();
    }

    /** Returns the speed at the end of the underlying motion profile. */
    public double getSpeedAtEnd() {
        double finalT = speeds.getTotalTime();
        return speeds.calculateSpeedAtTime(finalT);
    }

    /**
     * Returns the train's position at the specified elapsed time.
     *
     * @param time elapsed time in this activity
     * @return the train's head-to-tail map position with its speed and state
     * @throws IllegalArgumentException if the underlying motion rejects the time
     */
    @Override
    public TrainPositionOnMap getStateAtTime(double time) {
        time = Math.min(time, speeds.getTotalTime());
        double offset = calcOffSet(time);
        Pair<PathIterator, Integer> pathIt = path.subPath(offset, trainLength);
        double speed = speeds.calculateSpeedAtTime(time);
        double acceleration = speeds.calculateAccelerationAtTime(time);
        return TrainPositionOnMap.createInSameDirectionAsPathReversed(pathIt, speed, acceleration, trainState);
    }

    /**
     * Returns the portion of the route occupied by the train at the specified time.
     *
     * @param t elapsed time in this activity
     * @return path transitions spanning the train's occupied route
     * @throws IllegalArgumentException if {@code t} is outside the activity interval
     */
    public PathOnTiles getTiles(double t) {
        checkT(t);
        t = Math.min(t, speeds.getTotalTime());
        double start = calcOffSet(t);
        double end = start + trainLength;
        ArrayList<TileTransition> tileTransitions = new ArrayList<>();
        double distanceSoFar = 0;

        int stepsBeforeStart = 0;
        int stepsAfterEnd = 0;

        for (int i = 0; i < path.steps(); i++) {
            if (distanceSoFar > end) stepsAfterEnd++;

            TileTransition tileTransition = path.getStep(i);
            distanceSoFar += tileTransition.getLength();

            if (distanceSoFar < start) stepsBeforeStart++;
        }

        int lastStep = path.steps() - stepsAfterEnd;
        for (int i = stepsBeforeStart; i < lastStep; i++) {
            tileTransitions.add(path.getStep(i));
        }

        Vec2D p = path.getStart();
        int x = p.x();
        int y = p.y();
        for (int i = 0; i < stepsBeforeStart; i++) {
            TileTransition tileTransition = path.getStep(i);
            x += tileTransition.deltaX;
            y += tileTransition.deltaY;
        }

        Vec2D startPoint = new Vec2D(x, y);

        return new PathOnTiles(startPoint, tileTransitions);
    }

    /** Returns the train length in map-distance units. */
    public int getTrainLength() {
        return trainLength;
    }

    /** Returns a hash code based on the path, motion profile, and train length. */
    @Override
    public int hashCode() {
        int result;
        result = path.hashCode();
        result = 29 * result + speeds.hashCode();
        result = 29 * result + trainLength;
        return result;
    }

    /** Returns the route path used by this activity. */
    public PathOnTiles getPath() {
        return path;
    }

    /** Returns the train state during this activity. */
    public TrainState getTrainState() {
        return trainState;
    }

    /** Returns the engine's initial distance along the route. */
    public double getInitialPosition() {
        return initialPosition;
    }

}

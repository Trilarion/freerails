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

import freerails.model.WorldConstants;
import freerails.util.Pair;
import freerails.util.Vector2D;
import freerails.model.Activity;
import freerails.model.terrain.TileTransition;
import freerails.model.track.PathIterator;
import freerails.model.train.motion.CompositeMotion;
import freerails.model.train.motion.ConstantAccelerationMotion;
import freerails.model.train.motion.Motion;

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
public strictfp class TrainMotion implements Activity<TrainPositionOnMap> {

    private static final long serialVersionUID = 3618423722025891641L;
    private final double duration, distanceEngineWillTravel;
    private final double initialPosition;
    private final PathOnTiles path;
    private final Motion speeds;
    private final int trainLength;
    private final TrainState activity;

    /**
     * Creates a new TrainMotion instance.
     *
     * @param path        the path the train will take.
     * @param engineStep  the position measured in tiles that trains engine is along the
     *                    path
     * @param trainLength the length of the train, as returned by
     *                    {@code TrainModel.getLength()}.
     * @throws IllegalArgumentException if trainLength is out the range
     *                                  {@code trainLength > TrainModel.WAGON_LENGTH || trainLength < TrainModel.MAX_TRAIN_LENGTH}
     * @throws IllegalArgumentException if {@code path.getDistance(engineStep) < trainLength}.
     * @throws IllegalArgumentException if
     *                                  {@code (path.getLength() - initialPosition) > speeds.getTotalDistance()}.
     */

    public TrainMotion(PathOnTiles path, int engineStep, int trainLength, Motion speeds) {
        if (trainLength < WorldConstants.WAGON_LENGTH || trainLength > WorldConstants.MAX_TRAIN_LENGTH)
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
            while ((speeds.calculateDistanceAtTime(tempDuration) - distanceEngineWillTravel) > 0) {
                tempDuration -= Math.ulp(tempDuration);
            }
            duration = tempDuration;
        }

        activity = TrainState.READY;
        sanityCheck();
    }

    /**
     * @param path
     * @param trainLength
     * @param duration
     * @param act
     */
    public TrainMotion(PathOnTiles path, int trainLength, double duration, TrainState act) {
        this.path = path;
        this.trainLength = trainLength;
        activity = act;
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
    private void sanityCheck() {
        double offset = calcOffSet(duration);
        double totalLength = path.getTotalDistance();
        double trainLengthDouble = trainLength;
        if (totalLength < offset + trainLengthDouble)
            throw new IllegalStateException(offset + " + " + trainLengthDouble + " > " + totalLength);
    }

    private double calcOffSet(double t) {
        return getDistance(t) + initialPosition - trainLength;
    }

    private void checkT(double t) {
        if (t < 0.0d || t > duration) throw new IllegalArgumentException("t=" + t + ", but duration=" + duration);
    }

    /**
     * @return
     */
    public double duration() {
        return duration;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TrainMotion)) return false;

        final TrainMotion trainMotion = (TrainMotion) obj;

        if (trainLength != trainMotion.trainLength) return false;
        if (!path.equals(trainMotion.path)) return false;
        return speeds.equals(trainMotion.speeds);
    }

    /**
     * Returns the train's distance along the track from the point the train was
     * at at time {@code getStart()} at the specified time.
     *
     * @param t the time.
     * @return the distance
     * @throws IllegalArgumentException if t is outside the interval
     */
    public double getDistance(double t) {
        checkT(t);
        t = Math.min(t, speeds.getTotalTime());
        return speeds.calculateDistanceAtTime(t);
    }

    /**
     * @return
     */
    public PositionOnTrack getFinalPosition() {
        return path.getFinalPosition();
    }

    /**
     * @return
     */
    public double getSpeedAtEnd() {
        double finalT = speeds.getTotalTime();
        return speeds.calculateSpeedAtTime(finalT);
    }

    /**
     * Returns the train's position at the specified time.
     *
     * @param time the time.
     * @return the train's position.
     * @throws IllegalArgumentException if t is outside the interval
     */
    public TrainPositionOnMap getStateAtTime(double time) {
        time = Math.min(time, speeds.getTotalTime());
        double offset = calcOffSet(time);
        Pair<PathIterator, Integer> pathIt = path.subPath(offset, trainLength);
        double speed = speeds.calculateSpeedAtTime(time);
        double acceleration = speeds.calculateAccelerationAtTime(time);
        return TrainPositionOnMap.createInSameDirectionAsPathReversed(pathIt, speed, acceleration, activity);
    }

    /**
     * Returns a PathOnTiles object that identifies the tiles the train is on at
     * the specified time.
     *
     * @param t the time.
     * @return an array of the tiles the train is on
     * @throws IllegalArgumentException if t is outside the interval
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

        Vector2D p = path.getStart();
        int x = p.x;
        int y = p.y;
        for (int i = 0; i < stepsBeforeStart; i++) {
            TileTransition tileTransition = path.getStep(i);
            x += tileTransition.deltaX;
            y += tileTransition.deltaY;
        }

        Vector2D startPoint = new Vector2D(x, y);

        return new PathOnTiles(startPoint, tileTransitions);
    }

    /**
     * @return
     */
    public int getTrainLength() {
        return trainLength;
    }

    @Override
    public int hashCode() {
        int result;
        result = path.hashCode();
        result = 29 * result + speeds.hashCode();
        result = 29 * result + trainLength;
        return result;
    }

    /**
     * @return
     */
    public PathOnTiles getPath() {
        return path;
    }

    /**
     * @return
     */
    public TrainState getActivity() {
        return activity;
    }

    /**
     * @return
     */
    public double getInitialPosition() {
        return initialPosition;
    }

}

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

import freerails.model.ModelConstants;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.station.StationUtils;
import freerails.model.terrain.TileTransition;
import freerails.model.track.NoTrackException;
import freerails.model.track.explorer.FlatTrackExplorer;
import freerails.model.train.activity.Activity;
import freerails.model.train.motion.ConstantAccelerationMotion;
import freerails.model.train.motion.TrainMotion;
import freerails.model.train.schedule.TrainOrder;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.BidirectionalIterator;
import freerails.util.Vec2D;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Utility operations for train capacity, placement, routing, and updates. */
public final class TrainUtils {

    private static final Logger logger = Logger.getLogger(TrainUtils.class.getName());


    /** Prevents construction of this utility class. */
    private TrainUtils() {
    }

    /**
     * @param world world providing cargo definitions and wagon capacities
     * @param cargoBatchBundle cargo already carried
     * @param consist wagon type identifiers
     * @return available capacity for each cargo type, indexed by world cargo index
     */
    public static List<Integer> spaceAvailable2(UnmodifiableWorld world, UnmodifiableCargoBatchBundle cargoBatchBundle, List<Integer> consist) {
        // This array will store the amount of space available on the train for each cargo type.
        final int NUM_CARGO_TYPES = world.getCargos().size();
        Integer[] spaceAvailable = new Integer[NUM_CARGO_TYPES];
        Arrays.fill(spaceAvailable, 0);

        // First calculate the train's total capacity.
        for (Integer aConsist : consist) {
            int cargoIndex = world.getCargoIndex(aConsist);
            spaceAvailable[cargoIndex] += ModelConstants.UNITS_OF_CARGO_PER_WAGON;
        }

        for (int cargoIndex = 0; cargoIndex < NUM_CARGO_TYPES; cargoIndex++) {
            int cargoId = world.getCargoAtIndex(cargoIndex).getId();
            spaceAvailable[cargoIndex] -= cargoBatchBundle.getAmountOfType(cargoId);
        }
        return new ArrayList<>(Arrays.asList(spaceAvailable));
    }

    /**
     * @return the location of the station the train is currently heading
     * towards.
     * @param world world containing the train and stations
     * @param player owner of the train
     * @param trainId train identifier
     * @return the target station location, or {@link Vec2D#ZERO} if no station is scheduled
     */
    public static Vec2D getTargetLocation(UnmodifiableWorld world, Player player, int trainId) {
        Train train = world.getTrain(player, trainId);
        UnmodifiableSchedule schedule = train.getSchedule();
        int stationId = schedule.getNextStationId();

        if (-1 == stationId) {
            // There are no stations on the schedule.
            return Vec2D.ZERO;
        }

        Station station = world.getStation(player, stationId);
        return station.getLocation();
    }

    /**
     * Calculates the length of an engine and its wagons.
     *
     * @param engineId engine type identifier
     * @param wagons wagon type identifiers
     * @return train length in map-distance units
     */
    public static int calculateTrainLength(int engineId, List<Integer> wagons) {
        // TODO is this a good idea, how often is this called, instead extract getLength() method
        Train train = new Train(0, engineId);
        train.setConsist(wagons);
        return train.getLength();
    }

    /**
     * After creation.
     *
     * @param world world used to inspect track
     * @param point initial tile coordinates
     * @param engineId engine type identifier
     * @param wagons wagon type identifiers
     * @return path long enough to place the complete train
     * @throws IllegalArgumentException if no track is available at the selected position
     */
    public static PathOnTiles initPositionTrainGetPath(UnmodifiableWorld world, Vec2D point, int engineId, List<Integer> wagons) {
        PositionOnTrack[] pp = FlatTrackExplorer.getPossiblePositions(world, point);
        FlatTrackExplorer fte;
        try {
            fte = new FlatTrackExplorer(world, pp[0]);
        } catch (NoTrackException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        List<TileTransition> tileTransitions = new ArrayList<>();
        int length = calculateTrainLength(engineId, wagons);
        int distanceTravelled = 0;
        PositionOnTrack p = new PositionOnTrack();
        while (distanceTravelled < length) {
            fte.nextEdge();
            fte.moveForward();
            p.setValuesFromInt(fte.getPosition());
            TileTransition v = p.getComingFrom();
            distanceTravelled += v.getLength();
            tileTransitions.add(v);
        }
        return new PathOnTiles(point, tileTransitions);
    }

    /**
     * Creates a stopped motion representing the train at its initial placement.
     *
     * @param path path containing the train
     * @param engineId engine type identifier
     * @param wagons wagon type identifiers
     * @return initial stopped train motion
     */
    public static TrainMotion initPositionTrainGetMotion(PathOnTiles path, int engineId, List<Integer> wagons) {
        return new TrainMotion(path, path.steps(), calculateTrainLength(engineId, wagons), ConstantAccelerationMotion.STOPPED);
    }

    /**
     * Extends the rear of a train path until it covers the requested train length.
     *
     * @param world world used to follow connected track
     * @param path current path
     * @param currentTrainLength required length in map-distance units
     * @return path extended at its start to provide the required length
     * @throws IllegalArgumentException if no connected track is available to extend the path
     */
    public static PathOnTiles lengthenPath(UnmodifiableWorld world, PathOnTiles path, int currentTrainLength) {
        double pathDistance = path.getTotalDistance();
        double extraDistanceNeeded = currentTrainLength - pathDistance;

        List<TileTransition> tileTransitions = new ArrayList<>();
        Vec2D start = path.getStart();
        TileTransition firstTileTransition = path.getStep(0);
        PositionOnTrack nextPositionOnTrack = new PositionOnTrack(start, firstTileTransition);

        while (extraDistanceNeeded > 0) {

            FlatTrackExplorer flatTrackExplorer;
            try {
                flatTrackExplorer = new FlatTrackExplorer(world, nextPositionOnTrack);
            } catch (NoTrackException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            flatTrackExplorer.nextEdge();
            nextPositionOnTrack.setValuesFromInt(flatTrackExplorer.getVertexConnectedByEdge());
            TileTransition cameFrom = nextPositionOnTrack.getFacingTo();
            tileTransitions.addFirst(cameFrom);
            extraDistanceNeeded -= cameFrom.getLength();
        }

        // Add existing tileTransitions
        for (int i = 0; i < path.steps(); i++) {
            TileTransition tileTransition = path.getStep(i);
            tileTransitions.add(tileTransition);
        }

        path = new PathOnTiles(nextPositionOnTrack.getLocation(), tileTransitions);
        return path;
    }

    /**
     * Returns whether all wagon capacity is occupied.
     *
     * @param world world containing cargo and train data
     * @param player train owner
     * @param trainId train identifier
     * @return {@code true} if no cargo type has remaining capacity
     */
    public static boolean isTrainFull(UnmodifiableWorld world, Player player, int trainId) {
        // determine the space available on the train measured in cargo units.
        Train train = world.getTrain(player, trainId);
        List<Integer> spaceAvailable = spaceAvailable2(world, train.getCargoBatchBundle(), train.getConsist());
        return spaceAvailable.stream().noneMatch(space -> space > 0);
    }

    /**
     * Returns whether the train is not full and its current order requests waiting for a full load.
     *
     * @param world world containing the train
     * @param player train owner
     * @param trainId train identifier
     * @return {@code true} when the train is waiting for full loading
     */
    public static boolean isWaitingForFullLoad(UnmodifiableWorld world, Player player, int trainId) {
        Train train = world.getTrain(player, trainId);
        UnmodifiableSchedule schedule = train.getSchedule();
        int orderToGoto = schedule.getCurrentOrderIndex();
        if (orderToGoto < 0) {
            return false;
        }
        TrainOrder order = schedule.getOrder(orderToGoto);
        return !isTrainFull(world, player, trainId) && order.isWaitUntilFull();
    }

    /**
     * Determines whether the train should receive another activity update.
     *
     * @param world current world state
     * @param player train owner
     * @param trainId train identifier
     * @return {@code true} if cargo availability or activity completion calls for an update
     * @throws IllegalStateException if a train waiting for cargo is not at a station
     */
    public static boolean isUpdateDue(UnmodifiableWorld world, Player player, int trainId) {
        Time currentTime = world.getClock().getCurrentTime();
        BidirectionalIterator<Activity> bidirectionalIterator = world.getTrain(player, trainId).getActivities();
        bidirectionalIterator.gotoLast();

        double finishTime = bidirectionalIterator.get().getStartTime() + bidirectionalIterator.get().getDuration();

        Train train = world.getTrain(player, trainId);
        TrainMotion trainMotion = train.findCurrentMotion(finishTime);
        TrainState trainState = trainMotion.getTrainState();

        if (trainState == TrainState.WAITING_FOR_FULL_LOAD) {
            // Check whether there is any cargo that can be added to the train.
            // determine the space available on the train measured in cargo units.
            List<Integer> spaceAvailable = spaceAvailable2(world, train.getCargoBatchBundle(), train.getConsist());
            Integer stationId = StationUtils.getStationId(world, player, train.getLocation(currentTime));
            if (stationId == null) throw new IllegalStateException();

            Station station = world.getStation(player, stationId);
            UnmodifiableCargoBatchBundle cargoBatchBundle = station.getCargoBatchBundle();

            for (int cargoIndex = 0; cargoIndex < spaceAvailable.size(); cargoIndex++) {
                int space = spaceAvailable.get(cargoIndex);
                int cargoId = world.getCargoAtIndex(cargoIndex).getId();
                int atStation = cargoBatchBundle.getAmountOfType(cargoId);
                if (space * atStation > 0) {
                    logger.debug("There is cargo to transfer!");
                    return true;
                }
            }
            return !keepWaiting(world, player, trainId);
        }

        boolean hasFinishedLastActivity = Math.floor(finishTime) <= (double) currentTime.ticks();
        return hasFinishedLastActivity;
    }

    /**
     * Returns true if all the following hold.
     * <ol>
     * <li>The train is waiting for a full load at some station X.</li>
     * <li>The current train order tells the train to goto station X.</li>
     * <li>The current train order tells the train to wait for a full load.</li>
     * <li>The current train order specifies a consist that matches the train's current consist.</li>
     * </ol>
     *
     * @param world current world state
     * @param player train owner
     * @param trainId train identifier
     * @return {@code true} while the current full-load order remains applicable
     */
    public static boolean keepWaiting(UnmodifiableWorld world, Player player, int trainId) {
        Time currentTime = world.getClock().getCurrentTime();
        Train train = world.getTrain(player, trainId);
        Integer stationId =  StationUtils.getStationId(world, player, train.getLocation(currentTime));
        if (stationId == null) return false;
        TrainMotion trainMotion = train.findCurrentMotion(currentTime.ticks());
        TrainState trainState = trainMotion.getTrainState();
        if (trainState != TrainState.WAITING_FOR_FULL_LOAD) return false;
        UnmodifiableSchedule schedule = train.getSchedule();
        TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());
        if (order.getStationId() != stationId) return false;
        if (!order.isWaitUntilFull()) return false;
        return order.getConsist().equals(train.getConsist());
    }
}

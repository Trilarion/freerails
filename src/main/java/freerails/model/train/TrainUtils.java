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
import freerails.util.Utils;
import freerails.util.Vec2D;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public final class TrainUtils {

    private static final Logger logger = Logger.getLogger(TrainUtils.class.getName());


    private TrainUtils() {
    }

    /**
     * @param world
     * @param cargoBatchBundle
     * @param consist
     * @return
     */
    public static List<Integer> spaceAvailable2(UnmodifiableWorld world, UnmodifiableCargoBatchBundle cargoBatchBundle, List<Integer> consist) {
        // This array will store the amount of space available on the train for each cargo type.
        final int NUM_CARGO_TYPES = world.getCargos().size();
        Integer[] spaceAvailable = new Integer[NUM_CARGO_TYPES];
        Arrays.fill(spaceAvailable, 0);

        // First calculate the train's total capacity.
        for (Integer aConsist : consist) {
            int cargoType = aConsist;
            spaceAvailable[cargoType] += ModelConstants.UNITS_OF_CARGO_PER_WAGON;
        }

        for (int cargoType = 0; cargoType < NUM_CARGO_TYPES; cargoType++) {
            spaceAvailable[cargoType] = spaceAvailable[cargoType] - cargoBatchBundle.getAmountOfType(cargoType);
        }
        // TODO what to do in case of negative numbers? throw an exception?
        return new ArrayList<>(Arrays.asList(spaceAvailable));
    }

    /**
     * @return the location of the station the train is currently heading
     * towards.
     * @param world
     * @param player
     * @param trainId
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

    public static int calculateTrainLength(int engineId, List<Integer> wagons) {
        // TODO is this a good idea, how often is this called, instead extract getLength() method
        Train train = new Train(0, engineId);
        train.setConsist(wagons);
        return train.getLength();
    }

    /**
     * After creation.
     *
     * @param world
     * @param point
     * @param engineId
     * @param wagons
     * @return
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

    public static TrainMotion initPositionTrainGetMotion(PathOnTiles path, int engineId, List<Integer> wagons) {
        return new TrainMotion(path, path.steps(), calculateTrainLength(engineId, wagons), ConstantAccelerationMotion.STOPPED);
    }

    /**
     * If wagons are added to a train, we need to increase its length.
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
            tileTransitions.add(0, cameFrom);
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
     * @return
     */
    public static boolean isTrainFull(UnmodifiableWorld world, Player player, int trainId) {
        // determine the space available on the train measured in cargo units.
        Train train = world.getTrain(player, trainId);
        List<Integer> spaceAvailable = spaceAvailable2(world, train.getCargoBatchBundle(), train.getConsist());
        // TODO this is not fully correct, because there could also be negative numbers returned from spaceAvailable and they could sum up to zero
        return Utils.sumOfIntegerList(spaceAvailable) == 0;
    }

    /**
     * @return
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
     * Returns true if an updated is due.
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

            for (int i = 0; i < spaceAvailable.size(); i++) {
                int space = spaceAvailable.get(i);
                int atStation = cargoBatchBundle.getAmountOfType(i);
                if (space * atStation > 0) {
                    logger.debug("There is cargo to transfer!");
                    return true;
                }
            }
            return !keepWaiting(world, player, trainId);
        }

        boolean hasFinishedLastActivity = Math.floor(finishTime) <= (double) currentTime.getTicks();
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
     */
    public static boolean keepWaiting(UnmodifiableWorld world, Player player, int trainId) {
        Time currentTime = world.getClock().getCurrentTime();
        Train train = world.getTrain(player, trainId);
        Integer stationId =  StationUtils.getStationId(world, player, train.getLocation(currentTime));
        if (stationId == null) return false;
        TrainMotion trainMotion = train.findCurrentMotion(currentTime.getTicks());
        TrainState trainState = trainMotion.getTrainState();
        if (trainState != TrainState.WAITING_FOR_FULL_LOAD) return false;
        UnmodifiableSchedule schedule = train.getSchedule();
        TrainOrder order = schedule.getOrder(schedule.getCurrentOrderIndex());
        if (order.getStationId() != stationId) return false;
        if (!order.isWaitUntilFull()) return false;
        return order.getConsist().equals(train.getConsist());
    }
}

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
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.TileTransition;
import freerails.model.track.NoTrackException;
import freerails.model.track.explorer.FlatTrackExplorer;
import freerails.model.train.motion.ConstantAccelerationMotion;
import freerails.model.train.motion.TrainMotion;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Vec2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public final class TrainUtils {

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
        Train train = new Train(0, engineId, wagons, new CargoBatchBundle(), new Schedule());
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
            TileTransition v = p.cameFrom();
            distanceTravelled += v.getLength();
            tileTransitions.add(v);
        }
        return new PathOnTiles(point, tileTransitions);
    }

    public static TrainMotion initPositionTrainGetMotion(PathOnTiles path, int engineId, List<Integer> wagons) {
        return new TrainMotion(path, path.steps(), calculateTrainLength(engineId, wagons), ConstantAccelerationMotion.STOPPED);
    }
}

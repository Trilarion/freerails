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
package freerails.controller;

import freerails.move.*;
import freerails.util.ImmutableList;
import freerails.util.Point2D;
import freerails.util.Utils;
import freerails.world.KEY;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.cargo.ImmutableCargoBatchBundle;
import freerails.world.finances.ItemTransaction;
import freerails.world.finances.Money;
import freerails.world.finances.Transaction;
import freerails.world.finances.TransactionCategory;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.TileTransition;
import freerails.world.train.*;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class AddTrainPreMove implements PreMove {

    private static final long serialVersionUID = 4050201951105069624L;
    private final int engineTypeId;
    private final ImmutableList<Integer> wagons;
    private final Point2D point;
    private final FreerailsPrincipal principal;
    private final ImmutableSchedule schedule;

    /**
     * @param e
     * @param wagons
     * @param p
     * @param principal
     * @param schedule
     */
    public AddTrainPreMove(int e, ImmutableList<Integer> wagons, Point2D p, FreerailsPrincipal principal, ImmutableSchedule schedule) {
        engineTypeId = e;
        this.wagons = Utils.verifyNotNull(wagons);
        point = Utils.verifyNotNull(p);
        this.principal = Utils.verifyNotNull(principal);
        this.schedule = Utils.verifyNotNull(schedule);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AddTrainPreMove)) return false;

        final AddTrainPreMove addTrainPreMove = (AddTrainPreMove) obj;

        if (engineTypeId != addTrainPreMove.engineTypeId) return false;
        if (!point.equals(addTrainPreMove.point)) return false;
        if (!principal.equals(addTrainPreMove.principal)) return false;
        if (!schedule.equals(addTrainPreMove.schedule)) return false;
        return wagons.equals(addTrainPreMove.wagons);
    }

    @Override
    public int hashCode() {
        int result;
        result = engineTypeId;
        result = 29 * result + point.hashCode();
        result = 29 * result + principal.hashCode();
        result = 29 * result + schedule.hashCode();
        return result;
    }

    private PathOnTiles initPositionStep1(ReadOnlyWorld world) {
        PositionOnTrack[] pp = FlatTrackExplorer.getPossiblePositions(world, point);
        FlatTrackExplorer fte;
        try {
            fte = new FlatTrackExplorer(world, pp[0]);
        } catch (NoTrackException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        List<TileTransition> tileTransitions = new ArrayList<>();
        int length = calTrainLength();
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

    private int calTrainLength() {
        TrainModel train = new TrainModel(engineTypeId, wagons, 0);
        return train.getLength();
    }

    private TrainMotion initPositionStep2(PathOnTiles path) {
        return new TrainMotion(path, path.steps(), calTrainLength(), ConstantAcceleration.STOPPED);
    }

    /**
     * Generates a move that does the following.
     * <ol>
     * <li>Adds the train</li>
     * <li>Adds a cargo bundle to represent the cargo the train is carrying</li>
     * <li>Adds a schedule for the train</li>
     * <li>Adds transaction to pay for the train</li>
     * <li>Init. the trains position and motion</li>
     * </ol>
     */
    public Move generateMove(ReadOnlyWorld world) {
        // Add cargo bundle.
        int bundleId = world.size(principal, KEY.CARGO_BUNDLES);
        ImmutableCargoBatchBundle cargo = ImmutableCargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE;
        AddItemToListMove addCargoBundle = new AddItemToListMove(KEY.CARGO_BUNDLES, bundleId, cargo, principal);

        // Add schedule
        int scheduleId = world.size(principal, KEY.TRAIN_SCHEDULES);
        AddItemToListMove addSchedule = new AddItemToListMove(KEY.TRAIN_SCHEDULES, scheduleId, schedule, principal);

        // Add train to train list.
        TrainModel train = new TrainModel(engineTypeId, wagons, scheduleId, bundleId);
        int trainId = world.size(principal, KEY.TRAINS);
        AddItemToListMove addTrain = new AddItemToListMove(KEY.TRAINS, trainId, train, principal);

        // Pay for train.
        int quantity = 1;
        // Determine the price of the train.
        EngineType engineType = (EngineType) world.get(SKEY.ENGINE_TYPES, engineTypeId);
        Money price = engineType.getPrice();
        Transaction transaction = new ItemTransaction(TransactionCategory.TRAIN, engineTypeId, quantity, new Money(-price.getAmount()));
        AddTransactionMove transactionMove = new AddTransactionMove(principal, transaction);

        // Setup and add train position.

        PathOnTiles path = initPositionStep1(world);
        TrainMotion motion = initPositionStep2(path);

        Move addPosition = new AddActiveEntityMove(motion, trainId, principal);

        return new CompositeMove(addCargoBundle, addSchedule, addTrain, transactionMove, addPosition);
    }

}

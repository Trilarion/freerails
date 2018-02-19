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
package freerails.move.premove;

import freerails.controller.explorer.FlatTrackExplorer;
import freerails.move.*;
import freerails.move.listmove.AddItemToListMove;
import freerails.util.ImmutableList;
import freerails.util.Vector2D;
import freerails.util.Utils;
import freerails.model.world.PlayerKey;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.model.cargo.ImmutableCargoBatchBundle;
import freerails.model.finances.ItemTransaction;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionCategory;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.terrain.TileTransition;
import freerails.model.track.NoTrackException;
import freerails.model.train.*;
import freerails.model.train.motion.ConstantAccelerationMotion;
import freerails.model.train.schedule.ImmutableSchedule;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class AddTrainMoveGenerator implements MoveGenerator {

    private static final long serialVersionUID = 4050201951105069624L;
    private final int engineTypeId;
    private final ImmutableList<Integer> wagons;
    private final Vector2D point;
    private final FreerailsPrincipal principal;
    private final ImmutableSchedule schedule;

    /**
     * @param e
     * @param wagons
     * @param p
     * @param principal
     * @param schedule
     */
    public AddTrainMoveGenerator(int e, ImmutableList<Integer> wagons, Vector2D p, FreerailsPrincipal principal, ImmutableSchedule schedule) {
        engineTypeId = e;
        this.wagons = Utils.verifyNotNull(wagons);
        point = Utils.verifyNotNull(p);
        this.principal = Utils.verifyNotNull(principal);
        this.schedule = Utils.verifyNotNull(schedule);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AddTrainMoveGenerator)) return false;

        final AddTrainMoveGenerator addTrainPreMove = (AddTrainMoveGenerator) obj;

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
        return new TrainMotion(path, path.steps(), calTrainLength(), ConstantAccelerationMotion.STOPPED);
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
    public Move generate(ReadOnlyWorld world) {
        // Add cargo bundle.
        int bundleId = world.size(principal, PlayerKey.CargoBundles);
        ImmutableCargoBatchBundle cargo = ImmutableCargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE;
        AddItemToListMove addCargoBundle = new AddItemToListMove(PlayerKey.CargoBundles, bundleId, cargo, principal);

        // Add schedule
        int scheduleId = world.size(principal, PlayerKey.TrainSchedules);
        AddItemToListMove addSchedule = new AddItemToListMove(PlayerKey.TrainSchedules, scheduleId, schedule, principal);

        // Add train to train list.
        TrainModel train = new TrainModel(engineTypeId, wagons, scheduleId, bundleId);
        int trainId = world.size(principal, PlayerKey.Trains);
        AddItemToListMove addTrain = new AddItemToListMove(PlayerKey.Trains, trainId, train, principal);

        // Pay for train.
        int quantity = 1;
        // Determine the price of the train.
        EngineType engineType = (EngineType) world.get(SharedKey.EngineTypes, engineTypeId);
        Money price = engineType.getPrice();
        Transaction transaction = new ItemTransaction(TransactionCategory.TRAIN, engineTypeId, quantity, Money.opposite(price));
        AddTransactionMove transactionMove = new AddTransactionMove(principal, transaction);

        // Setup and add train position.
        PathOnTiles path = initPositionStep1(world);
        TrainMotion motion = initPositionStep2(path);

        Move addPosition = new AddActiveEntityMove(motion, trainId, principal);

        return new CompositeMove(addCargoBundle, addSchedule, addTrain, transactionMove, addPosition);
    }

}

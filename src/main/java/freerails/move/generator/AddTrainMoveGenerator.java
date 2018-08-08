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
package freerails.move.generator;

import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.track.explorer.FlatTrackExplorer;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.move.*;
import freerails.move.listmove.AddItemToListMove;

import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.PlayerKey;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.finances.ItemTransaction;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.terrain.TileTransition;
import freerails.model.track.NoTrackException;
import freerails.model.train.*;
import freerails.model.train.motion.ConstantAccelerationMotion;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class AddTrainMoveGenerator implements MoveGenerator {

    private static final long serialVersionUID = 4050201951105069624L;
    private final int engineId;
    private final List<Integer> wagons;
    private final Vec2D point;
    private final Player player;
    private final UnmodifiableSchedule schedule;

    /**
     * @param engineId
     * @param wagons
     * @param p
     * @param player
     * @param schedule
     */
    public AddTrainMoveGenerator(int engineId, List<Integer> wagons, Vec2D p, Player player, UnmodifiableSchedule schedule) {
        this.engineId = engineId;
        this.wagons = Utils.verifyNotNull(wagons);
        point = Utils.verifyNotNull(p);
        this.player = Utils.verifyNotNull(player);
        this.schedule = Utils.verifyNotNull(schedule);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AddTrainMoveGenerator)) return false;

        final AddTrainMoveGenerator addTrainPreMove = (AddTrainMoveGenerator) obj;

        if (engineId != addTrainPreMove.engineId) return false;
        if (!point.equals(addTrainPreMove.point)) return false;
        if (!player.equals(addTrainPreMove.player)) return false;
        if (!schedule.equals(addTrainPreMove.schedule)) return false;
        return wagons.equals(addTrainPreMove.wagons);
    }

    @Override
    public int hashCode() {
        int result;
        result = engineId;
        result = 29 * result + point.hashCode();
        result = 29 * result + player.hashCode();
        result = 29 * result + schedule.hashCode();
        return result;
    }

    private PathOnTiles initPositionStep1(UnmodifiableWorld world) {
        PositionOnTrack[] pp = FlatTrackExplorer.getPossiblePositions(world, point);
        FlatTrackExplorer fte;
        try {
            fte = new FlatTrackExplorer(world, pp[0]);
        } catch (NoTrackException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        List<TileTransition> tileTransitions = new ArrayList<>();
        int length = calculateTrainLength();
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

    private int calculateTrainLength() {
        // TODO is this a good idea, how often is this called, should it maybe only be called once?
        Train train = new Train(0, engineId, wagons, 0, new Schedule());
        return train.getLength();
    }

    private TrainMotion initPositionStep2(PathOnTiles path) {
        return new TrainMotion(path, path.steps(), calculateTrainLength(), ConstantAccelerationMotion.STOPPED);
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
    public Move generate(UnmodifiableWorld world) {
        // Add cargo bundle.
        int bundleId = world.size(player, PlayerKey.CargoBundles);
        AddItemToListMove addCargoBundle = new AddItemToListMove(PlayerKey.CargoBundles, bundleId, CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, player);

        // Add train to train list.
        // TODO need a way to get a new id for trains, this is not the best way so far
        int id = world.getTrains(player).size();
        Train train = new Train(id, engineId, wagons, bundleId, schedule);
        // TODO this is a quite good idea and ensures unique ids (should be done on the server side only)
        int trainId = world.getTrains(player).size();
        // TODO we need an AddTrainMove
        // AddItemToListMove addTrain = new AddItemToListMove(PlayerKey.Trains, trainId, train, player);
        AddTrainMove addTrain = new AddTrainMove(player, train);

        // Pay for train.
        int quantity = 1;
        // Determine the price of the train.
        Engine engine = world.getEngine(engineId);
        Money price = engine.getPrice();
        Transaction transaction = new ItemTransaction(TransactionCategory.TRAIN, engineId, quantity, Money.opposite(price));
        AddTransactionMove transactionMove = new AddTransactionMove(player, transaction);

        // Setup and add train position.
        PathOnTiles path = initPositionStep1(world);
        TrainMotion motion = initPositionStep2(path);

        Move addPosition = new AddActiveEntityMove(motion, trainId, player);

        return new CompositeMove(addCargoBundle, addTrain, transactionMove, addPosition);
    }

}

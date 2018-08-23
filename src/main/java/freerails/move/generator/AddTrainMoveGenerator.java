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

import freerails.model.finance.transaction.Transaction;
import freerails.model.train.motion.TrainMotion;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.move.*;

import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.Money;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.train.*;

import java.util.Arrays;
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
    @Override
    public Move generate(UnmodifiableWorld world) {
        // Add train to train list.
        // TODO need a way to get a new id for trains, this is not the best way
        int id = world.getTrains(player).size();
        Train train = new Train(id, engineId);
        train.setConsist(wagons);
        train.setSchedule(schedule);
        // add train position.
        PathOnTiles path = TrainUtils.initPositionTrainGetPath(world, point, engineId, wagons);
        TrainMotion motion = TrainUtils.initPositionTrainGetMotion(path, engineId, wagons);
        train.addActivity(motion);
        AddTrainMove addTrain = new AddTrainMove(player, train);

        // Pay for the train.
        // Determine the price of the train.
        Money price = world.getEngine(engineId).getPrice();
        Transaction transaction = new ItemTransaction(TransactionCategory.TRAIN, Money.opposite(price), world.getClock().getCurrentTime(), 1, engineId);
        AddTransactionMove transactionMove = new AddTransactionMove(player, transaction);

        return new CompostMove(Arrays.asList(addTrain, transactionMove));
    }

}

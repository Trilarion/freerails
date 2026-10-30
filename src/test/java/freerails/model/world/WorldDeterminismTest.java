/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the FreeRails Team, either version 3 of the License, or
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

package freerails.model.world;

import freerails.model.cargo.CargoBatch;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.finance.BalanceSheetGenerator;
import freerails.model.finance.Money;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.terrain.TileTransition;
import freerails.model.train.PathOnTiles;
import freerails.model.train.Train;
import freerails.model.train.TrainState;
import freerails.model.train.motion.TrainMotion;
import freerails.move.AddTransactionMove;
import freerails.move.AdvanceClockMove;
import freerails.util.Utils;
import freerails.util.Vec2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldDeterminismTest extends TestCase {

    public void testEquivalentWorldsAdvanceIdenticallyThroughTheSameMovesAndTicks() {
        Player firstPlayer = new Player(1, "One");
        Player secondPlayer = new Player(1, "One");
        World first = createWorld(firstPlayer);
        World second = createWorld(secondPlayer);

        for (int tick = 0; tick < 6; tick++) {
            new AdvanceClockMove(Player.AUTHORITATIVE).apply(first);
            new AdvanceClockMove(Player.AUTHORITATIVE).apply(second);
            if (tick == 2) {
                new AddTransactionMove(firstPlayer, transaction(75, first.getClock().getCurrentTime())).apply(first);
                new AddTransactionMove(secondPlayer, transaction(75, second.getClock().getCurrentTime())).apply(second);
            }
        }

        Train firstTrain = first.getTrain(firstPlayer, 2);
        Train secondTrain = second.getTrain(secondPlayer, 2);
        assertEquals(first.getClock().getCurrentTime(), second.getClock().getCurrentTime());
        assertEquals(firstTrain.getLocation(first.getClock().getCurrentTime()),
                secondTrain.getLocation(second.getClock().getCurrentTime()));
        assertEquals(firstTrain.findCurrentMotion(first.getClock().getCurrentTime().ticks()).getTrainState(),
                secondTrain.findCurrentMotion(second.getClock().getCurrentTime().ticks()).getTrainState());
        assertEquals(firstTrain.getCargoBatchBundle(), secondTrain.getCargoBatchBundle());
        assertEquals(first.getCurrentBalance(firstPlayer), second.getCurrentBalance(secondPlayer));
        assertEquals(first.getTransactions(firstPlayer), second.getTransactions(secondPlayer));
        assertEquals(first, second);
    }

    public void testRepeatedModelQueriesDoNotMutateTheWorldOrTrain() {
        Player player = new Player(1, "One");
        World world = createWorld(player);
        World beforeQueries = (World) Utils.cloneBySerialisation(world);
        Train train = world.getTrain(player, 2);
        Time currentTime = world.getClock().getCurrentTime();
        Set<?> expectedSections = new HashSet<>(train.occupiedTrackSection(currentTime));
        Money expectedBalance = world.getCurrentBalance(player);
        var expectedTotal = new BalanceSheetGenerator(world, player).total;
        var expectedYtd = new BalanceSheetGenerator(world, player).ytd;

        for (int query = 0; query < 5; query++) {
            assertEquals(TrainState.READY, train.findCurrentMotion(currentTime.ticks()).getTrainState());
            assertEquals(new Vec2D(2, 0), train.getLocation(currentTime));
            Set<?> sections = train.occupiedTrackSection(currentTime);
            assertEquals(expectedSections, sections);
            sections.clear();
            assertEquals(expectedSections, train.occupiedTrackSection(currentTime));
            assertEquals(4, train.getCargoBatchBundle().getAmountOfType(3));
            assertEquals(expectedBalance, world.getCurrentBalance(player));

            var report = new BalanceSheetGenerator(world, player);
            assertEquals(expectedTotal.getLoans(), report.total.getLoans());
            assertEquals(expectedTotal.getEquity(), report.total.getEquity());
            assertEquals(expectedYtd.getLoans(), report.ytd.getLoans());
            assertEquals(expectedYtd.getEquity(), report.ytd.getEquity());
        }

        assertEquals(beforeQueries, world);
    }

    private static World createWorld(Player player) {
        World world = WorldGenerator.minimalWorld();
        world.addPlayer(player);
        Train train = new Train(2, 0);
        train.setConsist(List.of(4));
        CargoBatchBundle cargo = new CargoBatchBundle();
        cargo.addCargo(new CargoBatch(3, Vec2D.ZERO, 0, 0), 4);
        train.setCargoBatchBundle(cargo);
        TrainMotion motion = new TrainMotion(
                new PathOnTiles(Vec2D.ZERO, TileTransition.EAST, TileTransition.EAST),
                24, 20, TrainState.READY);
        world.addTrain(player, train);
        world.addActivity(player, train.getId(), motion);
        world.addTransaction(player, transaction(10, Time.ZERO));
        return world;
    }

    private static Transaction transaction(long amount, Time time) {
        return new Transaction(TransactionCategory.MISC_INCOME, new Money(amount), time);
    }
}

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

package freerails.move;

import freerails.model.operations.cargo.CargoBatch;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.simulation.Time;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.TrainConfiguration;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.world.TestTrainMotion;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.Utils;
import freerails.util.Vec2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.List;

public class RepresentativeMoveSerializationTest extends TestCase {

    public void testRepresentativeMovesSurviveSerialization() {
        Player player = new Player(3, "Test player");
        Transaction transaction = new Transaction(
                TransactionCategory.MISC_INCOME, new Money(125), new Time(42));
        TrackPiece trackPiece = new TrackPiece(
                TrackConfiguration.getFlatInstance("000010000"),
                WorldGenerator.testTrackTypes().first(),
                player.getId());
        CargoBatchBundle cargo = new CargoBatchBundle();
        cargo.addCargo(new CargoBatch(1, new Vec2D(8, 9), 42, 9), 4);
        ChangeCargoAtStationMove cargoMove = new ChangeCargoAtStationMove(player, 9, cargo);

        Train configuredTrain = new Train(7, new TrainConfiguration(2, List.of(4, 5)));
        configuredTrain.setSchedule(new ScheduleState());
        configuredTrain.setCargoBatchBundle(cargo);
        configuredTrain.addActivity(TestTrainMotion.of(60));

        Move[] moves = {
                new AddPlayerMove(player),
                new ChangeTileMove(new TerrainTile(1), new Vec2D(2, 3)),
                new ChangeTrackPieceMove(null, trackPiece, new Vec2D(2, 3)),
                new AddTrainMove(player, configuredTrain),
                new AddActivityMove(player, 7, TestTrainMotion.of(60)),
                new AddStationMove(player,
                        new Station(9, new Vec2D(4, 5), "Station", CargoBatchBundle.EMPTY)),
                cargoMove,
                new AddTransactionMove(player, transaction),
                new AddTransactionMove(player, transaction, true)
        };

        for (Move move : moves) {
            assertSurvivesSerialization(move);
        }

        assertFalse(moves[moves.length - 2].equals(moves[moves.length - 1]));
        assertFalse(cargoMove.equals(new ChangeCargoAtStationMove(player, 10, cargo)));

        CargoBatchBundle differentCargo = new CargoBatchBundle();
        differentCargo.addCargo(new CargoBatch(1, new Vec2D(8, 9), 42, 9), 5);
        assertFalse(cargoMove.equals(new ChangeCargoAtStationMove(player, 9, differentCargo)));
    }

    private void assertSurvivesSerialization(Move move) {
        Move copy = (Move) Utils.cloneBySerialisation(move);

        assertNotSame(move, copy);
        assertEquals(move, copy);
        assertEquals(move.hashCode(), copy.hashCode());
    }
}

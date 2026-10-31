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
import freerails.model.simulation.Speed;
import freerails.model.simulation.Time;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.operations.station.StationDemand;
import freerails.model.operations.station.StationSupply;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.TrainTemplate;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.world.TestTrainMotion;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.RemoveStationCompositeMove;
import freerails.util.Vec2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests move equality contract behavior.
 */
public class MoveEqualityContractTest extends TestCase {

    private static final Player PLAYER = new Player(1, "One");
    private static final Player OTHER_PLAYER = new Player(2, "Two");
    private static final Time TIME = new Time(42);
    private static final TrackType TRACK_TYPE = WorldGenerator.testTrackTypes().first();
    private static final TrackType OTHER_TRACK_TYPE = WorldGenerator.testTrackTypes().last();
    private static final TrackPiece TRACK_A = new TrackPiece(
            TrackConfiguration.getFlatInstance("000010000"), TRACK_TYPE, PLAYER.getId());
    private static final TrackPiece TRACK_OTHER_OWNER = new TrackPiece(
            TrackConfiguration.getFlatInstance("000010000"), TRACK_TYPE, OTHER_PLAYER.getId());

    /**
     * Tests player and transaction moves compare every field behavior.
     */
    public void testPlayerAndTransactionMovesCompareEveryField() {
        assertEqualMoves(new AddPlayerMove(new Player(1, "One")), new AddPlayerMove(new Player(1, "One")));
        assertDifferentMoves(new AddPlayerMove(PLAYER), new AddPlayerMove(new Player(2, "One")));
        assertDifferentMoves(new AddPlayerMove(PLAYER), new AddPlayerMove(new Player(1, "Different name")));

        Transaction income = transaction(100);
        assertEqualMoves(new AddTransactionMove(PLAYER, income), new AddTransactionMove(PLAYER, transaction(100)));
        assertDifferentMoves(new AddTransactionMove(PLAYER, income), new AddTransactionMove(new Player(2, "One"), income));
        assertDifferentMoves(new AddTransactionMove(PLAYER, income), new AddTransactionMove(PLAYER, transaction(101)));
        assertDifferentMoves(new AddTransactionMove(PLAYER, income),
                new AddTransactionMove(PLAYER, transaction(TransactionCategory.BOND, 100, TIME)));
        assertDifferentMoves(new AddTransactionMove(PLAYER, income),
                new AddTransactionMove(PLAYER, transaction(TransactionCategory.MISC_INCOME, 100, new Time(43))));
        assertDifferentMoves(new AddTransactionMove(PLAYER, income, false), new AddTransactionMove(PLAYER, income, true));

        assertEqualMoves(new AdvanceClockMove(PLAYER), new AdvanceClockMove(new Player(1, "One")));
        assertDifferentMoves(new AdvanceClockMove(PLAYER), new AdvanceClockMove(new Player(2, "One")));
        assertEqualMoves(new ChangeGameSpeedMove(Speed.SLOW), new ChangeGameSpeedMove(Speed.SLOW));
        assertDifferentMoves(new ChangeGameSpeedMove(Speed.SLOW), new ChangeGameSpeedMove(Speed.FAST));
    }

    /**
     * Tests train moves compare every field behavior.
     */
    public void testTrainMovesCompareEveryField() {
        Train train = new Train(4, 3);
        train.setConsist(List.of(5, 6));
        Train equalTrain = new Train(4, 3);
        equalTrain.setConsist(List.of(5, 6));
        assertEqualMoves(new AddTrainMove(PLAYER, train), new AddTrainMove(new Player(1, "One"), equalTrain));

        Train differentTrain = new Train(4, 7);
        differentTrain.setConsist(List.of(5, 6));
        assertDifferentMoves(new AddTrainMove(PLAYER, train), new AddTrainMove(PLAYER, differentTrain));
        assertDifferentMoves(new AddTrainMove(PLAYER, train), new AddTrainMove(PLAYER, new Train(5, 3)));
        assertDifferentMoves(new AddTrainMove(PLAYER, train), new AddTrainMove(new Player(2, "One"), train));
        Train trainWithActivity = new Train(4, 3);
        trainWithActivity.setConsist(List.of(5, 6));
        trainWithActivity.addActivity(TestTrainMotion.of(10));
        assertDifferentMoves(new AddTrainMove(PLAYER, train), new AddTrainMove(PLAYER, trainWithActivity));

        TrainMotion activity = TestTrainMotion.of(10);
        assertEqualMoves(new AddActivityMove(PLAYER, 4, activity),
                new AddActivityMove(new Player(1, "One"), 4, TestTrainMotion.of(10)));
        assertDifferentMoves(new AddActivityMove(PLAYER, 4, activity),
                new AddActivityMove(new Player(2, "One"), 4, activity));
        assertDifferentMoves(new AddActivityMove(PLAYER, 4, activity), new AddActivityMove(PLAYER, 5, activity));
        assertDifferentMoves(new AddActivityMove(PLAYER, 4, activity), new AddActivityMove(PLAYER, 4, TestTrainMotion.of(11)));

        CargoBatchBundle cargo = cargo(4);
        List<Integer> consist = List.of(5, 6);
        ScheduleState schedule = new ScheduleState();
        ScheduleState otherSchedule = new ScheduleState(new TrainOrder[]{
                TrainOrder.specifiedConsist(9, List.of(7), false)
        }, 0);
        UpdateTrainMove update = new UpdateTrainMove(PLAYER, 4, cargo, consist, schedule);
        assertEqualMoves(update, new UpdateTrainMove(new Player(1, "One"), 4, cargo(4),
                List.of(5, 6), new ScheduleState()));
        assertDifferentMoves(update, new UpdateTrainMove(new Player(2, "One"), 4, cargo, consist, schedule));
        assertDifferentMoves(update, new UpdateTrainMove(PLAYER, 5, cargo, consist, schedule));
        assertDifferentMoves(update, new UpdateTrainMove(PLAYER, 4, cargo(5), consist, schedule));
        assertDifferentMoves(update, new UpdateTrainMove(PLAYER, 4, cargo, List.of(6, 5), schedule));
        assertDifferentMoves(update, new UpdateTrainMove(PLAYER, 4, cargo, consist, otherSchedule));
    }

    /**
     * Tests station moves compare every field used by the move behavior.
     */
    public void testStationMovesCompareEveryFieldUsedByTheMove() {
        Station station = station(10, new Vec2D(3, 4), "Central");
        assertEqualMoves(new AddStationMove(PLAYER, station),
                new AddStationMove(new Player(1, "One"), station(10, new Vec2D(3, 4), "Central")));
        assertDifferentMoves(new AddStationMove(PLAYER, station),
                new AddStationMove(new Player(2, "One"), station));
        assertDifferentMoves(new AddStationMove(PLAYER, station), new AddStationMove(PLAYER, station(11, new Vec2D(3, 4), "Central")));
        assertDifferentMoves(new AddStationMove(PLAYER, station),
                new AddStationMove(PLAYER, station(10, new Vec2D(4, 4), "Central")));
        assertDifferentMoves(new AddStationMove(PLAYER, station),
                new AddStationMove(PLAYER, station(10, new Vec2D(3, 4), "Different name")));

        Station stationWithCargo = station(10, new Vec2D(3, 4), "Central");
        stationWithCargo.setCargoBatchBundle(cargo(2));
        assertDifferentMoves(new AddStationMove(PLAYER, station),
                new AddStationMove(PLAYER, stationWithCargo));
        Station stationWithSupply = station(10, new Vec2D(3, 4), "Central");
        stationWithSupply.setSupply(new StationSupply(Map.of(1, 1)));
        assertDifferentMoves(new AddStationMove(PLAYER, station),
                new AddStationMove(PLAYER, stationWithSupply));
        Station stationWithDemand = station(10, new Vec2D(3, 4), "Central");
        stationWithDemand.setDemandForCargo(new StationDemand(Set.of(1)));
        assertDifferentMoves(new AddStationMove(PLAYER, station),
                new AddStationMove(PLAYER, stationWithDemand));
        assertEqualMoves(new RemoveStationMove(PLAYER, 10), new RemoveStationMove(new Player(1, "One"), 10));
        assertDifferentMoves(new RemoveStationMove(PLAYER, 10), new RemoveStationMove(new Player(2, "One"), 10));
        assertDifferentMoves(new RemoveStationMove(PLAYER, 10), new RemoveStationMove(PLAYER, 11));

        CargoBatchBundle waiting = cargo(4);
        ChangeCargoAtStationMove changeCargo = new ChangeCargoAtStationMove(PLAYER, 10, waiting);
        assertEqualMoves(changeCargo, new ChangeCargoAtStationMove(new Player(1, "One"), 10, cargo(4)));
        assertDifferentMoves(changeCargo,
                new ChangeCargoAtStationMove(new Player(2, "One"), 10, waiting));
        assertDifferentMoves(changeCargo, new ChangeCargoAtStationMove(PLAYER, 11, waiting));
        assertDifferentMoves(changeCargo, new ChangeCargoAtStationMove(PLAYER, 10, cargo(5)));

        List<TrainTemplate> production = List.of(new TrainTemplate(3, List.of(5)));
        ChangeProductionAtEngineShopMove productionMove =
                new ChangeProductionAtEngineShopMove(production, 10, PLAYER);
        assertEqualMoves(productionMove, new ChangeProductionAtEngineShopMove(
                List.of(new TrainTemplate(3, List.of(5))), 10, new Player(1, "One")));
        assertDifferentMoves(productionMove,
                new ChangeProductionAtEngineShopMove(production, 10, new Player(2, "One")));
        assertDifferentMoves(productionMove, new ChangeProductionAtEngineShopMove(production, 11, PLAYER));
        assertDifferentMoves(productionMove, new ChangeProductionAtEngineShopMove(
                List.of(new TrainTemplate(4, List.of(5))), 10, PLAYER));
        assertDifferentMoves(productionMove, new ChangeProductionAtEngineShopMove(
                List.of(new TrainTemplate(3, List.of(6))), 10, PLAYER));

        Station cargoSettings = station(10, Vec2D.ZERO, "Settings");
        Station sameSettings = station(10, Vec2D.ZERO, "Different ignored name");
        UpdateStationCargoPolicyMove settingsMove =
                new UpdateStationCargoPolicyMove(PLAYER, cargoSettings);
        assertEqualMoves(settingsMove,
                new UpdateStationCargoPolicyMove(new Player(1, "One"), sameSettings));
        assertDifferentMoves(settingsMove,
                new UpdateStationCargoPolicyMove(new Player(2, "One"), cargoSettings));
        assertDifferentMoves(settingsMove,
                new UpdateStationCargoPolicyMove(PLAYER, station(11, Vec2D.ZERO, "Settings")));

        Station differentSupply = station(10, Vec2D.ZERO, "Settings");
        differentSupply.setSupply(new StationSupply(Map.of(1, 1)));
        assertDifferentMoves(settingsMove, new UpdateStationCargoPolicyMove(PLAYER, differentSupply));

        Station differentDemand = station(10, Vec2D.ZERO, "Settings");
        differentDemand.setDemandForCargo(new StationDemand(Set.of(1)));
        assertDifferentMoves(settingsMove, new UpdateStationCargoPolicyMove(PLAYER, differentDemand));
    }

    /**
     * Tests map and composite moves compare every field behavior.
     */
    public void testMapAndCompositeMovesCompareEveryField() {
        TerrainTile tile = new TerrainTile(4);
        assertEqualMoves(new ChangeTileMove(new TerrainTile(4), new Vec2D(2, 3)),
                new ChangeTileMove(new TerrainTile(4), new Vec2D(2, 3)));
        assertDifferentMoves(new ChangeTileMove(tile, new Vec2D(2, 3)),
                new ChangeTileMove(tile, new Vec2D(3, 3)));
        assertDifferentMoves(new ChangeTileMove(tile, new Vec2D(2, 3)),
                new ChangeTileMove(new TerrainTile(5), new Vec2D(2, 3)));
        assertDifferentMoves(new ChangeTileMove(tile, new Vec2D(2, 3)),
                new ChangeTileMove(new TerrainTile(4, TRACK_A), new Vec2D(2, 3)));

        ChangeTrackPieceMove trackMove = new ChangeTrackPieceMove(null, TRACK_A, new Vec2D(2, 3));
        assertEqualMoves(trackMove, new ChangeTrackPieceMove(null, TRACK_A, new Vec2D(2, 3)));
        assertDifferentMoves(trackMove, new ChangeTrackPieceMove(null, TRACK_OTHER_OWNER, new Vec2D(2, 3)));
        assertDifferentMoves(trackMove, new ChangeTrackPieceMove(null,
                new TrackPiece(TRACK_A.trackConfiguration(), OTHER_TRACK_TYPE, PLAYER.getId()), new Vec2D(2, 3)));
        assertDifferentMoves(trackMove, new ChangeTrackPieceMove(null,
                new TrackPiece(TrackConfiguration.getFlatInstance("010010000"), TRACK_TYPE, PLAYER.getId()),
                new Vec2D(2, 3)));
        assertDifferentMoves(trackMove, new ChangeTrackPieceMove(null, TRACK_A, new Vec2D(3, 3)));
        assertDifferentMoves(new ChangeTrackPieceMove(TRACK_A, TRACK_A, new Vec2D(2, 3)), trackMove);
        ChangeTrackPieceMove removal = new ChangeTrackPieceMove(TRACK_A, null, new Vec2D(2, 3));
        assertEqualMoves(removal, new ChangeTrackPieceMove(TRACK_A, null, new Vec2D(2, 3)));

        Move speedSlow = new ChangeGameSpeedMove(Speed.SLOW);
        Move speedFast = new ChangeGameSpeedMove(Speed.FAST);
        assertEqualMoves(new CompositeMove(List.of(speedSlow, new GrowCitiesMove())),
                new CompositeMove(List.of(new ChangeGameSpeedMove(Speed.SLOW), new GrowCitiesMove())));
        assertDifferentMoves(new CompositeMove(List.of(speedSlow, new GrowCitiesMove())),
                new CompositeMove(List.of(speedFast, new GrowCitiesMove())));
        assertEqualMoves(new CompostMove(List.of(speedSlow, speedFast)),
                new CompostMove(List.of(new ChangeGameSpeedMove(Speed.SLOW),
                        new ChangeGameSpeedMove(Speed.FAST))));
        assertDifferentMoves(new CompostMove(List.of(speedSlow, speedFast)),
                new CompostMove(List.of(speedSlow, new GrowCitiesMove())));

        ChangeTrackPieceMove secondTrackMove = new ChangeTrackPieceMove(null, TRACK_A, new Vec2D(3, 3));
        ChangeTrackPieceCompositeMove trackComposite =
                new ChangeTrackPieceCompositeMove(trackMove, secondTrackMove, PLAYER);
        ChangeTrackPieceCompositeMove sameTrackComposite =
                new ChangeTrackPieceCompositeMove(
                        new ChangeTrackPieceMove(null, TRACK_A, new Vec2D(2, 3)),
                        new ChangeTrackPieceMove(null, TRACK_A, new Vec2D(3, 3)),
                        new Player(1, "One"));
        assertEqualMoves(trackComposite, sameTrackComposite);
        assertDifferentMoves(trackComposite,
                new ChangeTrackPieceCompositeMove(trackMove, secondTrackMove, new Player(2, "One")));
        assertDifferentMoves(new CompositeMove(List.of(trackMove, secondTrackMove)), trackComposite);

        RemoveStationCompositeMove removeComposite = new RemoveStationCompositeMove(
                List.of(new ChangeTileMove(tile, new Vec2D(2, 3)), new RemoveStationMove(PLAYER, 10)));
        RemoveStationCompositeMove differentRemoveComposite = new RemoveStationCompositeMove(
                List.of(new ChangeTileMove(tile, new Vec2D(2, 3)), new RemoveStationMove(PLAYER, 11)));
        assertDifferentMoves(removeComposite, differentRemoveComposite);
        assertEqualMoves(removeComposite, new RemoveStationCompositeMove(
                List.of(new ChangeTileMove(new TerrainTile(4), new Vec2D(2, 3)), new RemoveStationMove(PLAYER, 10))));

        assertEqualMoves(new GrowCitiesMove(), new GrowCitiesMove());
    }

    /**
     * Performs transaction.
     * @param amount the amount to use in the test.
     * @return the result of transaction.
     */
    private static Transaction transaction(long amount) {
        return new Transaction(TransactionCategory.MISC_INCOME, new Money(amount), TIME);
    }

    /**
     * Performs transaction.
     * @param category the category to use in the test.
     * @param amount the amount to use in the test.
     * @param time the time to use in the test.
     * @return the result of transaction.
     */
    private static Transaction transaction(TransactionCategory category, long amount, Time time) {
        return new Transaction(category, new Money(amount), time);
    }

    /**
     * Performs cargo.
     * @param amount the amount to use in the test.
     * @return the result of cargo.
     */
    private static CargoBatchBundle cargo(int amount) {
        CargoBatchBundle bundle = new CargoBatchBundle();
        bundle.addCargo(new CargoBatch(1, Vec2D.ZERO, 42, 9), amount);
        return bundle;
    }

    /**
     * Performs station.
     * @param id the id to use in the test.
     * @param location the location to use in the test.
     * @param name the name to use in the test.
     * @return the result of station.
     */
    private static Station station(int id, Vec2D location, String name) {
        return new Station(id, location, name, CargoBatchBundle.EMPTY);
    }

    /**
     * Asserts equal moves.
     * @param first the first to use in the test.
     * @param independent the independent to use in the test.
     */
    private void assertEqualMoves(Move first, Move independent) {
        assertNotSame(first, independent);
        assertEquals(first, independent);
        assertEquals(independent, first);
        assertEquals(first.hashCode(), independent.hashCode());
    }

    /**
     * Asserts different moves.
     * @param first the first to use in the test.
     * @param different the different to use in the test.
     */
    private void assertDifferentMoves(Move first, Move different) {
        assertFalse("Expected unequal moves: " + first + " and " + different, first.equals(different));
        assertFalse("Move equality must be symmetric", different.equals(first));
    }
}

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

package freerails.move.generator;

import freerails.move.generator.TrackMoveGenerator;
import freerails.model.economy.TransactionUtils;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.model.player.Player;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackType;
import freerails.model.world.World;
import freerails.move.Move;
import freerails.move.Status;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.RemoveStationCompositeMove;
import freerails.util.IntPoint2D;
import freerails.util.TestUtils;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

/**
 * Tests that removing station track removes the station and the owner's schedule stops at it.
 */
public class RemoveStationMoveGeneratorTest extends TestCase {

    private static final IntPoint2D TRACK_START = new IntPoint2D(10, 10);
    private static final IntPoint2D STATION_LOCATION = TileTransition.EAST.createRelocatedPoint(TRACK_START);

    private final Player owner = WorldGenerator.TEST_PLAYER;
    private final Player other = new Player(1, "other player");
    private World world;
    private int stationId;

    /**
     * Builds one track segment for the owner and turns its end into a station.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = WorldGenerator.defaultWorld();
        world.addPlayer(owner);
        world.addPlayer(other);
        world.addTransaction(owner, TransactionUtils.issueBond(5, world.getClock().getCurrentTime()));

        TrackType trackType = world.getTrackTypes().stream()
                .filter(type -> !type.isStation())
                .findFirst()
                .orElseThrow();
        assertApplied(TrackMoveGenerator.generateBuildTrackMove(
                TRACK_START, TileTransition.EAST, trackType, trackType, world, owner));

        TrackType stationType = world.getTrackTypes().stream()
                .filter(TrackType::isStation)
                .findFirst()
                .orElseThrow();
        stationId = world.allocateStationId(owner);
        assertApplied(new AddStationMoveGenerator(STATION_LOCATION, stationType.getId(), owner).generate(world));
        assertEquals(stationId, world.getStations(owner).iterator().next().getId());
    }

    /**
     * Removing a station only updates schedules of trains owned by the station owner, even when
     * another player uses the same station and train identifiers.
     * @throws Exception if the track removal move cannot be generated.
     */
    public void testRemovingStationTrackOnlyUpdatesOwnersSchedules() throws Exception {
        int otherOnlyStationId = stationId + 5;
        world.addStation(other, new Station(stationId, new IntPoint2D(30, 30), "Other", CargoBatchBundle.EMPTY));
        world.addStation(other, new Station(otherOnlyStationId, new IntPoint2D(31, 30), "Other 2",
                CargoBatchBundle.EMPTY));

        int engineId = world.getEngines().iterator().next().getId();
        int trainId = 0;
        Train ownerTrain = new Train(trainId, engineId);
        ownerTrain.setSchedule(schedule(stationId, stationId + 1));
        world.addTrain(owner, ownerTrain);
        Train otherTrain = new Train(trainId, engineId);
        UnmodifiableSchedule otherSchedule = schedule(stationId, otherOnlyStationId);
        otherTrain.setSchedule(otherSchedule);
        world.addTrain(other, otherTrain);

        ChangeTrackPieceCompositeMove removal = TrackMoveGenerator.generateRemoveTrackMove(
                TRACK_START, TileTransition.EAST, world, owner);
        assertTrue(removal.getMove(1) instanceof RemoveStationCompositeMove);
        assertApplied(removal);

        assertNull(world.getTile(STATION_LOCATION).trackPiece());
        assertTrue(world.getStations(owner).isEmpty());
        assertEquals(schedule(stationId + 1), world.getTrain(owner, trainId).getSchedule());
        assertEquals(2, world.getStations(other).size());
        assertEquals(otherSchedule, world.getTrain(other, trainId).getSchedule());
    }

    /**
     * Removing track that would remove another player's station is refused with a clear reason.
     */
    public void testRemovingAnotherPlayersStationIsRefused() {
        IllegalStateException exception = TestUtils.assertThrows(IllegalStateException.class,
                () -> TrackMoveGenerator.generateRemoveTrackMove(TRACK_START, TileTransition.EAST, world, other));
        assertEquals("Not allowed to remove another player's station.", exception.getMessage());
        assertEquals(1, world.getStations(owner).size());
    }

    /**
     * The generator refuses a track change at a location without a station of the player.
     */
    public void testGenerateFailsWithoutStationAtLocation() {
        ChangeTrackPieceMove trackChange = new ChangeTrackPieceMove(
                world.getTile(TRACK_START).trackPiece(), null, TRACK_START);
        IllegalArgumentException exception = TestUtils.assertThrows(IllegalArgumentException.class,
                () -> new RemoveStationMoveGenerator(trackChange, owner).generate(world));
        assertEquals("Could not find a station at " + TRACK_START, exception.getMessage());
    }

    /**
     * Generators with the same track change and player are equal.
     */
    public void testEquality() {
        ChangeTrackPieceMove trackChange = new ChangeTrackPieceMove(
                world.getTile(STATION_LOCATION).trackPiece(), null, STATION_LOCATION);
        RemoveStationMoveGenerator generator = new RemoveStationMoveGenerator(trackChange, owner);
        assertEquals(generator, new RemoveStationMoveGenerator(trackChange, owner));
        assertEquals(generator.hashCode(), new RemoveStationMoveGenerator(trackChange, owner).hashCode());
        assertFalse(generator.equals(new RemoveStationMoveGenerator(trackChange, other)));
    }

    /**
     * Creates a schedule that stops at the given stations in order.
     * @param stationIds the station identifiers
     * @return the schedule
     */
    private static UnmodifiableSchedule schedule(int... stationIds) {
        ScheduleState schedule = new ScheduleState();
        for (int id : stationIds) {
            schedule = schedule.addOrder(TrainOrder.autoConsist(id));
        }
        return schedule;
    }

    /**
     * Asserts that a move is applicable and applies it.
     * @param move the move
     */
    private void assertApplied(Move move) {
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
        move.apply(world);
    }
}

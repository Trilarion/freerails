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

package freerails.move.mapupdatemove;

import freerails.move.generator.TrackMoveGenerator;
import freerails.model.economy.TransactionUtils;
import freerails.model.game.Rules;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.model.world.World;
import freerails.move.Move;
import freerails.move.RemoveStationMove;
import freerails.move.Status;
import freerails.move.UpdateTrainMove;
import freerails.move.generator.AddStationMoveGenerator;
import freerails.move.generator.RemoveStationMoveGenerator;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.List;

/**
 * Tests that station track is only removed together with its station.
 */
public class RemoveStationCompositeMoveTest extends TestCase {

    private static final IntPoint2D TRACK_START = new IntPoint2D(10, 10);
    private static final IntPoint2D STATION_LOCATION = TileTransition.EAST.createRelocatedPoint(TRACK_START);
    private static final String INCOMPLETE_SCHEDULES =
            "A station removal must remove the station from every schedule that stops at it.";

    private final Player owner = WorldGenerator.TEST_PLAYER;
    private final Player other = new Player(1, "other player");
    private World world;
    private int stationId;
    private int trainId;
    private ChangeTrackPieceMove stationTrackRemoval;

    /**
     * Creates the default fixture, in which other players' track may not be removed.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createWorld(new Rules(true, false, false));
    }

    /**
     * Builds one track segment for the owner, turns its end into a station, and adds
     * an owner train that stops at the station.
     * @param rules the game rules
     */
    private void createWorld(Rules rules) {
        world = WorldGenerator.defaultWorld(rules);
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

        int engineId = world.getEngines().iterator().next().getId();
        trainId = world.allocateTrainId(owner);
        Train train = new Train(trainId, engineId);
        train.setSchedule(schedule(stationId, stationId + 1));
        world.addTrain(owner, train);

        stationTrackRemoval = new ChangeTrackPieceMove(owner,
                world.getTile(STATION_LOCATION).trackPiece(), null, STATION_LOCATION);
    }

    /**
     * A plain track change must not remove station track.
     */
    public void testPlainTrackChangeCannotRemoveStationTrack() {
        assertRefused(stationTrackRemoval, "Station track can only be removed together with its station.");
    }

    /**
     * A pair of plain track changes must not remove station track either.
     */
    public void testPlainTrackPairCannotRemoveStationTrack() {
        ChangeTrackPieceMove trackStartRemoval = new ChangeTrackPieceMove(owner,
                world.getTile(TRACK_START).trackPiece(), null, TRACK_START);
        assertRefused(new ChangeTrackPieceCompositeMove(trackStartRemoval, stationTrackRemoval, owner),
                "Station track can only be removed together with its station.");
    }

    /**
     * The generated station removal removes track, station, and schedule stops.
     */
    public void testGeneratedStationRemovalIsApplied() {
        Move removal = new RemoveStationMoveGenerator(stationTrackRemoval, owner).generate(world);
        assertApplicable(removal);
        removal.apply(world);

        assertNull(world.getTile(STATION_LOCATION).trackPiece());
        assertTrue(world.getStations(owner).isEmpty());
        assertEquals(schedule(stationId + 1), world.getTrain(owner, trainId).getSchedule());
    }

    /**
     * A station removal must update every schedule that stops at the station.
     */
    public void testMissingScheduleUpdateIsRefused() {
        assertRefused(new RemoveStationCompositeMove(
                stationTrackRemoval, new RemoveStationMove(owner, stationId), List.of()), INCOMPLETE_SCHEDULES);
    }

    /**
     * A station removal must set the schedule without the removed station.
     */
    public void testWrongScheduleUpdateIsRefused() {
        UpdateTrainMove wrongUpdate = new UpdateTrainMove(owner, trainId, null, null, schedule(stationId + 2));
        assertRefused(new RemoveStationCompositeMove(
                stationTrackRemoval, new RemoveStationMove(owner, stationId), List.of(wrongUpdate)),
                INCOMPLETE_SCHEDULES);
    }

    /**
     * A station removal must not change more than the schedules.
     */
    public void testScheduleUpdateWithOtherChangesIsRefused() {
        UpdateTrainMove update = new UpdateTrainMove(owner, trainId, null, List.of(), schedule(stationId + 1));
        assertRefused(new RemoveStationCompositeMove(
                stationTrackRemoval, new RemoveStationMove(owner, stationId), List.of(update)),
                "A station removal may only update the schedules of the owner's trains.");
    }

    /**
     * A station removal must remove the station at the removed station track.
     */
    public void testWrongStationIsRefused() {
        int otherStationId = stationId + 1;
        world.addStation(owner, new Station(otherStationId, new IntPoint2D(30, 30), "Elsewhere",
                CargoBatchBundle.EMPTY));
        assertRefused(new RemoveStationCompositeMove(
                stationTrackRemoval, new RemoveStationMove(owner, otherStationId), List.of(scheduleUpdate())),
                "The removed station must be at the removed station track.");
    }

    /**
     * A station removal must remove the station of the track owner.
     */
    public void testOtherPlayersStationIsRefused() {
        world.addStation(other, new Station(stationId, STATION_LOCATION, "Other", CargoBatchBundle.EMPTY));
        assertRefused(new RemoveStationCompositeMove(
                stationTrackRemoval, new RemoveStationMove(other, stationId), List.of()),
                "The removed station must belong to the owner of the station track.");
    }

    /**
     * Another player may not remove a station when other players' track may not be removed.
     */
    public void testOtherPlayerCannotRemoveStationWhenRulesForbidIt() {
        assertRefused(stationRemovalBy(other), "Only the owner may remove a station.");
    }

    /**
     * Another player may not remove a station even when other players' track may be removed.
     */
    public void testOtherPlayerCannotRemoveStationWhenRulesAllowRemovingTrack() {
        createWorld(new Rules(true, false, true));
        assertRefused(stationRemovalBy(other), "Only the owner may remove a station.");
    }

    /**
     * Another player may not build a station on the owner's track, even when
     * connecting to other players' track is allowed.
     */
    public void testOtherPlayerCannotBuildStationOnTrack() {
        TrackType stationType = world.getTrackTypes().stream().filter(TrackType::isStation).findFirst().orElseThrow();
        Move addStation = new AddStationMoveGenerator(TRACK_START, stationType.getId(), other).generate(world);

        Status status = addStation.applicable(world);
        assertEquals("Not allowed to change the track type of another player's track.", status.getMessage());
        assertEquals(owner.getId(), world.getTile(TRACK_START).trackPiece().playerId());
    }

    /**
     * Builds the owner's track and station removal, made by the supplied player.
     * @param player the player submitting the removal
     * @return the track removal move
     */
    private ChangeTrackPieceCompositeMove stationRemovalBy(Player player) {
        // The submitting player owns the other tile, so only the station half is someone else's track.
        TerrainTile start = world.getTile(TRACK_START);
        TrackPiece startTrack = start.trackPiece();
        world.setTile(TRACK_START, new TerrainTile(start.terrainTypeId(),
                new TrackPiece(startTrack.trackConfiguration(), startTrack.trackType(), player.getId())));
        ChangeTrackPieceMove trackStartRemoval = new ChangeTrackPieceMove(player,
                world.getTile(TRACK_START).trackPiece(), null, TRACK_START);
        ChangeTrackPieceMove stationChange = new ChangeTrackPieceMove(player,
                stationTrackRemoval.trackPieceBefore(), null, STATION_LOCATION);
        RemoveStationCompositeMove stationRemoval =
                new RemoveStationMoveGenerator(stationChange, owner).generate(world);
        return new ChangeTrackPieceCompositeMove(trackStartRemoval, stationRemoval, player);
    }

    /**
     * @return the correct schedule update of the owner's train
     */
    private UpdateTrainMove scheduleUpdate() {
        return new UpdateTrainMove(owner, trainId, null, null,
                new ScheduleState(world.getTrain(owner, trainId).getSchedule()).removeAllStopsAtStation(stationId));
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
     * Asserts that a move is applicable.
     * @param move the move
     */
    private void assertApplicable(Move move) {
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
    }

    /**
     * Asserts that a set up move is applicable and applies it.
     * @param move the move
     */
    private void assertApplied(Move move) {
        assertApplicable(move);
        move.apply(world);
    }

    /**
     * Asserts that a move is refused with the supplied reason and leaves the station in place.
     * @param move the move
     * @param message the expected reason
     */
    private void assertRefused(Move move, String message) {
        Status status = move.applicable(world);
        assertFalse(status.isSuccess());
        assertEquals(message, status.getMessage());
        assertTrue(world.getStations(owner).stream().anyMatch(station -> station.getId() == stationId));
    }
}

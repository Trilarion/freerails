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

package freerails.client;

import freerails.controller.StationBuilder;
import freerails.controller.TrackMoveProducer;
import freerails.model.MapFixtureFactory2;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.CargoDeliveryTransaction;
import freerails.model.operations.cargo.CargoBatch;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.station.Station;
import freerails.model.operations.station.StationDemand;
import freerails.model.operations.train.TrainState;
import freerails.model.operations.train.motion.MotionUtils;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.player.Player;
import freerails.model.simulation.Time;
import freerails.model.terrain.TileTransition;
import freerails.model.track.OccupiedTracks;
import freerails.model.world.World;
import freerails.move.*;
import freerails.move.generator.AddTrainMoveGenerator;
import freerails.move.generator.MoveTrainMoveGenerator;
import freerails.util.IntPoint2D;
import freerails.util.Utils;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Tests the messages and sounds generated for cargo deliveries and trains leaving stations.
 * <p>
 * The world has track from (10, 10) to (30, 10), stations 0, 1 and 2 at x = 10, 20 and 28, and
 * train 0 with two wagons for cargo 0 that starts at station 0 and is scheduled to station 2.
 */
public class UserMessageGeneratorTest extends TestCase {

    private static final int STATION_1_X = 20;
    private static final int STATION_2_X = 28;

    private World world;
    private Player player;
    private ModelRootImpl modelRoot;
    private UserMessageGenerator generator;
    private final List<String> sounds = new ArrayList<>();

    /**
     * Builds the track, the stations and the train, and the generator under test.
     *
     * @throws Exception if the setup fails
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, world.getPlayer(0));
        player = moveExecutor.getPlayer();
        modelRoot = TestModelRoots.create(world, player);

        // Build the track and the stations.
        TrackMoveProducer trackBuilder = new TrackMoveProducer(moveExecutor, modelRoot.getProperties());
        StationBuilder stationBuilder = new StationBuilder(moveExecutor);
        stationBuilder.setStationType(stationBuilder.getTrackTypeID("terminal"));
        TileTransition[] track = new TileTransition[20];
        Arrays.fill(track, TileTransition.EAST);
        assertTrue(trackBuilder.buildTrack(new IntPoint2D(10, 10), track).isSuccess());
        assertTrue(stationBuilder.buildStation(new IntPoint2D(10, 10)).isSuccess());
        assertTrue(stationBuilder.buildStation(new IntPoint2D(STATION_1_X, 10)).isSuccess());
        assertTrue(stationBuilder.buildStation(new IntPoint2D(STATION_2_X, 10)).isSuccess());

        // Build the train.
        ScheduleState schedule = new ScheduleState()
                .addOrder(TrainOrder.keepConsist(2))
                .addOrder(TrainOrder.keepConsist(0));
        int engineId = world.getEngines().iterator().next().getId();
        Move addTrain = new AddTrainMoveGenerator(engineId, Arrays.asList(0, 0), new IntPoint2D(10, 10), player,
                schedule).generate(world);
        assertTrue(addTrain.applicable(world).isSuccess());
        addTrain.apply(world);

        generator = new UserMessageGenerator(modelRoot, (sound, loops) -> sounds.add(sound));
    }

    /**
     * Tests that the whistle plays once, when the train leaves the station it stopped at, and not
     * while it arrives, stops, or keeps moving.
     */
    public void testWhistlesWhenTrainLeavesStation() {
        TrainMotion motion = moveTrainUntil(STATION_1_X);
        assertEquals(TrainState.READY, motion.getTrainState());
        sounds.clear();

        motion = moveTrain();
        assertEquals(TrainState.STOPPED_AT_STATION, motion.getTrainState());
        assertEquals(List.of(), sounds);

        motion = moveTrain();
        assertEquals(STATION_1_X + 1, motion.getFinalPosition().getLocation().x());
        assertEquals(List.of(ClientConstants.SOUND_WHISTLE), sounds);

        moveTrain();
        assertEquals(List.of(ClientConstants.SOUND_WHISTLE), sounds);
    }

    /**
     * Tests that a delivery made by the simulation shows the train, the station, the cargo and the
     * revenue, and plays the cash register sound.
     */
    public void testDeliveryAtStationShowsMessageAndPlaysCashSound() {
        // Station 1 has cargo for the train, and station 2 demands it.
        CargoBatchBundle cargo = new CargoBatchBundle();
        cargo.addCargo(new CargoBatch(0, new IntPoint2D(6, 6), new Time(0), 1), 800);
        world.getStation(player, 1).setCargoBatchBundle(cargo);
        Station station2 = (Station) Utils.cloneBySerialisation(world.getStation(player, 2));
        station2.setDemandForCargo(new StationDemand(Set.of(0)));
        world.removeStation(player, 2);
        world.addStation(player, station2);

        moveTrainUntil(STATION_2_X);
        sounds.clear();
        TrainMotion motion = moveTrain();
        assertEquals(TrainState.STOPPED_AT_STATION, motion.getTrainState());

        String message = (String) modelRoot.getProperties().getProperty(ModelRootProperty.QUICK_MESSAGE);
        String[] lines = message.split("\n");
        assertEquals("Train 0 delivers at " + station2.getStationName(), lines[0]);
        assertEquals("80 " + world.getCargo(0).getName(), lines[1]);
        assertTrue(lines[2], lines[2].startsWith("Revenue "));
        assertEquals(List.of(ClientConstants.SOUND_CASH), sounds);
    }

    /**
     * Tests that deliveries of the same cargo by one train at one station are added up into one line.
     */
    public void testAddsUpDeliveriesOfSameCargo() {
        Move move = new CompositeMove(List.of(
                new CompostMove(List.of(delivery(player, 10, 100), delivery(player, 20, 200)))));

        generator.process(move);

        String message = (String) modelRoot.getProperties().getProperty(ModelRootProperty.QUICK_MESSAGE);
        String[] lines = message.split("\n");
        assertEquals("Train 0 delivers at " + world.getStation(player, 2).getStationName(), lines[0]);
        assertEquals("30 " + world.getCargo(0).getName(), lines[1]);
        assertEquals("Revenue " + MoneyFormatter.format(new Money(300)), lines[2]);
        assertEquals(3, lines.length);
        assertEquals(List.of(ClientConstants.SOUND_CASH), sounds);
    }

    /**
     * Tests that deliveries by another player's train produce neither a message nor a sound.
     */
    public void testIgnoresDeliveriesOfOtherPlayers() {
        Player other = world.getPlayer(1);
        generator.process(new CompostMove(List.of(delivery(other, 10, 100), delivery(other, 20, 200))));

        assertNull(modelRoot.getProperties().getProperty(ModelRootProperty.QUICK_MESSAGE));
        assertEquals(List.of(), sounds);
    }

    /**
     * Creates a move that pays for a delivery of cargo 0 by train 0 at station 2.
     *
     * @param owner the player who is paid
     * @param quantity the delivered quantity
     * @param revenue the payment
     * @return the move
     */
    private Move delivery(Player owner, int quantity, long revenue) {
        CargoBatch batch = new CargoBatch(0, new IntPoint2D(6, 6), new Time(0), 1);
        CargoDeliveryTransaction transaction = new CargoDeliveryTransaction(new Money(revenue),
                world.getClock().getCurrentTime(), quantity, 2, 0, batch);
        return new AddTransactionMove(owner, transaction);
    }

    /**
     * Moves the train until it reaches the given x coordinate.
     *
     * @param x the x coordinate to reach
     * @return the train's latest motion
     */
    private TrainMotion moveTrainUntil(int x) {
        TrainMotion motion;
        do {
            motion = moveTrain();
        } while (motion.getFinalPosition().getLocation().x() < x);
        assertEquals(x, motion.getFinalPosition().getLocation().x());
        return motion;
    }

    /**
     * Advances the clock until the train's next update is due, generates and applies that update
     * as the server would, and passes it to the generator as the client would.
     *
     * @return the train's latest motion
     */
    private TrainMotion moveTrain() {
        TrainMotion last = MotionUtils.lastMotion(world, player, 0);
        Time finishTime = new Time((int) Math.floor(last.getStartTime() + last.getDuration()));
        while (world.getClock().getCurrentTime().compareTo(finishTime) < 0) {
            world.getClock().advanceTime();
        }

        Move move = new MoveTrainMoveGenerator(0, player, new OccupiedTracks(player, world)).generate(world);
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
        move.apply(world);
        generator.process(move);
        return world.getTrain(player, 0).findCurrentMotion(Integer.MAX_VALUE);
    }
}

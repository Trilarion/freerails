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

import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.player.Player;
import freerails.model.world.World;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

/**
 * Tests that a schedule change only changes the schedule of the player's own
 * train, with stops at the player's own stations.
 */
public class ChangeTrainScheduleMoveTest extends TestCase {

    private final Player owner = WorldGenerator.TEST_PLAYER;
    private final Player other = new Player(1, "other player");
    private World world;
    private int trainId;
    private int ownStation;
    private int othersStation;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = WorldGenerator.defaultWorld();
        world.addPlayer(owner);
        world.addPlayer(other);
        ownStation = world.allocateStationId(owner);
        world.addStation(owner, new Station(ownStation, new IntPoint2D(5, 5), "Own", CargoBatchBundle.EMPTY));
        othersStation = world.allocateStationId(other) + 100;
        world.addStation(other, new Station(othersStation, new IntPoint2D(8, 8), "Other", CargoBatchBundle.EMPTY));
        trainId = world.allocateTrainId(owner);
        world.addTrain(owner, new Train(trainId, world.getEngines().iterator().next().getId()));
    }

    /**
     * Tests that a schedule with the owner's stations is applied.
     */
    public void testOwnStationsAreAccepted() {
        ScheduleState schedule = new ScheduleState().addOrder(TrainOrder.autoConsist(ownStation));
        Move move = new ChangeTrainScheduleMove(owner, trainId, schedule);

        assertTrue(move.applicable(world).isSuccess());
        move.apply(world);
        assertEquals(schedule, new ScheduleState(world.getTrain(owner, trainId).getSchedule()));
    }

    /**
     * Tests that stops at other players' stations and other players' trains are refused.
     */
    public void testOtherPlayersStationsAndTrainsAreRefused() {
        ScheduleState toOthers = new ScheduleState().addOrder(TrainOrder.autoConsist(othersStation));
        assertEquals("A train may only stop at its owner's stations.",
                new ChangeTrainScheduleMove(owner, trainId, toOthers).applicable(world).getMessage());

        ScheduleState own = new ScheduleState().addOrder(TrainOrder.autoConsist(ownStation));
        assertFalse(new ChangeTrainScheduleMove(other, trainId, own).applicable(world).isSuccess());
    }
}

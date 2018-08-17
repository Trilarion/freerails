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
package freerails.controller;

import freerails.model.player.Player;
import freerails.model.train.Train;
import freerails.model.train.schedule.Schedule;
import freerails.move.generator.DropOffAndPickupCargoMoveGenerator;
import freerails.move.Move;
import freerails.move.Status;

import freerails.util.Vec2D;
import freerails.model.world.World;
import freerails.model.cargo.*;
import freerails.model.station.StationDemand;
import freerails.model.station.Station;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This Junit TestCase tests whether a train picks up and drops off the right
 * cargo at a station.
 */
public class DropOffAndPickupCargoMoveGeneratorTest extends TestCase {

    private final CargoBatch cargoType0FromStation2 = new CargoBatch(0, Vec2D.ZERO,0, 2);
    private final CargoBatch cargoType1FromStation2 = new CargoBatch(1, Vec2D.ZERO,0, 2);
    private final CargoBatch cargoType0FromStation0 = new CargoBatch(0,new Vec2D(0,0),0, 0);
    private World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Set up the world object with three cargo types, one station, and one train.
        world = WorldGenerator.defaultWorld();
        world.addPlayer(WorldGenerator.TEST_PLAYER);

        // Set up station
        int x = 10;
        int y = 10;
        String stationName = "Station 1";
        Station station = new Station(0, new Vec2D(x, y), stationName, world.getCargos().size(), CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE);
        world.addStation(WorldGenerator.TEST_PLAYER, station);

        // Set up train: 3 wagons to carry cargo type 0.
        List<Integer> wagons = Arrays.asList(0, 0, 0);
        Train train = new Train(0, 0, wagons, CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, new Schedule());
        world.addTrain(WorldGenerator.TEST_PLAYER, train);
    }

    /**
     * Tests picking up cargo from a station.
     */
    public void testPickUpCargo1() {
        // Set up the variables for this test.
        CargoBatchBundle cargoBundleWith2CarloadsOfCargo0 = new CargoBatchBundle();

        // cargoBundleWith2CarloadsOfCargo0.setAmount(cargoType0FromStation2, 2);
        cargoBundleWith2CarloadsOfCargo0.setAmount(cargoType0FromStation2, 80);

        assertEquals("There shouldn't be any cargo at the station yet",
                CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, getCargoAtStation());
        assertEquals("There shouldn't be any cargo on the train yet",
                CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, getCargoOnTrain());

        // Now add 2 carloads of cargo type 0 to the station.
        // getCargoAtStation().setAmount(cargoType0FromStation2, 2);
        setCargoAtStation(cargoType0FromStation2, 80);

        // The train should pick up this cargo, since it has three wagons
        // capable of carrying cargo type 0.
        stopAtStation();

        // The train should now have the two car loads of cargo and there should
        // be no cargo at the station.
        assertEquals("There should no longer be any cargo at the station",
                CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, getCargoAtStation());
        assertEquals("The train should now have the two car loads of cargo",
                cargoBundleWith2CarloadsOfCargo0, getCargoOnTrain());
    }

    /**
     * Tests picking up cargo when the there is too much cargo at the station
     * for the train to carry.
     */
    public void testPickUpCargo2() {
        setCargoAtStation(this.cargoType0FromStation2, 200);

        stopAtStation();

        // The train has 3 wagons, each wagon carries 40 units of cargo, so
        // the train should pickup 120 units of cargo.
        CargoBatchBundle expectedOnTrain = new CargoBatchBundle();
        expectedOnTrain.setAmount(this.cargoType0FromStation2, 120);

        // The remaining 80 units of cargo should be left at the station.
        CargoBatchBundle expectedAtStation = new CargoBatchBundle();
        expectedAtStation.setAmount(this.cargoType0FromStation2, 80);

        // Test the expected values against the actual values.
        assertEquals(expectedOnTrain, getCargoOnTrain());
        assertEquals(expectedAtStation, getCargoAtStation());
    }

    /**
     * Tests that a train takes into account how much cargo it already has and
     * the type of wagons it has when it is picking up cargo.
     */
    public void testPickUpCargo3() {
        List<Integer> wagons = Arrays.asList(0, 0, 2, 2);

        // 2 wagons for cargo type 0; 2 wagons for cargo type 2.
        setWagons(wagons);

        // Set cargo on train.
        setCargoOnTrain(this.cargoType0FromStation2, 30);

        // Set cargo at station.
        setCargoAtStation(this.cargoType0FromStation0, 110);

        // Check that station does not demand cargo type 0.
        Station station = world.getStation(WorldGenerator.TEST_PLAYER, 0);
        assertFalse(station.getDemandForCargo().isCargoDemanded(0));

        // Stop at station.
        stopAtStation();

        /*
         * The train has 2 wagons for cargo type 0 but had 30 units of cargo
         * type 0 before stopping so it can only pick up 50 units.
         */
        CargoBatchBundle expectedAtStation = new CargoBatchBundle();
        expectedAtStation.setAmount(cargoType0FromStation0, 60);

        CargoBatchBundle expectedOnTrain = new CargoBatchBundle();
        expectedOnTrain.setAmount(this.cargoType0FromStation2, 30);
        expectedOnTrain.setAmount(this.cargoType0FromStation0, 50);

        assertEquals(expectedAtStation, getCargoAtStation());
        assertEquals(expectedOnTrain, getCargoOnTrain());
    }

    /**
     * Tests that a train drops of cargo that a station demands and does not
     * drop off cargo that is not demanded unless it has to.
     */
    public void testDropOffCargo() {
        // Set the station to demand cargo type 0.
        Station station = world.getStation(WorldGenerator.TEST_PLAYER, 0);
        StationDemand demand = new StationDemand(new boolean[]{true, false, false, false});
        station.setDemandForCargo(demand);
        world.removeStation(WorldGenerator.TEST_PLAYER, station.getId());
        world.addStation(WorldGenerator.TEST_PLAYER, station);

        // Check that the station demands what we think it does.
        assertTrue("The station should demand cargo type 0.", station
                .getDemandForCargo().isCargoDemanded(0));
        assertFalse("The station shouldn't demand cargo type 1.", station
                .getDemandForCargo().isCargoDemanded(1));

        // Add 2 wagons for cargo type 0 and 1 for cargo type 1 to train.
        List<Integer> wagons = Arrays.asList(0, 0, 1, 1);
        setWagons(wagons);

        // Add quantities of cargo type 0 and 2 to the train.
        setCargoOnTrain(this.cargoType0FromStation2, 50);
        setCargoOnTrain(this.cargoType1FromStation2, 40);

        stopAtStation();

        /*
         * The train should have dropped of the 50 units cargo of type 0 since
         * the station demands it but not the 40 units of cargo type 1 which is
         * does not demand.
         */
        CargoBatchBundle expectedOnTrain = new CargoBatchBundle();
        expectedOnTrain.setAmount(this.cargoType1FromStation2, 40);

        assertEquals(expectedOnTrain, getCargoOnTrain());
        assertEquals(CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, getCargoAtStation());

        // Now remove the wagons from the train.
        removeAllWagonsFromTrain();
        stopAtStation();

        /*
         * This time the train has no wagons, so has to drop the 40 units of
         * cargo type 1 even though the station does not demand it. Since ths
         * station does not demand it, it is added to the cargo waiting at the
         * station.
         */
        CargoBatchBundle expectedAtStation = new CargoBatchBundle();
        expectedAtStation.setAmount(this.cargoType1FromStation2, 40);

        assertEquals(expectedAtStation, getCargoAtStation());
        assertEquals(CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, getCargoOnTrain());
    }

    /**
     * Tests that a train does not drop cargo off at its station of origin
     * unless it has to.
     */
    public void testDontDropOffCargo() {
        // Set station to
        setCargoOnTrain(cargoType0FromStation0, 50);
        setCargoOnTrain(cargoType0FromStation2, 50);

        stopAtStation();

        // The train shouldn't have dropped anything off.
        CargoBatchBundle expectedOnTrain = new CargoBatchBundle();
        expectedOnTrain.setAmount(cargoType0FromStation0, 50);
        expectedOnTrain.setAmount(cargoType0FromStation2, 50);

        assertEquals(expectedOnTrain, getCargoOnTrain());
        assertEquals(CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, getCargoAtStation());

        // Now remove the wagons from the train.
        removeAllWagonsFromTrain();

        stopAtStation();

        /*
         * The train now has no wagons, so must drop off the cargo whether the
         * station demands it or not. Since the station does not demand it, the
         * cargo should get added to the cargo waiting at the station.
         */
        CargoBatchBundle expectedAtStation = new CargoBatchBundle();
        expectedAtStation.setAmount(cargoType0FromStation0, 50);
        expectedAtStation.setAmount(cargoType0FromStation2, 50);

        assertEquals(expectedAtStation, getCargoAtStation());
        assertEquals(CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, getCargoOnTrain());
    }

    /**
     * Tests that a train drops off any cargo before picking up cargo.
     */
    public void testPickUpAndDropOffSameCargoType() {
        // Set cargo at station and on train.
        setCargoOnTrain(this.cargoType0FromStation2, 120);
        setCargoAtStation(this.cargoType0FromStation0, 200);

        // Set station to demand cargo 0.
        Station station = world.getStation(WorldGenerator.TEST_PLAYER, 0);
        StationDemand demand = new StationDemand(new boolean[]{true, false, false, false});
        station.setDemandForCargo(demand);

        // TODO if this is the same station the removal and addition is meaningless
        world.removeStation(WorldGenerator.TEST_PLAYER, station.getId());
        world.addStation(WorldGenerator.TEST_PLAYER, station);

        assertTrue(station.getDemandForCargo().isCargoDemanded(0));
        stopAtStation();

        CargoBatchBundle expectedOnTrain = new CargoBatchBundle();
        expectedOnTrain.setAmount(this.cargoType0FromStation0, 120);

        CargoBatchBundle expectedAtStation = new CargoBatchBundle();
        expectedAtStation.setAmount(this.cargoType0FromStation0, 80);

        assertEquals(expectedOnTrain, getCargoOnTrain());
        assertEquals(expectedAtStation, getCargoAtStation());
    }

    private void removeAllWagonsFromTrain() {
        setWagons(new ArrayList<>());
    }

    private void setWagons(List<Integer> wagons) {
        Train train = world.getTrain(WorldGenerator.TEST_PLAYER, 0);
        Train newTrain = new Train(train.getId(), train.getEngineId(), wagons, train.getCargoBatchBundle(), train.getSchedule());
        world.removeTrain(WorldGenerator.TEST_PLAYER, 0);
        world.addTrain(WorldGenerator.TEST_PLAYER, newTrain);
    }

    private void stopAtStation() {
        DropOffAndPickupCargoMoveGenerator moveGenerator = new DropOffAndPickupCargoMoveGenerator(
                0, 0, world, WorldGenerator.TEST_PLAYER, false, false);
        Move move = moveGenerator.generate();
        if (null != move) {
            Status status = move.doMove(world, Player.AUTHORITATIVE);
            assertEquals(Status.OK, status);
        }
    }

    /**
     * Retrieves the cargo bundle that is waiting at the station from the world
     * object.
     */
    private UnmodifiableCargoBatchBundle getCargoAtStation() {
        Station station = world.getStation(WorldGenerator.TEST_PLAYER, 0);
        return station.getCargoBatchBundle();
    }

    /**
     * Retrieves the cargo bundle that the train is carrying from the world
     * object.
     */
    private UnmodifiableCargoBatchBundle getCargoOnTrain() {
        Train train = world.getTrain(WorldGenerator.TEST_PLAYER, 0);
        return train.getCargoBatchBundle();
    }

    private void setCargoAtStation(CargoBatch cargoBatch, int amount) {
        Station station = world.getStation(WorldGenerator.TEST_PLAYER, 0);
        CargoBatchBundle cargoBatchBundle = new CargoBatchBundle(getCargoAtStation());
        cargoBatchBundle.setAmount(cargoBatch, amount);
        station.setCargoBatchBundle(cargoBatchBundle);
    }

    private void setCargoOnTrain(CargoBatch cargoBatch, int amount) {
        Train train = world.getTrain(WorldGenerator.TEST_PLAYER, 0);
        CargoBatchBundle cargoBatchBundle = new CargoBatchBundle(getCargoOnTrain());
        cargoBatchBundle.setAmount(cargoBatch, amount);
        train.setCargoBatchBundle(cargoBatchBundle);
    }
}
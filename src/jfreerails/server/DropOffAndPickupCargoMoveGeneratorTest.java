/*
 * Created on 28-Jun-2003
 *
 */
package jfreerails.server;

import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.train.TrainModel;
import junit.framework.TestCase;

/**
 * @author Luke Lindsay
 *
 */
public class DropOffAndPickupCargoMoveGeneratorTest extends TestCase {

	private World w;

	protected void setUp() throws Exception {
		//Set up the world object with three cargo types, one station, and one train.		
		w = new WorldImpl();

		//set up the cargo types.
		w.add(KEY.CARGO_TYPES, new CargoType(0, "Mail", "Mail"));
		w.add(KEY.CARGO_TYPES, new CargoType(0, "Passengers", "Passengers"));
		w.add(KEY.CARGO_TYPES, new CargoType(0, "Goods", "Goods"));

		//Set up station
		int x = 10;
		int y = 10;
		int stationCargoBundleId =
			w.add(KEY.CARGO_BUNDLES, new CargoBundleImpl());
		String stationName = "Station 1";
		StationModel station =
			new StationModel(
				x,
				y,
				stationName,
				w.size(KEY.CARGO_TYPES),
				stationCargoBundleId);
		w.add(KEY.STATIONS, station);

		//Set up train
		int trainCargoBundleId =
			w.add(KEY.CARGO_BUNDLES, new CargoBundleImpl());
		
		//3 wagons to carry cargo type 0.
		int[] wagons = new int[] { 0, 0, 0 };			
		TrainModel train = new TrainModel(wagons, trainCargoBundleId);
		w.add(KEY.TRAINS, train);
	}

	public void testGenerateMove() {

		//Set up the variables for this test.
		CargoBatch type0CargoBatch = new CargoBatch(0, 0, 0, 0, 0);
		CargoBundle emptyCargoBundle = new CargoBundleImpl();
		CargoBundle cargoBundleWith2CarloadsOfCargo0 = new CargoBundleImpl();
		cargoBundleWith2CarloadsOfCargo0.setAmount(type0CargoBatch, 2);

		//Get the station and train from the world object.
		StationModel station = (StationModel) w.get(KEY.STATIONS, 0);
		TrainModel train = (TrainModel) w.get(KEY.TRAINS, 0);
		CargoBundle cargoAtStation =
			(CargoBundle) w.get(
				KEY.CARGO_BUNDLES,
				station.getCargoBundleNumber());
		CargoBundle cargoOnTrain =
			(CargoBundle) w.get(
				KEY.CARGO_BUNDLES,
				train.getCargoBundleNumber());

		assertEquals(
			"There shouldn't be any cargo at the station yet",
			emptyCargoBundle,
			cargoAtStation);
		assertEquals(
			"There shouldn't be any cargo on the train yet",
			emptyCargoBundle,
			cargoOnTrain);

		//Now add 2 carloads of cargo type 0 to the station.				
		cargoAtStation.setAmount(type0CargoBatch, 2);

		//The train should pick up this cargo, since it has three wagons capable of carrying cargo type 0.
		DropOffAndPickupCargoMoveGenerator moveGenerator =
			new DropOffAndPickupCargoMoveGenerator(0, 0, w);
		Move m = moveGenerator.generateMove();
		MoveStatus ms = m.doMove(w);
		assertTrue(ms.isOk());

		//The train should now have the two car loads of cargo and there should be no cargo at the station.
		cargoAtStation =
			(CargoBundle) w.get(
				KEY.CARGO_BUNDLES,
				station.getCargoBundleNumber());
		cargoOnTrain =
			(CargoBundle) w.get(
				KEY.CARGO_BUNDLES,
				train.getCargoBundleNumber());
		System.out.println(cargoAtStation);
		System.out.println(cargoOnTrain);
		assertEquals(
			"There should no longer be any cargo at the station",
			emptyCargoBundle,
			cargoAtStation);
		assertEquals(
			"The train should now have the two car loads of cargo",
			cargoBundleWith2CarloadsOfCargo0,
			cargoOnTrain);

	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		junit.framework.TestSuite testSuite =
			new junit.framework.TestSuite(
				DropOffAndPickupCargoMoveGeneratorTest.class);
		return testSuite;
	}

}

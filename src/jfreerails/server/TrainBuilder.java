package jfreerails.server;

import java.awt.Point;

import jfreerails.controller.MoveExecuter;
import jfreerails.controller.TrainMover;
import jfreerails.controller.pathfinder.FlatTrackExplorer;
import jfreerails.move.AddTrainMove;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackRule;
import jfreerails.world.train.EngineType;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
import jfreerails.world.train.TrainPathIterator;

/**
 * @author Luke Lindsay 13-Oct-2002
 *
 */
public class TrainBuilder {

	private World world;
	private ServerGameEngine gameEngine;
	private int trainId;

	public TrainBuilder(World w, ServerGameEngine gm) {
		this.world = w;
		this.gameEngine = gm;
	}

	public void buildTrain(int engineTypeNumber, int[] wagons, Point p) {

		FreerailsTile tile = (FreerailsTile) world.getTile(p.x, p.y);

		TrackRule tr = tile.getTrackRule();

		if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != tr.getRuleNumber()) {

			//Add train to train list.
			WorldIterator wi = new NonNullElements(KEY.STATIONS, world);

			MutableSchedule s = new MutableSchedule();
			

			//Add upto 4 stations to the schedule.
			while (wi.next() && s.getNumOrders() < 5) {
				TrainOrdersModel orders =
					new TrainOrdersModel(wi.getIndex(), null, false);
				s.addOrder(orders);
			}
			s.setOrderToGoto(0);

			CargoBundle cb = new CargoBundleImpl();
			world.add(KEY.CARGO_BUNDLES, cb);
			int cargoBundleNumber = world.size(KEY.CARGO_BUNDLES) - 1;
			int scheduleNumber = world.size(KEY.TRAIN_SCHEDULES);

			TrainModel train =
				new TrainModel(
					engineTypeNumber,
					wagons,
					null,
					scheduleNumber,
					cargoBundleNumber);

			EngineType engineType =
				(EngineType) world.get(KEY.ENGINE_TYPES, engineTypeNumber);
			int trainNumber = world.size(KEY.TRAINS);
			AddTrainMove move =
				AddTrainMove.generateMove(
					trainNumber,
					train,
					engineType.getPrice(),
					s.toImmutableSchedule());

			MoveExecuter.getMoveExecuter().processMove(move);

			this.trainId = trainNumber;

			TrainMover trainMover =
				new TrainMover(
					getPathToFollow(p, world, trainNumber),
					getPathToFollow(p, world, trainNumber),
					world,
					trainNumber);

			gameEngine.addTrainMover(trainMover);
			//FreerailsPathIterator it = getPath(p);
			//PathWalker pw = new PathWalkerImpl(it);

			//pw.stepForward(400); 	

			//TrainPosition s = TrainPosition.createInSameDirectionAsPath(pw);

			/*
			int x = p.x * 30 + 15;
			int y = p.y * 30 + 15;
			
			s.moveHead(x, y);
			s.moveTail(x - 400, y - 400);
			*/

			System.out.println("Built train at: " + p.x + ", " + p.y);
		} else {
			System.out.println("No track here so cannot build train");
		}
	}

	public FreerailsPathIterator getPathToFollow(
		Point p,
		World w,
		int trainNumber) {

		PositionOnTrack pot =
			FlatTrackExplorer.getPossiblePositions(world, p)[0];

		//NewFlatTrackExplorer explorer =new NewFlatTrackExplorer(world.getMap(), pot);
		FlatTrackExplorer explorer = new FlatTrackExplorer(pot, world);
		//FlatTrackExplorer explorer = new FlatTrackExplorer(pot, world, trainId);

		FreerailsPathIterator it;

		it =
			new TrainPathIterator(
				new TrainPathFinder(explorer, w, trainNumber));

		return it;

	}
}

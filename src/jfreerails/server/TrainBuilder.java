package jfreerails.server;

import java.awt.Point;

import jfreerails.controller.MoveReceiver;
import jfreerails.controller.TrainMover;
import jfreerails.controller.pathfinder.FlatTrackExplorer;
import jfreerails.move.AddCargoBundleMove;
import jfreerails.move.AddTrainMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackRule;
import jfreerails.world.train.EngineType;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
import jfreerails.world.train.TrainPathIterator;

/** This class generates the move that adds a train to the game world and sets its initial position.  Note, the client
 * should not use this class to build trains, instead it should request that a train gets built by setting production 
 * at an engine shop.
 * 
 * @author Luke Lindsay 13-Oct-2002
 *
 */
public class TrainBuilder {

	private ReadOnlyWorld world;
	private ServerGameEngine gameEngine;
	private int trainId;
	private MoveReceiver moveReceiver;

	public TrainBuilder(ReadOnlyWorld w, ServerGameEngine gm,
		MoveReceiver mr) {
		this.world = w;
		this.gameEngine = gm;
		moveReceiver = mr;
		if (null == mr){
			throw new NullPointerException();
		}
	}

	/**
	 * @param engineTypeNumber type of the engine
	 * @param wagons array of wagon types
	 * @param p point at which to add train on map.
	 */
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
			int cargoBundleNumber = world.size(KEY.CARGO_BUNDLES);
			Move addCargoBundleMove = new
				AddCargoBundleMove(cargoBundleNumber, cb);
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
			
			ImmutableSchedule is = s.toImmutableSchedule();

			/* create a composite move so that they are executed
			 * atomically */
			/* TODO FIXME need to figure out what to do if the above
			 * step fails! */

			this.trainId = trainNumber;
		TrainPathFinder tpf = getPathToFollow(p, world,
		trainNumber);
		
		Move m = tpf.initTarget(train, is);

		AddTrainMove move =
		    AddTrainMove.generateMove(
			    trainNumber,
			    train,
			    engineType.getPrice(),
			    is);

		Move compositeMove = new CompositeMove(new Move[]
			{addCargoBundleMove, move, m});

		/*
		 * can't set the trains initial position yet because
		 * TrainPathFinder.nextInt() requires that the train exists
		 * in the world DB. I don't want to have special-case
		 * handling for moves which must be performed differently on
		 * the client than the server.
		 */
		moveReceiver.processMove(compositeMove);

		FreerailsPathIterator from = new TrainPathIterator(tpf); 
		
		tpf = getPathToFollow(p, world,
			trainNumber);

		tpf.initTarget(train, is);

		FreerailsPathIterator to = new TrainPathIterator(tpf);

		TrainMover trainMover =
		    new TrainMover( to, world, trainNumber);
		    

		/*
		 * call setInitialTrainPosition before processing the move so
		 * that the trains position is initialised on the client
		 * This means that the trains position is indeterminate for a
		 * period in the client (yuk!) 
		 */    
		Move positionMove = trainMover.setInitialTrainPosition(train,
		from);
		moveReceiver.processMove(positionMove);

			gameEngine.addTrainMover(trainMover);

			
		} else {
			System.err.println("No track here so cannot build train");
		}
	}

	/**
	 * @return a path iterator describing the path the train is to follow.
	 * @param p the point at which the path iterator starts.
	 */
	public TrainPathFinder getPathToFollow(
		Point p, ReadOnlyWorld w, int trainNumber) {

		PositionOnTrack pot =
			FlatTrackExplorer.getPossiblePositions(world, p)[0];

		FlatTrackExplorer explorer = new FlatTrackExplorer(pot, world);

		FreerailsPathIterator it;

		return new TrainPathFinder(explorer, w, trainNumber, moveReceiver);
	}
}

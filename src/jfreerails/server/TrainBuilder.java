package jfreerails.server;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import jfreerails.controller.FlatTrackExplorer;
import jfreerails.controller.RandomPathFinder;
import jfreerails.move.AddCargoBundleMove;
import jfreerails.move.AddTrainMove;
import jfreerails.move.ChangeProductionAtEngineShopMove;
import jfreerails.move.ChangeTrainMove;
import jfreerails.move.ChangeTrainScheduleMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.InitialiseTrainPositionMove;
import jfreerails.move.MarkAsCrashSiteMove;
import jfreerails.move.Move;
import jfreerails.move.PreMoveException;
import jfreerails.move.RemoveTrainMove;
import jfreerails.move.RemoveTrainPositionOnMapMove;
import jfreerails.move.TrainCrashException;
import jfreerails.network.MoveReceiver;
import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.Money;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.train.EngineType;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
import jfreerails.world.train.TrainPositionOnMap;

/**
 * This class generates the move that adds a train to the game world and sets
 * its initial position. Note, the client should not use this class to build
 * trains, instead it should request that a train gets built by setting
 * production at an engine shop.
 * 
 * @author Luke Lindsay 13-Oct-2002
 * 
 */
public class TrainBuilder implements ServerAutomaton {

	private static final long serialVersionUID = 3258410646839243577L;

	private static FreerailsPathIterator getRandomPathToFollow(Point p,
			ReadOnlyWorld w) {
		PositionOnTrack pot = FlatTrackExplorer.getPossiblePositions(w, p)[0];

		FlatTrackExplorer explorer = new FlatTrackExplorer(w, pot);

		/*
		 * Not 100% clear why next 2 lines are needed, but any exception gets
		 * thrown when the train's position gets updated if they are removed.
		 */
		explorer.nextEdge();
		explorer.moveForward();

		RandomPathFinder randomPathFinder = new RandomPathFinder(explorer);

		return randomPathFinder;
	}

	/**
	 * @return a move that initialises the trains schedule.
	 */
	public static Move initTarget(TrainModel train, int trainID,
			ImmutableSchedule currentSchedule, FreerailsPrincipal principal) {
		Vector<Move> moves = new Vector<Move>();
		int scheduleID = train.getScheduleID();
		MutableSchedule schedule = new MutableSchedule(currentSchedule);
		int[] wagonsToAdd = schedule.getWagonsToAdd();

		if (null != wagonsToAdd) {
			int engine = train.getEngineType();
			ChangeTrainMove move = ChangeTrainMove.generateMove(trainID, train,
					engine, wagonsToAdd, principal);
			moves.add(move);
		}

		schedule.gotoNextStaton();

		ImmutableSchedule newSchedule = schedule.toImmutableSchedule();
		ChangeTrainScheduleMove move = new ChangeTrainScheduleMove(scheduleID,
				currentSchedule, newSchedule, principal);
		moves.add(move);

		return new CompositeMove(moves.toArray(new Move[1]));
	}

	static TrainPositionOnMap setInitialTrainPosition(TrainModel train,
			FreerailsPathIterator from) {
		int trainLength = train.getLength();
		PathWalker fromPathWalker = new PathWalkerImpl(from);
		assert fromPathWalker.canStepForward();
		fromPathWalker.stepForward(trainLength);

		TrainPositionOnMap initialPosition = TrainPositionOnMap
				.createInSameDirectionAsPath(fromPathWalker);

		return initialPosition;
	}

	public static Point[] trainPos2Tiles(TrainPositionOnMap pos) {
		Point[] returnValue = new Point[pos.getLength()];
		final int TILE_WIDTH = 30;
		for (int i = 0; i < returnValue.length; i++) {
			returnValue[i] = new Point(pos.getX(i) / TILE_WIDTH, pos.getY(i)
					/ TILE_WIDTH);
		}

		return returnValue;
	}

	private transient MoveReceiver moveReceiver;

	private ArrayList<TrainMover> trainMovers = new ArrayList<TrainMover>();

	public TrainBuilder(MoveReceiver mr, ArrayList<TrainMover> trainMovers) {
		moveReceiver = mr;

		if (null == mr) {
			throw new NullPointerException();
		}

		this.trainMovers = trainMovers;
	}

	private void addTrainMover(TrainMover m) {
		trainMovers.add(m);
	}

	/**
	 * Generates a composite move that adds a train to the train list, adds a
	 * cargo bundle for the train to the cargo bundles list, and sets the
	 * train's initial position. The move is sent to the moveProcessor and a
	 * TrainMover object to update the trains position is returned.
	 * 
	 * @param engineTypeId
	 *            type of the engine
	 * @param wagons
	 *            array of wagon types
	 * @param p
	 *            point at which to add train on map.
	 * 
	 * 
	 */
	public TrainMover buildTrain(int engineTypeId, int[] wagons, Point p,
			FreerailsPrincipal principal, ReadOnlyWorld world) {
		/* Check that the specified position is on the track. */
		FreerailsTile tile = (FreerailsTile) world.getTile(p.x, p.y);
		if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != tile.getTrackTypeID()) {
			/* Create the move that sets up the train's cargo bundle. */
			int cargoBundleId = world.size(KEY.CARGO_BUNDLES, principal);
			Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleId,
					ImmutableCargoBundle.EMPTY_BUNDLE, principal);

			/* Create the train model object. */
			int scheduleId = world.size(KEY.TRAIN_SCHEDULES, principal);
			TrainModel train = new TrainModel(engineTypeId, wagons, scheduleId,
					cargoBundleId);

			/* Create the move that sets up the train's schedule. */

			// If there are no wagons, setup an automatic schedule.
			boolean autoSchedule = 0 == wagons.length;

			ImmutableSchedule is = generateInitialSchedule(principal, world,
					autoSchedule);
			int trainId = world.size(KEY.TRAINS, principal);
			Move setupScheduleMove = TrainBuilder.initTarget(train, trainId,
					is, principal);

			/* Create the move that sets the train's initial position. */
			FreerailsPathIterator from = getRandomPathToFollow(p, world);
			TrainPositionOnMap initialPosition = TrainBuilder
					.setInitialTrainPosition(train, from);
			Move positionMove = new InitialiseTrainPositionMove(trainId,
					initialPosition, principal);

			/* Determine the price of the train. */
			EngineType engineType = (EngineType) world.get(SKEY.ENGINE_TYPES,
					engineTypeId);
			Money price = engineType.getPrice();

			/* Create the move that adds the train to the train list. */
			AddTrainMove addTrainMove = AddTrainMove.generateMove(trainId,
					train, price, is, principal);

			/* Create a composite move made up of the moves created above. */
			Move compositeMove = new CompositeMove(new Move[] {
					addCargoBundleMove, addTrainMove, setupScheduleMove });

			/* Execute the move. */
			moveReceiver.processMove(compositeMove);
			moveReceiver.processMove(positionMove);

			/* Create a TrainMover to update the train's position. */
			TrainPathFinder tpf = getPathToFollow(p, world, trainId, principal);
			TrainMover trainMover = new TrainMover(tpf, world, trainId,
					principal);

			return trainMover;
		}
		throw new IllegalArgumentException("No track here (" + p.x + ", " + p.y
				+ ") so cannot build train");
	}

	/**
	 * Iterator over the stations and build trains at any that have their
	 * production field set.
	 * 
	 */
	void buildTrains(ReadOnlyWorld world) {
		for (int k = 0; k < world.getNumberOfPlayers(); k++) {
			FreerailsPrincipal principal = world.getPlayer(k).getPrincipal();

			for (int i = 0; i < world.size(KEY.STATIONS, principal); i++) {
				StationModel station = (StationModel) world.get(KEY.STATIONS,
						i, principal);

				if (null != station && null != station.getProduction()) {
					ProductionAtEngineShop[] production = station
							.getProduction();
					Point p = new Point(station.x, station.y);

					for (int j = 0; j < production.length; j++) {
						TrainMover trainMover = this.buildTrain(production[j]
								.getEngineType(),
								production[j].getWagonTypes(), p, principal,
								world);

						this.addTrainMover(trainMover);
					}

					ChangeProductionAtEngineShopMove move = new ChangeProductionAtEngineShopMove(
							production, new ProductionAtEngineShop[0], i,
							principal);
					moveReceiver.processMove(move);
				}
			}
		}
	}

	private boolean checkTrackType(TrainPositionOnMap pos, ReadOnlyWorld world) {
		Point[] positionA = trainPos2Tiles(pos);
		FreerailsTile tileA = (FreerailsTile) world.getTile(positionA[0].x,
				positionA[0].y);
		if (!(tileA.getTrackRule().isDouble())) {
			return false;
		}
		return true;
	}

	// crashes trains that meet head to head or head to tail when both trains
	// are moving. Still does not account for double track situations
	void crashTrains(TrainMover moverA, ReadOnlyWorld w)
			throws TrainCrashException {
		Iterator i = trainMovers.iterator();
		int trainAId = moverA.getTrainID();
		FreerailsPrincipal p = moverA.getPrincipal();
		TrainPositionOnMap currentPosition = (TrainPositionOnMap) w.get(
				KEY.TRAIN_POSITIONS, trainAId, p);
		if (!currentPosition.isCrashSite()) {
			while (i.hasNext()) {
				TrainMover moverB = (TrainMover) i.next();
				int trainBId = moverB.getTrainID();
				Point currentHead = new Point(currentPosition.getX(0),
						currentPosition.getY(0));
				Point currentTail = new Point(currentPosition
						.getX(currentPosition.getLength() - 1), currentPosition
						.getY(currentPosition.getLength() - 1));
				if (trainAId != trainBId) {
					TrainPositionOnMap trainBposition = (TrainPositionOnMap) w
							.get(KEY.TRAIN_POSITIONS, trainBId, p);
					Point trainBhead = new Point(trainBposition.getX(0),
							trainBposition.getY(0));
					Point trainBtail = new Point(trainBposition
							.getX(trainBposition.getLength() - 1),
							trainBposition.getY(trainBposition.getLength() - 1));
					if (moverA.isTrainMoving() && moverB.isTrainMoving()) {
						boolean crashed = false;
						if (willTrainsCrash(currentHead, trainBhead, 7)) {
							if (!(checkTrackType(currentPosition, w))) {
								crashed = true;
							}
						} else if (willTrainsCrash(currentHead, trainBtail, 7)) {
							if (!(checkTrackType(currentPosition, w))) {
								crashed = true;
							}
						} else if (willTrainsCrash(trainBhead, currentTail, 7)) {
							if (!(checkTrackType(trainBposition, w))) {
								crashed = true;
							}
						}
						if (crashed) {
							Move m = MarkAsCrashSiteMove.generateMove(trainAId,
									w, p);
							Move m2 = MarkAsCrashSiteMove.generateMove(
									trainBId, w, p);
							moveReceiver.processMove(m);
							moveReceiver.processMove(m2);
							throw new TrainCrashException(trainAId, trainBId);
						}
					}
				}

			}
		} else if (currentPosition.getFrameCt() == TrainPositionOnMap.CRASH_FRAMES_COUNT) {
			Move m = RemoveTrainPositionOnMapMove.generateMove(trainAId, p, w);
			moveReceiver.processMove(m);
		}
	}

	private ImmutableSchedule generateInitialSchedule(
			FreerailsPrincipal principal, ReadOnlyWorld world,
			boolean autoSchedule) {
		WorldIterator wi = new NonNullElements(KEY.STATIONS, world, principal);

		MutableSchedule s = new MutableSchedule();

		// Add upto 4 stations to the schedule.
		while (wi.next() && s.getNumOrders() < 5) {
			TrainOrdersModel orders = new TrainOrdersModel(wi.getIndex(), null,
					false, autoSchedule);
			s.addOrder(orders);
		}

		s.setOrderToGoto(0);

		ImmutableSchedule is = s.toImmutableSchedule();

		return is;
	}

	/**
	 * @return a path iterator describing the path the train is to follow.
	 * @param p
	 *            the point at which the path iterator starts.
	 */
	private TrainPathFinder getPathToFollow(Point p, ReadOnlyWorld w,
			int trainNumber, FreerailsPrincipal principal) {
		PositionOnTrack pot = FlatTrackExplorer.getPossiblePositions(w, p)[0];

		FlatTrackExplorer explorer = new FlatTrackExplorer(w, pot);

		TrainPathFinder tpf = new TrainPathFinder(explorer, w, trainNumber,
				moveReceiver, principal);

		return tpf;
	}

	public ArrayList getTrainMovers() {
		return trainMovers;
	}

	public void initAutomaton(MoveReceiver mr) {
		moveReceiver = mr;

		Iterator it = trainMovers.iterator();

		while (it.hasNext()) {
			TrainMover tm = (TrainMover) it.next();
			tm.initAutomaton(mr);
		}
	}

	void moveTrains(ReadOnlyWorld world) {
		int deltaDistance = 5;

		Iterator i = trainMovers.iterator();
		ArrayList crashedTrains = new ArrayList();
		while (i.hasNext()) {
			Object o = i.next();
			TrainMover trainMover = (TrainMover) o;

			try {
				crashTrains(trainMover, world);
				trainMover.update(deltaDistance, moveReceiver);
			} catch (PreMoveException e) {
				// Thrown when track under train is removed.
				// (1) Remove the train mover..
				i.remove();
				FreerailsPrincipal principal = trainMover.getPrincipal();
				// (2) Remove the train.
				int trainID = trainMover.getTrainID();

				Move removeTrainMove = RemoveTrainMove.getInstance(trainID,
						principal, world);
				moveReceiver.processMove(removeTrainMove);
			} catch (TrainCrashException tcex) {
				crashedTrains.add(tcex.getTrainA());
				crashedTrains.add(tcex.getTrainB());
			}
		}

		// remove the crashed trains and their train movers
		Iterator j = trainMovers.iterator();
		while (j.hasNext()) {
			TrainMover tMover = (TrainMover) j.next();
			int trainId = tMover.getTrainID();
			if (crashedTrains.contains(trainId)) {
				j.remove();
				FreerailsPrincipal principal = tMover.getPrincipal();
				Move removeTrainMove = RemoveTrainMove.getInstance(trainId,
						principal, world);
				moveReceiver.processMove(removeTrainMove);
			}
		}
	}

	private boolean willTrainsCrash(Point trainA, Point trainB, int tolerance) {
		return (Math.abs(trainA.y - trainB.y) < tolerance && Math.abs(trainA.x
				- trainB.x) < tolerance);
	}
}
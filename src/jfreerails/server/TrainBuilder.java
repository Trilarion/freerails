package jfreerails.server;

import java.awt.Point;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.pathfinder.FlatTrackExplorer;
import jfreerails.controller.pathfinder.RandomPathFinder;
import jfreerails.move.AddCargoBundleMove;
import jfreerails.move.AddTrainMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.InitialiseTrainPositionMove;
import jfreerails.move.Move;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.Money;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackRule;
import jfreerails.world.train.EngineType;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
import jfreerails.world.train.TrainPositionOnMap;


/** This class generates the move that adds a train to the game world and sets its initial position.  Note, the client
 * should not use this class to build trains, instead it should request that a train gets built by setting production
 * at an engine shop.
 *
 * @author Luke Lindsay 13-Oct-2002
 *
 */
public class TrainBuilder {
    private final World world;
    private final MoveReceiver moveReceiver;

    public TrainBuilder(World w, MoveReceiver mr) {
        this.world = w;
        moveReceiver = mr;

        if (null == mr) {
            throw new NullPointerException();
        }
    }

    /** Generates a composite move that adds a train to the train list, adds a
     * cargo bundle for the train to the cargo bundles list, and sets the train's
     * initial position.  The move is sent to the moveProcessor and a TrainMover object
     * to update the trains position is returned.
     *
     * @param engineTypeId type of the engine
     * @param wagons array of wagon types
     * @param p point at which to add train on map.
     *
     *
     */
    public TrainMover buildTrain(int engineTypeId, int[] wagons, Point p,
        FreerailsPrincipal principal) {
        /* Check that the specified position is on the track.*/
        FreerailsTile tile = world.getTile(p.x, p.y);
        TrackRule tr = tile.getTrackRule();

        if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != tr.getRuleNumber()) {
            /* Create the move that sets up the train's cargo bundle.*/
            CargoBundle cb = new CargoBundleImpl();
            int cargoBundleId = world.size(KEY.CARGO_BUNDLES, principal);
            Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleId, cb,
                    principal);

            /* Create the train model object.*/
            int scheduleId = world.size(KEY.TRAIN_SCHEDULES, principal);
            TrainModel train = new TrainModel(engineTypeId, wagons, null,
                    scheduleId, cargoBundleId);

            /* Create the move that sets up the train's schedule.*/
            ImmutableSchedule is = generateInitialSchedule(principal);
            int trainId = world.size(KEY.TRAINS, principal);
            Move setupScheduleMove = TrainPathFinder.initTarget(train, trainId,
                    is, principal);

            /* Create the move that sets the train's initial position.*/
            FreerailsPathIterator from = getRandomPathToFollow(p, world);
            TrainPositionOnMap initialPosition = TrainBuilder.setInitialTrainPosition(train,
                    from);
            Move positionMove = new InitialiseTrainPositionMove(trainId,
                    initialPosition, principal);

            /* Determine the price of the train.*/
            EngineType engineType = (EngineType)world.get(SKEY.ENGINE_TYPES,
                    engineTypeId);
            Money price = engineType.getPrice();

            /* Create the move that adds the train to the train list.*/
            AddTrainMove addTrainMove = AddTrainMove.generateMove(trainId,
                    train, price, is, principal);

            /* Create a composite move made up of the moves created above.*/
            Move compositeMove = new CompositeMove(new Move[] {
                        addCargoBundleMove, addTrainMove, setupScheduleMove
                    });

            /* Execute the move.*/
            moveReceiver.processMove(compositeMove);
            moveReceiver.processMove(positionMove);

            /* Create a TrainMover to update the train's position.*/
            TrainPathFinder tpf = getPathToFollow(p, world, trainId, principal);
            TrainMover trainMover = new TrainMover(tpf, world, trainId,
                    principal);

            return trainMover;
        } else {
            throw new IllegalArgumentException("No track here (" + p.x + ", " +
                p.y + ") so cannot build train");
        }
    }

    private ImmutableSchedule generateInitialSchedule(
        FreerailsPrincipal principal) {
        WorldIterator wi = new NonNullElements(KEY.STATIONS, world, principal);

        MutableSchedule s = new MutableSchedule();

        //Add upto 4 stations to the schedule.
        while (wi.next() && s.getNumOrders() < 5) {
            TrainOrdersModel orders = new TrainOrdersModel(wi.getIndex(), null,
                    false);
            s.addOrder(orders);
        }

        s.setOrderToGoto(0);

        ImmutableSchedule is = s.toImmutableSchedule();

        return is;
    }

    /**
    * @return a path iterator describing the path the train is to follow.
    * @param p the point at which the path iterator starts.
    */
    private TrainPathFinder getPathToFollow(Point p, ReadOnlyWorld w,
        int trainNumber, FreerailsPrincipal principal) {
        PositionOnTrack pot = FlatTrackExplorer.getPossiblePositions(world, p)[0];

        FlatTrackExplorer explorer = new FlatTrackExplorer(pot, world);

        TrainPathFinder tpf = new TrainPathFinder(explorer, w, trainNumber,
                moveReceiver, principal);

        return tpf;
    }

    private static FreerailsPathIterator getRandomPathToFollow(Point p,
        ReadOnlyWorld w) {
        PositionOnTrack pot = FlatTrackExplorer.getPossiblePositions(w, p)[0];

        FlatTrackExplorer explorer = new FlatTrackExplorer(pot, w);

        /* Not 100% clear why next 2 lines are needed, but any
         * exception gets thrown when the train's position gets
         * updated if they are removed.
         */
        explorer.nextEdge();
        explorer.moveForward();

        RandomPathFinder randomPathFinder = new RandomPathFinder(explorer);

        return randomPathFinder;
    }

    static TrainPositionOnMap setInitialTrainPosition(TrainModel train,
        FreerailsPathIterator from) {
        int trainLength = train.getLength();
        PathWalker fromPathWalker = new PathWalkerImpl(from);
        assert fromPathWalker.canStepForward();
        fromPathWalker.stepForward(trainLength);

        TrainPositionOnMap initialPosition = TrainPositionOnMap.createInSameDirectionAsPath(fromPathWalker);

        return initialPosition;
    }
}
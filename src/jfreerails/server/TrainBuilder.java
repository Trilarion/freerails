package jfreerails.server;

import java.awt.Point;
import jfreerails.controller.MoveReceiver;
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
import jfreerails.world.top.World;
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
import jfreerails.world.player.Player;
import jfreerails.world.player.FreerailsPrincipal;


/** This class generates the move that adds a train to the game world and sets its initial position.  Note, the client
 * should not use this class to build trains, instead it should request that a train gets built by setting production
 * at an engine shop.
 *
 * @author Luke Lindsay 13-Oct-2002
 *
 */
public class TrainBuilder {
    private World world;
    private MoveReceiver moveReceiver;

    public TrainBuilder(World w, MoveReceiver mr) {
        this.world = w;
        moveReceiver = mr;

        if (null == mr) {
            throw new NullPointerException();
        }
    }

    /** Warning, this method assumes that no other threads are accessing the world object!
     *
     * @param engineTypeNumber type of the engine
     * @param wagons array of wagon types
     * @param p point at which to add train on map.
     *
     *
     */
    public TrainMover buildTrain(int engineTypeNumber, int[] wagons, Point p) {
        FreerailsTile tile = (FreerailsTile)world.getTile(p.x, p.y);

        TrackRule tr = tile.getTrackRule();

        if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != tr.getRuleNumber()) {
            //Add train to train list.
            WorldIterator wi = new NonNullElements(KEY.STATIONS, world);

            MutableSchedule s = new MutableSchedule();

            //Add upto 4 stations to the schedule.
            while (wi.next() && s.getNumOrders() < 5) {
                TrainOrdersModel orders = new TrainOrdersModel(wi.getIndex(),
                        null, false);
                s.addOrder(orders);
            }

            s.setOrderToGoto(0);

            CargoBundle cb = new CargoBundleImpl();
            int cargoBundleNumber = world.size(KEY.CARGO_BUNDLES);
            Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleNumber,
                    cb);
            int scheduleNumber = world.size(KEY.TRAIN_SCHEDULES);

            TrainModel train = new TrainModel(engineTypeNumber, wagons, null,
                    scheduleNumber, cargoBundleNumber);

            EngineType engineType = (EngineType)world.get(KEY.ENGINE_TYPES,
                    engineTypeNumber);
            int trainNumber = world.size(KEY.TRAINS);

            ImmutableSchedule is = s.toImmutableSchedule();

            /* create a composite move so that they are executed
             * atomically */
            /* TODO FIXME need to figure out what to do if the above
             * step fails! */
            TrainPathFinder tpf = getPathToFollow(p, world, trainNumber);

            Move setupScheduleMove = tpf.initTarget(train, is);

	    /*
	     * FIXME all trains are currently billed to player 0 until such
	     * time as they are owned by players
	     */
	    FreerailsPrincipal playerZero = ((Player) world.get(KEY.PLAYERS, 0,
			Player.AUTHORITATIVE)) .getPrincipal();
	    AddTrainMove addTrainMove = AddTrainMove.generateMove(trainNumber,
		    train, engineType.getPrice(), is, playerZero);

            Move compositeMove = new CompositeMove(new Move[] {
                        addCargoBundleMove, addTrainMove, setupScheduleMove
                    });

            /*
             * We can't set the trains initial position yet because
             * TrainPathFinder.nextInt() requires that the train exists
             * in the world DB.  To get arround this we temporarily do the move
             * directly on world.  We will undo it before sending it to the client as
                         * one composite move.
                         */
            //compositeMove.doMove(world);
            moveReceiver.processMove(compositeMove);

            FreerailsPathIterator from = new TrainPathIterator(tpf);

            tpf = getPathToFollow(p, world, trainNumber);

            tpf.initTarget(train, is);

            TrainMover trainMover = new TrainMover(tpf, world, trainNumber);

            Move positionMove = trainMover.setInitialTrainPosition(train, from);

            /* Undo the move so that all the moves can be sent to the client as one
             * composite move.
             */

            //compositeMove.undoMove(world);
            Move compositeMovePlusPositionMove = new CompositeMove(new Move[] {
                        addCargoBundleMove, addTrainMove, setupScheduleMove,
                        positionMove
                    });

            //moveReceiver.processMove(compositeMovePlusPositionMove);  
            moveReceiver.processMove(positionMove);

            return trainMover;
        } else {
            throw new IllegalArgumentException("No track here (" + p.x + ", " +
                p.y + ") so cannot build train");
        }
    }

    /**
     * @return a path iterator describing the path the train is to follow.
     * @param p the point at which the path iterator starts.
     */
    public TrainPathFinder getPathToFollow(Point p, ReadOnlyWorld w,
        int trainNumber) {
        PositionOnTrack pot = FlatTrackExplorer.getPossiblePositions(world, p)[0];

        FlatTrackExplorer explorer = new FlatTrackExplorer(pot, world);

        FreerailsPathIterator it;

        TrainPathFinder tpf = new TrainPathFinder(explorer, w, trainNumber,
                moveReceiver);

        return tpf;
    }
}
package jfreerails.server;

import java.awt.Point;
import java.util.logging.Logger;
import jfreerails.controller.FreerailsServerSerializable;
import jfreerails.move.ChangeTrainPositionMove;
import jfreerails.move.PreMoveException;
import jfreerails.network.MoveReceiver;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPathIterator;
import jfreerails.world.train.TrainPositionOnMap;


/**
 * Responsible for moving the trains.
 *
 * @author Luke Lindsay 27-Oct-2002
 *
 * TODO make TrainMover stateless.
 *
 */
public class TrainMover implements FreerailsServerSerializable, ServerAutomaton {
    private static final Logger logger = Logger.getLogger(TrainMover.class.getName());
    private final PathWalker walker;
    private final int trainNumber;
    private final ReadOnlyWorld w;
    private final TrainPathFinder trainPathFinder;
    private final FreerailsPrincipal principal;
    private Point head;

    public TrainMover(FreerailsPathIterator to, ReadOnlyWorld world,
        int trainNo, FreerailsPrincipal p) {
        this.trainNumber = trainNo;
        this.w = world;
        walker = new PathWalkerImpl(to);
        trainPathFinder = null;
        principal = p;
    }

    public TrainMover(TrainPathFinder pathFinder, ReadOnlyWorld world,
        int trainNo, FreerailsPrincipal p) {
        this.trainNumber = trainNo;
        this.w = world;
        principal = p;

        FreerailsPathIterator to = new TrainPathIterator(pathFinder);
        walker = new PathWalkerImpl(to);
        this.trainPathFinder = pathFinder;
    }

    public PathWalker getWalker() {
        return walker;
    }

    public int getTrainID() {
        return trainNumber;
    }

    public FreerailsPrincipal getPrincipal() {
        return principal;
    }

    public void update(int distanceTravelled, MoveReceiver moveReceiver) throws PreMoveException {
        if (walker.canStepForward()) {
            double distanceTravelledAsDouble = distanceTravelled;
            double distance = distanceTravelledAsDouble * getTrainSpeed();
            walker.stepForward(distance);

            checkTrainPosition();
          
            ChangeTrainPositionMove m = ChangeTrainPositionMove.generate(w,
                    walker, trainNumber, principal);

            moveReceiver.processMove(m);
            head = m.newHead();
            checkTrainPosition();
            
        }
    }

    private void checkTrainPosition() {
        TrainPositionOnMap currentPosition = (TrainPositionOnMap)w.get(KEY.TRAIN_POSITIONS,
                trainNumber, principal);

        Point currentHead = new Point(currentPosition.getX(0),
                currentPosition.getY(0));

        if (null != head && !head.equals(currentHead)) {
            logger.severe("Expected head =" + head.toString() +
                ", actual head =" + currentHead.toString());
            logger.severe("Thread:" + Thread.currentThread().getName());

            IllegalStateException e = new IllegalStateException();
            e.printStackTrace();
            throw e;
        }
    }

    private double getTrainSpeed() {
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber, principal);
        int trainLength = train.getNumberOfWagons();

        //For now train speeds are hard coded.
        switch (trainLength) {
        case 0:
            return 1;

        case 1:
            return 0.8;

        case 2:
            return 0.6;

        case 3:
            return 0.4;

        case 4:
            return 0.3;

        default:
            return 0.2;
        }
    }

    public TrainPathFinder getTrainPathFinder() {
        return trainPathFinder;
    }

    public void initAutomaton(MoveReceiver mr) {
        trainPathFinder.initAutomaton(mr);
    }
}
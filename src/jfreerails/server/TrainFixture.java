package jfreerails.server;

import java.awt.Point;
import java.util.ArrayList;
import jfreerails.controller.MoveExecuter;
import jfreerails.controller.ToAndFroPathIterator;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.train.TrainModel;


/**
 * @author Luke Lindsay 30-Oct-2002
 *
 */
public class TrainFixture {
    TrainMover trainMover;
    ArrayList points = new ArrayList();
    World w = new WorldImpl(0, 0);
    Object mutex = new Integer(1);
    MoveExecuter moveExecuter;

    public TrainFixture() {
        moveExecuter = new MoveExecuter(w, null, mutex);

        points.add(new Point(0, 0));
        points.add(new Point(80, 80));
        points.add(new Point(150, 100));

        TrainModel train = new TrainModel(0);

        w.add(KEY.TRAINS, train);

        if (null == w.get(KEY.TRAINS, 0)) {
            throw new NullPointerException();
        }

        FreerailsPathIterator to = pathIterator();
        FreerailsPathIterator from = pathIterator();
        trainMover = new TrainMover(to, w, 0);
        moveExecuter.processMove(trainMover.setInitialTrainPosition(train, from));
    }

    public FreerailsPathIterator pathIterator() {
        return new ToAndFroPathIterator(points);
    }

    /**
     * Returns the points.
     * @return ArrayList
     */
    public ArrayList getPoints() {
        return points;
    }

    public World getWorld() {
        return w;
    }

    /**
     * Returns the trainMover.
     * @return TrainMover
     */
    public TrainMover getTrainMover() {
        return trainMover;
    }
}
package jfreerails.server;

import java.awt.Point;
import java.util.ArrayList;
import jfreerails.controller.ToAndFroPathIterator;
import jfreerails.move.InitialiseTrainPositionMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.player.Player;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;


/**
 * Used by TrainMoverTest.
 * @author Luke Lindsay 30-Oct-2002
 *
 */
public class TrainFixture {
    private TrainMover trainMover;
    private final ArrayList points = new ArrayList();
    private final World w = new WorldImpl(0, 0);

    public TrainFixture() {
        points.add(new Point(0, 0));
        points.add(new Point(80, 80));
        points.add(new Point(150, 100));

        TrainModel train = new TrainModel(0);
        w.addPlayer(MapFixtureFactory.TEST_PLAYER);
        w.add(KEY.TRAINS, train, MapFixtureFactory.TEST_PRINCIPAL);
        w.add(KEY.TRAIN_POSITIONS, null, MapFixtureFactory.TEST_PRINCIPAL);

        if (null == w.get(KEY.TRAINS, 0, MapFixtureFactory.TEST_PRINCIPAL)) {
            throw new NullPointerException();
        }

        FreerailsPathIterator to = pathIterator();
        FreerailsPathIterator from = pathIterator();
        trainMover = new TrainMover(to, w, 0, MapFixtureFactory.TEST_PRINCIPAL);

        TrainPositionOnMap initialPosition = TrainBuilder.setInitialTrainPosition(train,
                from);

        Move positionMove = new InitialiseTrainPositionMove(0, initialPosition,
                MapFixtureFactory.TEST_PRINCIPAL);

        MoveStatus ms = positionMove.doMove(w, Player.AUTHORITATIVE);

        if (!ms.isOk()) {
            throw new IllegalStateException(ms.message);
        }
    }

    private FreerailsPathIterator pathIterator() {
        return new ToAndFroPathIterator(points);
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
package jfreerails.server;

import java.awt.Point;
import java.util.ArrayList;

import jfreerails.controller.ToAndFroPathIterator;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.move.ChangeTrainPositionMove;
import jfreerails.world.common.IntLine;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;
import jfreerails.world.player.Player;
import junit.framework.TestCase;

/**
 * @author Luke Lindsay 30-Oct-2002
 *
 */
public class TrainMoverTest extends TestCase {
    TrainMover trainMover;
    World w;
    ArrayList points;  

    private Player testPlayer = new Player ("test player", (new Player ("test"
		    + " player")).getPublicKey(), 0);

    /**
     * Constructor for TrainMoverTest.
     * @param arg0
     */
    public TrainMoverTest(String arg0) {
        super(arg0);
    }

    protected void setUp() {
	w = new WorldImpl(0, 0);
	points = new ArrayList();

	points.add(new Point(0, 0));
	points.add(new Point(80, 80));
	points.add(new Point(150, 100));

	w.add(KEY.PLAYERS, testPlayer, Player.AUTHORITATIVE);

	TrainModel train = new TrainModel(0);

	w.add(KEY.TRAINS, train, testPlayer.getPrincipal());

	if (null == w.get(KEY.TRAINS, 0, testPlayer.getPrincipal())) {
	    throw new NullPointerException();
	}

	FreerailsPathIterator to = new ToAndFroPathIterator(points);
	FreerailsPathIterator from = new ToAndFroPathIterator(points);
	trainMover = new TrainMover(to, w, 0, testPlayer.getPrincipal());

	Move move = trainMover.setInitialTrainPosition(train, from);
	MoveStatus ms = move.doMove(w, Player.AUTHORITATIVE);

	if (!ms.isOk()) {
	    throw new IllegalStateException(ms.message);
	}
    }

    public void testTrainMover() {
        setUp();

        TrainModel t = (TrainModel)w.get(KEY.TRAINS, 0,
		testPlayer.getPrincipal());

        TrainPositionOnMap pos = t.getPosition();

        assertEquals(pos.getX(0), 0);
        assertEquals(pos.getY(0), 0);

        PathWalker pw = trainMover.getWalker();

        pw.stepForward(10);

        IntLine line = new IntLine();

        pw.nextSegment(line);

        assertEquals(line.x1, 0);
        assertEquals(line.y1, 0);
    }

    public void testUpdate() {
        setUp();

        TrainModel t = (TrainModel)w.get(KEY.TRAINS, 0,
		testPlayer.getPrincipal());

        TrainPositionOnMap pos = t.getPosition();

        ChangeTrainPositionMove m = trainMover.update(30);

        m.doMove(w, testPlayer.getPrincipal());
    }
}

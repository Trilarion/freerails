package jfreerails.server;

import java.util.ArrayList;
import jfreerails.move.ChangeTrainPositionMove;
import jfreerails.world.common.IntLine;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;
import junit.framework.TestCase;


/**
 * @author Luke Lindsay 30-Oct-2002
 *
 */
public class TrainMoverTest extends TestCase {
    ArrayList points;
    TrainMover trainMover;
    World w;

    /**
     * Constructor for TrainMoverTest.
     * @param arg0
     */
    public TrainMoverTest(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        TrainFixture trainFixture = new TrainFixture();

        points = trainFixture.getPoints();

        trainMover = trainFixture.getTrainMover();

        w = trainFixture.getWorld();
    }

    public void testTrainMover() {
        setUp();

        TrainModel t = (TrainModel)w.get(KEY.TRAINS, 0);

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

        TrainModel t = (TrainModel)w.get(KEY.TRAINS, 0);

        TrainPositionOnMap pos = t.getPosition();

        ChangeTrainPositionMove m = trainMover.update(30);

        m.doMove(w);
    }
}
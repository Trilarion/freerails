package jfreerails.server;

import jfreerails.move.ChangeTrainPositionMove;
import jfreerails.world.common.IntLine;
import jfreerails.world.player.Player;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
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

        trainMover = trainFixture.getTrainMover();

        w = trainFixture.getWorld();
        w.addPlayer(MapFixtureFactory.TEST_PLAYER, Player.AUTHORITATIVE);
    }

    public void testTrainMover() {
        setUp();

        TrainModel t = (TrainModel)w.get(KEY.TRAINS, 0,
                MapFixtureFactory.TEST_PRINCIPAL);

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

        ChangeTrainPositionMove m = trainMover.update(30);

        m.doMove(w, Player.AUTHORITATIVE);
    }
}
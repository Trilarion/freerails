package jfreerails.server;

import jfreerails.move.Move;
import jfreerails.move.PreMoveException;
import jfreerails.network.MoveReceiver;
import jfreerails.world.common.IntLine;
import jfreerails.world.player.Player;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.train.PathWalker;
import junit.framework.TestCase;


/**    JUnit test for TrainMover.
 * @author Luke Lindsay 30-Oct-2002
 *
 */
public class TrainMoverTest extends TestCase implements MoveReceiver {
    private TrainMover trainMover;
    private World w;

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
        w.addPlayer(MapFixtureFactory.TEST_PLAYER);
    }

    public void testTrainMover() {
        setUp();

        PathWalker pw = trainMover.getWalker();

        pw.stepForward(10);

        IntLine line = new IntLine();

        pw.nextSegment(line);

        assertEquals(line.x1, 0);
        assertEquals(line.y1, 0);
    }

    public void testUpdate() {
        setUp();
        try{
        	trainMover.update(30, this);
        }catch (PreMoveException e){
        	fail();
        }
    }

    public void processMove(Move move) {
        move.doMove(w, Player.AUTHORITATIVE);
    }
}
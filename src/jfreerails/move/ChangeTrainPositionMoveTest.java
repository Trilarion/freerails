package jfreerails.move;

import jfreerails.world.player.Player;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;
import junit.framework.TestCase;


/**
 * @author Luke Lindsay 03-Nov-2002
 *
 */
public class ChangeTrainPositionMoveTest extends TestCase {
    public World w;

    /**
     * Constructor for ChangeTrainPositionMoveTest.
     * @param arg0
     */
    public ChangeTrainPositionMoveTest(String arg0) {
        super(arg0);
    }

    public void testChangeTrainPositionMove() {
    }

    public void testGenerate() {
    }

    public void testDoMove() {
        setUp();

        TrainModel t = (TrainModel)this.w.get(KEY.TRAINS, 0,
                MapFixtureFactory.TEST_PRINCIPAL);
        TrainPositionOnMap oldPosition = t.getPosition();
        assertEquals(FIXTURE1_BEFORE_MOVE1, oldPosition);

        MoveStatus status = MOVE1.doMove(this.w, Player.AUTHORITATIVE);
        assertTrue(status.ok);

        t = (TrainModel)this.w.get(KEY.TRAINS, 0,
                MapFixtureFactory.TEST_PRINCIPAL);

        TrainPositionOnMap newPosition = t.getPosition();
        assertEquals(FIXTURE1_AFTER_MOVE1, newPosition);
    }

    public void testUndoMove() {
    }

    public void testTryDoMove() {
        setUp();

        MoveStatus status = MOVE1.tryDoMove(this.w, Player.AUTHORITATIVE);
        assertTrue(status.ok);
    }

    public void testTryUndoMove() {
    }

    protected void setUp() {
        w = new WorldImpl(1, 1);
        w.addPlayer(MapFixtureFactory.TEST_PLAYER, Player.AUTHORITATIVE);

        TrainModel train1 = new TrainModel(0, new int[] {},
                FIXTURE1_BEFORE_MOVE1, 0);
        w.add(KEY.TRAINS, train1, MapFixtureFactory.TEST_PRINCIPAL);
    }

    public static final ChangeTrainPositionMove MOVE1 = new ChangeTrainPositionMove(TrainPositionOnMap.createInstance(
                new int[] {0, 10}, new int[] {1, 11}),
            TrainPositionOnMap.createInstance(new int[] {37, 40},
                new int[] {38, 44}), 0, true, false,
            MapFixtureFactory.TEST_PRINCIPAL);
    public static final TrainPositionOnMap FIXTURE1_BEFORE_MOVE1 = TrainPositionOnMap.createInstance(new int[] {
                10, 30, 40
            }, new int[] {11, 33, 44});
    public static final TrainPositionOnMap FIXTURE1_AFTER_MOVE1 = TrainPositionOnMap.createInstance(new int[] {
                0, 30, 37
            }, new int[] {1, 33, 38});
}
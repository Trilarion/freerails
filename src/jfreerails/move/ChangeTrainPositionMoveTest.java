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
 *  JUnit test.
 * @author Luke
 *
 */
public class ChangeTrainPositionMoveTest extends TestCase {
    private World w;

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

        TrainPositionOnMap oldPosition = (TrainPositionOnMap)w.get(KEY.TRAIN_POSITIONS,
                0, MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(FIXTURE1_BEFORE_MOVE1, oldPosition);

        MoveStatus status = MOVE1.doMove(this.w, Player.AUTHORITATIVE);
        assertTrue(status.ok);

        TrainPositionOnMap newPosition = (TrainPositionOnMap)w.get(KEY.TRAIN_POSITIONS,
                0, MapFixtureFactory.TEST_PRINCIPAL);

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
        w.addPlayer(MapFixtureFactory.TEST_PLAYER);

        TrainModel train1 = new TrainModel(0, new int[] {}, 0);
        w.add(KEY.TRAINS, train1, MapFixtureFactory.TEST_PRINCIPAL);
        w.add(KEY.TRAIN_POSITIONS, FIXTURE1_BEFORE_MOVE1,
            MapFixtureFactory.TEST_PRINCIPAL);
    }

    private static final ChangeTrainPositionMove MOVE1 = new ChangeTrainPositionMove(TrainPositionOnMap.createInstance(
                new int[] {0, 10}, new int[] {1, 11}),
            TrainPositionOnMap.createInstance(new int[] {37, 40},
                new int[] {38, 44}), 0, true, false,
            MapFixtureFactory.TEST_PRINCIPAL);
    private static final TrainPositionOnMap FIXTURE1_BEFORE_MOVE1 = TrainPositionOnMap.createInstance(new int[] {
                10, 30, 40
            }, new int[] {11, 33, 44});
    private static final TrainPositionOnMap FIXTURE1_AFTER_MOVE1 = TrainPositionOnMap.createInstance(new int[] {
                0, 30, 37
            }, new int[] {1, 33, 38});
}
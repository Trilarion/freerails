package jfreerails.move;

import jfreerails.world.common.ImInts;
import jfreerails.world.player.Player;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;

/**
 * JUnit test.
 * 
 * @author Luke
 * 
 */
public class ChangeTrainPositionMoveTest extends AbstractMoveTestCase {

	/**
	 * Constructor for ChangeTrainPositionMoveTest.
	 * 
	 * @param arg0
	 */
	public ChangeTrainPositionMoveTest(String arg0) {
		super(arg0);
	}

	public void testDoMove() {
		setUp();

		TrainPositionOnMap oldPosition = (TrainPositionOnMap) world.get(
				KEY.TRAIN_POSITIONS, 0, MapFixtureFactory.TEST_PRINCIPAL);
		assertEquals(FIXTURE1_BEFORE_MOVE1, oldPosition);

		MoveStatus status = MOVE1.doMove(world, Player.AUTHORITATIVE);
		assertTrue(status.ok);

		TrainPositionOnMap newPosition = (TrainPositionOnMap) world.get(
				KEY.TRAIN_POSITIONS, 0, MapFixtureFactory.TEST_PRINCIPAL);

		assertEquals(FIXTURE1_AFTER_MOVE1, newPosition);
	}

	public void testUndoMove() {
	}

	public void testTryDoMove() {
		setUp();

		MoveStatus status = MOVE1.tryDoMove(world, Player.AUTHORITATIVE);
		assertTrue(status.ok);
	}

	protected void setUp() {
		world = new WorldImpl(1, 1);
		world.addPlayer(MapFixtureFactory.TEST_PLAYER);

		TrainModel train1 = new TrainModel(0, new ImInts(), 0);
		world.add(KEY.TRAINS, train1, MapFixtureFactory.TEST_PRINCIPAL);
		world.add(KEY.TRAIN_POSITIONS, FIXTURE1_BEFORE_MOVE1,
				MapFixtureFactory.TEST_PRINCIPAL);
	}

	private static final ChangeTrainPositionMove MOVE1 = new ChangeTrainPositionMove(
			TrainPositionOnMap.createInstance(new int[] { 0, 10 }, new int[] {
					1, 11 }), TrainPositionOnMap.createInstance(new int[] { 37,
					40 }, new int[] { 38, 44 }), 0, true, false,
			MapFixtureFactory.TEST_PRINCIPAL);

	private static final TrainPositionOnMap FIXTURE1_BEFORE_MOVE1 = TrainPositionOnMap
			.createInstance(new int[] { 10, 30, 40 }, new int[] { 11, 33, 44 });

	private static final TrainPositionOnMap FIXTURE1_AFTER_MOVE1 = TrainPositionOnMap
			.createInstance(new int[] { 0, 30, 37 }, new int[] { 1, 33, 38 });

	@Override
	public void testMove() {
		assertSurvivesSerialisation(MOVE1);
		assertSurvivesSerialisation(FIXTURE1_BEFORE_MOVE1);
		assertSurvivesSerialisation(FIXTURE1_AFTER_MOVE1);
	}
}
package jfreerails.move;

import jfreerails.world.train.EngineModel;
import jfreerails.world.train.TrainList;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPosition;
import junit.framework.TestCase;

/**
 * @author Luke Lindsay 03-Nov-2002
 *
 */
public class ChangeTrainPositionMoveTest extends TestCase {
	
	public TrainList trainList;

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
		TrainPosition oldPosition = this.trainList.getTrain(0).getPosition();
		assertEquals(FIXTURE1_BEFORE_MOVE1, oldPosition);
		MoveStatus status = MOVE1.doMove(this.trainList);
		assertTrue(status.ok);
		TrainPosition newPosition = this.trainList.getTrain(0).getPosition();
		assertEquals(FIXTURE1_AFTER_MOVE1, newPosition);
	}

	public void testUndoMove() {
	}

	public void testTryDoMove() {
		setUp();
		MoveStatus status = MOVE1.tryDoMove(this.trainList);
		assertTrue(status.ok);
		
	}

	public void testTryUndoMove() {
	}
	
	protected void setUp(){
		this.trainList = new TrainList();
		TrainModel train1 = new TrainModel(new EngineModel(), FIXTURE1_BEFORE_MOVE1);	
		trainList.addTrain(train1);		
	}
	

	public static final ChangeTrainPositionMove MOVE1 =
		new ChangeTrainPositionMove(
			TrainPosition.createInstance(
				new int[] { 0, 10 },
				new int[] { 1, 11 }),
			TrainPosition.createInstance(
				new int[] { 37, 40 },
				new int[] { 38, 44 }),
				0, true, false);

	public static final TrainPosition FIXTURE1_BEFORE_MOVE1 =
		TrainPosition.createInstance(
			new int[] { 10,  30, 40 },
			new int[] { 11,  33, 44 });
			
	public static final TrainPosition FIXTURE1_AFTER_MOVE1 =
		TrainPosition.createInstance(
			new int[] {0, 30, 37 },
			new int[] {1, 33, 38 });

}

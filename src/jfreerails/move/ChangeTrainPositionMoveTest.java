package jfreerails.move;

import jfreerails.world.misc.FreerailsPathIterator;
import jfreerails.world.train.EngineModel;
import jfreerails.world.train.SimplePathIteratorImpl;
import jfreerails.world.train.TrainList;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPosition;
import junit.framework.TestCase;

/**
 * @author Luke Lindsay 27-Oct-2002
 *
 */
public class ChangeTrainPositionMoveTest extends TestCase {

	/**
	 * Constructor for ChangeTrainPositionMoveTest.
	 * @param arg0
	 */
	public ChangeTrainPositionMoveTest(String arg0) {
		super(arg0);
	}
	
	protected void setUp(){
		this.trainList = new TrainList();
		TrainModel train1 = new TrainModel(new EngineModel(), FIXTURE1_BEFORE_MOVE1);	
		trainList.addTrain(train1);		
	}

	public void testChangeTrainPositionMove() {
	}

	public void testGenerate() {
		setUp();
		
		TrainPosition currentPosition = this.trainList.getTrain(0).getPosition();		
		
		TrainPosition newSection=TrainPosition.createInstance(
				new int[] { 40, 50 },
				new int[] { 44, 55 });
				
		assertTrue(TrainPosition.canBeAdded(currentPosition, newSection));
				
		FreerailsPathIterator path = newSection.path();
							
		
		ChangeTrainPositionMove move = ChangeTrainPositionMove.generate(currentPosition, path, 0);
				
		MoveStatus status = move.doMove(trainList);
		assertNotNull(status);
		assertTrue(status.ok);
		
		status = move.undoMove(trainList);
		assertNotNull(status);
		assertTrue(status.ok);
			
	}

	public void testDoMove() {
		setUp();
				
		assertMoveSucceeds(FIXTURE1_BEFORE_MOVE1, FIXTURE1_AFTER_MOVE1, MOVE1);
		
		
	}

	private void assertMoveSucceeds(
		TrainPosition before,
		TrainPosition after,
		ChangeTrainPositionMove m) {
		TrainModel train = trainList.getTrain(m.trainPositionNumber);
		
		assertEquals(train.getPosition(), before);
		
		MoveStatus status = m.doMove(trainList);
		assertNotNull(status);
		assertTrue(status.ok);
		
		
		assertTrue(train.getPosition().equals(after));
	}

	public void testUndoMove() {
		setUp();
		
		MoveStatus status;
		
		status = MOVE1.undoMove(trainList);
		assertNotNull(status);
		assertTrue(!status.ok);
				
		assertMoveSucceeds(FIXTURE1_BEFORE_MOVE1, FIXTURE1_AFTER_MOVE1, MOVE1);
		
		status = MOVE1.doMove(trainList);
		assertNotNull(status);
		assertTrue(!status.ok);
		
		status = MOVE1.undoMove(trainList);
		assertNotNull(status);
		assertTrue(status.ok);
		
		TrainModel train = trainList.getTrain(MOVE1.trainPositionNumber);
		
		assertEquals(train.getPosition(), FIXTURE1_BEFORE_MOVE1);
				
		
	}

	public void testTryDoMove() {
		
		setUp();
		MoveStatus status = MOVE1.tryDoMove(trainList);
		assertNotNull(status);
		assertTrue(status.ok);
		
	}

	public void testTryUndoMove() {
		setUp();
		
		MoveStatus status;
		
		status = MOVE1.tryUndoMove(trainList);
		assertNotNull(status);
		assertTrue(!status.ok);
				
		assertMoveSucceeds(FIXTURE1_BEFORE_MOVE1, FIXTURE1_AFTER_MOVE1, MOVE1);
		
		status = MOVE1.tryDoMove(trainList);
		assertNotNull(status);
		assertTrue(!status.ok);
		
		status = MOVE1.tryUndoMove(trainList);
		assertNotNull(status);
		assertTrue(status.ok);
		
						
		
	}
	
	public TrainList trainList;

	public static final ChangeTrainPositionMove MOVE1 =
		new ChangeTrainPositionMove(
			TrainPosition.createInstance(
				new int[] { 40, 50 },
				new int[] { 44, 55 }),
			TrainPosition.createInstance(
				new int[] { 10, 20 },
				new int[] { 11, 22 }),
				0);

	public static final TrainPosition FIXTURE1_BEFORE_MOVE1 =
		TrainPosition.createInstance(
			new int[] { 10,  30, 40 },
			new int[] { 11,  33, 44 });
			
	public static final TrainPosition FIXTURE1_AFTER_MOVE1 =
		TrainPosition.createInstance(
			new int[] { 20, 30,  50 },
			new int[] { 22, 33,  55 });

}

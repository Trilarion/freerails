/*
 * Created on 28-Mar-2003
 * 
 */
package jfreerails.move;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 * All TestCases for moves should extend this class.
 * 
 * @author Luke
 * 
 */
public abstract class AbstractMoveTestCase extends TestCase {

	World world;

	private boolean hasSetupBeenCalled = false; //

	protected void setUp() {
		hasSetupBeenCalled = true;
		world = new WorldImpl();
	}

	abstract public void testMove();

	protected void assertTryMoveIsOk(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryDoMove(world);
		assertNotNull(ms);
		assertEquals("First try failed", MoveStatus.MOVE_OK, ms);

		ms = m.tryDoMove(world);
		assertNotNull(ms);
		assertEquals(
			"Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",
			MoveStatus.MOVE_OK,
			ms);
	}

	protected void assertTryMoveFails(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryDoMove(world);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertDoMoveIsOk(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.doMove(world);
		assertNotNull(ms);
		assertEquals(MoveStatus.MOVE_OK, ms);

	}

	protected void assertDoMoveFails(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.doMove(world);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertTryUndoMoveIsOk(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryUndoMove(world);
		assertNotNull(ms);
		assertEquals("First try failed", MoveStatus.MOVE_OK, ms);

		ms = m.tryUndoMove(world);
		assertNotNull(ms);
		assertEquals(
			"Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",
			MoveStatus.MOVE_OK,
			ms);

	}

	protected void assertTryUndoMoveFails(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryUndoMove(world);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertUndoMoveIsOk(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.undoMove(world);
		assertNotNull(ms);
		assertEquals(MoveStatus.MOVE_OK, ms);
	}

	protected void assertUndoMoveFails(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryUndoMove(world);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	/** Generally moves should not be repeatable.  For example,
	 * if we have just removed a piece of track, that piece of 
	 * track is gone, so we cannot remove it again.
	 */

	protected void assertOkButNotRepeatable(Move m) {
		assertSetupHasBeenCalled();

		assertTryMoveIsOk(m);
		assertDoMoveIsOk(m);
		assertTryMoveFails(m);
		assertDoMoveFails(m);
		assertTryUndoMoveIsOk(m);
		assertUndoMoveIsOk(m);
		assertTryUndoMoveFails(m);
		assertTryMoveIsOk(m);
		assertDoMoveIsOk(m);
	}

	/** This method asserts that if we serialise then deserialise the
	 * specified move, the specified move is equal to the deserialised move.
	 * The assertion depends on the move being serialisable and the equals method 
	 * being implemented correctly.
	 *  
	 * @param m
	 */
	protected void assertEqualsSurvivesSerialisation(Move m) {
		
		assertEquals("Reflexivity violated: the move does not equal itself", m ,m);
		try {

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(out);
			objectOut.writeObject(m);
			objectOut.flush();

			byte[] bytes = out.toByteArray();

			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			ObjectInputStream objectIn = new ObjectInputStream(in);
			Object o = objectIn.readObject();
			assertEquals(m, o);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

	}

	protected void assertOkAndRepeatable(Move m) {
		assertSetupHasBeenCalled();

		//Do move
		assertTryMoveIsOk(m);
		assertDoMoveIsOk(m);
		assertTryMoveIsOk(m);
		assertDoMoveIsOk(m);

		//Since it leaves the world unchanged it should also be 
		//possible to undo it repeatably
		assertTryUndoMoveIsOk(m);
		assertUndoMoveIsOk(m);
		assertTryUndoMoveIsOk(m);
		assertUndoMoveIsOk(m);
	}

	private void assertSetupHasBeenCalled() {
		assertTrue("AbstractMoveTestCase.setUp has not been called!", hasSetupBeenCalled);
	}
	
	public AbstractMoveTestCase(){
		
	}
	
	public AbstractMoveTestCase(String str){
		super(str);
	}

}

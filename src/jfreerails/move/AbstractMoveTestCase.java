/*
 * Created on 28-Mar-2003
 * 
 */
package jfreerails.move;

import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 * All move TestCases for moves should extend this class.
 * 
 * @author Luke
 * 
 */
public abstract class AbstractMoveTestCase extends TestCase {

	World w;
	
	private boolean hasSetupBeenCalled = false;		//

	protected void setUp() {
		hasSetupBeenCalled = true;
		w = new WorldImpl();
	}

	abstract public void testMove();	

	protected void assertTryMoveIsOk(Move m) {
		assertSetupHasBeenCalled();
		
		MoveStatus ms = m.tryDoMove(w);
		assertNotNull(ms);
		assertEquals("First try failed", MoveStatus.MOVE_OK, ms);
				
		ms = m.tryDoMove(w);
		assertNotNull(ms);
		assertEquals("Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",MoveStatus.MOVE_OK, ms);
	}

	protected void assertTryMoveFails(Move m) {
		assertSetupHasBeenCalled();
		
		MoveStatus ms = m.tryDoMove(w);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertDoMoveIsOk(Move m) {
		assertSetupHasBeenCalled();
		
		MoveStatus ms = m.doMove(w);
		assertNotNull(ms);
		assertEquals(MoveStatus.MOVE_OK, ms);
				
	}

	protected void assertDoMoveFails(Move m) {
		assertSetupHasBeenCalled();
		
		MoveStatus ms = m.doMove(w);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertTryUndoMoveIsOk(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryUndoMove(w);
		assertNotNull(ms);
		assertEquals("First try failed",MoveStatus.MOVE_OK, ms);
				
		ms = m.tryUndoMove(w);
		assertNotNull(ms);
		assertEquals("Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",MoveStatus.MOVE_OK, ms);
		
	}

	protected void assertTryUndoMoveFails(Move m) {
		assertSetupHasBeenCalled();
		
		MoveStatus ms = m.tryUndoMove(w);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertUndoMoveIsOk(Move m) {
		assertSetupHasBeenCalled();
		
		MoveStatus ms = m.undoMove(w);
		assertNotNull(ms);
		assertEquals(MoveStatus.MOVE_OK, ms);
	}

	protected void assertUndoMoveFails(Move m) {
		assertSetupHasBeenCalled();
		
		MoveStatus ms = m.tryUndoMove(w);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}
	
	/** Generally moves should not be repeatable.  For example,
	 * if we have just removed a piece of track, that piece of 
	 * track is gone, so we cannot remove it again.
	 */
	
	protected void assertOkButNotRepeatable(Move m){
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
	
	
	protected void assertOkAndRepeatable(Move m){
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
	
	private void assertSetupHasBeenCalled(){
		assertTrue("AbstractMoveTestCase.setUp has not been called!", hasSetupBeenCalled);
	}

}

/*
 * Created on 28-Mar-2003
 *
 */
package jfreerails.move;

import jfreerails.util.Utils;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.Step;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.train.PathOnTiles;
import junit.framework.TestCase;

/**
 * All TestCases for moves should extend this class.
 * 
 * @author Luke
 * 
 */
public abstract class AbstractMoveTestCase extends TestCase {
	private boolean hasSetupBeenCalled = false;

	protected World world;

	protected AbstractMoveTestCase() {
	}

	public AbstractMoveTestCase(String str) {
		super(str);
	}

	protected void assertDoMoveFails(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.doMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertDoMoveIsOk(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.doMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertEquals(MoveStatus.MOVE_OK, ms);
	}

	protected void assertDoThenUndoLeavesWorldUnchanged(Move m) {
		try {
			World w = getWorld();
			World before = (World) Utils.cloneBySerialisation(w);

			assertEquals(before, w);

			assertTrue(m.doMove(w, Player.AUTHORITATIVE).ok);
			World after = (World) Utils.cloneBySerialisation(w);
			assertFalse(after.equals(before));

			boolean b = m.undoMove(w, Player.AUTHORITATIVE).ok;
			assertTrue(b);
			assertEquals(before, w);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * This method asserts that if we serialise then deserialise the specified
	 * move, the specified move is equal to the deserialised move. The assertion
	 * depends on the move being serialisable and the equals method being
	 * implemented correctly. Also checks that the hashcode does not change.
	 * 
	 * @param m
	 */
	protected void assertSurvivesSerialisation(FreerailsSerializable m) {
		assertEquals("Reflexivity violated: the move does not equal itself", m,
				m);

		try {
			Object o = Utils.cloneBySerialisation(m);
			assertEquals(m, o);
			assertEquals("The hashcodes should be the same!", m.hashCode(), o
					.hashCode());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	protected void assertOkAndRepeatable(Move m) {
		assertSetupHasBeenCalled();

		// Do move
		assertTryMoveIsOk(m);
		assertDoMoveIsOk(m);
		assertTryMoveIsOk(m);
		assertDoMoveIsOk(m);

		// Since it leaves the world unchanged it should also be
		// possible to undo it repeatably
		assertTryUndoMoveIsOk(m);
		assertUndoMoveIsOk(m);
		assertTryUndoMoveIsOk(m);
		assertUndoMoveIsOk(m);
	}

	/**
	 * Generally moves should not be repeatable. For example, if we have just
	 * removed a piece of track, that piece of track is gone, so we cannot
	 * remove it again.
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

	private void assertSetupHasBeenCalled() {
		assertTrue("AbstractMoveTestCase.setUp has not been called!",
				hasSetupBeenCalled());
	}

	protected void assertTryMoveFails(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryDoMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertTryMoveIsOk(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryDoMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertEquals("First try failed", MoveStatus.MOVE_OK, ms);

		ms = m.tryDoMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertEquals(
				"Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",
				MoveStatus.MOVE_OK, ms);
	}

	protected void assertTryUndoMoveFails(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryUndoMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertTryUndoMoveIsOk(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryUndoMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertEquals("First try failed", MoveStatus.MOVE_OK, ms);

		ms = m.tryUndoMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertEquals(
				"Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",
				MoveStatus.MOVE_OK, ms);
	}

	protected void assertUndoMoveFails(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.tryUndoMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertTrue("Move went through when it should have failed", !ms.ok);
	}

	protected void assertUndoMoveIsOk(Move m) {
		assertSetupHasBeenCalled();

		MoveStatus ms = m.undoMove(getWorld(), Player.AUTHORITATIVE);
		assertNotNull(ms);
		assertEquals(MoveStatus.MOVE_OK, ms);
	}

	FreerailsPrincipal getPrincipal() {
		return world.getPlayer(0).getPrincipal();
	}

	World getWorld() {
		return world;
	}

	protected boolean hasSetupBeenCalled() {
		return hasSetupBeenCalled;
	}

	protected void setHasSetupBeenCalled(boolean hasSetupBeenCalled) {
		this.hasSetupBeenCalled = hasSetupBeenCalled;
	}

	protected void setUp() throws Exception {
		setHasSetupBeenCalled(true);
		setupWorld();
	}

	protected void setupWorld() {
		setWorld(new WorldImpl(10, 10));
		// Set the time..
		getWorld().set(ITEM.CALENDAR, new GameCalendar(12000, 1840));
		getWorld().addPlayer(MapFixtureFactory.TEST_PLAYER);
	}

	void setWorld(World world) {
		this.world = world;
	}

	public void testMove(){}

	protected void assertTrackHere(int x, int y) {

		FreerailsTile tile = (FreerailsTile) world.getTile(x, y);
		assertTrue(tile.hasTrack());
	}

	protected void assertTrackHere(PathOnTiles path) {
		ImPoint start = path.getStart();
		int x = start.x;
		int y = start.y;
		for (int i = 0; i < path.steps(); i++) {
			assertTrackHere(x, y);
			Step step = path.getStep(i);
			x += step.deltaX;
			y += step.deltaY;
			assertTrackHere(x, y);
		}
	}
}
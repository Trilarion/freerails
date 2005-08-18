/*
 * Created on 03-Aug-2005
 *
 */
package jfreerails.move;

import jfreerails.util.Utils;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.accounts.Transaction.Category;
import jfreerails.world.common.Activity;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.terrain.CityModel;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldDiffs;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.top.WorldImplTest.TestActivity;
import junit.framework.TestCase;

public class WorldDiffsMoveTest extends TestCase {

	World world;

	WorldDiffs diffs;

	FreerailsPrincipal fp1;

	CityModel city1 = new CityModel("City 1", 8, 4);

	CityModel city2 = new CityModel("City 2", 9, 4);

	protected void setUp() throws Exception {
		world = new WorldImpl(10, 10);
		// Set the time..
		world.set(ITEM.CALENDAR, new GameCalendar(12000, 1840));
		world.addPlayer(MapFixtureFactory.TEST_PLAYER);
		fp1 = world.getPlayer(0).getPrincipal();
		diffs = new WorldDiffs(world);
	}

	public void testChangingMap() {

		diffs.setTile(4, 0, city1);
		diffs.setTile(8, 5, city2);

		runTests();
	}

	public void testChangingElementInList1() {
		world.add(fp1, KEY.STATIONS, city1);
		world.add(fp1, KEY.STATIONS, city1);
		diffs.set(fp1, KEY.STATIONS, 0, city2);
		diffs.set(fp1, KEY.STATIONS, 1, city2);
		runTests();

	}

	public void testChangingElementInList2() {
		world.add(fp1, KEY.STATIONS, city1);
		world.add(fp1, KEY.STATIONS, city1);
		diffs.set(fp1, KEY.STATIONS, 0, city2);
		diffs.set(fp1, KEY.STATIONS, 1, city2);
		assertEquals(2, diffs.listDiffs());
		WorldDiffMove move = WorldDiffMove.generate(diffs, WorldDiffMove.Cause.Other);

		assertEquals(2, move.listDiffs());

	}

	public void testAddingElementToList() {
		world.add(fp1, KEY.STATIONS, city1);
		diffs.add(fp1, KEY.STATIONS, city2);
		diffs.add(fp1, KEY.STATIONS, city2);
		diffs.add(fp1, KEY.STATIONS, city2);
		runTests();
	}

	public void testAddingTransaction() {
		Transaction t1 = new AddItemTransaction(Category.BOND, 1, 1, new Money(
				100));
		Transaction t2 = new AddItemTransaction(Category.BOND, 2, 2, new Money(
				1000));
		Transaction t3 = new AddItemTransaction(Category.BOND, 3, 3, new Money(
				10000));
		world.addTransaction(fp1, t1);
		diffs.addTransaction(fp1, t2);
		diffs.addTransaction(fp1, t3);
		runTests();

	}

	public void  testAddingActivity(){
		Activity act = new TestActivity(30);
		int row = world.addActiveEntity(fp1, act);
		act = new TestActivity(40);
		world.add(fp1, row, act);
		act = new TestActivity(50);
		diffs.add(fp1, row, act);
		runTests();
	}
	
	public void testAddingActiviteEntity() {
		Activity act = new TestActivity(30);
		int row = world.addActiveEntity(fp1, act);
		act = new TestActivity(40);
		world.add(fp1, row, act);
		act = new TestActivity(50);
		diffs.add(fp1, row, act);
		act = new TestActivity(60);
		row = diffs.addActiveEntity(fp1, act);
		act = new TestActivity(70);
		diffs.add(fp1, row, act);
		act = new TestActivity(80);
		row = diffs.addActiveEntity(fp1, act);
		act = new TestActivity(90);
		diffs.add(fp1, row, act);

		runTests();
	}

	void runTests() {
		assertFalse(diffs.equals(world));
		WorldDiffMove move = WorldDiffMove.generate(diffs, WorldDiffMove.Cause.Other);

		// Doing the move on the world should also succeed.
		World worldCopy = (World) Utils.cloneBySerialisation(world);
		assertEquals(worldCopy, world);
		MoveStatus ms = move.tryDoMove(worldCopy, fp1);
		
		if(!ms.ok)
			ms.printStackTrack();
		assertTrue(ms.message, ms.ok);
		
		ms = move.doMove(worldCopy, fp1);
		assertTrue(ms.ok);
		assertEquals(worldCopy, diffs);

		// Undoing the move on the diffs should suceed.
		WorldDiffs diffsCopy = (WorldDiffs) Utils.cloneBySerialisation(diffs);
		assertEquals(diffsCopy, diffs);
		ms = move.tryUndoMove(diffsCopy, fp1);
		assertTrue(ms.message, ms.ok);
		assertFalse(diffsCopy.equals(world));
		ms = move.undoMove(diffsCopy, fp1);
		assertTrue(ms.ok);
		assertEquals(diffsCopy, world);

		// The move should survive serialisation.
		Object moveCopy = Utils.cloneBySerialisation(move);
		assertEquals(moveCopy, move);
		assertEquals(moveCopy.hashCode(), move.hashCode());

	}

}

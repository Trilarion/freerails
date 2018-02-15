/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.move.mapupdatemove;

import freerails.model.world.*;
import freerails.move.MoveStatus;
import freerails.move.WorldDiffMoveCause;
import freerails.util.Vector2D;
import freerails.util.Utils;
import freerails.model.*;
import freerails.model.finances.ItemTransaction;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionCategory;
import freerails.model.game.GameCalendar;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.terrain.City;
import junit.framework.TestCase;

/**
 *
 */
public class WorldDiffsMoveTest extends TestCase {

    private final City city1 = new City("City 1", new Vector2D(8, 4));
    private final City city2 = new City("City 2", new Vector2D(9, 4));
    private World world;
    private FullWorldDiffs diffs;
    private FreerailsPrincipal fp1;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = new FullWorld(new Vector2D(10, 10));
        // Set the time..
        world.set(WorldItem.Calendar, new GameCalendar(12000, 1840));
        world.addPlayer(MapFixtureFactory.TEST_PLAYER);
        fp1 = world.getPlayer(0).getPrincipal();
        diffs = new FullWorldDiffs(world);
    }

    /**
     *
     */
    public void testChangingMap() {

        diffs.setTile(new Vector2D(4, 0), city1);
        diffs.setTile(new Vector2D(8, 5), city2);

        runTests();
    }

    /**
     *
     */
    public void testChangingElementInList1() {
        world.add(fp1, PlayerKey.Stations, city1);
        world.add(fp1, PlayerKey.Stations, city1);
        diffs.set(fp1, PlayerKey.Stations, 0, city2);
        diffs.set(fp1, PlayerKey.Stations, 1, city2);
        runTests();
    }

    /**
     *
     */
    public void testChangingElementInList2() {
        world.add(fp1, PlayerKey.Stations, city1);
        world.add(fp1, PlayerKey.Stations, city1);
        diffs.set(fp1, PlayerKey.Stations, 0, city2);
        diffs.set(fp1, PlayerKey.Stations, 1, city2);
        assertEquals(2, diffs.listDiffs());
        WorldDiffMove move = WorldDiffMove.generate(diffs, WorldDiffMoveCause.Other);
        assertEquals(2, move.listDiffs());
    }

    /**
     *
     */
    public void testAddingElementToList() {
        world.add(fp1, PlayerKey.Stations, city1);
        diffs.add(fp1, PlayerKey.Stations, city2);
        diffs.add(fp1, PlayerKey.Stations, city2);
        diffs.add(fp1, PlayerKey.Stations, city2);
        runTests();
    }

    /**
     *
     */
    public void testAddingTransaction() {
        Transaction t1 = new ItemTransaction(TransactionCategory.BOND, 1, 1, new Money(100));
        Transaction t2 = new ItemTransaction(TransactionCategory.BOND, 2, 2, new Money(1000));
        Transaction t3 = new ItemTransaction(TransactionCategory.BOND, 3, 3, new Money(10000));
        world.addTransaction(fp1, t1);
        diffs.addTransaction(fp1, t2);
        diffs.addTransaction(fp1, t3);
        runTests();
    }

    /**
     *
     */
    public void testAddingActivity() {
        Activity act = new TestActivity(30);
        int row = world.addActiveEntity(fp1, act);
        act = new TestActivity(40);
        world.addActivity(fp1, row, act);
        act = new TestActivity(50);
        diffs.addActivity(fp1, row, act);
        runTests();
    }

    /**
     *
     */
    public void testAddingActiveEntity() {
        Activity act = new TestActivity(30);
        int row = world.addActiveEntity(fp1, act);
        act = new TestActivity(40);
        world.addActivity(fp1, row, act);
        act = new TestActivity(50);
        diffs.addActivity(fp1, row, act);
        act = new TestActivity(60);
        row = diffs.addActiveEntity(fp1, act);
        act = new TestActivity(70);
        diffs.addActivity(fp1, row, act);
        act = new TestActivity(80);
        row = diffs.addActiveEntity(fp1, act);
        act = new TestActivity(90);
        diffs.addActivity(fp1, row, act);

        runTests();
    }

    private void runTests() {
        assertFalse(diffs.equals(world));
        WorldDiffMove move = WorldDiffMove.generate(diffs, WorldDiffMoveCause.Other);

        // Doing the move on the world should also succeed.
        World worldCopy = (World) Utils.cloneBySerialisation(world);
        assertEquals(worldCopy, world);
        MoveStatus moveStatus = move.tryDoMove(worldCopy, fp1);

        assertTrue(moveStatus.getMessage(), moveStatus.succeeds());

        moveStatus = move.doMove(worldCopy, fp1);
        assertTrue(moveStatus.succeeds());
        assertEquals(worldCopy, diffs);

        // Undoing the move on the diffs should succeed.
        FullWorldDiffs diffsCopy = (FullWorldDiffs) Utils.cloneBySerialisation(diffs);
        assertEquals(diffsCopy, diffs);
        moveStatus = move.tryUndoMove(diffsCopy, fp1);
        assertTrue(moveStatus.getMessage(), moveStatus.succeeds());
        assertFalse(diffsCopy.equals(world));
        moveStatus = move.undoMove(diffsCopy, fp1);
        assertTrue(moveStatus.succeeds());
        assertEquals(diffsCopy, world);

        // The move should survive serialisation.
        Object moveCopy = Utils.cloneBySerialisation(move);
        assertEquals(moveCopy, move);
        assertEquals(moveCopy.hashCode(), move.hashCode());
    }

}

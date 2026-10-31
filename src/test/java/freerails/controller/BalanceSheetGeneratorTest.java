/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
package freerails.controller;

import freerails.model.player.Player;
import freerails.model.economy.reporting.BalanceSheetGenerator;
import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.move.Status;
import freerails.model.*;
import freerails.model.economy.Money;
import freerails.model.world.World;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

/**
 * Test for BalanceSheetGenerator.
 */
public class BalanceSheetGeneratorTest extends TestCase {

    private Player player;
    private World world;

    /**
     * Tests bonds figure behavior.
     */
    public void testBondsFigure() {
        BalanceSheetGenerator generator = new BalanceSheetGenerator(world, player);
        assertEquals(ModelConstants.BOND_VALUE_ISSUE, generator.total.getLoans());
        assertEquals(ModelConstants.BOND_VALUE_ISSUE, generator.ytd.getLoans());
    }

    /**
     * Tests stock holder equity figure behavior.
     */
    public void testStockHolderEquityFigure() {
        BalanceSheetGenerator generator = new BalanceSheetGenerator(world, player);
        assertEquals(new Money(500000), generator.total.getEquity());
    }

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        world = WorldGenerator.minimalWorld();
        player = new Player(world.getPlayers().size(), "Player X");
        // add a new player
        Move move = new AddPlayerMove(player);
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
        move.apply(world);

    }

}

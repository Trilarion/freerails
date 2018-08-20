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
package freerails.controller;

import freerails.model.player.Player;
import freerails.model.finance.BalanceSheetGenerator;
import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.nove.Status;
import freerails.model.*;
import freerails.model.finance.Money;
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
     *
     */
    public void testBondsFigure() {
        BalanceSheetGenerator generator = new BalanceSheetGenerator(world, player);
        assertEquals(ModelConstants.BOND_VALUE_ISSUE, generator.total.getLoans());
        assertEquals(ModelConstants.BOND_VALUE_ISSUE, generator.ytd.getLoans());
    }

    /**
     *
     */
    public void testStockHolderEquityFigure() {
        BalanceSheetGenerator generator = new BalanceSheetGenerator(world, player);
        assertEquals(new Money(500000), generator.total.getEquity());
    }

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        world = WorldGenerator.minimalWorld();
        player = new Player(world.getPlayers().size(), "Player X");
        Move addPlayerMove = AddPlayerMove.generateMove(world, player);
        Status status = addPlayerMove.doMove(world, player);
        assertTrue(status.getMessage(), status.isSuccess());

        // world.setTime(new Clock.Time(100));
    }

}

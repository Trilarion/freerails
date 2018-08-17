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

import freerails.io.GsonManager;
import freerails.model.game.Rules;
import freerails.model.player.Player;
import freerails.model.statistics.BalanceSheetGenerator;
import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.move.Status;
import freerails.model.*;
import freerails.model.finances.Money;
import freerails.model.game.Time;
import freerails.model.world.World;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMoveTest;
import freerails.util.Vec2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.File;
import java.net.URL;

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
        Money expectedBondValue = ModelConstants.BOND_VALUE_ISSUE;
        assertEquals(Money.opposite(expectedBondValue), generator.total.loans);
        assertEquals(Money.opposite(expectedBondValue), generator.ytd.loans);
    }

    /**
     *
     */
    public void testStockHolderEquityFigure() {
        BalanceSheetGenerator generator = new BalanceSheetGenerator(world, player);
        Money expectStockHolderEquity = new Money(-500000);
        assertEquals(expectStockHolderEquity, generator.total.equity);
    }

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        world = WorldGenerator.minimalWorld();
        player = new Player(world.getPlayers().size(), "Player X");
        world.setTime(new Time(0));

        Move addPlayerMove = AddPlayerMove.generateMove(world, player);
        Status status = addPlayerMove.doMove(world, player);
        assertTrue(status.getMessage(), status.succeeds());

        world.setTime(new Time(100));
    }

}

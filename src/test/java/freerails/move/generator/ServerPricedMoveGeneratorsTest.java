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

package freerails.move.generator;

import freerails.model.MapFixtureFactory2;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TileTransition;
import freerails.model.world.World;
import freerails.move.Move;
import freerails.move.RefusedMove;
import freerails.move.Status;
import freerails.util.IntPoint2D;
import junit.framework.TestCase;

/**
 * Tests that the generators clients send compute effects and costs from the
 * generating world, and that player-initiated costs need enough cash.
 */
public class ServerPricedMoveGeneratorsTest extends TestCase {

    private static final IntPoint2D FROM = new IntPoint2D(5, 5);

    private World world;
    private Player player;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        player = world.getPlayer(0);
    }

    /**
     * Tests that building track charges its cost and needs enough cash.
     */
    public void testBuildTrackIsChargedAndCashConstrained() {
        BuildTrackMoveGenerator generator = new BuildTrackMoveGenerator(FROM, TileTransition.EAST, 0, 0, player);

        setBalance(new Money(-1));
        assertFalse(generator.generate(world).applicable(world).isSuccess());

        setBalance(new Money(1_000_000_000L));
        Money before = world.getCurrentBalance(player);
        apply(generator.generate(world));
        assertTrue(world.getCurrentBalance(player).amount() < before.amount());
        assertNotNull(world.getTile(FROM).trackPiece());
    }

    /**
     * Tests that removing or upgrading missing track is refused with a reason.
     */
    public void testMissingTrackIsRefused() {
        assertEquals(new RefusedMove("No track to remove."),
                new RemoveTrackMoveGenerator(FROM, TileTransition.EAST, player).generate(world));
        assertEquals(new RefusedMove("No track to upgrade."),
                new UpgradeTrackMoveGenerator(FROM, 0, player).generate(world));
    }

    /**
     * Tests that building an industry charges the industry's cost and needs enough cash.
     */
    public void testBuildIndustryIsChargedAndCashConstrained() {
        Terrain industry = world.getTerrains().stream()
                .filter(terrain -> terrain.getIndustryBuildCost() != null)
                .findFirst().orElseThrow();
        IntPoint2D location = clearTile();
        BuildIndustryMoveGenerator generator = new BuildIndustryMoveGenerator(location, industry.getId(), player);

        setBalance(Money.ZERO);
        assertFalse(generator.generate(world).applicable(world).isSuccess());

        setBalance(new Money(1_000_000_000L));
        Money before = world.getCurrentBalance(player);
        apply(generator.generate(world));
        assertEquals(Money.subtract(before, industry.getIndustryBuildCost()), world.getCurrentBalance(player));
        assertEquals(industry.getId(), world.getTile(location).terrainTypeId());
    }

    /**
     * Tests that only industries with a build cost can be built, and only on the map.
     */
    public void testInvalidIndustriesAreRefused() {
        Terrain notIndustry = world.getTerrains().stream()
                .filter(terrain -> terrain.getIndustryBuildCost() == null)
                .findFirst().orElseThrow();
        assertTrue(new BuildIndustryMoveGenerator(clearTile(), notIndustry.getId(), player).generate(world)
                instanceof RefusedMove);
        assertTrue(new BuildIndustryMoveGenerator(new IntPoint2D(-1, 0), notIndustry.getId(), player).generate(world)
                instanceof RefusedMove);
    }

    private IntPoint2D clearTile() {
        for (int x = 0; x < world.getMapSize().x(); x++) {
            for (int y = 0; y < world.getMapSize().y(); y++) {
                IntPoint2D location = new IntPoint2D(x, y);
                if (world.getTerrain(world.getTile(location).terrainTypeId()).getCategory() == TerrainCategory.COUNTRY) {
                    return location;
                }
            }
        }
        throw new AssertionError("No clear tile in the fixture.");
    }

    private void apply(Move move) {
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
        move.apply(world);
    }

    private void setBalance(Money balance) {
        Money change = Money.subtract(balance, world.getCurrentBalance(player));
        world.addTransaction(player, new Transaction(TransactionCategory.MISC_INCOME, change,
                world.getClock().getCurrentTime()));
    }
}

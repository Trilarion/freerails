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
package freerails.move;

import freerails.model.track.TrackRule;
import freerails.model.world.*;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.TrackMove;
import freerails.util.Vec2D;
import freerails.model.player.Player;
import freerails.model.MapFixtureFactory;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import junit.framework.TestCase;

/**
 * Test for TrackMoveTransactionsGenerator.
 */
public class TrackMoveTransactionsGeneratorTest extends TestCase {

    private World world;
    private TrackMoveTransactionsGenerator transactionGenerator;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = new World.Builder().setMapSize(new Vec2D(10, 10)).build();
        MapFixtureFactory.generateTrackRuleList(world);
        Player player = new Player("test player", 0);
        world.addPlayer(player);
        transactionGenerator = new TrackMoveTransactionsGenerator(world, player.getPrincipal());
    }

    /**
     *
     */
    public void testAddTrackMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        TrackMove trackMove;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = world.getTile(Vec2D.ZERO).getTrackPiece();

        TrackRule r = (TrackRule) world.get(SharedKey.TrackRules, 0);
        int owner = WorldUtils.getPlayerIndex(world, MapFixtureFactory.TEST_PRINCIPAL);
        newTrackPiece = new TrackPiece(newConfig, r, owner, 0);
        trackMove = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, Vec2D.ZERO);

        Move move = transactionGenerator.addTransactions(trackMove);
        assertNotNull(move);
    }
}
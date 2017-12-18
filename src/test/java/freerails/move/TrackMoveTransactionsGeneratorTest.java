/*
 * Created on 10-Aug-2003
 *
 */
package freerails.move;

import freerails.world.common.ImPoint;
import freerails.world.player.Player;
import freerails.world.top.MapFixtureFactory;
import freerails.world.top.SKEY;
import freerails.world.top.World;
import freerails.world.top.WorldImpl;
import freerails.world.track.*;
import junit.framework.TestCase;

/**
 * JUnit test case for TrackMoveTransactionsGenerator.
 *
 * @author Luke Lindsay
 */
public class TrackMoveTransactionsGeneratorTest extends TestCase {
    private World world;

    private TrackMoveTransactionsGenerator transactionGenerator;

    /**
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        world = new WorldImpl(20, 20);
        MapFixtureFactory.generateTrackRuleList(world);
        Player player = new Player("test player", 0);
        world.addPlayer(player);
        transactionGenerator = new TrackMoveTransactionsGenerator(world, player
                .getPrincipal());
    }

    /**
     *
     */
    public void testAddTrackMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        TrackMove move;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = ((FreerailsTile) world.getTile(0, 0)).getTrackPiece();

        TrackRule r = (TrackRule) world.get(SKEY.TRACK_RULES, 0);
        int owner = ChangeTrackPieceCompositeMove.getOwner(
                MapFixtureFactory.TEST_PRINCIPAL, world);
        newTrackPiece = new TrackPieceImpl(newConfig, r, owner, 0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece,
                new ImPoint(0, 0));

        Move m = transactionGenerator.addTransactions(move);
        assertNotNull(m);
    }
}
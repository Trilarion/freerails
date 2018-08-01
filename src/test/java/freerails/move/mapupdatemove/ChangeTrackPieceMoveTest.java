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
 * ChangeTrackPieceMoveTest.java
 * JUnit based test
 *
 */
package freerails.move.mapupdatemove;

import freerails.io.GsonManager;
import freerails.model.player.Player;
import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.move.AbstractMoveTestCase;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.savegames.MapCreator;
import freerails.util.Vec2D;
import freerails.model.world.World;
import freerails.model.game.GameRules;
import freerails.model.MapFixtureFactory;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;

import java.io.File;
import java.net.URL;
import java.util.SortedSet;

/**
 *
 */
public class ChangeTrackPieceMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setHasSetupBeenCalled(true);
        // load terrain types
        URL url = MapCreator.class.getResource("/freerails/data/scenario/terrain_types.json");
        File file = new File(url.toURI());
        SortedSet<Terrain> terrainTypes = GsonManager.loadTerrainTypes(file);

        // generate track types
        SortedSet<TrackType> trackTypes = MapFixtureFactory.generateTrackRuleList();

        setWorld(new World.Builder().setMapSize(new Vec2D(10, 10)).setTerrainTypes(terrainTypes).setTrackTypes(trackTypes).build());
        getWorld().setGameRules(GameRules.NO_RESTRICTIONS);
    }

    /**
     *
     */
    public void testTryDoMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        TrackMove move;
        MoveStatus moveStatus;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = getWorld().getTile(Vec2D.ZERO).getTrackPiece();

        final int trackRuleID = 0;
        TrackType trackType = getWorld().getTrackType(trackRuleID);

        newTrackPiece = new TrackPiece(newConfig, trackType, 0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, Vec2D.ZERO);
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.succeeds());

        // As above but with newTrackPiece and oldTrackPiece in the wrong order,
        // should fail.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece, Vec2D.ZERO);
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertFalse(moveStatus.succeeds());

        // Try a move that does nothing, i.e. oldTrackPiece==newTrackPiece, should fail.
        move = new ChangeTrackPieceMove(oldTrackPiece, oldTrackPiece, Vec2D.ZERO);
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertFalse(moveStatus.succeeds());

        // Try to build track outside the map.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece, new Vec2D(100, 0));
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertFalse(moveStatus.succeeds());

        // Try building an illegal track configuration.
        newConfig = TrackConfiguration.getFlatInstance("000011111");

        newTrackPiece = new TrackPiece(newConfig, trackType,0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, Vec2D.ZERO);
        moveStatus = move.tryDoMove(getWorld(), Player.AUTHORITATIVE);
        assertFalse(moveStatus.succeeds());
    }

    /**
     *
     */
    public void testDoMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("000010000");
        oldTrackPiece = getWorld().getTile(Vec2D.ZERO).getTrackPiece();

        TrackType trackType = getWorld().getTrackType(0);
        newTrackPiece = new TrackPiece(newConfig, trackType, 0);

        assertMoveDoMoveIsOk(oldTrackPiece, newTrackPiece);
    }

    /**
     * @param oldTrackPiece
     * @param newTrackPiece
     */
    private void assertMoveDoMoveIsOk(TrackPiece oldTrackPiece, TrackPiece newTrackPiece) {
        TrackMove move;
        MoveStatus moveStatus;

        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, Vec2D.ZERO);
        moveStatus = move.doMove(getWorld(), Player.AUTHORITATIVE);
        assertNotNull(moveStatus);
        assertEquals(true, moveStatus.succeeds());
        TrackConfiguration actual = getWorld().getTile(Vec2D.ZERO)
                .getTrackPiece().getTrackConfiguration();
        assertEquals(newTrackPiece.getTrackConfiguration(), actual);
    }

    /**
     *
     */
    public void testMove() {
        TrackPiece oldTrackPiece = getWorld().getTile(Vec2D.ZERO).getTrackPiece();

        TrackConfiguration newConfig = TrackConfiguration.getFlatInstance("000010000");
        TrackType trackType = getWorld().getTrackType(0);
        TrackPiece newTrackPiece = new TrackPiece(newConfig, trackType,0);

        Move move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, Vec2D.ZERO);

        assertSurvivesSerialisation(move);

        assertOkButNotRepeatable(move);
    }
}
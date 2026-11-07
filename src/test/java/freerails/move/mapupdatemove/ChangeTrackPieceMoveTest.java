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
 * ChangeTrackPieceMoveTest.java
 * JUnit based test
 *
 */
package freerails.move.mapupdatemove;

import freerails.io.GsonManager;
import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.move.AbstractMoveTestCase;
import freerails.move.Move;
import freerails.move.Status;
import freerails.io.scenario.ClasspathScenarioSource;
import freerails.util.IntPoint2D;
import freerails.model.world.World;
import freerails.model.game.Rules;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.util.WorldGenerator;

import java.io.File;
import java.net.URL;
import java.util.SortedSet;

/**
 * Tests change track piece move behavior.
 */
public class ChangeTrackPieceMoveTest extends AbstractMoveTestCase {

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setHasSetupBeenCalled(true);
        // load terrain types
        URL url = ClasspathScenarioSource.class.getResource("/freerails/data/scenario/terrain_types.json");
        File file = new File(url.toURI());
        SortedSet<Terrain> terrainTypes = GsonManager.loadTerrainTypes(file);

        // generate track types
        SortedSet<TrackType> trackTypes = WorldGenerator.testTrackTypes();

        // load rules
        url = ChangeTrackPieceCompositeMoveTest.class.getResource("/rules.without_restrictions.json");
        file = new File(url.toURI());
        Rules rules = GsonManager.load(file, Rules.class);

        setWorld(new World.Builder().setMapSize(new IntPoint2D(10, 10)).setTerrainTypes(terrainTypes).setTrackTypes(trackTypes).setRules(rules).build());
    }

    /**
     * Tests try do move behavior.
     */
    public void testTryDoMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;
        TrackMove move;
        Status status;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("00010000");
        oldTrackPiece = getWorld().getTile(IntPoint2D.ZERO).trackPiece();

        final int trackRuleID = 0;
        TrackType trackType = getWorld().getTrackType(trackRuleID);

        newTrackPiece = new TrackPiece(newConfig, trackType, 0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, IntPoint2D.ZERO);
        status = move.applicable(getWorld());
        assertNotNull(status);
        assertEquals(true, status.isSuccess());

        // As above but with newTrackPiece and oldTrackPiece in the wrong order,
        // should fail.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece, IntPoint2D.ZERO);
        status = move.applicable(getWorld());
        assertNotNull(status);
        assertFalse(status.isSuccess());

        // Try a move that does nothing, i.e. oldTrackPiece==newTrackPiece, should fail.
        move = new ChangeTrackPieceMove(oldTrackPiece, oldTrackPiece, IntPoint2D.ZERO);
        status = move.applicable(getWorld());
        assertNotNull(status);
        assertFalse(status.isSuccess());

        // Try to build track outside the map.
        move = new ChangeTrackPieceMove(newTrackPiece, oldTrackPiece, new IntPoint2D(100, 0));
        status = move.applicable(getWorld());
        assertNotNull(status);
        assertFalse(status.isSuccess());

        // Try building an illegal track configuration.
        newConfig = TrackConfiguration.getFlatInstance("00111100");

        newTrackPiece = new TrackPiece(newConfig, trackType,0);
        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, IntPoint2D.ZERO);
        status = move.applicable(getWorld());
        assertFalse(status.isSuccess());
    }

    /**
     * Verifies that applying a track-piece move replaces the tile's track configuration.
     */
    public void testDoMove() {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;
        TrackConfiguration newConfig;

        // Try building the simplest piece of track.
        newConfig = TrackConfiguration.getFlatInstance("00010000");
        oldTrackPiece = getWorld().getTile(IntPoint2D.ZERO).trackPiece();

        TrackType trackType = getWorld().getTrackType(0);
        newTrackPiece = new TrackPiece(newConfig, trackType, 0);

        assertMoveDoMoveIsOk(oldTrackPiece, newTrackPiece);
    }

    /**
     * Asserts move do move is ok.
     * @param oldTrackPiece the old track piece to use in the test.
     * @param newTrackPiece the new track piece to use in the test.
     */
    private void assertMoveDoMoveIsOk(TrackPiece oldTrackPiece, TrackPiece newTrackPiece) {
        TrackMove move;
        Status status;

        move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, IntPoint2D.ZERO);
        status = move.applicable(getWorld());
        assertEquals(true, status.isSuccess());
        move.apply(getWorld());
        TrackConfiguration actual = getWorld().getTile(IntPoint2D.ZERO)
                .trackPiece().trackConfiguration();
        assertEquals(newTrackPiece.trackConfiguration(), actual);
    }

    /**
     * Tests move behavior.
     */
    public void testMove() {
        TrackPiece oldTrackPiece = getWorld().getTile(IntPoint2D.ZERO).trackPiece();

        TrackConfiguration newConfig = TrackConfiguration.getFlatInstance("00010000");
        TrackType trackType = getWorld().getTrackType(0);
        TrackPiece newTrackPiece = new TrackPiece(newConfig, trackType,0);

        Move move = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, IntPoint2D.ZERO);

        assertSurvivesSerialisation(move);

        assertOkButNotRepeatable(move);
    }
}
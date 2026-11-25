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

package freerails.model.world;

import freerails.model.MapFixtureFactory2;
import freerails.model.simulation.Speed;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackPiece;
import freerails.util.IntPoint2D;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

/**
 * Tests the read-only view of a world with proposed track on top.
 */
public class ProposedTrackWorldTest extends TestCase {

    private static final IntPoint2D TILE = new IntPoint2D(5, 5);

    private World world;
    private ProposedTrackWorld view;
    private TrackPiece piece;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        view = new ProposedTrackWorld(world);
        piece = TrackPiece.create(TileTransition.EAST, world.getTrackType(0), 0);
    }

    /**
     * Tests that a proposed piece replaces the track of a tile and keeps its terrain,
     * without changing the live world.
     */
    public void testProposedTrackReplacesTheTilesTrack() {
        TerrainTile live = world.getTile(TILE);

        view.propose(TILE, piece);

        assertEquals(new TerrainTile(live.terrainTypeId(), piece), view.getTile(TILE));
        assertEquals(live, world.getTile(TILE));
        assertEquals(Map.of(TILE, piece), view.getProposedTrack());
    }

    /**
     * Tests that proposing no piece removes the track of a tile.
     */
    public void testProposingNoPieceRemovesTrack() {
        world.setTile(TILE, new TerrainTile(world.getTile(TILE).terrainTypeId(), piece));

        view.propose(TILE, null);

        assertNull(view.getTile(TILE).trackPiece());
        assertTrue(view.getProposedTrack().containsKey(TILE));
        assertEquals(piece, world.getTile(TILE).trackPiece());
    }

    /**
     * Tests that tiles without proposals and everything else come from the live
     * world, also when it changes, and that clear restores the live view.
     */
    public void testEverythingElseIsLive() {
        view.propose(TILE, piece);
        IntPoint2D other = new IntPoint2D(6, 6);

        assertSame(world.getTile(other), view.getTile(other));
        assertEquals(world.getRules(), view.getRules());
        assertEquals(world.getMapSize(), view.getMapSize());
        assertEquals(List.copyOf(world.getPlayers()), List.copyOf(view.getPlayers()));

        world.setTile(other, new TerrainTile(world.getTile(other).terrainTypeId(), piece));
        assertEquals(piece, view.getTile(other).trackPiece());

        view.clear();
        assertEquals(world.getTile(TILE), view.getTile(TILE));
        assertTrue(view.getProposedTrack().isEmpty());
    }

    /**
     * Tests that proposals off the map and state changes are refused.
     */
    public void testInvalidUseIsRefused() {
        try {
            view.propose(new IntPoint2D(-1, 0), piece);
            fail("Track off the map must not be proposed.");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        try {
            view.setSpeed(Speed.FAST);
            fail("The view must not change the game speed.");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
        try {
            view.getProposedTrack().clear();
            fail("The proposed track must not be changed through its view.");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }
}

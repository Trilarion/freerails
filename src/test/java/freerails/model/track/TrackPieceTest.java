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
package freerails.model.track;

import freerails.util.TestUtils;
import freerails.model.MapFixtureFactory2;
import freerails.model.terrain.TileTransition;
import freerails.model.world.World;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Tests track piece behavior.
 */
public class TrackPieceTest extends TestCase {

    private World world;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
    }

    /**
     * Tests equals object behavior.
     */
    public void testEqualsObject() {
        TrackConfiguration trackConfiguration = TrackConfiguration.getFlatInstance(TileTransition.NORTH);

        TrackType type0 = world.getTrackType(0);
        TrackType type4 = world.getTrackType(4);

        TrackPiece trackPiece1 = new TrackPiece(trackConfiguration, type0,0);
        assertEquals(trackPiece1, trackPiece1);

        TrackPiece trackPiece2 = new TrackPiece(trackConfiguration, type4,0);
        Assert.assertNotEquals(trackPiece1, trackPiece2);

        TestUtils.assertCloneBySerializationBehavesWell(trackPiece1);
        TestUtils.assertCloneBySerializationBehavesWell(trackPiece2);
    }

    /**
     * Tests that a new piece contains exactly the requested connection.
     */
    public void testCreate() {
        TrackType type = world.getTrackType(0);

        TrackPiece piece = TrackPiece.create(TileTransition.EAST, type, 3);

        assertEquals(TrackConfiguration.getFlatInstance(TileTransition.EAST), piece.trackConfiguration());
        assertEquals(type, piece.trackType());
        assertEquals(3, piece.playerId());
    }

    /**
     * Tests adding and removing connections while keeping the track type.
     */
    public void testAddAndRemoveConnections() {
        TrackType type = world.getTrackType(0);
        TrackPiece east = TrackPiece.create(TileTransition.EAST, type, 3);

        TrackPiece straight = east.withConnection(TileTransition.WEST, 3);
        assertEquals(TrackConfiguration.add(east.trackConfiguration(), TileTransition.WEST), straight.trackConfiguration());
        assertEquals(type, straight.trackType());

        assertEquals(east, straight.withoutConnection(TileTransition.WEST, 3).orElseThrow());
        assertTrue(east.withoutConnection(TileTransition.EAST, 3).isEmpty());
    }

    /**
     * Tests that changing the track type keeps the configuration.
     */
    public void testWithTrackType() {
        TrackPiece piece = TrackPiece.create(TileTransition.EAST, world.getTrackType(0), 3);
        TrackType other = world.getTrackType(4);

        TrackPiece changed = piece.withTrackType(other, 3);

        assertEquals(piece.trackConfiguration(), changed.trackConfiguration());
        assertEquals(other, changed.trackType());
    }
}

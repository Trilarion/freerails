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

package freerails.model.game;

import freerails.model.MapFixtureFactory2;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.model.world.World;
import junit.framework.TestCase;

import java.util.Optional;

/**
 * Tests the track ownership rules.
 */
public class RulesTest extends TestCase {

    private static final int ME = 0;
    private static final int THEM = 1;

    private TrackType trackType;
    private TrackType otherType;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        World world = MapFixtureFactory2.getCopy();
        trackType = world.getTrackType(0);
        otherType = world.getTrackType(4);
    }

    /**
     * Tests every ownership case for each combination of the rule flags.
     */
    public void testTrackChangeOwnershipTable() {
        for (boolean canConnect : new boolean[]{false, true}) {
            for (boolean canRemove : new boolean[]{false, true}) {
                Rules rules = new Rules(canConnect, false, canRemove);
                TrackPiece mine = TrackPiece.create(TileTransition.EAST, trackType, ME);
                TrackPiece theirs = TrackPiece.create(TileTransition.EAST, trackType, THEM);
                TrackPiece theirsStraight = theirs.withConnection(TileTransition.WEST);

                // New track belongs to its builder.
                assertAllowed(rules.checkTrackChange(ME, null, mine));
                assertRefused(rules.checkTrackChange(ME, null, theirs));

                // Own track may change freely.
                assertAllowed(rules.checkTrackChange(ME, mine, mine.withConnection(TileTransition.WEST)));
                assertAllowed(rules.checkTrackChange(ME, mine, mine.withTrackType(otherType)));
                assertAllowed(rules.checkTrackChange(ME, mine, null));

                // The owner never changes, whatever the rules.
                assertRefused(rules.checkTrackChange(ME, theirs,
                        new TrackPiece(theirs.trackConfiguration(), trackType, ME)));
                assertRefused(rules.checkTrackChange(ME, mine,
                        new TrackPiece(mine.trackConfiguration(), trackType, THEM)));

                // Another player's track.
                assertEquals(canConnect,
                        rules.checkTrackChange(ME, theirs, theirs.withConnection(TileTransition.WEST)).isEmpty());
                assertEquals(canRemove,
                        rules.checkTrackChange(ME, theirsStraight, theirs).isEmpty());
                assertEquals(canRemove, rules.checkTrackChange(ME, theirs, null).isEmpty());
                assertRefused(rules.checkTrackChange(ME, theirs, theirs.withTrackType(otherType)));
                TrackPiece rebuilt = theirsStraight.withoutConnection(TileTransition.EAST).orElseThrow()
                        .withConnection(TileTransition.NORTH);
                assertRefused(rules.checkTrackChange(ME, theirsStraight, rebuilt));
            }
        }
    }

    private static void assertAllowed(Optional<String> refusal) {
        assertTrue(refusal.orElse(""), refusal.isEmpty());
    }

    private static void assertRefused(Optional<String> refusal) {
        assertTrue(refusal.isPresent());
    }
}

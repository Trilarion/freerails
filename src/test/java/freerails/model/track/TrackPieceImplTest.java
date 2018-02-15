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
package freerails.model.track;

import freerails.util.TestUtils;
import freerails.model.MapFixtureFactory2;
import freerails.model.SKEY;
import freerails.model.terrain.TileTransition;
import freerails.model.world.World;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 *
 */
public class TrackPieceImplTest extends TestCase {

    private World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
    }

    /**
     *
     */
    public void testEqualsObject() {
        TrackConfiguration trackConfiguration = TrackConfiguration.getFlatInstance(TileTransition.NORTH);

        TrackRule rule0 = (TrackRule) world.get(SKEY.TRACK_RULES, 0);
        TrackRule rule4 = (TrackRule) world.get(SKEY.TRACK_RULES, 4);

        TrackPieceImpl trackPiece1 = new TrackPieceImpl(trackConfiguration, rule0, 0, 0);
        assertEquals(trackPiece1, trackPiece1);

        TrackPieceImpl trackPiece2 = new TrackPieceImpl(trackConfiguration, rule4, 0, 4);
        Assert.assertNotEquals(trackPiece1, trackPiece2);

        TestUtils.assertCloneBySerializationBehavesWell(trackPiece1);
        TestUtils.assertCloneBySerializationBehavesWell(trackPiece2);
    }
}

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
import freerails.model.terrain.TileTransition;
import freerails.model.world.World;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 *
 */
public class TrackPieceTest extends TestCase {

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

        TrackType type0 = world.getTrackType(0);
        TrackType type4 = world.getTrackType(4);

        TrackPiece trackPiece1 = new TrackPiece(trackConfiguration, type0,0);
        assertEquals(trackPiece1, trackPiece1);

        TrackPiece trackPiece2 = new TrackPiece(trackConfiguration, type4,0);
        Assert.assertNotEquals(trackPiece1, trackPiece2);

        TestUtils.assertCloneBySerializationBehavesWell(trackPiece1);
        TestUtils.assertCloneBySerializationBehavesWell(trackPiece2);
    }
}

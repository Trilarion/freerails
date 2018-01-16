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
package freerails.world.track;

import freerails.server.MapFixtureFactory2;
import freerails.util.Utils;
import freerails.world.SKEY;
import freerails.world.terrain.TileTransition;
import freerails.world.World;
import junit.framework.TestCase;

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
        world = MapFixtureFactory2.getCopy();
    }

    /**
     *
     */
    public void testEqualsObject() {
        TrackConfiguration tc1 = TrackConfiguration.getFlatInstance(TileTransition.NORTH);

        TrackRule rule0 = (TrackRule) world.get(SKEY.TRACK_RULES, 0);
        TrackRule rule4 = (TrackRule) world.get(SKEY.TRACK_RULES, 4);

        TrackPieceImpl tp1 = new TrackPieceImpl(tc1, rule0, 0, 0);
        assertEquals(tp1, tp1);
        TrackPieceImpl tp2 = new TrackPieceImpl(tc1, rule4, 0, 4);
        assertFalse(tp1.equals(tp2));
        TrackPieceImpl tp1Clone = (TrackPieceImpl) Utils
                .cloneBySerialisation(tp1);
        assertEquals(tp1, tp1Clone);
    }

}
